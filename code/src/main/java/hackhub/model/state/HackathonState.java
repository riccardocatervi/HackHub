package hackhub.model.state;

/**
 * Interfaccia del pattern State per il ciclo di vita dell'Hackathon.
 * Ogni stato concreto implementa le guardie sulle operazioni permesse.
 */
public interface HackathonState {

    /**
     * Verifica che lo stato corrente consenta l'invio di una sottomissione.
     * Lancia IllegalStateTransitionException se l'operazione non è consentita.
     */
    void accettaSottomissione();

    /**
     * Verifica che lo stato corrente consenta la proclamazione del vincitore.
     * Lancia IllegalStateTransitionException se l'operazione non è consentita.
     */
    void accettaProclamazione();

    /**
     * Restituisce il valore enum corrispondente a questo stato (usato per la persistenza).
     */
    StatoHackathon getNome();
}
