package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile per la rappresentazione di un mentore.
 */
public record MentoreDTO(UUID id, String nome, String cognome) {
}
