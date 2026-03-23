package hackhub.rest;

import hackhub.controller.SottomissioneController;
import hackhub.dto.SottomissioneFormDTO;
import hackhub.dto.SottomissioneResponseDTO;
import hackhub.dto.SottomissioneSubmissionDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Inviare Sottomissione del Team'.
 * Delega al coordinatore GRASP {@link SottomissioneController}.
 */
@RestController
@RequestMapping("/api/sottomissioni")
public class SottomissioneRestController {

    private final SottomissioneController sottomissioneController;

    public SottomissioneRestController(SottomissioneController sottomissioneController) {
        this.sottomissioneController = sottomissioneController;
    }

    /**
     * GET /api/sottomissioni/form — Dati di contesto per il form di invio sottomissione.
     */
    @GetMapping("/form")
    public ResponseEntity<SottomissioneFormDTO> getFormInvio(
            @RequestParam("hackathon") UUID idHackathon,
            @RequestParam("team") UUID idTeam) {
        return ResponseEntity.ok(
                sottomissioneController.richiediFormInvioSottomissione(idHackathon, idTeam));
    }

    /**
     * POST /api/sottomissioni — Invia una nuova sottomissione.
     */
    @PostMapping
    public ResponseEntity<SottomissioneResponseDTO> inviaSottomissione(
            @RequestBody SottomissioneSubmissionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(sottomissioneController.inviaSottomissione(dto));
    }
}
