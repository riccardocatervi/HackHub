package hackhub.rest;

import hackhub.controller.CallController;
import hackhub.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint REST per i casi d'uso 'Pianificare call con un team' e
 * 'Gestire invito a call da parte di un mentore'.
 * Delega al coordinatore GRASP {@link CallController}.
 */
@RestController
@RequestMapping("/api/calls")
public class CallRestController {

    private final CallController callController;

    public CallRestController(CallController callController) {
        this.callController = callController;
    }

    /**
     * GET /api/calls/form/{idRichiesta} — Form precompilato per la pianificazione call.
     */
    @GetMapping("/form/{idRichiesta}")
    public ResponseEntity<CallFormDTO> getFormPianificazione(
            @PathVariable UUID idRichiesta,
            @RequestParam("mentore") UUID idMentore) {
        return ResponseEntity.ok(
                callController.richiediFormPianificazioneCall(idRichiesta, idMentore));
    }

    /**
     * POST /api/calls — Pianifica una nuova call.
     */
    @PostMapping
    public ResponseEntity<CallResponseDTO> pianificaCall(@RequestBody CallCreateDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(callController.pianificaCall(dto));
    }

    /**
     * GET /api/calls/inviti/{idTeam} — Lista degli inviti a call ricevuti dal team.
     */
    @GetMapping("/inviti/{idTeam}")
    public ResponseEntity<List<CallInviteListItemDTO>> getCallInvites(
            @PathVariable UUID idTeam,
            @RequestParam("leader") UUID idLeader) {
        return ResponseEntity.ok(callController.getCallInvites(idTeam, idLeader));
    }

    /**
     * POST /api/calls/{idCall}/rispondi — Risposta del leader a un invito a call.
     */
    @PostMapping("/{idCall}/rispondi")
    public ResponseEntity<CallInviteResponseDTO> respondToCallInvite(
            @PathVariable UUID idCall,
            @RequestParam("leader") UUID idLeader,
            @RequestParam("accettata") boolean accettata) {
        return ResponseEntity.ok(
                callController.respondToCallInvite(idCall, idLeader, accettata));
    }
}
