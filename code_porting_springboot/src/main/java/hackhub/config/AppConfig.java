package hackhub.config;

import hackhub.model.OAuthProvider;
import hackhub.service.NotificationsService;
import hackhub.service.OAuth2Config;
import hackhub.service.SegnalazioneService;
import hackhub.service.SupportRequestService;
import hackhub.service.SupportoService;
import hackhub.service.InvitationService;
import hackhub.service.ValutazioneService;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configurazione centrale dell'applicazione Spring Boot.
 * <p>
 * Registra i bean che necessitano di configurazione manuale
 * e collega gli observer del pattern GoF Observer ai service produttori di eventi.
 */
@Configuration
public class AppConfig {

    private final NotificationsService notificationsService;
    private final SegnalazioneService segnalazioneService;
    private final ValutazioneService valutazioneService;
    private final SupportoService supportoService;
    private final SupportRequestService supportRequestService;
    private final InvitationService invitationService;

    public AppConfig(NotificationsService notificationsService,
                     SegnalazioneService segnalazioneService,
                     ValutazioneService valutazioneService,
                     SupportoService supportoService,
                     SupportRequestService supportRequestService,
                     InvitationService invitationService) {
        this.notificationsService = notificationsService;
        this.segnalazioneService = segnalazioneService;
        this.valutazioneService = valutazioneService;
        this.supportoService = supportoService;
        this.supportRequestService = supportRequestService;
        this.invitationService = invitationService;
    }

    /**
     * Registra NotificationsService come observer su tutti i service
     * che implementano il pattern GoF Observer.
     * Equivalente al wiring manuale presente nel vecchio HackHubApplication.main().
     */
    @PostConstruct
    public void registraObservers() {
        // Segnalazione: notifica l'organizzatore alla creazione e alla gestione
        segnalazioneService.addObserver(notificationsService);
        segnalazioneService.addGestioneObserver(notificationsService);

        // Valutazione: notifica quando una valutazione viene completata
        valutazioneService.addObserver(notificationsService);

        // Supporto: notifica il mentore alla ricezione di una nuova richiesta
        supportoService.addObserver(notificationsService);

        // Gestione richieste supporto: notifica il leader all'accettazione/rifiuto
        supportRequestService.addObserver(notificationsService);

        // Inviti: notifica leader e invitato alla risposta/creazione di un invito
        invitationService.addObserver(notificationsService);
        invitationService.addNuovoInvitoObserver(notificationsService);
    }

    /**
     * Configurazione OAuth2 per i provider supportati.
     * I valori sono impostabili tramite variabili d'ambiente in produzione.
     */
    @Bean
    public Map<OAuthProvider, OAuth2Config> oauth2Configs() {
        Map<OAuthProvider, OAuth2Config> configs = new HashMap<>();

        String googleClientId = System.getenv("OAUTH_GOOGLE_CLIENT_ID");
        String googleClientSecret = System.getenv("OAUTH_GOOGLE_CLIENT_SECRET");
        String googleRedirectUri = System.getenv("OAUTH_GOOGLE_REDIRECT_URI");

        if (googleClientId != null && !googleClientId.isBlank()) {
            configs.put(OAuthProvider.GOOGLE, new OAuth2Config(
                    OAuthProvider.GOOGLE, googleClientId, googleClientSecret, googleRedirectUri));
        }

        String githubClientId = System.getenv("OAUTH_GITHUB_CLIENT_ID");
        String githubClientSecret = System.getenv("OAUTH_GITHUB_CLIENT_SECRET");
        String githubRedirectUri = System.getenv("OAUTH_GITHUB_REDIRECT_URI");

        if (githubClientId != null && !githubClientId.isBlank()) {
            configs.put(OAuthProvider.GITHUB, new OAuth2Config(
                    OAuthProvider.GITHUB, githubClientId, githubClientSecret, githubRedirectUri));
        }

        return configs;
    }
}
