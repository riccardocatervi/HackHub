package hackhub.service;

import hackhub.dto.HackathonFormDataDTO;
import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSubmissionDTO;
import hackhub.dto.HackathonSummaryDTO;
import hackhub.dto.HackathonUpdatedDTO;
import hackhub.dto.MentoreDTO;
import hackhub.dto.ModificaMentoriRequestDTO;
import hackhub.dto.PersonaDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.model.entity.Giudice;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.Organizzatore;
import hackhub.repository.GiudiceRepository;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.OrganizzatoreRepository;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servizio per il caso d'uso 'Organizzare Hackathon'.
 * Coordina la validazione, la creazione dell'entità e la persistenza.
 */
@Service
public class HackathonService {

    private final HackathonRepository hackathonRepository;
    private final GiudiceRepository giudiceRepository;
    private final MentoreRepository mentoreRepository;
    private final OrganizzatoreRepository organizzatoreRepository;
    private final NotificationsService notificationsService;

    public HackathonService(HackathonRepository hackathonRepository,
                            GiudiceRepository giudiceRepository,
                            MentoreRepository mentoreRepository,
                            OrganizzatoreRepository organizzatoreRepository,
                            NotificationsService notificationsService) {
        this.hackathonRepository = hackathonRepository;
        this.giudiceRepository = giudiceRepository;
        this.mentoreRepository = mentoreRepository;
        this.organizzatoreRepository = organizzatoreRepository;
        this.notificationsService = notificationsService;
    }

    /**
     * Recupera i dati necessari per popolare il form di creazione hackathon:
     * nome dell'organizzatore, giudici e mentori disponibili.
     */
    public HackathonFormDataDTO getFormData(UUID idOrganizzatore) {
        Organizzatore organizzatore = organizzatoreRepository.findById(idOrganizzatore)
                .orElseThrow(() -> new RuntimeException("Organizzatore non trovato: " + idOrganizzatore));

        List<PersonaDTO> giudici = giudiceRepository.findAllDisponibili().stream()
                .map(g -> new PersonaDTO(g.getId(), g.getNome(), g.getCognome()))
                .collect(Collectors.toList());

        List<PersonaDTO> mentori = mentoreRepository.findAllDisponibili().stream()
                .map(m -> new PersonaDTO(m.getId(), m.getNome(), m.getCognome()))
                .collect(Collectors.toList());

        String nomeOrganizzatore = organizzatore.getNome() + " " + organizzatore.getCognome();
        return new HackathonFormDataDTO(nomeOrganizzatore, giudici, mentori);

    }

    /**
     * Crea un nuovo hackathon in stato IN_ISCRIZIONE.
     * Esegue la validazione delle date, verifica la disponibilità dello staff
     * e persiste l'entità nel database.
     */
    public HackathonResponseDTO creaHackathon(HackathonSubmissionDTO dto) {
        validaDate(dto);

        // Verifica esistenza del giudice selezionato
        Giudice giudice = giudiceRepository.findById(dto.getIdGiudice())
                .orElseThrow(() -> new HackathonNotFoundException("Giudice non trovato: " + dto.getIdGiudice()));

        if (!giudice.isDisponibile()) {
            throw new IllegalArgumentException("Il giudice selezionato non è disponibile.");
        }

        // Verifica esistenza e disponibilità di tutti i mentori selezionati
        List<Mentore> mentori = dto.getIdsMentori().stream()
                .map(idM -> {
                    Mentore m = mentoreRepository.findById(idM)
                            .orElseThrow(() -> new HackathonNotFoundException("Mentore non trovato: " + idM));
                    if (!m.isDisponibile()) {
                        throw new IllegalArgumentException("Il mentore " + idM + " non è disponibile.");
                    }
                    return m;
                })
                .collect(Collectors.toList());

        // Creazione entità: lo stato iniziale IN_ISCRIZIONE è garantito dal costruttore
        Hackathon hackathon = new Hackathon(
                dto.getNome(),
                dto.getDataInizio(),
                dto.getDataFine(),
                dto.getScadenzaIscrizioni(),
                dto.getScadenzaSottomissioni(),
                dto.getPremio(),
                dto.getLuogo(),
                dto.getDimensioneMaxTeam(),
                dto.getRegolamento(),
                dto.getIdOrganizzatore(),
                dto.getIdGiudice(),
                dto.getIdsMentori()
        );

        hackathonRepository.save(hackathon);

        // Notifica staff dell'avvenuta creazione
        notificationsService.notificaStaff(hackathon);

        return toResponseDTO(hackathon);
    }

