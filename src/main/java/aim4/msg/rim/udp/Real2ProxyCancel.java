package aim4.msg.rim.udp;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A real vehicle to proxy vehicle message for cancel message.
 */
public class Real2ProxyCancel extends Real2ProxyMsg {
    /////////////////////////////////
    // PUBLIC FINAL FIELDS
    /////////////////////////////////

    /** The reservation ID */
    public final int reservationId;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a real vehicle to proxy vehicle message for cancel message.
     *
     * @param dis           the I/O stream
     * @param receivedTime  the time stamp
     * @throws IOException
     */
    public Real2ProxyCancel(DataInputStream dis, double receivedTime)
            throws IOException {
        super(Type.REQUEST, receivedTime);
        reservationId = dis.readInt();
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Real2ProxyCancel()";
    }
}
