package hackhub.model.state;

import hackhub.exception.IllegalStateTransitionException;

/**
 * Stato terminale CONCLUSO: l'hackathon è terminato con la proclamazione del vincitore.
 * Nessuna operazione ulteriore è consentita.
 */
public class StatoConcluso implements HackathonState {

    @Override
    public void accettaSottomissione() {
        throw new IllegalStateTransitionException(
            "Impossibile inviare una sottomissione: l'hackathon è già concluso."
        );
    }

    @Override
    public void accettaProclamazione() {
        throw new IllegalStateTransitionException(
            "Impossibile proclamare il vincitore: l'hackathon è già concluso."
        );
    }

    @Override
    public StatoHackathon getNome() {
        return StatoHackathon.CONCLUSO;
    }
}
