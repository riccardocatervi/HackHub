package hackhub.repository;

import hackhub.model.entity.MembroTeam;
import hackhub.model.entity.Team;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends GenericRepository<Team, UUID> {

    /**
     * Persiste un nuovo team e imposta l'id generato dal DB sull'entità.
     */
    public void save(Team team);

    /**
     * Trova il team di cui un utente è membro accettato per un determinato hackathon.
     * Usato per verificare che un utente non sia già in un team prima di inviare un invito.
     */
    public Optional<Team> findByHackathonAndMembro(UUID idHackathon, UUID idUtente);

    /**
     * Restituisce tutti i team registrati per un determinato hackathon.
     * Usato per popolare il modulo di segnalazione del mentore.
     */
    public List<Team> findByHackathon(UUID idHackathon);

    /**
     * Aggiunge un utente come membro effettivo del team (a seguito di accettazione invito).
     */
    public void addMembro(UUID idTeam, UUID idUtente);

    /**
     * Restituisce i membri effettivi (accettati) di un team,
     * con i dati anagrafici recuperati tramite JOIN con la tabella utente.
     */
    public List<MembroTeam> findMembri(UUID idTeam);

    /**
     * Aggiorna il flag di squalifica di un team nel database.
     * Chiamato dal service quando l'organizzatore accetta una segnalazione di violazione.
     */
    public void updateSqualificato(UUID idTeam, boolean squalificato);

    /**
     * Trova il team (se esiste) di cui un utente è membro accettato,
     * indipendentemente dall'hackathon. Restituisce il più recente in caso di più risultati.
     * Utilizzato per recuperare i dettagli di partecipazione corrente di un membro.
     */
    public Optional<Team> findByMembro(UUID idMembro);

    /**
     * Rimuove l'associazione tra un utente e un team (abbandono del team).
     * Opera sulla tabella membro_team.
     */
    public void removeMembro(UUID idTeam, UUID idMembro);

    /**
     * Aggiorna il leader di un team nel database.
     * Chiamato quando il leader corrente abbandona il team e viene eletto un nuovo leader.
     */
    public void updateLeader(UUID idTeam, UUID nuovoLeaderId);

    /**
     * Elimina definitivamente un team e tutte le sue associazioni di membri.
     * Chiamato durante la disiscrizione del team dall'hackathon.
     */
    public void delete(Team team);
}
