package hackhub.controller;

import hackhub.dto.SottomissioneFormDTO;
import hackhub.dto.SottomissioneResponseDTO;
import hackhub.dto.SottomissioneSubmissionDTO;
import hackhub.service.SottomissioneService;

import java.util.UUID;

/**
 * Controller per il caso d'uso 'Inviare Sottomissione del Team'.
 * Agisce come coordinatore GRASP.
 */
public class SottomissioneController {

    private final SottomissioneService sottomissioneService;

    public SottomissioneController(SottomissioneService sottomissioneService) {
        this.sottomissioneService = sottomissioneService;
    }

    /**
     * Restituisce i dati di contesto per il form di invio sottomissione.
     *
     * @param idHackathon l'hackathon per cui si vuole inviare la sottomissione
     * @param idTeam      il team che intende inviare la sottomissione
     */
    public SottomissioneFormDTO richiediFormInvioSottomissione(UUID idHackathon, UUID idTeam) {
        return sottomissioneService.getFormData(idHackathon, idTeam);
    }

    /**
     * Elabora l'invio di una sottomissione da parte di un team.
     *
     * @param requestDTO i dati compilati dal team (link repo, link demo, descrizione)
     * @return l'id e la data di invio della sottomissione registrata
     */
    public SottomissioneResponseDTO inviaSottomissione(SottomissioneSubmissionDTO requestDTO) {
        return sottomissioneService.inviaSottomissione(requestDTO);
    }
}
