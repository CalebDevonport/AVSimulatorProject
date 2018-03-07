package aim4.driver.rim.navigator;

import aim4.im.rim.IntersectionManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.vehicle.VehicleSpec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A base class for an agent that chooses which way a vehicle should go.
 */
public class BasicNavigator implements Navigator{
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The map object
     */
    private BasicRIMIntersectionMap basicRIMIntersectionMap;

    /**
     * The vehicle for which this agent is navigating.
     */
    private VehicleSpec vehicleSpec;

    /**
     * A cache of the road leading away from the intersection with the fastest
     * path leading to the destination.
     */
    private Map<List<Integer>, Road> fastestMap =
            new HashMap<List<Integer>, Road>();


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Construct a new Navigator for the given Vehicle specification.
     * This will only be called by derived classes.
     *
     * @param vehicleSpec  the vehicle's specification
     * @param basicRIMIntersectionMap     the map object
     */
    public BasicNavigator(VehicleSpec vehicleSpec, BasicRIMIntersectionMap basicRIMIntersectionMap) {
        this.vehicleSpec = vehicleSpec;
        this.basicRIMIntersectionMap = basicRIMIntersectionMap;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public Road navigate(Road current, IntersectionManager im, Road destination) {
        return fastestPath(current, im, destination);
    }


    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /**
     * Find the fastest path
     * For a single intersection, the fastest path is unique.
     *
     * @param currentRoad     the Road on which the vehicle is currently traveling
     * @param im          the IntersectionManager the vehicle is approaching
     * @param destinationRoad the Road on which the vehicle would ultimately like to
     *                    end up
     * @return  The fastest road
     */
    private Road fastestPath(Road currentRoad, IntersectionManager im,
                             Road destinationRoad) {
        return destinationRoad;
    }
}
