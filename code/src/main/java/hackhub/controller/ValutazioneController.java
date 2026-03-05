package hackhub.controller;

import hackhub.dto.SottomissioniDaValutareDTO;
import hackhub.dto.ValutazioneRequestDTO;
import hackhub.dto.ValutazioneResponseDTO;
import hackhub.service.ValutazioneService;

import java.util.UUID;

/**
 * Controller GRASP Coordinator per il caso d'uso 'Valutare sottomissione di un team'.
 * Coordina il flusso: recupero lista sottomissioni → selezione → valutazione → conferma.
 */
public class ValutazioneController {

    private final ValutazioneService valutazioneService;

    public ValutazioneController(ValutazioneService valutazioneService) {
        this.valutazioneService = valutazioneService;
    }

    /**
     * Recupera la dashboard di valutazione per il giudice:
     * lista completa di sottomissioni con flag che indica se già valutate.
     *
     * @param hackathonId  id dell'hackathon
     * @param giudiceId    id del giudice autenticato
     * @return DTO con lista sottomissioni e stato di valutazione di ciascuna
     */
    public SottomissioniDaValutareDTO richiediSottomissioniDaValutare(UUID hackathonId, UUID giudiceId) {
        return valutazioneService.getSottomissioniDaValutare(hackathonId, giudiceId);
    }

    /**
     * Riceve la valutazione inserita dal giudice e la persiste.
     * Dopo il salvataggio la sottomissione risulta valutata e non è rivalutabile.
     *
     * @param request  punteggio e giudizio scritto del giudice
     * @return DTO di conferma con i dati della valutazione salvata
     */
    public ValutazioneResponseDTO inviaValutazione(ValutazioneRequestDTO request) {
        return valutazioneService.valutaSottomissione(request);
    }
}
