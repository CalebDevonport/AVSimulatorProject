package aim4.driver.rim;

import aim4.driver.BasicDriver;
import aim4.im.aim.IntersectionManager;
import aim4.map.lane.Lane;
import aim4.vehicle.AutoVehicleDriverModel;

/**
 * Autonomous driver from the viewpoint of pilots.
 */
public interface AutoDriverPilotView {
    /**
     * Get the Vehicle this DriverAgent is controlling.
     *
     * @return the Vehicle this DriverAgent is controlling
     */
    AutoVehicleDriverModel getVehicle();

    /**
     * Get the Lane the DriverAgent is currently following.
     *
     * @return the Lane the DriverAgent is currently following
     */
    Lane getCurrentLane();

    /**
     * Set the Lane the DriverAgent is currently following.
     *
     * @param lane the Lane the DriverAgent should follow
     */
    void setCurrentLane(Lane lane);


    // IM

    /**
     * Get the IntersectionManager with which the agent is currently
     * interacting.
     *
     * @return the IntersectionManager with which the agent is currently
     *         interacting
     *
     */
    IntersectionManager getCurrentIM();

    /**
     * Find the distance to the next intersection in the Lane in which
     * the Vehicle is, from the position at which the Vehicle is.  This version
     * overrides the version in {@link BasicDriver}, but only to memoize it for
     * speed.
     *
     * @return the distance to the next intersection given the current Lane
     *         and position of the Vehicle.
     */
    double distanceToNextIntersection();
}
