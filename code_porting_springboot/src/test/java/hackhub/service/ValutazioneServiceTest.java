package hackhub.service;

import hackhub.dto.SottomissioniDaValutareDTO;
import hackhub.dto.ValutazioneRequestDTO;
import hackhub.dto.ValutazioneResponseDTO;
import hackhub.exception.AlreadyEvaluatedException;
import hackhub.exception.InvalidHackathonStateException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.model.entity.Valutazione;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoInCorso;
import hackhub.model.state.StatoInValutazione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.ValutazioneRepository;
import hackhub.service.observer.ValutazioneObserver;
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
import static org.mockito.Mockito.*;

/**
 * Test unitari per ValutazioneService (caso d'uso: Valutare sottomissione di un team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValutazioneService — Valutare sottomissione di un team")
class ValutazioneServiceTest {

    @Mock
    private ValutazioneRepository valutazioneRepository;
    @Mock
    private SottomissioneRepository sottomissioneRepository;
    @Mock
    private HackathonRepository hackathonRepository;
    @Mock
    private TeamRepository teamRepository;

    private ValutazioneService valutazioneService;

    private static final LocalDateTime BASE = LocalDateTime.now();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_GIUDICE = UUID.randomUUID();
    private static final UUID ID_SOTTOMISSIONE = UUID.randomUUID();
    private static final UUID ID_TEAM = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        valutazioneService = new ValutazioneService(
                valutazioneRepository,
                sottomissioneRepository,
                hackathonRepository,
                teamRepository
        );
    }

    // =========================================================================
    // valutaSottomissione — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("valutaSottomissione()")
    class ValutaSottomissione {

        @Test
        @DisplayName("Dati validi, hackathon IN_VALUTAZIONE → salva valutazione, segna valutato, notifica observer")
        void datiValidi_salvaENotifica() {
            // Given
            stubHackathonInValutazione();
            stubSottomissioneNonValutata();
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alpha", "desc", UUID.randomUUID(), ID_HACKATHON)));

            ValutazioneObserver observer = mock(ValutazioneObserver.class);
            valutazioneService.addObserver(observer);

            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 8.5, "Ottimo progetto, ben strutturato.");

            // When
            ValutazioneResponseDTO response = valutazioneService.valutaSottomissione(request);

            // Then
            assertNotNull(response);
            assertEquals(ID_SOTTOMISSIONE, response.sottomissioneId());
            assertEquals("Team Alpha", response.nomeTeam());
            assertEquals(8.5, response.punteggio());
            assertEquals("Ottimo progetto, ben strutturato.", response.giudizioScritto());

            verify(valutazioneRepository).save(any(Valutazione.class));
            verify(sottomissioneRepository).markAsValutata(ID_SOTTOMISSIONE);
            verify(observer).onValutazioneCompletata(any(Valutazione.class), eq(ID_HACKATHON));
        }

        @Test
        @DisplayName("Punteggio esatto 0.0 → valido")
        void punteggioZero_valido() {
            stubHackathonInValutazione();
            stubSottomissioneNonValutata();
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team B", "desc", UUID.randomUUID(), ID_HACKATHON)));

            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 0.0, "Progetto non completato.");

            assertDoesNotThrow(() -> valutazioneService.valutaSottomissione(request));
        }

        @Test
        @DisplayName("Punteggio esatto 10.0 → valido")
        void punteggioDieci_valido() {
            stubHackathonInValutazione();
            stubSottomissioneNonValutata();
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team C", "desc", UUID.randomUUID(), ID_HACKATHON)));

            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 10.0, "Eccellente in ogni aspetto.");

            assertDoesNotThrow(() -> valutazioneService.valutaSottomissione(request));
        }

        @Test
        @DisplayName("Hackathon in stato IN_CORSO → lancia InvalidHackathonStateException")
        void hackathonInCorso_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));

            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 7.0, "Giudizio di test valido.");

            assertThrows(InvalidHackathonStateException.class,
                    () -> valutazioneService.valutaSottomissione(request));
            verify(valutazioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Sottomissione già valutata → lancia AlreadyEvaluatedException")
        void sottomissioneGiaValutata_lancia() {
            stubHackathonInValutazione();
            when(sottomissioneRepository.findById(ID_SOTTOMISSIONE))
                    .thenReturn(Optional.of(buildSottomissione(true)));

            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 7.0, "Giudizio di test valido.");

            assertThrows(AlreadyEvaluatedException.class,
                    () -> valutazioneService.valutaSottomissione(request));
            verify(valutazioneRepository, never()).save(any());
        }

        @Test
        @DisplayName("Punteggio > 10 → lancia ValidationException")
        void punteggioFuoriRange_lancia() {
            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 10.1, "Giudizio di test valido.");

            assertThrows(ValidationException.class,
                    () -> valutazioneService.valutaSottomissione(request));
        }

        @Test
        @DisplayName("Punteggio negativo → lancia ValidationException")
        void punteggioNegativo_lancia() {
            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, -1.0, "Giudizio di test valido.");

            assertThrows(ValidationException.class,
                    () -> valutazioneService.valutaSottomissione(request));
        }

        @Test
        @DisplayName("Giudizio scritto vuoto → lancia ValidationException")
        void giudizioVuoto_lancia() {
            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 7.0, "");

            assertThrows(ValidationException.class,
                    () -> valutazioneService.valutaSottomissione(request));
        }

        @Test
        @DisplayName("Giudizio scritto troppo corto (< 10 caratteri) → lancia ValidationException")
        void giudizioTroppoCorto_lancia() {
            ValutazioneRequestDTO request = new ValutazioneRequestDTO(
                    ID_SOTTOMISSIONE, ID_GIUDICE, ID_HACKATHON, 7.0, "Corto");

            assertThrows(ValidationException.class,
                    () -> valutazioneService.valutaSottomissione(request));
        }
    }

    // =========================================================================
    // getSottomissioniDaValutare — percorso felice
    // =========================================================================

    @Nested
    @DisplayName("getSottomissioniDaValutare()")
    class GetSottomissioni {

        @Test
        @DisplayName("Hackathon IN_VALUTAZIONE con sottomissioni → restituisce dashboard completa")
        void inValutazione_restituisceDashboard() {
            // Given
            stubHackathonInValutazione();
            Sottomissione s1 = buildSottomissione(false);
            Sottomissione s2 = buildSottomissione(true);
            when(sottomissioneRepository.findAllByHackathon(ID_HACKATHON))
                    .thenReturn(List.of(s1, s2));
            when(teamRepository.findById(ID_TEAM))
                    .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alpha", "desc", UUID.randomUUID(), ID_HACKATHON)));

            // When
            SottomissioniDaValutareDTO result =
                    valutazioneService.getSottomissioniDaValutare(ID_HACKATHON, ID_GIUDICE);

            // Then
            assertNotNull(result);
            assertEquals(ID_HACKATHON, result.hackathonId());
            assertEquals("Hackathon Test", result.nomeHackathon());
            assertEquals(2, result.sottomissioni().size());
            assertFalse(result.sottomissioni().get(0).valutato());
            assertTrue(result.sottomissioni().get(1).valutato());
        }

        @Test
        @DisplayName("Hackathon non in IN_VALUTAZIONE → lancia InvalidHackathonStateException")
        void hackathonNonInValutazione_lancia() {
            when(hackathonRepository.findById(ID_HACKATHON))
                    .thenReturn(Optional.of(hackathonConStato(new StatoInCorso())));

            assertThrows(InvalidHackathonStateException.class,
                    () -> valutazioneService.getSottomissioniDaValutare(ID_HACKATHON, ID_GIUDICE));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void stubHackathonInValutazione() {
        when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInValutazione())));
    }

    private void stubSottomissioneNonValutata() {
        when(sottomissioneRepository.findById(ID_SOTTOMISSIONE))
                .thenReturn(Optional.of(buildSottomissione(false)));
    }

    private Hackathon hackathonConStato(HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "Hackathon Test",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                5000.0, null, 4, "Regolamento",
                UUID.randomUUID(), ID_GIUDICE, List.of(),
                stato
        );
    }

    private Sottomissione buildSottomissione(boolean valutato) {
        return new Sottomissione(
                ID_SOTTOMISSIONE,
                "https://github.com/repo",
                "https://demo.example.com",
                "Descrizione progetto di test",
                BASE.minusHours(1),
                ID_TEAM,
                ID_HACKATHON,
                false,
                valutato
        );
    }
}
