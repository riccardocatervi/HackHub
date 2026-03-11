package hackhub.service;

import hackhub.dto.PaymentSuccessDTO;
import hackhub.dto.ProclamazioneResponseDTO;
import hackhub.exception.EntityNotFoundException;
import hackhub.model.entity.Hackathon;
import hackhub.model.entity.Sottomissione;
import hackhub.model.entity.Team;
import hackhub.model.entity.Utente;
import hackhub.model.state.HackathonState;
import hackhub.model.state.StatoHackathon;
import hackhub.model.state.StatoInValutazione;
import hackhub.repository.HackathonRepository;
import hackhub.repository.SottomissioneRepository;
import hackhub.repository.TeamRepository;
import hackhub.repository.UserRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Test unitari per ProclamazioneService — caso d'uso 'Erogare premio al team vincitore'.
 * Verifica l'integrazione del PaymentService nel flusso di proclamazione.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProclamazioneService — Erogare premio al team vincitore")
class ErogazionePremioServiceTest {

    @Mock private HackathonRepository     hackathonRepository;
    @Mock private SottomissioneRepository sottomissioneRepository;
    @Mock private TeamRepository          teamRepository;
    @Mock private NotificationsService    notificationsService;
    @Mock private PaymentService          paymentService;
    @Mock private UserRepository          userRepository;

    private ProclamazioneService proclamazioneService;

    private static final LocalDateTime BASE       = LocalDateTime.now();
    private static final UUID ID_HACKATHON        = UUID.randomUUID();
    private static final UUID ID_TEAM             = UUID.randomUUID();
    private static final UUID ID_SOTTOMISS        = UUID.randomUUID();
    private static final UUID ID_LEADER           = UUID.randomUUID();
    private static final double PREMIO            = 3000.0;
    private static final String EMAIL_LEADER      = "leader@team.it";

    @BeforeEach
    void setUp() {
        proclamazioneService = new ProclamazioneService(
                hackathonRepository,
                sottomissioneRepository,
                teamRepository,
                notificationsService,
                paymentService,
                userRepository
        );
    }

    // =========================================================================
    // eseguiProclamazione con PaymentService configurato
    // =========================================================================

    @Nested
    @DisplayName("eseguiProclamazione() — con PaymentService attivo")
    class ConPaymentService {

        @Test
        @DisplayName("Percorso felice → premio erogato, hackathon aggiornato con vincitore e premioDisbursed=true")
        void percorsoFelice_premioDisbursed() {
            // Given
            stubHackathon();
            stubVincitrice();
            stubTeamVincitore();
            stubLeader();

            PaymentSuccessDTO receipt = new PaymentSuccessDTO(
                    UUID.randomUUID(), EMAIL_LEADER, PREMIO, LocalDateTime.now()
            );
            when(paymentService.disbursePrize(eq(EMAIL_LEADER), eq(PREMIO), eq(ID_HACKATHON), eq(ID_TEAM)))
                    .thenReturn(receipt);

            // When
            ProclamazioneResponseDTO response = proclamazioneService.eseguiProclamazione(ID_HACKATHON);

            // Then
            assertNotNull(response);
            assertEquals(StatoHackathon.CONCLUSO, response.stato());
            assertEquals(PREMIO, response.premio());

            // Verifica interazioni obbligatorie
            verify(sottomissioneRepository).markAsVincitore(ID_SOTTOMISS);
            verify(hackathonRepository).updateStato(ID_HACKATHON, StatoHackathon.CONCLUSO);
            verify(paymentService).disbursePrize(EMAIL_LEADER, PREMIO, ID_HACKATHON, ID_TEAM);
            verify(hackathonRepository).updateVincitoreEPremio(eq(ID_HACKATHON), eq(ID_TEAM), eq(true));
            verify(notificationsService).notificaProclamazione(any(Hackathon.class), any(Team.class));
        }

        @Test
        @DisplayName("Leader non trovato nel repository → lancia EntityNotFoundException, nessun pagamento")
        void leaderNonTrovato_lancia() {
            // Given
            stubHackathon();
            stubVincitrice();
            stubTeamVincitore();
            when(userRepository.findById(ID_LEADER)).thenReturn(Optional.empty());

            // When / Then
            assertThrows(EntityNotFoundException.class,
                    () -> proclamazioneService.eseguiProclamazione(ID_HACKATHON));

            verify(paymentService, never()).disbursePrize(any(), anyDouble(), any(), any());
            verify(hackathonRepository, never()).updateVincitoreEPremio(any(), any(), anyBoolean());
        }
    }

