package hackhub.service;

import hackhub.dto.InvitationResponseDTO;
import hackhub.dto.InvitoListaItemDTO;
import hackhub.exception.InvitationNotFoundException;
import hackhub.exception.InvalidInvitationStateException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.UserAlreadyInTeamException;
import hackhub.model.StatoInvito;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Invito;
import hackhub.model.entity.Team;
import hackhub.model.entity.Utente;
import hackhub.model.state.StatoInIscrizione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.InvitoRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;
import hackhub.service.observer.InvitationObserver;
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
 * Test unitari per InvitationService
 * (caso d'uso: Accettare invito a unirsi al team).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("InvitationService — Accettare invito a unirsi al team")
class InvitationServiceTest {

    @Mock private InvitoRepository invitoRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private UserRepository userRepository;
    @Mock private InvitationObserver observer;

    private InvitationService service;

    private static final UUID ID_INVITO    = UUID.randomUUID();
    private static final UUID ID_UTENTE    = UUID.randomUUID();
    private static final UUID ID_TEAM      = UUID.randomUUID();
    private static final UUID ID_HACKATHON = UUID.randomUUID();
    private static final UUID ID_LEADER    = UUID.randomUUID();
    private static final LocalDateTime ORA = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new InvitationService(
                invitoRepository, teamRepository, hackathonRepository, userRepository);
        service.addObserver(observer);
    }

    // =========================================================================
    // processResponse() — accettazione
    // =========================================================================

    @Nested
    @DisplayName("processResponse() — accettazione")
    class Accettazione {

        @Test
        @DisplayName("Flusso felice: invito IN_ATTESA accettato → aggiunge membro, notifica observer")
        void invitoInAttesa_accettato_aggiungeMembroENotifica() {
            // Given
            Invito invito = invitoInAttesa();
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));
            when(userRepository.isUserAvailable(ID_UTENTE, ID_HACKATHON)).thenReturn(true);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));
            when(userRepository.findById(ID_UTENTE)).thenReturn(Optional.of(utente()));

            // When
            InvitationResponseDTO response = service.processResponse(ID_INVITO, ID_UTENTE, true);

            // Then
            assertEquals(StatoInvito.ACCETTATO, response.stato());
            assertTrue(response.messaggio().contains("membro del team"));
            verify(invitoRepository).aggiornaStato(ID_INVITO, StatoInvito.ACCETTATO);
            verify(teamRepository).addMembro(ID_TEAM, ID_UTENTE);
        }

        @Test
        @DisplayName("Observer riceve onInvitoAccettato con i parametri corretti")
        void observer_riceveOnInvitoAccettatoCorrettamente() {
            // Given
            Invito invito = invitoInAttesa();
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));
            when(userRepository.isUserAvailable(ID_UTENTE, ID_HACKATHON)).thenReturn(true);
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));
            when(userRepository.findById(ID_UTENTE)).thenReturn(Optional.of(utente()));

            // When
            service.processResponse(ID_INVITO, ID_UTENTE, true);

            // Then
            ArgumentCaptor<Invito> invitoCaptor = ArgumentCaptor.forClass(Invito.class);
            ArgumentCaptor<UUID> creatorCaptor = ArgumentCaptor.forClass(UUID.class);
            ArgumentCaptor<String> nomeCaptor = ArgumentCaptor.forClass(String.class);
            verify(observer).onInvitoAccettato(invitoCaptor.capture(), creatorCaptor.capture(), nomeCaptor.capture());

            assertEquals(ID_INVITO, invitoCaptor.getValue().getId());
            assertEquals(ID_LEADER, creatorCaptor.getValue());
            assertEquals("Mario Rossi", nomeCaptor.getValue());
        }

        @Test
        @DisplayName("Utente già in un team per lo stesso hackathon → lancia UserAlreadyInTeamException")
        void utenteGiàInTeam_lancia() {
            // Given
            Invito invito = invitoInAttesa();
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));
            when(userRepository.isUserAvailable(ID_UTENTE, ID_HACKATHON)).thenReturn(false);

            // When / Then
            assertThrows(UserAlreadyInTeamException.class,
                    () -> service.processResponse(ID_INVITO, ID_UTENTE, true));
            verify(teamRepository, never()).addMembro(any(), any());
            verify(observer, never()).onInvitoAccettato(any(), any(), any());
        }
    }

    // =========================================================================
    // processResponse() — rifiuto
    // =========================================================================

    @Nested
    @DisplayName("processResponse() — rifiuto")
    class Rifiuto {

        @Test
        @DisplayName("Flusso felice: invito IN_ATTESA rifiutato → NON aggiunge membro, notifica observer")
        void invitoInAttesa_rifiutato_nonAggiungeMembroENotifica() {
            // Given
            Invito invito = invitoInAttesa();
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));
            when(userRepository.findById(ID_UTENTE)).thenReturn(Optional.of(utente()));

            // When
            InvitationResponseDTO response = service.processResponse(ID_INVITO, ID_UTENTE, false);

            // Then
            assertEquals(StatoInvito.RIFIUTATO, response.stato());
            verify(invitoRepository).aggiornaStato(ID_INVITO, StatoInvito.RIFIUTATO);
            verify(teamRepository, never()).addMembro(any(), any());
            verify(userRepository, never()).isUserAvailable(any(), any());
        }

        @Test
        @DisplayName("Observer riceve onInvitoRifiutato al rifiuto")
        void observer_riceveOnInvitoRifiutato() {
            // Given
            Invito invito = invitoInAttesa();
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));
            when(userRepository.findById(ID_UTENTE)).thenReturn(Optional.of(utente()));

            // When
            service.processResponse(ID_INVITO, ID_UTENTE, false);

            // Then
            verify(observer).onInvitoRifiutato(any(Invito.class), eq(ID_LEADER), anyString());
            verify(observer, never()).onInvitoAccettato(any(), any(), any());
        }
    }

    // =========================================================================
    // processResponse() — casi di errore
    // =========================================================================

    @Nested
    @DisplayName("processResponse() — casi di errore")
    class CasiDiErrore {

        @Test
        @DisplayName("Invito non trovato → lancia InvitationNotFoundException")
        void invitoNonTrovato_lancia() {
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.empty());

            assertThrows(InvitationNotFoundException.class,
                    () -> service.processResponse(ID_INVITO, ID_UTENTE, true));
            verify(teamRepository, never()).addMembro(any(), any());
        }

        @Test
        @DisplayName("Utente non è il destinatario → lancia UnauthorizedActionException")
        void utenteNonDestinatario_lancia() {
            UUID altroUtente = UUID.randomUUID();
            Invito invito = invitoInAttesa(); // destinatario è ID_UTENTE
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.processResponse(ID_INVITO, altroUtente, true));
        }

        @Test
        @DisplayName("Invito già ACCETTATO → lancia InvalidInvitationStateException")
        void invitoGiàAccettato_lancia() {
            Invito invito = invitoConStato(StatoInvito.ACCETTATO);
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));

            assertThrows(InvalidInvitationStateException.class,
                    () -> service.processResponse(ID_INVITO, ID_UTENTE, true));
        }

        @Test
        @DisplayName("Invito già RIFIUTATO → lancia InvalidInvitationStateException")
        void invitoGiàRifiutato_lancia() {
            Invito invito = invitoConStato(StatoInvito.RIFIUTATO);
            when(invitoRepository.findById(ID_INVITO)).thenReturn(Optional.of(invito));

            assertThrows(InvalidInvitationStateException.class,
                    () -> service.processResponse(ID_INVITO, ID_UTENTE, true));
        }
    }

    // =========================================================================
    // getPendingInvitations()
    // =========================================================================

    @Nested
    @DisplayName("getPendingInvitations()")
    class GetPendingInvitations {

        @Test
        @DisplayName("Utente con un invito pendente → restituisce lista con un elemento")
        void invitoPendente_restituisceUnoElemento() {
            // Given
            Invito invito = invitoInAttesa();
            when(invitoRepository.findPendingByUtente(ID_UTENTE)).thenReturn(List.of(invito));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));
            when(userRepository.findById(ID_LEADER)).thenReturn(Optional.of(leader()));

            // When
            List<InvitoListaItemDTO> lista = service.getPendingInvitations(ID_UTENTE);

            // Then
            assertEquals(1, lista.size());
            InvitoListaItemDTO item = lista.get(0);
            assertEquals(ID_INVITO, item.idInvito());
            assertEquals(ID_TEAM, item.idTeam());
            assertEquals(ID_HACKATHON, item.idHackathon());
        }

        @Test
        @DisplayName("Nessun invito pendente → restituisce lista vuota")
        void nessunInvito_restituisceListaVuota() {
            when(invitoRepository.findPendingByUtente(ID_UTENTE)).thenReturn(List.of());

            List<InvitoListaItemDTO> lista = service.getPendingInvitations(ID_UTENTE);

            assertTrue(lista.isEmpty());
            verify(teamRepository, never()).findById(any());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private Invito invitoInAttesa() {
        return new Invito(ID_INVITO, ID_TEAM, ID_UTENTE, ID_HACKATHON,
                StatoInvito.IN_ATTESA, ORA);
    }

    private Invito invitoConStato(StatoInvito stato) {
        return new Invito(ID_INVITO, ID_TEAM, ID_UTENTE, ID_HACKATHON, stato, ORA);
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
                new StatoInIscrizione()
        );
    }

    private Utente utente() {
        return new Utente(ID_UTENTE, "Mario", "Rossi", "mario.rossi@test.com",
                "hash", null, null);
    }

    private Utente leader() {
        return new Utente(ID_LEADER, "Luigi", "Bianchi", "luigi.bianchi@test.com",
                "hash", null, null);
    }
}
