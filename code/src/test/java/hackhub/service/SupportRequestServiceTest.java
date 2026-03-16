package hackhub.service;

import hackhub.dto.EsitoGestioneRichiestaDTO;
import hackhub.dto.RichiestaSupportoDettagliDTO;
import hackhub.exception.InvalidRequestStateException;
import hackhub.exception.SupportRequestNotFoundException;
import hackhub.exception.UnauthorizedActionException;
import hackhub.exception.ValidationException;
import hackhub.model.LivelloUrgenza;
import hackhub.model.StatoRichiesta;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.RichiestaSupporto;
import hackhub.model.entity.Team;
import hackhub.model.state.StatoInCorso;
import hackhub.repository.HackathonRepository;
import hackhub.repository.RichiestaSupportoRepository;
import hackhub.repository.TeamRepository;
import hackhub.service.observer.GestioneRichiestaSupportoObserver;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Test unitari per SupportRequestService
 * (caso d'uso: Prendere in carico una richiesta di supporto).
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SupportRequestService — Prendere in carico una richiesta di supporto")
class SupportRequestServiceTest {

    @Mock private RichiestaSupportoRepository richiestaSupportoRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private HackathonRepository hackathonRepository;
    @Mock private GestioneRichiestaSupportoObserver observer;

    private SupportRequestService service;

    private static final UUID ID_RICHIESTA  = UUID.randomUUID();
    private static final UUID ID_MENTORE    = UUID.randomUUID();
    private static final UUID ID_ALTRO_MENTORE = UUID.randomUUID();
    private static final UUID ID_TEAM       = UUID.randomUUID();
    private static final UUID ID_LEADER     = UUID.randomUUID();
    private static final UUID ID_HACKATHON  = UUID.randomUUID();
    private static final LocalDateTime ORA  = LocalDateTime.now();

    @BeforeEach
    void setUp() {
        service = new SupportRequestService(
                richiestaSupportoRepository, teamRepository, hackathonRepository);
        service.addObserver(observer);
    }

    // =========================================================================
    // getRichiestePendentiByMentore()
    // =========================================================================

    @Nested
    @DisplayName("getRichiestePendentiByMentore()")
    class GetRichiestePendenti {

        @Test
        @DisplayName("Mentore con una richiesta pendente → restituisce lista con un elemento")
        void richiestaPendente_restituisceUnoElemento() {
            // Given
            RichiestaSupporto richiesta = richiestaPendente();
            when(richiestaSupportoRepository.findPendingByMentore(ID_MENTORE))
                    .thenReturn(List.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));

            // When
            List<RichiestaSupportoDettagliDTO> lista = service.getRichiestePendentiByMentore(ID_MENTORE);

            // Then
            assertEquals(1, lista.size());
            RichiestaSupportoDettagliDTO item = lista.get(0);
            assertEquals(ID_RICHIESTA, item.idRichiesta());
            assertEquals(ID_TEAM, item.idTeam());
            assertEquals(StatoRichiesta.PENDENTE, item.stato());
            assertEquals(LivelloUrgenza.ALTA, item.livelloUrgenza());
        }

        @Test
        @DisplayName("Nessuna richiesta pendente → restituisce lista vuota")
        void nessunaPendente_restituisceListaVuota() {
            when(richiestaSupportoRepository.findPendingByMentore(ID_MENTORE))
                    .thenReturn(List.of());

            List<RichiestaSupportoDettagliDTO> lista = service.getRichiestePendentiByMentore(ID_MENTORE);

            assertTrue(lista.isEmpty());
            verify(teamRepository, never()).findById(any());
        }
    }

    // =========================================================================
    // getDettagliRichiesta()
    // =========================================================================

    @Nested
    @DisplayName("getDettagliRichiesta()")
    class GetDettagli {

        @Test
        @DisplayName("Flusso felice: mentore proprietario → restituisce dettagli completi")
        void mentoreProprietario_restituisceDettagli() {
            // Given
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPendente()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            when(hackathonRepository.findById(ID_HACKATHON)).thenReturn(Optional.of(hackathon()));

            // When
            RichiestaSupportoDettagliDTO dto = service.getDettagliRichiesta(ID_RICHIESTA, ID_MENTORE);

            // Then
            assertNotNull(dto);
            assertEquals(ID_RICHIESTA, dto.idRichiesta());
            assertEquals("Team Alpha", dto.nomeTeam());
            assertEquals(StatoRichiesta.PENDENTE, dto.stato());
            assertEquals(LivelloUrgenza.ALTA, dto.livelloUrgenza());
        }

        @Test
        @DisplayName("Richiesta non trovata → lancia SupportRequestNotFoundException")
        void richiestaNonTrovata_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.empty());

            assertThrows(SupportRequestNotFoundException.class,
                    () -> service.getDettagliRichiesta(ID_RICHIESTA, ID_MENTORE));
        }

        @Test
        @DisplayName("Mentore non proprietario → lancia UnauthorizedActionException")
        void mentoreNonProprietario_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPendente()));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.getDettagliRichiesta(ID_RICHIESTA, ID_ALTRO_MENTORE));
        }
    }

    // =========================================================================
    // accettaRichiesta()
    // =========================================================================

    @Nested
    @DisplayName("accettaRichiesta()")
    class AccettaRichiesta {

        @Test
        @DisplayName("Flusso felice: richiesta PENDENTE accettata → aggiorna stato, notifica observer")
        void richiestaPendente_accettata_aggiornaStatoENotifica() {
            // Given
            RichiestaSupporto richiesta = richiestaPendente();
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));

            // When
            EsitoGestioneRichiestaDTO response = service.accettaRichiesta(ID_RICHIESTA, ID_MENTORE);

            // Then
            assertNotNull(response);
            assertEquals(ID_RICHIESTA, response.idRichiesta());
            assertEquals(ID_TEAM, response.idTeam());
            assertTrue(response.messaggio().contains("Team Alpha"));

            verify(richiestaSupportoRepository).aggiornaStato(ID_RICHIESTA, StatoRichiesta.PRESA_IN_CARICO);
            assertEquals(StatoRichiesta.PRESA_IN_CARICO, richiesta.getStato());
        }

        @Test
        @DisplayName("Observer riceve onRichiestaAccettata con i parametri corretti")
        void observer_riceveOnRichiestaAccettatoCorrettamente() {
            // Given
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPendente()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));

            // When
            service.accettaRichiesta(ID_RICHIESTA, ID_MENTORE);

            // Then
            ArgumentCaptor<UUID> leaderCaptor = ArgumentCaptor.forClass(UUID.class);
            ArgumentCaptor<UUID> richiestaCaptor = ArgumentCaptor.forClass(UUID.class);
            ArgumentCaptor<UUID> mentoreCaptor = ArgumentCaptor.forClass(UUID.class);
            verify(observer).onRichiestaAccettata(
                    leaderCaptor.capture(), richiestaCaptor.capture(), mentoreCaptor.capture());

            assertEquals(ID_LEADER, leaderCaptor.getValue());
            assertEquals(ID_RICHIESTA, richiestaCaptor.getValue());
            assertEquals(ID_MENTORE, mentoreCaptor.getValue());
        }

        @Test
        @DisplayName("Richiesta già PRESA_IN_CARICO → lancia InvalidRequestStateException")
        void richiestaGiàPresa_lancia() {
            RichiestaSupporto richiesta = richiestaConStato(StatoRichiesta.PRESA_IN_CARICO);
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));

            assertThrows(InvalidRequestStateException.class,
                    () -> service.accettaRichiesta(ID_RICHIESTA, ID_MENTORE));
            verify(richiestaSupportoRepository, never()).aggiornaStato(any(), any());
            verify(observer, never()).onRichiestaAccettata(any(), any(), any());
        }

        @Test
        @DisplayName("Richiesta già RESPINTA → lancia InvalidRequestStateException")
        void richiestaGiàRespinta_lancia() {
            RichiestaSupporto richiesta = richiestaConStato(StatoRichiesta.RESPINTA);
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));

            assertThrows(InvalidRequestStateException.class,
                    () -> service.accettaRichiesta(ID_RICHIESTA, ID_MENTORE));
        }

        @Test
        @DisplayName("Mentore non proprietario → lancia UnauthorizedActionException")
        void mentoreNonProprietario_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPendente()));

            assertThrows(UnauthorizedActionException.class,
                    () -> service.accettaRichiesta(ID_RICHIESTA, ID_ALTRO_MENTORE));
            verify(richiestaSupportoRepository, never()).aggiornaStato(any(), any());
        }

        @Test
        @DisplayName("Richiesta non trovata → lancia SupportRequestNotFoundException")
        void richiestaNonTrovata_lancia() {
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.empty());

            assertThrows(SupportRequestNotFoundException.class,
                    () -> service.accettaRichiesta(ID_RICHIESTA, ID_MENTORE));
        }
    }

    // =========================================================================
    // respingiRichiesta()
    // =========================================================================

    @Nested
    @DisplayName("respingiRichiesta()")
    class RespingiRichiesta {

        @Test
        @DisplayName("Flusso felice: richiesta PENDENTE rifiutata → aggiorna stato e motivazione, notifica")
        void richiestaPendente_rifiutata_aggiornaENotifica() {
            // Given
            RichiestaSupporto richiesta = richiestaPendente();
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            String motivazione = "Il problema descritto non è di mia competenza.";

            // When
            EsitoGestioneRichiestaDTO response = service.respingiRichiesta(ID_RICHIESTA, ID_MENTORE, motivazione);

            // Then
            assertNotNull(response);
            assertEquals(ID_RICHIESTA, response.idRichiesta());
            assertEquals(motivazione, response.motivazione());
            assertTrue(response.messaggio().contains("Team Alpha"));

            verify(richiestaSupportoRepository).aggiornaStatoEMotivazione(
                    eq(ID_RICHIESTA), eq(StatoRichiesta.RESPINTA), eq(motivazione));
            assertEquals(StatoRichiesta.RESPINTA, richiesta.getStato());
            assertEquals(motivazione, richiesta.getMotivazioneRifiuto());
        }

        @Test
        @DisplayName("Observer riceve onRichiestaRespinta con i parametri corretti")
        void observer_riceveOnRichiestaRespintaCorrettamente() {
            // Given
            when(richiestaSupportoRepository.findById(ID_RICHIESTA))
                    .thenReturn(Optional.of(richiestaPendente()));
            when(teamRepository.findById(ID_TEAM)).thenReturn(Optional.of(team()));
            String motivazione = "Problema fuori dalla mia area di competenza.";

            // When
            service.respingiRichiesta(ID_RICHIESTA, ID_MENTORE, motivazione);

            // Then
            verify(observer).onRichiestaRespinta(
                    eq(ID_LEADER), eq(ID_RICHIESTA), eq(ID_MENTORE), eq(motivazione));
            verify(observer, never()).onRichiestaAccettata(any(), any(), any());
        }

        @Test
        @DisplayName("Motivazione assente → lancia ValidationException")
        void motivazioneAssente_lancia() {
            assertThrows(ValidationException.class,
                    () -> service.respingiRichiesta(ID_RICHIESTA, ID_MENTORE, null));
            verify(richiestaSupportoRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Motivazione vuota → lancia ValidationException")
        void motivazioneVuota_lancia() {
            assertThrows(ValidationException.class,
                    () -> service.respingiRichiesta(ID_RICHIESTA, ID_MENTORE, "  "));
        }

        @Test
        @DisplayName("Motivazione troppo breve (< 10 caratteri) → lancia ValidationException")
        void motivazioneTroppoBreve_lancia() {
            assertThrows(ValidationException.class,
                    () -> service.respingiRichiesta(ID_RICHIESTA, ID_MENTORE, "Troppo."));
        }

        @Test
        @DisplayName("Richiesta già gestita → lancia InvalidRequestStateException")
        void richiestaGiàGestita_lancia() {
            RichiestaSupporto richiesta = richiestaConStato(StatoRichiesta.PRESA_IN_CARICO);
            when(richiestaSupportoRepository.findById(ID_RICHIESTA)).thenReturn(Optional.of(richiesta));

            assertThrows(InvalidRequestStateException.class,
                    () -> service.respingiRichiesta(ID_RICHIESTA, ID_MENTORE,
                            "Motivazione valida di almeno dieci caratteri."));
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private RichiestaSupporto richiestaPendente() {
        RichiestaSupporto r = new RichiestaSupporto(
                ID_RICHIESTA, ID_TEAM, ID_HACKATHON, ID_MENTORE,
                "Problema tecnico urgente con la pipeline CI/CD.",
                ORA, StatoRichiesta.PENDENTE, LivelloUrgenza.ALTA, null);
        return r;
    }

    private RichiestaSupporto richiestaConStato(StatoRichiesta stato) {
        return new RichiestaSupporto(
                ID_RICHIESTA, ID_TEAM, ID_HACKATHON, ID_MENTORE,
                "Motivo di test per verifica stato.",
                ORA, stato, LivelloUrgenza.NORMALE, null);
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
}
