package hackhub.repository;

import hackhub.model.StatoSegnalazione;
import hackhub.model.entity.Segnalazione;

import java.util.List;
import java.util.UUID;

public interface SegnalazioneRepository extends GenericRepository<Segnalazione, UUID> {

    /**
     * Persiste una nuova segnalazione e imposta l'id generato dal DB sull'entità.
     */
    public void save(Segnalazione segnalazione);

    /**
     * Restituisce tutte le segnalazioni relative a un determinato hackathon.
     */
    public List<Segnalazione> findByHackathon(UUID idHackathon);

    /**
     * Aggiorna lo stato di una segnalazione nel database.
     * Chiamato dal service dopo che l'organizzatore ha preso una decisione.
     */
    public void updateStato(UUID idSegnalazione, StatoSegnalazione stato);
}
