package aim4.map.rim.destination;

import aim4.map.Road;
import aim4.map.lane.Lane;

/**
 * The destination selector.
 */
public interface DestinationSelector {
    /**
     * Select the Road which the given Vehicle should use as its destination.
     *
     * @param currentLane the lane the Vehicle is currently on, usually also
     *                    the lane the vehicle spawned on
     * @return            the Road which the Vehicle should use as its
     *                    destination
     */
    Road selectDestination(Lane currentLane);
}
