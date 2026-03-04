package hackhub.repository;

import hackhub.model.entity.Segnalazione;

import java.util.Optional;
import java.util.UUID;

public interface SegnalazioneRepository {
    Optional<Segnalazione> findById(UUID segnalazioneId);
    void save(Segnalazione segnalazione);
}
