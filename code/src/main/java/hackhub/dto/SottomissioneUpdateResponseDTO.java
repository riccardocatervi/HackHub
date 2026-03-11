package hackhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO di risposta alla conferma dell'aggiornamento di una sottomissione.
 * Usato nel caso d'uso 'Aggiornare Sottomissione del Team'.
 */
public record SottomissioneUpdateResponseDTO(
        UUID idSottomissione,
        UUID idHackathon,
        UUID idTeam,
        String linkRepo,
        String linkDemo,
        String descrizione,
        LocalDateTime dataAggiornamento
) { }
