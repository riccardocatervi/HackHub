package hackhub.model.entity;

import hackhub.model.OAuthProvider;

import java.util.UUID;

/**
 * Utente registrato alla piattaforma HackHub.
 * Supporta due modalità di registrazione:
 * <ul>
 *   <li>Locale: email + password (hashedPassword valorizzata, campi OAuth nulli)</li>
 *   <li>OAuth2: provider esterno (Google/GitHub) — hashedPassword nulla</li>
 * </ul>
 */
public class Utente {

    private UUID id;           // assegnato dal DB al momento del salvataggio
    private final String nome;
    private final String cognome;
    private final String email;
    private final String hashedPassword;   // null per utenti OAuth
    private final OAuthProvider oauthProvider;     // null per utenti locali
    private final String oauthExternalId;   // null per utenti locali

    /**
     * Costruttore per registrazione locale (email + password).
     */
    public Utente(String nome, String cognome, String email, String hashedPassword) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.oauthProvider = null;
        this.oauthExternalId = null;
    }

    /**
     * Costruttore per registrazione tramite OAuth2.
     */
    public Utente(String nome, String cognome, String email,
                  OAuthProvider oauthProvider, String oauthExternalId) {
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.hashedPassword = null;
        this.oauthProvider = oauthProvider;
        this.oauthExternalId = oauthExternalId;
    }

    /**
     * Costruttore per la ricostruzione dal database.
     */
    public Utente(UUID id, String nome, String cognome, String email,
                  String hashedPassword, OAuthProvider oauthProvider, String oauthExternalId) {
        this.id = id;
        this.nome = nome;
        this.cognome = cognome;
        this.email = email;
        this.hashedPassword = hashedPassword;
        this.oauthProvider = oauthProvider;
        this.oauthExternalId = oauthExternalId;
    }

    public UUID getId() {
        return id;
    }

    /**
     * Chiamato dal repository dopo l'INSERT con RETURNING id.
     */
    public void setId(UUID id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getEmail() {
        return email;
    }

    public String getHashedPassword() {
        return hashedPassword;
    }

    public OAuthProvider getOauthProvider() {
        return oauthProvider;
    }

    public String getOauthExternalId() {
        return oauthExternalId;
    }

    public boolean isOAuthUser() {
        return oauthProvider != null;
    }
}
