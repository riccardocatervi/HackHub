package hackhub.dto;

import java.util.UUID;

/**
 * DTO per pre-popolare il form di aggiornamento di una sottomissione.
 * Contiene i dati correnti della sottomissione che il leader può modificare.
 * Usato nel caso d'uso 'Aggiornare Sottomissione del Team'.
 */
public record SottomissioneAggiornamentoFormDTO(
        UUID idHackathon,
        String nomeHackathon,
        UUID idTeam,
        String nomeTeam,
        UUID idSottomissione,
        String linkRepoAttuale,
        String linkDemoAttuale,
        String descrizioneAttuale
) { }
