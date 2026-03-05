package hackhub.controller;

import hackhub.dto.HackathonFormDataDTO;
import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSubmissionDTO;
import hackhub.service.HackathonService;

import java.util.UUID;

/**
 * Controller per il caso d'uso 'Organizzare Hackathon'.
 * Agisce come coordinatore GRASP: riceve la richiesta dalla UI,
 * la delega al service e restituisce la risposta.
 */
public class HackathonController {

    private final HackathonService hackathonService;

    public HackathonController(HackathonService hackathonService) {
        this.hackathonService = hackathonService;
    }

    /**
     * Restituisce i dati per popolare il form di creazione hackathon.
     *
     * @param idOrganizzatore l'id dell'organizzatore che vuole creare l'hackathon
     */
    public HackathonFormDataDTO richiediFormCreazione(UUID idOrganizzatore) {
        return hackathonService.getFormData(idOrganizzatore);
    }

    /**
     * Crea un nuovo hackathon con i dati del form compilato.
     * L'hackathon viene creato in stato IN_ISCRIZIONE.
     *
     * @param requestDTO i dati compilati dall'organizzatore
     * @return i dettagli dell'hackathon appena creato
     */
    public HackathonResponseDTO creaHackathon(HackathonSubmissionDTO requestDTO) {
        return hackathonService.creaHackathon(requestDTO);
    }
}
