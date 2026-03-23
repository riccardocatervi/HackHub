package hackhub.controller;

import hackhub.dto.SupportoFormDTO;
import hackhub.dto.SupportoRequestDTO;
import hackhub.dto.SupportoResponseDTO;
import hackhub.service.SupportoService;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Controller per il caso d'uso 'Inviare Richiesta di Supporto a un Mentore'.
 * Agisce come coordinatore GRASP: riceve le richieste, esegue la validazione
 * formale del payload e delega tutta la logica di business al servizio.
 */
@Component
public class SupportoController {

    private final SupportoService supportoService;

    public SupportoController(SupportoService supportoService) {
        this.supportoService = supportoService;
    }

    /**
     * Restituisce i dati di contesto per il form di richiesta di supporto.
     * Verifica che l'hackathon sia IN_CORSO prima di mostrare il form.
     *
     * @param idHackathon id dell'hackathon per cui si richiede supporto
     * @param idTeam      id del team del leader richiedente
     * @return DTO con nome hackathon e team per il form
     */
    public SupportoFormDTO richiediFormSupporto(UUID idHackathon, UUID idTeam) {
        return supportoService.getFormData(idHackathon, idTeam);
    }

    /**
     * Elabora l'invio della richiesta di supporto e seleziona un mentore.
     *
     * @param dto DTO con hackathon, team e motivo della richiesta
     * @return DTO di conferma con il mentore assegnato e il timestamp
     */
    public SupportoResponseDTO inviaSupporto(SupportoRequestDTO dto) {
        return supportoService.inviaSupporto(dto);
    }
}
