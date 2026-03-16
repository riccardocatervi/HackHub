package hackhub.controller;

import hackhub.dto.CallCreateDTO;
import hackhub.dto.CallFormDTO;
import hackhub.dto.CallResponseDTO;
import hackhub.service.CallService;

import java.util.UUID;

/**
 * Controller GRASP per il caso d'uso 'Pianificare call con un team'.
 * <p>
 * Agisce come coordinatore (Pattern Controller GRASP): riceve le richieste dell'UI,
 * esegue la validazione formale del payload e delega tutta la logica di business
 * a {@link CallService}.
 * <p>
 * Questo controller viene invocato in due modalità:
 * <ul>
 *   <li>In modo automatico da {@link SupportRequestController} al termine dell'accettazione
 *       di una richiesta di supporto (transizione UC1 → UC2).</li>
 *   <li>Direttamente dall'UI quando il mentore compila e invia il form della call.</li>
 * </ul>
 */
public class CallController {

    private final CallService callService;

    public CallController(CallService callService) {
        this.callService = callService;
    }

    /**
     * Fornisce il form precompilato per la pianificazione della call.
     * Invocato automaticamente dopo l'accettazione di una richiesta di supporto.
     *
     * @param idRichiesta id della richiesta di supporto accettata
     * @param idMentore   id del mentore autenticato
     * @return DTO con i dati di contesto per il form (nome team, nome hackathon)
     */
    public CallFormDTO richiediFormPianificazioneCall(UUID idRichiesta, UUID idMentore) {
        return callService.getFormData(idRichiesta, idMentore);
    }

    /**
     * Processa la pianificazione della call con i dati inseriti dal mentore.
     * Delega al service la comunicazione con il sistema Calendar e il salvataggio nel DB.
     *
     * @param dto DTO con data, ora e descrizione della call inseriti dal mentore
     * @return DTO di conferma con link della call generato dal sistema Calendar
     */
    public CallResponseDTO pianificaCall(CallCreateDTO dto) {
        return callService.pianificaCall(dto);
    }
}
