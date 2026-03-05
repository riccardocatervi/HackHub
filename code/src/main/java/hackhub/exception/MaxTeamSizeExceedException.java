package hackhub.exception;

public class MaxTeamSizeExceedException extends RuntimeException {
    public MaxTeamSizeExceedException(String message) {
        super(message);
    }
}
