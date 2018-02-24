package aim4.vehicle.rim;

import aim4.driver.rim.ProxyDriver;
import aim4.msg.rim.udp.Real2ProxyMsg;

import java.net.SocketAddress;

/**
 * The interface of a proxy vehicle from the viewpoint of a simulator.
 */
public interface ProxyVehicleSimModel extends RIMAutoVehicleSimModel{
    /**
     * {@inheritDoc}
     */
    @Override
    ProxyDriver getDriver();

    /**
     * Set this proxy vehicle's driver.
     *
     * @param driver  the new driver to control this Vehicle
     */
    void setDriver(ProxyDriver driver);

    /**
     * @return the socket address
     */
    SocketAddress getSa();

    /**
     * @param sa the new socket address to set
     */
    void setSa(SocketAddress sa);

    /**
     * Process the incoming Real2Proxy message
     *
     * @param msg  the Real2Proxy message
     */
    void processReal2ProxyMsg(Real2ProxyMsg msg);
}
