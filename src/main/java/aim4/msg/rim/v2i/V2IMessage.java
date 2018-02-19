package aim4.msg.rim.v2i;

import aim4.config.Constants;

/**
 * A message sent from a Vehicle to a RIM Intersection Manager.
 */
public abstract class V2IMessage {
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The different types of Vehicle to Intersection Manager
     * messages.
     */
    public enum Type {
        /** Mesage requesting a reservation or a change of reservation. */
        REQUEST,
        /** Message cancelling a currently held reservation. */
        CANCEL,
        /** Message indicating that the vehicle has traversed the intersection. */
        DONE,
        /** Message requesting entry into the admission control zone. */
        ACZ_REQUEST,
        /** Message cancelling a previous ACZ_REQUEST. */
        ACZ_CANCEL,
        /**
         * Message indicating the vehicle has completed entering the admission
         * control zone.
         */
        ACZ_ENTERED,
        /**
         * Message indicating the vehicle has left the admission control zone by
         * leaving the roadway.
         */
        ACZ_EXIT,
        /**
         * Message indicating the vehicle has left the admission control zone by
         * driving straight out of it.
         */
        AWAY,
    };

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The ID number of the Intersection Manager to which this message is
     * being sent.
     */
    private int imId;
    /**
     * The ID number of the Vehicle sending this message
     */
    private int vin;

    /////////////////////////////////
    // PROTECTED FIELDS
    /////////////////////////////////

    /** The type of this message. */
    protected Type messageType;

    /**
     * The size, in bits, of this message.
     */
    protected int size = Constants.ENUM_SIZE + 2 * Constants.INTEGER_SIZE;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Class constructor to be called by subclasses to set the source and
     * destination ID numbers.
     *
     * @param vin      the ID number of the Vehicle sending this message
     * @param imID the ID number of the IntersectionManager to which
     *                      this message is being sent
     */
    public V2IMessage(int vin, int imID) {
        this.vin = vin;
        this.imId = imID;
    }

    public V2IMessage(V2IMessage msg) {
        this.vin = msg.vin;
        this.imId = msg.imId;
        this.messageType = msg.messageType;
        this.size = msg.size;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the ID number of the Intersection Manager to which this
     * message is being sent.
     *
     * @return the ID number of the IntersectionManager to which this message is
     *         being sent
     */
    public int getImId() {
        return imId;
    }

    /**
     * Get the ID number of the Vehicle sending this message.
     *
     * @return the ID number of the Vehicle sending this message
     */
    public int getVin() {
        return vin;
    }

    /**
     * Get the type of this message.
     *
     * @return the type of this message
     */
    public Type getMessageType() {
        return messageType;
    }

    /**
     * Get the size of this message in bits.
     *
     * @return the size of this message in bits
     */
    public int getSize() {
        return size;
    }
}
