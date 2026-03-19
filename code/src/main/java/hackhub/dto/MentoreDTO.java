package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile per la rappresentazione di un mentore.
 * Usato nelle operazioni di gestione mentori di un hackathon.
 */
public record MentoreDTO(
        UUID id,
        String nome,
        String cognome,
        String email
) {
}
