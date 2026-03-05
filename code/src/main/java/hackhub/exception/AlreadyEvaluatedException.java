package hackhub.exception;

public class AlreadyEvaluatedException extends RuntimeException {
    public AlreadyEvaluatedException(String message) {
        super(message);
    }
}
