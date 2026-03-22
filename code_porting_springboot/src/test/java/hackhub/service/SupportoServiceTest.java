package hackhub.service;

import hackhub.dto.SupportoFormDTO;
import hackhub.dto.SupportoRequestDTO;
import hackhub.dto.SupportoResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.NoMentorsAvailableException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoConcluso;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInIscrizione;
import hackhub.model.state.StatoInValutazione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.RichiestaSupportoRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.RichiestaSupportoObserver;
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
 * Test unitari per SupportoService
 * (caso d'uso: Inviare Richiesta di Supporto a un Mentore).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SupportoService — Inviare Richiesta di Supporto a un Mentore")
class SupportoServiceTest {

    @Mock
    private HackathonRepository hackathonRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private MentoreRepository mentoreRepository;
    @Mock
    private RichiestaSupportoRepository richiestaSupportoRepository;
    @Mock
    private RichiestaSupportoObserver observer;

    private SupportoService service;

    private static final LocalDateTime BASE = LocalDateTime.now();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_TEAM = UUID.randomUUID();
    private static final UUID ID_MENTORE = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        service = new SupportoService(
                hackathonRepository,
                teamRepository,
                mentoreRepository,
                richiestaSupportoRepository
        );
        service.addObserver(observer);
    }

    // =========================================================================
    // inviaSupporto() — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("inviaSupporto()")
    class InviaSupporto {

        @Test
        @DisplayName("Hackathon IN_CORSO, mentore disponibile → salva richiesta e notifica observer")
        void hackathonInCorso_mentoreDisponibile_salvaENotifica() {
            // Given
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInCorso(), ID_MENTORE)));
            when(mentoreRepository.findById(ID_MENTORE))
                    .thenReturn(Optional.of(mentore()));
            doAnswer(inv -> {
                RichiestaSupporto r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return null;
            }).when(richiestaSupportoRepository).save(any(RichiestaSupporto.class));

            // When
            SupportoResponseDTO response = service.inviaSupporto(buildRequest());

            // Then
            assertNotNull(response);
            assertNotNull(response.idRichiesta());
            assertEquals(ID_MENTORE, response.idMentoreAssegnato());
            assertNotNull(response.nomeMentore());
            assertNotNull(response.dataInvio());
            assertTrue(response.messaggio().contains("Mario Rossi"));
            verify(richiestaSupportoRepository).save(any(RichiestaSupporto.class));
        }

        @Test
        @DisplayName("Observer viene notificato con la richiesta e l'email corretti")
        void observer_vienNotificatoCorrettamente() {
            // Given
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInCorso(), ID_MENTORE)));
            Mentore m = mentore();
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.of(m));
            doAnswer(inv -> {
                RichiestaSupporto r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return null;
            }).when(richiestaSupportoRepository).save(any(RichiestaSupporto.class));

            // When
            service.inviaSupporto(buildRequest());

            // Then
            ArgumentCaptor<RichiestaSupporto> richiestaCaptor =
                    ArgumentCaptor.forClass(RichiestaSupporto.class);
            ArgumentCaptor<String> emailCaptor = ArgumentCaptor.forClass(String.class);
            verify(observer).onNuovaRichiestaSupporto(richiestaCaptor.capture(), emailCaptor.capture());

            assertEquals(ID_TEAM, richiestaCaptor.getValue().getIdTeam());
            assertEquals(ID_HACKATHON, richiestaCaptor.getValue().getIdHackathon());
            assertEquals(ID_MENTORE, richiestaCaptor.getValue().getIdMentore());
            assertEquals("mario.rossi@example.com", emailCaptor.getValue());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.inviaSupporto(buildRequest()));
            verify(richiestaSupportoRepository, never()).save(any());
            verify(observer, never()).onNuovaRichiestaSupporto(any(), any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_ISCRIZIONE → lancia InvalidHackathonStateException")
        void hackathonInIscrizione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInIscrizione(), ID_MENTORE)));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.inviaSupporto(buildRequest()));
            verify(richiestaSupportoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hackathon in stato IN_VALUTAZIONE → lancia InvalidHackathonStateException")
        void hackathonInValutazione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInValutazione(), ID_MENTORE)));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.inviaSupporto(buildRequest()));
            verify(richiestaSupportoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Hackathon in stato CONCLUSO → lancia InvalidHackathonStateException")
        void hackathonConcluso_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoConcluso(), ID_MENTORE)));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.inviaSupporto(buildRequest()));
        }

        @Test
        @DisplayName("Hackathon senza mentori → lancia NoMentorsAvailableException")
        void nessunMentore_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonSenzaMentori(new StatoInCorso())));

            assertThrows(NoMentorsAvailableException.class,
                    () -> service.inviaSupporto(buildRequest()));
            verify(mentoreRepository, never()).findById(any());
            verify(richiestaSupportoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Motivo nullo → lancia ValidationException prima di accedere al repository")
        void motivoNullo_lancia() {
            SupportoRequestDTO dto = new SupportoRequestDTO(ID_HACKATHON, ID_TEAM, null);

            assertThrows(ValidationException.class, () -> service.inviaSupporto(dto));
            verify(hackathonRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Motivo troppo corto → lancia ValidationException")
        void motivoTroppoCorto_lancia() {
            SupportoRequestDTO dto = new SupportoRequestDTO(ID_HACKATHON, ID_TEAM, "corto");

            assertThrows(ValidationException.class, () -> service.inviaSupporto(dto));
        }
    }

    // =========================================================================
    // getFormData()
    // =========================================================================

    @Nested
    @DisplayName("getFormData()")
    class GetFormData {

        @Test
        @DisplayName("Hackathon IN_CORSO, team trovato → restituisce form")
        void hackathonInCorso_teamTrovato_restituisceForm() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInCorso(), ID_MENTORE)));
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alfa", "Desc", UUID.randomUUID(), ID_HACKATHON)));

            SupportoFormDTO form = service.getFormData(ID_HACKATHON, ID_TEAM);

            assertNotNull(form);
            assertEquals(ID_HACKATHON, form.idHackathon());
            assertEquals(ID_TEAM, form.idTeam());
            assertEquals("Team Alfa", form.nomeTeam());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> service.getFormData(ID_HACKATHON, ID_TEAM));
        }

        @Test
        @DisplayName("Hackathon IN_ISCRIZIONE → lancia InvalidHackathonStateException prima del team lookup")
        void hackathonInIscrizione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInIscrizione(), ID_MENTORE)));

            assertThrows(InvalidHackathonStateException.class,
                    () -> service.getFormData(ID_HACKATHON, ID_TEAM));
            verify(teamRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConMentore(new StatoInCorso(), ID_MENTORE)));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> service.getFormData(ID_HACKATHON, ID_TEAM));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private SupportoRequestDTO buildRequest() {
        return new SupportoRequestDTO(
                ID_HACKATHON,
                ID_TEAM,
                "Abbiamo bisogno di aiuto con l'architettura del progetto."
        );
    }

    private Mentore mentore() {
        return new Mentore(ID_MENTORE, "Mario", "Rossi", "mario.rossi@example.com", true);
    }

    private Hackathon hackathonConMentore(HackathonState stato, UUID idMentore) {
        return new Hackathon(
                ID_HACKATHON, "Hackathon Test",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                1000.0, null, 5, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(idMentore),
                stato
        );
    }

    private Hackathon hackathonSenzaMentori(HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "Hackathon Test",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                1000.0, null, 5, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(),
                List.of(),
                stato
        );
    }
}
