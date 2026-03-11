package hackhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO di conferma dell'invio della richiesta di supporto.
 * Include i riferimenti al mentore assegnato e il timestamp dell'operazione.
 * Usato nel caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 */
public record SupportoResponseDTO(
        UUID idRichiesta,
        UUID idMentoreAssegnato,
        String nomeMentore,
        LocalDateTime dataInvio,
        String messaggio
) { }
