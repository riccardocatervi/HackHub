package hackhub.rest;

import hackhub.controller.InvitationController;
import hackhub.dto.InvitationResponseDTO;
import hackhub.dto.InviteCreatedDTO;
import hackhub.dto.InvitoListaItemDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint REST per i casi d'uso relativi agli inviti:
 * accettare/rifiutare inviti e inviare nuovi inviti.
 * Delega al coordinatore GRASP {@link InvitationController}.
 */
@RestController
@RequestMapping("/api/inviti")
public class InvitationRestController {

    private final InvitationController invitationController;

    public InvitationRestController(InvitationController invitationController) {
        this.invitationController = invitationController;
    }

    /**
     * GET /api/inviti/pendenti/{idUtente} — Lista inviti pendenti dell'utente.
     */
    @GetMapping("/pendenti/{idUtente}")
    public ResponseEntity<List<InvitoListaItemDTO>> getPendingInvitations(
            @PathVariable UUID idUtente) {
        return ResponseEntity.ok(invitationController.getPendingInvitations(idUtente));
    }

    /**
     * POST /api/inviti/rispondi — Risposta a un invito (accettazione o rifiuto).
     */
    @PostMapping("/rispondi")
    public ResponseEntity<InvitationResponseDTO> respondToInvitation(
            @RequestParam("invito") UUID idInvito,
            @RequestParam("utente") UUID idUtente,
            @RequestParam("accettato") boolean accettato) {
        return ResponseEntity.ok(
                invitationController.respondToInvitation(idInvito, idUtente, accettato));
    }

    /**
     * POST /api/inviti — Invia un nuovo invito a un utente.
     */
    @PostMapping
    public ResponseEntity<InviteCreatedDTO> sendInvite(
            @RequestParam("team") UUID teamId,
            @RequestParam("target") UUID targetUserId,
            @RequestParam("requester") UUID requesterId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(invitationController.sendInvite(teamId, targetUserId, requesterId));
    }
}
