package hackhub.controller;

import hackhub.dto.SottomissioniDaValutareDTO;
import hackhub.dto.ValutazioneRequestDTO;
import hackhub.dto.ValutazioneResponseDTO;
import hackhub.service.ValutazioneService;

import java.util.UUID;

public class ValutazioneController {

    private final ValutazioneService valutazioneService;

    public ValutazioneController(ValutazioneService valutazioneService) {
        this.valutazioneService = valutazioneService;
    }

    public SottomissioniDaValutareDTO richiediSottomissioniDaValutare(UUID hackathonId, UUID giudiceId) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
        return null;
    }

    public ValutazioneResponseDTO inviaValutazione(ValutazioneRequestDTO valutazioneRequest) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
        return null;
    }
}
