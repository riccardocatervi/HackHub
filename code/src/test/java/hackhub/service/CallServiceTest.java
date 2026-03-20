package hackhub.service;

import hackhub.dto.CallCreateDTO;
import hackhub.dto.CallFormDTO;
import hackhub.dto.CallResponseDTO;
import hackhub.dto.DatiEventoEsterno;
import hackhub.exception.CalendarConnectionException;
import hackhub.exception.PersistenceException;
import hackhub.exception.SupportRequestNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.ValidationException;
import hackhub.model.LivelloUrgenza;
import hackhub.model.StatoRichiesta;
import hackhub.model.entity.Call;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.model.entity.Utente;
import hackhub.model.state.StatoInCorso;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test unitari per CallService
 * (caso d'uso: Pianificare call con un team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CallService — Pianificare call con un team")
class CallServiceTest {

    @Mock private RichiestaSupportoRepository richiestaSupportoRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private UserRepository userRepository;
    @Mock private CallRepository callRepository;
    @Mock private CalendarService calendarService;
    @Mock private MentoreRepository mentoreRepository;
    @Mock private RispostaCallObserver rispostaCallObserver;

    private CallService service;

    private static final UUID ID_RICHIESTA  = UUID.randomUUID();
    private static final UUID ID_MENTORE    = UUID.randomUUID();
    private static final UUID ID_ALTRO_MENTORE = UUID.randomUUID();
    private static final UUID ID_TEAM       = UUID.randomUUID();
    private static final UUID ID_LEADER     = UUID.randomUUID();
    private static final UUID ID_HACKATHON  = UUID.randomUUID();
    private static final LocalDateTime ORA  = LocalDateTime.now();
    private static final LocalDate DATA_CALL = LocalDate.now().plusDays(3);
    private static final LocalTime ORA_CALL  = LocalTime.of(15, 0);

    @BeforeEach
    void setUp() {
        service = new CallService(
                richiestaSupportoRepository, teamRepository, hackathonRepository,
                userRepository, callRepository, calendarService,
                mentoreRepository, rispostaCallObserver);
    }

    // =========================================================================
    // getFormData()
    // =========================================================================

    @Nested
    @DisplayName("getFormData()")
    class GetFormData {

        @Test
        @DisplayName("Flusso felice: mentore proprietario → restituisce CallFormDTO precompilato")
        void mentoreProprietario_restituisceFormDTO() {
            // Given
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPresa()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));

            // When
            CallFormDTO dto = service.getFormData(ID_RICHIESTA, ID_MENTORE);

            // Then
            assertNotNull(dto);
            assertEquals(ID_RICHIESTA, dto.idRichiesta());
            assertEquals(ID_MENTORE, dto.idMentore());
            assertEquals("Team Alpha", dto.nomeTeam());
            assertEquals("HackTest", dto.nomeHackathon());
        }

        @Test
        @DisplayName("Richiesta non trovata → lancia SupportRequestNotFoundException")
        void richiestaNonTrovata_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.empty());

            assertThrows(SupportRequestNotFoundException.class,
                    () -> service.getFormData(ID_RICHIESTA, ID_MENTORE));
        }

        @Test
        @DisplayName("Mentore non proprietario → lancia UnauthorizedActionException")
        void mentoreNonProprietario_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPresa()));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.getFormData(ID_RICHIESTA, ID_ALTRO_MENTORE));
        }
    }

    // =========================================================================
    // pianificaCall() — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("pianificaCall() — percorso felice")
    class FlussoFelice {

        @Test
        @DisplayName("Dati validi → crea evento Calendar, salva call, restituisce CallResponseDTO")
        void datiValidi_pianificaERestituisce() {
            // Given
            CallCreateDTO dto = callCreateDto();
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPresa()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(userRepository.findById(ID_LEADER))
                    .thenReturn(Optional.of(leader()));
            DatiEventoEsterno datiEvento = new DatiEventoEsterno(
                    "https://meet.hackhub.dev/call/abc123", "evt-001");
            when(calendarService.creaEventoCalendar(eq(dto), anyString()))
                    .thenReturn(datiEvento);

            // When
            CallResponseDTO response = service.pianificaCall(dto);

            // Then
            assertNotNull(response);
            assertEquals(ID_RICHIESTA, response.idRichiesta());
            assertEquals(DATA_CALL, response.dataCall());
            assertEquals(ORA_CALL, response.oraCall());
            assertEquals("https://meet.hackhub.dev/call/abc123", response.linkCall());
            assertTrue(response.messaggio().contains("Team Alpha"));

            verify(calendarService).creaEventoCalendar(eq(dto), eq("leader@test.com"));
            verify(callRepository).save(any(Call.class));
        }

        @Test
        @DisplayName("Il link della call nel DTO corrisponde a quello restituito dal Calendar")
        void linkCallCorrispondeAEventoCalendar() {
            // Given
            String linkAtteso = "https://meet.google.com/xyz-abc-def";
            CallCreateDTO dto = callCreateDto();
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPresa()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(userRepository.findById(ID_LEADER)).thenReturn(Optional.of(leader()));
            when(calendarService.creaEventoCalendar(any(), any()))
                    .thenReturn(new DatiEventoEsterno(linkAtteso, "evt-002"));

            // When
            CallResponseDTO response = service.pianificaCall(dto);

            // Then
            assertEquals(linkAtteso, response.linkCall());
        }
    }

    // =========================================================================
    // pianificaCall() — errori di validazione
    // =========================================================================

    @Nested
    @DisplayName("pianificaCall() — errori di validazione")
    class ErroriValidazione {

        @Test
        @DisplayName("idRichiesta null → lancia ValidationException senza contattare Calendar")
        void idRichiestaNullo_lancia() {
            CallCreateDTO dto = new CallCreateDTO(null, ID_MENTORE, DATA_CALL, ORA_CALL, "Sessione tecnica");

            assertThrows(ValidationException.class, () -> service.pianificaCall(dto));
            verify(calendarService, never()).creaEventoCalendar(any(), any());
            verify(callRepository, never()).save(any());
        }

        @Test
        @DisplayName("dataCall null → lancia ValidationException senza contattare Calendar")
        void dataCallNulla_lancia() {
            CallCreateDTO dto = new CallCreateDTO(ID_RICHIESTA, ID_MENTORE, null, ORA_CALL, "Sessione tecnica");

            assertThrows(ValidationException.class, () -> service.pianificaCall(dto));
            verify(calendarService, never()).creaEventoCalendar(any(), any());
        }

        @Test
        @DisplayName("descrizione assente → lancia ValidationException senza contattare Calendar")
        void descrizioneAssente_lancia() {
            CallCreateDTO dto = new CallCreateDTO(ID_RICHIESTA, ID_MENTORE, DATA_CALL, ORA_CALL, "  ");

            assertThrows(ValidationException.class, () -> service.pianificaCall(dto));
            verify(calendarService, never()).creaEventoCalendar(any(), any());
        }
    }

    // =========================================================================
    // pianificaCall() — errori tecnici
    // =========================================================================

    @Nested
    @DisplayName("pianificaCall() — errori tecnici")
    class ErroriTecnici {

        @Test
        @DisplayName("Calendar non raggiungibile → CalendarConnectionException propagata, call non salvata")
        void calendarNonRaggiungibile_propagaEccezioneESaltaSalvataggio() {
            // Given
            CallCreateDTO dto = callCreateDto();
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPresa()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(userRepository.findById(ID_LEADER)).thenReturn(Optional.of(leader()));
            when(calendarService.creaEventoCalendar(any(), any()))
                    .thenThrow(new CalendarConnectionException("Impossibile raggiungere il sistema Calendar."));

            // When / Then
            assertThrows(CalendarConnectionException.class, () -> service.pianificaCall(dto));
            verify(callRepository, never()).save(any());
        }

        @Test
        @DisplayName("Errore salvataggio DB → PersistenceException propagata")
        void erroreSalvaggioDb_propagaEccezione() {
            // Given
            CallCreateDTO dto = callCreateDto();
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPresa()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(userRepository.findById(ID_LEADER)).thenReturn(Optional.of(leader()));
            when(calendarService.creaEventoCalendar(any(), any()))
                    .thenReturn(new DatiEventoEsterno("https://meet.hackhub.dev/call/test", "evt-003"));
            doThrow(new PersistenceException("DB non disponibile", new RuntimeException()))
                    .when(callRepository).save(any());

            // When / Then
            assertThrows(PersistenceException.class, () -> service.pianificaCall(dto));
        }

        @Test
        @DisplayName("Richiesta non trovata → lancia SupportRequestNotFoundException")
        void richiestaNonTrovata_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.empty());

            assertThrows(SupportRequestNotFoundException.class,
                    () -> service.pianificaCall(callCreateDto()));
            verify(calendarService, never()).creaEventoCalendar(any(), any());
            verify(callRepository, never()).save(any());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private RichiestaSupporto richiestaPresa() {
        return new RichiestaSupporto(
                ID_RICHIESTA, ID_TEAM, ID_HACKATHON, ID_MENTORE,
                "Problema tecnico con la pipeline di build.",
                ORA, StatoRichiesta.PRESA_IN_CARICO, LivelloUrgenza.ALTA, null);
    }

    private Team team() {
        return new Team(ID_TEAM, "Team Alpha", "Descrizione", ID_LEADER, ID_HACKATHON);
    }

    private Hackathon hackathon() {
        return new Hackathon(
                ID_HACKATHON, "HackTest",
                ORA.plusDays(10), ORA.plusDays(20),
                ORA.plusDays(5), ORA.plusDays(15),
                1000.0, null, 4, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(),
                new StatoInCorso()
        );
    }

    private Utente leader() {
        return new Utente(ID_LEADER, "Leader", "Rossi", "leader@test.com",
                "hash", null, null);
    }

    private CallCreateDTO callCreateDto() {
        return new CallCreateDTO(ID_RICHIESTA, ID_MENTORE, DATA_CALL, ORA_CALL, "Sessione tecnica di supporto.");
    }
}
