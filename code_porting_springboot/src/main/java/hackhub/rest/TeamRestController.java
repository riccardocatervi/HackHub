package hackhub.rest;

import hackhub.controller.TeamController;
import hackhub.dto.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint REST per i casi d'uso relativi ai team:
 * creazione, risposta inviti, abbandono e visualizzazione.
 * Delega al coordinatore GRASP {@link TeamController}.
 */
@RestController
@RequestMapping("/api/teams")
public class TeamRestController {

    private final TeamController teamController;

    public TeamRestController(TeamController teamController) {
        this.teamController = teamController;
    }

    /**
     * GET /api/teams/hackathon-disponibili — Hackathon aperti alle iscrizioni.
     */
    @GetMapping("/hackathon-disponibili")
    public ResponseEntity<List<HackathonResponseDTO>> getHackathonDisponibili() {
        return ResponseEntity.ok(teamController.getHackathonDisponibili());
    }

    /**
     * GET /api/teams/form — Dati per il form di creazione team.
     */
    @GetMapping("/form")
    public ResponseEntity<TeamFormDTO> getCreateTeamForm(
            @RequestParam("hackathon") UUID idHackathon,
            @RequestParam("leader") UUID idLeader) {
        return ResponseEntity.ok(teamController.getCreateTeamForm(idHackathon, idLeader));
    }

    /**
     * POST /api/teams — Crea un nuovo team con inviti ai membri.
     */
    @PostMapping
    public ResponseEntity<TeamResponseDTO> createTeam(@RequestBody CreateTeamRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(teamController.createTeam(dto));
    }

    /**
     * POST /api/teams/rispondi-invito — Risposta a un invito al team.
     */
    @PostMapping("/rispondi-invito")
    public ResponseEntity<InvitationResponseDTO> rispondiInvito(
            @RequestBody RispostaInvitoDTO dto) {
        return ResponseEntity.ok(teamController.rispondiInvito(dto));
    }

    // --- Gestione partecipazione ---

    /**
     * GET /api/teams/partecipazione/{idMembro} — Dettagli di partecipazione di un membro.
     */
    @GetMapping("/partecipazione/{idMembro}")
    public ResponseEntity<DettagliPartecipazioneDTO> getDettagliPartecipazione(
            @PathVariable UUID idMembro) {
        return ResponseEntity.ok(teamController.richiediDettagliPartecipazione(idMembro));
    }

    /**
     * DELETE /api/teams/{idTeam}/membri/{idMembro} — Abbandono del team.
     */
    @DeleteMapping("/{idTeam}/membri/{idMembro}")
    public ResponseEntity<GestioneTeamResponseDTO> abbandonaTeam(
            @PathVariable UUID idTeam,
            @PathVariable UUID idMembro) {
        return ResponseEntity.ok(teamController.confermaAbbandonoTeam(idTeam, idMembro));
    }

    // --- Visualizzazione team ---

    /**
     * GET /api/teams/utente/{idUtente} — Lista dei team dell'utente.
     */
    @GetMapping("/utente/{idUtente}")
    public ResponseEntity<List<TeamSummaryDTO>> getUserTeams(@PathVariable UUID idUtente) {
        return ResponseEntity.ok(teamController.getUserTeams(idUtente));
    }

    /**
     * GET /api/teams/{idTeam} — Dettagli completi di un team.
     */
    @GetMapping("/{idTeam}")
    public ResponseEntity<TeamDetailsDTO> getTeamDetails(
            @PathVariable UUID idTeam,
            @RequestParam("utente") UUID idUtente) {
        return ResponseEntity.ok(teamController.getTeamDetails(idTeam, idUtente));
    }
}
