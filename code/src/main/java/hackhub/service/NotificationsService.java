package hackhub.service;

import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.Segnalazione;
import hackhub.model.entity.Team;
import hackhub.model.entity.Valutazione;
import hackhub.service.observer.SegnalazioneObserver;
import hackhub.service.observer.ValutazioneObserver;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servizio responsabile dell'invio delle notifiche ai partecipanti.
 * <p>
 * Implementa i pattern Observer {@link SegnalazioneObserver} e {@link ValutazioneObserver}
 * per essere notificato quando vengono registrate segnalazioni o valutazioni.
 * <p>
 * Attualmente le notifiche sono implementate tramite log di sistema.
 * Progettato per essere esteso con email/push nelle iterazioni successive.
 */
public class NotificationsService implements SegnalazioneObserver, ValutazioneObserver {

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
                hackathon.getIdsMentori()
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

    // -------------------------------------------------------------------------
    // Implementazione Observer — pattern GoF
    // -------------------------------------------------------------------------

    /**
     * Riceve l'evento di nuova segnalazione e notifica l'organizzatore.
     * Implementazione di {@link SegnalazioneObserver}.
     */
    @Override
    public void onNuovaSegnalazione(Segnalazione segnalazione, UUID organizzatoreId) {
        LOG.info(String.format(
                "Nuova segnalazione (id: %s) per team %s nell'hackathon %s " +
                "inoltrata all'organizzatore (id: %s) per revisione immediata.",
                segnalazione.getId(),
                segnalazione.getTeamId(),
                segnalazione.getHackathonId(),
                organizzatoreId
        ));
    }

    /**
     * Riceve l'evento di valutazione completata e registra il log.
     * Implementazione di {@link ValutazioneObserver}.
     */
    @Override
    public void onValutazioneCompletata(Valutazione valutazione, UUID idHackathon) {
        LOG.info(String.format(
                "Valutazione completata (id: %s) per sottomissione %s nell'hackathon %s — " +
                "punteggio: %.2f, giudice: %s.",
                valutazione.getId(),
                valutazione.getIdSottomissione(),
                idHackathon,
                valutazione.getPunteggio(),
                valutazione.getIdGiudice()
        ));
    }
}
