package hackhub.service;

import hackhub.builder.EquipeBuilder;
import hackhub.builder.EquipeCreationResult;
import hackhub.dto.*;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.NoTeamsFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.UserAlreadyInTeamException;
import hackhub.exception.ValidationException;
import hackhub.model.StatoInvito;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoHackathon;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servizio per la gestione del ciclo di vita dei team all'interno di un hackathon.
 * <p>
 * Coordina {@link EquipeBuilder} (pattern GoF Builder) per costruire il team
 * con validazione delle regole di dominio, i repository per la persistenza
 * e il {@link NotificationsService} per le notifiche agli utenti invitati.
 */
public class TeamService {

    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final UserRepository userRepository;
    private final InvitoRepository invitoRepository;
    private final NotificationsService notificationsService;

    public TeamService(HackathonRepository hackathonRepository,
                       TeamRepository teamRepository,
                       UserRepository userRepository,
                       InvitoRepository invitoRepository,
                       NotificationsService notificationsService) {
        this.hackathonRepository = hackathonRepository;
        this.teamRepository = teamRepository;
        this.userRepository = userRepository;
        this.invitoRepository = invitoRepository;
        this.notificationsService = notificationsService;
    }

    /**
     * Restituisce i dati necessari per il form di creazione team:
     * informazioni sull'hackathon e lista degli utenti invitabili
     * (non ancora iscritti ad alcun team per quell'hackathon).
     *
     * @param idHackathon id dell'hackathon selezionato
     * @param idLeader    id dell'utente che vuole creare il team
     * @return DTO con dati dell'hackathon e utenti disponibili
     */
    public TeamFormDTO getCreateTeamForm(UUID idHackathon, UUID idLeader) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + idHackathon));

        verificaStatoIscrizioni(hackathon);
        verificaLeaderDisponibile(idLeader, idHackathon);

        List<UtenteDisponibileDTO> utentiDisponibili = userRepository
                .findAvailableForHackathon(idHackathon)
                .stream()
                .filter(u -> !u.getId().equals(idLeader))
                .map(u -> new UtenteDisponibileDTO(
                        u.getId(), u.getNome(), u.getCognome(), u.getEmail()))
                .toList();

        return new TeamFormDTO(
                hackathon.getId(),
                hackathon.getNome(),
                hackathon.getDimensioneMaxTeam(),
                utentiDisponibili
        );
    }

    /**
     * Crea un nuovo team per l'hackathon specificato, aggiunge il leader
     * come membro effettivo e invia gli inviti agli utenti selezionati.
     * <p>
     * Utilizza il pattern GoF Builder ({@link EquipeBuilder}) per la costruzione
     * del team con validazione delle regole di dominio (dimensione massima, unicità).
     *
     * @param dto dati del form di creazione team
     * @return DTO di risposta con i dati del team creato
     */
    public TeamResponseDTO createTeam(CreateTeamRequestDTO dto) {
        Hackathon hackathon = hackathonRepository.findById(dto.idHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + dto.idHackathon()));

        verificaStatoIscrizioni(hackathon);
        verificaLeaderDisponibile(dto.idLeader(), dto.idHackathon());
        verificaDisponibilitaInvitati(dto.idUtentiInvitati(), dto.idHackathon());

        // Costruzione team + inviti tramite Builder (pattern GoF)
        EquipeBuilder builder = new EquipeBuilder()
                .nome(dto.nome())
                .descrizione(dto.descrizione())
                .leader(dto.idLeader())
                .hackathon(dto.idHackathon(), hackathon.getDimensioneMaxTeam());

        for (UUID idUtente : dto.idUtentiInvitati()) {
            builder.invitaMembro(idUtente);
        }

        EquipeCreationResult risultato = builder.build();
        Team team = risultato.getTeam();

        // Persistenza team
        teamRepository.save(team);

        // Aggiunge il leader come membro effettivo
        teamRepository.addMembro(team.getId(), dto.idLeader());

        // Valorizza idTeam sugli inviti e li persiste
        List<Invito> inviti = risultato.getInviti();
        for (Invito invito : inviti) {
            invito.setIdTeam(team.getId());
            invitoRepository.save(invito);
        }

        // Notifica gli utenti invitati
        notificationsService.inviaInvitiTeam(team, inviti);

        return new TeamResponseDTO(
                team.getId(),
                team.getNome(),
                team.getDescrizione(),
                team.getIdLeader(),
                team.getIdHackathon(),
                inviti.stream().map(Invito::getIdUtente).toList()
        );
    }

    /**
     * Gestisce la risposta di un utente a un invito (accettazione o rifiuto).
     * Se accettato, l'utente viene aggiunto come membro effettivo del team.
     *
     * @param dto DTO con id invito, id utente e risposta (accettato/rifiutato)
     * @return DTO aggiornato con il nuovo stato dell'invito
     */
    public InvitationResponseDTO rispondiInvito(RispostaInvitoDTO dto) {
        Invito invito = invitoRepository.findById(dto.idInvito())
                .orElseThrow(() -> new ValidationException(
                        "Invito non trovato: " + dto.idInvito()));

        if (!invito.getIdUtente().equals(dto.idUtente())) {
            throw new ValidationException(
                    "L'utente " + dto.idUtente() + " non è il destinatario di questo invito.");
        }

        if (invito.getStato() != StatoInvito.IN_ATTESA) {
            throw new ValidationException(
                    "L'invito ha già ricevuto una risposta: " + invito.getStato());
        }

        StatoInvito nuovoStato = dto.accettato() ? StatoInvito.ACCETTATO : StatoInvito.RIFIUTATO;
        invitoRepository.aggiornaStato(invito.getId(), nuovoStato);
        invito.setStato(nuovoStato);

        if (dto.accettato()) {
            teamRepository.addMembro(invito.getIdTeam(), invito.getIdUtente());
        }

        Team team = teamRepository.findById(invito.getIdTeam())
                .orElseThrow(() -> new ValidationException("Team non trovato: " + invito.getIdTeam()));

        Hackathon hackathon = hackathonRepository.findById(invito.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + invito.getIdHackathon()));

        String messaggio = dto.accettato()
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
     * Restituisce la lista degli hackathon in fase di iscrizione aperta.
     * Esposta per il layer di presentazione prima della creazione del team.
     */
    public List<HackathonResponseDTO> getHackathonDisponibili() {
        return hackathonRepository.findAllInIscrizione().stream()
                .map(h -> new HackathonResponseDTO(
                        h.getId(), h.getNome(),
                        h.getDataInizio(), h.getDataFine(),
                        h.getScadenzaIscrizioni(), h.getScadenzaSottomissioni(),
                        h.getPremio(), h.getLuogo(),
                        h.getDimensioneMaxTeam(), h.getRegolamento(),
                        h.getIdOrganizzatore(), h.getIdGiudice(),
                        h.getIdsMentori(), h.getStatoEnum()
                ))
                .toList();
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire iscrizione al team (UC2)
    // -----------------------------------------------------------------------

    /**
     * Recupera i dettagli di partecipazione corrente di un membro al proprio team.
     * Il Sistema carica il team, l'hackathon associato e la lista dei membri.
     *
     * @param idMembro id dell'utente membro del team
     * @return DTO con tutti i dettagli della partecipazione
     * @throws TeamNotFoundException se l'utente non è membro di nessun team
     */
    public DettagliPartecipazioneDTO ottieniDettagliTeamPerMembro(UUID idMembro) {
        Team team = teamRepository.findByMembro(idMembro)
                .orElseThrow(() -> new TeamNotFoundException(idMembro));

        Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + team.getIdHackathon()));

        List<MembroTeam> membri = teamRepository.findMembri(team.getId());
        boolean isLeader = team.getIdLeader().equals(idMembro);
        boolean puoAbbandonare = hackathon.getStatoEnum() == StatoHackathon.IN_ISCRIZIONE;

        return new DettagliPartecipazioneDTO(
                team.getId(),
                team.getNome(),
                team.getDescrizione(),
                team.getIdLeader(),
                hackathon.getId(),
                hackathon.getNome(),
                hackathon.getStatoEnum(),
                isLeader,
                puoAbbandonare,
                membri
        );
    }

    /**
     * Permette a un membro di abbandonare il proprio team.
     * <p>
     * Se il membro è il <strong>leader</strong>, viene eletto casualmente un nuovo leader
     * tra i restanti membri; il vecchio leader viene poi rimosso e tutti vengono notificati.
     * <br>
     * Se il membro è un <strong>membro ordinario</strong>, viene rimosso direttamente
     * e il leader corrente viene notificato.
     *
     * @param idTeam   id del team da abbandonare
     * @param idMembro id dell'utente che vuole abbandonare
     * @return DTO di conferma dell'abbandono
     * @throws TeamNotFoundException          se il team non esiste
     * @throws HackathonNotFoundException     se l'hackathon associato non esiste
     * @throws InvalidHackathonStateException se l'hackathon non è in fase IN_ISCRIZIONE
     * @throws ValidationException            se l'utente non è membro del team
     * @throws IllegalStateException          se il leader è l'unico membro (usare la disiscrizione)
     */
    public GestioneTeamResponseDTO abbandonaTeam(UUID idTeam, UUID idMembro) {
        Team team = teamRepository.findById(idTeam)
                .orElseThrow(() -> new TeamNotFoundException(idTeam));

        Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + team.getIdHackathon()));

        if (hackathon.getStatoEnum() != StatoHackathon.IN_ISCRIZIONE) {
            throw new InvalidHackathonStateException(
                    "Impossibile abbandonare il team: l'hackathon '" + hackathon.getNome() +
                            "' non è in fase di iscrizione. Stato attuale: " + hackathon.getStatoEnum());
        }

        List<MembroTeam> tuttiIMembri = teamRepository.findMembri(idTeam);
        boolean isMembro = tuttiIMembri.stream().anyMatch(m -> m.getId().equals(idMembro));
        if (!isMembro) {
            throw new ValidationException(
                    "L'utente " + idMembro + " non è membro del team " + idTeam);
        }

        boolean isLeader = team.getIdLeader().equals(idMembro);

        if (isLeader) {
            List<MembroTeam> membriRestanti = tuttiIMembri.stream()
                    .filter(m -> !m.getId().equals(idMembro))
                    .toList();

            if (membriRestanti.isEmpty()) {
                throw new IllegalStateException(
                        "Impossibile abbandonare: sei l'unico membro del team. " +
                                "Utilizza la funzione di disiscrizione del team dall'hackathon.");
            }

            UUID nuovoLeaderId = selezionaNuovoLeaderRandom(membriRestanti);
            teamRepository.updateLeader(idTeam, nuovoLeaderId);
            teamRepository.removeMembro(idTeam, idMembro);
            team.rimuoviMembro(idMembro);

            notificationsService.notificaCambioLeadership(nuovoLeaderId, membriRestanti, team.getNome());
        } else {
            teamRepository.removeMembro(idTeam, idMembro);
            team.rimuoviMembro(idMembro);

            notificationsService.notificaAbbandono(team.getIdLeader(), idMembro, team.getNome());
        }

        return new GestioneTeamResponseDTO(
                team.getId(),
                team.getNome(),
                idMembro,
                "Hai abbandonato il team '" + team.getNome() + "' con successo."
        );
    }

    // -----------------------------------------------------------------------
    // Metodi privati di validazione e supporto (GRASP: Information Expert)
    // -----------------------------------------------------------------------

    /**
     * Seleziona casualmente un nuovo leader dalla lista dei membri restanti.
     * Riutilizza il pattern ThreadLocalRandom già presente nel progetto.
     */
    private UUID selezionaNuovoLeaderRandom(List<MembroTeam> membriRestanti) {
        int indice = ThreadLocalRandom.current().nextInt(membriRestanti.size());
        return membriRestanti.get(indice).getId();
    }

    private void verificaStatoIscrizioni(Hackathon hackathon) {
        if (hackathon.getStatoEnum() != StatoHackathon.IN_ISCRIZIONE) {
            throw new InvalidHackathonStateException(
                    "L'hackathon '" + hackathon.getNome() + "' non è in fase di iscrizione. " +
                            "Stato attuale: " + hackathon.getStatoEnum());
        }
    }

    private void verificaLeaderDisponibile(UUID idLeader, UUID idHackathon) {
        if (!userRepository.isUserAvailable(idLeader, idHackathon)) {
            throw new UserAlreadyInTeamException(
                    "L'utente " + idLeader + " è già membro di un team per questo hackathon.");
        }
    }

    private void verificaDisponibilitaInvitati(List<UUID> idsInvitati, UUID idHackathon) {
        for (UUID idUtente : idsInvitati) {
            if (!userRepository.isUserAvailable(idUtente, idHackathon)) {
                throw new UserAlreadyInTeamException(
                        "L'utente " + idUtente + " è già membro di un team per questo hackathon " +
                                "e non può essere invitato.");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Visualizzare team di appartenenza (UC3)
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista sintetica di tutti i team di cui un utente è membro.
     *
     * @param idUtente l'id dell'utente autenticato
     * @return lista di {@link TeamSummaryDTO} con i dati sintetici dei team
     * @throws NoTeamsFoundException se l'utente non è membro di alcun team
     */
    public List<TeamSummaryDTO> getTeamsByUserId(UUID idUtente) {
        List<Team> teams = teamRepository.findAllByMembro(idUtente);

        if (teams.isEmpty()) {
            throw new NoTeamsFoundException(idUtente);
        }

        List<TeamSummaryDTO> risultato = new ArrayList<>();
        for (Team team : teams) {
            Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                    .orElseThrow(() -> new HackathonNotFoundException(
                            "Hackathon non trovato: " + team.getIdHackathon()));

            risultato.add(new TeamSummaryDTO(
                    team.getId(),
                    team.getNome(),
                    hackathon.getId(),
                    hackathon.getNome(),
                    hackathon.getStatoEnum(),
                    team.getIdLeader().equals(idUtente)
            ));
        }
        return risultato;
    }

    /**
     * Restituisce i dettagli completi di un team specifico, verificando che
     * l'utente richiedente ne sia effettivamente membro.
     *
     * @param idTeam   l'id del team di cui si richiedono i dettagli
     * @param idUtente l'id dell'utente autenticato
     * @return {@link TeamDetailsDTO} con i dati completi del team
     * @throws TeamNotFoundException       se il team non esiste
     * @throws HackathonNotFoundException  se l'hackathon associato non esiste
     * @throws UnauthorizedActionException se l'utente non è membro del team
     */
    public TeamDetailsDTO getTeamDetails(UUID idTeam, UUID idUtente) {
        Team team = teamRepository.findById(idTeam)
                .orElseThrow(() -> new TeamNotFoundException(idTeam));

        // Carica i membri per abilitare la verifica tramite l'entità (Information Expert)
        List<MembroTeam> membri = teamRepository.findMembri(idTeam);
        team.setMembri(membri);

        // Verifica che l'utente richiedente sia membro del team
        if (!team.hasMember(idUtente)) {
            throw new UnauthorizedActionException(
                    "L'utente " + idUtente + " non è membro del team " + idTeam +
                            " e non può visualizzarne i dettagli.");
        }

        Hackathon hackathon = hackathonRepository.findById(team.getIdHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + team.getIdHackathon()));

        return new TeamDetailsDTO(
                team.getId(),
                team.getNome(),
                team.getDescrizione(),
                team.getIdLeader(),
                hackathon.getId(),
                hackathon.getNome(),
                hackathon.getStatoEnum(),
                membri
        );
    }
}