    private void validaDate(HackathonSubmissionDTO dto) {
        if (dto.getDataInizio().isAfter(dto.getDataFine())) {
            throw new IllegalArgumentException(
                    "La data di inizio deve essere antecedente alla data di fine.");
        }
        if (!dto.getScadenzaIscrizioni().isBefore(dto.getDataInizio())) {
            throw new IllegalArgumentException(
                    "La scadenza iscrizioni deve essere antecedente alla data di inizio.");
        }
        if (dto.getScadenzaSottomissioni().isBefore(dto.getDataInizio()) ||
                dto.getScadenzaSottomissioni().isAfter(dto.getDataFine())) {
            throw new IllegalArgumentException(
                    "La scadenza sottomissioni deve essere compresa nel periodo dell'hackathon.");
        }
    }

    private HackathonResponseDTO toResponseDTO(Hackathon h) {
        return new HackathonResponseDTO(
                h.getId(),
                h.getNome(),
                h.getDataInizio(),
                h.getDataFine(),
                h.getScadenzaIscrizioni(),
                h.getScadenzaSottomissioni(),
                h.getPremio(),
                h.getLuogo(),
                h.getDimensioneMaxTeam(),
                h.getRegolamento(),
                h.getIdOrganizzatore(),
                h.getIdGiudice(),
                h.getIdsMentori(),
                h.getStatoEnum()
        );
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Gestire mentori di un hackathon
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista sintetica degli hackathon creati dall'organizzatore.
     * Punto di ingresso del caso d'uso 'Gestire mentori di un hackathon'.
     *
     * @param idOrganizzatore l'id dell'organizzatore autenticato
     * @return lista di {@link hackhub.dto.HackathonSummaryDTO} degli hackathon di competenza
     */
    public List<HackathonSummaryDTO> getHackathonByOrganizzatore(UUID idOrganizzatore) {
        return hackathonRepository.findByOrganizzatore(idOrganizzatore).stream()
                .map(h -> new HackathonSummaryDTO(
                        h.getId(),
                        h.getNome(),
                        h.getStatoEnum(),
                        h.getDataInizio(),
                        h.getDataFine()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i dettagli completi di un hackathon, inclusa la lista dei mentori attuali.
     *
     * @param idHackathon l'id dell'hackathon selezionato
     * @return {@link HackathonResponseDTO} con tutti i dati dell'hackathon
     * @throws HackathonNotFoundException se l'id non corrisponde ad alcun hackathon
     */
    public HackathonResponseDTO getHackathonDetails(UUID idHackathon) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + idHackathon));
        return toResponseDTO(hackathon);
    }

    /**
     * Restituisce i mentori disponibili (non ancora assegnati) per un hackathon.
     * Permette all'organizzatore di scegliere nuovi mentori da aggiungere.
     *
     * @param idHackathon l'id dell'hackathon
     * @return lista di {@link hackhub.dto.MentoreDTO} dei mentori aggiungibili
     * @throws HackathonNotFoundException se l'hackathon non esiste
     */
    public List<MentoreDTO> getMentoriDisponibili(UUID idHackathon) {
        hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + idHackathon));

