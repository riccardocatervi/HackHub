package hackhub.repository;

import hackhub.model.entity.Team;

import java.util.Optional;
import java.util.UUID;

public interface TeamRepository extends GenericRepository<Team, UUID> {

    /** Trova il team a cui appartiene un membro specifico all'interno di un hackathon. */
    Optional<Team> findByHackathonAndMembro(UUID idHackathon, UUID idMembro);
}
