package hackhub.controller;

import hackhub.dto.HackathonFormDataDTO;
import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSubmissionDTO;
import hackhub.dto.HackathonSummaryDTO;
import hackhub.dto.HackathonUpdatedDTO;
import hackhub.dto.MentoreDTO;
import hackhub.dto.ModificaMentoriRequestDTO;
import hackhub.service.HackathonService;
import hackhub.service.VisualizzaHackathonIscrittoService;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Controller per i casi d'uso relativi agli hackathon.
 * Agisce come coordinatore GRASP: riceve la richiesta dalla UI,
 * la delega al service appropriato e restituisce la risposta.
 */
@Component
public class HackathonController {

    private final HackathonService hackathonService;
    private final VisualizzaHackathonIscrittoService visualizzaHackathonIscrittoService;

    public HackathonController(HackathonService hackathonService,
                               VisualizzaHackathonIscrittoService visualizzaHackathonIscrittoService) {
        this.hackathonService = hackathonService;
        this.visualizzaHackathonIscrittoService = visualizzaHackathonIscrittoService;
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

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire mentori di un hackathon
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista degli hackathon creati dall'organizzatore.
     * Punto di ingresso del caso d'uso 'Gestire mentori di un hackathon'.
     *
     * @param idOrganizzatore l'id dell'organizzatore autenticato
     * @return lista sintetica degli hackathon di competenza
     */
    public List<HackathonSummaryDTO> richiediListaHackathon(UUID idOrganizzatore) {
        if (idOrganizzatore == null) {
            throw new IllegalArgumentException("idOrganizzatore non può essere null.");
        }
        return hackathonService.getHackathonByOrganizzatore(idOrganizzatore);
    }

    /**
     * Restituisce i dettagli completi di un hackathon selezionato, inclusa la lista mentori.
     *
     * @param idHackathon l'id dell'hackathon da visualizzare
     * @return DTO con i dati completi dell'hackathon
     */
    public HackathonResponseDTO selezionaHackathon(UUID idHackathon) {
        if (idHackathon == null) {
            throw new IllegalArgumentException("idHackathon non può essere null.");
        }
        return hackathonService.getHackathonDetails(idHackathon);
    }

    /**
     * Restituisce i mentori disponibili per l'aggiunta e la lista di quelli già assegnati.
     * Abilita la schermata di modifica della lista mentori.
     *
     * @param idHackathon l'id dell'hackathon da gestire
     * @return mappa con mentori correnti e mentori aggiungibili (restituiti separatamente)
     */
    public List<MentoreDTO> richiediModificaMentori(UUID idHackathon) {
        if (idHackathon == null) {
            throw new IllegalArgumentException("idHackathon non può essere null.");
        }
        return hackathonService.getMentoriDisponibili(idHackathon);
    }

    /**
     * Applica le modifiche alla lista mentori dell'hackathon.
     * Valida le modifiche e delega l'elaborazione a {@link HackathonService}.
     *
     * @param dto il DTO con le liste di mentori da aggiungere e rimuovere
     * @return DTO di conferma con la lista mentori aggiornata
     */
    public HackathonUpdatedDTO confermaModificheLista(ModificaMentoriRequestDTO dto) {
        if (dto == null || dto.idHackathon() == null) {
            throw new IllegalArgumentException("Il DTO e l'id hackathon non possono essere null.");
        }
        return hackathonService.updateMentori(dto);
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Visualizzare informazioni pubbliche sugli hackathon
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista di tutti gli hackathon pubblicamente disponibili.
     * Punto di ingresso del caso d'uso 'Visualizzare informazioni pubbliche sugli hackathon'.
     * Accessibile a qualsiasi visitatore o utente, senza autenticazione.
     *
     * @return lista sintetica degli hackathon disponibili
     */
    public List<HackathonSummaryDTO> richiediListaHackathon() {
        return hackathonService.getHackathonDisponibili();
    }

    /**
     * Restituisce i dettagli pubblici di un hackathon disponibile selezionato dall'utente.
     * Verifica che l'hackathon esista e non sia in stato CONCLUSO.
     *
     * @param idHackathon l'id dell'hackathon scelto dall'utente
     * @return DTO con i dati completi dell'hackathon
     */
    public HackathonResponseDTO richiediDettagliHackathon(UUID idHackathon) {
        if (idHackathon == null) {
            throw new IllegalArgumentException("idHackathon non può essere null.");
        }
        return hackathonService.getDettagliHackathon(idHackathon);
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Visualizzare hackathon a cui l'utente è iscritto
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista sintetica di tutti gli hackathon a cui l'utente è iscritto
     * tramite i propri team.
     * Punto di ingresso del caso d'uso 'Visualizzare hackathon a cui l'utente è iscritto'.
     *
     * @param idUtente l'id dell'utente autenticato
     * @return lista sintetica degli hackathon a cui l'utente partecipa
     */
    public List<HackathonSummaryDTO> getTeamHackathons(UUID idUtente) {
        if (idUtente == null) {
            throw new IllegalArgumentException("idUtente non può essere null.");
        }
        return visualizzaHackathonIscrittoService.getHackathonsByUserId(idUtente);
    }

    /**
     * Restituisce i dettagli completi di un hackathon a cui l'utente è iscritto.
     * Verifica che il team specificato esista e che l'utente ne sia membro.
     * A differenza della vista pubblica, mostra i dati anche per hackathon in stato CONCLUSO.
     *
     * @param idHackathon l'id dell'hackathon selezionato dalla lista
     * @param idTeam      l'id del team tramite cui l'utente è iscritto
     * @param idUtente    l'id dell'utente autenticato
     * @return DTO con tutti i dati dell'hackathon selezionato
     */
    public HackathonResponseDTO getHackathonDetails(UUID idHackathon, UUID idTeam, UUID idUtente) {
        if (idHackathon == null || idTeam == null || idUtente == null) {
            throw new IllegalArgumentException("idHackathon, idTeam e idUtente non possono essere null.");
        }
        return visualizzaHackathonIscrittoService.getHackathonDetails(idHackathon, idTeam, idUtente);
    }
}
