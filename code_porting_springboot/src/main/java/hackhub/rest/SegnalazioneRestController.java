package hackhub.rest;

import hackhub.controller.SegnalazioneController;
import hackhub.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per i casi d'uso relativi alle segnalazioni:
 * segnalare una violazione (mentore) e gestire la penalizzazione (organizzatore).
 * Delega al coordinatore GRASP {@link SegnalazioneController}.
 */
@RestController
@RequestMapping("/api/segnalazioni")
public class SegnalazioneRestController {

    private final SegnalazioneController segnalazioneController;

    public SegnalazioneRestController(SegnalazioneController segnalazioneController) {
        this.segnalazioneController = segnalazioneController;
    }

    /**
     * GET /api/segnalazioni/modulo — Dati per il modulo di segnalazione del mentore.
     */
    @GetMapping("/modulo")
    public ResponseEntity<ModuloSegnalazioneDTO> getDatiModulo(
            @RequestParam("hackathon") UUID hackathonId) {
        return ResponseEntity.ok(segnalazioneController.getDatiModulo(hackathonId));
    }

    /**
     * POST /api/segnalazioni — Invia una nuova segnalazione di violazione.
     */
    @PostMapping
    public ResponseEntity<SegnalazioneResponseDTO> inviaSegnalazione(
            @RequestBody SegnalazioneRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(segnalazioneController.inviaSegnalazione(request));
    }

    /**
     * GET /api/segnalazioni/{id} — Dettagli di una segnalazione.
     */
    @GetMapping("/{id}")
    public ResponseEntity<SegnalazioneDettagliDTO> getDettagli(@PathVariable UUID id) {
        return ResponseEntity.ok(segnalazioneController.getDettagliSegnalazione(id));
    }

    /**
     * PUT /api/segnalazioni/gestione — Decisione dell'organizzatore su una segnalazione.
     */
    @PutMapping("/gestione")
    public ResponseEntity<SegnalazioneGestioneResponseDTO> gestisciSegnalazione(
            @RequestBody SegnalazioneGestioneDTO dto) {
        return ResponseEntity.ok(segnalazioneController.gestisciSegnalazione(dto));
    }
}
