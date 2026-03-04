package hackhub.service;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.dto.SegnalazioneRequestDTO;
import hackhub.dto.SegnalazioneResponseDTO;
import hackhub.dto.TeamSegnalabileItemDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Segnalazione;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SegnalazioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.SegnalazioneObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Segnalare violazione del regolamento di un team'.
 * <p>
 * Utilizza il pattern GoF Observer: dopo il salvataggio della segnalazione,
 * tutti gli osservatori registrati (es. {@link NotificationsService}) vengono
 * notificati in modo da comunicare immediatamente all'organizzatore la violazione.
 */
public class SegnalazioneService {

    private final SegnalazioneRepository segnalazioneRepository;
    private final TeamRepository         teamRepository;
    private final HackathonRepository    hackathonRepository;

    private final List<SegnalazioneObserver> observers = new ArrayList<>();

    public SegnalazioneService(SegnalazioneRepository segnalazioneRepository,
                               TeamRepository teamRepository,
                               HackathonRepository hackathonRepository) {
        this.segnalazioneRepository = segnalazioneRepository;
        this.teamRepository         = teamRepository;
        this.hackathonRepository    = hackathonRepository;
    }

    /**
     * Registra un osservatore per gli eventi di segnalazione.
     * (Pattern GoF Observer — subscribe)
     */
    public void addObserver(SegnalazioneObserver observer) {
        observers.add(observer);
    }

    /**
     * Recupera i dati necessari per il modulo di segnalazione:
     * nome dell'hackathon ed elenco dei team che il mentore può segnalare.
     *
     * @param hackathonId  id dell'hackathon per cui si vuole aprire il modulo
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
     * @param request  dati compilati dal mentore nel modulo
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

    // -----------------------------------------------------------------------
    // Validazione input (GRASP: Information Expert)
    // -----------------------------------------------------------------------

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
}
