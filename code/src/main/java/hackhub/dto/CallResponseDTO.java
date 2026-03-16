package hackhub.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO di conferma restituito al mentore al completamento della pianificazione della call.
 * Include il link generato dal sistema Calendar e i dettagli dell'evento salvato.
 * Usato nel caso d'uso 'Pianificare call con un team'.
 */
public record CallResponseDTO(
        UUID idCall,
        UUID idRichiesta,
        LocalDate dataCall,
        LocalTime oraCall,
        String linkCall,
        String descrizione,
        String messaggio
) {
}