    // =========================================================================
    // eseguiProclamazione — costruttore 4-param (senza PaymentService)
    // =========================================================================

    @Nested
    @DisplayName("eseguiProclamazione() — senza PaymentService (costruttore retro-compatibile)")
    class SenzaPaymentService {

        private ProclamazioneService serviceVecchio;

        @BeforeEach
        void setUpVecchio() {
            serviceVecchio = new ProclamazioneService(
                    hackathonRepository,
                    sottomissioneRepository,
                    teamRepository,
                    notificationsService
            );
        }

        @Test
        @DisplayName("PaymentService null → pagamento saltato, hackathon aggiornato con premioDisbursed=false")
        void paymentServiceNull_premioNonErogato() {
            // Given
            stubHackathon();
            stubVincitrice();
            stubTeamVincitore();

            // When
            ProclamazioneResponseDTO response = serviceVecchio.eseguiProclamazione(ID_HACKATHON);

            // Then
            assertNotNull(response);
            assertEquals(StatoHackathon.CONCLUSO, response.stato());

            // Il pagamento non deve mai essere invocato
            verifyNoInteractions(paymentService);
            verifyNoInteractions(userRepository);

            // Il DB viene comunque aggiornato con premioDisbursed=false
            verify(hackathonRepository).updateVincitoreEPremio(eq(ID_HACKATHON), eq(ID_TEAM), eq(false));
            verify(hackathonRepository).updateStato(ID_HACKATHON, StatoHackathon.CONCLUSO);
            verify(notificationsService).notificaProclamazione(any(Hackathon.class), any(Team.class));
        }
    }

    // =========================================================================
    // PaymentService — test unitario isolato
    // =========================================================================

    @Nested
    @DisplayName("PaymentService.disbursePrize()")
    class PaymentServiceTest {

        @Test
        @DisplayName("Gateway risponde con ricevuta → restituisce PaymentSuccessDTO corretto")
        void gatewaySuccesso_restituisceDTO() {
            // Usiamo il MockPaymentProviderGateway reale (non un mock) per testare l'integrazione
            hackhub.payment.MockPaymentProviderGateway mockGateway =
                    new hackhub.payment.MockPaymentProviderGateway();
            PaymentService realPaymentService = new PaymentService(mockGateway);

            // When
            PaymentSuccessDTO dto = realPaymentService.disbursePrize(
                    "winner@test.it", 1500.0, UUID.randomUUID(), UUID.randomUUID()
            );

            // Then
            assertNotNull(dto);
            assertEquals("winner@test.it", dto.emailDestinatario());
            assertEquals(1500.0, dto.importo(), 0.001);
            assertNotNull(dto.transactionId());
            assertNotNull(dto.timestamp());
        }
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private void stubHackathon() {
        when(hackathonRepository.findById(ID_HACKATHON))
                .thenReturn(Optional.of(hackathonConStato(new StatoInValutazione())));
    }

    private void stubVincitrice() {
        when(sottomissioneRepository.findVincitore(ID_HACKATHON))
                .thenReturn(Optional.of(buildSottomissione()));
    }

    private void stubTeamVincitore() {
        when(teamRepository.findById(ID_TEAM))
                .thenReturn(Optional.of(new Team(ID_TEAM, "Team Campioni", "desc", ID_LEADER, ID_HACKATHON)));
    }

    private void stubLeader() {
        when(userRepository.findById(ID_LEADER))
                .thenReturn(Optional.of(new Utente(ID_LEADER, "Anna", "Bianchi", EMAIL_LEADER, null, null, null)));
    }

    private Hackathon hackathonConStato(HackathonState stato) {
        return new Hackathon(
                ID_HACKATHON, "HackTest 2025",
                BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                PREMIO, null, 5, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(), List.of(),
                stato
        );
    }

    private Sottomissione buildSottomissione() {
        return new Sottomissione(
                ID_SOTTOMISS,
                "https://github.com/campioni/repo",
                "https://demo.campioni.it",
                "Soluzione premiata",
                BASE.minusHours(3),
                ID_TEAM,
                ID_HACKATHON,
                false,
                true
        );
    }
}
