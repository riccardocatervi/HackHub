![logoHackHub](https://github.com/user-attachments/assets/21fc1067-8548-4c28-b175-458cdca4616b)
# HackHub

> Piattaforma di gestione hackathon sviluppata come progetto universitario per il corso di **Ingegneria del Software** — Università di Camerino.

**Autori:** Riccardo Catervi, Alice Massetani — Anno Accademico 2025/2026

---

## Indice / Table of Contents

- 🚀 **[Setup e Avvio (IT)](#setup-e-avvio)** — Come avviare il progetto
- 🚀 **[Setup and Running (EN)](#setup-and-running)** — How to run the project
- [Italiano](#-hackhub--italiano)
- [English](#-hackhub--english)

---

# 🇮🇹 HackHub — Italiano

## Descrizione del Progetto

**HackHub** è una piattaforma software per la gestione completa del ciclo di vita di un hackathon. Il sistema supporta i principali attori coinvolti — organizzatori, partecipanti (iscritti a team), mentori e giudici — e copre tutte le fasi operative: dalla pubblicazione e iscrizione, alla sottomissione dei progetti, alla valutazione e proclamazione del vincitore.

Il progetto è stato sviluppato nell'ambito del corso di **Ingegneria del Software** presso l'**Università di Camerino**, con l'obiettivo esplicito di emulare il **Processo Unificato (Unified Process, UP)** così come presentato durante il corso. Non si tratta di un'implementazione commerciale, bensì di un esercizio accademico volto a mettere in pratica i principi dell'ingegneria del software orientata agli oggetti: uso sistematico degli use case, iterazioni incrementali, modellazione UML e applicazione dei principi di progettazione GRASP e dei pattern GoF.

## Contesto Accademico e Metodologia

### Processo Unificato

Lo sviluppo ha seguito il modello iterativo e incrementale del Processo Unificato, strutturato in cinque iterazioni. Per ogni iterazione sono stati prodotti:

- **Diagramma dei casi d'uso** aggiornato
- **Diagramma delle classi** (analisi e progettazione)
- **Diagrammi di sequenza** per i casi d'uso principali dell'iterazione
- **Implementazione Java** corrispondente

Il file UML `uml/HackHub.vpp` (formato Visual Paradigm) raccoglie l'intera evoluzione del modello attraverso le iterazioni, ed è possibile analizzarvi la progressiva maturazione del sistema: dall'analisi dei requisiti alla progettazione dettagliata.

Le quattro discipline del Processo Unificato attive durante lo sviluppo sono state:

| Disciplina | Attività principali |
|---|---|
| Ingegneria dei requisiti | Elicitazione, casi d'uso fully dressed, prioritizzazione MoSCoW |
| Analisi e progettazione | Diagrammi delle classi, pattern GRASP e GoF, diagrammi di sequenza |
| Implementazione | Codice Java, repository JDBC, gestione delle eccezioni |
| Test | Unit test con JUnit 5 e Mockito, validazione dei service layer |

### Iterazioni

| Iterazione | Casi d'uso implementati |
|---|---|
| 1 | Organizzare un hackathon · Proclamare team vincitori di un hackathon · Inviare sottomissione del team |
| 2 | Registrarsi alla piattaforma · Creare team per un hackathon · Valutare sottomissione di un team · Segnalare violazione del regolamento di un team |
| 3 | Gestire penalizzazione o squalifica di un team · Erogare premio al team vincitore · Modifica sottomissione del team · Inviare richiesta di supporto a un mentore |
| 4 | Accettare invito a unirsi a un team · Prendere in carico richiesta di supporto · Pianificare call con un team · Gestire iscrizione al team · Gestire iscrizione del team all'hackathon |
| 5 | Gestire mentori di un hackathon · Autenticarsi alla piattaforma · Invitare utente a unirsi a un team · Visualizzare team di appartenenza · Visualizzare hackathon a cui l'utente è iscritto · Gestire invito a call da parte di un mentore · Visualizzare informazioni pubbliche sugli hackathon |

### Test

I test di unità sono stati scritti prevalentemente con il supporto di modelli linguistici (LLM), successivamente validati e corretti manualmente per garantirne la correttezza e la coerenza con il comportamento atteso del sistema. La suite copre principalmente il livello dei service e delle state machine.

---

## Funzionalità Principali

### Organizzatore
- Creazione e gestione di hackathon (titolo, date, premi, capienza team)
- Transizione dello stato dell'hackathon (iscrizioni → in corso → valutazione → concluso)
- Gestione dei mentori assegnati all'hackathon
- Proclamazione del vincitore ed erogazione del premio

### Partecipante / Team
- Registrazione alla piattaforma (locale o OAuth2)
- Visualizzazione hackathon disponibili e dettagli
- Creazione e abbandono del team; gestione degli inviti
- Iscrizione e cancellazione dell'iscrizione all'hackathon
- Sottomissione e aggiornamento del progetto
- Invio di richieste di supporto ai mentori; risposta agli inviti di call

### Mentore
- Visualizzazione e presa in carico delle richieste di supporto
- Pianificazione di call con i team; gestione della risposta all'invito

### Giudice
- Valutazione delle sottomissioni dei team

---

## Architettura

### Struttura a Strati

Il sistema adotta un'architettura a strati classica, rispecchiando le raccomandazioni del Processo Unificato per la separazione delle responsabilità:

```
Controller  →  Service  →  Repository  →  Model / Entity
```

- **Controller**: coordinatori GRASP (Use Case Controller); ricevono le richieste e delegano ai service
- **Service**: logica applicativa e di dominio; orchestrano repository, entità e observer
- **Repository**: astrazione dell'accesso ai dati; implementazioni JDBC senza ORM
- **Model/Entity**: entità di dominio POJO; DTO immutabili come Java records

### Pattern GRASP Applicati

| Pattern | Applicazione nel progetto |
|---|---|
| **Controller** | Ogni caso d'uso ha un controller dedicato (variante *Use Case Controller*); es. `InvitationController`, `HackathonRegistrationController` |
| **Information Expert** | Le entità gestiscono la propria logica interna; es. `Team.hasMember()`, `Hackathon.verificaAccettaSottomissione()` |
| **High Cohesion** | Ogni service è responsabile di un singolo caso d'uso o di un dominio ben delimitato |
| **Low Coupling** | Dipendenze iniettate tramite constructor injection; uso di interfacce repository e observer |
| **Pure Fabrication** | `SessionManager` (gestione sessioni in-memory), `MockCalendarService`, `MockPaymentProviderGateway` |

### Pattern GoF Applicati

#### State — Ciclo di Vita dell'Hackathon

Il ciclo di vita dell'hackathon è modellato con il pattern **State**. L'entità `Hackathon` funge da Context e delega le operazioni sensibili allo stato all'implementazione corrente di `StatoHackathon`:

```
StatoInIscrizione → StatoInCorso → StatoInValutazione → StatoConcluso
```

Ogni stato definisce quali operazioni sono permesse (es. accettare iscrizioni, sottomissioni, richieste di supporto) e lancia eccezioni appropriate per transizioni non valide.

#### Observer — Notifiche e Reattività

Il pattern **Observer** è applicato trasversalmente per disaccoppiare i service dalle logiche di notifica. Sono presenti nove interfacce observer distinte:

- `SegnalazioneObserver`, `GestioneSegnalazioneObserver`
- `ValutazioneObserver`
- `InvitazioneObserver`, `NuovoInvitoObserver`
- `RichiestaSupportoObserver`, `GestioneRichiestaSupportoObserver`
- `GestioneMentoriObserver`
- `RispostaCallObserver`

`NotificationsService` implementa tutte le interfacce observer, centralizzando la gestione delle notifiche.

#### Builder — Creazione del Team

`EquipeBuilder` implementa il pattern **Builder** per la costruzione controllata di un team, restituendo un `EquipeCreationResult` che incapsula il team creato e i dati correlati.

### Dependency Injection

Tutte le dipendenze sono iniettate tramite costruttore (*Constructor Injection*), rendendo il codice testabile e pronto per un container DI come Spring (cfr. il porting descritto di seguito).

---

## Stack Tecnologico

| Componente | Tecnologia |
|---|---|
| Linguaggio | Java 21 |
| Build tool | Gradle |
| Database | PostgreSQL |
| Accesso ai dati | JDBC puro (nessun ORM) |
| Framework web | Nessuno (pure Java) / Spring Boot 3.4.4 (porting) |
| Testing | JUnit 5, Mockito |
| UML | Visual Paradigm (`.vpp`) |

---

## Struttura del Repository

```
HackHub/
├── code_pure_java/                  # Implementazione principale (Java puro + JDBC)
│   └── src/
│       ├── main/java/hackhub/
│       │   ├── controller/          # GRASP Controller (Use Case Controller)
│       │   ├── service/             # Business logic
│       │   │   └── observer/        # Interfacce Observer (GoF)
│       │   ├── repository/          # Interfacce repository
│       │   │   └── jdbc/            # Implementazioni JDBC
│       │   ├── model/
│       │   │   ├── entity/          # Entità di dominio
│       │   │   └── state/           # Pattern State (ciclo di vita hackathon)
│       │   ├── dto/                 # Record immutabili
│       │   ├── exception/           # RuntimeException specializzate
│       │   ├── payment/             # Gateway pagamento (interfaccia + mock)
│       │   └── infrastructure/      # DBConnection, DBConfig
│       └── main/resources/
│           └── schema.sql
├── code_porting_springboot/         # Porting Spring Boot 3.4.4
│   └── src/main/resources/
│       ├── application.properties.example   # ← copiare in application.properties
│       └── schema.sql
├── uml/
│   └── HackHub.vpp                  # Modello UML Visual Paradigm (tutte le iterazioni)
└── README.md
```

---

## Diagrammi UML

Il file `uml/HackHub.vpp` contiene l'intero modello UML del progetto, organizzato per iterazione:

- **Diagrammi dei casi d'uso**: panoramica degli attori e delle funzionalità per iterazione
- **Diagrammi delle classi di analisi**: identificazione delle entità concettuali e delle relazioni
- **Diagrammi delle classi di progettazione**: struttura tecnica con package, interfacce e dipendenze
- **Diagrammi di sequenza**: flusso di interazione per i principali scenari di ogni iterazione

Per visualizzare il file è necessario **Visual Paradigm** (edizione Community o superiore).

---

## Setup e Avvio

### Prerequisiti

- Java 21+
- Gradle 8+
- PostgreSQL 13+

---

### ▶ Porting Spring Boot 3.4.4 — unica versione avviabile

> **Il file `application.properties` non è incluso nel repository** (contiene credenziali).
> È obbligatorio crearlo prima di avviare l'applicazione.

**Passo 1 — Creare il database PostgreSQL**

```sql
CREATE DATABASE hackhub;
```

**Passo 2 — Creare il file di configurazione**

```bash
cd code_porting_springboot/src/main/resources
cp application.properties.example application.properties
```

Aprire `application.properties` e sostituire `YOUR_POSTGRES_USER` e `YOUR_POSTGRES_PASSWORD` con le proprie credenziali PostgreSQL.

**Passo 3 — Avviare l'applicazione**

```bash
cd code_porting_springboot
./gradlew bootRun
```

> Spring Boot crea automaticamente tutte le tabelle all'avvio tramite `schema.sql`.
> Non è necessario eseguire lo script manualmente.
> Il server si avvia sulla porta **8080**.

---

### Implementazione Java puro — non avviabile

> L'implementazione in `code_pure_java` non espone un server HTTP avviabile.
> Contiene la logica di dominio completa (service, repository JDBC, pattern State/Observer)
> sviluppata nelle prime iterazioni del progetto; il porting in Spring Boot è stato richiesto
> esplicitamente come deliverable finale del corso per fornire un'applicazione eseguibile.

Per compilare e lanciare i test:

```bash
cd code_pure_java
./gradlew build
./gradlew test
```

---

---

# 🇬🇧 HackHub — English

## Project Description

**Authors:** Riccardo Catervi, Alice Massetani — Academic Year 2025/2026

**HackHub** is a software platform for the complete lifecycle management of a hackathon. The system supports the main actors involved — organizers, participants (members of teams), mentors, and judges — and covers all operational phases: from publication and registration, through project submission, to evaluation and winner announcement.

The project was developed as part of the **Software Engineering** course at the **University of Camerino**, with the explicit goal of emulating the **Unified Process (UP)** as taught in the course. This is not a commercial implementation but an academic exercise aimed at applying the principles of object-oriented software engineering in practice: systematic use of use cases, incremental iterations, UML modeling, and the application of GRASP design principles and GoF patterns.

## Academic Context and Methodology

### Unified Process

Development followed the iterative and incremental model of the Unified Process, structured in five iterations. For each iteration, the following artifacts were produced:

- **Updated use case diagram**
- **Class diagram** (analysis and design)
- **Sequence diagrams** for the iteration's primary use cases
- **Corresponding Java implementation**

The UML file `uml/HackHub.vpp` (Visual Paradigm format) captures the full evolution of the model across iterations, and it is possible to observe the progressive maturation of the system — from requirements analysis through to detailed design.

The four Unified Process disciplines active during development were:

| Discipline | Key activities |
|---|---|
| Requirements engineering | Elicitation, fully dressed use cases, MoSCoW prioritization |
| Analysis & design | Class diagrams, GRASP and GoF patterns, sequence diagrams |
| Implementation | Java code, JDBC repositories, exception handling |
| Testing | Unit tests with JUnit 5 and Mockito, service layer validation |

### Iterations

| Iteration | Implemented use cases |
|---|---|
| 1 | Organize a hackathon · Proclaim winning teams of a hackathon · Submit team project |
| 2 | Register on the platform · Create a team for a hackathon · Evaluate a team's submission · Report a team's regulation violation |
| 3 | Manage team penalty or disqualification · Disburse prize to winning team · Update team submission · Send support request to a mentor |
| 4 | Accept invitation to join a team · Take charge of a support request · Schedule a call with a team · Manage team membership · Manage team registration for a hackathon |
| 5 | Manage hackathon mentors · Authenticate on the platform · Invite a user to join a team · View team membership · View hackathons the user is enrolled in · Manage call invitation as a mentor · View public hackathon information |

### Testing

Unit tests were written primarily with the assistance of large language models (LLMs) and subsequently validated and manually corrected to ensure correctness and consistency with the system's expected behavior. The test suite covers mainly the service layer and state machine logic.

---

## Key Features

### Organizer
- Create and manage hackathons (title, dates, prize, team capacity)
- Hackathon state transitions (registration → ongoing → evaluation → concluded)
- Manage mentors assigned to the hackathon
- Proclaim the winner and disburse the prize

### Participant / Team
- Platform registration (local or OAuth2)
- Browse available hackathons and view details
- Create and leave teams; manage invitations
- Register for and unregister from a hackathon
- Submit and update the team project
- Send support requests to mentors; respond to call invitations

### Mentor
- View and take charge of support requests
- Schedule calls with teams; handle call invitation responses

### Judge
- Evaluate team submissions

---

## Architecture

### Layered Structure

The system adopts a classic layered architecture, reflecting the Unified Process recommendations for separation of concerns:

```
Controller  →  Service  →  Repository  →  Model / Entity
```

- **Controller**: GRASP coordinators (Use Case Controller); receive requests and delegate to services
- **Service**: application and domain logic; orchestrate repositories, entities, and observers
- **Repository**: data access abstraction; JDBC implementations with no ORM
- **Model/Entity**: POJO domain entities; immutable DTOs as Java records

### GRASP Patterns Applied

| Pattern | Application in the project |
|---|---|
| **Controller** | Each use case has a dedicated controller (*Use Case Controller* variant); e.g. `InvitationController`, `HackathonRegistrationController` |
| **Information Expert** | Entities manage their own internal logic; e.g. `Team.hasMember()`, `Hackathon.verificaAccettaSottomissione()` |
| **High Cohesion** | Each service is responsible for a single use case or a well-delimited domain |
| **Low Coupling** | Dependencies injected via constructor injection; use of repository and observer interfaces |
| **Pure Fabrication** | `SessionManager` (in-memory session management), `MockCalendarService`, `MockPaymentProviderGateway` |

### GoF Patterns Applied

#### State — Hackathon Lifecycle

The hackathon lifecycle is modeled with the **State** pattern. The `Hackathon` entity acts as the Context and delegates state-sensitive operations to the current `StatoHackathon` implementation:

```
StatoInIscrizione → StatoInCorso → StatoInValutazione → StatoConcluso
```

Each state defines which operations are permitted (e.g. accepting registrations, submissions, support requests) and throws appropriate exceptions for invalid transitions.

#### Observer — Notifications and Reactivity

The **Observer** pattern is applied throughout to decouple services from notification logic. Nine distinct observer interfaces are present:

- `SegnalazioneObserver`, `GestioneSegnalazioneObserver`
- `ValutazioneObserver`
- `InvitazioneObserver`, `NuovoInvitoObserver`
- `RichiestaSupportoObserver`, `GestioneRichiestaSupportoObserver`
- `GestioneMentoriObserver`
- `RispostaCallObserver`

`NotificationsService` implements all observer interfaces, centralizing notification management.

#### Builder — Team Creation

`EquipeBuilder` implements the **Builder** pattern for the controlled construction of a team, returning an `EquipeCreationResult` that encapsulates the created team and related data.

### Dependency Injection

All dependencies are injected via constructor (*Constructor Injection*), making the code testable and ready for a DI container such as Spring (see the porting described below).

---

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 21 |
| Build tool | Gradle |
| Database | PostgreSQL |
| Data access | Plain JDBC (no ORM) |
| Web framework | None (pure Java) / Spring Boot 3.4.4 (porting) |
| Testing | JUnit 5, Mockito |
| UML | Visual Paradigm (`.vpp`) |

---

## Repository Structure

```
HackHub/
├── code_pure_java/                  # Main implementation (pure Java + JDBC)
│   └── src/
│       ├── main/java/hackhub/
│       │   ├── controller/          # GRASP Controller (Use Case Controller)
│       │   ├── service/             # Business logic
│       │   │   └── observer/        # Observer interfaces (GoF)
│       │   ├── repository/          # Repository interfaces
│       │   │   └── jdbc/            # JDBC implementations
│       │   ├── model/
│       │   │   ├── entity/          # Domain entities
│       │   │   └── state/           # State pattern (hackathon lifecycle)
│       │   ├── dto/                 # Immutable records
│       │   ├── exception/           # Specialized RuntimeExceptions
│       │   ├── payment/             # Payment gateway (interface + mock)
│       │   └── infrastructure/      # DBConnection, DBConfig
│       └── main/resources/
│           └── schema.sql
├── code_porting_springboot/         # Spring Boot 3.4.4 porting
│   └── src/main/resources/
│       ├── application.properties.example   # ← copy to application.properties
│       └── schema.sql
├── uml/
│   └── HackHub.vpp                  # Visual Paradigm UML model (all iterations)
└── README.md
```

---

## UML Diagrams

The file `uml/HackHub.vpp` contains the complete UML model of the project, organized by iteration:

- **Use case diagrams**: overview of actors and features per iteration
- **Analysis class diagrams**: identification of conceptual entities and relationships
- **Design class diagrams**: technical structure with packages, interfaces and dependencies
- **Sequence diagrams**: interaction flow for the main scenarios of each iteration

**Visual Paradigm** (Community edition or higher) is required to open the file.

---

## Setup and Running

### Prerequisites

- Java 21+
- Gradle 8+
- PostgreSQL 13+

---

### ▶ Spring Boot 3.4.4 porting — only runnable version

> **The `application.properties` file is not included in the repository** (it contains credentials).
> It must be created before starting the application.

**Step 1 — Create the PostgreSQL database**

```sql
CREATE DATABASE hackhub;
```

**Step 2 — Create the configuration file**

```bash
cd code_porting_springboot/src/main/resources
cp application.properties.example application.properties
```

Open `application.properties` and replace `YOUR_POSTGRES_USER` and `YOUR_POSTGRES_PASSWORD` with your PostgreSQL credentials.

**Step 3 — Start the application**

```bash
cd code_porting_springboot
./gradlew bootRun
```

> Spring Boot automatically creates all tables on startup via `schema.sql`.
> There is no need to run the script manually.
> The server starts on port **8080**.

---

### Pure Java implementation — not runnable

> The `code_pure_java` implementation does not expose a runnable HTTP server.
> It contains the complete domain logic (services, JDBC repositories, State/Observer patterns)
> developed during the earlier project iterations; the Spring Boot porting was explicitly required
> as the final course deliverable to provide a runnable application.

To compile and run the tests:

```bash
cd code_pure_java
./gradlew build
./gradlew test
```
