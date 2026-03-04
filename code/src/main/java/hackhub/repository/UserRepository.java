package hackhub.repository;

import hackhub.model.OAuthProvider;
import hackhub.model.entity.Utente;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends GenericRepository<Utente, UUID> {

    /** Cerca un utente per indirizzo email. */
    Optional<Utente> findByEmail(String email);

    /** Verifica se esiste già un utente con la stessa email. */
    boolean existsByEmail(String email);

    /** Cerca un utente tramite le credenziali OAuth2. */
    Optional<Utente> findByOAuth(OAuthProvider provider, String externalId);

    /**
     * Restituisce tutti gli utenti registrati che non fanno ancora parte
     * di alcun team (come membro accettato) per l'hackathon specificato.
     * Utilizzato per popolare la lista di utenti invitabili.
     */
    List<Utente> findAvailableForHackathon(UUID idHackathon);

    /**
     * Verifica se un utente è disponibile a ricevere inviti per un hackathon
     * (ovvero non è ancora membro accettato di nessun team per quell'hackathon).
     */
    boolean isUserAvailable(UUID idUtente, UUID idHackathon);

    /** Persiste un nuovo utente e imposta l'id generato dal DB sull'entità. */
    void salva(Utente utente);
}
