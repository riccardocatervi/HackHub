package hackhub.repository;

import hackhub.model.StatoInvito;
import hackhub.model.entity.Invito;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvitoRepository extends GenericRepository<Invito, UUID> {

    /**
     * Persiste un nuovo invito e imposta l'id generato dal DB sull'entità.
     */
    public void save(Invito invito);

    /**
     * Restituisce tutti gli inviti (in qualsiasi stato) associati a un team.
     */
    public List<Invito> findByTeam(UUID idTeam);

    /**
     * Restituisce gli inviti in uno stato specifico per un determinato team.
     */
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

    /**
     * Restituisce tutti gli inviti in stato IN_ATTESA per un determinato utente.
     * Utilizzato per mostrare la lista degli inviti pendenti all'utente autenticato.
     */
    public List<Invito> findPendingByUtente(UUID idUtente);

    /**
     * Elimina tutti gli inviti associati a un team.
     * Chiamato durante la disiscrizione del team dall'hackathon, prima della cancellazione del team.
     */
    public void deleteByTeam(UUID idTeam);

    /**
     * Conta gli inviti in stato IN_ATTESA per un determinato team.
     * Utilizzato per verificare la capienza del team prima di inviare un nuovo invito.
     *
     * @param idTeam l'id del team
     * @return il numero di inviti pendenti
     */
    public int countPendingByTeam(UUID idTeam);

    /**
     * Verifica se esiste già un invito in un certo stato per un utente in un determinato team.
     * Utilizzato per evitare inviti duplicati.
     *
     * @param idUtente l'id dell'utente
     * @param idTeam   l'id del team
     * @param stato    lo stato da verificare
     * @return true se esiste già un invito con quel stato, false altrimenti
     */
    public boolean existsByUtenteAndTeamAndStato(UUID idUtente, UUID idTeam, StatoInvito stato);
}
