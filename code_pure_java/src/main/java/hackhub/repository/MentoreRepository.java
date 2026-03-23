package hackhub.repository;

import hackhub.model.entity.Mentore;

import java.util.List;
import java.util.UUID;

public interface MentoreRepository extends GenericRepository<Mentore, UUID> {

    /**
     * Restituisce tutti i mentori con disponibilità = true.
     */
    public List<Mentore> findAllDisponibili();

    /**
     * Restituisce tutti i mentori NON ancora assegnati all'hackathon specificato.
     * Utilizzato per popolare la lista dei mentori aggiungibili nel caso d'uso
     * 'Gestire mentori di un hackathon'.
     *
     * @param idHackathon l'id dell'hackathon di riferimento
     * @return lista dei mentori disponibili per l'aggiunta
     */
    public List<Mentore> findAllAvailable(UUID idHackathon);

    /**
     * Recupera le istanze dei mentori corrispondenti agli id forniti.
     * Utilizzato per validare e notificare i mentori aggiunti o rimossi.
     *
     * @param ids lista degli id dei mentori da recuperare
     * @return lista delle entità Mentore trovate
     */
    public List<Mentore> findAllByIds(List<UUID> ids);
}
