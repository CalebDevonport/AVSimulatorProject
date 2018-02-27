package aim4.driver.rim.coordinator;

/**
 * The exception for wrong arrival estimation.
 */
public class ArrivalEstimationException extends Exception{

    private static final long serialVersionUID = 1L;

    /**
     * Construct an arrival estimation exception.
     *
     * @param message  the exception message
     */
    public ArrivalEstimationException(String message) {
        super(message);
    }
}
