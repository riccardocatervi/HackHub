package hackhub.dto;

/**
 * DTO con i dati dell'evento restituiti dal sistema Calendar esterno
 * a seguito della creazione di una call pianificata.
 * Usato nel caso d'uso 'Pianificare call con un team'.
 */
public record DatiEventoEsterno(
        String linkCall,
        String idEventoCalendar
) {
}
