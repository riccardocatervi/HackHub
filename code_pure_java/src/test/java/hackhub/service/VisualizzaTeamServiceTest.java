package hackhub.service;

import hackhub.dto.TeamDetailsDTO;
import hackhub.dto.TeamSummaryDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.NoTeamsFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoInIscrizione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;
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
 * Test unitari per TeamService (caso d'uso: Visualizzare team di appartenenza).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService — Visualizzare team di appartenenza")
class VisualizzaTeamServiceTest {

    @Mock private HackathonRepository hackathonRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvitoRepository invitoRepository;
    @Mock private NotificationsService notificationsService;

    private TeamService service;

    private static final UUID ID_UTENTE    = UUID.randomUUID();
    private static final UUID ID_LEADER    = UUID.randomUUID();
    private static final UUID ID_TEAM_1    = UUID.randomUUID();
    private static final UUID ID_TEAM_2    = UUID.randomUUID();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final LocalDateTime BASE = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new TeamService(
                hackathonRepository, teamRepository, userRepository,
                invitoRepository, notificationsService);
    }

    // =========================================================================
    // getTeamsByUserId()
    // =========================================================================

    @Nested
    @DisplayName("getTeamsByUserId()")
    class GetTeamsByUserId {

        @Test
        @DisplayName("Utente membro di due team → restituisce lista con due TeamSummaryDTO")
        void dueTeam_restituisceListaCorretta() {
            // Given
            Team team1 = new Team(ID_TEAM_1, "Team Alpha", "Desc1", ID_UTENTE, ID_HACKATHON);
            Team team2 = new Team(ID_TEAM_2, "Team Beta",  "Desc2", ID_LEADER, ID_HACKATHON);
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of(team1, team2));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));

            // When
            List<TeamSummaryDTO> lista = service.getTeamsByUserId(ID_UTENTE);

            // Then
            assertEquals(2, lista.size());

            TeamSummaryDTO s1 = lista.get(0);
            assertEquals(ID_TEAM_1, s1.idTeam());
            assertEquals("Team Alpha", s1.nomeTeam());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, s1.statoHackathon());
            assertTrue(s1.isLeader());  // ID_UTENTE == idLeader di team1

            TeamSummaryDTO s2 = lista.get(1);
            assertEquals(ID_TEAM_2, s2.idTeam());
            assertFalse(s2.isLeader()); // ID_UTENTE != idLeader di team2
        }

        @Test
        @DisplayName("Utente non membro di alcun team → lancia NoTeamsFoundException")
        void nessunTeam_lancia() {
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of());

            assertThrows(NoTeamsFoundException.class,
                    () -> service.getTeamsByUserId(ID_UTENTE));
            verify(hackathonRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Hackathon associato non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            Team team1 = new Team(ID_TEAM_1, "Team Alpha", "Desc1", ID_UTENTE, ID_HACKATHON);
            when(teamRepository.findAllByMembro(ID_UTENTE)).thenReturn(List.of(team1));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getTeamsByUserId(ID_UTENTE));
        }
    }

    // =========================================================================
    // getTeamDetails()
    // =========================================================================

    @Nested
    @DisplayName("getTeamDetails()")
    class GetTeamDetails {

        @Test
        @DisplayName("Flusso felice: membro richiede dettagli → restituisce TeamDetailsDTO completo")
        void membroRichiede_restituisceDettagli() {
            // Given
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Descrizione dettagliata", ID_LEADER, ID_HACKATHON);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM_1, "Leader", "Cognome", "leader@test.it"),
                    new MembroTeam(ID_UTENTE, ID_TEAM_1, "Mario",  "Rossi",   "mario@test.it")
            );

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));

            // When
            TeamDetailsDTO details = service.getTeamDetails(ID_TEAM_1, ID_UTENTE);

            // Then
            assertNotNull(details);
            assertEquals(ID_TEAM_1, details.idTeam());
            assertEquals("Team Alpha", details.nomeTeam());
            assertEquals("Descrizione dettagliata", details.descrizioneTeam());
            assertEquals(ID_LEADER, details.idLeader());
            assertEquals(ID_HACKATHON, details.idHackathon());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, details.statoHackathon());
            assertEquals(2, details.membri().size());
        }

        @Test
        @DisplayName("Utente non è membro del team → lancia UnauthorizedActionException")
        void nonMembro_lancia() {
            // Given: team con solo il leader, ID_UTENTE non è membro
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_LEADER, ID_HACKATHON);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM_1, "Leader", "Cognome", "leader@test.it"));

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);

            // When / Then
            assertThrows(UnauthorizedActionException.class,
                    () -> service.getTeamDetails(ID_TEAM_1, ID_UTENTE));
            verify(hackathonRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.getTeamDetails(ID_TEAM_1, ID_UTENTE));
        }

        @Test
        @DisplayName("Leader richiede dettagli del proprio team → restituisce DTO con idLeader corretto")
        void leaderRichiede_restituisceDettagliConLeaderCorretto() {
            // Given
            Team team = new Team(ID_TEAM_1, "Team Alpha", "Desc", ID_UTENTE, ID_HACKATHON);
            List<MembroTeam> membri = List.of(
                    new MembroTeam(ID_UTENTE, ID_TEAM_1, "Mario", "Rossi", "mario@test.it"));

            when(teamRepository.findById(ID_TEAM_1)).thenReturn(Optional.of(team));
            when(teamRepository.findMembri(ID_TEAM_1)).thenReturn(membri);
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));

            // When
            TeamDetailsDTO details = service.getTeamDetails(ID_TEAM_1, ID_UTENTE);

            // Then
            assertEquals(ID_UTENTE, details.idLeader());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Hackathon hackathon() {
        return new Hackathon(
                ID_HACKATHON, "HackTest",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                1000.0, null, 4, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>(),
                new StatoInIscrizione()
        );
    }
}
