package hackhub;

import hackhub.controller.HackathonController;
import hackhub.controller.ProclamazioneController;
import hackhub.controller.SottomissioneController;
import hackhub.infrastructure.db.DBConfig;
import hackhub.infrastructure.db.DBConnection;
import hackhub.repository.jdbc.JdbcGiudiceRepository;
import hackhub.repository.jdbc.JdbcHackathonRepository;
import hackhub.repository.jdbc.JdbcMentoreRepository;
import hackhub.repository.jdbc.JdbcOrganizzatoreRepository;
import hackhub.repository.jdbc.JdbcSottomissioneRepository;
import hackhub.repository.jdbc.JdbcTeamRepository;
import hackhub.service.HackathonService;
import hackhub.service.NotificationsService;
import hackhub.service.ProclamazioneService;
import hackhub.service.SottomissioneService;

/**
 * Punto di ingresso dell'applicazione HackHub (Java SE puro).
 *
 * Esegue il wiring manuale di tutte le dipendenze tramite Constructor Injection.
 * La configurazione del database viene letta da 'application.properties'
 * (escluso dal versionamento) con possibilità di override tramite variabili d'ambiente.
 *
 * Migrazione a Spring Boot: annotare le classi con @Service, @Repository, @Controller
 * e sostituire questo file con @SpringBootApplication. Nessun'altra modifica richiesta.
 */
public class HackHubApplication {

    private static final String CONFIG_FILE = "application.properties";

    public static void main(String[] args) {

        // --- Caricamento configurazione DB (nessuna credenziale hardcodata) ---
        DBConfig dbConfig   = DBConfig.fromClasspath(CONFIG_FILE);
        DBConnection db     = new DBConnection(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());

        // --- Repository (JDBC) ---
        JdbcHackathonRepository     hackathonRepository     = new JdbcHackathonRepository(db);
        JdbcGiudiceRepository       giudiceRepository       = new JdbcGiudiceRepository(db);
        JdbcMentoreRepository       mentoreRepository       = new JdbcMentoreRepository(db);
        JdbcOrganizzatoreRepository organizzatoreRepository = new JdbcOrganizzatoreRepository(db);
        JdbcTeamRepository          teamRepository          = new JdbcTeamRepository(db);
        JdbcSottomissioneRepository sottomissioneRepository = new JdbcSottomissioneRepository(db);

        // --- Service (Constructor Injection) ---
        NotificationsService notificationsService = new NotificationsService();

        HackathonService hackathonService = new HackathonService(
            hackathonRepository,
            giudiceRepository,
            mentoreRepository,
            organizzatoreRepository,
            notificationsService
        );

        SottomissioneService sottomissioneService = new SottomissioneService(
            hackathonRepository,
            sottomissioneRepository,
            teamRepository
        );

        ProclamazioneService proclamazioneService = new ProclamazioneService(
            hackathonRepository,
            sottomissioneRepository,
            teamRepository,
            notificationsService
        );

        // --- Controller (Constructor Injection) ---
        HackathonController     hackathonController     = new HackathonController(hackathonService);
        SottomissioneController sottomissioneController = new SottomissioneController(sottomissioneService);
        ProclamazioneController proclamazioneController = new ProclamazioneController(proclamazioneService);

        System.out.println("HackHub avviato. Connessione DB: " + dbConfig.getUrl());
    }
}
