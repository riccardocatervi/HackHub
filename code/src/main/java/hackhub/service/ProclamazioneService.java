package hackhub.service;

import hackhub.dto.ProclamazioneFormDTO;
import hackhub.dto.ProclamazioneResponseDTO;
import hackhub.exception.EntityNotFoundException;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.model.entity.Utente;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;

import java.util.UUID;

/**
 * Servizio per i casi d'uso:
 * <ul>
 *   <li>'Proclamare team vincitore di un hackathon' — {@link #preparaProclamazione}, {@link #eseguiProclamazione}</li>
 *   <li>'Erogare premio al team vincitore' — integrato in {@link #eseguiProclamazione} tramite {@link PaymentService}</li>
 * </ul>
 * <p>
 * Flusso di {@link #eseguiProclamazione}:
 * <ol>
 *   <li>Carica hackathon e verifica lo stato tramite il pattern State.</li>
 *   <li>Individua la sottomissione con il voto più alto.</li>
 *   <li>Segna la sottomissione come vincitrice nel database.</li>
 *   <li>Transiziona l'hackathon a CONCLUSO e persiste il nuovo stato.</li>
 *   <li>Eroga il premio al leader del team vincitore (se {@link PaymentService} configurato).</li>
 *   <li>Persiste il team vincitore e lo stato di erogazione del premio sull'hackathon.</li>
 *   <li>Invia le notifiche ai partecipanti.</li>
 * </ol>
 */
public class ProclamazioneService {

    private final HackathonRepository hackathonRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final TeamRepository teamRepository;
    private final NotificationsService notificationsService;
    private final PaymentService paymentService;   // può essere null (retro-compatibilità)
    private final UserRepository userRepository;   // può essere null (retro-compatibilità)

    // -------------------------------------------------------------------------
    // Costruttori
    // -------------------------------------------------------------------------

    /**
     * Costruttore retro-compatibile (4 parametri).
     * Utilizzato dai test esistenti: il pagamento viene saltato perché
     * {@code paymentService} e {@code userRepository} sono null.
     */
    public ProclamazioneService(HackathonRepository hackathonRepository,
                                SottomissioneRepository sottomissioneRepository,
                                TeamRepository teamRepository,
                                NotificationsService notificationsService) {
        this(hackathonRepository, sottomissioneRepository, teamRepository,
                notificationsService, null, null);
    }

    /**
     * Costruttore completo per la produzione.
     * Con {@code paymentService} e {@code userRepository} valorizzati, il premio
     * viene erogato automaticamente al leader del team vincitore durante la proclamazione.
     */
    public ProclamazioneService(HackathonRepository hackathonRepository,
                                SottomissioneRepository sottomissioneRepository,
                                TeamRepository teamRepository,
                                NotificationsService notificationsService,
                                PaymentService paymentService,
                                UserRepository userRepository) {
        this.hackathonRepository = hackathonRepository;
        this.sottomissioneRepository = sottomissioneRepository;
        this.teamRepository = teamRepository;
        this.notificationsService = notificationsService;
        this.paymentService = paymentService;
        this.userRepository = userRepository;
    }

    // -------------------------------------------------------------------------
    // Metodi di business
    // -------------------------------------------------------------------------

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
     * Include l'erogazione del premio se il servizio di pagamento è configurato.
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

        // Imposta il team vincitore sull'hackathon
        hackathon.setIdTeamVincitore(teamVincitore.getId());

        // Eroga il premio al leader del team vincitore (caso d'uso incluso)
        boolean premioDisbursed = false;
        String emailLeader = null;
        if (paymentService != null && userRepository != null) {
            Utente leader = userRepository.findById(teamVincitore.getIdLeader())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Leader del team vincitore non trovato: " + teamVincitore.getIdLeader()));
            emailLeader = leader.getEmail();
            paymentService.disbursePrize(emailLeader, hackathon.getPremio(),
                    hackathon.getId(), teamVincitore.getId());
            premioDisbursed = true;
        }

        // Persiste team vincitore e stato di erogazione del premio
        hackathon.setPremioDisbursed(premioDisbursed);
        hackathonRepository.updateVincitoreEPremio(hackathon.getId(),
                teamVincitore.getId(),
                premioDisbursed);

        // Notifica i partecipanti (comportamento esistente — firma invariata)
        notificationsService.notificaProclamazione(hackathon, teamVincitore);

        // Notifica aggiuntiva con dettaglio del premio erogato
        notificationsService.notificaVincitoreEPartecipanti(hackathon, teamVincitore,
                emailLeader, premioDisbursed);

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
