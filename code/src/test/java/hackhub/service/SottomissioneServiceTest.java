package hackhub.service;

import hackhub.dto.SottomissioneFormDTO;
import hackhub.dto.SottomissioneResponseDTO;
import hackhub.dto.SottomissioneSubmissionDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.IllegalStateTransitionException;
import hackhub.exception.SottomissioneAlreadyExistsException;
import hackhub.exception.TeamNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInIscrizione;
import hackhub.model.state.StatoInValutazione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;
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
 * Test unitari per SottomissioneService (caso d'uso: Inviare Sottomissione del Team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SottomissioneService — Inviare Sottomissione del Team")
class SottomissioneServiceTest {

    @Mock private HackathonRepository     hackathonRepository;
    @Mock private SottomissioneRepository sottomissioneRepository;
    @Mock private TeamRepository          teamRepository;

    private SottomissioneService sottomissioneService;

    private static final LocalDateTime BASE        = LocalDateTime.now();
    private static final UUID          ID_HACKATHON = UUID.randomUUID();
    private static final UUID          ID_TEAM      = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        sottomissioneService = new SottomissioneService(
            hackathonRepository,
            sottomissioneRepository,
            teamRepository
        );
    }

    // =========================================================================
    // inviaSottomissione — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("inviaSottomissione()")
    class InviaSottomissione {

        @Test
        @DisplayName("Hackathon IN_CORSO, nessun duplicato → salva e restituisce DTO")
        void hackathonInCorso_nessunDuplicato_salvaERestituisce() {
            // Given
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(sottomissioneRepository.existsByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                .thenReturn(false);
            doAnswer(invocation -> {
                Sottomissione s = invocation.getArgument(0);
                s.setId(UUID.randomUUID());
                return null;
            }).when(sottomissioneRepository).save(any(Sottomissione.class));

            // When
            SottomissioneResponseDTO response =
                sottomissioneService.inviaSottomissione(buildDto());

            // Then
            assertNotNull(response);
            assertNotNull(response.getIdSottomissione());
            assertNotNull(response.getDataInvio());
            verify(sottomissioneRepository).save(any(Sottomissione.class));
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                () -> sottomissioneService.inviaSottomissione(buildDto()));
            verify(sottomissioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_ISCRIZIONE → lancia IllegalStateTransitionException")
        void hackathonInIscrizione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInIscrizione())));

            assertThrows(IllegalStateTransitionException.class,
                () -> sottomissioneService.inviaSottomissione(buildDto()));
            verify(sottomissioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_VALUTAZIONE → lancia IllegalStateTransitionException")
        void hackathonInValutazione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInValutazione())));

            assertThrows(IllegalStateTransitionException.class,
                () -> sottomissioneService.inviaSottomissione(buildDto()));
            verify(sottomissioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Team ha già inviato una sottomissione → lancia SottomissioneAlreadyExistsException")
        void sottomissioneDuplicata_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(sottomissioneRepository.existsByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                .thenReturn(true);

            assertThrows(SottomissioneAlreadyExistsException.class,
                () -> sottomissioneService.inviaSottomissione(buildDto()));
            verify(sottomissioneRepository, never()).save(any());
        }
    }

    // =========================================================================
    // getFormData
    // =========================================================================

    @Nested
    @DisplayName("getFormData()")
    class GetFormData {

        @Test
        @DisplayName("Hackathon IN_CORSO, team trovato → restituisce form")
        void hackathonInCorso_teamTrovato_restituisceForm() {
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(teamRepository.findById(ID_TEAM))
                .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alfa", "Desc", UUID.randomUUID(), ID_HACKATHON)));

            SottomissioneFormDTO form =
                sottomissioneService.getFormData(ID_HACKATHON, ID_TEAM);

            assertNotNull(form);
            assertEquals(ID_HACKATHON, form.getIdHackathon());
            assertEquals(ID_TEAM, form.getIdTeam());
            assertEquals("Team Alfa", form.getNomeTeam());
        }

        @Test
        @DisplayName("Hackathon IN_ISCRIZIONE → lancia IllegalStateTransitionException prima del team lookup")
        void hackathonInIscrizione_lanciaImmediatamente() {
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInIscrizione())));

            assertThrows(IllegalStateTransitionException.class,
                () -> sottomissioneService.getFormData(ID_HACKATHON, ID_TEAM));
            verify(teamRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                () -> sottomissioneService.getFormData(ID_HACKATHON, ID_TEAM));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SottomissioneSubmissionDTO buildDto() {
        SottomissioneSubmissionDTO dto = new SottomissioneSubmissionDTO();
        dto.setIdHackathon(ID_HACKATHON);
        dto.setIdTeam(ID_TEAM);
        dto.setLinkRepository("https://github.com/test/progetto");
        dto.setLinkDemo("https://demo.test.com");
        dto.setDescrizione("Descrizione del progetto di test.");
        return dto;
    }

    private Hackathon hackathonConStato(HackathonState stato) {
        return new Hackathon(
            ID_HACKATHON, "Hackathon Test",
            BASE.plusDays(10), BASE.plusDays(20),
            BASE.plusDays(5),  BASE.plusDays(15),
            1000.0, null, 5, "Regolamento",
            UUID.randomUUID(), UUID.randomUUID(), List.of(),
            stato
        );
    }
}
