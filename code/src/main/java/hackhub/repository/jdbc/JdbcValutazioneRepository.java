package hackhub.repository.jdbc;

import hackhub.infrastructure.db.DBConnection;
import hackhub.model.entity.Valutazione;
import hackhub.repository.ValutazioneRepository;

import java.util.Optional;
import java.util.UUID;

public class JdbcValutazioneRepository implements ValutazioneRepository {

    private final DBConnection dbConnection;

    public JdbcValutazioneRepository(DBConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    @Override
    public Optional<Valutazione> findById(UUID valutazioneId) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
        return Optional.empty();
    }

    @Override
    public boolean existsBySottomissione(UUID idSottomissione) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
        return false;
    }

    @Override
    public void save(Valutazione valutazione) {
        // TODO: Implementare nel caso d'uso 'Valutare sottomissione'
    }
}
