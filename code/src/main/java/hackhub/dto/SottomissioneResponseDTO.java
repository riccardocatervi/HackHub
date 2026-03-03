package hackhub.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class SottomissioneResponseDTO {

    private final UUID          idSottomissione;
    private final LocalDateTime dataInvio;

    public SottomissioneResponseDTO(UUID idSottomissione, LocalDateTime dataInvio) {
        this.idSottomissione = idSottomissione;
        this.dataInvio       = dataInvio;
    }

    public UUID          getIdSottomissione() { return idSottomissione; }
    public LocalDateTime getDataInvio()       { return dataInvio; }
}
