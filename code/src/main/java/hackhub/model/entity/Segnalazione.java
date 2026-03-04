package hackhub.model.entity;

import java.util.UUID;

public class Segnalazione {

    private UUID teamId;
    private String descrizione;
    private UUID mentoreId;

    public Segnalazione(UUID teamId, String descrizione, UUID mentoreId) {
        this.teamId = teamId;
        this.descrizione = descrizione;
        this.mentoreId = mentoreId;
    }
}
