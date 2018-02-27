package aim4.vehicle.rim;

import aim4.config.Debug;
import aim4.driver.rim.ProxyDriver;
import aim4.im.rim.IntersectionManager;
import aim4.map.lane.Lane;
import aim4.msg.rim.i2v.Confirm;
import aim4.msg.rim.i2v.I2VMessage;
import aim4.msg.rim.i2v.Reject;
import aim4.msg.rim.udp.*;
import aim4.msg.rim.v2i.Cancel;
import aim4.msg.rim.v2i.Done;
import aim4.msg.rim.v2i.Request;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.vehicle.BasicVehicle;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.VehicleUtil;

import java.awt.geom.Point2D;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.List;

/**
 * The proxy vehicle.
 */
public class ProxyVehicle extends RIMBasicAutoVehicle implements ProxyVehicleSimModel{
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The extra time to be added to the arrival time span of the request message.
     */
    private static final double DEFAULT_ARRIVAL_TIME_DELAY = 0.04;

    /**
     * Whether to send the intervalometer reading to the real vehicle from time
     * to time.
     */
    private static final boolean IS_SEND_INTERVALOMETER_READING = false;

    /**
     * The period of time between the sending of intervalometer reading.
     */
    private static final double SEND_INTERVALOMETER_READING_PERIOD = 1.0;


    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The socket address */
    SocketAddress sa;
    /** The last time stamp */
    private double lastTimeStamp;
    /** The next request Id */
    private int nextRequestId;
    /** The PVUpdate for next move() */
    private Real2ProxyPVUpdate pvUpdate;
    /** The next intervalometer reading time */
    private double nextIntervalometerReadingTime;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a proxy vehicle.
     *
     * @param pos             the position
     * @param heading         the heading
     * @param steeringAngle   the steering angle
     * @param velocity        the velocity
     * @param targetVelocity  the target velocity
     * @param acceleration    the acceleration
     * @param currentTime     the current time
     */
    public ProxyVehicle(Point2D pos,
                        double heading,
                        double steeringAngle,
                        double velocity,
                        double targetVelocity,
                        double acceleration,
                        double currentTime) {
        super(VehicleSpecDatabase.getVehicleSpecByName("MARVIN"), pos, heading,
                steeringAngle, velocity, targetVelocity, acceleration, currentTime);
        driver = null;
        sa = null;
        lastTimeStamp = Double.MIN_VALUE;
        nextRequestId = 0;
        pvUpdate = null;
        nextIntervalometerReadingTime = currentTime;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // driver

    /**
     * {@inheritDoc}
     */
    @Override
    public ProxyDriver getDriver() {
        return (ProxyDriver) super.getDriver();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setDriver(ProxyDriver driver) {
        super.setDriver(driver);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // the socket address

    /**
     * {@inheritDoc}
     */
    @Override
    public SocketAddress getSa() {
        return sa;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSa(SocketAddress sa) {
        this.sa = sa;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // processing the incoming Real2ProxyMsg and outgoing I2VMessages

    /**
     * {@inheritDoc}
     */
    @Override
    public void processReal2ProxyMsg(Real2ProxyMsg msg) {
        if (lastTimeStamp <= msg.receivedTime) {
            // TODO: maybe currentRIM should be put in ProxyDriver.
            IntersectionManager currentRIM =
                    getDriver().getCurrentLane().getLaneRIM()
                            .nextIntersectionManager(gaugePosition());

            V2IMessage v2iMsg = null;
            switch (msg.messageType) {
                case PV_UPDATE:
                    pvUpdate = (Real2ProxyPVUpdate)msg;
                    break;
                case REQUEST:
                    v2iMsg = convertReal2ProxyRequestToRequest((Real2ProxyRequest)msg);
                    break;
                case CANCEL:
                    v2iMsg = new Cancel(getVIN(), currentRIM.getId(),
                            ((Real2ProxyCancel)msg).reservationId);
                    break;
                case DONE:
                    v2iMsg = new Done(getVIN(), currentRIM.getId(), 0);
                    break;
                default:
                    assert (false):"Unknown message Real2ProxyMsg type";
            }

            if (v2iMsg != null) {
                if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
                    System.err.printf("Proxy vehicle is sending a V2I message " +
                            "to IM: %s\n.", v2iMsg);
                }
                super.send(v2iMsg);  // this will put the message to the outgoing queue
            }
            lastTimeStamp = msg.receivedTime;
        } else {
            if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
                System.err.printf("The incoming Real2Proxy messages are out of " +
                        "order.\n");
                System.err.printf("lastTimeStamp = %.2f\n", lastTimeStamp);
                System.err.printf("msg.receivedTime = %.2f\n", msg.receivedTime);
            }
            // do nothing else
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void receive(I2VMessage msg) {
        // super.receive(msg);  // do not call this!
        bitsReceived += msg.getSize();

        DatagramPacket dp = null;
        switch(msg.getMessageType()) {
            case CONFIRM:
                try {
                    dp = Proxy2RealAdapter.toDatagramPacket((Confirm)msg, sa, gaugeTime());
                } catch (IOException e) {
                    System.err.println("Failed to convert confirm message to a datagram");
                    e.printStackTrace();
                }
                if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
                    System.err.printf("Sending a confirm message to real vehicle: %s\n",
                            (Confirm)msg);
                }
                break;
            case REJECT:
                try {
                    dp = Proxy2RealAdapter.toDatagramPacket((Reject)msg, sa, gaugeTime());
                } catch (IOException e) {
                    System.err.println("Failed to convert reject message to a datagram");
                    e.printStackTrace();
                }
                if (Debug.SHOW_PROXY_VEHICLE_DEBUG_MSG) {
                    System.err.printf("Sending a reject message to real vehicle: %s\n",
                            (Reject)msg);
                }
                break;
            default:
                assert (false):("Cannot create the UdpAdaptor for a I2VMessage because " +
                        "the adaptor message has not been implemented yet");
        }

        try {
            DatagramSocket ds = new DatagramSocket();
            ds.send(dp);
            ds.close();
        } catch (IOException e) {
            System.err.println("Failed to send a datagram to a real vehicle.");
            e.printStackTrace();
        }
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // control

    /**
     * {@inheritDoc}
     */
    @Override
    public void move(double timeStep) {
        if (IS_SEND_INTERVALOMETER_READING) {
            if (currentTime >= nextIntervalometerReadingTime) {
                System.err.printf("Try sending the intervalometer reading %.2f to " +
                        "Marvin...\n", getIntervalometer().read());
                DatagramPacket dp = null;
                try {
                    dp = Proxy2RealAdapter.toDatagramPacket(getIntervalometer().read(),
                            sa, gaugeTime());
                } catch (IOException e) {
                    System.err.println("Failed to convert getIntervalometer().read() " +
                            "message to a datagram");
                    e.printStackTrace();
                }
                try {
                    DatagramSocket ds = new DatagramSocket();
                    ds.send(dp);
                    ds.close();
                    System.err.printf("intervalometer reading sent.\n");
                } catch (IOException e) {
                    System.err.println("Failed to send a datagram to a real vehicle.");
                    e.printStackTrace();
                }
                nextIntervalometerReadingTime =
                        currentTime + SEND_INTERVALOMETER_READING_PERIOD;
            }
        }

        if (pvUpdate == null) {
            super.move(timeStep);
        } else {
            // time difference
            if (lastUpdateTime >= 0.0) {
                double timeDiff = currentTime - lastUpdateTime;
                double xDiff = pvUpdate.position.getX()- movement.getPosition().getX();
                double yDiff = pvUpdate.position.getY()- movement.getPosition().getY();
                double dist = Math.sqrt(xDiff * xDiff + yDiff * yDiff);
                double actualVelocity = dist / timeDiff;
                //System.err.printf("Reported velocity = %.5f\n", pvUpdate.velocity);
                //System.err.printf("Velocity in sim.  = %.5f\n\n", actualVelocity);
                pvUpdate.velocity = actualVelocity;
            }
            lastUpdateTime = currentTime;
            updateState(pvUpdate);
            pvUpdate = null;
            currentTime += timeStep;
        }
    }

    /** The last update time */
    private double lastUpdateTime = -1.0;

    /**
     * Update the state of the vehicle according to the PV Update message.
     *
     * @param up  the PV update message
     */
    private void updateState(Real2ProxyPVUpdate up) {
        // System.err.printf("Update proxy vehicle state: %s\n", up);
        this.movement =
                new BasicVehicle.MoveToTargetVelocityMovement(spec,
                        up.position,
                        up.heading,
                        up.velocity,
                        up.steeringAngle,
                        up.acceleration,
                        up.targetVelocity);
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /**
     * Create a request message from the Real2ProxyRequest message object.
     * Increment nextRequestId as well.
     */
    private Request convertReal2ProxyRequestToRequest(Real2ProxyRequest msg) {
        assert msg.vin == vin;  // the VIN number must match up

        // TODO: should be in ProxyDriver
        IntersectionManager currentRIM =
                getDriver().getCurrentLane().getLaneRIM()
                        .nextIntersectionManager(gaugePosition());

        Lane arrivalLane = getDriver().getCurrentLane();
        Lane departureLane = getDriver().getDestination().getExitApproachLane();
        double maxTurnVelocity = VehicleUtil.maxTurnVelocity(spec,
                                  arrivalLane,
                                  departureLane,
                                  currentRIM);

        List<Request.Proposal> proposals = new LinkedList<Request.Proposal>();
        proposals.add(new Request.Proposal(arrivalLane.getId(),
                msg.departureLaneId,
                msg.receivedTime + msg.arrivalTimeSpan
                        + DEFAULT_ARRIVAL_TIME_DELAY,
                movement.getVelocity(),
                //msg.arrivalVelocity,
                maxTurnVelocity));

        System.err.printf("msg.arrivalVelocity = %.5f\n", msg.arrivalVelocity);
        System.err.printf("this.velocity       = %.5f\n\n", movement.getVelocity());

        Request request =
                new Request(vin, // sourceID
                        currentRIM.getId(), // destinationID
                        nextRequestId,
                        new Request.VehicleSpecForRequestMsg(spec),
                        proposals);

        nextRequestId++;
        return request;
    }
}
