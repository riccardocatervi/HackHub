package hackhub.service;

import hackhub.dto.SupportoFormDTO;
import hackhub.dto.SupportoRequestDTO;
import hackhub.dto.SupportoResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.NoMentorsAvailableException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.RichiestaSupportoRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.RichiestaSupportoObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Servizio per il caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 * <p>
 * Coordinamento del workflow:
 * <ol>
 *   <li>Carica l'hackathon e delega al pattern State la verifica dello stato (IN_CORSO).</li>
 *   <li>Verifica che l'hackathon abbia mentori associati.</li>
 *   <li>Seleziona casualmente un mentore dalla lista dei mentori dell'hackathon.</li>
 *   <li>Crea e persiste la richiesta di supporto.</li>
 *   <li>Notifica il mentore tramite il pattern GoF Observer.</li>
 * </ol>
 */
public class SupportoService {

    private final HackathonRepository hackathonRepository;
    private final TeamRepository teamRepository;
    private final MentoreRepository mentoreRepository;
    private final RichiestaSupportoRepository richiestaSupportoRepository;

    private final List<RichiestaSupportoObserver> observers = new ArrayList<>();

    public SupportoService(HackathonRepository hackathonRepository,
                           TeamRepository teamRepository,
                           MentoreRepository mentoreRepository,
                           RichiestaSupportoRepository richiestaSupportoRepository) {
        this.hackathonRepository = hackathonRepository;
        this.teamRepository = teamRepository;
        this.mentoreRepository = mentoreRepository;
        this.richiestaSupportoRepository = richiestaSupportoRepository;
    }

    // -------------------------------------------------------------------------
    // Registrazione osservatori (pattern GoF Observer)
    // -------------------------------------------------------------------------

    /**
     * Registra un osservatore per gli eventi di nuova richiesta di supporto.
     */
    public void addObserver(RichiestaSupportoObserver observer) {
        observers.add(observer);
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: Inviare Richiesta di Supporto a un Mentore
    // -------------------------------------------------------------------------

    /**
     * Recupera i dati di contesto per il form di richiesta di supporto.
     * Verifica che l'hackathon sia nello stato IN_CORSO.
     *
     * @param idHackathon id dell'hackathon
     * @param idTeam      id del team che vuole inviare la richiesta
     * @return DTO con i dati di contesto per il form
     */
    public SupportoFormDTO getFormData(UUID idHackathon, UUID idTeam) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(idHackathon));

        // Guard del pattern State: lancia InvalidHackathonStateException se non IN_CORSO
        hackathon.verificaAccettaRichiestaSupporto();

        Team team = teamRepository.findById(idTeam)
                .orElseThrow(() -> new TeamNotFoundException(idTeam));

        return new SupportoFormDTO(
                hackathon.getId(),
                hackathon.getNome(),
                team.getId(),
                team.getNome()
        );
    }

    /**
     * Processa l'invio della richiesta di supporto da parte del leader del team.
     * <p>
     * Passi:
     * <ol>
     *   <li>Valida il DTO di input.</li>
     *   <li>Carica l'hackathon e verifica lo stato IN_CORSO tramite il pattern State.</li>
     *   <li>Verifica che esistano mentori associati all'hackathon.</li>
     *   <li>Seleziona un mentore casuale e carica il suo profilo.</li>
     *   <li>Crea e persiste la richiesta di supporto.</li>
     *   <li>Notifica il mentore tramite gli observer registrati.</li>
     * </ol>
     *
     * @param dto payload con hackathon, team e motivo della richiesta
     * @return DTO di conferma con il mentore assegnato
     */
    public SupportoResponseDTO inviaSupporto(SupportoRequestDTO dto) {
        validaRequest(dto);

        Hackathon hackathon = hackathonRepository.findById(dto.idHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(dto.idHackathon()));

        // Guard del pattern State: lancia InvalidHackathonStateException se non IN_CORSO
        hackathon.verificaAccettaRichiestaSupporto();

        List<UUID> idsMentori = hackathon.getIdsMentori();
        if (idsMentori == null || idsMentori.isEmpty()) {
            throw new NoMentorsAvailableException(dto.idHackathon());
        }

        // Selezione casuale del mentore (GRASP Information Expert: logica localizzata nel servizio)
        UUID idMentoreSelezionato = selezionaMentoreCasuale(idsMentori);

        Mentore mentore = mentoreRepository.findById(idMentoreSelezionato)
                .orElseThrow(() -> new NoMentorsAvailableException(dto.idHackathon()));

        RichiestaSupporto richiesta = new RichiestaSupporto(
                dto.idTeam(),
                dto.idHackathon(),
                mentore.getId(),
                dto.motivo()
        );

        richiestaSupportoRepository.save(richiesta);

        // Notifica gli observer registrati (pattern GoF Observer)
        String emailMentore = mentore.getEmail();
        for (RichiestaSupportoObserver observer : observers) {
            observer.onNuovaRichiestaSupporto(richiesta, emailMentore);
        }

        String nomeMentore = mentore.getNome() + " " + mentore.getCognome();
        return new SupportoResponseDTO(
                richiesta.getId(),
                mentore.getId(),
                nomeMentore,
                richiesta.getDataInvio(),
                "Richiesta di supporto inoltrata con successo al mentore " + nomeMentore + "."
        );
    }

    // -------------------------------------------------------------------------
    // Logica interna
    // -------------------------------------------------------------------------

    /**
     * Seleziona casualmente un id di mentore dalla lista fornita.
     *
     * @param idsMentori lista non vuota di id mentori associati all'hackathon
     * @return l'id del mentore selezionato
     */
    private UUID selezionaMentoreCasuale(List<UUID> idsMentori) {
        int indice = ThreadLocalRandom.current().nextInt(idsMentori.size());
        return idsMentori.get(indice);
    }

    // -------------------------------------------------------------------------
    // Validazione input (GRASP: Information Expert)
    // -------------------------------------------------------------------------

    private void validaRequest(SupportoRequestDTO dto) {
        if (dto.idHackathon() == null) {
            throw new ValidationException("L'id dell'hackathon è obbligatorio.");
        }
        if (dto.idTeam() == null) {
            throw new ValidationException("L'id del team è obbligatorio.");
        }
        if (dto.motivo() == null || dto.motivo().isBlank()) {
            throw new ValidationException("Il motivo della richiesta di supporto è obbligatorio.");
        }
        if (dto.motivo().length() < 10) {
            throw new ValidationException("Il motivo deve contenere almeno 10 caratteri.");
        }
    }
}
