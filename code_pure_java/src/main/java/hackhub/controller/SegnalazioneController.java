package hackhub.controller;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.dto.SegnalazioneDettagliDTO;
import hackhub.dto.SegnalazioneGestioneDTO;
import hackhub.dto.SegnalazioneGestioneResponseDTO;
import hackhub.dto.SegnalazioneRequestDTO;
import hackhub.dto.SegnalazioneResponseDTO;
import hackhub.service.SegnalazioneService;

import java.util.UUID;

/**
 * Controller GRASP Coordinator per i casi d'uso relativi alle segnalazioni:
 * <ul>
 *   <li>'Segnalare violazione del regolamento' — flusso mentore.</li>
 *   <li>'Gestire penalizzazione o squalifica di un team' — flusso organizzatore.</li>
 * </ul>
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
     * @param hackathonId id dell'hackathon visualizzato nella dashboard del mentore
     * @return DTO con i dati del form da mostrare al mentore
     */
    public ModuloSegnalazioneDTO getDatiModulo(UUID hackathonId) {
        return segnalazioneService.getDatiModulo(hackathonId);
    }

    /**
     * Riceve i dati compilati dal mentore e registra la segnalazione.
     * Notifica immediatamente l'organizzatore tramite il servizio notifiche (Observer).
     *
     * @param request dati del modulo compilato
     * @return DTO di conferma con messaggio di avvenuta segnalazione
     */
    public SegnalazioneResponseDTO inviaSegnalazione(SegnalazioneRequestDTO request) {
        return segnalazioneService.segnalaViolazione(request);
    }

    /**
     * Restituisce i dettagli completi di una segnalazione selezionata dall'organizzatore
     * dalla lista delle segnalazioni dell'hackathon.
     *
     * @param idSegnalazione id della segnalazione da visualizzare
     * @return DTO con dettagli della segnalazione (mentore, team, descrizione, prove, stato)
     */
    public SegnalazioneDettagliDTO getDettagliSegnalazione(UUID idSegnalazione) {
        return segnalazioneService.getDettagliSegnalazione(idSegnalazione);
    }

    /**
     * Riceve la decisione dell'organizzatore su una segnalazione pendente.
     * Squalifica il team se la segnalazione viene accettata, o chiude il caso se rifiutata.
     * Notifica il mentore tramite il pattern Observer.
     *
     * @param dto DTO con l'id della segnalazione e la decisione (ACCETTATA o RIFIUTATA)
     * @return DTO di risposta con il risultato dell'operazione
     */
    public SegnalazioneGestioneResponseDTO gestisciSegnalazione(SegnalazioneGestioneDTO dto) {
        return segnalazioneService.gestisciSegnalazione(dto);
    }
}
