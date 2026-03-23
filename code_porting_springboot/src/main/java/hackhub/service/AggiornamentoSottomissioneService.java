package hackhub.service;

import hackhub.dto.SottomissioneAggiornamentoFormDTO;
import hackhub.dto.SottomissioneUpdateDTO;
import hackhub.dto.SottomissioneUpdateResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.SottomissioneNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Aggiornare Sottomissione del Team'.
 * <p>
 * Coordinamento del processo di aggiornamento:
 * <ol>
 *   <li>Carica l'hackathon e delega al pattern State la verifica dello stato.</li>
 *   <li>Recupera la sottomissione esistente tramite la coppia (hackathon, team).</li>
 *   <li>Delega all'entità Sottomissione (GRASP Information Expert) il proprio aggiornamento.</li>
 *   <li>Persiste le modifiche tramite il repository.</li>
 * </ol>
 */
@Service
public class AggiornamentoSottomissioneService {

    private final HackathonRepository hackathonRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final TeamRepository teamRepository;

    public AggiornamentoSottomissioneService(HackathonRepository hackathonRepository,
                                             SottomissioneRepository sottomissioneRepository,
                                             TeamRepository teamRepository) {
        this.hackathonRepository = hackathonRepository;
        this.sottomissioneRepository = sottomissioneRepository;
        this.teamRepository = teamRepository;
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: Aggiornare Sottomissione del Team
    // -------------------------------------------------------------------------

    /**
     * Restituisce i dati pre-popolati per il form di aggiornamento.
     * Verifica che l'hackathon si trovi nello stato che consente le sottomissioni (IN_CORSO).
     *
     * @param idHackathon id dell'hackathon
     * @param idTeam      id del team leader
     * @return DTO con i dati correnti della sottomissione pronto per la visualizzazione
     */
    public SottomissioneAggiornamentoFormDTO getFormDataAggiornamento(UUID idHackathon, UUID idTeam) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(idHackathon));

        // Guard del pattern State: lancia IllegalStateTransitionException se non IN_CORSO
        hackathon.verificaAccettaSottomissione();

        Team team = teamRepository.findById(idTeam)
                .orElseThrow(() -> new TeamNotFoundException(idTeam));

        Sottomissione sottomissione = sottomissioneRepository
                .findByHackathonAndTeam(idHackathon, idTeam)
                .orElseThrow(() -> new SottomissioneNotFoundException(
                        "Nessuna sottomissione trovata per il team " + idTeam +
                                " nell'hackathon " + idHackathon + "."));

        return new SottomissioneAggiornamentoFormDTO(
                hackathon.getId(),
                hackathon.getNome(),
                team.getId(),
                team.getNome(),
                sottomissione.getId(),
                sottomissione.getLinkRepo(),
                sottomissione.getLinkDemo(),
                sottomissione.getDescrizione()
        );
    }

    /**
     * Processa l'aggiornamento della sottomissione del team.
     * <p>
     * Passi:
     * <ol>
     *   <li>Valida il DTO di input.</li>
     *   <li>Carica l'hackathon e verifica lo stato tramite il pattern State.</li>
     *   <li>Recupera la sottomissione esistente.</li>
     *   <li>Delega all'entità l'aggiornamento dei propri dati (Information Expert).</li>
     *   <li>Persiste le modifiche.</li>
     * </ol>
     *
     * @param dto dati aggiornati forniti dal leader del team
     * @return DTO con i dati aggiornati della sottomissione
     */
    public SottomissioneUpdateResponseDTO aggiornaSottomissione(SottomissioneUpdateDTO dto) {
        validaUpdateDTO(dto);

        Hackathon hackathon = hackathonRepository.findById(dto.idHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(dto.idHackathon()));

        // Guard del pattern State: lancia IllegalStateTransitionException se non IN_CORSO
        hackathon.verificaAccettaSottomissione();

        Sottomissione sottomissione = sottomissioneRepository
                .findByHackathonAndTeam(dto.idHackathon(), dto.idTeam())
                .orElseThrow(() -> new SottomissioneNotFoundException(
                        "Nessuna sottomissione trovata per il team " + dto.idTeam() +
                                " nell'hackathon " + dto.idHackathon() + "."));

        // GRASP Information Expert: l'entità gestisce il proprio aggiornamento
        LocalDateTime timestampAggiornamento = LocalDateTime.now();
        sottomissione.updateDetails(
                dto.linkRepo(),
                dto.linkDemo(),
                dto.descrizione(),
                timestampAggiornamento
        );

        sottomissioneRepository.update(sottomissione);

        return new SottomissioneUpdateResponseDTO(
                sottomissione.getId(),
                sottomissione.getIdHackathon(),
                sottomissione.getIdTeam(),
                sottomissione.getLinkRepo(),
                sottomissione.getLinkDemo(),
                sottomissione.getDescrizione(),
                sottomissione.getDataInvio()
        );
    }

    // -------------------------------------------------------------------------
    // Validazione input (GRASP: Information Expert)
    // -------------------------------------------------------------------------

    private void validaUpdateDTO(SottomissioneUpdateDTO dto) {
        if (dto.idHackathon() == null) {
            throw new ValidationException("L'id dell'hackathon è obbligatorio.");
        }
        if (dto.idTeam() == null) {
            throw new ValidationException("L'id del team è obbligatorio.");
        }
        if (dto.linkRepo() == null || dto.linkRepo().isBlank()) {
            throw new ValidationException("Il link al repository è obbligatorio.");
        }
        if (dto.descrizione() == null || dto.descrizione().isBlank()) {
            throw new ValidationException("La descrizione del progetto è obbligatoria.");
        }
    }
}
