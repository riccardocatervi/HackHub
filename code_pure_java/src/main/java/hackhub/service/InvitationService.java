package hackhub.service;

import hackhub.dto.InvitationResponseDTO;
import hackhub.dto.InviteCreatedDTO;
import hackhub.dto.InvitoListaItemDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvitationNotFoundException;
import hackhub.exception.InvalidInvitationStateException;
import hackhub.exception.TeamCapacityExceededException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.UserAlreadyInTeamException;
import hackhub.exception.UserNotFoundException;
import hackhub.exception.UserNotEligibleException;
import hackhub.model.StatoInvito;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.Team;
import hackhub.model.entity.Utente;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;
import hackhub.service.observer.InvitationObserver;
import hackhub.service.observer.NuovoInvitoObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per il caso d'uso "Accettare invito a unirsi al team".
 * <p>
 * Coordina la risposta agli inviti pendenti: mostra la lista degli inviti IN_ATTESA,
 * verifica i requisiti di business (unicità per hackathon) e delega le notifiche
 * agli observer registrati tramite il pattern GoF Observer ({@link InvitationObserver}).
 */
public class InvitationService {

    private final InvitoRepository invitoRepository;
    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final List<InvitationObserver> observers = new ArrayList<>();
    private final List<NuovoInvitoObserver> nuovoInvitoObservers = new ArrayList<>();

    public InvitationService(InvitoRepository invitoRepository,
                             TeamRepository teamRepository,
                             HackathonRepository hackathonRepository,
                             UserRepository userRepository) {
        this.invitoRepository = invitoRepository;
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
    }

    /**
     * Registra un observer per gli eventi di risposta agli inviti.
     *
     * @param observer implementazione che riceverà le notifiche
     */
    public void addObserver(InvitationObserver observer) {
        this.observers.add(observer);
    }

    // -------------------------------------------------------------------------
    // Caso d'uso principale
    // -------------------------------------------------------------------------

    /**
     * Restituisce tutti gli inviti in stato IN_ATTESA per l'utente specificato.
     * Per ogni invito carica i dati del team, dell'hackathon e del leader creatore.
     *
     * @param idUtente id dell'utente autenticato
     * @return lista degli inviti pendenti con i dati di contesto
     */
    public List<InvitoListaItemDTO> getPendingInvitations(UUID idUtente) {
        List<Invito> inviti = invitoRepository.findPendingByUtente(idUtente);
        List<InvitoListaItemDTO> risultato = new ArrayList<>();

        for (Invito invito : inviti) {
            Team team = teamRepository.findById(invito.getIdTeam())
                    .orElseThrow(() -> new TeamNotFoundException(invito.getIdTeam()));

            Hackathon hackathon = hackathonRepository.findById(invito.getIdHackathon())
                    .orElseThrow(() -> new HackathonNotFoundException(
                            "Hackathon non trovato: " + invito.getIdHackathon()));

            String nomeLeader = userRepository.findById(team.getIdLeader())
                    .map(u -> u.getNome() + " " + u.getCognome())
                    .orElse("N/D");

            risultato.add(new InvitoListaItemDTO(
                    invito.getId(),
                    team.getId(),
                    team.getNome(),
                    hackathon.getId(),
                    hackathon.getNome(),
                    team.getIdLeader(),
                    nomeLeader,
                    invito.getDataInvio()
            ));
        }

        return risultato;
    }

