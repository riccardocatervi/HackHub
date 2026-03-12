package hackhub.service.observer;

import hackhub.model.entity.RichiestaSupporto;

/**
 * Interfaccia Observer (GoF) per gli eventi di nuova richiesta di supporto.
 * Consente di notificare il mentore assegnato in modo disaccoppiato dal servizio.
 */
public interface RichiestaSupportoObserver {

    /**
     * Invocato quando una nuova richiesta di supporto viene registrata nel sistema.
     *
     * @param richiesta    la richiesta di supporto appena creata
     * @param emailMentore indirizzo email del mentore destinatario della notifica
     */
    public void onNuovaRichiestaSupporto(RichiestaSupporto richiesta, String emailMentore);
}
