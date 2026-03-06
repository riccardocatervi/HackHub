package hackhub.repository;

import hackhub.model.entity.Valutazione;

import java.util.Optional;
import java.util.UUID;

public interface ValutazioneRepository extends GenericRepository<Valutazione, UUID> {
    boolean existsBySottomissione(UUID idSottomissione);
    void save(Valutazione valutazione);
}
