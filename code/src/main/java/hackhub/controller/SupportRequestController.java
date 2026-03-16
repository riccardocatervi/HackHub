package hackhub.controller;

import hackhub.dto.CallFormDTO;
import hackhub.dto.EsitoGestioneRichiestaDTO;
import hackhub.dto.RichiestaSupportoDettagliDTO;
import hackhub.service.SupportRequestService;

import java.util.List;
import java.util.UUID;

/**
 * Controller GRASP per il caso d'uso 'Prendere in carico una richiesta di supporto'.
 * <p>
 * Agisce come coordinatore (Pattern Controller GRASP): riceve le richieste dell'UI,
 * delega la logica di business a {@link SupportRequestService} e, nel caso di accettazione,
 * avvia automaticamente il caso d'uso correlato 'Pianificare call con un team'
 * tramite {@link CallController}.
 * <p>
 * La dipendenza su {@link CallController} è intenzionale: rappresenta la transizione
 * automatica tra i due casi d'uso quando il mentore accetta la richiesta, come da specifica.
 */
public class SupportRequestController {

    private final SupportRequestService supportRequestService;
    private final CallController callController;

    public SupportRequestController(SupportRequestService supportRequestService,
                                    CallController callController) {
        this.supportRequestService = supportRequestService;
        this.callController = callController;
    }

    /**
     * Restituisce la lista delle richieste di supporto pendenti per il mentore.
     * Punto di ingresso del caso d'uso: il mentore seleziona la funzionalità dedicata.
     *
     * @param idMentore id del mentore autenticato
     * @return lista delle richieste pendenti con i dati di contesto
     */
    public List<RichiestaSupportoDettagliDTO> getRichiestePendenti(UUID idMentore) {
        return supportRequestService.getRichiestePendentiByMentore(idMentore);
    }

    /**
     * Restituisce i dettagli di una singola richiesta di supporto.
     * Il mentore analizza i dettagli prima di decidere come procedere.
     *
     * @param idRichiesta id della richiesta selezionata
     * @param idMentore   id del mentore autenticato
     * @return DTO con tutti i dettagli della richiesta (team, problema, urgenza)
     */
    public RichiestaSupportoDettagliDTO getDettagliRichiesta(UUID idRichiesta, UUID idMentore) {
        return supportRequestService.getDettagliRichiesta(idRichiesta, idMentore);
    }

    /**
     * Processa l'accettazione di una richiesta di supporto e avvia automaticamente
     * il caso d'uso 'Pianificare call con un team'.
     * <p>
     * Flusso:
     * <ol>
     *   <li>Il service aggiorna lo stato a PRESA_IN_CARICO e notifica il leader.</li>
     *   <li>Il controller avvia il UC2 richiedendo il form di pianificazione call.</li>
     * </ol>
     *
     * @param idRichiesta id della richiesta da accettare
     * @param idMentore   id del mentore autenticato
     * @return {@link CallFormDTO} precompilato per avviare la pianificazione della call
     */
    public CallFormDTO accettaRichiesta(UUID idRichiesta, UUID idMentore) {
        // UC1: accetta la richiesta, notifica il leader
        supportRequestService.accettaRichiesta(idRichiesta, idMentore);
        // Transizione automatica verso UC2: fornisce il form di pianificazione call
        return callController.richiediFormPianificazioneCall(idRichiesta, idMentore);
    }

    /**
     * Processa il rifiuto di una richiesta di supporto da parte del mentore.
     * Il mentore fornisce obbligatoriamente una motivazione.
     *
     * @param idRichiesta id della richiesta da rifiutare
     * @param idMentore   id del mentore autenticato
     * @param motivazione motivazione del rifiuto inserita dal mentore
     * @return DTO di conferma con la motivazione salvata
     */
    public EsitoGestioneRichiestaDTO respingiRichiesta(UUID idRichiesta, UUID idMentore, String motivazione) {
        return supportRequestService.respingiRichiesta(idRichiesta, idMentore, motivazione);
    }
}
