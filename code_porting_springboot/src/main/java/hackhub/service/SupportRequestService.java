package hackhub.service;

import hackhub.dto.EsitoGestioneRichiestaDTO;
import hackhub.dto.RichiestaSupportoDettagliDTO;
import hackhub.exception.InvalidRequestStateException;
import hackhub.exception.SupportRequestNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.ValidationException;
import hackhub.model.StatoRichiesta;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.RichiestaSupportoRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.GestioneRichiestaSupportoObserver;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Prendere in carico una richiesta di supporto'.
 * <p>
 * Flusso principale:
 * <ol>
 *   <li>Il mentore visualizza la lista delle richieste pendenti a lui assegnate.</li>
 *   <li>Il mentore seleziona una richiesta per vederne i dettagli.</li>
 *   <li>Il mentore accetta (→ stato PRESA_IN_CARICO, notifica leader, avvio UC2)
 *       o rifiuta (→ stato RESPINTA, salva motivazione, notifica leader).</li>
 * </ol>
 * <p>
 * Le notifiche sono delegate agli observer registrati tramite il pattern GoF Observer
 * ({@link GestioneRichiestaSupportoObserver}).
 */
@Service
public class SupportRequestService {

    private final RichiestaSupportoRepository richiestaSupportoRepository;
    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;
    private final List<GestioneRichiestaSupportoObserver> observers = new ArrayList<>();

    public SupportRequestService(RichiestaSupportoRepository richiestaSupportoRepository,
                                 TeamRepository teamRepository,
                                 HackathonRepository hackathonRepository) {
        this.richiestaSupportoRepository = richiestaSupportoRepository;
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
    }

    // -------------------------------------------------------------------------
    // Registrazione osservatori (pattern GoF Observer)
    // -------------------------------------------------------------------------

