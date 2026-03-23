package hackhub.service.observer;

import hackhub.model.entity.Invito;

/**
 * Observer per l'evento di creazione di un nuovo invito a unirsi a un team.
 * Implementato da {@link hackhub.service.NotificationsService}.
 */
public interface NuovoInvitoObserver {

    /**
     * Invocato quando un nuovo invito viene creato e salvato con successo.
     *
     * @param invito            l'invito appena creato
     * @param emailDestinatario l'indirizzo email dell'utente invitato
     * @param nomeTeam          il nome del team per cui è stato emesso l'invito
     */
    public void onNuovoInvito(Invito invito, String emailDestinatario, String nomeTeam);
}
