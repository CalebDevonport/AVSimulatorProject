package aim4.map.rim.destination;

import aim4.config.Debug;
import aim4.map.Road;
import aim4.map.lane.Lane;

/**
 * The IdentityDestinationSelector always chooses the Vehicle's current Road
 * as the destination Road, unless it is not a legal destination Road, in
 * which case it throws a RuntimeException.
 */
public class IdentityDestinationSelector implements DestinationSelector{
    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a new IdentityDestinationSelector from the given Layout.
     */
    public IdentityDestinationSelector() {
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Road selectDestination(Lane currentLane) {
        return Debug.currentRimMap.getRoad(currentLane);
    }
}
