package aim4.msg.rim.udp;

import aim4.config.Constants;
import aim4.vehicle.AccelSchedule;

import java.awt.geom.Point2D;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * A real vehicle to proxy vehicle message for PV update message.
 */
public class Real2ProxyPVUpdate extends Real2ProxyMsg{
    /** The vehicle's identification number. */
    public final int vin;

    // the current state of the vehicle

    /**
     * The position of the vehicle, represented by the point at
     * the center of the front of the Vehicle.
     */
    public final Point2D position;
    /** The direction of the vehicle */
    public final double heading;
    /** The steering angle */
    public final double steeringAngle;
    /** The velocity of the vehicle */
    public double velocity;
    /** The velocity at which the driver would like to be traveling. */
    public final double targetVelocity;
    /** The acceleration of the vehicle */
    public final double acceleration;
    /** The acceleration profile */
    public final AccelSchedule accelProfile;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a real vehicle to proxy vehicle message for PV update message.
     *
     * @param dis           the I/O stream
     * @param receivedTime  the time stamp
     * @throws IOException
     */
    public Real2ProxyPVUpdate(DataInputStream dis, double receivedTime)
            throws IOException {
        super(Type.PV_UPDATE, receivedTime);

        // Read the new values
        vin = dis.readInt();
        double x = (double)dis.readFloat();
        double y = (double)dis.readFloat();
        position = new Point2D.Double(x, y);
        heading =  (double)dis.readFloat();
        steeringAngle = (double)dis.readFloat();
        velocity = (double)dis.readFloat();
        targetVelocity = (double)dis.readFloat();
        acceleration = (double)dis.readFloat();
        accelProfile = null;
        // TODO: Marvin can't generate accelProfile yet. Thus, just leave it null
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        String s = "Real2ProxyPVUpdate(";
        s += "vin=" + vin + ", ";
        s += "receivedTime=" + Constants.TWO_DEC.format(receivedTime) + ", ";
        s +=
                "position=(" + Constants.TWO_DEC.format(position.getX()) + ","
                        + Constants.TWO_DEC.format(position.getY()) + "), ";
        s += "heading=" + Constants.TWO_DEC.format(heading) + ", ";
        s += "steeringAngle=" + Constants.TWO_DEC.format(steeringAngle) + ", ";
        s += "velocity=" + Constants.TWO_DEC.format(velocity) + ", ";
        s += "targetVelocity=" + Constants.TWO_DEC.format(targetVelocity) + ", ";
        s += "acceleration=" + Constants.TWO_DEC.format(acceleration) + ", ";
        s += "accelProfile=" + accelProfile;
        s += ")";
        return s;
    }
}
