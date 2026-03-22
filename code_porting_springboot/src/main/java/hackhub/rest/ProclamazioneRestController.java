package hackhub.rest;

import hackhub.controller.ProclamazioneController;
import hackhub.dto.ProclamazioneFormDTO;
import hackhub.dto.ProclamazioneResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Proclamare Vincitore'.
 * Delega al coordinatore GRASP {@link ProclamazioneController}.
 */
@RestController
@RequestMapping("/api/proclamazione")
public class ProclamazioneRestController {

    private final ProclamazioneController proclamazioneController;

    public ProclamazioneRestController(ProclamazioneController proclamazioneController) {
        this.proclamazioneController = proclamazioneController;
    }

    /**
     * GET /api/proclamazione/{idHackathon}/form — Form di conferma con il team vincitore.
     */
    @GetMapping("/{idHackathon}/form")
    public ResponseEntity<ProclamazioneFormDTO> getFormProclamazione(
            @PathVariable UUID idHackathon) {
        return ResponseEntity.ok(proclamazioneController.richiediFormProclamazione(idHackathon));
    }

    /**
     * POST /api/proclamazione/{idHackathon} — Esegue la proclamazione del vincitore.
     */
    @PostMapping("/{idHackathon}")
    public ResponseEntity<ProclamazioneResponseDTO> confermaProclamazione(
            @PathVariable UUID idHackathon) {
        return ResponseEntity.ok(proclamazioneController.confermaProclamazione(idHackathon));
    }
}
