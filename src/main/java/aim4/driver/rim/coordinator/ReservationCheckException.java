package aim4.driver.rim.coordinator;

/**
 * The exception for failure of reservation check.
 */
public class ReservationCheckException extends Exception{

    private static final long serialVersionUID = 1L;

    /**
     * Construct an reservation exception.
     *
     * @param message  the exception message
     */
    public ReservationCheckException(String message) {
        super(message);
    }
}
