package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Segnalazione;
import hackhub.repository.SegnalazioneRepository;

import java.util.Optional;
import java.util.UUID;

public class JdbcSegnalazioneRepository implements SegnalazioneRepository {

    private final DBConnection dbConnection;

    public JdbcSegnalazioneRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Segnalazione> findById(UUID segnalazioneId) {
        // TODO: Implementare nel caso d'uso 'Segnalare violazione del regolamento'
        return Optional.empty();
    }

    @Override
    public void save(Segnalazione segnalazione) {
        // TODO: Implementare nel caso d'uso 'Segnalare violazione del regolamento'
    }
}
