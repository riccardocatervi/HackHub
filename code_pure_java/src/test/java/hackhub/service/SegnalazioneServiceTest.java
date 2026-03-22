package hackhub.service;

import hackhub.dto.ModuloSegnalazioneDTO;
import hackhub.dto.SegnalazioneRequestDTO;
import hackhub.dto.SegnalazioneResponseDTO;
import hackhub.exception.HackathonNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Segnalazione;
import hackhub.model.entity.Team;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoInCorso;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SegnalazioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.SegnalazioneObserver;
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
 * Test unitari per SegnalazioneService (caso d'uso: Segnalare violazione del regolamento).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SegnalazioneService — Segnalare violazione del regolamento")
class SegnalazioneServiceTest {

    @Mock
    private SegnalazioneRepository segnalazioneRepository;
    @Mock
    private TeamRepository teamRepository;
    @Mock
    private HackathonRepository hackathonRepository;

    private SegnalazioneService segnalazioneService;

    private static final LocalDateTime BASE = LocalDateTime.now();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_MENTORE = UUID.randomUUID();
    private static final UUID ID_TEAM = UUID.randomUUID();
    private static final UUID ID_ORGANIZZATORE = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        segnalazioneService = new SegnalazioneService(
                segnalazioneRepository,
                teamRepository,
                hackathonRepository
        );
    }

    // =========================================================================
    // segnalaViolazione — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("segnalaViolazione()")
    class SegnalaViolazione {

        @Test
        @DisplayName("Dati validi → salva segnalazione, notifica observer con organizzatoreId corretto")
        void datiValidi_salvaENotifica() {
            // Given
            stubHackathon();
            stubTeamValido();
            // Il mock di save setta l'id sull'entità (simula il DB)
            doAnswer(inv -> {
                Segnalazione s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return null;
            }).when(segnalazioneRepository).save(any(Segnalazione.class));

            SegnalazioneObserver observer = mock(SegnalazioneObserver.class);
            segnalazioneService.addObserver(observer);

            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, ID_TEAM,
                    "Utilizzo di strumenti non consentiti dal regolamento.",
                    "Screenshot allegato come prova."
            );

            // When
            SegnalazioneResponseDTO response = segnalazioneService.segnalaViolazione(request);

            // Then
            assertNotNull(response);
            assertNotNull(response.id());
            assertFalse(response.messaggio().isBlank());

            verify(segnalazioneRepository).save(any(Segnalazione.class));
            verify(observer).onNuovaSegnalazione(any(Segnalazione.class), eq(ID_ORGANIZZATORE));
        }

        @Test
        @DisplayName("Segnalazione senza prove → valida (prove è opzionale)")
        void senzaProve_valida() {
            stubHackathon();
            stubTeamValido();
            doAnswer(inv -> {
                Segnalazione s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return null;
            }).when(segnalazioneRepository).save(any(Segnalazione.class));

            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, ID_TEAM,
                    "Comportamento scorretto verso altri partecipanti.",
                    null
            );

            assertDoesNotThrow(() -> segnalazioneService.segnalaViolazione(request));
            verify(segnalazioneRepository).save(any(Segnalazione.class));
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, ID_TEAM,
                    "Descrizione valida della violazione.", null
            );

            assertThrows(HackathonNotFoundException.class,
                    () -> segnalazioneService.segnalaViolazione(request));
            verify(segnalazioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Team non appartiene all'hackathon → lancia ValidationException")
        void teamNonAppartieneHackathon_lancia() {
            stubHackathon();
            // Team appartiene a un hackathon diverso
            UUID altroHackathon = UUID.randomUUID();
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team Estraneo", "desc", UUID.randomUUID(), altroHackathon)));

            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, ID_TEAM,
                    "Descrizione valida della violazione.", null
            );

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.segnalaViolazione(request));
            verify(segnalazioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Descrizione vuota → lancia ValidationException prima di accedere al DB")
        void descrizioneVuota_lancia() {
            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, ID_TEAM, "", null
            );

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.segnalaViolazione(request));
            verify(segnalazioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Descrizione troppo corta (< 10 caratteri) → lancia ValidationException")
        void descrizioneTroppoCorta_lancia() {
            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, ID_TEAM, "Breve", null
            );

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.segnalaViolazione(request));
        }

        @Test
        @DisplayName("TeamId null → lancia ValidationException")
        void teamIdNull_lancia() {
            SegnalazioneRequestDTO request = new SegnalazioneRequestDTO(
                    ID_HACKATHON, ID_MENTORE, null,
                    "Descrizione valida della violazione.", null
            );

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.segnalaViolazione(request));
        }
    }

    // =========================================================================
    // getDatiModulo
    // =========================================================================

    @Nested
    @DisplayName("getDatiModulo()")
    class GetDatiModulo {

        @Test
        @DisplayName("Hackathon esistente con team → restituisce modulo con lista team")
        void hackathonConTeam_restituisceModulo() {
            // Given
            stubHackathon();
            Team t1 = new Team(UUID.randomUUID(), "Team A", "desc", UUID.randomUUID(), ID_HACKATHON);
            Team t2 = new Team(UUID.randomUUID(), "Team B", "desc", UUID.randomUUID(), ID_HACKATHON);
            when(teamRepository.findByHackathon(ID_HACKATHON)).thenReturn(List.of(t1, t2));

            // When
            ModuloSegnalazioneDTO modulo = segnalazioneService.getDatiModulo(ID_HACKATHON);

            // Then
            assertNotNull(modulo);
            assertEquals(ID_HACKATHON, modulo.hackathonId());
            assertEquals("Hackathon Test", modulo.nomeHackathon());
            assertEquals(2, modulo.teams().size());
            assertEquals("Team A", modulo.teams().get(0).nomeTeam());
        }

        @Test
        @DisplayName("Hackathon non trovato → lancia HackathonNotFoundException")
        void hackathonNonTrovato_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.empty());

            assertThrows(HackathonNotFoundException.class,
                    () -> segnalazioneService.getDatiModulo(ID_HACKATHON));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void stubHackathon() {
        when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));
    }

    private void stubTeamValido() {
        when(teamRepository.findById(ID_TEAM))
                .thenReturn(Optional.of(new Team(ID_TEAM, "Team Segnalato", "desc",
                        UUID.randomUUID(), ID_HACKATHON)));
    }

    private Hackathon hackathonConStato(HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "Hackathon Test",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                5000.0, null, 4, "Regolamento",
                ID_ORGANIZZATORE, UUID.randomUUID(), List.of(),
                stato
        );
    }
}
