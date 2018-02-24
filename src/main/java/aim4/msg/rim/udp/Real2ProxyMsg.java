package aim4.msg.rim.udp;

/**
 * A real vehicle to proxy vehicle message.
 */
public abstract class Real2ProxyMsg {
    // ///////////////////////////////
    // NESTED CLASSES
    // ///////////////////////////////

    /**
     * The type of the message.
     */
    public enum Type {
        PV_UPDATE,
        REQUEST,
        CANCEL,
        DONE,
    };

    /////////////////////////////////
    // PUBLIC FINAL FIELDS
    /////////////////////////////////

    /** The type of this message. */
    public final Type messageType;

    /** the received time of this message */
    public final double receivedTime;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a real vehicle to proxy vehicle message.
     *
     * @param messageType   the message type
     * @param receivedTime  the time stamp
     */
    public Real2ProxyMsg(Type messageType, double receivedTime) {
        this.messageType = messageType;
        this.receivedTime = receivedTime;
    }
}
