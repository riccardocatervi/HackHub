package hackhub.controller;

import hackhub.dto.SottomissioneAggiornamentoFormDTO;
import hackhub.dto.SottomissioneUpdateDTO;
import hackhub.dto.SottomissioneUpdateResponseDTO;
import hackhub.service.AggiornamentoSottomissioneService;

import java.util.UUID;

/**
 * Controller per il caso d'uso 'Aggiornare Sottomissione del Team'.
 * Agisce come coordinatore GRASP: riceve le richieste, valida formalmente il payload
 * e delega tutta la logica di business al servizio.
 */
public class AggiornamentoSottomissioneController {

    private final AggiornamentoSottomissioneService aggiornamentoService;

    public AggiornamentoSottomissioneController(
            AggiornamentoSottomissioneService aggiornamentoService) {
        this.aggiornamentoService = aggiornamentoService;
    }

    /**
     * Restituisce i dati correnti della sottomissione per pre-popolare il form di modifica.
     *
     * @param idHackathon id dell'hackathon
     * @param idTeam      id del team il cui leader desidera aggiornare la sottomissione
     * @return DTO con i dati attuali della sottomissione
     */
    public SottomissioneAggiornamentoFormDTO richiediFormAggiornamento(UUID idHackathon,
                                                                       UUID idTeam) {
        return aggiornamentoService.getFormDataAggiornamento(idHackathon, idTeam);
    }

    /**
     * Elabora la conferma dell'aggiornamento della sottomissione da parte del leader.
     *
     * @param dto DTO con i nuovi valori di linkRepo, linkDemo e descrizione
     * @return DTO con i dati aggiornati e il timestamp dell'aggiornamento
     */
    public SottomissioneUpdateResponseDTO aggiornaSottomissione(SottomissioneUpdateDTO dto) {
        return aggiornamentoService.aggiornaSottomissione(dto);
    }
}
