package hackhub.dto;

import java.util.UUID;

/**
 * Rappresentazione di un utente disponibile a ricevere inviti per un hackathon.
 * Utilizzato per popolare la lista di selezione nel form di creazione team.
 */
public record UtenteDisponibileDTO(
        UUID id,
        String nome,
        String cognome,
        String email
) {
}
