package hackhub.service;

import hackhub.dto.CallInviteListItemDTO;
import hackhub.dto.CallInviteResponseDTO;
import hackhub.exception.CallNotFoundException;
import hackhub.exception.InvalidCallStateException;
import hackhub.exception.SupportRequestNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.model.StatoCall;
import hackhub.model.entity.Call;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.repository.CallRepository;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.RichiestaSupportoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;
import hackhub.service.observer.RispostaCallObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per CallService (caso d'uso: Gestire invito a call da parte di un mentore).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CallService — Gestire invito a call da parte di un mentore")
class GestioneInvitoCallServiceTest {

    @Mock
    private RichiestaSupportoRepository richiestaSupportoRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private HackathonRepository hackathonRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CallRepository callRepository;
    @Mock
    private CalendarService calendarService;
    @Mock
    private MentoreRepository mentoreRepository;
    @Mock
    private RispostaCallObserver rispostaCallObserver;

    private CallService service;

    private static final UUID ID_CALL = UUID.randomUUID();
    private static final UUID ID_RICHIESTA = UUID.randomUUID();
    private static final UUID ID_TEAM = UUID.randomUUID();
    private static final UUID ID_LEADER = UUID.randomUUID();
    private static final UUID ID_ALTRO = UUID.randomUUID();
    private static final UUID ID_MENTORE = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new CallService(
                richiestaSupportoRepository,
                teamRepository,
                hackathonRepository,
                userRepository,
                callRepository,
                calendarService,
                mentoreRepository,
                rispostaCallObserver
        );
    }

    // =========================================================================
    // getCallInvites()
    // =========================================================================

    @Nested
    @DisplayName("getCallInvites()")
    class GetCallInvites {

        @Test
        @DisplayName("Leader con due call pendenti → restituisce lista con due CallInviteListItemDTO")
        void dueCallPendenti_restituisceListaCorretta() {
            // Given
            Team team = team(ID_TEAM, ID_LEADER);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));

            Call call1 = call(UUID.randomUUID(), ID_RICHIESTA, StatoCall.PENDENTE);
            Call call2 = call(UUID.randomUUID(), UUID.randomUUID(), StatoCall.PENDENTE);
            when(callRepository.findByTeamId(ID_TEAM)).thenReturn(List.of(call1, call2));

            RichiestaSupporto richiesta1 = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            RichiestaSupporto richiesta2 = richiesta(UUID.randomUUID(), ID_TEAM, ID_MENTORE);
            when(richiestaSupportoRepository.findById(call1.getIdRichiestaSupporto()))
                    .thenReturn(Optional.of(richiesta1));
            when(richiestaSupportoRepository.findById(call2.getIdRichiestaSupporto()))
                    .thenReturn(Optional.of(richiesta2));

            Mentore mentore = new Mentore(ID_MENTORE, "Carlo", "Bianchi", "carlo@test.it", true);
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.of(mentore));

            // When
            List<CallInviteListItemDTO> lista = service.getCallInvites(ID_TEAM, ID_LEADER);

            // Then
            assertEquals(2, lista.size());
            assertEquals("Carlo", lista.get(0).nomeMentore());
            assertEquals("Bianchi", lista.get(0).cognomeMentore());
            assertEquals(StatoCall.PENDENTE, lista.get(0).stato());
        }

        @Test
        @DisplayName("Richiedente non è leader del team → lancia UnauthorizedActionException")
        void nonLeader_lancia() {
            Team team = team(ID_TEAM, ID_LEADER);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.getCallInvites(ID_TEAM, ID_ALTRO));

            verify(callRepository, never()).findByTeamId(any());
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.getCallInvites(ID_TEAM, ID_LEADER));
        }

        @Test
        @DisplayName("Nessuna call per il team → restituisce lista vuota")
        void nessunaCall_restituisceListaVuota() {
            Team team = team(ID_TEAM, ID_LEADER);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));
            when(callRepository.findByTeamId(ID_TEAM)).thenReturn(List.of());

            List<CallInviteListItemDTO> lista = service.getCallInvites(ID_TEAM, ID_LEADER);

            assertTrue(lista.isEmpty());
        }
    }

    // =========================================================================
    // processInviteResponse() — accettazione
    // =========================================================================

    @Nested
    @DisplayName("processInviteResponse() — accettazione")
    class ProcessInviteResponseAccettazione {

        @Test
        @DisplayName("Leader accetta call pendente → aggiorna stato, notifica mentore, restituisce DTO ACCETTATA")
        void accettaCallPendente_flussoCo() {
            // Given
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.PENDENTE);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            Team team = team(ID_TEAM, ID_LEADER);
            Mentore mentore = new Mentore(ID_MENTORE, "Carlo", "Bianchi", "carlo@test.it", true);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.of(mentore));

            // When
            CallInviteResponseDTO result = service.processInviteResponse(ID_CALL, ID_LEADER, true);

            // Then
            assertNotNull(result);
            assertEquals(ID_CALL, result.idCall());
            assertEquals(ID_TEAM, result.idTeam());
            assertEquals(StatoCall.ACCETTATA, result.nuovoStato());
            verify(callRepository).updateStato(ID_CALL, StatoCall.ACCETTATA);
            verify(rispostaCallObserver).onCallAccettata(ID_CALL, "carlo@test.it", team.getNome());
            verify(rispostaCallObserver, never()).onCallRifiutata(any(), any(), any());
        }
    }

    // =========================================================================
    // processInviteResponse() — rifiuto
    // =========================================================================

    @Nested
    @DisplayName("processInviteResponse() — rifiuto")
    class ProcessInviteResponseRifiuto {

        @Test
        @DisplayName("Leader rifiuta call pendente → aggiorna stato, notifica mentore, restituisce DTO RIFIUTATA")
        void rifiutaCallPendente_flussoCo() {
            // Given
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.PENDENTE);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            Team team = team(ID_TEAM, ID_LEADER);
            Mentore mentore = new Mentore(ID_MENTORE, "Carlo", "Bianchi", "carlo@test.it", true);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.of(mentore));

            // When
            CallInviteResponseDTO result = service.processInviteResponse(ID_CALL, ID_LEADER, false);

            // Then
            assertEquals(StatoCall.RIFIUTATA, result.nuovoStato());
            verify(callRepository).updateStato(ID_CALL, StatoCall.RIFIUTATA);
            verify(rispostaCallObserver).onCallRifiutata(ID_CALL, "carlo@test.it", team.getNome());
            verify(rispostaCallObserver, never()).onCallAccettata(any(), any(), any());
        }
    }

    // =========================================================================
    // processInviteResponse() — guardie (eccezioni)
    // =========================================================================

    @Nested
    @DisplayName("processInviteResponse() — guardie")
    class ProcessInviteResponseGuardie {

        @Test
        @DisplayName("Call non trovata → lancia CallNotFoundException")
        void callNonTrovata_lancia() {
            when(callRepository.findById(ID_CALL)).thenReturn(Optional.empty());

            assertThrows(CallNotFoundException.class,
                    () -> service.processInviteResponse(ID_CALL, ID_LEADER, true));
        }

        @Test
        @DisplayName("Richiesta di supporto non trovata → lancia SupportRequestNotFoundException")
        void richiestaNonTrovata_lancia() {
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.PENDENTE);
            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.empty());

            assertThrows(SupportRequestNotFoundException.class,
                    () -> service.processInviteResponse(ID_CALL, ID_LEADER, true));
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.PENDENTE);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.processInviteResponse(ID_CALL, ID_LEADER, true));
        }

        @Test
        @DisplayName("Richiedente non è il leader del team → lancia UnauthorizedActionException")
        void nonLeader_lancia() {
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.PENDENTE);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            Team team = team(ID_TEAM, ID_LEADER);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.processInviteResponse(ID_CALL, ID_ALTRO, true));

            verify(callRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("Call già accettata → lancia InvalidCallStateException")
        void callGiaAccettata_lancia() {
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.ACCETTATA);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            Team team = team(ID_TEAM, ID_LEADER);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));

            assertThrows(InvalidCallStateException.class,
                    () -> service.processInviteResponse(ID_CALL, ID_LEADER, true));

            verify(callRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("Call già rifiutata → lancia InvalidCallStateException")
        void callGiaRifiutata_lancia() {
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.RIFIUTATA);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            Team team = team(ID_TEAM, ID_LEADER);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));

            assertThrows(InvalidCallStateException.class,
                    () -> service.processInviteResponse(ID_CALL, ID_LEADER, false));

            verify(callRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("updateStato chiamato con id e stato corretti al momento dell'accettazione")
        void updateStatoCorretto_alAccettazione() {
            Call call = call(ID_CALL, ID_RICHIESTA, StatoCall.PENDENTE);
            RichiestaSupporto richiesta = richiesta(ID_RICHIESTA, ID_TEAM, ID_MENTORE);
            Team team = team(ID_TEAM, ID_LEADER);

            when(callRepository.findById(ID_CALL)).thenReturn(Optional.of(call));
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team));
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.empty());

            service.processInviteResponse(ID_CALL, ID_LEADER, true);

            verify(callRepository, times(1)).updateStato(ID_CALL, StatoCall.ACCETTATA);
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Team team(UUID idTeam, UUID idLeader) {
        return new Team(idTeam, "Team Test", "Descrizione", idLeader, UUID.randomUUID());
    }

    private Call call(UUID idCall, UUID idRichiesta, StatoCall stato) {
        Call c = new Call(
                idCall,
                idRichiesta,
                LocalDate.now().plusDays(3),
                LocalTime.of(15, 0),
                "https://meet.example.com/room",
                "Descrizione della call",
                LocalDateTime.now(),
                stato
        );
        return c;
    }

    private RichiestaSupporto richiesta(UUID idRichiesta, UUID idTeam, UUID idMentore) {
        return new RichiestaSupporto(
                idRichiesta,
                idTeam,
                UUID.randomUUID(),
                idMentore,
                "Motivo della richiesta",
                LocalDateTime.now()
        );
    }
}
