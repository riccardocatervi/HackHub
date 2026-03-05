package hackhub.model.state;

import hackhub.exception.IllegalStateTransitionException;

/**
 * Stato IN_VALUTAZIONE: le sottomissioni sono chiuse e il giudice le sta valutando.
 * La proclamazione è consentita solo quando tutte le sottomissioni sono state valutate.
 */
public class StatoInValutazione implements HackathonState {

    @Override
    public void accettaSottomissione() {
        throw new IllegalStateTransitionException(
                "Impossibile inviare una sottomissione: la scadenza per le sottomissioni è scaduta."
        );
    }

    @Override
    public void accettaProclamazione() {
        // Operazione consentita in questo stato: nessuna azione richiesta.
    }

    @Override
    public StatoHackathon getNome() {
        return StatoHackathon.IN_VALUTAZIONE;
    }
}
