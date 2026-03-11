package hackhub.dto;

import java.util.UUID;

/**
 * DTO per la richiesta di aggiornamento di una sottomissione già inviata.
 * Usato nel caso d'uso 'Aggiornare Sottomissione del Team'.
 */
public record SottomissioneUpdateDTO(
        UUID idHackathon,
        UUID idTeam,
        String linkRepo,
        String linkDemo,
        String descrizione
) { }