    /**
     * Processa la risposta di un utente a un invito (accettazione o rifiuto).
     * <p>
     * Flusso accettazione:
     * <ol>
     *   <li>Verifica esistenza e proprietà dell'invito.</li>
     *   <li>Verifica che l'invito sia IN_ATTESA.</li>
     *   <li>Verifica che l'utente non sia già in un team per lo stesso hackathon.</li>
     *   <li>Aggiorna lo stato dell'invito ad ACCETTATO.</li>
     *   <li>Aggiunge l'utente al team come membro effettivo.</li>
     *   <li>Notifica il creatore del team.</li>
     * </ol>
     * <p>
     * Flusso rifiuto: aggiorna lo stato a RIFIUTATO e notifica il creatore.
     *
     * @param idInvito  id dell'invito a cui rispondere
     * @param idUtente  id dell'utente autenticato che risponde
     * @param accettato true per accettare, false per rifiutare
     * @return DTO con il nuovo stato dell'invito e un messaggio esplicativo
     * @throws InvitationNotFoundException     se l'invito non esiste
     * @throws UnauthorizedActionException     se l'utente non è il destinatario dell'invito
     * @throws InvalidInvitationStateException se l'invito non è IN_ATTESA
     * @throws UserAlreadyInTeamException      se l'utente è già in un team per lo stesso hackathon
     */
    public InvitationResponseDTO processResponse(UUID idInvito, UUID idUtente, boolean accettato) {
        Invito invito = invitoRepository.findById(idInvito)
                .orElseThrow(() -> new InvitationNotFoundException(idInvito));

        if (!invito.getIdUtente().equals(idUtente)) {
            throw new UnauthorizedActionException(
                    "L'utente " + idUtente + " non è il destinatario dell'invito " + idInvito);
        }

        if (invito.getStato() != StatoInvito.IN_ATTESA) {
            throw new InvalidInvitationStateException(invito.getStato());
        }

        if (accettato && !userRepository.isUserAvailable(idUtente, invito.getIdHackathon())) {
            throw new UserAlreadyInTeamException(
                    "L'utente " + idUtente + " è già membro di un team per questo hackathon " +
                            "(id: " + invito.getIdHackathon() + ").");
        }

        StatoInvito nuovoStato = accettato ? StatoInvito.ACCETTATO : StatoInvito.RIFIUTATO;
        invitoRepository.aggiornaStato(idInvito, nuovoStato);
        invito.setStato(nuovoStato);

        if (accettato) {
            teamRepository.addMembro(invito.getIdTeam(), idUtente);
        }

        Team team = teamRepository.findById(invito.getIdTeam())
                .orElseThrow(() -> new TeamNotFoundException(invito.getIdTeam()));

        Hackathon hackathon = hackathonRepository.findById(invito.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + invito.getIdHackathon()));

        String nomeInvitato = userRepository.findById(idUtente)
                .map(u -> u.getNome() + " " + u.getCognome())
                .orElse("N/D");

        notificaObservers(invito, team.getIdLeader(), nomeInvitato, accettato);

        String messaggio = accettato
                ? "Invito accettato. Sei ora membro del team '" + team.getNome() + "'."
                : "Invito rifiutato. Non sei stato aggiunto al team '" + team.getNome() + "'.";

