package hackhub.dto;

import java.util.List;

/**
 * DTO immutabile con i dati necessari per popolare il form di creazione hackathon.
 */
public record HackathonFormDataDTO(
        String nomeOrganizzatore,
        List<GiudiceDTO> giudiciDisponibili,
        List<MentoreDTO> mentoriDisponibili
) {
}
