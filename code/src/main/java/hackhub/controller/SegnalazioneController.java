package hackhub.controller;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.dto.SegnalazioneRequestDTO;
import hackhub.dto.SegnalazioneResponseDTO;
import hackhub.service.SegnalazioneService;

import java.util.UUID;

/**
 * Controller GRASP Coordinator per il caso d'uso 'Segnalare violazione del regolamento'.
 * Coordina il flusso: recupero dati modulo → compilazione → invio segnalazione.
 */
public class SegnalazioneController {

    private final SegnalazioneService segnalazioneService;

    public SegnalazioneController(SegnalazioneService segnalazioneService) {
        this.segnalazioneService = segnalazioneService;
    }

    /**
     * Fornisce i dati necessari per il modulo di segnalazione:
     * nome hackathon e lista dei team che il mentore può segnalare.
     *
     * @param hackathonId  id dell'hackathon visualizzato nella dashboard del mentore
     * @return DTO con i dati del form da mostrare al mentore
     */
    public ModuloSegnalazioneDTO getDatiModulo(UUID hackathonId) {
        return segnalazioneService.getDatiModulo(hackathonId);
    }

    /**
     * Riceve i dati compilati dal mentore e registra la segnalazione.
     * Notifica immediatamente l'organizzatore tramite il servizio notifiche (Observer).
     *
     * @param request  dati del modulo compilato
     * @return DTO di conferma con messaggio di avvenuta segnalazione
     */
    public SegnalazioneResponseDTO inviaSegnalazione(SegnalazioneRequestDTO request) {
        return segnalazioneService.segnalaViolazione(request);
    }
}
