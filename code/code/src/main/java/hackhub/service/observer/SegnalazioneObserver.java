package hackhub.service.observer;

import hackhub.model.entity.Segnalazione;

import java.util.UUID;

/**
 * Interfaccia Observer (pattern GoF) per gli eventi di segnalazione violazione.
 * I componenti interessati (es. NotificationsService) implementano questa
 * interfaccia per essere notificati quando una nuova segnalazione viene registrata.
 */
public interface SegnalazioneObserver {

    /**
     * Invocato quando una nuova segnalazione di violazione è stata salvata.
     *
     * @param segnalazione     la segnalazione appena persistita
     * @param organizzatoreId  id dell'organizzatore dell'hackathon da notificare
     */
    void onNuovaSegnalazione(Segnalazione segnalazione, UUID organizzatoreId);
}
