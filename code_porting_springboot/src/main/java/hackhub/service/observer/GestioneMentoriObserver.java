package hackhub.service.observer;

import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;

import java.util.List;

/**
 * Observer per gli eventi di gestione dei mentori di un hackathon.
 * Implementato da {@link hackhub.service.NotificationsService}.
 */
public interface GestioneMentoriObserver {

    /**
     * Invocato quando nuovi mentori vengono aggiunti a un hackathon.
     *
     * @param nuoviMentori lista dei mentori appena aggiunti
     * @param hackathon    l'hackathon di riferimento
     */
    public void onMentoriAggiunti(List<Mentore> nuoviMentori, Hackathon hackathon);

    /**
     * Invocato quando dei mentori vengono rimossi da un hackathon.
     *
     * @param mentoriRimossi lista dei mentori rimossi
     * @param hackathon      l'hackathon di riferimento
     */
    public void onMentoriRimossi(List<Mentore> mentoriRimossi, Hackathon hackathon);
}
