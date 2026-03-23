package hackhub.rest;

import hackhub.exception.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestore centralizzato delle eccezioni per tutti i REST controller.
 * Mappa le eccezioni di dominio ai codici HTTP appropriati.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // --- 404 Not Found ---

    @ExceptionHandler({
            HackathonNotFoundException.class,
            TeamNotFoundException.class,
            UserNotFoundException.class,
            SottomissioneNotFoundException.class,
            SegnalazioneNotFoundException.class,
            CallNotFoundException.class,
            SupportRequestNotFoundException.class,
            InvitationNotFoundException.class,
            EntityNotFoundException.class,
            NoTeamsFoundException.class
    })
    public ResponseEntity<Map<String, Object>> handleNotFound(RuntimeException ex) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    // --- 409 Conflict (violazioni di stato o duplicati) ---

    @ExceptionHandler({
            InvalidHackathonStateException.class,
            IllegalStateTransitionException.class,
            InvalidCallStateException.class,
            InvalidInvitationStateException.class,
            InvalidRequestStateException.class,
            SottomissioneAlreadyExistsException.class,
            AlreadyEvaluatedException.class,
            UserAlreadyInTeamException.class,
            EmailAlreadyExistsException.class,
            SegnalazioneAlreadyManagedException.class
    })
    public ResponseEntity<Map<String, Object>> handleConflict(RuntimeException ex) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage());
    }

    // --- 400 Bad Request (validazione) ---

    @ExceptionHandler({
            ValidationException.class,
            IllegalArgumentException.class,
            MaxTeamSizeExceedException.class,
            TeamCapacityExceededException.class,
            UserNotEligibleException.class,
            NoMentorsAvailableException.class
    })
    public ResponseEntity<Map<String, Object>> handleBadRequest(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    // --- 401 Unauthorized ---

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(InvalidCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    // --- 403 Forbidden ---

    @ExceptionHandler(UnauthorizedActionException.class)
    public ResponseEntity<Map<String, Object>> handleForbidden(UnauthorizedActionException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, ex.getMessage());
    }

    // --- 502 Bad Gateway (errori di integrazione esterna) ---

    @ExceptionHandler({
            PaymentException.class,
            CalendarConnectionException.class
    })
    public ResponseEntity<Map<String, Object>> handleExternalServiceError(RuntimeException ex) {
        return buildResponse(HttpStatus.BAD_GATEWAY, ex.getMessage());
    }

    // --- 500 Internal Server Error (errori di persistenza e generici) ---

    @ExceptionHandler(PersistenceException.class)
    public ResponseEntity<Map<String, Object>> handlePersistence(PersistenceException ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    // --- Costruzione della risposta di errore ---

    private ResponseEntity<Map<String, Object>> buildResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return ResponseEntity.status(status).body(body);
    }
}
