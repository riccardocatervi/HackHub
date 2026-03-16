package hackhub.service;

import hackhub.dto.UnsubscriptionSuccessDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoConcluso;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInIscrizione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Test unitari per TeamRegistrationService
 * (caso d'uso: Gestire iscrizione del team all'Hackathon — disiscrizione).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("TeamRegistrationService — Gestire iscrizione del team all'Hackathon")
class TeamRegistrationServiceTest {

    @Mock private TeamRepository teamRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private InvitoRepository invitoRepository;
    @Mock private NotificationsService notificationsService;

    private TeamRegistrationService service;

    private static final UUID ID_TEAM      = UUID.randomUUID();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_LEADER    = UUID.randomUUID();
    private static final UUID ID_MEMBRO    = UUID.randomUUID();
    private static final LocalDateTime ORA = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new TeamRegistrationService(
                teamRepository, hackathonRepository, invitoRepository, notificationsService);
    }

    // =========================================================================
    // processTeamUnsubscription() — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("processTeamUnsubscription() — percorso felice")
    class FlussoFelice {

        @Test
        @DisplayName("Leader disiscrisce team IN_ISCRIZIONE → elimina inviti, elimina team, notifica membri")
        void leader_inIscrizione_eliminaENotifica() {
            // Given
            Team team = team();
            Hackathon hackathon = hackathon(new StatoInIscrizione());
            List<MembroTeam> membri = List.of(membro(ID_LEADER), membro(ID_MEMBRO));

            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(membri);

            // When
            UnsubscriptionSuccessDTO response = service.processTeamUnsubscription(ID_TEAM, ID_LEADER);

            // Then
            assertNotNull(response);
            assertEquals(ID_TEAM, response.idTeam());
            assertEquals(ID_HACKATHON, response.idHackathon());
            assertTrue(response.messaggio().contains("disiscritto"));

            verify(invitoRepository).deleteByTeam(ID_TEAM);
            verify(teamRepository).delete(team);
            verify(notificationsService).notifyTeamUnsubscription(eq(membri), eq(team), eq(hackathon));
        }

        @Test
        @DisplayName("DTO di risposta contiene nome hackathon e team corretti")
        void dtoContieneNomiCorretti() {
            // Given
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_LEADER)));

            // When
            UnsubscriptionSuccessDTO response = service.processTeamUnsubscription(ID_TEAM, ID_LEADER);

            // Then
            assertEquals("HackTest", response.nomeHackathon());
            assertEquals("Team Gamma", response.nomeTeam());
        }
    }

    // =========================================================================
    // processTeamUnsubscription() — casi di errore
    // =========================================================================

    @Nested
    @DisplayName("processTeamUnsubscription() — casi di errore")
    class CasiDiErrore {

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.processTeamUnsubscription(ID_TEAM, ID_LEADER));
            verify(teamRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.processTeamUnsubscription(ID_TEAM, ID_LEADER));
            verify(teamRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Richiedente non è il leader → lancia UnauthorizedActionException")
        void nonLeader_lancia() {
            UUID nonLeader = UUID.randomUUID();
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.processTeamUnsubscription(ID_TEAM, nonLeader));
            verify(teamRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Hackathon IN_CORSO → lancia InvalidHackathonStateException")
        void hackathonInCorso_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInCorso())));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.processTeamUnsubscription(ID_TEAM, ID_LEADER));
            verify(teamRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Hackathon CONCLUSO → lancia InvalidHackathonStateException")
        void hackathonConcluso_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoConcluso())));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.processTeamUnsubscription(ID_TEAM, ID_LEADER));
        }

        @Test
        @DisplayName("Errore durante delete → PersistenceException propagata, notifica non inviata")
        void erroreDurante_delete_notificaNonInviata() {
            // Given
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon(new StatoInIscrizione())));
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(membro(ID_LEADER)));
            doThrow(new hackhub.exception.PersistenceException("DB giù", new RuntimeException()))
                    .when(teamRepository).delete(any());

            // When / Then
            assertThrows(hackhub.exception.PersistenceException.class,
                    () -> service.processTeamUnsubscription(ID_TEAM, ID_LEADER));
            verify(notificationsService, never()).notifyTeamUnsubscription(anyList(), any(), any());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Team team() {
        return new Team(ID_TEAM, "Team Gamma", "Descrizione", ID_LEADER, ID_HACKATHON);
    }

    private Hackathon hackathon(hackhub.model.state.HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "HackTest",
                ORA.plusDays(10), ORA.plusDays(20),
                ORA.plusDays(5), ORA.plusDays(15),
                800.0, null, 5, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(),
                stato
        );
    }

    private MembroTeam membro(UUID id) {
        return new MembroTeam(id, ID_TEAM, "Nome", "Cognome", "email@test.com");
    }
}
