package hackhub.service.observer;

import hackhub.model.StatoSegnalazione;

import java.util.UUID;

/**
 * Interfaccia Observer (pattern GoF) per gli eventi di gestione di una segnalazione.
 * I componenti interessati (es. {@link hackhub.service.NotificationsService}) implementano
 * questa interfaccia per essere notificati quando l'organizzatore prende una decisione
 * su una segnalazione (ACCETTATA o RIFIUTATA).
 */
public interface GestioneSegnalazioneObserver {

    /**
     * Invocato quando l'organizzatore ha gestito una segnalazione.
     *
     * @param idSegnalazione id della segnalazione gestita
     * @param mentoreId      id del mentore che ha inviato la segnalazione, da notificare
     * @param stato          nuovo stato assegnato alla segnalazione (ACCETTATA o RIFIUTATA)
     */
    public void onSegnalazioneGestita(UUID idSegnalazione, UUID mentoreId, StatoSegnalazione stato);
}
