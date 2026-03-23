package hackhub.service.observer;

import java.util.UUID;

/**
 * Observer per gli eventi di risposta a un invito a call da parte del leader del team.
 * Implementato da {@link hackhub.service.NotificationsService}.
 * <p>
 * Parte del pattern GoF Observer applicato al caso d'uso
 * 'Gestire invito a call da parte di un mentore'.
 */
public interface RispostaCallObserver {

    /**
     * Invocato quando il leader del team accetta l'invito a una call.
     *
     * @param idCall       id della call accettata
     * @param emailMentore indirizzo email del mentore da notificare
     * @param nomeTeam     nome del team che ha accettato
     */
    public void onCallAccettata(UUID idCall, String emailMentore, String nomeTeam);

    /**
     * Invocato quando il leader del team rifiuta l'invito a una call.
     *
     * @param idCall       id della call rifiutata
     * @param emailMentore indirizzo email del mentore da notificare
     * @param nomeTeam     nome del team che ha rifiutato
     */
    public void onCallRifiutata(UUID idCall, String emailMentore, String nomeTeam);
}
