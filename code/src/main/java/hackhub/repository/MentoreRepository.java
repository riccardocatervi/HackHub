package hackhub.repository;

import hackhub.model.entity.Mentore;

import java.util.List;
import java.util.UUID;

public interface MentoreRepository extends GenericRepository<Mentore, UUID> {

    /** Restituisce tutti i mentori con disponibilità = true. */
    List<Mentore> findAllDisponibili();
}
