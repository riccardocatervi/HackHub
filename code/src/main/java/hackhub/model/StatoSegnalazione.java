package hackhub.model;

/**
 * Enumerazione degli stati del ciclo di vita di una segnalazione di violazione.
 * <ul>
 *   <li>{@code PENDENTE}  — la segnalazione è in attesa di revisione da parte dell'organizzatore.</li>
 *   <li>{@code ACCETTATA} — l'organizzatore ha accettato la segnalazione; il team viene squalificato.</li>
 *   <li>{@code RIFIUTATA} — l'organizzatore ha rifiutato la segnalazione per prove insufficienti.</li>
 * </ul>
 */
public enum StatoSegnalazione {
    PENDENTE,
    ACCETTATA,
    RIFIUTATA
}
