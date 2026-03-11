package hackhub.service;

import hackhub.dto.SottomissioneAggiornamentoFormDTO;
import hackhub.dto.SottomissioneUpdateDTO;
import hackhub.dto.SottomissioneUpdateResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.IllegalStateTransitionException;
import hackhub.exception.SottomissioneNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
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
import org.mockito.ArgumentCaptor;
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
 * Test unitari per AggiornamentoSottomissioneService
 * (caso d'uso: Aggiornare Sottomissione del Team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AggiornamentoSottomissioneService — Aggiornare Sottomissione del Team")
class AggiornamentoSottomissioneServiceTest {

    @Mock private HackathonRepository    hackathonRepository;
    @Mock private SottomissioneRepository sottomissioneRepository;
    @Mock private TeamRepository         teamRepository;

    private AggiornamentoSottomissioneService service;

    private static final LocalDateTime BASE         = LocalDateTime.now();
    private static final UUID          ID_HACKATHON = UUID.randomUUID();
    private static final UUID          ID_TEAM      = UUID.randomUUID();
    private static final UUID          ID_SOTTOMISSIONE = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new AggiornamentoSottomissioneService(
                hackathonRepository,
                sottomissioneRepository,
                teamRepository
        );
    }

    // =========================================================================
    // aggiornaSottomissione() — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("aggiornaSottomissione()")
    class AggiornaSottomissione {

        @Test
        @DisplayName("Hackathon IN_CORSO, sottomissione trovata → aggiorna e restituisce DTO")
        void hackathonInCorso_sottomissioneTrovata_aggiornaERestituisce() {
            // Given
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(sottomissioneRepository.findByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                    .thenReturn(Optional.of(sottomissioneEsistente()));
            doNothing().when(sottomissioneRepository).update(any(Sottomissione.class));

            SottomissioneUpdateDTO dto = buildUpdateDto();

            // When
            SottomissioneUpdateResponseDTO response = service.aggiornaSottomissione(dto);

            // Then
            assertNotNull(response);
            assertEquals(ID_SOTTOMISSIONE, response.idSottomissione());
            assertEquals(ID_HACKATHON,    response.idHackathon());
            assertEquals(ID_TEAM,         response.idTeam());
            assertEquals("https://github.com/nuovo/repo", response.linkRepo());
            assertEquals("https://demo.nuovo.com",        response.linkDemo());
            assertEquals("Nuova descrizione aggiornata.", response.descrizione());
            assertNotNull(response.dataAggiornamento());
            verify(sottomissioneRepository).update(any(Sottomissione.class));
        }

        @Test
        @DisplayName("Il metodo update() viene invocato con l'entità aggiornata correttamente")
        void update_riceveEntitaAggiornata() {
            // Given
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(sottomissioneRepository.findByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                    .thenReturn(Optional.of(sottomissioneEsistente()));
            doNothing().when(sottomissioneRepository).update(any(Sottomissione.class));

            SottomissioneUpdateDTO dto = buildUpdateDto();

            // When
            service.aggiornaSottomissione(dto);

            // Then
            ArgumentCaptor<Sottomissione> captor = ArgumentCaptor.forClass(Sottomissione.class);
            verify(sottomissioneRepository).update(captor.capture());
            Sottomissione aggiornata = captor.getValue();
            assertEquals("https://github.com/nuovo/repo", aggiornata.getLinkRepo());
            assertEquals("https://demo.nuovo.com",        aggiornata.getLinkDemo());
            assertEquals("Nuova descrizione aggiornata.", aggiornata.getDescrizione());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.aggiornaSottomissione(buildUpdateDto()));
            verify(sottomissioneRepository, never()).update(any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_ISCRIZIONE → lancia IllegalStateTransitionException")
        void hackathonInIscrizione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInIscrizione())));

            assertThrows(IllegalStateTransitionException.class,
                    () -> service.aggiornaSottomissione(buildUpdateDto()));
            verify(sottomissioneRepository, never()).update(any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_VALUTAZIONE → lancia IllegalStateTransitionException")
        void hackathonInValutazione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInValutazione())));

            assertThrows(IllegalStateTransitionException.class,
                    () -> service.aggiornaSottomissione(buildUpdateDto()));
            verify(sottomissioneRepository, never()).update(any());
        }

        @Test
        @DisplayName("Sottomissione non trovata → lancia SottomissioneNotFoundException")
        void sottomissioneNonTrovata_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(sottomissioneRepository.findByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                    .thenReturn(Optional.empty());

            assertThrows(SottomissioneNotFoundException.class,
                    () -> service.aggiornaSottomissione(buildUpdateDto()));
            verify(sottomissioneRepository, never()).update(any());
        }

        @Test
        @DisplayName("LinkRepo nullo → lancia ValidationException senza toccare il repository")
        void linkRepoNullo_lancia() {
            SottomissioneUpdateDTO dto = new SottomissioneUpdateDTO(
                    ID_HACKATHON, ID_TEAM, null, "https://demo.it", "descrizione valida");

            assertThrows(ValidationException.class, () -> service.aggiornaSottomissione(dto));
            verify(hackathonRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Descrizione vuota → lancia ValidationException senza toccare il repository")
        void descrizioneVuota_lancia() {
            SottomissioneUpdateDTO dto = new SottomissioneUpdateDTO(
                    ID_HACKATHON, ID_TEAM, "https://repo.it", "https://demo.it", "  ");

            assertThrows(ValidationException.class, () -> service.aggiornaSottomissione(dto));
            verify(hackathonRepository, never()).findById(any());
        }
    }

    // =========================================================================
    // getFormDataAggiornamento()
    // =========================================================================

    @Nested
    @DisplayName("getFormDataAggiornamento()")
    class GetFormDataAggiornamento {

        @Test
        @DisplayName("Hackathon IN_CORSO, team e sottomissione trovati → restituisce form pre-popolato")
        void tuttoTrovato_restituisceForm() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alfa", "Desc", UUID.randomUUID(), ID_HACKATHON)));
            when(sottomissioneRepository.findByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                    .thenReturn(Optional.of(sottomissioneEsistente()));

            SottomissioneAggiornamentoFormDTO form =
                    service.getFormDataAggiornamento(ID_HACKATHON, ID_TEAM);

            assertNotNull(form);
            assertEquals(ID_HACKATHON,     form.idHackathon());
            assertEquals(ID_TEAM,          form.idTeam());
            assertEquals(ID_SOTTOMISSIONE, form.idSottomissione());
            assertEquals("https://github.com/vecchio/repo", form.linkRepoAttuale());
            assertEquals("https://demo.vecchio.com",        form.linkDemoAttuale());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getFormDataAggiornamento(ID_HACKATHON, ID_TEAM));
        }

        @Test
        @DisplayName("Hackathon IN_ISCRIZIONE → lancia IllegalStateTransitionException prima del team lookup")
        void hackathonInIscrizione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInIscrizione())));

            assertThrows(IllegalStateTransitionException.class,
                    () -> service.getFormDataAggiornamento(ID_HACKATHON, ID_TEAM));
            verify(teamRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.getFormDataAggiornamento(ID_HACKATHON, ID_TEAM));
        }

        @Test
        @DisplayName("Sottomissione non trovata → lancia SottomissioneNotFoundException")
        void sottomissioneNonTrovata_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alfa", "Desc", UUID.randomUUID(), ID_HACKATHON)));
            when(sottomissioneRepository.findByHackathonAndTeam(ID_HACKATHON, ID_TEAM))
                    .thenReturn(Optional.empty());

            assertThrows(SottomissioneNotFoundException.class,
                    () -> service.getFormDataAggiornamento(ID_HACKATHON, ID_TEAM));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SottomissioneUpdateDTO buildUpdateDto() {
        return new SottomissioneUpdateDTO(
                ID_HACKATHON,
                ID_TEAM,
                "https://github.com/nuovo/repo",
                "https://demo.nuovo.com",
                "Nuova descrizione aggiornata."
        );
    }

    private Sottomissione sottomissioneEsistente() {
        return new Sottomissione(
                ID_SOTTOMISSIONE,
                "https://github.com/vecchio/repo",
                "https://demo.vecchio.com",
                "Descrizione originale.",
                BASE.minusDays(1),
                ID_TEAM,
                ID_HACKATHON,
                false,
                false
        );
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
