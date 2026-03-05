package hackhub.model.state;

import hackhub.exception.IllegalStateTransitionException;

/**
 * Stato IN_ISCRIZIONE: l'hackathon è aperto alle iscrizioni dei team.
 * Non sono accettate sottomissioni né proclamazioni.
 */
public class StatoInIscrizione implements HackathonState {

    @Override
    public void accettaSottomissione() {
        throw new IllegalStateTransitionException(
                "Impossibile inviare una sottomissione: l'hackathon è ancora in fase di iscrizione."
        );
    }

    @Override
    public void accettaProclamazione() {
        throw new IllegalStateTransitionException(
                "Impossibile proclamare il vincitore: l'hackathon è ancora in fase di iscrizione."
        );
    }

    @Override
    public StatoHackathon getNome() {
        return StatoHackathon.IN_ISCRIZIONE;
    }
}
