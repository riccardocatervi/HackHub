package hackhub.dto;

import hackhub.model.LivelloUrgenza;
import hackhub.model.StatoRichiesta;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO con i dettagli completi di una richiesta di supporto, visualizzati dal mentore
 * prima di decidere se accettare o rifiutare la richiesta.
 * Usato nel caso d'uso 'Prendere in carico una richiesta di supporto'.
 */
public record RichiestaSupportoDettagliDTO(
        UUID idRichiesta,
        UUID idTeam,
        String nomeTeam,
        UUID idHackathon,
        String nomeHackathon,
        String motivo,
        LivelloUrgenza livelloUrgenza,
        StatoRichiesta stato,
        LocalDateTime dataInvio,
        String motivazioneRifiuto
) {
}
