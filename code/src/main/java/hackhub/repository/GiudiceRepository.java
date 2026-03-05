package hackhub.repository;

import hackhub.model.entity.Giudice;

import java.util.List;
import java.util.UUID;

public interface GiudiceRepository extends GenericRepository<Giudice, UUID> {

    /**
     * Restituisce tutti i giudici con disponibilità = true.
     */
    List<Giudice> findAllDisponibili();
}
