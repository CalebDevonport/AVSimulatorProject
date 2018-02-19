package aim4.msg.rim.i2v;

import aim4.config.Constants;

/**
 * A message sent from a RIM Intersection Manager to a Vehicle.
 */
public abstract class I2VMessage {
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The different types of Intersection Manager to
     * Vehicle messages.
     */
    public enum Type {
        /** Message confirming a reservation Request. */
        CONFIRM,
        /** Message rejecting a Request. */
        REJECT,
        /** Message granting a request to enter the admission control zone. */
        ACZ_CONFIRM,
        /** Message rejecting a request to enter the admission control zone. */
        ACZ_REJECT,
    };

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The ID number of the Vehicle to which this message is being sent.
     */
    private int vin;
    /**
     * The ID number of the Intersection Manager from which this message
     * is being sent.
     */
    private int imId;

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The type of this message.
     */
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
     * @param imId  the ID number of the IntersectionManager sending this message
     * @param vin   the ID number of the Vehicle to which this message is being
     *              sent
     */
    public I2VMessage(int imId, int vin) {
        this.imId = imId;
        this.vin = vin;
    }

    /**
     * Create a new copy of the message.
     *
     * @param msg  the message
     */
    public I2VMessage(I2VMessage msg) {
        this.imId = msg.imId;
        this.vin = msg.vin;
        this.messageType = msg.messageType;
        this.size = msg.size;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the ID number of the Vehicle to which this message is being
     * sent.
     *
     * @return the ID number of the Vehicle to which this message is being sent
     */
    public int getVin() {
        return vin;
    }

    /**
     * Get the ID number of the Intersection Manager sending this
     * message.
     *
     * @return the ID number of the IntersectionManager sending this message
     */
    public int getImId() {
        return imId;
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
