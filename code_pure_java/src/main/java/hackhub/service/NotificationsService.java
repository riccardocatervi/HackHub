package hackhub.service;

import hackhub.model.StatoSegnalazione;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Segnalazione;
import hackhub.model.entity.Team;
import hackhub.model.entity.Valutazione;
import hackhub.model.entity.Mentore;
import hackhub.service.observer.GestioneMentoriObserver;
import hackhub.service.observer.GestioneRichiestaSupportoObserver;
import hackhub.service.observer.GestioneSegnalazioneObserver;
import hackhub.service.observer.InvitationObserver;
import hackhub.service.observer.NuovoInvitoObserver;
import hackhub.service.observer.RichiestaSupportoObserver;
import hackhub.service.observer.RispostaCallObserver;
import hackhub.service.observer.SegnalazioneObserver;
import hackhub.service.observer.ValutazioneObserver;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Servizio responsabile dell'invio delle notifiche ai partecipanti.
 * <p>
 * Implementa i pattern Observer:
 * <ul>
 *   <li>{@link SegnalazioneObserver} — notifica l'organizzatore alla creazione di una segnalazione.</li>
 *   <li>{@link ValutazioneObserver} — notifica quando una valutazione viene completata.</li>
 *   <li>{@link GestioneSegnalazioneObserver} — notifica il mentore quando la sua segnalazione viene gestita.</li>
 * </ul>
 * <p>
 * Attualmente le notifiche sono implementate tramite log di sistema.
 * Progettato per essere esteso con email/push nelle iterazioni successive.
 */
