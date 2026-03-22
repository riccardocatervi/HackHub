package hackhub.rest;

import hackhub.controller.SupportoController;
import hackhub.dto.SupportoFormDTO;
import hackhub.dto.SupportoRequestDTO;
import hackhub.dto.SupportoResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 * Delega al coordinatore GRASP {@link SupportoController}.
 */
@RestController
@RequestMapping("/api/supporto")
public class SupportoRestController {

    private final SupportoController supportoController;

    public SupportoRestController(SupportoController supportoController) {
        this.supportoController = supportoController;
    }

    /**
     * GET /api/supporto/form — Dati di contesto per il form di richiesta supporto.
     */
    @GetMapping("/form")
    public ResponseEntity<SupportoFormDTO> getFormSupporto(
            @RequestParam("hackathon") UUID idHackathon,
            @RequestParam("team") UUID idTeam) {
        return ResponseEntity.ok(supportoController.richiediFormSupporto(idHackathon, idTeam));
    }

    /**
     * POST /api/supporto — Invia una richiesta di supporto.
     */
    @PostMapping
    public ResponseEntity<SupportoResponseDTO> inviaSupporto(
            @RequestBody SupportoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(supportoController.inviaSupporto(dto));
    }
}
