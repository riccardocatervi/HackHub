package hackhub.exception;

/**
 * Lanciata quando si tenta di rispondere a una call che non si trova
 * nello stato corretto (es. non è più PENDENTE).
 * Usata nel caso d'uso 'Gestire invito a call da parte di un mentore'.
 */
public class InvalidCallStateException extends RuntimeException {

    public InvalidCallStateException(String message) {
        super(message);
    }
}
