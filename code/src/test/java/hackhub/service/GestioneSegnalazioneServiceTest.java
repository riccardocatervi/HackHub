package hackhub.service;

import hackhub.dto.SegnalazioneDettagliDTO;
import hackhub.dto.SegnalazioneGestioneDTO;
import hackhub.dto.SegnalazioneGestioneResponseDTO;
import hackhub.exception.SegnalazioneAlreadyManagedException;
import hackhub.exception.SegnalazioneNotFoundException;
import hackhub.exception.TeamNotFoundException;
import hackhub.exception.ValidationException;
import hackhub.model.StatoSegnalazione;
import hackhub.model.entity.Mentore;
import hackhub.model.entity.Segnalazione;
import hackhub.model.entity.Team;
import hackhub.repository.HackathonRepository;
import hackhub.repository.MentoreRepository;
import hackhub.repository.SegnalazioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.GestioneSegnalazioneObserver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test unitari per SegnalazioneService — caso d'uso 'Gestire penalizzazione o squalifica di un team'.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SegnalazioneService — Gestire penalizzazione o squalifica di un team")
class GestioneSegnalazioneServiceTest {

    @Mock private SegnalazioneRepository segnalazioneRepository;
    @Mock private TeamRepository         teamRepository;
    @Mock private HackathonRepository    hackathonRepository;
    @Mock private MentoreRepository      mentoreRepository;

    private SegnalazioneService segnalazioneService;

    private static final UUID ID_SEGNALAZIONE  = UUID.randomUUID();
    private static final UUID ID_TEAM          = UUID.randomUUID();
    private static final UUID ID_MENTORE       = UUID.randomUUID();
    private static final UUID ID_HACKATHON     = UUID.randomUUID();
    private static final LocalDateTime DATA    = LocalDateTime.now().minusHours(2);

    @BeforeEach
    void setUp() {
        segnalazioneService = new SegnalazioneService(
                segnalazioneRepository,
                teamRepository,
                hackathonRepository,
                mentoreRepository
        );
    }

    // =========================================================================
    // getDettagliSegnalazione
    // =========================================================================

    @Nested
    @DisplayName("getDettagliSegnalazione()")
    class GetDettagliSegnalazione {

        @Test
        @DisplayName("Segnalazione e team esistenti, mentore trovato → DTO completo con nome mentore")
        void tuttiDatiPresenti_restituisceDTOCompleto() {
            // Given
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            stubTeam();
            when(mentoreRepository.findById(ID_MENTORE))
                    .thenReturn(Optional.of(new Mentore(ID_MENTORE, "Mario", "Rossi", "mario@test.it", true)));

            // When
            SegnalazioneDettagliDTO dto = segnalazioneService.getDettagliSegnalazione(ID_SEGNALAZIONE);

            // Then
            assertNotNull(dto);
            assertEquals(ID_SEGNALAZIONE, dto.id());
            assertEquals(ID_TEAM, dto.teamId());
            assertEquals("Team Alfa", dto.nomeTeam());
            assertEquals(ID_MENTORE, dto.mentoreId());
            assertEquals("Mario Rossi", dto.nomeMentore());
            assertEquals("Violazione grave del regolamento.", dto.descrizione());
            assertEquals(StatoSegnalazione.PENDENTE, dto.stato());
        }

        @Test
        @DisplayName("Mentore non trovato nel repository → nomeMentore null nel DTO")
        void mentoreNonTrovato_nomeMentoreNull() {
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            stubTeam();
            when(mentoreRepository.findById(ID_MENTORE)).thenReturn(Optional.empty());

            SegnalazioneDettagliDTO dto = segnalazioneService.getDettagliSegnalazione(ID_SEGNALAZIONE);

            assertNull(dto.nomeMentore());
        }

        @Test
        @DisplayName("Costruttore 3-param (senza MentoreRepository) → nomeMentore null nel DTO")
        void senzaMentoreRepository_nomeMentoreNull() {
            // Service creato senza MentoreRepository (costruttore retro-compatibile)
            SegnalazioneService serviceSenzaMentoreRepo = new SegnalazioneService(
                    segnalazioneRepository, teamRepository, hackathonRepository
            );
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            stubTeam();

            SegnalazioneDettagliDTO dto = serviceSenzaMentoreRepo.getDettagliSegnalazione(ID_SEGNALAZIONE);

            assertNull(dto.nomeMentore());
            // MentoreRepository non deve mai essere invocato
            verifyNoInteractions(mentoreRepository);
        }

        @Test
        @DisplayName("Segnalazione non trovata → lancia SegnalazioneNotFoundException")
        void segnalazioneNonTrovata_lancia() {
            when(segnalazioneRepository.findById(ID_SEGNALAZIONE)).thenReturn(Optional.empty());

            assertThrows(SegnalazioneNotFoundException.class,
                    () -> segnalazioneService.getDettagliSegnalazione(ID_SEGNALAZIONE));
            verifyNoInteractions(teamRepository);
        }

        @Test
        @DisplayName("Team non trovato → lancia TeamNotFoundException")
        void teamNonTrovato_lancia() {
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            assertThrows(TeamNotFoundException.class,
                    () -> segnalazioneService.getDettagliSegnalazione(ID_SEGNALAZIONE));
        }
    }

    // =========================================================================
    // gestisciSegnalazione — RIFIUTATA
    // =========================================================================

    @Nested
    @DisplayName("gestisciSegnalazione() — azione RIFIUTATA")
    class GestisciSegnalazioneRifiutata {

        @Test
        @DisplayName("Segnalazione PENDENTE + azione RIFIUTATA → stato aggiornato, team NON squalificato, observer notificato")
        void rifiutata_nonSqualificaTeam() {
            // Given
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            GestioneSegnalazioneObserver observer = mock(GestioneSegnalazioneObserver.class);
            segnalazioneService.addGestioneObserver(observer);

            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.RIFIUTATA);

