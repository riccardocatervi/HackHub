package hackhub.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO con i dati inseriti dal mentore nel form di pianificazione della call.
 * Usato nel caso d'uso 'Pianificare call con un team'.
 */
public record CallCreateDTO(
        UUID idRichiesta,
        UUID idMentore,
        LocalDate dataCall,
        LocalTime oraCall,
        String descrizione
) {
}
