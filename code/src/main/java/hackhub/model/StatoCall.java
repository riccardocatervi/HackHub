package hackhub.model;

/**
 * Enumera i possibili stati di una call pianificata tra mentore e team.
 * Usato nel caso d'uso 'Gestire invito a call da parte di un mentore'.
 */
public enum StatoCall {

    /**
     * La call è stata pianificata dal mentore e attende risposta dal leader del team.
     */
    PENDENTE,

    /**
     * Il leader del team ha accettato l'invito alla call.
     */
    ACCETTATA,

    /**
     * Il leader del team ha rifiutato l'invito alla call.
     */
    RIFIUTATA
}
