package hackhub.service;

import hackhub.dto.SottomissioneFormDTO;
import hackhub.dto.SottomissioneResponseDTO;
import hackhub.dto.SottomissioneSubmissionDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.SottomissioneAlreadyExistsException;
import hackhub.exception.TeamNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Inviare Sottomissione del Team'.
 * Gestisce il guard di stato tramite il pattern State e l'unicità della sottomissione.
 */
public class SottomissioneService {

    private final HackathonRepository    hackathonRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final TeamRepository         teamRepository;

    public SottomissioneService(HackathonRepository hackathonRepository,
                                SottomissioneRepository sottomissioneRepository,
                                TeamRepository teamRepository) {
        this.hackathonRepository     = hackathonRepository;
        this.sottomissioneRepository = sottomissioneRepository;
        this.teamRepository          = teamRepository;
    }

    /**
     * Recupera i dati di contesto per il form di invio sottomissione.
     * Verifica che l'hackathon sia nello stato corretto tramite il pattern State.
     */
    public SottomissioneFormDTO getFormData(UUID idHackathon, UUID idTeam) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
            .orElseThrow(() -> new HackathonNotFoundException(idHackathon));

        // Guard del pattern State: lancia eccezione se non IN_CORSO
        hackathon.verificaAccettaSottomissione();

        Team team = teamRepository.findById(idTeam)
            .orElseThrow(() -> new TeamNotFoundException(idTeam));

        return new SottomissioneFormDTO(
            hackathon.getId(), hackathon.getNome(),
            team.getId(), team.getNome()
        );
    }

    /**
     * Processa l'invio della sottomissione di un team.
     *
     * Passi:
     * 1. Carica l'hackathon dal repository.
     * 2. Delega al pattern State la verifica che le sottomissioni siano accettate.
     * 3. Controlla che il team non abbia già inviato una sottomissione per questo hackathon.
     * 4. Crea e persiste la sottomissione.
     */
    public SottomissioneResponseDTO inviaSottomissione(SottomissioneSubmissionDTO dto) {
        Hackathon hackathon = hackathonRepository.findById(dto.getIdHackathon())
            .orElseThrow(() -> new HackathonNotFoundException(dto.getIdHackathon()));

        // Guard del pattern State: lancia IllegalStateTransitionException se non IN_CORSO
        hackathon.verificaAccettaSottomissione();

        // Vincolo di unicità applicato a livello di business prima di tentare l'INSERT
        if (sottomissioneRepository.existsByHackathonAndTeam(dto.getIdHackathon(), dto.getIdTeam())) {
            throw new SottomissioneAlreadyExistsException(dto.getIdHackathon(), dto.getIdTeam());
        }

        Sottomissione sottomissione = new Sottomissione(
            dto.getLinkRepository(),
            dto.getLinkDemo(),
            dto.getDescrizione(),
            LocalDateTime.now(),
            dto.getIdTeam(),
            dto.getIdHackathon()
        );

        sottomissioneRepository.save(sottomissione);

        return new SottomissioneResponseDTO(sottomissione.getId(), sottomissione.getDataInvio());
    }
}
