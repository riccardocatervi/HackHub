package hackhub.service;

import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.Team;

import java.util.List;
import java.util.UUID;
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

    /**
     * Notifica gli utenti invitati a unirsi a un team.
     * Per ogni invito viene emesso un log; in produzione andrà sostituito
     * con l'invio di email o push notification.
     */
    public void inviaInvitiTeam(Team team, List<Invito> inviti) {
        if (inviti == null || inviti.isEmpty()) {
            LOG.info(String.format(
                    "Team '%s' (id: %s) creato senza inviti. " +
                    "Il leader parteciperà da solo o inviterà membri successivamente.",
                    team.getNome(), team.getId()));
            return;
        }

        for (Invito invito : inviti) {
            LOG.info(String.format(
                    "Invito inviato: utente %s invitato a unirsi al team '%s' (id: %s) " +
                    "per l'hackathon %s — stato invito: %s",
                    invito.getIdUtente(),
                    team.getNome(),
                    team.getId(),
                    invito.getIdHackathon(),
                    invito.getStato()
            ));
        }
    }

    /**
     * Notifica l'organizzatore della ricezione di una nuova segnalazione di violazione.
     */
    public void notificaOrganizzatore(UUID organizzatoreId, UUID segnalazioneId) {
        LOG.info(String.format(
                "Nuova segnalazione (id: %s) inoltrata all'organizzatore (id: %s) per revisione.",
                segnalazioneId, organizzatoreId));
    }
}
