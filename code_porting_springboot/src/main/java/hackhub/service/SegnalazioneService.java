package hackhub.service;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.dto.SegnalazioneDettagliDTO;
import hackhub.dto.SegnalazioneGestioneDTO;
import hackhub.dto.SegnalazioneGestioneResponseDTO;
import hackhub.dto.SegnalazioneRequestDTO;
import hackhub.dto.SegnalazioneResponseDTO;
import hackhub.dto.TeamSegnalabileItemDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.SegnalazioneAlreadyManagedException;
import hackhub.exception.SegnalazioneNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.StatoSegnalazione;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Segnalazione;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.SegnalazioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.GestioneSegnalazioneObserver;
import hackhub.service.observer.SegnalazioneObserver;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per i casi d'uso:
 * <ul>
 *   <li>'Segnalare violazione del regolamento di un team' — {@link #segnalaViolazione}</li>
 *   <li>'Gestire penalizzazione o squalifica di un team' — {@link #getDettagliSegnalazione}, {@link #gestisciSegnalazione}</li>
 * </ul>
 * <p>
 * Utilizza il pattern GoF Observer:
 * <ul>
 *   <li>{@link SegnalazioneObserver} — notifica l'organizzatore alla creazione di una segnalazione.</li>
 *   <li>{@link GestioneSegnalazioneObserver} — notifica il mentore quando la sua segnalazione viene gestita.</li>
 * </ul>
 */
@Service
public class SegnalazioneService {

    private final SegnalazioneRepository segnalazioneRepository;
    private final TeamRepository teamRepository;
    private final HackathonRepository hackathonRepository;
    private final MentoreRepository mentoreRepository;   // può essere null (retro-compatibilità)

    private final List<SegnalazioneObserver> observers = new ArrayList<>();
    private final List<GestioneSegnalazioneObserver> gestioneObservers = new ArrayList<>();

    /**
     * Costruttore retro-compatibile (3 parametri).
     * Utilizzato dai test esistenti che non richiedono il lookup del mentore
     * né le notifiche di gestione.
     */
    public SegnalazioneService(SegnalazioneRepository segnalazioneRepository,
                               TeamRepository teamRepository,
                               HackathonRepository hackathonRepository) {
        this(segnalazioneRepository, teamRepository, hackathonRepository, null);
    }

    /**
     * Costruttore completo con accesso al repository dei mentori.
     * Da utilizzare in produzione per arricchire il DTO dei dettagli con
     * nome e cognome del mentore che ha inviato la segnalazione.
     */
    @Autowired
    public SegnalazioneService(SegnalazioneRepository segnalazioneRepository,
                               TeamRepository teamRepository,
                               HackathonRepository hackathonRepository,
                               MentoreRepository mentoreRepository) {
        this.segnalazioneRepository = segnalazioneRepository;
        this.teamRepository = teamRepository;
        this.hackathonRepository = hackathonRepository;
        this.mentoreRepository = mentoreRepository;
    }

    // -------------------------------------------------------------------------
    // Registrazione osservatori (pattern GoF Observer)
    // -------------------------------------------------------------------------

    /**
     * Registra un osservatore per gli eventi di nuova segnalazione.
     * (Pattern GoF Observer — subscribe)
     */
    public void addObserver(SegnalazioneObserver observer) {
        observers.add(observer);
    }

    /**
     * Registra un osservatore per gli eventi di gestione segnalazione.
     * (Pattern GoF Observer — subscribe)
     */
    public void addGestioneObserver(GestioneSegnalazioneObserver observer) {
        gestioneObservers.add(observer);
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: Segnalare violazione del regolamento
    // -------------------------------------------------------------------------

    /**
     * Recupera i dati necessari per il modulo di segnalazione:
     * nome dell'hackathon ed elenco dei team che il mentore può segnalare.
     *
     * @param hackathonId id dell'hackathon per cui si vuole aprire il modulo
     * @return DTO con i dati del form
     */
    public ModuloSegnalazioneDTO getDatiModulo(UUID hackathonId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + hackathonId));

        List<TeamSegnalabileItemDTO> teamsDTO = teamRepository
                .findByHackathon(hackathonId)
                .stream()
                .map(t -> new TeamSegnalabileItemDTO(t.getId(), t.getNome()))
                .toList();

        return new ModuloSegnalazioneDTO(hackathonId, hackathon.getNome(), teamsDTO);
    }

    /**
     * Valida e salva la segnalazione di violazione del regolamento.
     * Al termine notifica gli osservatori registrati (es. organizzatore).
     *
     * @param request dati compilati dal mentore nel modulo
     * @return DTO di conferma con id della segnalazione e messaggio di avvenuta registrazione
     */
    public SegnalazioneResponseDTO segnalaViolazione(SegnalazioneRequestDTO request) {
        validaRequest(request);

        Hackathon hackathon = hackathonRepository.findById(request.hackathonId())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + request.hackathonId()));

        // Verifica che il team appartenga all'hackathon
        teamRepository.findById(request.teamId())
                .filter(t -> t.getIdHackathon().equals(request.hackathonId()))
                .orElseThrow(() -> new ValidationException(
                        "Il team " + request.teamId() +
                                " non è registrato per l'hackathon " + request.hackathonId()));

        Segnalazione segnalazione = new Segnalazione(
                request.teamId(),
                request.mentoreId(),
                request.hackathonId(),
                request.descrizione(),
                request.prove()
        );

        segnalazioneRepository.save(segnalazione);

        // Notifica tutti gli osservatori registrati (pattern Observer)
        UUID organizzatoreId = hackathon.getIdOrganizzatore();
        for (SegnalazioneObserver observer : observers) {
            observer.onNuovaSegnalazione(segnalazione, organizzatoreId);
        }

        return new SegnalazioneResponseDTO(
                segnalazione.getId(),
                "Segnalazione registrata con successo. L'organizzatore è stato notificato."
        );
    }

    // -------------------------------------------------------------------------
    // Caso d'uso: Gestire penalizzazione o squalifica di un team
    // -------------------------------------------------------------------------

    /**
     * Carica i dettagli completi di una segnalazione per l'organizzatore.
     * Include nome del team segnalato e, se disponibile, nome del mentore che ha segnalato.
     *
     * @param idSegnalazione id della segnalazione selezionata dall'organizzatore
     * @return DTO con tutti i dettagli della segnalazione
     */
    public SegnalazioneDettagliDTO getDettagliSegnalazione(UUID idSegnalazione) {
        Segnalazione segnalazione = segnalazioneRepository.findById(idSegnalazione)
                .orElseThrow(() -> new SegnalazioneNotFoundException(idSegnalazione));

        Team team = teamRepository.findById(segnalazione.getTeamId())
                .orElseThrow(() -> new TeamNotFoundException(segnalazione.getTeamId()));

        String nomeMentore = null;
        if (mentoreRepository != null) {
            nomeMentore = mentoreRepository.findById(segnalazione.getMentoreId())
                    .map(m -> m.getNome() + " " + m.getCognome())
                    .orElse(null);
        }

        return new SegnalazioneDettagliDTO(
                segnalazione.getId(),
                segnalazione.getTeamId(),
                team.getNome(),
                segnalazione.getMentoreId(),
                nomeMentore,
                segnalazione.getDescrizione(),
                segnalazione.getProve(),
                segnalazione.getDataInvio(),
                segnalazione.getStato()
        );
    }

    /**
     * Gestisce la decisione dell'organizzatore su una segnalazione pendente.
     * <p>
     * Se l'azione è {@code ACCETTATA}: squalifica il team e persiste il flag nel DB.
     * Se l'azione è {@code RIFIUTATA}: aggiorna solo lo stato della segnalazione.
     * Al termine notifica il mentore tramite il pattern Observer.
     *
     * @param dto DTO con l'id della segnalazione e la decisione (ACCETTATA o RIFIUTATA)
     * @return DTO di risposta con il risultato dell'operazione
     */
    public SegnalazioneGestioneResponseDTO gestisciSegnalazione(SegnalazioneGestioneDTO dto) {
        validaGestioneRequest(dto);

        Segnalazione segnalazione = segnalazioneRepository.findById(dto.idSegnalazione())
                .orElseThrow(() -> new SegnalazioneNotFoundException(dto.idSegnalazione()));

        if (segnalazione.getStato() != StatoSegnalazione.PENDENTE) {
            throw new SegnalazioneAlreadyManagedException(dto.idSegnalazione());
        }

        boolean teamSqualificato = false;

        if (dto.azione() == StatoSegnalazione.ACCETTATA) {
            Team team = teamRepository.findById(segnalazione.getTeamId())
                    .orElseThrow(() -> new TeamNotFoundException(segnalazione.getTeamId()));

            team.squalifica();
            teamRepository.updateSqualificato(team.getId(), true);
            teamSqualificato = true;
        }

        segnalazione.setStato(dto.azione());
        segnalazioneRepository.updateStato(segnalazione.getId(), dto.azione());

        // Notifica tutti gli osservatori di gestione (pattern Observer)
        for (GestioneSegnalazioneObserver observer : gestioneObservers) {
            observer.onSegnalazioneGestita(segnalazione.getId(), segnalazione.getMentoreId(), dto.azione());
        }

        String messaggio = (dto.azione() == StatoSegnalazione.ACCETTATA)
                ? "Segnalazione accettata. Il team è stato squalificato dall'hackathon."
                : "Segnalazione rifiutata per prove insufficienti. Il team non subisce penalizzazioni.";

        return new SegnalazioneGestioneResponseDTO(
                segnalazione.getId(),
                dto.azione(),
                teamSqualificato,
                messaggio
        );
    }

    // -------------------------------------------------------------------------
    // Validazione input (GRASP: Information Expert)
    // -------------------------------------------------------------------------

    private void validaRequest(SegnalazioneRequestDTO request) {
        if (request.hackathonId() == null) {
            throw new ValidationException("L'id dell'hackathon è obbligatorio.");
        }
        if (request.mentoreId() == null) {
            throw new ValidationException("L'id del mentore è obbligatorio.");
        }
        if (request.teamId() == null) {
            throw new ValidationException("È necessario selezionare un team da segnalare.");
        }
        if (request.descrizione() == null || request.descrizione().isBlank()) {
            throw new ValidationException(
                    "La descrizione della violazione è obbligatoria e non può essere vuota.");
        }
        if (request.descrizione().length() < 10) {
            throw new ValidationException(
                    "La descrizione deve contenere almeno 10 caratteri.");
        }
    }

    private void validaGestioneRequest(SegnalazioneGestioneDTO dto) {
        if (dto.idSegnalazione() == null) {
            throw new ValidationException("L'id della segnalazione è obbligatorio.");
        }
        if (dto.azione() == null || dto.azione() == StatoSegnalazione.PENDENTE) {
            throw new ValidationException(
                    "L'azione deve essere ACCETTATA o RIFIUTATA, non può essere PENDENTE o nulla.");
        }
    }
}
