package hackhub.rest;

import hackhub.controller.HackathonController;
import hackhub.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint REST per i casi d'uso relativi agli hackathon:
 * creazione, gestione mentori, visualizzazione pubblica e iscritti.
 * Delega al coordinatore GRASP {@link HackathonController}.
 */
@RestController
@RequestMapping("/api/hackathons")
public class HackathonRestController {

    private final HackathonController hackathonController;

    public HackathonRestController(HackathonController hackathonController) {
        this.hackathonController = hackathonController;
    }

    // --- Visualizzazione pubblica ---

    /**
     * GET /api/hackathons — Lista di tutti gli hackathon pubblicamente disponibili.
     */
    @GetMapping
    public ResponseEntity<List<HackathonSummaryDTO>> getHackathonDisponibili() {
        return ResponseEntity.ok(hackathonController.richiediListaHackathon());
    }

    /**
     * GET /api/hackathons/{id} — Dettagli pubblici di un hackathon.
     */
    @GetMapping("/{id}")
    public ResponseEntity<HackathonResponseDTO> getDettagliHackathon(@PathVariable UUID id) {
        return ResponseEntity.ok(hackathonController.richiediDettagliHackathon(id));
    }

    // --- Creazione hackathon ---

    /**
     * GET /api/hackathons/form — Dati per il form di creazione hackathon.
     */
    @GetMapping("/form")
    public ResponseEntity<HackathonFormDataDTO> getFormCreazione(
            @RequestParam("organizzatore") UUID idOrganizzatore) {
        return ResponseEntity.ok(hackathonController.richiediFormCreazione(idOrganizzatore));
    }

    /**
     * POST /api/hackathons — Crea un nuovo hackathon.
     */
    @PostMapping
    public ResponseEntity<HackathonResponseDTO> creaHackathon(
            @RequestBody HackathonSubmissionDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(hackathonController.creaHackathon(dto));
    }

    // --- Gestione mentori ---

    /**
     * GET /api/hackathons/organizzatore/{idOrganizzatore} — Lista hackathon dell'organizzatore.
     */
    @GetMapping("/organizzatore/{idOrganizzatore}")
    public ResponseEntity<List<HackathonSummaryDTO>> getHackathonByOrganizzatore(
            @PathVariable UUID idOrganizzatore) {
        return ResponseEntity.ok(hackathonController.richiediListaHackathon(idOrganizzatore));
    }

    /**
     * GET /api/hackathons/{id}/dettagli — Dettagli completi (inclusi mentori) per l'organizzatore.
     */
    @GetMapping("/{id}/dettagli")
    public ResponseEntity<HackathonResponseDTO> selezionaHackathon(@PathVariable UUID id) {
        return ResponseEntity.ok(hackathonController.selezionaHackathon(id));
    }

    /**
     * GET /api/hackathons/{id}/mentori-disponibili — Mentori aggiungibili all'hackathon.
     */
    @GetMapping("/{id}/mentori-disponibili")
    public ResponseEntity<List<MentoreDTO>> getMentoriDisponibili(@PathVariable UUID id) {
        return ResponseEntity.ok(hackathonController.richiediModificaMentori(id));
    }

    /**
     * PUT /api/hackathons/mentori — Aggiorna la lista mentori dell'hackathon.
     */
    @PutMapping("/mentori")
    public ResponseEntity<HackathonUpdatedDTO> aggiornaMentori(
            @RequestBody ModificaMentoriRequestDTO dto) {
        return ResponseEntity.ok(hackathonController.confermaModificheLista(dto));
    }

    // --- Hackathon a cui l'utente è iscritto ---

    /**
     * GET /api/hackathons/iscritti/{idUtente} — Hackathon a cui l'utente partecipa.
     */
    @GetMapping("/iscritti/{idUtente}")
    public ResponseEntity<List<HackathonSummaryDTO>> getHackathonIscritti(
            @PathVariable UUID idUtente) {
        return ResponseEntity.ok(hackathonController.getTeamHackathons(idUtente));
    }

    /**
     * GET /api/hackathons/{idHackathon}/iscritti/{idTeam} — Dettagli hackathon per utente iscritto.
     */
    @GetMapping("/{idHackathon}/iscritti/{idTeam}")
    public ResponseEntity<HackathonResponseDTO> getHackathonDetailsIscritto(
            @PathVariable UUID idHackathon,
            @PathVariable UUID idTeam,
            @RequestParam("utente") UUID idUtente) {
        return ResponseEntity.ok(
                hackathonController.getHackathonDetails(idHackathon, idTeam, idUtente));
    }
}
