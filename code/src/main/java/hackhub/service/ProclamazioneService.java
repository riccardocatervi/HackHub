package hackhub.service;

import hackhub.dto.ProclamazioneFormDTO;
import hackhub.dto.ProclamazioneResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;

import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Proclamare Vincitore'.
 * Coordina il controllo dello stato tramite il pattern State, la selezione
 * del vincitore per punteggio, la transizione a CONCLUSO e le notifiche.
 */
public class ProclamazioneService {

    private final HackathonRepository     hackathonRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final TeamRepository          teamRepository;
    private final NotificationsService    notificationsService;

    public ProclamazioneService(HackathonRepository hackathonRepository,
                                SottomissioneRepository sottomissioneRepository,
                                TeamRepository teamRepository,
                                NotificationsService notificationsService) {
        this.hackathonRepository     = hackathonRepository;
        this.sottomissioneRepository = sottomissioneRepository;
        this.teamRepository          = teamRepository;
        this.notificationsService    = notificationsService;
    }

    /**
     * Carica i dati del potenziale vincitore per mostrare il form di conferma.
     * Verifica che l'hackathon sia in stato IN_VALUTAZIONE tramite il pattern State.
     */
    public ProclamazioneFormDTO preparaProclamazione(UUID idHackathon) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
            .orElseThrow(() -> new HackathonNotFoundException(idHackathon));

        // Guard del pattern State: lancia IllegalStateTransitionException se non IN_VALUTAZIONE
        hackathon.verificaAccettaProclamazione();

        Sottomissione candidataVincitrice = sottomissioneRepository.findVincitore(idHackathon)
            .orElseThrow(() -> new RuntimeException(
                "Nessuna sottomissione valutata trovata per l'hackathon: " + idHackathon));

        Team teamVincitore = teamRepository.findById(candidataVincitrice.getIdTeam())
            .orElseThrow(() -> new TeamNotFoundException(candidataVincitrice.getIdTeam()));

        return new ProclamazioneFormDTO(
            hackathon.getId(),
            hackathon.getNome(),
            teamVincitore.getId(),
            teamVincitore.getNome(),
            hackathon.getPremio()
        );
    }

    /**
     * Esegue la proclamazione ufficiale del team vincitore.
     *
     * Passi:
     * 1. Carica l'hackathon e verifica lo stato tramite il pattern State.
     * 2. Individua la sottomissione con il voto più alto.
     * 3. Segna la sottomissione come vincitrice nel database.
     * 4. Transiziona l'hackathon a CONCLUSO e persiste il nuovo stato.
     * 5. Invia le notifiche ai partecipanti.
     */
    public ProclamazioneResponseDTO eseguiProclamazione(UUID idHackathon) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
            .orElseThrow(() -> new HackathonNotFoundException(idHackathon));

        // Guard del pattern State: lancia IllegalStateTransitionException se non IN_VALUTAZIONE
        hackathon.verificaAccettaProclamazione();

        Sottomissione vincitrice = sottomissioneRepository.findVincitore(idHackathon)
            .orElseThrow(() -> new RuntimeException(
                "Nessuna sottomissione valutata trovata per l'hackathon: " + idHackathon));

        Team teamVincitore = teamRepository.findById(vincitrice.getIdTeam())
            .orElseThrow(() -> new TeamNotFoundException(vincitrice.getIdTeam()));

        // Segna la sottomissione come vincitrice nel DB
        sottomissioneRepository.markAsVincitore(vincitrice.getId());

        // Transizione di stato: IN_VALUTAZIONE → CONCLUSO (delegata al Context)
        hackathon.concludi();
        hackathonRepository.updateStato(hackathon.getId(), hackathon.getStatoEnum());

        // Notifica i partecipanti
        notificationsService.notificaProclamazione(hackathon, teamVincitore);

        return new ProclamazioneResponseDTO(
            hackathon.getId(),
            hackathon.getNome(),
            teamVincitore.getId(),
            teamVincitore.getNome(),
            hackathon.getPremio(),
            hackathon.getStatoEnum()
        );
    }
}