    /**
     * Registra un observer per gli eventi di gestione delle richieste di supporto.
     */
    public void addObserver(GestioneRichiestaSupportoObserver observer) {
        this.observers.add(observer);
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: visualizzazione lista richieste pendenti
    // -------------------------------------------------------------------------

    /**
     * Restituisce la lista delle richieste di supporto in stato PENDENTE
     * assegnate al mentore specificato.
     *
     * @param idMentore id del mentore autenticato
     * @return lista di item DTO per la visualizzazione, eventualmente vuota
     */
    public List<RichiestaSupportoDettagliDTO> getRichiestePendentiByMentore(UUID idMentore) {
        List<RichiestaSupporto> richieste = richiestaSupportoRepository.findPendingByMentore(idMentore);
        List<RichiestaSupportoDettagliDTO> risultato = new ArrayList<>();

        for (RichiestaSupporto richiesta : richieste) {
            Team team = teamRepository.findById(richiesta.getIdTeam())
                    .orElseThrow(() -> new TeamNotFoundException(richiesta.getIdTeam()));

            String nomeHackathon = hackathonRepository.findById(richiesta.getIdHackathon())
                    .map(h -> h.getNome())
                    .orElse("N/D");

            risultato.add(new RichiestaSupportoDettagliDTO(
                    richiesta.getId(),
                    team.getId(),
                    team.getNome(),
                    richiesta.getIdHackathon(),
                    nomeHackathon,
                    richiesta.getMotivo(),
                    richiesta.getLivelloUrgenza(),
                    richiesta.getStato(),
                    richiesta.getDataInvio(),
                    null
            ));
        }
        return risultato;
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: visualizzazione dettagli richiesta
    // -------------------------------------------------------------------------

    /**
     * Restituisce i dettagli completi di una richiesta di supporto.
     * Verifica che il mentore richiedente sia il destinatario della richiesta.
     *
     * @param idRichiesta id della richiesta da visualizzare
     * @param idMentore   id del mentore autenticato
     * @return DTO con tutti i dati della richiesta
     * @throws SupportRequestNotFoundException se la richiesta non esiste
     * @throws UnauthorizedActionException     se il mentore non è il destinatario
     */
    public RichiestaSupportoDettagliDTO getDettagliRichiesta(UUID idRichiesta, UUID idMentore) {
        RichiestaSupporto richiesta = caricaEVerificaProprietario(idRichiesta, idMentore);

        Team team = teamRepository.findById(richiesta.getIdTeam())
                .orElseThrow(() -> new TeamNotFoundException(richiesta.getIdTeam()));

        String nomeHackathon = hackathonRepository.findById(richiesta.getIdHackathon())
                .map(h -> h.getNome())
                .orElse("N/D");

        return new RichiestaSupportoDettagliDTO(
                richiesta.getId(),
                team.getId(),
                team.getNome(),
                richiesta.getIdHackathon(),
                nomeHackathon,
                richiesta.getMotivo(),
                richiesta.getLivelloUrgenza(),
                richiesta.getStato(),
                richiesta.getDataInvio(),
                richiesta.getMotivazioneRifiuto()
        );
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: accettazione richiesta (flusso principale)
    // -------------------------------------------------------------------------

    /**
     * Processa l'accettazione di una richiesta di supporto da parte del mentore.
     * <p>
     * Passi:
     * <ol>
     *   <li>Verifica esistenza richiesta e autorizzazione del mentore.</li>
     *   <li>Verifica che la richiesta sia ancora PENDENTE.</li>
     *   <li>Aggiorna lo stato a PRESA_IN_CARICO nel database.</li>
     *   <li>Notifica il leader del team tramite gli observer.</li>
     * </ol>
     * Al termine, il controller avvierà automaticamente il UC 'Pianificare call con un team'.
     *
     * @param idRichiesta id della richiesta da accettare
     * @param idMentore   id del mentore autenticato
     * @return DTO di conferma con i dati del team per avviare il flusso di pianificazione call
     * @throws SupportRequestNotFoundException se la richiesta non esiste
     * @throws UnauthorizedActionException     se il mentore non è il destinatario
     * @throws InvalidRequestStateException    se la richiesta non è in stato PENDENTE
     */
    public EsitoGestioneRichiestaDTO accettaRichiesta(UUID idRichiesta, UUID idMentore) {
        RichiestaSupporto richiesta = caricaEVerificaStatoPendente(idRichiesta, idMentore);

        // Aggiornamento stato in memoria e nel DB
        richiesta.setStato(StatoRichiesta.PRESA_IN_CARICO);
        richiestaSupportoRepository.aggiornaStato(idRichiesta, StatoRichiesta.PRESA_IN_CARICO);

        // Notifica il leader del team tramite gli observer
        Team team = teamRepository.findById(richiesta.getIdTeam())
                .orElseThrow(() -> new TeamNotFoundException(richiesta.getIdTeam()));

        notificaObservers(team.getIdLeader(), idRichiesta, idMentore, true, null);

        return new EsitoGestioneRichiestaDTO(
                richiesta.getId(),
                team.getId(),
                team.getNome(),
                null,
                "Richiesta di supporto accettata. È possibile ora pianificare la call con il team '"
                        + team.getNome() + "'."
        );
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: rifiuto richiesta (flusso alternativo)
    // -------------------------------------------------------------------------

    /**
     * Processa il rifiuto di una richiesta di supporto da parte del mentore.
     * <p>
     * Passi:
     * <ol>
     *   <li>Valida la motivazione di rifiuto (obbligatoria).</li>
     *   <li>Verifica esistenza richiesta e autorizzazione del mentore.</li>
     *   <li>Verifica che la richiesta sia ancora PENDENTE.</li>
     *   <li>Aggiorna lo stato a RESPINTA e salva la motivazione nel database.</li>
     *   <li>Notifica il leader del team tramite gli observer.</li>
     * </ol>
     *
     * @param idRichiesta id della richiesta da rifiutare
     * @param idMentore   id del mentore autenticato
     * @param motivazione motivazione del rifiuto (obbligatoria)
     * @return DTO di conferma con la motivazione inserita
     * @throws ValidationException             se la motivazione è assente o troppo breve
     * @throws SupportRequestNotFoundException se la richiesta non esiste
     * @throws UnauthorizedActionException     se il mentore non è il destinatario
     * @throws InvalidRequestStateException    se la richiesta non è in stato PENDENTE
     */
    public EsitoGestioneRichiestaDTO respingiRichiesta(UUID idRichiesta, UUID idMentore, String motivazione) {
        validaMotivazione(motivazione);

        RichiestaSupporto richiesta = caricaEVerificaStatoPendente(idRichiesta, idMentore);

        // Aggiornamento stato e motivazione in memoria e nel DB
        richiesta.setStato(StatoRichiesta.RESPINTA);
        richiesta.setMotivazioneRifiuto(motivazione);
        richiestaSupportoRepository.aggiornaStatoEMotivazione(
                idRichiesta, StatoRichiesta.RESPINTA, motivazione);

        // Notifica il leader del team tramite gli observer
        Team team = teamRepository.findById(richiesta.getIdTeam())
                .orElseThrow(() -> new TeamNotFoundException(richiesta.getIdTeam()));

        notificaObservers(team.getIdLeader(), idRichiesta, idMentore, false, motivazione);

        return new EsitoGestioneRichiestaDTO(
                richiesta.getId(),
                team.getId(),
                team.getNome(),
                motivazione,
                "Richiesta di supporto rifiutata. Il leader del team '" + team.getNome()
                        + "' è stato notificato."
        );
    }

    // -------------------------------------------------------------------------
    // Logica interna di supporto
    // -------------------------------------------------------------------------

    /**
     * Carica la richiesta e verifica che il mentore ne sia il destinatario,
     * senza effettuare controlli sullo stato corrente.
     */
    private RichiestaSupporto caricaEVerificaProprietario(UUID idRichiesta, UUID idMentore) {
        RichiestaSupporto richiesta = richiestaSupportoRepository.findById(idRichiesta)
                .orElseThrow(() -> new SupportRequestNotFoundException(idRichiesta));

        if (!richiesta.getIdMentore().equals(idMentore)) {
            throw new UnauthorizedActionException(
                    "Il mentore " + idMentore + " non è il destinatario della richiesta " + idRichiesta);
        }
        return richiesta;
    }

    /**
     * Carica la richiesta, verifica il proprietario e controlla che sia PENDENTE.
     * Lancia eccezione se la richiesta è già stata gestita (PRESA_IN_CARICO o RESPINTA).
     */
    private RichiestaSupporto caricaEVerificaStatoPendente(UUID idRichiesta, UUID idMentore) {
        RichiestaSupporto richiesta = caricaEVerificaProprietario(idRichiesta, idMentore);

        if (richiesta.getStato() != StatoRichiesta.PENDENTE) {
            throw new InvalidRequestStateException(richiesta.getStato());
        }
        return richiesta;
    }

    /**
     * Notifica gli observer registrati dell'esito della gestione della richiesta.
     */
    private void notificaObservers(UUID idLeader, UUID idRichiesta, UUID idMentore,
                                   boolean accettata, String motivazione) {
        for (GestioneRichiestaSupportoObserver observer : observers) {
            if (accettata) {
                observer.onRichiestaAccettata(idLeader, idRichiesta, idMentore);
            } else {
                observer.onRichiestaRespinta(idLeader, idRichiesta, idMentore, motivazione);
            }
        }
    }

    /**
     * Valida la motivazione di rifiuto: deve essere presente e di almeno 10 caratteri.
     */
    private void validaMotivazione(String motivazione) {
        if (motivazione == null || motivazione.isBlank()) {
            throw new ValidationException("La motivazione del rifiuto è obbligatoria.");
        }
        if (motivazione.trim().length() < 10) {
            throw new ValidationException("La motivazione del rifiuto deve contenere almeno 10 caratteri.");
        }
    }
}
