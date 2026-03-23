CREATE
EXTENSION IF NOT EXISTS "pgcrypto";

DO
$$
BEGIN
CREATE TYPE stato_segnalazione AS ENUM ('PENDENTE', 'ACCETTATA', 'RIFIUTATA');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO
$$
BEGIN
CREATE TYPE stato_richiesta AS ENUM ('PENDENTE', 'PRESA_IN_CARICO', 'RESPINTA');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

DO
$$
BEGIN
CREATE TYPE stato_call AS ENUM ('PENDENTE', 'ACCETTATA', 'RIFIUTATA');
EXCEPTION WHEN duplicate_object THEN NULL;
END $$;

CREATE TABLE IF NOT EXISTS utente
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    nome VARCHAR
(
    100
) NOT NULL,
    cognome VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    hashed_password VARCHAR
(
    512
), -- NULL per utenti OAuth
    oauth_provider VARCHAR
(
    20
), -- GOOGLE | GITHUB | NULL
    oauth_external_id VARCHAR
(
    512
), -- ID univoco presso il provider OAuth
    CONSTRAINT chk_credenziali CHECK
(
    hashed_password
    IS
    NOT
    NULL
    OR
(
    oauth_provider
    IS
    NOT
    NULL
    AND
    oauth_external_id
    IS
    NOT
    NULL
)
    ),
    CONSTRAINT uq_oauth_identity UNIQUE
(
    oauth_provider,
    oauth_external_id
)
    );

-- ATTORI (staff della piattaforma — non sono Utenti registrati)

CREATE TABLE IF NOT EXISTS organizzatore
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    nome VARCHAR
(
    100
) NOT NULL,
    cognome VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE
    );

CREATE TABLE IF NOT EXISTS giudice
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    nome VARCHAR
(
    100
) NOT NULL,
    cognome VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    disponibile BOOLEAN NOT NULL DEFAULT TRUE
    );

CREATE TABLE IF NOT EXISTS mentore
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    nome VARCHAR
(
    100
) NOT NULL,
    cognome VARCHAR
(
    100
) NOT NULL,
    email VARCHAR
(
    255
) NOT NULL UNIQUE,
    disponibile BOOLEAN NOT NULL DEFAULT TRUE
    );

-- HACKATHON (indirizzo embedded — no tabella separata per Address)

CREATE TABLE IF NOT EXISTS hackathon
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    nome VARCHAR
(
    255
) NOT NULL,
    data_inizio TIMESTAMP NOT NULL,
    data_fine TIMESTAMP NOT NULL,
    scadenza_iscrizioni TIMESTAMP NOT NULL,
    scadenza_sottomissioni TIMESTAMP NOT NULL,
    premio DECIMAL
(
    10,
    2
) NOT NULL CHECK
(
    premio
    >=
    0
),
    dimensione_max_team INTEGER NOT NULL CHECK
(
    dimensione_max_team >
    0
),
    regolamento TEXT NOT NULL,
    -- Ciclo di vita: gestito dal pattern State, persistito come stringa
    stato VARCHAR
(
    20
) NOT NULL DEFAULT 'IN_ISCRIZIONE'
    CHECK
(
    stato
    IN
(
    'IN_ISCRIZIONE',
    'IN_CORSO',
    'IN_VALUTAZIONE',
    'CONCLUSO'
)),
    id_organizzatore UUID NOT NULL REFERENCES organizzatore
(
    id
),
    id_giudice UUID NOT NULL REFERENCES giudice
(
    id
),
    -- Indirizzo fisico embedded (nullable: hackathon online non ha sede fisica)
    via VARCHAR
(
    255
),
    numero_civico INTEGER,
    citta VARCHAR
(
    100
),
    cap VARCHAR
(
    10
),
    provincia VARCHAR
(
    50
),
    -- Team vincitore (NULL finché l'hackathon non è CONCLUSO)
    id_team_vincitore UUID REFERENCES team
(
    id
),
    -- Flag che indica se il premio è stato erogato tramite il gateway di pagamento
    premio_disbursed BOOLEAN NOT NULL DEFAULT FALSE,
    -- Vincoli di coerenza temporale
    CONSTRAINT chk_date_ordine CHECK
(
    data_inizio <
    data_fine
),
    CONSTRAINT chk_scadenza_iscrizioni CHECK
(
    scadenza_iscrizioni <
    data_inizio
),
    CONSTRAINT chk_scadenza_sottomissioni
    CHECK
(
    scadenza_sottomissioni
    >=
    data_inizio
    AND
    scadenza_sottomissioni
    <=
    data_fine
)
    );