            // When
            SegnalazioneGestioneResponseDTO response = segnalazioneService.gestisciSegnalazione(dto);

            // Then
            assertNotNull(response);
            assertEquals(StatoSegnalazione.RIFIUTATA, response.stato());
            assertFalse(response.teamSqualificato());
            assertFalse(response.messaggio().isBlank());

            verify(segnalazioneRepository).updateStato(eq(ID_SEGNALAZIONE), eq(StatoSegnalazione.RIFIUTATA));
            verifyNoInteractions(teamRepository);
            verify(observer).onSegnalazioneGestita(eq(ID_SEGNALAZIONE), eq(ID_MENTORE), eq(StatoSegnalazione.RIFIUTATA));
        }
    }

    // =========================================================================
    // gestisciSegnalazione — ACCETTATA
    // =========================================================================

    @Nested
    @DisplayName("gestisciSegnalazione() — azione ACCETTATA")
    class GestisciSegnalazioneAccettata {

        @Test
        @DisplayName("Segnalazione PENDENTE + azione ACCETTATA → team squalificato, stato aggiornato, observer notificato")
        void accettata_squalificaTeam() {
            // Given
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            stubTeam();
            GestioneSegnalazioneObserver observer = mock(GestioneSegnalazioneObserver.class);
            segnalazioneService.addGestioneObserver(observer);

            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.ACCETTATA);

            // When
            SegnalazioneGestioneResponseDTO response = segnalazioneService.gestisciSegnalazione(dto);

            // Then
            assertTrue(response.teamSqualificato());
            assertEquals(StatoSegnalazione.ACCETTATA, response.stato());
            assertFalse(response.messaggio().isBlank());

            verify(teamRepository).updateSqualificato(eq(ID_TEAM), eq(true));
            verify(segnalazioneRepository).updateStato(eq(ID_SEGNALAZIONE), eq(StatoSegnalazione.ACCETTATA));
            verify(observer).onSegnalazioneGestita(eq(ID_SEGNALAZIONE), eq(ID_MENTORE), eq(StatoSegnalazione.ACCETTATA));
        }

        @Test
        @DisplayName("Team del segnalazione non trovato → lancia TeamNotFoundException, nessun aggiornamento")
        void teamNonTrovato_lanciaEsenzaAggiornamenti() {
            stubSegnalazione(StatoSegnalazione.PENDENTE);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.empty());

            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.ACCETTATA);

            assertThrows(TeamNotFoundException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
            verify(teamRepository, never()).updateSqualificato(any(), anyBoolean());
            verify(segnalazioneRepository, never()).updateStato(any(), any());
        }
    }

    // =========================================================================
    // gestisciSegnalazione — casi di errore
    // =========================================================================

    @Nested
    @DisplayName("gestisciSegnalazione() — casi di errore")
    class GestisciSegnalazioneCasiErrore {

        @Test
        @DisplayName("Segnalazione non trovata → lancia SegnalazioneNotFoundException")
        void segnalazioneNonTrovata_lancia() {
            when(segnalazioneRepository.findById(ID_SEGNALAZIONE)).thenReturn(Optional.empty());

            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.RIFIUTATA);

            assertThrows(SegnalazioneNotFoundException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
            verify(segnalazioneRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("Segnalazione già ACCETTATA → lancia SegnalazioneAlreadyManagedException")
        void segnalazioneGiaAccettata_lancia() {
            stubSegnalazione(StatoSegnalazione.ACCETTATA);

            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.RIFIUTATA);

            assertThrows(SegnalazioneAlreadyManagedException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
            verify(segnalazioneRepository, never()).updateStato(any(), any());
        }

        @Test
        @DisplayName("Segnalazione già RIFIUTATA → lancia SegnalazioneAlreadyManagedException")
        void segnalazioneGiaRifiutata_lancia() {
            stubSegnalazione(StatoSegnalazione.RIFIUTATA);

            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.ACCETTATA);

            assertThrows(SegnalazioneAlreadyManagedException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
        }

        @Test
        @DisplayName("Azione null → lancia ValidationException prima di accedere al DB")
        void azioneNull_lancia() {
            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, null);

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
            verifyNoInteractions(segnalazioneRepository);
        }

        @Test
        @DisplayName("Azione PENDENTE → lancia ValidationException prima di accedere al DB")
        void azionePendente_lancia() {
            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(ID_SEGNALAZIONE, StatoSegnalazione.PENDENTE);

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
            verifyNoInteractions(segnalazioneRepository);
        }

        @Test
        @DisplayName("Id segnalazione null → lancia ValidationException")
        void idNull_lancia() {
            SegnalazioneGestioneDTO dto = new SegnalazioneGestioneDTO(null, StatoSegnalazione.RIFIUTATA);

            assertThrows(ValidationException.class,
                    () -> segnalazioneService.gestisciSegnalazione(dto));
            verifyNoInteractions(segnalazioneRepository);
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void stubSegnalazione(StatoSegnalazione stato) {
        Segnalazione segnalazione = new Segnalazione(
                ID_SEGNALAZIONE,
                ID_TEAM,
                ID_MENTORE,
                ID_HACKATHON,
                "Violazione grave del regolamento.",
                "Screenshot allegato.",
                DATA,
                stato
        );
        when(segnalazioneRepository.findById(ID_SEGNALAZIONE)).thenReturn(Optional.of(segnalazione));
    }

    private void stubTeam() {
        when(teamRepository.findById(ID_TEAM))
                .thenReturn(Optional.of(new Team(ID_TEAM, "Team Alfa", "desc",
                        UUID.randomUUID(), ID_HACKATHON)));
    }
}
