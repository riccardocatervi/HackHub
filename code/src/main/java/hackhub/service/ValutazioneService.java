package hackhub.service;

import hackhub.dto.ValutazioneRequestDTO;
import hackhub.dto.ValutazioneResponseDTO;
import hackhub.model.entity.Sottomissione;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.ValutazioneRepository;

import java.util.List;
import java.util.UUID;

public class ValutazioneService {

    private final ValutazioneRepository  valutazioneRepository;
    private final SottomissioneRepository sottomissioneRepository;

    public ValutazioneService(ValutazioneRepository valutazioneRepository,
                              SottomissioneRepository sottomissioneRepository) {
        this.valutazioneRepository  = valutazioneRepository;
        this.sottomissioneRepository = sottomissioneRepository;
    }

    public List<Sottomissione> getSottomissioniDaValutare(UUID hackathonId, UUID giudiceId) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
        return null;
    }

    public ValutazioneResponseDTO valutaSottomissione(ValutazioneRequestDTO valutazioneRequest) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
        return null;
    }
}
