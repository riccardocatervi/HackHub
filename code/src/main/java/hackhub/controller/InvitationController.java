package hackhub.controller;

import hackhub.dto.InvitationResponseDTO;
import hackhub.dto.InvitoListaItemDTO;
import hackhub.service.InvitationService;

import java.util.List;
import java.util.UUID;

/**
 * Coordinatore GRASP per il caso d'uso "Accettare invito a unirsi al team".
 * <p>
 * Riceve le richieste dal layer di presentazione, esegue la validazione formale
 * degli argomenti e delega tutta la logica di business a {@link InvitationService}.
 */
public class InvitationController {

    private final InvitationService invitationService;

    public InvitationController(InvitationService invitationService) {
        this.invitationService = invitationService;
    }

    /**
     * Restituisce la lista degli inviti in stato IN_ATTESA per l'utente autenticato.
     * Punto di ingresso del flusso: l'utente accede alla sezione "Inviti ricevuti".
     *
     * @param idUtente id dell'utente autenticato
     * @return lista di inviti pendenti con dettagli di team e hackathon
     */
    public List<InvitoListaItemDTO> getPendingInvitations(UUID idUtente) {
        if (idUtente == null) {
            throw new IllegalArgumentException("idUtente non può essere null.");
        }
        return invitationService.getPendingInvitations(idUtente);
    }

    /**
     * Processa la risposta dell'utente a un invito (accettazione o rifiuto).
     * Delega la validazione di business e la persistenza a {@link InvitationService}.
     *
     * @param idInvito  id dell'invito selezionato dall'utente
     * @param idUtente  id dell'utente autenticato (il destinatario dell'invito)
     * @param accettato true se l'utente vuole accettare, false per rifiutare
     * @return DTO con il nuovo stato dell'invito e messaggio di esito
     */
    public InvitationResponseDTO respondToInvitation(UUID idInvito, UUID idUtente, boolean accettato) {
        if (idInvito == null || idUtente == null) {
            throw new IllegalArgumentException("idInvito e idUtente non possono essere null.");
        }
        return invitationService.processResponse(idInvito, idUtente, accettato);
    }
}