        return mentoreRepository.findAllAvailable(idHackathon).stream()
                .map(m -> new MentoreDTO(m.getId(), m.getNome(), m.getCognome(), m.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce la lista dei mentori attualmente assegnati a un hackathon.
     *
     * @param idHackathon l'id dell'hackathon
     * @return lista di {@link hackhub.dto.MentoreDTO} dei mentori correnti
     * @throws HackathonNotFoundException se l'hackathon non esiste
     */
    public List<MentoreDTO> getListaMentori(UUID idHackathon) {
        Hackathon hackathon = hackathonRepository.findById(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + idHackathon));

        return mentoreRepository.findAllByIds(hackathon.getIdsMentori()).stream()
                .map(m -> new MentoreDTO(m.getId(), m.getNome(), m.getCognome(), m.getEmail()))
                .collect(Collectors.toList());
    }

    /**
     * Applica le modifiche alla lista dei mentori di un hackathon.
     * <p>
     * Flusso:
     * <ol>
     *   <li>Carica l'hackathon dal database.</li>
     *   <li>Applica rimozioni e aggiunte in memoria tramite l'entità.</li>
     *   <li>Valida il vincolo di dominio: almeno un mentore deve rimanere assegnato.</li>
     *   <li>Persiste la nuova lista tramite repository (operazione atomica).</li>
     *   <li>Notifica i mentori aggiunti e rimossi.</li>
     * </ol>
     *
     * @param dto il DTO con le liste di mentori da aggiungere e rimuovere
     * @return {@link HackathonUpdatedDTO} con i dati aggiornati dell'hackathon
     * @throws HackathonNotFoundException             se l'hackathon non esiste
     * @throws IllegalStateException                  se si tenta di rimuovere tutti i mentori
     * @throws hackhub.exception.PersistenceException se il salvataggio fallisce
     */
    public HackathonUpdatedDTO updateMentori(ModificaMentoriRequestDTO dto) {
        Hackathon hackathon = hackathonRepository.findById(dto.idHackathon())
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato: " + dto.idHackathon()));

        // Recupera le istanze dei mentori coinvolti per le notifiche
        List<Mentore> mentoriDaRimuovere = mentoreRepository.findAllByIds(dto.idMentoriDaRimuovere());
        List<Mentore> mentoriDaAggiungere = mentoreRepository.findAllByIds(dto.idMentoriDaAggiungere());

        // Applica le modifiche in memoria tramite l'entità (Information Expert)
        for (UUID idRimosso : dto.idMentoriDaRimuovere()) {
            hackathon.removeMentore(idRimosso);
        }
        for (UUID idAggiunto : dto.idMentoriDaAggiungere()) {
            hackathon.addMentore(idAggiunto);
        }

        // Validazione del vincolo di dominio: almeno un mentore deve rimanere
        if (hackathon.getIdsMentori().isEmpty()) {
            throw new IllegalStateException(
                    "L'hackathon '" + hackathon.getNome() +
                            "' deve avere almeno un mentore assegnato. " +
                            "Impossibile completare la rimozione.");
        }

        // Persistenza atomica della nuova lista
        hackathonRepository.updateMentori(hackathon.getId(), hackathon.getIdsMentori());

        // Notifiche agli interessati
        if (!mentoriDaAggiungere.isEmpty()) {
            notificationsService.notificaNuoviMentori(mentoriDaAggiungere, hackathon);
        }
        if (!mentoriDaRimuovere.isEmpty()) {
            notificationsService.notificaRimozioneMentori(mentoriDaRimuovere, hackathon);
        }

        // Costruzione della risposta con i dati aggiornati
        List<MentoreDTO> mentoriAttuali = mentoreRepository.findAllByIds(hackathon.getIdsMentori())
                .stream()
                .map(m -> new MentoreDTO(m.getId(), m.getNome(), m.getCognome(), m.getEmail()))
                .collect(Collectors.toList());

        return new HackathonUpdatedDTO(
                hackathon.getId(),
                hackathon.getNome(),
                mentoriAttuali,
                "Lista mentori dell'hackathon '" + hackathon.getNome() + "' aggiornata con successo."
        );
    }

    // -----------------------------------------------------------------------
    // Caso d'uso: Visualizzare informazioni pubbliche sugli hackathon
    // -----------------------------------------------------------------------

    /**
     * Restituisce la lista sintetica di tutti gli hackathon pubblicamente disponibili
     * (stato diverso da CONCLUSO).
     * Punto di ingresso del caso d'uso 'Visualizzare informazioni pubbliche sugli hackathon'.
     *
     * @return lista di {@link HackathonSummaryDTO} degli hackathon attivi
     * @throws HackathonNotFoundException se non esistono hackathon disponibili
     */
    public List<HackathonSummaryDTO> getHackathonDisponibili() {
        List<Hackathon> hackathon = hackathonRepository.findAllAvailable();
        if (hackathon.isEmpty()) {
            throw new HackathonNotFoundException(
                    "Nessun hackathon pubblico disponibile al momento.");
        }
        return hackathon.stream()
                .map(h -> new HackathonSummaryDTO(
                        h.getId(),
                        h.getNome(),
                        h.getStatoEnum(),
                        h.getDataInizio(),
                        h.getDataFine()))
                .collect(Collectors.toList());
    }

    /**
     * Restituisce i dettagli pubblici di un hackathon disponibile selezionato dall'utente.
     * Verifica che l'hackathon esista e non sia in stato CONCLUSO prima di restituire i dati.
     *
     * @param idHackathon l'id dell'hackathon selezionato
     * @return {@link HackathonResponseDTO} con i dati completi dell'hackathon
     * @throws HackathonNotFoundException se l'hackathon non esiste o non è più disponibile
     */
    public HackathonResponseDTO getDettagliHackathon(UUID idHackathon) {
        Hackathon hackathon = hackathonRepository.findByIdAndDisponibile(idHackathon)
                .orElseThrow(() -> new HackathonNotFoundException(
                        "Hackathon non trovato o non più disponibile: " + idHackathon));
        return toResponseDTO(hackathon);
    }
}
