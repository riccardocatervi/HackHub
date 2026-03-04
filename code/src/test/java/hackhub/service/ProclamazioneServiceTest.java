package hackhub.service;

import hackhub.dto.ProclamazioneFormDTO;
import hackhub.dto.ProclamazioneResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.IllegalStateTransitionException;
import hackhub.exception.TeamNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoInCorso;
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
 * Test unitari per ProclamazioneService (caso d'uso: Proclamare Vincitore).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProclamazioneService — Proclamare Vincitore")
class ProclamazioneServiceTest {

    @Mock
    private HackathonRepository hackathonRepository;
    @Mock
    private SottomissioneRepository sottomissioneRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private NotificationsService notificationsService;

    private ProclamazioneService proclamazioneService;

    private static final LocalDateTime BASE = LocalDateTime.now();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_TEAM = UUID.randomUUID();
    private static final UUID ID_SOTTOMISS = UUID.randomUUID();
    private static final double PREMIO = 2000.0;

    @BeforeEach
    void setUp() {
        proclamazioneService = new ProclamazioneService(
                hackathonRepository,
                sottomissioneRepository,
                teamRepository,
                notificationsService
        );
    }

    // =========================================================================
    // eseguiProclamazione — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("eseguiProclamazione()")
    class EseguiProclamazione {

        @Test
        @DisplayName("Hackathon IN_VALUTAZIONE, vincitore valutato → CONCLUSO, flag vincitore, notifica")
        void hackathonInValutazione_completa() {
            // Given
            Hackathon hackathon = hackathonConStato(new StatoInValutazione());
            Sottomissione vincitrice = buildSottomissione();
            Team teamVincitore = buildTeam();

            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon));
            when(sottomissioneRepository.findVincitore(ID_HACKATHON)).thenReturn(Optional.of(vincitrice));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamVincitore));

            // When
            ProclamazioneResponseDTO response =
                    proclamazioneService.eseguiProclamazione(ID_HACKATHON);

            // Then
            assertNotNull(response);
            assertEquals(StatoHackathon.CONCLUSO, response.getStato());
            assertEquals("Team Vincitore", response.getNomeTeamVincitore());
            assertEquals(ID_TEAM, response.getIdTeamVincitore());
            assertEquals(PREMIO, response.getPremio());

            // Verifica interazioni con i repository e il servizio notifiche
            verify(sottomissioneRepository).markAsVincitore(ID_SOTTOMISS);
            verify(hackathonRepository).updateStato(ID_HACKATHON, StatoHackathon.CONCLUSO);
            verify(notificationsService).notificaProclamazione(hackathon, teamVincitore);
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> proclamazioneService.eseguiProclamazione(ID_HACKATHON));
            verify(sottomissioneRepository, never()).markAsVincitore(any());
            verify(hackathonRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_CORSO → lancia IllegalStateTransitionException")
        void hackathonInCorso_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));

            assertThrows(IllegalStateTransitionException.class,
                    () -> proclamazioneService.eseguiProclamazione(ID_HACKATHON));
            verify(sottomissioneRepository, never()).markAsVincitore(any());
            verify(hackathonRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("Nessuna sottomissione valutata → lancia RuntimeException")
        void nessunaVincitriceValutata_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInValutazione())));
            when(sottomissioneRepository.findVincitore(ID_HACKATHON))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class,
                    () -> proclamazioneService.eseguiProclamazione(ID_HACKATHON));
            verify(sottomissioneRepository, never()).markAsVincitore(any());
        }

        @Test
        @DisplayName("Team vincitore non trovato nel repository → lancia TeamNotFoundException")
        void teamVincitoreNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInValutazione())));
            when(sottomissioneRepository.findVincitore(ID_HACKATHON))
                    .thenReturn(Optional.of(buildSottomissione()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> proclamazioneService.eseguiProclamazione(ID_HACKATHON));
            verify(sottomissioneRepository, never()).markAsVincitore(any());
        }
    }

    // =========================================================================
    // preparaProclamazione
    // =========================================================================

    @Nested
    @DisplayName("preparaProclamazione()")
    class PreparaProclamazione {

        @Test
        @DisplayName("Hackathon IN_VALUTAZIONE, vincitore trovato → restituisce form corretto")
        void hackathonInValutazione_restituisceForm() {
            Hackathon hackathon = hackathonConStato(new StatoInValutazione());
            Sottomissione vincitrice = buildSottomissione();
            Team teamVincitore = buildTeam();

            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon));
            when(sottomissioneRepository.findVincitore(ID_HACKATHON)).thenReturn(Optional.of(vincitrice));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamVincitore));

            ProclamazioneFormDTO form =
                    proclamazioneService.preparaProclamazione(ID_HACKATHON);

            assertNotNull(form);
            assertEquals("Hackathon Test", form.getNomeHackathon());
            assertEquals("Team Vincitore", form.getNomeTeamVincitore());
            assertEquals(PREMIO, form.getPremio());
        }

        @Test
        @DisplayName("Hackathon IN_CORSO → lancia IllegalStateTransitionException")
        void hackathonInCorso_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));

            assertThrows(IllegalStateTransitionException.class,
                    () -> proclamazioneService.preparaProclamazione(ID_HACKATHON));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Hackathon hackathonConStato(HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "Hackathon Test",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                PREMIO, null, 5, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(), List.of(),
                stato
        );
    }

    private Sottomissione buildSottomissione() {
        return new Sottomissione(
                ID_SOTTOMISS,
                "https://github.com/test/repo",
                "https://demo.test.com",
                "Progetto vincitore",
                LocalDateTime.now().minusHours(2),
                ID_TEAM,
                ID_HACKATHON,
                false,
                true
        );
    }

    private Team buildTeam() {
        return new Team(ID_TEAM, "Team Vincitore", "Il miglior team", UUID.randomUUID(), ID_HACKATHON);
    }
}
