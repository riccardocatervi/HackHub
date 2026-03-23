package hackhub.service;

import hackhub.dto.CallCreateDTO;
import hackhub.dto.DatiEventoEsterno;
import hackhub.exception.CalendarConnectionException;

/**
 * Interfaccia per l'integrazione con un sistema di Calendar esterno
 * (es. Google Calendar o qualsiasi servizio compatibile).
 * <p>
 * Astrae la comunicazione con l'attore esterno 'Sistema Calendar',
 * delegando a esso la creazione dell'evento e l'invio dell'invito al leader del team.
 * Usata nel caso d'uso 'Pianificare call con un team'.
 * <p>
 * Progettata per rispettare il principio DIP: il service dipende da questa interfaccia,
 * non dall'implementazione concreta. L'implementazione viene iniettata via costruttore.
 */
public interface CalendarService {

    /**
     * Richiede al sistema Calendar esterno la creazione di un evento per la call
     * e l'invio dell'invito al leader del team tramite email.
     *
     * @param dto         dati della call (data, ora, descrizione, idRichiesta)
     * @param emailLeader indirizzo email del leader del team a cui inviare l'invito
     * @return dati dell'evento creato restituiti dal sistema Calendar (link, id evento)
     * @throws CalendarConnectionException se non è possibile stabilire la connessione
     *                                     con il sistema Calendar esterno
     */
    public DatiEventoEsterno creaEventoCalendar(CallCreateDTO dto, String emailLeader);
}
