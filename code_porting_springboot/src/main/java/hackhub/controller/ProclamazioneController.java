package hackhub.controller;

import hackhub.dto.ProclamazioneFormDTO;
import hackhub.dto.ProclamazioneResponseDTO;
import hackhub.service.ProclamazioneService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Controller per il caso d'uso 'Proclamare Vincitore'.
 * Agisce come coordinatore GRASP: espone due passi distinti
 * (visualizza il form di conferma, poi esegue la proclamazione).
 */
@Component
public class ProclamazioneController {

    private final ProclamazioneService proclamazioneService;

    public ProclamazioneController(ProclamazioneService proclamazioneService) {
        this.proclamazioneService = proclamazioneService;
    }

    /**
     * Restituisce il form di conferma con il team che ha ottenuto il punteggio più alto.
     * Verifica che l'hackathon sia in stato IN_VALUTAZIONE.
     *
     * @param idHackathon l'id dell'hackathon per cui proclamare il vincitore
     */
    public ProclamazioneFormDTO richiediFormProclamazione(UUID idHackathon) {
        return proclamazioneService.preparaProclamazione(idHackathon);
    }

    /**
     * Esegue la proclamazione ufficiale del vincitore.
     * Transiziona l'hackathon a CONCLUSO e invia le notifiche.
     *
     * @param idHackathon l'id dell'hackathon
     * @return i dettagli della proclamazione con il team vincitore e il premio
     */
    public ProclamazioneResponseDTO confermaProclamazione(UUID idHackathon) {
        return proclamazioneService.eseguiProclamazione(idHackathon);
    }
}
