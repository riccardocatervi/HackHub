package hackhub.rest;

import hackhub.controller.AggiornamentoSottomissioneController;
import hackhub.dto.SottomissioneAggiornamentoFormDTO;
import hackhub.dto.SottomissioneUpdateDTO;
import hackhub.dto.SottomissioneUpdateResponseDTO;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Endpoint REST per il caso d'uso 'Aggiornare Sottomissione del Team'.
 * Delega al coordinatore GRASP {@link AggiornamentoSottomissioneController}.
 */
@RestController
@RequestMapping("/api/sottomissioni/aggiornamento")
public class AggiornamentoSottomissioneRestController {

    private final AggiornamentoSottomissioneController aggiornamentoController;

    public AggiornamentoSottomissioneRestController(
            AggiornamentoSottomissioneController aggiornamentoController) {
        this.aggiornamentoController = aggiornamentoController;
    }

    /**
     * GET /api/sottomissioni/aggiornamento/form — Dati correnti per il form di modifica.
     */
    @GetMapping("/form")
    public ResponseEntity<SottomissioneAggiornamentoFormDTO> getFormAggiornamento(
            @RequestParam("hackathon") UUID idHackathon,
            @RequestParam("team") UUID idTeam) {
        return ResponseEntity.ok(
                aggiornamentoController.richiediFormAggiornamento(idHackathon, idTeam));
    }

    /**
     * PUT /api/sottomissioni/aggiornamento — Aggiorna la sottomissione del team.
     */
    @PutMapping
    public ResponseEntity<SottomissioneUpdateResponseDTO> aggiornaSottomissione(
            @RequestBody SottomissioneUpdateDTO dto) {
        return ResponseEntity.ok(aggiornamentoController.aggiornaSottomissione(dto));
    }
}
