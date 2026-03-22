package hackhub.service;

import hackhub.dto.DettagliPartecipazioneDTO;
import hackhub.dto.GestioneTeamResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoConcluso;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoInCorso;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test unitari per i nuovi metodi di TeamService relativi al caso d'uso
 * "Gestire iscrizione al team" (abbandono del team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamService — Gestire iscrizione al team (abbandono)")
class TeamServiceAbbandonoTest {

    @Mock private HackathonRepository hackathonRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvitoRepository invitoRepository;
    @Mock private NotificationsService notificationsService;

    private TeamService service;

    private static final UUID ID_TEAM      = UUID.randomUUID();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_LEADER    = UUID.randomUUID();
    private static final UUID ID_MEMBRO    = UUID.randomUUID();
    private static final UUID ID_MEMBRO_2  = UUID.randomUUID();
    private static final LocalDateTime ORA = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new TeamService(
                hackathonRepository, teamRepository, userRepository,
                invitoRepository, notificationsService);
    }

    // =========================================================================
    // abbandonaTeam() — membro ordinario
    // =========================================================================

    @Nested
    @DisplayName("abbandonaTeam() — membro ordinario")
    class MembroOrdinario {

        @Test
        @DisplayName("Hackathon IN_ISCRIZIONE, membro non-leader → rimuove e notifica leader")
        void membroOrdinario_inIscrizione_rimuoveENotifica() {
            // Given
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    membro(ID_LEADER), membro(ID_MEMBRO)));

            // When
            GestioneTeamResponseDTO response = service.abbandonaTeam(ID_TEAM, ID_MEMBRO);

            // Then
            assertNotNull(response);
            assertEquals(ID_MEMBRO, response.idMembro());
            verify(teamRepository).removeMembro(ID_TEAM, ID_MEMBRO);
            verify(teamRepository, never()).updateLeader(any(), any());
            verify(notificationsService).notificaAbbandono(eq(ID_LEADER), eq(ID_MEMBRO), anyString());
        }

        @Test
        @DisplayName("Hackathon non IN_ISCRIZIONE → lancia InvalidHackathonStateException")
        void hackathonInCorso_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInCorso())));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.abbandonaTeam(ID_TEAM, ID_MEMBRO));
            verify(teamRepository, never()).removeMembro(any(), any());
        }

        @Test
        @DisplayName("Hackathon CONCLUSO → lancia InvalidHackathonStateException")
        void hackathonConcluso_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoConcluso())));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.abbandonaTeam(ID_TEAM, ID_MEMBRO));
        }

        @Test
        @DisplayName("Utente non è membro del team → lancia ValidationException")
        void utenteNonMembro_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_LEADER)));

            UUID esterno = UUID.randomUUID();
            assertThrows(ValidationException.class,
                    () -> service.abbandonaTeam(ID_TEAM, esterno));
            verify(teamRepository, never()).removeMembro(any(), any());
        }
    }

    // =========================================================================
    // abbandonaTeam() — leader
    // =========================================================================

    @Nested
    @DisplayName("abbandonaTeam() — leader del team")
    class Leader {

        @Test
        @DisplayName("Leader abbandona con altri membri → elegge nuovo leader casuale e notifica")
        void leaderAbbandona_altriMembri_eleggeLeggeNuovoLeader() {
            // Given
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    membro(ID_LEADER), membro(ID_MEMBRO), membro(ID_MEMBRO_2)));

            // When
            GestioneTeamResponseDTO response = service.abbandonaTeam(ID_TEAM, ID_LEADER);

            // Then
            assertNotNull(response);
            assertEquals(ID_LEADER, response.idMembro());
            verify(teamRepository).updateLeader(eq(ID_TEAM), any(UUID.class));
            verify(teamRepository).removeMembro(ID_TEAM, ID_LEADER);
            verify(notificationsService).notificaCambioLeadership(any(UUID.class), anyList(), anyString());
        }

        @Test
        @DisplayName("Leader è l'unico membro → lancia IllegalStateException")
        void leaderUnicoMembro_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_LEADER)));

            assertThrows(IllegalStateException.class,
                    () -> service.abbandonaTeam(ID_TEAM, ID_LEADER));
            verify(teamRepository, never()).removeMembro(any(), any());
        }
    }

    // =========================================================================
    // abbandonaTeam() — not-found
    // =========================================================================

    @Nested
    @DisplayName("abbandonaTeam() — entità non trovate")
    class NotFound {

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.abbandonaTeam(ID_TEAM, ID_MEMBRO));
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.abbandonaTeam(ID_TEAM, ID_MEMBRO));
        }
    }

    // =========================================================================
    // ottieniDettagliTeamPerMembro()
    // =========================================================================

    @Nested
    @DisplayName("ottieniDettagliTeamPerMembro()")
    class OttieniDettagli {

        @Test
        @DisplayName("Membro trovato, hackathon IN_ISCRIZIONE → restituisce dettagli con puoAbbandonare=true")
        void membroTrovato_inIscrizione_restituisceDettagliConAbbandono() {
            when(teamRepository.findByMembro(ID_MEMBRO)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_LEADER), membro(ID_MEMBRO)));

            DettagliPartecipazioneDTO dto = service.ottieniDettagliTeamPerMembro(ID_MEMBRO);

            assertNotNull(dto);
            assertEquals(ID_TEAM, dto.idTeam());
            assertEquals(ID_HACKATHON, dto.idHackathon());
            assertEquals(StatoHackathon.IN_ISCRIZIONE, dto.statoHackathon());
            assertTrue(dto.puoAbbandonare());
            assertFalse(dto.isLeader());
            assertEquals(2, dto.membri().size());
        }

        @Test
        @DisplayName("Membro è il leader → isLeader=true")
        void membroELeader_isLeaderTrue() {
            when(teamRepository.findByMembro(ID_LEADER)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_LEADER)));

            DettagliPartecipazioneDTO dto = service.ottieniDettagliTeamPerMembro(ID_LEADER);

            assertTrue(dto.isLeader());
        }

        @Test
        @DisplayName("Hackathon IN_CORSO → puoAbbandonare=false")
        void hackathonInCorso_puoAbbandonareFalse() {
            when(teamRepository.findByMembro(ID_MEMBRO)).thenReturn(Optional.of(teamConLeader(ID_LEADER)));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInCorso())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_MEMBRO)));

            DettagliPartecipazioneDTO dto = service.ottieniDettagliTeamPerMembro(ID_MEMBRO);

            assertFalse(dto.puoAbbandonare());
        }

        @Test
        @DisplayName("Membro non in nessun team → lancia TeamNotFoundException")
        void membroNonInTeam_lancia() {
            when(teamRepository.findByMembro(ID_MEMBRO)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.ottieniDettagliTeamPerMembro(ID_MEMBRO));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Team teamConLeader(UUID idLeader) {
        return new Team(ID_TEAM, "Team Beta", "Descrizione", idLeader, ID_HACKATHON);
    }

    private Hackathon hackathon(hackhub.model.state.HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "HackTest",
                ORA.plusDays(10), ORA.plusDays(20),
                ORA.plusDays(5), ORA.plusDays(15),
                500.0, null, 5, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(),
                stato
        );
    }

    private MembroTeam membro(UUID id) {
        return new MembroTeam(id, ID_TEAM, "Nome", "Cognome", "email@test.com");
    }
}
