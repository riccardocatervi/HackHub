package hackhub.controller;

import hackhub.dto.CallCreateDTO;
import hackhub.dto.CallFormDTO;
import hackhub.dto.CallInviteListItemDTO;
import hackhub.dto.CallInviteResponseDTO;
import hackhub.dto.CallResponseDTO;
import hackhub.service.CallService;

import java.util.List;
import java.util.UUID;

/**
 * Controller GRASP per i casi d'uso 'Pianificare call con un team' e
 * 'Gestire invito a call da parte di un mentore'.
 * <p>
 * Agisce come coordinatore (Pattern Controller GRASP): riceve le richieste dell'UI,
 * esegue la validazione formale del payload e delega tutta la logica di business
 * a {@link CallService}.
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

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire invito a call da parte di un mentore
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista degli inviti a call ricevuti dal leader del team.
     * Punto di ingresso del caso d'uso 'Gestire invito a call da parte di un mentore'.
     *
     * @param idTeam   l'id del team del leader
     * @param idLeader l'id del leader autenticato
     * @return lista degli inviti a call con i dettagli del mentore mittente
     */
    public List<CallInviteListItemDTO> getCallInvites(UUID idTeam, UUID idLeader) {
        if (idTeam == null || idLeader == null) {
            throw new IllegalArgumentException("idTeam e idLeader non possono essere null.");
        }
        return callService.getCallInvites(idTeam, idLeader);
    }

    /**
     * Processa la risposta del leader a un invito a call (accettazione o rifiuto).
     * Delega al service la validazione, l'aggiornamento dello stato e la notifica al mentore.
     *
     * @param idCall    l'id della call a cui rispondere
     * @param idLeader  l'id del leader autenticato
     * @param accettata true per accettare l'invito, false per rifiutarlo
     * @return DTO di conferma con il nuovo stato della call
     */
    public CallInviteResponseDTO respondToCallInvite(UUID idCall, UUID idLeader, boolean accettata) {
        if (idCall == null || idLeader == null) {
            throw new IllegalArgumentException("idCall e idLeader non possono essere null.");
        }
        return callService.processInviteResponse(idCall, idLeader, accettata);
    }
}
