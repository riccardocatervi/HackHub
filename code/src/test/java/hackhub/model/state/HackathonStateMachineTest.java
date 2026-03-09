package hackhub.model.state;

import hackhub.exception.IllegalStateTransitionException;
import hackhub.model.entity.Hackathon;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test unitari per il pattern State del ciclo di vita dell'Hackathon.
 * Nessun mock necessario: vengono testati oggetti di dominio puri.
 */
@DisplayName("Ciclo di vita Hackathon — pattern State")
class HackathonStateMachineTest {

    private static final LocalDateTime BASE = LocalDateTime.now();

    // -------------------------------------------------------------------------
    // Factory helper
    // -------------------------------------------------------------------------

    private static Hackathon hackathonConStato(HackathonState stato) {
        return new Hackathon(
                UUID.randomUUID(),
                "Hackathon di Test",
                BASE.plusDays(10),
                BASE.plusDays(20),
                BASE.plusDays(5),
                BASE.plusDays(15),
                1000.0,
                null,
                5,
                "Regolamento di test",
                UUID.randomUUID(),
                UUID.randomUUID(),
                List.of(),
                stato
        );
    }

    // =========================================================================
    // Guard: accettaSottomissione()
    // =========================================================================

    @Nested
    @DisplayName("accettaSottomissione()")
    class AccettaSottomissione {

        @Test
        @DisplayName("IN_ISCRIZIONE → lancia eccezione")
        void statoInIscrizione_lancia() {
            Hackathon h = hackathonConStato(new StatoInIscrizione());
            assertThrows(IllegalStateTransitionException.class,
                    h::verificaAccettaSottomissione);
        }

        @Test
        @DisplayName("IN_CORSO → consentita (nessuna eccezione)")
        void statoInCorso_consentita() {
            Hackathon h = hackathonConStato(new StatoInCorso());
            assertDoesNotThrow(h::verificaAccettaSottomissione);
        }

        @Test
        @DisplayName("IN_VALUTAZIONE → lancia eccezione")
        void statoInValutazione_lancia() {
            Hackathon h = hackathonConStato(new StatoInValutazione());
            assertThrows(IllegalStateTransitionException.class,
                    h::verificaAccettaSottomissione);
        }

        @Test
        @DisplayName("CONCLUSO → lancia eccezione")
        void statoConcluso_lancia() {
            Hackathon h = hackathonConStato(new StatoConcluso());
            assertThrows(IllegalStateTransitionException.class,
                    h::verificaAccettaSottomissione);
        }
    }

    // =========================================================================
    // Guard: accettaProclamazione()
    // =========================================================================

    @Nested
    @DisplayName("accettaProclamazione()")
    class AccettaProclamazione {

        @Test
        @DisplayName("IN_ISCRIZIONE → lancia eccezione")
        void statoInIscrizione_lancia() {
            Hackathon h = hackathonConStato(new StatoInIscrizione());
            assertThrows(IllegalStateTransitionException.class,
                    h::verificaAccettaProclamazione);
        }

        @Test
        @DisplayName("IN_CORSO → lancia eccezione")
        void statoInCorso_lancia() {
            Hackathon h = hackathonConStato(new StatoInCorso());
            assertThrows(IllegalStateTransitionException.class,
                    h::verificaAccettaProclamazione);
        }

        @Test
        @DisplayName("IN_VALUTAZIONE → consentita (nessuna eccezione)")
        void statoInValutazione_consentita() {
            Hackathon h = hackathonConStato(new StatoInValutazione());
            assertDoesNotThrow(h::verificaAccettaProclamazione);
        }

        @Test
        @DisplayName("CONCLUSO → lancia eccezione")
        void statoConcluso_lancia() {
            Hackathon h = hackathonConStato(new StatoConcluso());
            assertThrows(IllegalStateTransitionException.class,
                    h::verificaAccettaProclamazione);
        }
    }

    // =========================================================================
    // Transizioni di stato
    // =========================================================================

    @Nested
    @DisplayName("Transizioni di stato")
    class Transizioni {

        @Test
        @DisplayName("avvia(): IN_ISCRIZIONE → IN_CORSO")
        void avvia_daInIscrizione_transitaAInCorso() {
            Hackathon h = hackathonConStato(new StatoInIscrizione());
            h.avvia();
            assertEquals(StatoHackathon.IN_CORSO, h.getStatoEnum());
        }

        @Test
        @DisplayName("chiudiSottomissioni(): IN_CORSO → IN_VALUTAZIONE")
        void chiudiSottomissioni_daInCorso_transitaAInValutazione() {
            Hackathon h = hackathonConStato(new StatoInCorso());
            h.chiudiSottomissioni();
            assertEquals(StatoHackathon.IN_VALUTAZIONE, h.getStatoEnum());
        }

        @Test
        @DisplayName("concludi(): IN_VALUTAZIONE → CONCLUSO")
        void concludi_daInValutazione_transitaAConcluso() {
            Hackathon h = hackathonConStato(new StatoInValutazione());
            h.concludi();
            assertEquals(StatoHackathon.CONCLUSO, h.getStatoEnum());
        }

