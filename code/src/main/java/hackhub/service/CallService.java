package hackhub.service;

import hackhub.dto.CallCreateDTO;
import hackhub.dto.CallFormDTO;
import hackhub.dto.CallResponseDTO;
import hackhub.dto.DatiEventoEsterno;
import hackhub.exception.CalendarConnectionException;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.SupportRequestNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Call;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.repository.CallRepository;
import hackhub.repository.HackathonRepository;
import hackhub.repository.RichiestaSupportoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;

import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Pianificare call con un team'.
 * <p>
 * Questo caso d'uso viene avviato automaticamente dal sistema immediatamente
 * dopo che il mentore accetta una richiesta di supporto.
 * <p>
 * Flusso principale:
 * <ol>
 *   <li>Il sistema fornisce il form precompilato con i dati del team/hackathon.</li>
 *   <li>Il mentore inserisce data, ora e descrizione della call.</li>
 *   <li>Il sistema valida gli input.</li>
 *   <li>Il sistema delega al {@link CalendarService} la creazione dell'evento
 *       e l'invio dell'invito al leader del team.</li>
 *   <li>Il sistema salva i riferimenti della call nel database.</li>
 * </ol>
 * <p>
 * Eccezioni tecniche gestite dal controller:
 * <ul>
 *   <li>{@link CalendarConnectionException} — impossibile raggiungere il sistema Calendar</li>
 *   <li>{@link hackhub.exception.PersistenceException} — errore durante il salvataggio</li>
 * </ul>
 */
public class CallService {

    private final RichiestaSupportoRepository richiestaSupportoRepository;
    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;
    private final UserRepository userRepository;
    private final CallRepository callRepository;
    private final CalendarService calendarService;

    public CallService(RichiestaSupportoRepository richiestaSupportoRepository,
                       TeamRepository teamRepository,
                       HackathonRepository hackathonRepository,
                       UserRepository userRepository,
                       CallRepository callRepository,
                       CalendarService calendarService) {
        this.richiestaSupportoRepository = richiestaSupportoRepository;
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
        this.userRepository = userRepository;
        this.callRepository = callRepository;
        this.calendarService = calendarService;
    }

    // -------------------------------------------------------------------------
    // Preparazione form di pianificazione
    // -------------------------------------------------------------------------

    /**
     * Fornisce i dati di contesto per il form di pianificazione della call.
     * Verifica che la richiesta esista e che il mentore ne sia il destinatario.
     *
     * @param idRichiesta id della richiesta di supporto accettata
     * @param idMentore   id del mentore autenticato
     * @return DTO con il contesto del form (team, hackathon)
     * @throws SupportRequestNotFoundException se la richiesta non esiste
     * @throws UnauthorizedActionException     se il mentore non è il destinatario
     */
    public CallFormDTO getFormData(UUID idRichiesta, UUID idMentore) {
        RichiestaSupporto richiesta = richiestaSupportoRepository.findById(idRichiesta)
                .orElseThrow(() -> new SupportRequestNotFoundException(idRichiesta));

        if (!richiesta.getIdMentore().equals(idMentore)) {
            throw new UnauthorizedActionException(
                    "Il mentore " + idMentore + " non è autorizzato ad accedere alla richiesta " + idRichiesta);
        }

        Team team = teamRepository.findById(richiesta.getIdTeam())
                .orElseThrow(() -> new TeamNotFoundException(richiesta.getIdTeam()));

        String nomeHackathon = hackathonRepository.findById(richiesta.getIdHackathon())
                .map(h -> h.getNome())
                .orElseThrow(() -> new HackathonNotFoundException(richiesta.getIdHackathon()));

        return new CallFormDTO(idRichiesta, idMentore, team.getNome(), nomeHackathon);
    }

    // -------------------------------------------------------------------------
    // Pianificazione call (flusso principale)
    // -------------------------------------------------------------------------

    /**
     * Pianifica una call tra il mentore e il team, delegando la creazione
     * dell'evento al sistema Calendar esterno e salvando i riferimenti nel DB.
     * <p>
     * Passi:
     * <ol>
     *   <li>Valida il DTO di input.</li>
     *   <li>Carica la richiesta di supporto e il profilo del leader del team.</li>
     *   <li>Delega al {@link CalendarService} la creazione dell'evento e l'invio dell'invito.</li>
     *   <li>Salva la call (con link e dati evento) nel database.</li>
     * </ol>
     *
     * @param dto dati inseriti dal mentore nel form (data, ora, descrizione)
     * @return DTO di conferma con link della call e dettagli dell'evento
     * @throws ValidationException                    se i dati del form non sono validi
     * @throws SupportRequestNotFoundException        se la richiesta non esiste
     * @throws CalendarConnectionException            se il sistema Calendar non è raggiungibile
     * @throws hackhub.exception.PersistenceException se il salvataggio nel DB fallisce
     */
    public CallResponseDTO pianificaCall(CallCreateDTO dto) {
        validaCallCreate(dto);

        // Caricamento richiesta di supporto (Information Expert: conosce idTeam e idMentore)
        RichiestaSupporto richiesta = richiestaSupportoRepository.findById(dto.idRichiesta())
                .orElseThrow(() -> new SupportRequestNotFoundException(dto.idRichiesta()));

        // Recupero email del leader per l'invio dell'invito Calendar
        Team team = teamRepository.findById(richiesta.getIdTeam())
                .orElseThrow(() -> new TeamNotFoundException(richiesta.getIdTeam()));

        String emailLeader = userRepository.findById(team.getIdLeader())
                .map(u -> u.getEmail())
                .orElse(null);

        // Delega al sistema Calendar esterno la creazione dell'evento e l'invio dell'invito
        // Propaga CalendarConnectionException se non raggiungibile
        DatiEventoEsterno datiEvento = calendarService.creaEventoCalendar(dto, emailLeader);

        // Creazione dell'entità Call e salvataggio nel DB
        // Propaga PersistenceException se il salvataggio fallisce
        Call call = new Call(
                richiesta.getId(),
                dto.dataCall(),
                dto.oraCall(),
                datiEvento.linkCall(),
                dto.descrizione()
        );
        callRepository.save(call);

        return new CallResponseDTO(
                call.getId(),
                richiesta.getId(),
                dto.dataCall(),
                dto.oraCall(),
                datiEvento.linkCall(),
                dto.descrizione(),
                "Call pianificata con successo per il team '" + team.getNome()
                        + "'. Invito inviato al leader tramite il sistema Calendar."
        );
    }

    // -------------------------------------------------------------------------
    // Validazione input (GRASP Information Expert)
    // -------------------------------------------------------------------------

    private void validaCallCreate(CallCreateDTO dto) {
        if (dto.idRichiesta() == null) {
            throw new ValidationException("L'id della richiesta di supporto è obbligatorio.");
        }
        if (dto.idMentore() == null) {
            throw new ValidationException("L'id del mentore è obbligatorio.");
        }
        if (dto.dataCall() == null) {
            throw new ValidationException("La data della call è obbligatoria.");
        }
        if (dto.oraCall() == null) {
            throw new ValidationException("L'ora della call è obbligatoria.");
        }
        if (dto.descrizione() == null || dto.descrizione().isBlank()) {
            throw new ValidationException("La descrizione della call è obbligatoria.");
        }
    }
}
