package hackhub.service;

import hackhub.dto.GiudiceDTO;
import hackhub.dto.HackathonFormDataDTO;
import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSubmissionDTO;
import hackhub.dto.MentoreDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.model.entity.Giudice;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.Organizzatore;
import hackhub.repository.GiudiceRepository;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.OrganizzatoreRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Servizio per il caso d'uso 'Organizzare Hackathon'.
 * Coordina la validazione, la creazione dell'entità e la persistenza.
 */
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

        List<GiudiceDTO> giudici = giudiceRepository.findAllDisponibili().stream()
                .map(g -> new GiudiceDTO(g.getId(), g.getNome(), g.getCognome()))
                .collect(Collectors.toList());

        List<MentoreDTO> mentori = mentoreRepository.findAllDisponibili().stream()
                .map(m -> new MentoreDTO(m.getId(), m.getNome(), m.getCognome()))
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
}
