package hackhub.service;

import hackhub.dto.CallCreateDTO;
import hackhub.dto.DatiEventoEsterno;

import java.util.UUID;
import java.util.logging.Logger;

/**
 * Implementazione mock del servizio Calendar.
 * <p>
 * Simula la comunicazione con un sistema Calendar esterno (es. Google Calendar)
 * generando un link di meeting fittizio e un id evento casuale.
 * Utilizzata in fase di sviluppo e nei test unitari al posto dell'implementazione reale.
 * <p>
 * In produzione, questa classe viene sostituita con un'implementazione che integra
 * le API del provider Calendar prescelto (es. GoogleCalendarService).
 */
public class MockCalendarService implements CalendarService {

    private static final Logger LOG = Logger.getLogger(MockCalendarService.class.getName());

    @Override
    public DatiEventoEsterno creaEventoCalendar(CallCreateDTO dto, String emailLeader) {
        // Generazione di un link e di un id evento fittizi per l'ambiente di test
        String linkCall = "https://meet.hackhub.dev/call/" + UUID.randomUUID();
        String idEventoCalendar = "mock-event-" + UUID.randomUUID();

        LOG.info(String.format(
                "Mock Calendar: evento creato per la richiesta %s — data: %s %s, " +
                        "link: %s, invito inviato a: %s",
                dto.idRichiesta(),
                dto.dataCall(),
                dto.oraCall(),
                linkCall,
                emailLeader
        ));

        return new DatiEventoEsterno(linkCall, idEventoCalendar);
    }
}
