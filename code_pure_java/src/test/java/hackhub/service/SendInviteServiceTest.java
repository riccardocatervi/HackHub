package hackhub.service;

import hackhub.dto.InviteCreatedDTO;
import hackhub.exception.TeamCapacityExceededException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.UserNotFoundException;
import hackhub.exception.UserNotEligibleException;
import hackhub.model.StatoInvito;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;
import hackhub.model.entity.Utente;
import hackhub.model.state.StatoInIscrizione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;
import hackhub.service.observer.NuovoInvitoObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitari per InvitationService (caso d'uso: Invitare utente a unirsi al team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationService — Invitare utente a unirsi al team")
class SendInviteServiceTest {

    @Mock private InvitoRepository invitoRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private UserRepository userRepository;
    @Mock private NuovoInvitoObserver nuovoInvitoObserver;

    private InvitationService service;

    private static final UUID ID_TEAM        = UUID.randomUUID();
    private static final UUID ID_LEADER      = UUID.randomUUID();
    private static final UUID ID_TARGET      = UUID.randomUUID();
    private static final UUID ID_HACKATHON   = UUID.randomUUID();
    private static final UUID ID_MEMBRO_A    = UUID.randomUUID();
    private static final int  MAX_TEAM_SIZE  = 4;
    private static final LocalDateTime BASE  = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new InvitationService(
                invitoRepository, teamRepository, hackathonRepository, userRepository);
        service.addNuovoInvitoObserver(nuovoInvitoObserver);
    }

    // =========================================================================
    // createInvite() — flusso felice
    // =========================================================================

    @Nested
    @DisplayName("createInvite() — flusso felice")
    class FlussoCo {

        @Test
        @DisplayName("Leader invita utente disponibile → invito salvato, observer notificato, DTO restituito")
        void invitoValido_salvaENotifica() {
            // Given
            stubTeamEHackathon();
            // 1 membro attuale (il leader) + 0 pendenti → capienza ok
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM, "Mario", "Rossi", "mario@test.it")));
            when(invitoRepository.countPendingByTeam(ID_TEAM)).thenReturn(0);
            when(userRepository.findById(ID_TARGET)).thenReturn(Optional.of(utenteTarget()));
            when(invitoRepository.existsByUtenteAndTeamAndStato(
                    ID_TARGET, ID_TEAM, StatoInvito.IN_ATTESA)).thenReturn(false);

            // When
            InviteCreatedDTO result = service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER);

            // Then
            assertNotNull(result);
            assertEquals(ID_TEAM, result.idTeam());
            assertEquals(ID_TARGET, result.idUtenteInvitato());
            assertEquals("target@test.it", result.emailUtenteInvitato());
            verify(invitoRepository).save(any());
            verify(nuovoInvitoObserver).onNuovoInvito(any(), eq("target@test.it"), eq("Team Alpha"));
        }

        @Test
        @DisplayName("Observer riceve i parametri corretti al momento della notifica")
        void observer_riceveParametriCorretti() {
            // Given
            stubTeamEHackathon();
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM, "Mario", "Rossi", "mario@test.it")));
            when(invitoRepository.countPendingByTeam(ID_TEAM)).thenReturn(0);
            when(userRepository.findById(ID_TARGET)).thenReturn(Optional.of(utenteTarget()));
            when(invitoRepository.existsByUtenteAndTeamAndStato(
                    ID_TARGET, ID_TEAM, StatoInvito.IN_ATTESA)).thenReturn(false);

            // When
            service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER);

            // Then
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> nomeCaptor  = ArgumentCaptor.forClass(String.class);
            verify(nuovoInvitoObserver).onNuovoInvito(any(), emailCaptor.capture(), nomeCaptor.capture());
            assertEquals("target@test.it", emailCaptor.getValue());
            assertEquals("Team Alpha", nomeCaptor.getValue());
        }
    }

    // =========================================================================
    // createInvite() — casi di errore
    // =========================================================================

    @Nested
    @DisplayName("createInvite() — casi di errore")
    class CasiDiErrore {

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER));
            verify(invitoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Richiedente non è il leader → lancia UnauthorizedActionException")
        void nonLeader_lancia() {
            UUID altroUtente = UUID.randomUUID();
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.createInvite(ID_TEAM, ID_TARGET, altroUtente));
            verify(invitoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Capienza massima raggiunta → lancia TeamCapacityExceededException")
        void capienzaRaggiunta_lancia() {
            // Given: MAX_TEAM_SIZE = 4, 2 membri + 2 pendenti = 4 ≥ max
            stubTeamEHackathon();
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM, "Mario", "Rossi", "mario@test.it"),
                    new MembroTeam(ID_MEMBRO_A, ID_TEAM, "Luigi", "Bianchi", "luigi@test.it")));
            when(invitoRepository.countPendingByTeam(ID_TEAM)).thenReturn(2);

            assertThrows(TeamCapacityExceededException.class,
                    () -> service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER));
            verify(invitoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Utente target non trovato → lancia UserNotFoundException")
        void utenteTargetNonTrovato_lancia() {
            // Given: capienza libera (1 membro + 0 pendenti < 4)
            stubTeamEHackathon();
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM, "Mario", "Rossi", "mario@test.it")));
            when(invitoRepository.countPendingByTeam(ID_TEAM)).thenReturn(0);
            when(userRepository.findById(ID_TARGET)).thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class,
                    () -> service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER));
            verify(invitoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Utente già membro effettivo → lancia UserNotEligibleException")
        void utenteGiaMembro_lancia() {
            // Given: l'utente target è già nella lista membri
            stubTeamEHackathon();
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM, "Mario", "Rossi", "mario@test.it"),
                    new MembroTeam(ID_TARGET, ID_TEAM, "Target", "User", "target@test.it")));
            when(invitoRepository.countPendingByTeam(ID_TEAM)).thenReturn(0);
            when(userRepository.findById(ID_TARGET)).thenReturn(Optional.of(utenteTarget()));

            assertThrows(UserNotEligibleException.class,
                    () -> service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER));
            verify(invitoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Utente ha già un invito pendente → lancia UserNotEligibleException")
        void invitoPendenteEsistente_lancia() {
            // Given
            stubTeamEHackathon();
            when(teamRepository.findMembri(ID_TEAM)).thenReturn(List.of(
                    new MembroTeam(ID_LEADER, ID_TEAM, "Mario", "Rossi", "mario@test.it")));
            when(invitoRepository.countPendingByTeam(ID_TEAM)).thenReturn(0);
            when(userRepository.findById(ID_TARGET)).thenReturn(Optional.of(utenteTarget()));
            when(invitoRepository.existsByUtenteAndTeamAndStato(
                    ID_TARGET, ID_TEAM, StatoInvito.IN_ATTESA)).thenReturn(true);

            assertThrows(UserNotEligibleException.class,
                    () -> service.createInvite(ID_TEAM, ID_TARGET, ID_LEADER));
            verify(invitoRepository, never()).save(any());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Team team() {
        return new Team(ID_TEAM, "Team Alpha", "Descrizione", ID_LEADER, ID_HACKATHON);
    }

    private Hackathon hackathon() {
        return new Hackathon(
                ID_HACKATHON, "HackTest",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                1000.0, null, MAX_TEAM_SIZE, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                new ArrayList<>(),
                new StatoInIscrizione()
        );
    }

    private Utente utenteTarget() {
        return new Utente(ID_TARGET, "Target", "User", "target@test.it", "hash", null, null);
    }

    private void stubTeamEHackathon() {
        when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
        when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));
    }
}
