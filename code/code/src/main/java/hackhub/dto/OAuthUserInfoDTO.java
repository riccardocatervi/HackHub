package hackhub.dto;

import hackhub.model.OAuthProvider;

/**
 * Informazioni utente restituite dal provider OAuth2 dopo l'autenticazione.
 * Prodotto da {@link hackhub.service.OAuthService} e consumato da
 * {@link hackhub.service.UtenteService#registraDaOAuth(OAuthUserInfoDTO)}.
 */
public record OAuthUserInfoDTO(
        String nome,
        String cognome,
        String email,
        OAuthProvider provider,
        String externalId
) {
}
