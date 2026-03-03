-- =============================================================================
-- HACKHUB - Schema PostgreSQL - Iterazione 1
-- Casi d'uso: Organizzare Hackathon, Inviare Sottomissione, Proclamare Vincitore
-- =============================================================================

-- Abilitazione estensione per la generazione di UUID lato DB
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- =============================================================================
-- ATTORI
-- =============================================================================

CREATE TABLE IF NOT EXISTS organizzatore (
    id      UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome    VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email   VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS giudice (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(100) NOT NULL,
    cognome     VARCHAR(100) NOT NULL,
    disponibile BOOLEAN     NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS mentore (
    id          UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    nome        VARCHAR(100) NOT NULL,
    cognome     VARCHAR(100) NOT NULL,
    disponibile BOOLEAN     NOT NULL DEFAULT TRUE
);

-- =============================================================================
-- HACKATHON (indirizzo embedded — no tabella separata per Address)
-- =============================================================================

CREATE TABLE IF NOT EXISTS hackathon (
    id                     UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    nome                   VARCHAR(255)  NOT NULL,
    data_inizio            TIMESTAMP     NOT NULL,
    data_fine              TIMESTAMP     NOT NULL,
    scadenza_iscrizioni    TIMESTAMP     NOT NULL,
    scadenza_sottomissioni TIMESTAMP     NOT NULL,
    premio                 DECIMAL(10,2) NOT NULL CHECK (premio >= 0),
    dimensione_max_team    INTEGER       NOT NULL CHECK (dimensione_max_team > 0),
    regolamento            TEXT          NOT NULL,
    -- Ciclo di vita: gestito dal pattern State, persistito come stringa
    stato                  VARCHAR(20)   NOT NULL DEFAULT 'IN_ISCRIZIONE'
                               CHECK (stato IN ('IN_ISCRIZIONE', 'IN_CORSO', 'IN_VALUTAZIONE', 'CONCLUSO')),
    id_organizzatore       UUID          NOT NULL REFERENCES organizzatore(id),
    id_giudice             UUID          NOT NULL REFERENCES giudice(id),
    -- Indirizzo fisico embedded (nullable: hackathon online non ha sede fisica)
    via                    VARCHAR(255),
    numero_civico          INTEGER,
    citta                  VARCHAR(100),
    cap                    VARCHAR(10),
    provincia              VARCHAR(50),
    -- Vincoli di coerenza temporale
    CONSTRAINT chk_date_ordine        CHECK (data_inizio < data_fine),
    CONSTRAINT chk_scadenza_iscrizioni CHECK (scadenza_iscrizioni < data_inizio),
    CONSTRAINT chk_scadenza_sottomissioni
        CHECK (scadenza_sottomissioni >= data_inizio AND scadenza_sottomissioni <= data_fine)
);

-- Tabella di associazione N:M Hackathon ↔ Mentore
CREATE TABLE IF NOT EXISTS hackathon_mentori (
    id_hackathon UUID NOT NULL REFERENCES hackathon(id) ON DELETE CASCADE,
    id_mentore   UUID NOT NULL REFERENCES mentore(id),
    PRIMARY KEY  (id_hackathon, id_mentore)
);

-- =============================================================================
-- TEAM E MEMBRI
-- =============================================================================

-- team.id_leader aggiunto come FK deferrable dopo la creazione di membro_team
-- per gestire la dipendenza circolare (team ↔ membro_team).
CREATE TABLE IF NOT EXISTS team (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome         VARCHAR(255) NOT NULL,
    descrizione  TEXT,
    id_leader    UUID,        -- FK verso membro_team.id, aggiunta sotto
    id_hackathon UUID         NOT NULL REFERENCES hackathon(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS membro_team (
    id      UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nome    VARCHAR(100) NOT NULL,
    cognome VARCHAR(100) NOT NULL,
    email   VARCHAR(255) NOT NULL UNIQUE,
    id_team UUID         NOT NULL REFERENCES team(id) ON DELETE CASCADE
);

-- FK leader con DEFERRABLE per permettere l'inserimento circolare
ALTER TABLE team
    ADD CONSTRAINT fk_team_leader
    FOREIGN KEY (id_leader) REFERENCES membro_team(id)
    DEFERRABLE INITIALLY DEFERRED;

-- =============================================================================
-- SOTTOMISSIONI
-- =============================================================================

CREATE TABLE IF NOT EXISTS sottomissione (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    link_repo    VARCHAR(500) NOT NULL,
    link_demo    VARCHAR(500),
    descrizione  TEXT,
    data_invio   TIMESTAMP    NOT NULL DEFAULT NOW(),
    id_team      UUID         NOT NULL REFERENCES team(id),
    id_hackathon UUID         NOT NULL REFERENCES hackathon(id),
    vincitore    BOOLEAN      NOT NULL DEFAULT FALSE,
    -- Un team può inviare una sola sottomissione per hackathon
    CONSTRAINT uq_sottomissione_team_hackathon UNIQUE (id_hackathon, id_team)
);

-- =============================================================================
-- VALUTAZIONI (inserite dal giudice; fuori scope iterazione 1, schema predisposto)
-- =============================================================================

CREATE TABLE IF NOT EXISTS valutazione (
    id               UUID          PRIMARY KEY DEFAULT gen_random_uuid(),
    voto             DECIMAL(5,2)  NOT NULL CHECK (voto >= 0 AND voto <= 100),
    giudizio_scritto TEXT,
    id_sottomissione UUID          NOT NULL UNIQUE REFERENCES sottomissione(id) ON DELETE CASCADE
);

-- =============================================================================
-- INDICI per le query più frequenti
-- =============================================================================

CREATE INDEX IF NOT EXISTS idx_hackathon_stato         ON hackathon(stato);
CREATE INDEX IF NOT EXISTS idx_sottomissione_hackathon ON sottomissione(id_hackathon);
CREATE INDEX IF NOT EXISTS idx_sottomissione_team      ON sottomissione(id_team);
CREATE INDEX IF NOT EXISTS idx_valutazione_sottomissione ON valutazione(id_sottomissione);
CREATE INDEX IF NOT EXISTS idx_membro_team_email       ON membro_team(email);
CREATE INDEX IF NOT EXISTS idx_team_hackathon          ON team(id_hackathon);
