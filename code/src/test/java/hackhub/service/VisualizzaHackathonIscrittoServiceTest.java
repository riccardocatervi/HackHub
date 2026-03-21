package hackhub.service;

import hackhub.dto.HackathonResponseDTO;
import hackhub.dto.HackathonSummaryDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoConcluso;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInIscrizione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.TeamRepository;
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
 * Test unitari per VisualizzaHackathonIscrittoService
 * (caso d'uso: Visualizzare hackathon a cui l'utente è iscritto).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VisualizzaHackathonIscrittoService — Visualizzare hackathon a cui l'utente è iscritto")
class VisualizzaHackathonIscrittoServiceTest {

    @Mock private TeamRepository teamRepository;
    @Mock private HackathonRepository hackathonRepository;

    private VisualizzaHackathonIscrittoService service;

    private static final UUID ID_UTENTE    = UUID.randomUUID();
    private static final UUID ID_LEADER    = UUID.randomUUID();
    private static final UUID ID_TEAM_1    = UUID.randomUUID();
    private static final UUID ID_TEAM_2    = UUID.randomUUID();
    private static final UUID ID_HACKATHON_1 = UUID.randomUUID();
    private static final UUID ID_HACKATHON_2 = UUID.randomUUID();
    private static final LocalDateTime BASE = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new VisualizzaHackathonIscrittoService(teamRepository, hackathonRepository);
    }

    // =========================================================================
    // getHackathonsByUserId()
    // =========================================================================

    @Nested
    @DisplayName("getHackathonsByUserId()")
    class GetHackathonsByUserId {

        @Test
        @DisplayName("Utente in due team → restituisce lista con due HackathonSummaryDTO")
        void dueTeam_restituisceListaCorretta() {
            // Given
            Team team1 = new Team(ID_TEAM_1, "Team Alpha", "Desc1", ID_LEADER, ID_HACKATHON_1);
            Team team2 = new Team(ID_TEAM_2, "Team Beta",  "Desc2", ID_LEADER, ID_HACKATHON_2);
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of(team1, team2));
            when(hackathonRepository.findById(ID_HACKATHON_1))
                    .thenReturn(Optional.of(hackathon(ID_HACKATHON_1, "HackAlpha", new StatoInIscrizione())));
            when(hackathonRepository.findById(ID_HACKATHON_2))
                    .thenReturn(Optional.of(hackathon(ID_HACKATHON_2, "HackBeta", new StatoInCorso())));

            // When
            List<HackathonSummaryDTO> lista = service.getHackathonsByUserId(ID_UTENTE);

            // Then
            assertEquals(2, lista.size());
            assertEquals(ID_HACKATHON_1, lista.get(0).id());
            assertEquals("HackAlpha", lista.get(0).nome());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, lista.get(0).stato());
            assertEquals(ID_HACKATHON_2, lista.get(1).id());
            assertEquals("HackBeta", lista.get(1).nome());
            assertEquals(StatoHackathon.IN_CORSO, lista.get(1).stato());
        }

        @Test
        @DisplayName("Utente in un solo team con hackathon CONCLUSO → restituisce lista con un elemento")
        void teamConHackathonConcluso_restituisceUnoElemento() {
            // Given
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc1", ID_LEADER, ID_HACKATHON_1);
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of(team));
            when(hackathonRepository.findById(ID_HACKATHON_1))
                    .thenReturn(Optional.of(hackathon(ID_HACKATHON_1, "HackConcluso", new StatoConcluso())));

            // When
            List<HackathonSummaryDTO> lista = service.getHackathonsByUserId(ID_UTENTE);

            // Then
            assertEquals(1, lista.size());
            assertEquals(StatoHackathon.CONCLUSO, lista.get(0).stato());
        }

        @Test
        @DisplayName("Utente non membro di alcun team → lancia HackathonNotFoundException")
        void nessunTeam_lancia() {
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getHackathonsByUserId(ID_UTENTE));

            verify(hackathonRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Hackathon del team non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc1", ID_LEADER, ID_HACKATHON_1);
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of(team));
            when(hackathonRepository.findById(ID_HACKATHON_1)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getHackathonsByUserId(ID_UTENTE));
        }
    }

    // =========================================================================
    // getHackathonDetails()
    // =========================================================================

    @Nested
    @DisplayName("getHackathonDetails()")
    class GetHackathonDetails {

        @Test
        @DisplayName("Membro del team richiede dettagli → restituisce HackathonResponseDTO completo")
        void membroRichiede_restituisceDettagli() {
            // Given
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_LEADER, ID_HACKATHON_1);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM_1, "Leader", "Rossi", "leader@test.it"),
                    new MembroTeam(ID_UTENTE, ID_TEAM_1, "Mario",  "Verdi", "mario@test.it")
            );

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);
            when(hackathonRepository.findById(ID_HACKATHON_1))
                    .thenReturn(Optional.of(hackathon(ID_HACKATHON_1, "HackAlpha", new StatoInIscrizione())));

            // When
            HackathonResponseDTO result = service.getHackathonDetails(ID_HACKATHON_1, ID_TEAM_1, ID_UTENTE);

            // Then
            assertNotNull(result);
            assertEquals(ID_HACKATHON_1, result.id());
            assertEquals("HackAlpha", result.nome());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, result.stato());
        }

        @Test
        @DisplayName("Hackathon CONCLUSO visibile per utente iscritto → restituisce DTO correttamente")
        void hackathonConcluso_visibilePerIscritto() {
            // Given
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_LEADER, ID_HACKATHON_1);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_UTENTE, ID_TEAM_1, "Mario", "Verdi", "mario@test.it"));

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);
            when(hackathonRepository.findById(ID_HACKATHON_1))
                    .thenReturn(Optional.of(hackathon(ID_HACKATHON_1, "HackConcluso", new StatoConcluso())));

            // When
            HackathonResponseDTO result = service.getHackathonDetails(ID_HACKATHON_1, ID_TEAM_1, ID_UTENTE);

            // Then
            assertEquals(StatoHackathon.CONCLUSO, result.stato());
        }

        @Test
        @DisplayName("Utente non membro del team → lancia UnauthorizedActionException")
        void nonMembro_lancia() {
            // Given: team con solo il leader, ID_UTENTE non è membro
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_LEADER, ID_HACKATHON_1);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM_1, "Leader", "Rossi", "leader@test.it"));

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);

            // When / Then
            assertThrows(UnauthorizedActionException.class,
                    () -> service.getHackathonDetails(ID_HACKATHON_1, ID_TEAM_1, ID_UTENTE));
            verify(hackathonRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.getHackathonDetails(ID_HACKATHON_1, ID_TEAM_1, ID_UTENTE));
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_LEADER, ID_HACKATHON_1);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_UTENTE, ID_TEAM_1, "Mario", "Verdi", "mario@test.it"));

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);
            when(hackathonRepository.findById(ID_HACKATHON_1)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getHackathonDetails(ID_HACKATHON_1, ID_TEAM_1, ID_UTENTE));
        }

        @Test
        @DisplayName("findById chiamato con findByIdAndDisponibile mai invocato (mostra anche CONCLUSO)")
        void dettagliIscritto_usaFindByIdNonDisponibile() {
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_LEADER, ID_HACKATHON_1);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_UTENTE, ID_TEAM_1, "Mario", "Verdi", "mario@test.it"));

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);
            when(hackathonRepository.findById(ID_HACKATHON_1))
                    .thenReturn(Optional.of(hackathon(ID_HACKATHON_1, "H", new StatoInIscrizione())));

            service.getHackathonDetails(ID_HACKATHON_1, ID_TEAM_1, ID_UTENTE);

            // Verifica che sia usato findById (non findByIdAndDisponibile)
            verify(hackathonRepository, times(1)).findById(ID_HACKATHON_1);
            verify(hackathonRepository, never()).findByIdAndDisponibile(any());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Hackathon hackathon(UUID id, String nome, hackhub.model.state.HackathonState stato) {
        return new Hackathon(
                id, nome,
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5),  BASE.plusDays(15),
                1000.0, null, 4, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>(),
                stato
        );
    }
}
