package hackhub.service;

import hackhub.dto.SottomissioneDaValutareItemDTO;
import hackhub.dto.SottomissioniDaValutareDTO;
import hackhub.dto.ValutazioneRequestDTO;
import hackhub.dto.ValutazioneResponseDTO;
import hackhub.exception.AlreadyEvaluatedException;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.SottomissioneNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.model.entity.Valutazione;
import hackhub.model.state.StatoHackathon;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.ValutazioneRepository;
import hackhub.service.observer.ValutazioneObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Servizio per il caso d'uso 'Valutare sottomissione di un team'.
 * <p>
 * Verifica che l'hackathon sia in stato IN_VALUTAZIONE, che la sottomissione
 * non sia già stata valutata, e che il punteggio sia nel range ammesso (0–10).
 * <p>
 * Utilizza il pattern GoF Observer: dopo il salvataggio della valutazione,
 * tutti gli osservatori registrati vengono notificati.
 */
public class ValutazioneService {

    private static final double PUNTEGGIO_MIN = 0.0;
    private static final double PUNTEGGIO_MAX = 10.0;

    private final ValutazioneRepository   valutazioneRepository;
    private final SottomissioneRepository sottomissioneRepository;
    private final HackathonRepository     hackathonRepository;
    private final TeamRepository          teamRepository;

    private final List<ValutazioneObserver> observers = new ArrayList<>();

    public ValutazioneService(ValutazioneRepository valutazioneRepository,
                              SottomissioneRepository sottomissioneRepository,
                              HackathonRepository hackathonRepository,
                              TeamRepository teamRepository) {
        this.valutazioneRepository  = valutazioneRepository;
        this.sottomissioneRepository = sottomissioneRepository;
        this.hackathonRepository    = hackathonRepository;
        this.teamRepository         = teamRepository;
    }

    /**
     * Registra un osservatore per gli eventi di valutazione.
     * (Pattern GoF Observer — subscribe)
     */
    public void addObserver(ValutazioneObserver observer) {
        observers.add(observer);
    }

    /**
     * Restituisce la lista completa di sottomissioni per la dashboard del giudice.
     * Ogni item include il flag {@code valutato} per consentire alla vista di
     * differenziare graficamente le voci già gestite da quelle ancora in attesa.
     *
     * @param hackathonId  id dell'hackathon di cui visualizzare le sottomissioni
     * @param giudiceId    id del giudice che accede alla dashboard
     * @return DTO con nome hackathon e lista completa delle sottomissioni
     */
    public SottomissioniDaValutareDTO getSottomissioniDaValutare(UUID hackathonId, UUID giudiceId) {
        Hackathon hackathon = hackathonRepository.findById(hackathonId)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + hackathonId));

        if (hackathon.getStatoEnum() != StatoHackathon.IN_VALUTAZIONE) {
            throw new InvalidHackathonStateException(
                    "Le valutazioni sono disponibili solo quando l'hackathon è in stato IN_VALUTAZIONE. " +
                    "Stato attuale: " + hackathon.getStatoEnum());
        }

        List<Sottomissione> sottomissioni = sottomissioneRepository.findAllByHackathon(hackathonId);

        List<SottomissioneDaValutareItemDTO> items = sottomissioni.stream()
                .map(s -> {
                    String nomeTeam = teamRepository.findById(s.getIdTeam())
                            .map(Team::getNome)
                            .orElse("Team sconosciuto");
                    return new SottomissioneDaValutareItemDTO(
                            s.getId(),
                            s.getIdTeam(),
                            nomeTeam,
                            s.getLinkRepo(),
                            s.getLinkDemo(),
                            s.getDescrizione(),
                            s.isValutato()
                    );
                })
                .toList();

        return new SottomissioniDaValutareDTO(hackathonId, hackathon.getNome(), items);
    }

    /**
     * Valida e salva la valutazione assegnata dal giudice a una sottomissione.
     * Segna la sottomissione come valutata e notifica gli osservatori.
     *
     * @param request  dati inseriti dal giudice (punteggio, giudizio, riferimenti)
     * @return DTO di conferma con i dati della valutazione salvata
     */
    public ValutazioneResponseDTO valutaSottomissione(ValutazioneRequestDTO request) {
        validaRequest(request);

        Hackathon hackathon = hackathonRepository.findById(request.hackathonId())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + request.hackathonId()));

        if (hackathon.getStatoEnum() != StatoHackathon.IN_VALUTAZIONE) {
            throw new InvalidHackathonStateException(
                    "La valutazione è consentita solo quando l'hackathon è in stato IN_VALUTAZIONE. " +
                    "Stato attuale: " + hackathon.getStatoEnum());
        }

        Sottomissione sottomissione = sottomissioneRepository.findById(request.sottomissioneId())
                .orElseThrow(() -> new SottomissioneNotFoundException(
                        "Sottomissione non trovata: " + request.sottomissioneId()));

        if (sottomissione.isValutato()) {
            throw new AlreadyEvaluatedException(
                    "La sottomissione " + request.sottomissioneId() +
                    " è già stata valutata e non può essere rivalutata.");
        }

        // Recupera il nome del team per il DTO di risposta
        String nomeTeam = teamRepository.findById(sottomissione.getIdTeam())
                .map(Team::getNome)
                .orElse("Team sconosciuto");

        // Creazione entità valutazione
        Valutazione valutazione = new Valutazione(
                request.punteggio(),
                request.giudizioScritto(),
                request.sottomissioneId(),
                request.giudiceId()
        );

        valutazioneRepository.save(valutazione);

        // Segna la sottomissione come valutata: il team non potrà più essere rivalutato
        sottomissioneRepository.markAsValutata(request.sottomissioneId());

        // Notifica tutti gli osservatori registrati (pattern Observer)
        for (ValutazioneObserver observer : observers) {
            observer.onValutazioneCompletata(valutazione, request.hackathonId());
        }

        return new ValutazioneResponseDTO(
                valutazione.getId(),
                request.sottomissioneId(),
                nomeTeam,
                request.punteggio(),
                request.giudizioScritto()
        );
    }

    // -----------------------------------------------------------------------
    // Validazione input (GRASP: Information Expert)
    // -----------------------------------------------------------------------

    private void validaRequest(ValutazioneRequestDTO request) {
        if (request.sottomissioneId() == null) {
            throw new ValidationException("L'id della sottomissione è obbligatorio.");
        }
        if (request.giudiceId() == null) {
            throw new ValidationException("L'id del giudice è obbligatorio.");
        }
        if (request.hackathonId() == null) {
            throw new ValidationException("L'id dell'hackathon è obbligatorio.");
        }
        if (request.punteggio() < PUNTEGGIO_MIN || request.punteggio() > PUNTEGGIO_MAX) {
            throw new ValidationException(String.format(
                    "Il punteggio deve essere compreso tra %.1f e %.1f. Valore inserito: %.2f",
                    PUNTEGGIO_MIN, PUNTEGGIO_MAX, request.punteggio()));
        }
        if (request.giudizioScritto() == null || request.giudizioScritto().isBlank()) {
            throw new ValidationException(
                    "Il giudizio scritto è obbligatorio e non può essere vuoto.");
        }
        if (request.giudizioScritto().length() < 10) {
            throw new ValidationException(
                    "Il giudizio scritto deve contenere almeno 10 caratteri.");
        }
    }
}