-- Aggiunge le colonne di hackathon se la tabella esiste già (upgrade sicuro)
DO
$$
BEGIN
ALTER TABLE hackathon
    ADD COLUMN id_team_vincitore UUID REFERENCES team (id);
EXCEPTION WHEN duplicate_column THEN NULL;
END $$;

DO
$$
BEGIN
ALTER TABLE hackathon
    ADD COLUMN premio_disbursed BOOLEAN NOT NULL DEFAULT FALSE;
EXCEPTION WHEN duplicate_column THEN NULL;
END $$;

-- Tabella di associazione N:M Hackathon ↔ Mentore
CREATE TABLE IF NOT EXISTS hackathon_mentori
(
    id_hackathon
    UUID
    NOT
    NULL
    REFERENCES
    hackathon
(
    id
) ON DELETE CASCADE,
    id_mentore UUID NOT NULL REFERENCES mentore
(
    id
),
    PRIMARY KEY
(
    id_hackathon,
    id_mentore
)
    );

-- TEAM E MEMBRI
CREATE TABLE IF NOT EXISTS team
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    nome VARCHAR
(
    255
) NOT NULL,
    descrizione TEXT,
    id_leader UUID NOT NULL REFERENCES utente
(
    id
),
    id_hackathon UUID NOT NULL REFERENCES hackathon
(
    id
) ON DELETE CASCADE,
    squalificato BOOLEAN NOT NULL DEFAULT FALSE
    );

DO
$$
BEGIN
ALTER TABLE team
    ADD COLUMN squalificato BOOLEAN NOT NULL DEFAULT FALSE;
EXCEPTION WHEN duplicate_column THEN NULL;
END $$;

-- Associazione N:M Utente ↔ Team (membri accettati)
-- Un utente può essere membro di al più un team per hackathon (vincolo applicato a livello servizio)
CREATE TABLE IF NOT EXISTS membro_team
(
    id_utente
    UUID
    NOT
    NULL
    REFERENCES
    utente
(
    id
) ON DELETE CASCADE,
    id_team UUID NOT NULL REFERENCES team
(
    id
)
  ON DELETE CASCADE,
    PRIMARY KEY
(
    id_utente,
    id_team
)
    );

-- INVITI (gestione accettazione/rifiuto dei membri proposti dal leader)
CREATE TABLE IF NOT EXISTS invito
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    id_team UUID NOT NULL REFERENCES team
(
    id
) ON DELETE CASCADE,
    id_utente UUID NOT NULL REFERENCES utente
(
    id
)
  ON DELETE CASCADE,
    id_hackathon UUID NOT NULL REFERENCES hackathon
(
    id
),
    stato VARCHAR
(
    20
) NOT NULL DEFAULT 'IN_ATTESA'
    CHECK
(
    stato
    IN
(
    'IN_ATTESA',
    'ACCETTATO',
    'RIFIUTATO'
)),
    data_invio TIMESTAMP NOT NULL DEFAULT NOW
(
),
    -- Un utente riceve al più un invito per team
    CONSTRAINT uq_invito_team_utente UNIQUE
(
    id_team,
    id_utente
)
    );

-- SOTTOMISSIONI
CREATE TABLE IF NOT EXISTS sottomissione
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    link_repo VARCHAR
(
    500
) NOT NULL,
    link_demo VARCHAR
(
    500
),
    descrizione TEXT,
    data_invio TIMESTAMP NOT NULL DEFAULT NOW
(
),
    id_team UUID NOT NULL REFERENCES team
(
    id
),
    id_hackathon UUID NOT NULL REFERENCES hackathon
(
    id
),
    vincitore BOOLEAN NOT NULL DEFAULT FALSE,
    valutato BOOLEAN NOT NULL DEFAULT FALSE,
    -- Un team può inviare una sola sottomissione per hackathon
    CONSTRAINT uq_sottomissione_team_hackathon UNIQUE
(
    id_hackathon,
    id_team
)
    );

