package hackhub.service;

import hackhub.model.OAuthProvider;

/**
 * Configurazione del client OAuth2 per un singolo provider (Google o GitHub).
 * Incapsula le credenziali necessarie per il flusso Authorization Code con OIDC.
 * <p>
 * I valori vengono tipicamente caricati da variabili d'ambiente o da
 * {@code application.properties} durante l'inizializzazione dell'applicazione.
 */
public class OAuth2Config {

    private final OAuthProvider provider;
    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;

    public OAuth2Config(OAuthProvider provider,
                        String clientId,
                        String clientSecret,
                        String redirectUri) {
        this.provider = provider;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
    }

    public OAuthProvider getProvider() {
        return provider;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
