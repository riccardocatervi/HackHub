package hackhub.controller;

import hackhub.dto.OAuthUserInfoDTO;
import hackhub.dto.UserRegistrationDTO;
import hackhub.dto.UserRegistrationResponseDTO;
import hackhub.model.OAuthProvider;
import hackhub.service.OAuthService;
import hackhub.service.UtenteService;

/**
 * Coordinatore GRASP per il caso d'uso "Registrarsi alla piattaforma".
 * Delega tutta la logica di dominio a {@link UtenteService} e il recupero
 * delle informazioni OAuth2 a {@link OAuthService}.
 */
public class RegistrationController {

    private final UtenteService utenteService;
    private final OAuthService oauthService;

    public RegistrationController(UtenteService utenteService, OAuthService oauthService) {
        this.utenteService = utenteService;
        this.oauthService = oauthService;
    }

    /**
     * Registra un nuovo utente tramite il modulo standard (email + password).
     *
     * @param dto dati inseriti nel form di registrazione
     * @return DTO di conferma con i dati dell'account creato
     */
    public UserRegistrationResponseDTO compilaFormRegistrazione(UserRegistrationDTO dto) {
        return utenteService.registra(dto);
    }

    /**
     * Completa la registrazione tramite OAuth2 dopo che il provider ha reindirizzato
     * l'utente con il token di accesso.
     * <p>
     * Flusso:
     * <ol>
     *   <li>Il layer HTTP riceve il callback dal provider OAuth2 con l'access token</li>
     *   <li>Questo metodo recupera le informazioni utente tramite {@link OAuthService}</li>
     *   <li>Delega la registrazione/lookup a {@link UtenteService}</li>
     * </ol>
     *
     * @param provider    provider OAuth2 (GOOGLE o GITHUB)
     * @param accessToken token di accesso restituito dal provider
     * @return DTO di conferma con i dati dell'account
     */
    public UserRegistrationResponseDTO completaRegistrazioneOAuth(
            OAuthProvider provider, String accessToken) {

        OAuthUserInfoDTO infoUtente = oauthService.fetchUserInfo(provider, accessToken);
        return utenteService.registraDaOAuth(infoUtente);
    }
}
