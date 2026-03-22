package hackhub.rest;

import hackhub.controller.SupportRequestController;
import hackhub.dto.CallFormDTO;
import hackhub.dto.EsitoGestioneRichiestaDTO;
import hackhub.dto.RichiestaSupportoDettagliDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Prendere in carico una richiesta di supporto'.
 * Delega al coordinatore GRASP {@link SupportRequestController}.
 */
@RestController
@RequestMapping("/api/richieste-supporto")
public class SupportRequestRestController {

    private final SupportRequestController supportRequestController;

    public SupportRequestRestController(SupportRequestController supportRequestController) {
        this.supportRequestController = supportRequestController;
    }

    /**
     * GET /api/richieste-supporto/pendenti/{idMentore} — Lista richieste pendenti del mentore.
     */
    @GetMapping("/pendenti/{idMentore}")
    public ResponseEntity<List<RichiestaSupportoDettagliDTO>> getRichiestePendenti(
            @PathVariable UUID idMentore) {
        return ResponseEntity.ok(supportRequestController.getRichiestePendenti(idMentore));
    }

    /**
     * GET /api/richieste-supporto/{idRichiesta} — Dettagli di una richiesta di supporto.
     */
    @GetMapping("/{idRichiesta}")
    public ResponseEntity<RichiestaSupportoDettagliDTO> getDettagliRichiesta(
            @PathVariable UUID idRichiesta,
            @RequestParam("mentore") UUID idMentore) {
        return ResponseEntity.ok(
                supportRequestController.getDettagliRichiesta(idRichiesta, idMentore));
    }

    /**
     * POST /api/richieste-supporto/{idRichiesta}/accetta — Accetta e avvia pianificazione call.
     */
    @PostMapping("/{idRichiesta}/accetta")
    public ResponseEntity<CallFormDTO> accettaRichiesta(
            @PathVariable UUID idRichiesta,
            @RequestParam("mentore") UUID idMentore) {
        return ResponseEntity.ok(
                supportRequestController.accettaRichiesta(idRichiesta, idMentore));
    }

    /**
     * POST /api/richieste-supporto/{idRichiesta}/respingi — Rifiuta una richiesta con motivazione.
     */
    @PostMapping("/{idRichiesta}/respingi")
    public ResponseEntity<EsitoGestioneRichiestaDTO> respingiRichiesta(
            @PathVariable UUID idRichiesta,
            @RequestParam("mentore") UUID idMentore,
            @RequestParam("motivazione") String motivazione) {
        return ResponseEntity.ok(
                supportRequestController.respingiRichiesta(idRichiesta, idMentore, motivazione));
    }
}
