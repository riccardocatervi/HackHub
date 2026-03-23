package hackhub.model;

/**
 * Livello di urgenza di una richiesta di supporto inviata da un team.
 * Guida il mentore nella prioritizzazione delle richieste da gestire.
 */
public enum LivelloUrgenza {

    /**
     * Problema non bloccante, può essere gestito con calma.
     */
    BASSA,

    /**
     * Livello di urgenza standard, usato come valore predefinito.
     */
    NORMALE,

    /**
     * Problema bloccante che richiede attenzione immediata.
     */
    ALTA
}
