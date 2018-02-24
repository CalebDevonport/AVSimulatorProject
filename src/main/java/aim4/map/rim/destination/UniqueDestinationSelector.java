package aim4.map.rim.destination;

import aim4.map.Road;
import aim4.map.lane.Lane;

/**
 * The unique destination selector which always returns the same destination
 */
public class UniqueDestinationSelector implements DestinationSelector{
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The destination */
    private Road endRoad;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a unique destination selector.
     *
     * @param endRoad  the destination
     */
    public UniqueDestinationSelector(Road endRoad) {
        this.endRoad = endRoad;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Road selectDestination(Lane currentLane) {
        return endRoad;
    }
}
