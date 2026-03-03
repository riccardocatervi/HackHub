package hackhub.repository;

import hackhub.model.entity.Sottomissione;

import java.util.Optional;
import java.util.UUID;

public interface SottomissioneRepository extends GenericRepository<Sottomissione, UUID> {

    /** Persiste una nuova sottomissione e imposta l'id generato dal DB sull'entità passata. */
    void save(Sottomissione sottomissione);

    /** Verifica se il team ha già inviato una sottomissione per l'hackathon specificato. */
    boolean existsByHackathonAndTeam(UUID idHackathon, UUID idTeam);

    /** Conta le sottomissioni prive di valutazione per l'hackathon specificato. */
    long countNonValutate(UUID idHackathon);

    /**
     * Trova la sottomissione con il voto più alto per l'hackathon.
     * Utilizzata per determinare il vincitore prima della proclamazione.
     */
    Optional<Sottomissione> findVincitore(UUID idHackathon);

    /** Imposta il flag vincitore = true sulla sottomissione specificata. */
    void markAsVincitore(UUID idSottomissione);
}
