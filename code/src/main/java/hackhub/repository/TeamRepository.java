package hackhub.repository;

import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends GenericRepository<Team, UUID> {

    /** Persiste un nuovo team e imposta l'id generato dal DB sull'entità. */
    void save(Team team);

    /**
     * Trova il team di cui un utente è membro accettato per un determinato hackathon.
     * Usato per verificare che un utente non sia già in un team prima di inviare un invito.
     */
    Optional<Team> findByHackathonAndMembro(UUID idHackathon, UUID idUtente);

    /**
     * Aggiunge un utente come membro effettivo del team (a seguito di accettazione invito).
     */
    void addMembro(UUID idTeam, UUID idUtente);

    /**
     * Restituisce i membri effettivi (accettati) di un team,
     * con i dati anagrafici recuperati tramite JOIN con la tabella utente.
     */
    List<MembroTeam> findMembri(UUID idTeam);
}
