package hackhub.model;

/**
 * Stato del ciclo di vita di un invito a unirsi a un team.
 */
public enum StatoInvito {

    /**
     * Invito inviato, in attesa di risposta da parte dell'utente invitato.
     */
    IN_ATTESA,

    /**
     * Invito accettato: l'utente è stato aggiunto come membro del team.
     */
    ACCETTATO,

    /**
     * Invito rifiutato: l'utente non è membro del team.
     */
    RIFIUTATO
}
