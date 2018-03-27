package aim4.msg.rim.udp;

import aim4.config.Constants;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A real vehicle to proxy vehicle message for request message.
 */
public class Real2ProxyRequest extends Real2ProxyMsg{
    /////////////////////////////////
    // PUBLIC FINAL FIELDS
    /////////////////////////////////

    /** The VIN of the vehicle */
    public final int vin;
    /** The arrival time span */
    public final float arrivalTimeSpan;
    /** The arrival velocity */
    public final float arrivalVelocity;
    /** The departure lane ID */
    public final int departureLaneId;
    /** The departure lane ID */
    public final boolean isStoppedAtIntersection;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a real vehicle to proxy vehicle message for request message.
     *
     * @param dis           the I/O stream
     * @param receivedTime  the time stamp
     * @throws IOException
     */
    public Real2ProxyRequest(DataInputStream dis, double receivedTime)
            throws IOException {
        super(Type.REQUEST, receivedTime);
        vin = dis.readInt();
        arrivalTimeSpan = dis.readFloat();
        departureLaneId = dis.readInt();
        arrivalVelocity = dis.readFloat();
        isStoppedAtIntersection = dis.readBoolean();
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String s = "Real2ProxyRequest(";
        s += "vin=" + vin + ",";
        s += "arrivalTimeSpan=" + Constants.TWO_DEC.format(arrivalTimeSpan) + ",";
        s += "arrivalVelocity=" + Constants.TWO_DEC.format(arrivalVelocity) + ",";
        s += "departureLaneId=" + departureLaneId;
        s += ")";
        return s;
    }
}
