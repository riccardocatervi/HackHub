package hackhub.repository;

import hackhub.model.entity.Valutazione;

import java.util.Optional;
import java.util.UUID;

public interface ValutazioneRepository extends GenericRepository<Valutazione, UUID> {
    public boolean existsBySottomissione(UUID idSottomissione);
    public void save(Valutazione valutazione);
}