public class NotificationsService implements SegnalazioneObserver, ValutazioneObserver,
        GestioneSegnalazioneObserver, RichiestaSupportoObserver, InvitationObserver,
        GestioneRichiestaSupportoObserver, GestioneMentoriObserver, NuovoInvitoObserver,
        RispostaCallObserver {

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
     * Notifica il leader e i membri del team vincitore dell'avvenuta erogazione del premio,
     * e informa tutti i partecipanti dell'hackathon della conclusione.
     *
     * @param hackathon       l'hackathon appena concluso
     * @param teamVincitore   il team che ha vinto
     * @param emailLeader     indirizzo email del leader del team vincitore
     * @param premioDisbursed true se il premio è stato erogato con successo
     */
    public void notificaVincitoreEPartecipanti(Hackathon hackathon, Team teamVincitore,
                                               String emailLeader, boolean premioDisbursed) {
        String statoErogazione = premioDisbursed
                ? String.format("Premio di €%.2f erogato con successo all'indirizzo %s.", hackathon.getPremio(), emailLeader)
                : "Erogazione del premio non completata — contattare l'organizzatore.";

        LOG.info(String.format(
                "Notifica vincitore: hackathon '%s' concluso. Team vincitore: '%s' (id: %s). %s",
                hackathon.getNome(),
                teamVincitore.getNome(),
                teamVincitore.getId(),
                statoErogazione
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

    /**
     * Riceve l'evento di nuova richiesta di supporto e notifica il mentore assegnato.
     * Implementazione di {@link RichiestaSupportoObserver}.
     */
    @Override
    public void onNuovaRichiestaSupporto(RichiestaSupporto richiesta, String emailMentore) {
        LOG.info(String.format(
                "Nuova richiesta di supporto (id: %s) dal team %s nell'hackathon %s — " +
                        "inoltrata al mentore (id: %s) all'indirizzo %s. Motivo: %s",
                richiesta.getId(),
                richiesta.getIdTeam(),
                richiesta.getIdHackathon(),
                richiesta.getIdMentore(),
                emailMentore,
                richiesta.getMotivo()
        ));
    }

    /**
     * Riceve l'evento di gestione di una segnalazione e notifica il mentore.
     * Implementazione di {@link GestioneSegnalazioneObserver}.
     */
    @Override
    public void onSegnalazioneGestita(UUID idSegnalazione, UUID mentoreId, StatoSegnalazione stato) {
        String esito = (stato == StatoSegnalazione.ACCETTATA)
                ? "ACCETTATA — il team segnalato è stato squalificato"
                : "RIFIUTATA — prove insufficienti, il team non subisce penalizzazioni";

        LOG.info(String.format(
                "Notifica mentore (id: %s): la segnalazione (id: %s) è stata %s.",
                mentoreId,
                idSegnalazione,
                esito
        ));
    }

    /**
     * Notifica il creatore del team che un utente ha accettato il suo invito.
     * Implementazione di {@link InvitationObserver}.
     */
    @Override
    public void onInvitoAccettato(Invito invito, UUID idCreatore, String nomeInvitato) {
        LOG.info(String.format(
                "Notifica leader (id: %s): l'utente '%s' ha ACCETTATO l'invito (id: %s) " +
                        "a unirsi al team %s per l'hackathon %s.",
                idCreatore,
                nomeInvitato,
                invito.getId(),
                invito.getIdTeam(),
                invito.getIdHackathon()
        ));
    }

    /**
     * Notifica il creatore del team che un utente ha rifiutato il suo invito.
     * Implementazione di {@link InvitationObserver}.
     */
    @Override
    public void onInvitoRifiutato(Invito invito, UUID idCreatore, String nomeInvitato) {
        LOG.info(String.format(
                "Notifica leader (id: %s): l'utente '%s' ha RIFIUTATO l'invito (id: %s) " +
                        "a unirsi al team %s per l'hackathon %s.",
                idCreatore,
                nomeInvitato,
                invito.getId(),
                invito.getIdTeam(),
                invito.getIdHackathon()
        ));
    }

    /**
     * Notifica il nuovo leader e i restanti membri del cambio di leadership.
     * Chiamato da TeamService quando il leader abbandona il team.
     *
     * @param nuovoLeaderId  id del nuovo leader eletto
     * @param membriRestanti lista dei membri che rimangono nel team (escluso l'ex-leader)
     * @param nomeTeam       nome del team
     */
    public void notificaCambioLeadership(UUID nuovoLeaderId,
                                         List<MembroTeam> membriRestanti,
                                         String nomeTeam) {
        LOG.info(String.format(
                "Notifica cambio leadership: l'utente (id: %s) è il nuovo leader del team '%s'.",
                nuovoLeaderId,
                nomeTeam
        ));
        for (MembroTeam membro : membriRestanti) {
            if (!membro.getId().equals(nuovoLeaderId)) {
                LOG.info(String.format(
                        "Notifica membro (id: %s): il leader del team '%s' è cambiato. " +
                                "Nuovo leader: %s.",
                        membro.getId(),
                        nomeTeam,
                        nuovoLeaderId
                ));
            }
        }
    }

    /**
     * Notifica il leader del team che un membro lo ha abbandonato.
     * Chiamato da TeamService quando un membro non-leader abbandona il team.
     *
     * @param idLeader id del leader corrente del team
     * @param idMembro id del membro che ha abbandonato
     * @param nomeTeam nome del team
     */
    public void notificaAbbandono(UUID idLeader, UUID idMembro, String nomeTeam) {
        LOG.info(String.format(
                "Notifica leader (id: %s): il membro (id: %s) ha abbandonato il team '%s'.",
                idLeader,
                idMembro,
                nomeTeam
        ));
    }

    // -------------------------------------------------------------------------
    // Implementazione GestioneRichiestaSupportoObserver (it.4)
    // -------------------------------------------------------------------------

    /**
     * Notifica il leader del team che la sua richiesta di supporto è stata presa in carico.
     * Implementazione di {@link GestioneRichiestaSupportoObserver}.
     */
    @Override
    public void onRichiestaAccettata(UUID idLeader, UUID idRichiesta, UUID idMentore) {
        LOG.info(String.format(
                "Notifica leader (id: %s): la richiesta di supporto (id: %s) è stata " +
                        "PRESA IN CARICO dal mentore (id: %s). Verrà pianificata una call a breve.",
                idLeader,
                idRichiesta,
                idMentore
        ));
    }

    /**
     * Notifica il leader del team che la sua richiesta di supporto è stata respinta.
     * Implementazione di {@link GestioneRichiestaSupportoObserver}.
     */
    @Override
    public void onRichiestaRespinta(UUID idLeader, UUID idRichiesta, UUID idMentore,
                                    String motivazione) {
        LOG.info(String.format(
                "Notifica leader (id: %s): la richiesta di supporto (id: %s) è stata " +
                        "RESPINTA dal mentore (id: %s). Motivazione: %s",
                idLeader,
                idRichiesta,
                idMentore,
                motivazione
        ));
    }

    /**
     * Notifica tutti i membri del team della disiscrizione e scioglimento del gruppo.
     * Chiamato da TeamRegistrationService dopo l'eliminazione del team.
     *
     * @param membri    lista dei membri del team (incluso il leader)
     * @param team      il team sciolto
     * @param hackathon l'hackathon da cui il team si è disiscritto
     */
    public void notifyTeamUnsubscription(List<MembroTeam> membri, Team team, Hackathon hackathon) {
        for (MembroTeam membro : membri) {
            LOG.info(String.format(
                    "Notifica membro (id: %s): il team '%s' (id: %s) si è disiscritto " +
                            "dall'hackathon '%s' (id: %s) ed è stato sciolto.",
                    membro.getId(),
                    team.getNome(),
                    team.getId(),
                    hackathon.getNome(),
                    hackathon.getId()
            ));
        }
    }

    // -------------------------------------------------------------------------
    // Implementazione GestioneMentoriObserver (it.5)
    // -------------------------------------------------------------------------

    /**
     * Notifica i mentori appena aggiunti all'hackathon.
     * Implementazione di {@link GestioneMentoriObserver}.
     */
    @Override
    public void onMentoriAggiunti(List<Mentore> nuoviMentori, Hackathon hackathon) {
        for (Mentore mentore : nuoviMentori) {
            LOG.info(String.format(
                    "Notifica mentore (id: %s, email: %s): sei stato aggiunto come mentore " +
                            "all'hackathon '%s' (id: %s).",
                    mentore.getId(),
                    mentore.getEmail(),
                    hackathon.getNome(),
                    hackathon.getId()
            ));
        }
    }

    /**
     * Notifica i mentori rimossi dall'hackathon.
     * Implementazione di {@link GestioneMentoriObserver}.
     */
    @Override
    public void onMentoriRimossi(List<Mentore> mentoriRimossi, Hackathon hackathon) {
        for (Mentore mentore : mentoriRimossi) {
            LOG.info(String.format(
                    "Notifica mentore (id: %s, email: %s): sei stato rimosso dalla lista mentori " +
                            "dell'hackathon '%s' (id: %s).",
                    mentore.getId(),
                    mentore.getEmail(),
                    hackathon.getNome(),
                    hackathon.getId()
            ));
        }
    }

    /**
     * Metodo diretto invocato da {@link HackathonService} per notificare l'aggiunta di mentori.
     *
     * @param nuoviMentori lista dei mentori aggiunti
     * @param hackathon    l'hackathon interessato
     */
    public void notificaNuoviMentori(List<Mentore> nuoviMentori, Hackathon hackathon) {
        onMentoriAggiunti(nuoviMentori, hackathon);
    }

    /**
     * Metodo diretto invocato da {@link HackathonService} per notificare la rimozione di mentori.
     *
     * @param mentoriRimossi lista dei mentori rimossi
     * @param hackathon      l'hackathon interessato
     */
    public void notificaRimozioneMentori(List<Mentore> mentoriRimossi, Hackathon hackathon) {
        onMentoriRimossi(mentoriRimossi, hackathon);
    }

    // -------------------------------------------------------------------------
    // Implementazione NuovoInvitoObserver (it.5)
    // -------------------------------------------------------------------------

    /**
     * Notifica l'utente destinatario della ricezione di un nuovo invito a unirsi a un team.
     * Implementazione di {@link NuovoInvitoObserver}.
     */
    @Override
    public void onNuovoInvito(Invito invito, String emailDestinatario, String nomeTeam) {
        LOG.info(String.format(
                "Notifica utente (email: %s): hai ricevuto un invito (id: %s) a unirti " +
                        "al team '%s' (id: %s) per l'hackathon %s.",
                emailDestinatario,
                invito.getId(),
                nomeTeam,
                invito.getIdTeam(),
                invito.getIdHackathon()
        ));
    }

    // -------------------------------------------------------------------------
    // Implementazione RispostaCallObserver (it.5)
    // -------------------------------------------------------------------------

    /**
     * Notifica il mentore che il leader del team ha accettato l'invito alla call.
     * Implementazione di {@link RispostaCallObserver}.
     */
    @Override
    public void onCallAccettata(UUID idCall, String emailMentore, String nomeTeam) {
        LOG.info(String.format(
                "Notifica mentore (email: %s): il team '%s' ha ACCETTATO l'invito " +
                        "alla call (id: %s). La call è confermata.",
                emailMentore,
                nomeTeam,
                idCall
        ));
    }

    /**
     * Notifica il mentore che il leader del team ha rifiutato l'invito alla call.
     * Implementazione di {@link RispostaCallObserver}.
     */
    @Override
    public void onCallRifiutata(UUID idCall, String emailMentore, String nomeTeam) {
        LOG.info(String.format(
                "Notifica mentore (email: %s): il team '%s' ha RIFIUTATO l'invito " +
                        "alla call (id: %s).",
                emailMentore,
                nomeTeam,
                idCall
        ));
    }
}
