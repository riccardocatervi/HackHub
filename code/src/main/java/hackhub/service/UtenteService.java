package hackhub.service;

import hackhub.dto.OAuthUserInfoDTO;
import hackhub.dto.UserRegistrationDTO;
import hackhub.dto.UserRegistrationResponseDTO;
import hackhub.exception.EmailAlreadyExistsException;
import hackhub.exception.ValidationException;
import hackhub.model.entity.Utente;
import hackhub.repository.UserRepository;

import java.util.regex.Pattern;

/**
 * Servizio per la gestione della registrazione degli utenti alla piattaforma.
 * Supporta due modalità:
 * <ul>
 *   <li>Registrazione locale con email e password</li>
 *   <li>Registrazione tramite OAuth2 (Google, GitHub)</li>
 * </ul>
 */
public class UtenteService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuovo utente tramite il modulo standard (email + password).
     *
     * @param dto dati del modulo di registrazione
     * @return DTO con i dati dell'utente creato
     * @throws ValidationException         se i dati del modulo non sono validi
     * @throws EmailAlreadyExistsException se l'email è già registrata
     */
    public UserRegistrationResponseDTO registra(UserRegistrationDTO dto) {
        validaCampiLocali(dto);

        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException(
                    "Esiste già un account associato all'indirizzo email: " + dto.email());
        }

        String hashedPassword = passwordEncoder.encode(dto.password());
        Utente utente = new Utente(dto.nome(), dto.cognome(), dto.email(), hashedPassword);
        userRepository.salva(utente);

        return new UserRegistrationResponseDTO(
                utente.getId(), utente.getNome(), utente.getCognome(), utente.getEmail());
    }

    /**
     * Registra (o recupera) un utente a seguito di autenticazione OAuth2 riuscita.
     * Se l'utente risulta già registrato con le stesse credenziali OAuth, viene
     * restituito il suo account esistente senza creare duplicati.
     *
     * @param dto informazioni utente restituite dal provider OAuth2
     * @return DTO con i dati dell'utente registrato o già esistente
     * @throws ValidationException         se le informazioni OAuth2 sono incomplete
     * @throws EmailAlreadyExistsException se l'email è già associata a un account locale
     */
    public UserRegistrationResponseDTO registraDaOAuth(OAuthUserInfoDTO dto) {
        validaCampiOAuth(dto);

        // Verifica se l'utente ha già un account OAuth con questo provider
        var esistente = userRepository.findByOAuth(dto.provider(), dto.externalId());
        if (esistente.isPresent()) {
            Utente u = esistente.get();
            return new UserRegistrationResponseDTO(u.getId(), u.getNome(), u.getCognome(), u.getEmail());
        }

        // Verifica conflitto email con account locale
        if (userRepository.existsByEmail(dto.email())) {
            throw new EmailAlreadyExistsException(
                    "L'email " + dto.email() + " è già associata a un account locale. " +
                            "Accedi con email e password oppure usa il recupero account.");
        }

        Utente utente = new Utente(dto.nome(), dto.cognome(), dto.email(),
                dto.provider(), dto.externalId());
        userRepository.salva(utente);

        return new UserRegistrationResponseDTO(
                utente.getId(), utente.getNome(), utente.getCognome(), utente.getEmail());
    }

    private void validaCampiLocali(UserRegistrationDTO dto) {
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new ValidationException("Il nome è obbligatorio.");
        }
        if (dto.cognome() == null || dto.cognome().isBlank()) {
            throw new ValidationException("Il cognome è obbligatorio.");
        }
        if (dto.email() == null || !EMAIL_PATTERN.matcher(dto.email()).matches()) {
            throw new ValidationException("Indirizzo email non valido.");
        }
        if (dto.password() == null || dto.password().length() < 8) {
            throw new ValidationException("La password deve contenere almeno 8 caratteri.");
        }
    }

    private void validaCampiOAuth(OAuthUserInfoDTO dto) {
        if (dto.email() == null || !EMAIL_PATTERN.matcher(dto.email()).matches()) {
            throw new ValidationException(
                    "Il provider OAuth2 non ha fornito un indirizzo email valido.");
        }
        if (dto.externalId() == null || dto.externalId().isBlank()) {
            throw new ValidationException(
                    "Il provider OAuth2 non ha fornito un identificativo utente valido.");
        }
        if (dto.nome() == null || dto.nome().isBlank()) {
            throw new ValidationException(
                    "Il provider OAuth2 non ha fornito il nome dell'utente.");
        }
    }
}
