package aim4.msg.rim.udp;

import java.io.DataInputStream;
import java.io.IOException;

/**
 * A real vehicle to proxy vehicle message for done message.
 */
public class Real2ProxyDone extends Real2ProxyMsg{
    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a real vehicle to proxy vehicle message for done message.
     *
     * @param dis           the I/O stream
     * @param receivedTime  the time stamp
     * @throws IOException
     */
    public Real2ProxyDone(DataInputStream dis, double receivedTime)
            throws IOException {
        super(Type.DONE, receivedTime);
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Real2ProxyDone()";
    }
}
