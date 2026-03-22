package hackhub.service;

import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSummaryDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInIscrizione;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per HackathonService (caso d'uso: Visualizzare informazioni pubbliche sugli hackathon).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HackathonService — Visualizzare informazioni pubbliche sugli hackathon")
class VisualizzaHackathonPubbliciServiceTest {

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

    private HackathonService service;

    private static final UUID ID_HACK_1 = UUID.randomUUID();
    private static final UUID ID_HACK_2 = UUID.randomUUID();
    private static final UUID ID_ORG = UUID.randomUUID();
    private static final UUID ID_GIUDICE = UUID.randomUUID();
    private static final LocalDateTime BASE = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new HackathonService(
                hackathonRepository,
                giudiceRepository,
                mentoreRepository,
                organizzatoreRepository,
                notificationsService
        );
    }

    // =========================================================================
    // getHackathonDisponibili()
    // =========================================================================

    @Nested
    @DisplayName("getHackathonDisponibili()")
    class GetHackathonDisponibili {

        @Test
        @DisplayName("Due hackathon disponibili → restituisce lista con due HackathonSummaryDTO")
        void dueDisponibili_restituisceLista() {
            // Given
            Hackathon h1 = hackathon(ID_HACK_1, "HackAlpha", new StatoInIscrizione());
            Hackathon h2 = hackathon(ID_HACK_2, "HackBeta", new StatoInCorso());
            when(hackathonRepository.findAllAvailable()).thenReturn(List.of(h1, h2));

            // When
            List<HackathonSummaryDTO> lista = service.getHackathonDisponibili();

            // Then
            assertEquals(2, lista.size());

            HackathonSummaryDTO s1 = lista.get(0);
            assertEquals(ID_HACK_1, s1.id());
            assertEquals("HackAlpha", s1.nome());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, s1.stato());
            assertNotNull(s1.dataInizio());
            assertNotNull(s1.dataFine());

            HackathonSummaryDTO s2 = lista.get(1);
            assertEquals(ID_HACK_2, s2.id());
            assertEquals("HackBeta", s2.nome());
            assertEquals(StatoHackathon.IN_CORSO, s2.stato());
        }

        @Test
        @DisplayName("Nessun hackathon disponibile → lancia HackathonNotFoundException")
        void nessunoDisponibile_lancia() {
            when(hackathonRepository.findAllAvailable()).thenReturn(List.of());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getHackathonDisponibili());
        }

        @Test
        @DisplayName("Un solo hackathon disponibile → lista con un elemento")
        void unSoloDisponibile_restituisceListaConUnoElemento() {
            Hackathon h = hackathon(ID_HACK_1, "HackSolo", new StatoInIscrizione());
            when(hackathonRepository.findAllAvailable()).thenReturn(List.of(h));

            List<HackathonSummaryDTO> lista = service.getHackathonDisponibili();

            assertEquals(1, lista.size());
            assertEquals("HackSolo", lista.get(0).nome());
        }
    }

    // =========================================================================
    // getDettagliHackathon()
    // =========================================================================

    @Nested
    @DisplayName("getDettagliHackathon()")
    class GetDettagliHackathon {

        @Test
        @DisplayName("Hackathon disponibile trovato → restituisce HackathonResponseDTO completo")
        void trovato_restituisceDTO() {
            // Given
            Hackathon h = hackathon(ID_HACK_1, "HackAlpha", new StatoInIscrizione());
            when(hackathonRepository.findByIdAndDisponibile(ID_HACK_1))
                    .thenReturn(Optional.of(h));

            // When
            HackathonResponseDTO result = service.getDettagliHackathon(ID_HACK_1);

            // Then
            assertNotNull(result);
            assertEquals(ID_HACK_1, result.id());
            assertEquals("HackAlpha", result.nome());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, result.stato());
            assertNotNull(result.dataInizio());
            assertNotNull(result.dataFine());
        }

        @Test
        @DisplayName("Hackathon non trovato o concluso → lancia HackathonNotFoundException")
        void nonTrovatoOConcluso_lancia() {
            when(hackathonRepository.findByIdAndDisponibile(ID_HACK_1))
                    .thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getDettagliHackathon(ID_HACK_1));
        }

        @Test
        @DisplayName("Hackathon IN_CORSO disponibile → restituisce DTO con stato IN_CORSO")
        void inCorso_restituisceDTO() {
            Hackathon h = hackathon(ID_HACK_2, "HackAttivo", new StatoInCorso());
            when(hackathonRepository.findByIdAndDisponibile(ID_HACK_2))
                    .thenReturn(Optional.of(h));

            HackathonResponseDTO result = service.getDettagliHackathon(ID_HACK_2);

            assertEquals(StatoHackathon.IN_CORSO, result.stato());
        }

        @Test
        @DisplayName("findByIdAndDisponibile chiamato esattamente una volta con l'id corretto")
        void chiamataRepositoryCorretta() {
            when(hackathonRepository.findByIdAndDisponibile(ID_HACK_1))
                    .thenReturn(Optional.of(hackathon(ID_HACK_1, "H", new StatoInIscrizione())));

            service.getDettagliHackathon(ID_HACK_1);

            verify(hackathonRepository, times(1)).findByIdAndDisponibile(ID_HACK_1);
            verify(hackathonRepository, never()).findById(any());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Hackathon hackathon(UUID id, String nome, hackhub.model.state.HackathonState stato) {
        return new Hackathon(
                id, nome,
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                1000.0, null, 4, "Regolamento",
                ID_ORG, ID_GIUDICE,
                new ArrayList<>(),
                stato
        );
    }
}
