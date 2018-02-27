package aim4.driver.rim;


import aim4.driver.BasicDriver;
import aim4.im.rim.IntersectionManager;
import aim4.map.Road;
import aim4.map.lane.Lane;

/**
 * An autonomous driver's from the viewpoint of coordinators.
 */
public interface AutoDriverCoordinatorView {
    // lane

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

    /**
     * Add a lane that the DriverAgent's vehicle currently occupies.
     *
     * @param lane a lane that the DriverAgent's vehicle currently occupies
     */
    void addCurrentlyOccupiedLane(Lane lane);


    // origin and destination

    /**
     * Get where this DriverAgent is going.
     *
     * @return the Road where this DriverAgent is going
     */
    Road getDestination();


    // IM

    /**
     * Get the IntersectionManager with which the agent is currently
     * interacting.
     *
     * @return the IntersectionManager with which the agent is currently
     *         interacting
     *
     */
    IntersectionManager getCurrentRIM();

    /**
     * Find the next IntersectionManager that the Vehicle will need to
     * interact with, in this Lane. This version
     * overrides the version in {@link BasicDriver}, but only to memoize it for
     * speed.
     *
     * @return the nextIntersectionManager that the Vehicle will need
     *         to interact with, in this Lane
     */
    IntersectionManager nextIntersectionManager();

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

    /**
     * Find the distance from the previous intersection in the Lane in which
     * the Vehicle is, from the position at which the Vehicle is.  This
     * subtracts the length of the Vehicle from the distance from the front
     * of the Vehicle.  It overrides the version in DriverAgent, but only to
     * memoize it.
     *
     * @return the distance from the previous intersection given the current
     *         Lane and position of the Vehicle.
     */
    double distanceFromPrevIntersection();

    /**
     * Whether or not the Vehicle controlled by this driver agent
     * is inside the intersection managed by the current IntersectionManager.
     *
     * @return whether or not the Vehicle controlled by this
     *         CoordinatingDriverAgent is inside the intersection managed by the
     *         current IntersectionManager.
     */
    boolean inCurrentIntersection();
}
