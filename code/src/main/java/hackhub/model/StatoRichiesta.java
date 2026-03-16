package hackhub.model;

/**
 * Stato del ciclo di vita di una richiesta di supporto inviata da un team a un mentore.
 * Usato nel caso d'uso 'Prendere in carico una richiesta di supporto'.
 */
public enum StatoRichiesta {

    /**
     * La richiesta è stata inviata ed è in attesa di valutazione da parte del mentore.
     */
    PENDENTE,

    /**
     * Il mentore ha accettato la richiesta e la sta gestendo.
     */
    PRESA_IN_CARICO,

    /**
     * Il mentore ha rifiutato la richiesta, fornendo una motivazione.
     */
    RESPINTA
}
