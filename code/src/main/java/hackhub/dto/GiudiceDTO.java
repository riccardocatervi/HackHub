package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile per la rappresentazione di un giudice.
 */
public record GiudiceDTO(UUID id, String nome, String cognome) {
}
