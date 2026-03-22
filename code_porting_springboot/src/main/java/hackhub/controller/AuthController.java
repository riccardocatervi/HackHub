package hackhub.controller;

import hackhub.dto.AuthResponseDTO;
import hackhub.dto.LoginFormDTO;
import hackhub.service.AuthService;
import org.springframework.stereotype.Component;

/**
 * Coordinatore GRASP per il caso d'uso 'Autenticarsi alla piattaforma'.
 * Gestisce i due percorsi di autenticazione: credenziali locali e OAuth2 con OIDC.
 * Delega tutta la logica di dominio a {@link AuthService}.
 */
@Component
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Punto di ingresso del caso d'uso: segnala al sistema che il visitatore
     * ha richiesto la pagina di login.
     *
     * @return identificatore della vista di login da presentare
     */
    public String richiediPaginaLogin() {
        return "login";
    }

    /**
     * Gestisce il tentativo di autenticazione tramite credenziali locali.
     * Delega la validazione e la verifica della password ad {@link AuthService}.
     *
     * @param dto DTO con email e password inseriti dal visitatore
     * @return DTO con i dati dell'utente autenticato e il token di sessione
     */
    public AuthResponseDTO effettuaLogin(LoginFormDTO dto) {
        if (dto == null) {
            throw new IllegalArgumentException("I dati di accesso non possono essere nulli.");
        }
        return authService.authenticate(dto);
    }

    /**
     * Gestisce la richiesta di autenticazione tramite provider OAuth2 esterno.
     * Genera e restituisce l'URL verso cui reindirizzare il visitatore.
     *
     * @param providerName nome del provider selezionato (es. "GOOGLE", "GITHUB") — case-insensitive
     * @return URL di autorizzazione OAuth2 del provider
     */
    public String richiediLoginOAuth2(String providerName) {
        if (providerName == null || providerName.isBlank()) {
            throw new IllegalArgumentException("Il nome del provider OAuth2 non può essere vuoto.");
        }
        return authService.getOAuth2AuthorizationUrl(providerName);
    }

    /**
     * Riceve il codice di autorizzazione restituito dal provider OAuth2 al callback URI
     * e completa il flusso di autenticazione.
     * <p>
     * Se il visitatore ha annullato l'operazione o negato i consensi, il parametro
     * {@code authorizationCode} sarà null o vuoto: in questo caso viene segnalato
     * l'errore all'utente senza chiamare il service.
     *
     * @param authorizationCode codice di autorizzazione ricevuto dal provider (null se accesso negato)
     * @param providerName      nome del provider OAuth2 — case-insensitive
     * @return DTO con i dati dell'utente autenticato e il token di sessione
     */
    public AuthResponseDTO oauth2Callback(String authorizationCode, String providerName) {
        if (authorizationCode == null || authorizationCode.isBlank()) {
            throw new IllegalArgumentException(
                    "Accesso negato o autorizzazione annullata dal provider OAuth2. " +
                            "Riprovare il login.");
        }
        return authService.gestisciCallbackOAuth2(authorizationCode, providerName);
    }
}
