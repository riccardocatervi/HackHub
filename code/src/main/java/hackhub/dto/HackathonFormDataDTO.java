package hackhub.dto;

import java.util.List;

public class HackathonFormDataDTO {

    private final String           nomeOrganizzatore;
    private final List<GiudiceDTO> giudiciDisponibili;
    private final List<MentoreDTO> mentoriDisponibili;

    public HackathonFormDataDTO(String nomeOrganizzatore,
                                List<GiudiceDTO> giudiciDisponibili,
                                List<MentoreDTO> mentoriDisponibili) {
        this.nomeOrganizzatore  = nomeOrganizzatore;
        this.giudiciDisponibili = giudiciDisponibili;
        this.mentoriDisponibili = mentoriDisponibili;
    }

    public String           getNomeOrganizzatore()  { return nomeOrganizzatore; }
    public List<GiudiceDTO> getGiudiciDisponibili() { return giudiciDisponibili; }
    public List<MentoreDTO> getMentoriDisponibili()  { return mentoriDisponibili; }
}
