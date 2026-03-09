package hackhub.repository;

import hackhub.model.entity.Segnalazione;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SegnalazioneRepository extends GenericRepository<Segnalazione, UUID>{

    /** Persiste una nuova segnalazione e imposta l'id generato dal DB sull'entità. */
    void save(Segnalazione segnalazione);

    /** Restituisce tutte le segnalazioni relative a un determinato hackathon. */
    List<Segnalazione> findByHackathon(UUID idHackathon);
}
