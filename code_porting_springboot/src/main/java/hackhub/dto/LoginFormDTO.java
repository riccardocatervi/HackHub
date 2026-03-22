package hackhub.dto;

/**
 * DTO immutabile che raccoglie le credenziali di accesso inserite dal visitatore
 * nel form di login standard (email + password).
 */
public record LoginFormDTO(
        String email,
        String password
) {
}
