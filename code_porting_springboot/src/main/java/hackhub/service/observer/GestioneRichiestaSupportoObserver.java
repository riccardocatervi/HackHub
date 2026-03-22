package hackhub.service.observer;

import java.util.UUID;

/**
 * Interfaccia Observer per gli eventi di gestione di una richiesta di supporto.
 * <p>
 * Implementata da {@link hackhub.service.NotificationsService} per inviare
 * notifiche al leader del team in merito alla decisione del mentore.
 * Usata nel caso d'uso 'Prendere in carico una richiesta di supporto'.
 */
public interface GestioneRichiestaSupportoObserver {

    /**
     * Evento fired quando il mentore accetta una richiesta di supporto.
     *
     * @param idLeader    id del leader del team richiedente da notificare
     * @param idRichiesta id della richiesta accettata
     * @param idMentore   id del mentore che ha accettato
     */
    public void onRichiestaAccettata(UUID idLeader, UUID idRichiesta, UUID idMentore);

    /**
     * Evento fired quando il mentore rifiuta una richiesta di supporto.
     *
     * @param idLeader    id del leader del team richiedente da notificare
     * @param idRichiesta id della richiesta rifiutata
     * @param idMentore   id del mentore che ha rifiutato
     * @param motivazione motivazione del rifiuto inserita dal mentore
     */
    public void onRichiestaRespinta(UUID idLeader, UUID idRichiesta, UUID idMentore,
                                    String motivazione);
}
