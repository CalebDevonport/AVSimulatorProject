package aim4.driver.rim;

import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.rim.RIMSpawnPoint;
import aim4.vehicle.rim.RIMAutoVehicleDriverModel;

/**
 * A proxy driver.
 */
public class ProxyDriver extends RIMAutoDriver{
    /**
     * Construct a proxy driver.
     *
     * @param vehicle the vehicle object
     * @param basicRIMIntersectionMap  the map object
     */
    public ProxyDriver(RIMAutoVehicleDriverModel vehicle, BasicRIMIntersectionMap basicRIMIntersectionMap) {
        super(vehicle, basicRIMIntersectionMap);
        // TODO Auto-generated constructor stub
    }

    /**
     * Take control actions for driving the agent's Vehicle.  This allows
     * both the Coordinator and the Pilot to act (in that order).
     */
    @Override
    public void act() {
    }

    /**
     * Get where this DriverAgent is coming from.
     *
     * @return the Road where this DriverAgent is coming from
     */
    @Override
    public RIMSpawnPoint getSpawnPoint() {
        return null;
    }

    /**
     * Get where this DriverAgent is going.
     *
     * @return the Road where this DriverAgent is going
     */
    @Override
    public Road getDestination() {
        return null;
    }
}
