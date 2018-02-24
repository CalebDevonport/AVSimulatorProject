package aim4.map.rim.destination;

//TODO: Need to fix this class to avoid hard-coding

import aim4.config.Debug;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.Lane;

import java.util.List;

/**
 * The turn based destination selector.
 */
public class TurnBasedDestinationSelector implements DestinationSelector{
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
     * Create a new identity destination selector from the given Layout.
     *
     * @param layout  the layout from which to create the new
     *                identity destination selector
     */
    public TurnBasedDestinationSelector(BasicRIMIntersectionMap layout) {
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

        boolean hasLeft = currentLane.hasLeftNeighbor();
        boolean hasRight = currentLane.hasRightNeighbor();

        if (hasLeft && hasRight) {
            return currentRoad;
        } else if (!hasLeft && hasRight) {
            if (currentRoad.getName().equals("1st Street E")) {
                return destinationRoads.get(0);
            } else if (currentRoad.getName().equals("1st Street W")) {
                return destinationRoads.get(1);
            } else if (currentRoad.getName().equals("1st Avenue N")) {
                return destinationRoads.get(2);
            } else if (currentRoad.getName().equals("1st Avenue S")) {
                return destinationRoads.get(3);
            } else {
                throw new RuntimeException("Error in TurnBasedDestination");
            }
        } else if (hasLeft && !hasRight) {
            if (currentRoad.getName().equals("1st Street E")) {
                return destinationRoads.get(0);
            } else if (currentRoad.getName().equals("1st Street W")) {
                return destinationRoads.get(1);
            } else if (currentRoad.getName().equals("1st Avenue N")) {
                return destinationRoads.get(2);
            } else if (currentRoad.getName().equals("1st Avenue S")) {
                return destinationRoads.get(3);
            } else {
                throw new RuntimeException("Error in TurnBasedDestination");
            }
        } else {
            return currentRoad;
        }
    }
}
