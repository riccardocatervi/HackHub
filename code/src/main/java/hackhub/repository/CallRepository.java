package hackhub.repository;

import hackhub.model.entity.Call;

import java.util.List;
import java.util.UUID;

/**
 * Interfaccia repository per le call pianificate tra mentor e team.
 * Estende GenericRepository per ereditare il metodo findById.
 * Usata nel caso d'uso 'Pianificare call con un team'.
 */
public interface CallRepository extends GenericRepository<Call, UUID> {

    /**
     * Persiste una nuova call e imposta l'id generato dal DB sull'entità.
     *
     * @param call l'entità da salvare
     */
    public void save(Call call);

    /**
     * Recupera tutte le call associate a una richiesta di supporto.
     *
     * @param idRichiesta id della richiesta di supporto
     * @return lista delle call, eventualmente vuota
     */
    public List<Call> findByRichiesta(UUID idRichiesta);
}
