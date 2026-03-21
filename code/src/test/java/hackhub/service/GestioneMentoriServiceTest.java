package hackhub.service;

import hackhub.dto.HackathonSummaryDTO;
import hackhub.dto.HackathonUpdatedDTO;
import hackhub.dto.ModificaMentoriRequestDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test unitari per HackathonService (caso d'uso: Gestire mentori di un hackathon).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("HackathonService — Gestire mentori di un hackathon")
class GestioneMentoriServiceTest {

    @Mock private HackathonRepository hackathonRepository;
    @Mock private MentoreRepository mentoreRepository;
    @Mock private GiudiceRepository giudiceRepository;
    @Mock private OrganizzatoreRepository organizzatoreRepository;
    @Mock private NotificationsService notificationsService;

    private HackathonService service;

    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_ORG       = UUID.randomUUID();
    private static final UUID ID_MENTORE_1 = UUID.randomUUID();
    private static final UUID ID_MENTORE_2 = UUID.randomUUID();
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
    // updateMentori()
    // =========================================================================

    @Nested
    @DisplayName("updateMentori()")
    class UpdateMentori {

        @Test
        @DisplayName("Flusso felice: rimuove un mentore e ne aggiunge uno → persiste, notifica, restituisce DTO")
        void rimuoviEAggiungi_flussoCo() {
            // Given
            Hackathon hackathon = hackathonConMentori(new ArrayList<>(List.of(ID_MENTORE_1)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon));

            Mentore mentore1 = new Mentore(ID_MENTORE_1, "Anna", "Bianchi", "anna@test.it", true);
            Mentore mentore2 = new Mentore(ID_MENTORE_2, "Carlo", "Verdi", "carlo@test.it", true);

            // findAllByIds chiamato 3 volte: per rimossi, per aggiunti, e per la risposta
            when(mentoreRepository.findAllByIds(anyList()))
                    .thenReturn(List.of(mentore1))   // 1a chiamata: rimossi
                    .thenReturn(List.of(mentore2))   // 2a chiamata: aggiunti
                    .thenReturn(List.of(mentore2));  // 3a chiamata: lista finale per risposta

            ModificaMentoriRequestDTO dto = new ModificaMentoriRequestDTO(
                    ID_HACKATHON,
                    List.of(ID_MENTORE_2),  // da aggiungere
                    List.of(ID_MENTORE_1)   // da rimuovere
            );

            // When
            HackathonUpdatedDTO result = service.updateMentori(dto);

            // Then
            assertNotNull(result);
            assertEquals(ID_HACKATHON, result.idHackathon());
            assertEquals(1, result.mentoriAttuali().size());
            assertEquals(ID_MENTORE_2, result.mentoriAttuali().get(0).id());
            verify(hackathonRepository).updateMentori(eq(ID_HACKATHON), anyList());
            verify(notificationsService).notificaNuoviMentori(List.of(mentore2), hackathon);
            verify(notificationsService).notificaRimozioneMentori(List.of(mentore1), hackathon);
        }

        @Test
        @DisplayName("Solo aggiunta senza rimozione → NON chiama notificaRimozioneMentori")
        void soloAggiunta_nonNotificaRimozione() {
            // Given
            Hackathon hackathon = hackathonConMentori(new ArrayList<>(List.of(ID_MENTORE_1)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon));

            Mentore mentore2 = new Mentore(ID_MENTORE_2, "Carlo", "Verdi", "carlo@test.it", true);
            when(mentoreRepository.findAllByIds(anyList()))
                    .thenReturn(List.of())          // rimossi: lista vuota
                    .thenReturn(List.of(mentore2))  // aggiunti
                    .thenReturn(List.of(mentore2)); // risposta finale

            ModificaMentoriRequestDTO dto = new ModificaMentoriRequestDTO(
                    ID_HACKATHON,
                    List.of(ID_MENTORE_2),
                    List.of()
            );

            // When
            service.updateMentori(dto);

            // Then
            verify(notificationsService).notificaNuoviMentori(anyList(), any(Hackathon.class));
            verify(notificationsService, never()).notificaRimozioneMentori(anyList(), any());
        }

        @Test
        @DisplayName("Rimozione di tutti i mentori → lancia IllegalStateException, non persiste")
        void rimuoviTuttiIMentori_lanciaIllegalState() {
            // Given: hackathon con un solo mentore, si prova a rimuoverlo senza aggiungerne altri
            Hackathon hackathon = hackathonConMentori(new ArrayList<>(List.of(ID_MENTORE_1)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon));

            when(mentoreRepository.findAllByIds(anyList()))
                    .thenReturn(List.of(new Mentore(ID_MENTORE_1, "Anna", "Bianchi", "anna@test.it", true)))
                    .thenReturn(List.of()); // aggiunti: nessuno

            ModificaMentoriRequestDTO dto = new ModificaMentoriRequestDTO(
                    ID_HACKATHON,
                    List.of(),          // nessuna aggiunta
                    List.of(ID_MENTORE_1) // rimuove l'unico mentore
            );

            // When / Then
            assertThrows(IllegalStateException.class, () -> service.updateMentori(dto));
            verify(hackathonRepository, never()).updateMentori(any(), any());
            verify(notificationsService, never()).notificaNuoviMentori(any(), any());
            verify(notificationsService, never()).notificaRimozioneMentori(any(), any());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lanciaException() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            ModificaMentoriRequestDTO dto = new ModificaMentoriRequestDTO(
                    ID_HACKATHON, List.of(), List.of()
            );

            assertThrows(HackathonNotFoundException.class, () -> service.updateMentori(dto));
            verify(hackathonRepository, never()).updateMentori(any(), any());
        }
    }

    // =========================================================================
    // getHackathonByOrganizzatore()
    // =========================================================================

    @Nested
    @DisplayName("getHackathonByOrganizzatore()")
    class GetHackathonByOrganizzatore {

        @Test
        @DisplayName("Organizzatore con due hackathon → restituisce lista con due HackathonSummaryDTO")
        void dueHackathon_restituisceLista() {
            // Given
            Hackathon h1 = hackathonConMentori(new ArrayList<>());
            UUID id2 = UUID.randomUUID();
            Hackathon h2 = new Hackathon(id2, "Hack2",
                    BASE.plusDays(20), BASE.plusDays(30),
                    BASE.plusDays(15), BASE.plusDays(25),
                    500.0, null, 3, "Reg2",
                    ID_ORG, UUID.randomUUID(), new ArrayList<>(),
                    new StatoInIscrizione());

            when(hackathonRepository.findByOrganizzatore(ID_ORG)).thenReturn(List.of(h1, h2));

            // When
            List<HackathonSummaryDTO> lista = service.getHackathonByOrganizzatore(ID_ORG);

            // Then
            assertEquals(2, lista.size());
            assertEquals(ID_HACKATHON, lista.get(0).id());
            assertEquals(id2, lista.get(1).id());
        }

        @Test
        @DisplayName("Organizzatore senza hackathon → restituisce lista vuota")
        void nessunHackathon_restituisceListaVuota() {
            when(hackathonRepository.findByOrganizzatore(ID_ORG)).thenReturn(List.of());

            List<HackathonSummaryDTO> lista = service.getHackathonByOrganizzatore(ID_ORG);

            assertTrue(lista.isEmpty());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Hackathon hackathonConMentori(List<UUID> idsMentori) {
        return new Hackathon(
                ID_HACKATHON, "HackTest",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                1000.0, null, 4, "Regolamento",
                ID_ORG, UUID.randomUUID(),
                idsMentori,
                new StatoInIscrizione()
        );
    }
}
