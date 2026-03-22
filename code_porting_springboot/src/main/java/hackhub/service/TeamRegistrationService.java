package hackhub.service;

import hackhub.dto.UnsubscriptionSuccessDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoHackathon;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Servizio per il caso d'uso "Gestire iscrizione del team all'Hackathon".
 * <p>
 * Gestisce la disiscrizione di un team da un hackathon, verificando che il richiedente
 * sia il leader del team e che l'hackathon sia ancora in fase di iscrizione.
 * Al termine, elimina il team con tutte le sue associazioni e notifica tutti i membri.
 */
@Service
public class TeamRegistrationService {

    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;
    private final InvitoRepository invitoRepository;
    private final NotificationsService notificationsService;

    public TeamRegistrationService(TeamRepository teamRepository,
                                   HackathonRepository hackathonRepository,
                                   InvitoRepository invitoRepository,
                                   NotificationsService notificationsService) {
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
        this.invitoRepository = invitoRepository;
        this.notificationsService = notificationsService;
    }

    /**
     * Processa la disiscrizione del team dall'hackathon.
     * <p>
     * Sequenza di operazioni:
     * <ol>
     *   <li>Verifica esistenza del team.</li>
     *   <li>Verifica esistenza dell'hackathon associato.</li>
     *   <li>Verifica che il richiedente sia il leader del team.</li>
     *   <li>Verifica che l'hackathon sia in stato IN_ISCRIZIONE.</li>
     *   <li>Carica la lista dei membri per le notifiche successive.</li>
     *   <li>Elimina gli inviti associati al team.</li>
     *   <li>Elimina il team con le associazioni dei membri.</li>
     *   <li>Notifica tutti i membri dell'avvenuta disiscrizione e scioglimento.</li>
     * </ol>
     *
     * @param idTeam      id del team da disiscrivere
     * @param requesterId id dell'utente che effettua la richiesta (deve essere il leader)
     * @return DTO di conferma della disiscrizione avvenuta con successo
     * @throws TeamNotFoundException          se il team non esiste
     * @throws HackathonNotFoundException     se l'hackathon associato non esiste
     * @throws UnauthorizedActionException    se il richiedente non è il leader del team
     * @throws InvalidHackathonStateException se l'hackathon non è in stato IN_ISCRIZIONE
     */
    public UnsubscriptionSuccessDTO processTeamUnsubscription(UUID idTeam,
                                                              UUID requesterId) {
        Team team = teamRepository.findById(idTeam)
                .orElseThrow(() -> new TeamNotFoundException(idTeam));

        Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + team.getIdHackathon()));

        if (!team.getIdLeader().equals(requesterId)) {
            throw new UnauthorizedActionException(
                    "Solo il leader del team può disiscrivere il team dall'hackathon. " +
                            "Richiedente: " + requesterId + ", leader: " + team.getIdLeader());
        }

        if (hackathon.getStatoEnum() != StatoHackathon.IN_ISCRIZIONE) {
            throw new InvalidHackathonStateException(
                    "Impossibile disiscrivere il team: l'hackathon '" + hackathon.getNome() +
                            "' non è in fase di iscrizione. Stato attuale: " + hackathon.getStatoEnum());
        }

        // Carica i membri prima dell'eliminazione per poterli notificare
        List<MembroTeam> membri = teamRepository.findMembri(idTeam);

        // Eliminazione in sequenza: prima gli inviti (FK), poi team + membro_team
        invitoRepository.deleteByTeam(idTeam);
        teamRepository.delete(team);

        // Notifica tutti i membri dell'avvenuto scioglimento del team
        notificationsService.notifyTeamUnsubscription(membri, team, hackathon);

        return new UnsubscriptionSuccessDTO(
                hackathon.getId(),
                hackathon.getNome(),
                team.getId(),
                team.getNome(),
                "Il team '" + team.getNome() + "' si è disiscritto dall'hackathon '" +
                        hackathon.getNome() + "' ed è stato sciolto. Tutti i membri sono stati notificati."
        );
    }
}
