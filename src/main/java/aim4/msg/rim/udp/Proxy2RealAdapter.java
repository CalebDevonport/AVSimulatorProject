package aim4.msg.rim.udp;

import aim4.msg.rim.i2v.Confirm;
import aim4.msg.rim.i2v.Reject;
import aim4.msg.rim.udp.UdpHeader.UdpMessageType;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.SocketAddress;

/**
 * The proxy vehicle to real vehicle message adapter.
 */
public abstract class Proxy2RealAdapter {
    /**
     * Construct a DatagramPacket of this confirm message
     *
     * @param sa           SocketAddress of the intended destination of the
     *                     datagram
     * @param currentTime  absolute time in seconds
     *
     * @return a DatagramPacket object which can be sent over UDP representing
     *         this message
     * @throws IOException
     */
    public static DatagramPacket toDatagramPacket(Confirm msg,
                                                  SocketAddress sa,
                                                  double currentTime)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos =
                getDosWithHeader(baos, currentTime, UdpMessageType.I2V_Confirm);
        assert dos.size() == UdpHeader.LENGTH;

        dos.writeInt(msg.getReservationId());
        // arrival_time is relative
        dos.writeFloat((float) (msg.getArrivalTime() - currentTime));
        dos.writeFloat((float) msg.getEarlyError());
        dos.writeFloat((float) msg.getLateError());
        dos.writeFloat((float) msg.getArrivalVelocity());
        double accel = msg.getAccelerationProfile().peek()[0];
        // ignore other acceleration for now
        // TODO: fix it in the future
        dos.writeFloat((float) accel);
//    System.out.printf("I2V_Confirm: acceleration for car to use is %.2f\n",
//                      accel);

        int udpPacketSize = UdpHeader.LENGTH + 24;
        assert (baos.size() == udpPacketSize);
        DatagramPacket dp =
                new DatagramPacket(baos.toByteArray(), udpPacketSize, sa);
        return dp;
    }

    /**
     * Construct a DatagramPacket of this reject message
     *
     * @param sa           SocketAddress of the intended destination of the
     *                     datagram
     * @param currentTime  absolute time in seconds
     *
     * @return a DatagramPacket object which can be sent over UDP representing
     *         this message
     * @throws IOException
     */
    public static DatagramPacket toDatagramPacket(Reject msg,
                                                  SocketAddress sa,
                                                  double currentTime)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos =
                getDosWithHeader(baos, currentTime, UdpMessageType.I2V_Reject);
        assert dos.size() == UdpHeader.LENGTH;

        int udpPacketSize = UdpHeader.LENGTH;
        assert (baos.size() == udpPacketSize);
        DatagramPacket dp =
                new DatagramPacket(baos.toByteArray(), udpPacketSize, sa);
        return dp;
    }

    /**
     * Construct a DatagramPacket of this confirm message
     *
     * @param distToFrontVehicle  the distance of the vehicles in front
     * @param sa                  SocketAddress of the intended destination of the
     *                            datagram
     * @param currentTime  absolute time in seconds
     *
     * @return a DatagramPacket object which can be sent over UDP representing
     *         this message
     * @throws IOException
     */
    public static DatagramPacket toDatagramPacket(double distToFrontVehicle,
                                                  SocketAddress sa,
                                                  double currentTime)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos =
                getDosWithHeader(baos, currentTime,UdpMessageType.I2V_DistToFrontVehicle);
        assert dos.size() == UdpHeader.LENGTH;

        dos.writeFloat((float) distToFrontVehicle);

        int udpPacketSize = UdpHeader.LENGTH + 4;
        assert (baos.size() == udpPacketSize);
        DatagramPacket dp =
                new DatagramPacket(baos.toByteArray(), udpPacketSize, sa);
        return dp;
    }


    /**
     * Builds a header for the type of this message, and writes it to a new
     * DataOutputStream wrapped around a given ByteArrayOutputStream
     *
     * @param baos         The ByteArrayOutputStream
     * @param currentTime  The current, absolute time in seconds
     * @return A DataOutputStream object backed by the given baos containing a
     *         UdpHeader
     * @throws IOException
     */
    public static DataOutputStream getDosWithHeader(ByteArrayOutputStream baos,
                                                    double currentTime,
                                                    UdpMessageType type)
            throws IOException
    {
        DataOutputStream dos = new DataOutputStream(baos);
        UdpHeader header = new UdpHeader((float)currentTime, type);
        // TODO: compute and set the checksum
        header.writeToDataOutputStream(dos);
        return dos;
    }
}
