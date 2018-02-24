package aim4.map.rim.destination;

import aim4.config.Debug;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.util.Util;

import java.util.List;

/**
 * The RandomDestinationSelector selects Roads uniformly at random, but will
 * not select a Road that is the dual of the starting Road.  This is to
 * prevent Vehicles from simply going back from whence they came.
 */
public class RandomDestinationSelector implements DestinationSelector {
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The Set of legal Roads that a vehicle can use as an ultimate destination.
     */
    private List<Road> destinationRoads;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a new RandomDestinationSelector from the given Layout.
     *
     * @param layout the Layout from which to create the
     *               RandomDestinationSelector
     */
    public RandomDestinationSelector(BasicRIMIntersectionMap layout) {
        destinationRoads = layout.getDestinationRoads();
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Road selectDestination(Lane currentLane) {
        Road currentRoad = Debug.currentRimMap.getRoad(currentLane);
        Road dest =
                destinationRoads.get(Util.random.nextInt(destinationRoads.size()));
        while(dest.getDual() == currentRoad) {
            dest =
                    destinationRoads.get(Util.random.nextInt(destinationRoads.size()));
        }
        return dest;
    }
}
