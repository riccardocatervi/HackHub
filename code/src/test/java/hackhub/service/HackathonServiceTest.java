package hackhub.service;

import hackhub.dto.GiudiceDTO;
import hackhub.dto.HackathonFormDataDTO;
import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSubmissionDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.model.entity.Address;
import hackhub.model.entity.Giudice;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.Organizzatore;
import hackhub.model.state.StatoHackathon;
import hackhub.repository.GiudiceRepository;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.OrganizzatoreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitari per HackathonService (caso d'uso: Organizzare Hackathon).
 * Le dipendenze sono sostituite da mock Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HackathonService — Organizzare Hackathon")
class HackathonServiceTest {

    @Mock
    private HackathonRepository hackathonRepository;
    @Mock
    private GiudiceRepository giudiceRepository;
    @Mock
    private MentoreRepository mentoreRepository;
    @Mock
    private OrganizzatoreRepository organizzatoreRepository;
    @Mock
    private NotificationsService notificationsService;

    private HackathonService hackathonService;

    private static final LocalDateTime BASE = LocalDateTime.now();
    private static final UUID ID_GIUDICE = UUID.randomUUID();
    private static final UUID ID_MENTORE = UUID.randomUUID();
    private static final UUID ID_ORG = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        hackathonService = new HackathonService(
                hackathonRepository,
                giudiceRepository,
                mentoreRepository,
                organizzatoreRepository,
                notificationsService
        );
    }

    // =========================================================================
    // creaHackathon — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("creaHackathon()")
    class CreaHackathon {

        @Test
        @DisplayName("Dati validi → salva l'hackathon, notifica lo staff, restituisce IN_ISCRIZIONE")
        void datiValidi_salvaERestituisce() {
            // Given
            HackathonSubmissionDTO dto = buildDtoValido();
            stubGiudiceDisponibile();
            stubMentoreDisponibile();
            // Il mock di save setta l'id sull'entità (simula il comportamento del DB)
            doAnswer(invocation -> {
                Hackathon h = invocation.getArgument(0);
                h.setId(UUID.randomUUID());
                return null;
            }).when(hackathonRepository).save(any(Hackathon.class));

            // When
            HackathonResponseDTO response = hackathonService.creaHackathon(dto);

            // Then
            assertNotNull(response);
            assertNotNull(response.getId());
            assertEquals("Hackathon Test", response.getNome());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, response.getStato());
            verify(hackathonRepository).save(any(Hackathon.class));
            verify(notificationsService).notificaStaff(any(Hackathon.class));
        }

        @Test
        @DisplayName("Giudice non trovato → lancia HackathonNotFoundException")
        void giudiceNonTrovato_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            when(giudiceRepository.findById(ID_GIUDICE)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> hackathonService.creaHackathon(dto));
            verify(hackathonRepository, never()).save(any());
        }

        @Test
        @DisplayName("Giudice non disponibile → lancia IllegalArgumentException")
        void giudiceNonDisponibile_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            when(giudiceRepository.findById(ID_GIUDICE))
                    .thenReturn(Optional.of(new Giudice(ID_GIUDICE, "Mario", "Rossi", false)));

            assertThrows(IllegalArgumentException.class,
                    () -> hackathonService.creaHackathon(dto));
            verify(hackathonRepository, never()).save(any());
        }

        @Test
        @DisplayName("Mentore non trovato → lancia HackathonNotFoundException")
        void mentoreNonTrovato_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            stubGiudiceDisponibile();
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> hackathonService.creaHackathon(dto));
            verify(hackathonRepository, never()).save(any());
        }

        @Test
        @DisplayName("Mentore non disponibile → lancia IllegalArgumentException")
        void mentoreNonDisponibile_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            stubGiudiceDisponibile();
            when(mentoreRepository.findById(ID_MENTORE))
                    .thenReturn(Optional.of(new Mentore(ID_MENTORE, "Anna", "Bianchi", false)));

            assertThrows(IllegalArgumentException.class,
                    () -> hackathonService.creaHackathon(dto));
            verify(hackathonRepository, never()).save(any());
        }
    }

    // =========================================================================
    // creaHackathon — validazione date
    // =========================================================================

    @Nested
    @DisplayName("creaHackathon() — validazione date")
    class ValidazioneDateTest {

        @Test
        @DisplayName("Data inizio dopo data fine → lancia IllegalArgumentException")
        void dataInizioDopoDataFine_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            dto.setDataInizio(BASE.plusDays(20));
            dto.setDataFine(BASE.plusDays(10));

            assertThrows(IllegalArgumentException.class,
                    () -> hackathonService.creaHackathon(dto));
        }

        @Test
        @DisplayName("Scadenza iscrizioni non antecedente a data inizio → lancia IllegalArgumentException")
        void scadenzaIscrizioniNonAntecedente_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            dto.setScadenzaIscrizioni(BASE.plusDays(12)); // dopo dataInizio (plusDays(10))

            assertThrows(IllegalArgumentException.class,
                    () -> hackathonService.creaHackathon(dto));
        }

        @Test
        @DisplayName("Scadenza sottomissioni prima di data inizio → lancia IllegalArgumentException")
        void scadenzaSottomissioniPrimaInizio_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            dto.setScadenzaSottomissioni(BASE.plusDays(5)); // prima di dataInizio (plusDays(10))

            assertThrows(IllegalArgumentException.class,
                    () -> hackathonService.creaHackathon(dto));
        }

        @Test
        @DisplayName("Scadenza sottomissioni dopo data fine → lancia IllegalArgumentException")
        void scadenzaSottomissioniDopoFine_lancia() {
            HackathonSubmissionDTO dto = buildDtoValido();
            dto.setScadenzaSottomissioni(BASE.plusDays(25)); // dopo dataFine (plusDays(20))

            assertThrows(IllegalArgumentException.class,
                    () -> hackathonService.creaHackathon(dto));
        }
    }

    // =========================================================================
    // getFormData
    // =========================================================================

    @Nested
    @DisplayName("getFormData()")
    class GetFormData {

        @Test
        @DisplayName("Organizzatore trovato → restituisce form con giudici e mentori disponibili")
        void organizzatoreEsiste_restituisceForm() {
            // Given
            when(organizzatoreRepository.findById(ID_ORG))
                    .thenReturn(Optional.of(new Organizzatore(ID_ORG, "Luca", "Verdi", "luca@test.it")));
            when(giudiceRepository.findAllDisponibili())
                    .thenReturn(List.of(new Giudice(ID_GIUDICE, "Sara", "Neri", true)));
            when(mentoreRepository.findAllDisponibili())
                    .thenReturn(List.of(new Mentore(ID_MENTORE, "Marco", "Blu", true)));

            // When
            HackathonFormDataDTO form = hackathonService.getFormData(ID_ORG);

            // Then
            assertNotNull(form);
            assertEquals("Luca Verdi", form.getNomeOrganizzatore());
            assertEquals(1, form.getGiudiciDisponibili().size());
            assertEquals(1, form.getMentoriDisponibili().size());
            GiudiceDTO giudiceDto = form.getGiudiciDisponibili().get(0);
            assertEquals("Sara", giudiceDto.getNome());
        }

        @Test
        @DisplayName("Organizzatore non trovato → lancia RuntimeException")
        void organizzatoreNonTrovato_lancia() {
            when(organizzatoreRepository.findById(ID_ORG)).thenReturn(Optional.empty());
            assertThrows(RuntimeException.class,
                    () -> hackathonService.getFormData(ID_ORG));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private HackathonSubmissionDTO buildDtoValido() {
        HackathonSubmissionDTO dto = new HackathonSubmissionDTO();
        dto.setNome("Hackathon Test");
        dto.setRegolamento("Regolamento di test");
        dto.setLuogo(new Address("Via Roma", 1, "Milano", "20100", "MI"));
        dto.setPremio(1000.0);
        dto.setDimensioneMaxTeam(5);
        dto.setIdOrganizzatore(ID_ORG);
        dto.setIdGiudice(ID_GIUDICE);
        dto.setIdsMentori(List.of(ID_MENTORE));
        dto.setDataInizio(BASE.plusDays(10));
        dto.setDataFine(BASE.plusDays(20));
        dto.setScadenzaIscrizioni(BASE.plusDays(5));
        dto.setScadenzaSottomissioni(BASE.plusDays(15));
        return dto;
    }

    private void stubGiudiceDisponibile() {
        when(giudiceRepository.findById(ID_GIUDICE))
                .thenReturn(Optional.of(new Giudice(ID_GIUDICE, "Mario", "Rossi", true)));
    }

    private void stubMentoreDisponibile() {
        when(mentoreRepository.findById(ID_MENTORE))
                .thenReturn(Optional.of(new Mentore(ID_MENTORE, "Anna", "Bianchi", true)));
    }
}
