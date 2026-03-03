package hackhub.service;

import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Team;

import java.util.logging.Logger;

/**
 * Servizio responsabile dell'invio delle notifiche ai partecipanti.
 * Attualmente implementa le notifiche tramite log di sistema.
 * Progettato per essere esteso con email/push nelle iterazioni successive.
 */
public class NotificationsService {

    private static final Logger LOG = Logger.getLogger(NotificationsService.class.getName());

    /**
     * Notifica giudice e mentori della creazione di un nuovo hackathon.
     */
    public void notificaStaff(Hackathon hackathon) {
        LOG.info(String.format(
                "Notifica staff: hackathon '%s' (id: %s) creato con stato %s. " +
                        "Giudice assegnato: %s — Mentori: %s",
                hackathon.getNome(),
                hackathon.getId(),
                hackathon.getStatoEnum(),
                hackathon.getIdGiudice(),
                hackathon.getIdMentori()
        ));
    }

    /**
     * Notifica tutti i partecipanti della proclamazione del team vincitore.
     */
    public void notificaProclamazione(Hackathon hackathon, Team teamVincitore) {
        LOG.info(String.format(
                "Proclamazione vincitore: hackathon '%s' (id: %s) → Team vincitore: '%s' (id: %s) — Premio: €%.2f — Stato finale: %s",
                hackathon.getNome(),
                hackathon.getId(),
                teamVincitore.getNome(),
                teamVincitore.getId(),
                hackathon.getPremio(),
                hackathon.getStatoEnum()
        ));
    }
}
