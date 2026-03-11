package hackhub.repository;

import hackhub.model.StatoInvito;
import hackhub.model.entity.Invito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitoRepository extends GenericRepository<Invito, UUID> {

    /** Persiste un nuovo invito e imposta l'id generato dal DB sull'entità. */
    public void save(Invito invito);

    /** Restituisce tutti gli inviti (in qualsiasi stato) associati a un team. */
    public List<Invito> findByTeam(UUID idTeam);

    /** Restituisce gli inviti in uno stato specifico per un determinato team. */
    public List<Invito> findByTeamAndStato(UUID idTeam, StatoInvito stato);

    /**
     * Restituisce l'invito inviato a un utente specifico per un determinato hackathon.
     * Utile per verificare se un utente ha già ricevuto un invito prima di inviarne un altro.
     */
    public Optional<Invito> findByUtenteAndHackathon(UUID idUtente, UUID idHackathon);

    /**
     * Aggiorna lo stato di un invito (es. da IN_ATTESA ad ACCETTATO o RIFIUTATO).
     */
    public void aggiornaStato(UUID idInvito, StatoInvito nuovoStato);

    /**
     * Conta quanti inviti risultano ACCETTATI per un team.
     * Utile per determinare quanti slot sono ancora disponibili.
     */
    public int countAccettati(UUID idTeam);
}
