package hackhub.dto;

import hackhub.model.StatoCall;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

/**
 * DTO per la visualizzazione sintetica di un invito a call ricevuto dal leader del team.
 * Contiene i dati della call e le informazioni essenziali del mentore mittente.
 * Usato nel caso d'uso 'Gestire invito a call da parte di un mentore'.
 */
public record CallInviteListItemDTO(
        UUID idCall,
        UUID idRichiestaSupporto,
        UUID idMentore,
        String nomeMentore,
        String cognomeMentore,
        LocalDate dataCall,
        LocalTime oraCall,
        String linkCall,
        String descrizione,
        StatoCall stato
) {
}
