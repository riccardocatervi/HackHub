package hackhub.repository;

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
}