-- VALUTAZIONI (inserite dal giudice)
CREATE TABLE IF NOT EXISTS valutazione
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    voto DECIMAL
(
    4,
    2
) NOT NULL CHECK
(
    voto
    >=
    0
    AND
    voto
    <=
    10
),
    giudizio_scritto TEXT,
    id_sottomissione UUID NOT NULL UNIQUE REFERENCES sottomissione
(
    id
) ON DELETE CASCADE,
    id_giudice UUID NOT NULL REFERENCES giudice
(
    id
)
    );

-- SEGNALAZIONI (di violazione del regolamento — inserite dal mentore)
CREATE TABLE IF NOT EXISTS segnalazione
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    id_team UUID NOT NULL REFERENCES team
(
    id
),
    id_mentore UUID NOT NULL REFERENCES mentore
(
    id
),
    id_hackathon UUID NOT NULL REFERENCES hackathon
(
    id
),
    descrizione TEXT NOT NULL,
    prove TEXT,
    data_invio TIMESTAMP NOT NULL DEFAULT NOW
(
),
    stato VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDENTE'
    CHECK
(
    stato
    IN
(
    'PENDENTE',
    'ACCETTATA',
    'RIFIUTATA'
))
    );

-- RICHIESTE DI SUPPORTO (inviate dal leader del team al mentore)
CREATE TABLE IF NOT EXISTS richiesta_supporto
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    id_team UUID NOT NULL REFERENCES team
(
    id
),
    id_hackathon UUID NOT NULL REFERENCES hackathon
(
    id
),
    id_mentore UUID NOT NULL REFERENCES mentore
(
    id
),
    motivo TEXT NOT NULL,
    data_invio TIMESTAMP NOT NULL DEFAULT NOW
(
),
    stato VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDENTE'
    CHECK
(
    stato
    IN
(
    'PENDENTE',
    'PRESA_IN_CARICO',
    'RESPINTA'
)),
    livello_urgenza VARCHAR
(
    20
) NOT NULL DEFAULT 'NORMALE'
    CHECK
(
    livello_urgenza
    IN
(
    'BASSA',
    'NORMALE',
    'ALTA'
)),
    motivazione_rifiuto TEXT
    );

-- CALL PIANIFICATE (tra mentore e team, generate dopo accettazione supporto)
CREATE TABLE IF NOT EXISTS call_pianificata
(
    id
    UUID
    PRIMARY
    KEY
    DEFAULT
    gen_random_uuid
(
),
    id_richiesta_supporto UUID NOT NULL REFERENCES richiesta_supporto
(
    id
),
    data_call DATE NOT NULL,
    ora_call TIME NOT NULL,
    link_call VARCHAR
(
    500
) NOT NULL,
    descrizione TEXT,
    data_creazione TIMESTAMP NOT NULL DEFAULT NOW
(
),
    stato VARCHAR
(
    20
) NOT NULL DEFAULT 'PENDENTE'
    CHECK
(
    stato
    IN
(
    'PENDENTE',
    'ACCETTATA',
    'RIFIUTATA'
))
    );

-- INDICI per le query più frequenti
CREATE INDEX IF NOT EXISTS idx_utente_email ON utente(email);
CREATE INDEX IF NOT EXISTS idx_hackathon_stato ON hackathon(stato);
CREATE INDEX IF NOT EXISTS idx_team_hackathon ON team(id_hackathon);
CREATE INDEX IF NOT EXISTS idx_membro_team_utente ON membro_team(id_utente);
CREATE INDEX IF NOT EXISTS idx_invito_team ON invito(id_team);
CREATE INDEX IF NOT EXISTS idx_invito_utente_hackathon ON invito(id_utente, id_hackathon);
CREATE INDEX IF NOT EXISTS idx_sottomissione_hackathon ON sottomissione(id_hackathon);
CREATE INDEX IF NOT EXISTS idx_sottomissione_team ON sottomissione(id_team);
CREATE INDEX IF NOT EXISTS idx_valutazione_sottomissione ON valutazione(id_sottomissione);
CREATE INDEX IF NOT EXISTS idx_segnalazione_hackathon ON segnalazione(id_hackathon);
CREATE INDEX IF NOT EXISTS idx_richiesta_supporto_mentore ON richiesta_supporto(id_mentore);
CREATE INDEX IF NOT EXISTS idx_richiesta_supporto_team ON richiesta_supporto(id_team);
CREATE INDEX IF NOT EXISTS idx_call_richiesta ON call_pianificata(id_richiesta_supporto);
