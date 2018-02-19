package aim4.msg.rim.i2v;

import aim4.config.Constants;

/**
 * Message sent from a RIM Intersection Manager to a Vehicle to reject a
 * reservation request.
 */
public class Reject extends I2VMessage{
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * Some of the possible reasons that a vehicle may have a reservation
     * rejected.
     */
    public enum Reason {

        // normal reason for rejection (no room in the intersection)

        /**
         * The intersection could not find a clear path through the intersection
         * using the parameters supplied.
         */
        NO_CLEAR_PATH,

        // rejection to due to previous request.

        /**
         * The intersection manager has confirmed another request and cannot
         * guest a new request.
         */
        CONFIRMED_ANOTHER_REQUEST,  // TODO: think about whether we can remove this

        // rejection due to poor timing

        /**
         * The arrival time requested in the reservation parameters is too far in
         * the future.
         */
        ARRIVAL_TIME_TOO_LARGE,
        /**
         * The arrival time requested in the reservation parameters is before the
         * current time.
         */
        ARRIVAL_TIME_TOO_LATE,

        // rejection due to the timeout heuristic

        /**
         * This vehicle is still "timed out" from its last transmission and must
         * wait to transmit again (i.e., the vehicle cannot send the request before
         * the next allowed communication time in the previous reject messages.)
         */
        BEFORE_NEXT_ALLOWED_COMM,
    };

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The ID of the request message that this reject message corresponding
     * to.  More precisely, this confirm message is a reply to all the request
     * messages whose request id is equal to or less than this requestId,
     * and larger the requestId of last confirm message.
     */
    private int requestId;

    /**
     * The next time that communication from this Vehicle will be considered.
     */
    private double nextAllowedCommunication;

    /**
     * The reason this Reject message is being sent.
     */
    private Reason reason;


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Constructor with specific reason for rejection and limit on when
     * the next acceptable transmission is.
     *
     * @param sourceID                 the ID number of the IntersectionManager
     *                                 sending this message
     * @param destinationID            the ID number of the Vehicle to which
     *                                 this message is being sent
     * @param requestId                the request id of the request message
     *                                 this reject message corresponds to
     * @param nextAllowedCommunication the time after which communication will
     *                                 accepted by the IntersectionManager
     * @param reason                   the reason this Reject message is being
     *                                 sent
     */
    public Reject(int sourceID, int destinationID,
                  int requestId,
                  double nextAllowedCommunication,
                  Reason reason) {
        // Set the source and destination
        super(sourceID, destinationID);
        this.requestId = requestId;
        this.nextAllowedCommunication = nextAllowedCommunication;
        this.reason = reason;
        messageType = Type.REJECT;
        size += Constants.ENUM_SIZE + Constants.DOUBLE_SIZE;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the request ID of the request message this reject message correspond
     * to.
     *
     * @return the id of the request message.
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * Get the time after which communication will again be accepted by the
     * IntersectionManager.
     *
     * @return the time after which communication will be accepted by the
     *         IntersectionManager
     */
    public double getNextAllowedCommunication() {
        return nextAllowedCommunication;
    }

    /**
     * Get the reason that this Reject message was sent.
     *
     * @return the reason this Reject message was sent
     */
    public Reason getReason() {
        return reason;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Reject(im" + getImId() + " -> vin" + getVin()
                + ", requestID=" + requestId
                + ", nextcomm:" + String.format("%.2f", nextAllowedCommunication)
                + ", reason:" + reason
                + ")";
    }
}
