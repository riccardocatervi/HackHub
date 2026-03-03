package hackhub.model.state;

/**
 * Enum dei possibili stati del ciclo di vita di un Hackathon.
 * Funge da factory per la ricostruzione dello stato dal database.
 */
public enum StatoHackathon {

    IN_ISCRIZIONE,
    IN_CORSO,
    IN_VALUTAZIONE,
    CONCLUSO;

    /**
     * Factory method: crea l'istanza dello State concreto corrispondente a questo enum.
     * Utilizzato dal repository JDBC per ricostruire l'oggetto stato dal valore stringa nel DB.
     */
    public HackathonState creaIstanza() {
        return switch (this) {
            case IN_ISCRIZIONE  -> new StatoInIscrizione();
            case IN_CORSO       -> new StatoInCorso();
            case IN_VALUTAZIONE -> new StatoInValutazione();
            case CONCLUSO       -> new StatoConcluso();
        };
    }
}
