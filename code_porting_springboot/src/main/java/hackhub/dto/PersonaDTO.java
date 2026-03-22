package hackhub.dto;

import java.util.UUID;

/**
 * DTO immutabile per la rappresentazione di una persona con ruolo di staff
 * (giudice o mentore) disponibile per un hackathon.
 */
public record PersonaDTO(UUID id, String nome, String cognome) {
}
