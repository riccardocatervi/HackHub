package hackhub.service;

import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSummaryDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.TeamRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Visualizzare hackathon a cui l'utente è iscritto'.
 * <p>
 * Flusso principale:
 * <ol>
 *   <li>L'utente autenticato richiede la lista degli hackathon a cui i propri team risultano iscritti.</li>
 *   <li>Il sistema recupera tutti i team dell'utente e, per ciascuno, l'hackathon associato.</li>
 *   <li>Se l'utente non è iscritto ad alcun hackathon, viene sollevata {@link HackathonNotFoundException}.</li>
 *   <li>L'utente seleziona un hackathon specifico per visualizzarne i dettagli.</li>
 * </ol>
 */
public class VisualizzaHackathonIscrittoService {

    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;

    public VisualizzaHackathonIscrittoService(TeamRepository teamRepository,
                                              HackathonRepository hackathonRepository) {
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Lista hackathon a cui l'utente è iscritto
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista sintetica di tutti gli hackathon a cui l'utente
     * partecipa tramite i propri team.
     * <p>
     * Recupera tutti i team dell'utente e ne estrae l'hackathon associato.
     *
     * @param idUtente l'id dell'utente autenticato
     * @return lista di {@link HackathonSummaryDTO} degli hackathon a cui l'utente è iscritto
     * @throws HackathonNotFoundException se l'utente non è iscritto ad alcun hackathon
     */
    public List<HackathonSummaryDTO> getHackathonsByUserId(UUID idUtente) {
        List<Team> teams = teamRepository.findAllByMembro(idUtente);

        if (teams.isEmpty()) {
            throw new HackathonNotFoundException(
                    "L'utente " + idUtente + " non risulta iscritto ad alcun hackathon.");
        }

        List<HackathonSummaryDTO> risultato = new ArrayList<>();
        for (Team team : teams) {
            Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                    .orElseThrow(() -> new HackathonNotFoundException(
                            "Hackathon non trovato per il team " + team.getId() +
                                    ": " + team.getIdHackathon()));

            risultato.add(new HackathonSummaryDTO(
                    hackathon.getId(),
                    hackathon.getNome(),
                    hackathon.getStatoEnum(),
                    hackathon.getDataInizio(),
                    hackathon.getDataFine()
            ));
        }
        return risultato;
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Dettagli hackathon iscritto (selezione dalla lista)
    // -----------------------------------------------------------------------

    /**
     * Restituisce i dettagli completi di un hackathon selezionato dalla lista
     * degli hackathon a cui il team dell'utente è iscritto.
     * <p>
     * Verifica che il team esista e che l'utente ne sia membro prima di restituire i dati.
     * A differenza della vista pubblica, mostra i dettagli anche per hackathon conclusi.
     *
     * @param idHackathon l'id dell'hackathon selezionato
     * @param idTeam      l'id del team tramite cui l'utente è iscritto
     * @param idUtente    l'id dell'utente autenticato
     * @return {@link HackathonResponseDTO} con tutti i dati dell'hackathon
     * @throws TeamNotFoundException       se il team non esiste
     * @throws UnauthorizedActionException se l'utente non è membro del team
     * @throws HackathonNotFoundException  se l'hackathon non esiste
     */
    public HackathonResponseDTO getHackathonDetails(UUID idHackathon, UUID idTeam, UUID idUtente) {
        // Verifica esistenza team e appartenenza utente (Information Expert)
        Team team = teamRepository.findById(idTeam)
                .orElseThrow(() -> new TeamNotFoundException(idTeam));

        List<MembroTeam> membri = teamRepository.findMembri(idTeam);
        team.setMembri(membri);

        if (!team.hasMember(idUtente)) {
            throw new UnauthorizedActionException(
                    "L'utente " + idUtente + " non è membro del team " + idTeam +
                            " e non può visualizzare i dettagli dell'hackathon.");
        }

        Hackathon hackathon = hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + idHackathon));

        return toResponseDTO(hackathon);
    }

    // -----------------------------------------------------------------------
    // Mapping (GRASP: Information Expert)
    // -----------------------------------------------------------------------

    private HackathonResponseDTO toResponseDTO(Hackathon h) {
        return new HackathonResponseDTO(
                h.getId(),
                h.getNome(),
                h.getDataInizio(),
                h.getDataFine(),
                h.getScadenzaIscrizioni(),
                h.getScadenzaSottomissioni(),
                h.getPremio(),
                h.getLuogo(),
                h.getDimensioneMaxTeam(),
                h.getRegolamento(),
                h.getIdOrganizzatore(),
                h.getIdGiudice(),
                h.getIdsMentori(),
                h.getStatoEnum()
        );
    }
}
