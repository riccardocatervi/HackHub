package hackhub.repository;

import hackhub.model.entity.Hackathon;
import hackhub.model.state.StatoHackathon;

import java.util.List;
import java.util.UUID;

public interface HackathonRepository extends GenericRepository<Hackathon, UUID> {

    /** Persiste un nuovo hackathon e imposta l'id generato dal DB sull'entità passata. */
    void save(Hackathon hackathon);

    /** Aggiorna il campo 'stato' di un hackathon nel database. */
    void updateStato(UUID id, StatoHackathon stato);

    /**
     * Restituisce tutti gli hackathon in fase di iscrizione aperta.
     * Utilizzato per mostrare agli utenti registrati i contest disponibili.
     */
    List<Hackathon> findAllInIscrizione();
}
