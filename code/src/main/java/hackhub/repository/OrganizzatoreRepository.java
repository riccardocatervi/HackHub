package hackhub.repository;

import hackhub.model.entity.Organizzatore;

import java.util.UUID;

public interface OrganizzatoreRepository extends GenericRepository<Organizzatore, UUID> {
    // findById(UUID) ereditato da GenericRepository
}