        @Test
        @DisplayName("Ciclo completo: IN_ISCRIZIONE → IN_CORSO → IN_VALUTAZIONE → CONCLUSO")
        void cicloDiVitaCompleto() {
            Hackathon h = hackathonConStato(new StatoInIscrizione());

            assertEquals(StatoHackathon.IN_ISCRIZIONE, h.getStatoEnum());
            h.avvia();
            assertEquals(StatoHackathon.IN_CORSO, h.getStatoEnum());
            h.chiudiSottomissioni();
            assertEquals(StatoHackathon.IN_VALUTAZIONE, h.getStatoEnum());
            h.concludi();
            assertEquals(StatoHackathon.CONCLUSO, h.getStatoEnum());
        }
    }

    // =========================================================================
    // Transizioni non valide
    // =========================================================================

    @Nested
    @DisplayName("Transizioni non valide")
    class TransizioniNonValide {

        @Test
        @DisplayName("avvia() da IN_CORSO → lancia eccezione")
        void avvia_daInCorso_lancia() {
            Hackathon h = hackathonConStato(new StatoInCorso());
            assertThrows(IllegalStateTransitionException.class, h::avvia);
        }

        @Test
        @DisplayName("avvia() da IN_VALUTAZIONE → lancia eccezione")
        void avvia_daInValutazione_lancia() {
            Hackathon h = hackathonConStato(new StatoInValutazione());
            assertThrows(IllegalStateTransitionException.class, h::avvia);
        }

        @Test
        @DisplayName("chiudiSottomissioni() da IN_ISCRIZIONE → lancia eccezione")
        void chiudiSottomissioni_daInIscrizione_lancia() {
            Hackathon h = hackathonConStato(new StatoInIscrizione());
            assertThrows(IllegalStateTransitionException.class, h::chiudiSottomissioni);
        }

        @Test
        @DisplayName("concludi() da IN_CORSO → lancia eccezione")
        void concludi_daInCorso_lancia() {
            Hackathon h = hackathonConStato(new StatoInCorso());
            assertThrows(IllegalStateTransitionException.class, h::concludi);
        }

        @Test
        @DisplayName("concludi() da CONCLUSO → lancia eccezione (stato terminale)")
        void concludi_daConcluso_lancia() {
            Hackathon h = hackathonConStato(new StatoConcluso());
            assertThrows(IllegalStateTransitionException.class, h::concludi);
        }
    }

    // =========================================================================
    // StatoHackathon enum
    // =========================================================================

    @Nested
    @DisplayName("StatoHackathon — factory creaIstanza()")
    class StatoHackathonEnum {

        @Test
        @DisplayName("creaIstanza() restituisce StatoInIscrizione per IN_ISCRIZIONE")
        void inIscrizione_creaIstanzaCorretta() {
            assertInstanceOf(StatoInIscrizione.class,
                    StatoHackathon.IN_ISCRIZIONE.creaIstanza());
        }

        @Test
        @DisplayName("creaIstanza() restituisce StatoInCorso per IN_CORSO")
        void inCorso_creaIstanzaCorretta() {
            assertInstanceOf(StatoInCorso.class,
                    StatoHackathon.IN_CORSO.creaIstanza());
        }

        @Test
        @DisplayName("creaIstanza() restituisce StatoInValutazione per IN_VALUTAZIONE")
        void inValutazione_creaIstanzaCorretta() {
            assertInstanceOf(StatoInValutazione.class,
                    StatoHackathon.IN_VALUTAZIONE.creaIstanza());
        }

        @Test
        @DisplayName("creaIstanza() restituisce StatoConcluso per CONCLUSO")
        void concluso_creaIstanzaCorretta() {
            assertInstanceOf(StatoConcluso.class,
                    StatoHackathon.CONCLUSO.creaIstanza());
        }

        @Test
        @DisplayName("getNome() di ogni stato corrisponde al valore enum")
        void getNome_corrispondeAllEnum() {
            assertEquals(StatoHackathon.IN_ISCRIZIONE, new StatoInIscrizione().getNome());
            assertEquals(StatoHackathon.IN_CORSO, new StatoInCorso().getNome());
            assertEquals(StatoHackathon.IN_VALUTAZIONE, new StatoInValutazione().getNome());
            assertEquals(StatoHackathon.CONCLUSO, new StatoConcluso().getNome());
        }
    }

    // =========================================================================
    // Invariante del costruttore Hackathon
    // =========================================================================

    @Test
    @DisplayName("Nuovo Hackathon parte sempre con stato IN_ISCRIZIONE")
    void nuovoHackathon_iniziaInIscrizione() {
        Hackathon h = new Hackathon(
                "Test", BASE.plusDays(10), BASE.plusDays(20),
                BASE.plusDays(5), BASE.plusDays(15),
                500.0, null, 3, "Regolamento",
                UUID.randomUUID(), UUID.randomUUID(), List.of()
        );
        assertEquals(StatoHackathon.IN_ISCRIZIONE, h.getStatoEnum());
    }
}