        return new InvitationResponseDTO(
                invito.getId(),
                team.getId(),
                team.getNome(),
                hackathon.getId(),
                hackathon.getNome(),
                nuovoStato,
                messaggio
        );
    }

    /**
     * Registra un observer per l'evento di creazione di un nuovo invito.
     *
     * @param observer implementazione che riceverà le notifiche
     */
    public void addNuovoInvitoObserver(NuovoInvitoObserver observer) {
        this.nuovoInvitoObservers.add(observer);
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Invitare utente a unirsi al team
    // -----------------------------------------------------------------------

    /**
     * Crea e invia un nuovo invito a un utente per unirsi a un team.
     * <p>
     * Flusso:
     * <ol>
     *   <li>Verifica che il richiedente sia il leader del team.</li>
     *   <li>Verifica la capienza: membri effettivi + inviti pendenti &lt; dimensione max.</li>
     *   <li>Verifica l'esistenza dell'utente target.</li>
     *   <li>Verifica che l'utente non sia già membro e non abbia già un invito pendente.</li>
     *   <li>Persiste il nuovo invito in stato IN_ATTESA.</li>
     *   <li>Notifica l'utente invitato.</li>
     * </ol>
     *
     * @param teamId       l'id del team per cui si invia l'invito
     * @param targetUserId l'id dell'utente da invitare
     * @param requesterId  l'id dell'utente richiedente (deve essere il leader)
     * @return {@link InviteCreatedDTO} con i dettagli dell'invito creato
     * @throws TeamNotFoundException         se il team non esiste
     * @throws UnauthorizedActionException   se il richiedente non è il leader del team
     * @throws TeamCapacityExceededException se il team ha raggiunto la capienza massima
     * @throws UserNotFoundException         se l'utente target non esiste
     * @throws UserNotEligibleException      se l'utente è già membro o ha un invito pendente
     * @throws PersistenceException          se la persistenza del nuovo invito fallisce
     */
    public InviteCreatedDTO createInvite(UUID teamId, UUID targetUserId, UUID requesterId) {
        // Carica il team e verifica che il richiedente sia il leader
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId));

        if (!team.getIdLeader().equals(requesterId)) {
            throw new UnauthorizedActionException(
                    "Solo il leader del team può inviare inviti. " +
                            "L'utente " + requesterId + " non è il leader del team " + teamId);
        }

        // Recupera l'hackathon per controllare la dimensione massima del team
        Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + team.getIdHackathon()));

        // Conta i membri effettivi del team
        int membriAttuali = teamRepository.findMembri(teamId).size();

        // Conta gli inviti pendenti
        int invitatiPendenti = invitoRepository.countPendingByTeam(teamId);

        // Verifica la capienza: membri + pendenti non deve raggiungere la dimensione massima
        if (membriAttuali + invitatiPendenti >= hackathon.getDimensioneMaxTeam()) {
            throw new TeamCapacityExceededException(teamId, hackathon.getDimensioneMaxTeam());
        }

        // Verifica esistenza dell'utente target
        Utente utente = userRepository.findById(targetUserId)
                .orElseThrow(() -> new UserNotFoundException(targetUserId));

        // Verifica che l'utente non sia già membro del team
        boolean giaMembro = teamRepository.findMembri(teamId)
                .stream().anyMatch(m -> m.getId().equals(targetUserId));
        if (giaMembro) {
            throw new UserNotEligibleException(targetUserId,
                    "è già membro effettivo del team " + teamId);
        }

        // Verifica che non esista già un invito pendente per lo stesso utente e team
        if (invitoRepository.existsByUtenteAndTeamAndStato(targetUserId, teamId, StatoInvito.IN_ATTESA)) {
            throw new UserNotEligibleException(targetUserId,
                    "ha già un invito in attesa per il team " + teamId);
        }

        // Crea e persiste il nuovo invito
        Invito nuovoInvito = new Invito(teamId, targetUserId, team.getIdHackathon());
        invitoRepository.save(nuovoInvito);

        // Notifica l'utente invitato
        notificaNuovoInvito(nuovoInvito, utente.getEmail(), team.getNome());

        return new InviteCreatedDTO(
                nuovoInvito.getId(),
                team.getId(),
                team.getNome(),
                utente.getId(),
                utente.getEmail(),
                "Invito inviato con successo all'utente " + utente.getNome() +
                        " " + utente.getCognome() + " per il team '" + team.getNome() + "'."
        );
    }

    // -------------------------------------------------------------------------
    // Notifica Observer (pattern GoF)
    // -------------------------------------------------------------------------

    private void notificaObservers(Invito invito, UUID idCreatore,
                                   String nomeInvitato, boolean accettato) {
        for (InvitationObserver observer : observers) {
            if (accettato) {
                observer.onInvitoAccettato(invito, idCreatore, nomeInvitato);
            } else {
                observer.onInvitoRifiutato(invito, idCreatore, nomeInvitato);
            }
        }
    }

    private void notificaNuovoInvito(Invito invito, String emailDestinatario, String nomeTeam) {
        for (NuovoInvitoObserver observer : nuovoInvitoObservers) {
            observer.onNuovoInvito(invito, emailDestinatario, nomeTeam);
        }
    }
}
