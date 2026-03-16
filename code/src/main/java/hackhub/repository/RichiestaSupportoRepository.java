package hackhub.repository;

import hackhub.model.StatoRichiesta;
import hackhub.model.entity.RichiestaSupporto;

import java.util.List;
import java.util.UUID;

/**
 * Interfaccia repository per le richieste di supporto.
 * Estende GenericRepository per ereditare il metodo findById.
 */
public interface RichiestaSupportoRepository extends GenericRepository<RichiestaSupporto, UUID> {

    /**
     * Persiste una nuova richiesta di supporto e imposta l'id generato dal DB sull'entità.
     *
     * @param richiesta l'entità da salvare
     */
    public void save(RichiestaSupporto richiesta);

    /**
     * Recupera tutte le richieste di supporto per un hackathon.
     *
     * @param idHackathon id dell'hackathon
     * @return lista delle richieste, eventualmente vuota
     */
    public List<RichiestaSupporto> findByHackathon(UUID idHackathon);

    /**
     * Recupera tutte le richieste di supporto di un team specifico.
     *
     * @param idTeam id del team
     * @return lista delle richieste, eventualmente vuota
     */
    public List<RichiestaSupporto> findByTeam(UUID idTeam);

    // -------------------------------------------------------------------------
    // Metodi aggiunti in it.4 per il caso d'uso 'Prendere in carico una richiesta'
    // -------------------------------------------------------------------------

    /**
     * Recupera tutte le richieste di supporto assegnate a un mentore,
     * indipendentemente dallo stato.
     *
     * @param idMentore id del mentore
     * @return lista delle richieste, eventualmente vuota
     */
    public List<RichiestaSupporto> findByMentore(UUID idMentore);

    /**
     * Recupera le richieste di supporto assegnate a un mentore
     * che si trovano ancora nello stato PENDENTE.
     *
     * @param idMentore id del mentore
     * @return lista delle richieste pendenti, eventualmente vuota
     */
    public List<RichiestaSupporto> findPendingByMentore(UUID idMentore);

    /**
     * Aggiorna lo stato di una richiesta di supporto nel database.
     * Utilizzato per la transizione a PRESA_IN_CARICO o RESPINTA.
     *
     * @param idRichiesta id della richiesta da aggiornare
     * @param stato       nuovo stato da impostare
     */
    public void aggiornaStato(UUID idRichiesta, StatoRichiesta stato);

    /**
     * Aggiorna contestualmente lo stato e la motivazione di rifiuto di una richiesta.
     * Utilizzato quando il mentore rifiuta la richiesta con motivazione.
     *
     * @param idRichiesta id della richiesta da aggiornare
     * @param stato       nuovo stato (tipicamente RESPINTA)
     * @param motivazione motivazione del rifiuto
     */
    public void aggiornaStatoEMotivazione(UUID idRichiesta, StatoRichiesta stato, String motivazione);
}
