package hackhub.rest;

import hackhub.controller.ValutazioneController;
import hackhub.dto.SottomissioniDaValutareDTO;
import hackhub.dto.ValutazioneRequestDTO;
import hackhub.dto.ValutazioneResponseDTO;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Valutare sottomissione di un team'.
 * Delega al coordinatore GRASP {@link ValutazioneController}.
 */
@RestController
@RequestMapping("/api/valutazioni")
public class ValutazioneRestController {

    private final ValutazioneController valutazioneController;

    public ValutazioneRestController(ValutazioneController valutazioneController) {
        this.valutazioneController = valutazioneController;
    }

    /**
     * GET /api/valutazioni — Dashboard sottomissioni da valutare per il giudice.
     */
    @GetMapping
    public ResponseEntity<SottomissioniDaValutareDTO> getSottomissioniDaValutare(
            @RequestParam("hackathon") UUID hackathonId,
            @RequestParam("giudice") UUID giudiceId) {
        return ResponseEntity.ok(
                valutazioneController.richiediSottomissioniDaValutare(hackathonId, giudiceId));
    }

    /**
     * POST /api/valutazioni — Invia una valutazione.
     */
    @PostMapping
    public ResponseEntity<ValutazioneResponseDTO> inviaValutazione(
            @RequestBody ValutazioneRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(valutazioneController.inviaValutazione(request));
    }
}
