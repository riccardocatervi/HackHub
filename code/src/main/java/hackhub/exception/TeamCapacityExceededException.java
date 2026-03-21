package hackhub.exception;

import java.util.UUID;

/**
 * Lanciata quando il team ha raggiunto (o raggiungerà con gli inviti pendenti)
 * la dimensione massima consentita dall'hackathon.
 */
public class TeamCapacityExceededException extends RuntimeException {

    public TeamCapacityExceededException(UUID idTeam, int dimensioneMax) {
        super("Il team " + idTeam + " ha già raggiunto la capienza massima di " +
                dimensioneMax + " membri (membri effettivi + inviti pendenti).");
    }
}
