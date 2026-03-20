package hackhub.service;

import hackhub.dto.AuthResponseDTO;
import hackhub.dto.LoginFormDTO;
import hackhub.dto.OAuthUserInfoDTO;
import hackhub.dto.UserRegistrationResponseDTO;
import hackhub.exception.InvalidCredentialsException;
import hackhub.model.OAuthProvider;
import hackhub.model.entity.Utente;
import hackhub.repository.UserRepository;

import java.util.Map;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Autenticarsi alla piattaforma'.
 * Gestisce due percorsi distinti di autenticazione:
 * <ul>
 *   <li>Credenziali locali (email + password)</li>
 *   <li>OAuth2 con OIDC (Google, GitHub) tramite flusso Authorization Code</li>
 * </ul>
 * Coordina le operazioni tra {@link UserRepository}, {@link PasswordEncoder},
 * {@link OAuthService}, {@link UtenteService} e {@link SessionManager}.
 */
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final OAuthService oauthService;
    private final UtenteService utenteService;
    private final SessionManager sessionManager;
    private final Map<OAuthProvider, OAuth2Config> oauth2Configs;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       OAuthService oauthService,
                       UtenteService utenteService,
                       SessionManager sessionManager,
                       Map<OAuthProvider, OAuth2Config> oauth2Configs) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.oauthService = oauthService;
        this.utenteService = utenteService;
        this.sessionManager = sessionManager;
        this.oauth2Configs = oauth2Configs;
    }

    // -----------------------------------------------------------------------
    // Percorso 1 — Login con credenziali locali
    // -----------------------------------------------------------------------

    /**
     * Autentica un visitatore tramite email e password.
     * <p>
     * Flusso:
     * <ol>
     *   <li>Cerca l'utente per email; se non trovato lancia {@link InvalidCredentialsException}.</li>
     *   <li>Verifica che l'account non sia di tipo OAuth-only (privo di password).</li>
     *   <li>Confronta la password fornita con l'hash memorizzato.</li>
     *   <li>In caso di esito positivo genera una sessione e restituisce {@link AuthResponseDTO}.</li>
     * </ol>
     *
     * @param dto DTO con le credenziali inserite dal visitatore
     * @return DTO con i dati dell'utente autenticato e il token di sessione
     * @throws InvalidCredentialsException se l'account non esiste, è OAuth-only, o la password è errata
     */
    public AuthResponseDTO authenticate(LoginFormDTO dto) {
        Utente utente = userRepository.findByEmail(dto.email())
                .orElseThrow(() -> new InvalidCredentialsException(
                        "Credenziali non valide. Verificare email e password."));

        // Impossibile autenticarsi con password su un account creato tramite OAuth2
        if (utente.isOAuthUser()) {
            throw new InvalidCredentialsException(
                    "Account registrato tramite provider esterno. " +
                            "Utilizzare il login OAuth2 con il provider selezionato in fase di registrazione.");
        }

        if (!passwordEncoder.matches(dto.password(), utente.getHashedPassword())) {
            throw new InvalidCredentialsException(
                    "Credenziali non valide. Verificare email e password.");
        }

        String sessionToken = generaSessioneUtente(utente.getId());
        return new AuthResponseDTO(
                utente.getId(),
                utente.getNome(),
                utente.getCognome(),
                utente.getEmail(),
                sessionToken);
    }

    // -----------------------------------------------------------------------
    // Percorso 2 — Login OAuth2 con OIDC
    // -----------------------------------------------------------------------

    /**
     * Genera l'URL di autorizzazione OAuth2 verso il provider selezionato.
     * Il visitatore verrà reindirizzato a questo URL per autenticarsi
     * e concedere i consensi richiesti dall'applicazione.
     *
     * @param providerName nome del provider (es. "GOOGLE", "GITHUB") — case-insensitive
     * @return URL completo a cui reindirizzare il visitatore
     * @throws IllegalArgumentException se il provider non è supportato o non configurato
     */
    public String getOAuth2AuthorizationUrl(String providerName) {
        OAuthProvider provider = parseProvider(providerName);
        OAuth2Config config = getConfig(provider);
        // Genera uno stato anti-CSRF univoco per questa sessione di autenticazione
        String state = UUID.randomUUID().toString();
        return oauthService.buildAuthorizationUrl(
                provider, config.getClientId(), config.getRedirectUri(), state);
    }

    /**
     * Gestisce il callback OAuth2/OIDC ricevuto dal provider.
     * <p>
     * Flusso:
     * <ol>
     *   <li>Scambia il codice di autorizzazione con un access token.</li>
     *   <li>Recupera le informazioni utente dal provider tramite l'access token.</li>
     *   <li>Registra il nuovo utente oppure recupera l'account esistente
     *       (logica delegata a {@link UtenteService#registraDaOAuth}).</li>
     *   <li>Genera una sessione e restituisce {@link AuthResponseDTO}.</li>
     * </ol>
     *
     * @param authorizationCode codice di autorizzazione ricevuto dal provider
     * @param providerName      nome del provider OAuth2 — case-insensitive
     * @return DTO con i dati dell'utente autenticato e il token di sessione
     * @throws RuntimeException se la comunicazione con il provider fallisce
     */
    public AuthResponseDTO gestisciCallbackOAuth2(String authorizationCode, String providerName) {
        OAuthProvider provider = parseProvider(providerName);
        OAuth2Config config = getConfig(provider);

        // Scambio del codice con l'access token (Step 2 del flusso Authorization Code)
        String accessToken = oauthService.scambiaCodiceConToken(
                authorizationCode,
                provider,
                config.getClientId(),
                config.getClientSecret(),
                config.getRedirectUri());

        // Recupero delle informazioni utente dal provider (Step 3)
        OAuthUserInfoDTO userInfo = oauthService.fetchUserInfo(provider, accessToken);

        // Registrazione del nuovo utente o recupero dell'account già esistente
        UserRegistrationResponseDTO regResult = utenteService.registraDaOAuth(userInfo);

        String sessionToken = generaSessioneUtente(regResult.id());
        return new AuthResponseDTO(
                regResult.id(),
                regResult.nome(),
                regResult.cognome(),
                regResult.email(),
                sessionToken);
    }

    // -----------------------------------------------------------------------
    // Gestione sessione
    // -----------------------------------------------------------------------

    /**
     * Crea una nuova sessione applicativa per l'utente autenticato.
     *
     * @param idUtente l'id dell'utente per cui creare la sessione
     * @return token di sessione univoco
     */
    public String generaSessioneUtente(UUID idUtente) {
        return sessionManager.creaSessione(idUtente);
    }

    // -----------------------------------------------------------------------
    // Metodi privati di supporto
    // -----------------------------------------------------------------------

    private OAuthProvider parseProvider(String providerName) {
        try {
            return OAuthProvider.valueOf(providerName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Provider OAuth2 non riconosciuto: '" + providerName + "'. " +
                            "Provider supportati: GOOGLE, GITHUB.");
        }
    }

    private OAuth2Config getConfig(OAuthProvider provider) {
        OAuth2Config config = oauth2Configs.get(provider);
        if (config == null) {
            throw new IllegalArgumentException(
                    "Configurazione OAuth2 non trovata per il provider: " + provider +
                            ". Verificare che le credenziali siano state impostate.");
        }
        return config;
    }
}
