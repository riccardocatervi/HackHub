package hackhub.service;

import hackhub.builder.EquipeBuilder;
import hackhub.builder.EquipeCreationResult;
import hackhub.dto.*;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.UserAlreadyInTeamException;
import hackhub.exception.ValidationException;
import hackhub.model.StatoInvito;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoHackathon;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;

import java.util.List;
import java.util.UUID;

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
    public InvitoResponseDTO rispondiInvito(RispostaInvitoDTO dto) {
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

        return new InvitoResponseDTO(
                invito.getId(),
                team.getId(),
                team.getNome(),
                hackathon.getId(),
                hackathon.getNome(),
                nuovoStato
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
                        h.getIdMentori(), h.getStatoEnum()
                ))
                .toList();
    }

    // -----------------------------------------------------------------------
    // Metodi privati di validazione (GRASP: Information Expert)
    // -----------------------------------------------------------------------

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

    private void verificaDisponibilitaInvitati(List<UUID> idInvitati, UUID idHackathon) {
        for (UUID idUtente : idInvitati) {
            if (!userRepository.isUserAvailable(idUtente, idHackathon)) {
                throw new UserAlreadyInTeamException(
                        "L'utente " + idUtente + " è già membro di un team per questo hackathon " +
                                "e non può essere invitato.");
            }
        }
    }
}
