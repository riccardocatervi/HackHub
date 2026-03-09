package hackhub.model.state;

import hackhub.exception.IllegalStateTransitionException;

/**
 * Stato IN_CORSO: l'hackathon è attivo. I team possono inviare le sottomissioni.
 * La proclamazione del vincitore non è ancora consentita.
 */
public class StatoInCorso implements HackathonState {

    @Override
    public void accettaSottomissione() {
        // Operazione consentita in questo stato: nessuna azione richiesta.
    }

    @Override
    public void accettaProclamazione() {
        throw new IllegalStateTransitionException(
                "Impossibile proclamare il vincitore: l'hackathon è ancora in corso."
        );
    }

    @Override
    public StatoHackathon getNome() {
        return StatoHackathon.IN_CORSO;
    }
}
