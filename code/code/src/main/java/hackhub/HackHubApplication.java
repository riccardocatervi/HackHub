package hackhub;

import hackhub.controller.*;
import hackhub.infrastructure.db.DBConfig;
import hackhub.infrastructure.db.DBConnection;
import hackhub.repository.jdbc.*;
import hackhub.service.*;

/**
 * Punto di ingresso dell'applicazione HackHub (Java SE puro).
 * <p>
 * Esegue il wiring manuale di tutte le dipendenze tramite Constructor Injection.
 * La configurazione del database viene letta da 'application.properties'
 * (escluso dal versionamento) con possibilità di override tramite variabili d'ambiente.
 * <p>
 * Migrazione a Spring Boot: annotare le classi con @Service, @Repository, @Controller
 * e sostituire questo file con @SpringBootApplication. Nessun'altra modifica richiesta.
 */
public class HackHubApplication {

    private static final String CONFIG_FILE = "application.properties";

    public static void main(String[] args) {

        // --- Caricamento configurazione DB (nessuna credenziale hardcodata) ---
        DBConfig dbConfig = DBConfig.fromClasspath(CONFIG_FILE);
        DBConnection db = new DBConnection(dbConfig.getUrl(), dbConfig.getUsername(), dbConfig.getPassword());

        // --- Repository (JDBC) ---
        JdbcHackathonRepository hackathonRepository = new JdbcHackathonRepository(db);
        JdbcGiudiceRepository giudiceRepository = new JdbcGiudiceRepository(db);
        JdbcMentoreRepository mentoreRepository = new JdbcMentoreRepository(db);
        JdbcOrganizzatoreRepository organizzatoreRepository = new JdbcOrganizzatoreRepository(db);
        JdbcTeamRepository teamRepository = new JdbcTeamRepository(db);
        JdbcSottomissioneRepository sottomissioneRepository = new JdbcSottomissioneRepository(db);
        JdbcUserRepository userRepository = new JdbcUserRepository(db);
        JdbcInvitoRepository invitoRepository = new JdbcInvitoRepository(db);
        JdbcSegnalazioneRepository segnalazioneRepository = new JdbcSegnalazioneRepository(db);
        JdbcValutazioneRepository valutazioneRepository = new JdbcValutazioneRepository(db);

        // --- Servizi di supporto ---
        NotificationsService notificationsService = new NotificationsService();
        PasswordEncoder passwordEncoder = new PasswordEncoder();
        OAuthService oauthService = new OAuthService();

        // --- Service layer (Constructor Injection) ---
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

        UtenteService utenteService = new UtenteService(
                userRepository,
                passwordEncoder
        );

        TeamService teamService = new TeamService(
                hackathonRepository,
                teamRepository,
                userRepository,
                invitoRepository,
                notificationsService
        );

        // Segnalazione: registra NotificationsService come observer (pattern GoF Observer)
        SegnalazioneService segnalazioneService = new SegnalazioneService(
                segnalazioneRepository,
                teamRepository,
                hackathonRepository
        );
        segnalazioneService.addObserver(notificationsService);

        // Valutazione: registra NotificationsService come observer (pattern GoF Observer)
        ValutazioneService valutazioneService = new ValutazioneService(
                valutazioneRepository,
                sottomissioneRepository,
                hackathonRepository,
                teamRepository
        );
        valutazioneService.addObserver(notificationsService);

        // --- Controller layer (Constructor Injection) ---
        HackathonController hackathonController = new HackathonController(hackathonService);
        SottomissioneController sottomissioneController = new SottomissioneController(sottomissioneService);
        ProclamazioneController proclamazioneController = new ProclamazioneController(proclamazioneService);
        RegistrationController registrationController = new RegistrationController(utenteService, oauthService);
        TeamController teamController = new TeamController(teamService);
        SegnalazioneController segnalazioneController = new SegnalazioneController(segnalazioneService);
        ValutazioneController valutazioneController = new ValutazioneController(valutazioneService);

        System.out.println("HackHub avviato. Connessione DB: " + dbConfig.getUrl());
    }
}
