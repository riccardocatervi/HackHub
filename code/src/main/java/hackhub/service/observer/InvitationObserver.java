package hackhub.service.observer;

import hackhub.model.entity.Invito;

import java.util.UUID;

/**
 * Observer per gli eventi del ciclo di vita degli inviti a unirsi a un team.
 * <p>
 * Implementato da {@link hackhub.service.NotificationsService} per notificare
 * il creatore del team dell'esito della risposta all'invito.
 */
public interface InvitationObserver {

    /**
     * Chiamato quando un utente accetta un invito a unirsi al team.
     *
     * @param invito       l'invito accettato
     * @param idCreatore   id del leader del team (destinatario della notifica)
     * @param nomeInvitato nome completo dell'utente che ha accettato
     */
    public void onInvitoAccettato(Invito invito, UUID idCreatore, String nomeInvitato);

    /**
     * Chiamato quando un utente rifiuta un invito a unirsi al team.
     *
     * @param invito       l'invito rifiutato
     * @param idCreatore   id del leader del team (destinatario della notifica)
     * @param nomeInvitato nome completo dell'utente che ha rifiutato
     */
    public void onInvitoRifiutato(Invito invito, UUID idCreatore, String nomeInvitato);
}
