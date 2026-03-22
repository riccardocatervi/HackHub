package hackhub.rest;

import hackhub.controller.HackathonRegistrationController;
import hackhub.dto.UnsubscriptionSuccessDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Gestire iscrizione del team all'Hackathon'.
 * Delega al coordinatore GRASP {@link HackathonRegistrationController}.
 */
@RestController
@RequestMapping("/api/hackathon-registration")
public class HackathonRegistrationRestController {

    private final HackathonRegistrationController hackathonRegistrationController;

    public HackathonRegistrationRestController(
            HackathonRegistrationController hackathonRegistrationController) {
        this.hackathonRegistrationController = hackathonRegistrationController;
    }

    /**
     * DELETE /api/hackathon-registration/{idTeam} — Disiscrizione del team dall'hackathon.
     */
    @DeleteMapping("/{idTeam}")
    public ResponseEntity<UnsubscriptionSuccessDTO> unsubscribeTeam(
            @PathVariable UUID idTeam,
            @RequestParam("requester") UUID requesterId) {
        return ResponseEntity.ok(
                hackathonRegistrationController.unsubscribeTeam(idTeam, requesterId));
    }
}
