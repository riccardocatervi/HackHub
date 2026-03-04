package hackhub.repository;

import hackhub.model.entity.Valutazione;

import java.util.Optional;
import java.util.UUID;

public interface ValutazioneRepository {
    Optional<Valutazione> findById(UUID valutazioneId);
    boolean existsBySottomissione(UUID idSottomissione);
    void save(Valutazione valutazione);
}
