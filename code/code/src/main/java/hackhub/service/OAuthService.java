package hackhub.service;

import hackhub.dto.OAuthUserInfoDTO;
import hackhub.model.OAuthProvider;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

/**
 * Servizio di integrazione OAuth2 (Java SE, senza Spring Security).
 * <p>
 * Implementa il flusso Authorization Code per i provider configurati (Google, GitHub).
 * In questa iterazione fornisce l'infrastruttura per scambiare il codice di
 * autorizzazione con le informazioni dell'utente; il redirect iniziale e la gestione
 * della sessione sono delegati al layer HTTP (es. servlet o framework futuro).
 * <p>
 * In migrazione Spring Boot: sostituire con {@code OAuth2UserService} di Spring Security.
 */
public class OAuthService {

    private static final String GOOGLE_USERINFO_URL = "https://www.googleapis.com/oauth2/v3/userinfo";
    private static final String GITHUB_USERINFO_URL = "https://api.github.com/user";

    private final HttpClient httpClient;

    public OAuthService() {
        this.httpClient = HttpClient.newHttpClient();
    }

    /**
     * Recupera le informazioni dell'utente dal provider OAuth2 utilizzando
     * il token di accesso ottenuto dopo lo scambio del codice di autorizzazione.
     *
     * @param provider    provider OAuth2 (GOOGLE o GITHUB)
     * @param accessToken token di accesso OAuth2
     * @return DTO con i dati dell'utente restituiti dal provider
     */
    public OAuthUserInfoDTO fetchUserInfo(OAuthProvider provider, String accessToken) {
        String url = switch (provider) {
            case GOOGLE -> GOOGLE_USERINFO_URL;
            case GITHUB -> GITHUB_USERINFO_URL;
        };

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Errore nella chiamata al provider OAuth2 " + provider +
                                ": HTTP " + response.statusCode());
            }

            return parseUserInfo(provider, response.body());

        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(
                    "Errore di comunicazione con il provider OAuth2 " + provider + ": " + e.getMessage(), e);
        }
    }

    /**
     * Analizza la risposta JSON del provider e costruisce il DTO.
     * Parsing manuale senza librerie esterne (compatibile con Java SE).
     */
    private OAuthUserInfoDTO parseUserInfo(OAuthProvider provider, String json) {
        return switch (provider) {
            case GOOGLE -> parseGoogleResponse(json);
            case GITHUB -> parseGitHubResponse(json);
        };
    }

    private OAuthUserInfoDTO parseGoogleResponse(String json) {
        // Estrae sub (id), given_name, family_name, email dalla risposta Google
        String externalId = estraiValore(json, "sub");
        String nome = estraiValore(json, "given_name");
        String cognome = estraiValore(json, "family_name");
        String email = estraiValore(json, "email");
        return new OAuthUserInfoDTO(nome, cognome, email, OAuthProvider.GOOGLE, externalId);
    }

    private OAuthUserInfoDTO parseGitHubResponse(String json) {
        // GitHub restituisce id (numerico), name (nome completo), email
        String externalId = estraiValore(json, "id");
        String nomeCompleto = estraiValore(json, "name");
        String email = estraiValore(json, "email");

        // Suddivisione del nome completo in nome + cognome (best-effort)
        String nome = nomeCompleto;
        String cognome = "";
        if (nomeCompleto != null && nomeCompleto.contains(" ")) {
            int ultimoSpazio = nomeCompleto.lastIndexOf(' ');
            nome = nomeCompleto.substring(0, ultimoSpazio);
            cognome = nomeCompleto.substring(ultimoSpazio + 1);
        }

        return new OAuthUserInfoDTO(nome, cognome, email, OAuthProvider.GITHUB, externalId);
    }

    /**
     * Estrae il valore di un campo da una stringa JSON minimale.
     * Nota: parsing naive, sufficiente per le risposte flat dei provider OAuth2.
     */
    private String estraiValore(String json, String chiave) {
        String cerca = "\"" + chiave + "\"";
        int idx = json.indexOf(cerca);
        if (idx == -1) return null;

        int inizio = json.indexOf(':', idx) + 1;
        while (inizio < json.length() && Character.isWhitespace(json.charAt(inizio))) {
            inizio++;
        }

        if (json.charAt(inizio) == '"') {
            int fine = json.indexOf('"', inizio + 1);
            return json.substring(inizio + 1, fine);
        } else {
            // Valore numerico
            int fine = inizio;
            while (fine < json.length() && json.charAt(fine) != ',' && json.charAt(fine) != '}') {
                fine++;
            }
            return json.substring(inizio, fine).trim();
        }
    }
}
