package hackhub.service.observer;

import hackhub.model.entity.Valutazione;

import java.util.UUID;

/**
 * Interfaccia Observer (pattern GoF) per gli eventi di valutazione sottomissione.
 * I componenti interessati (es. NotificationsService) implementano questa
 * interfaccia per essere notificati quando una valutazione viene completata.
 */
public interface ValutazioneObserver {

    /**
     * Invocato quando una valutazione è stata salvata con successo.
     *
     * @param valutazione  la valutazione appena persistita
     * @param idHackathon  id dell'hackathon di riferimento
     */
    void onValutazioneCompletata(Valutazione valutazione, UUID idHackathon);
}
