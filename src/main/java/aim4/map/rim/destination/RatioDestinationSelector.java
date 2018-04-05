package aim4.map.rim.destination;

import aim4.config.Debug;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.rim.RIMSpawnPoint;
import aim4.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The destination selector that
 */
public class RatioDestinationSelector implements DestinationSelector{
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The set of roads that a vehicle can use as an ultimate destination.
     */
    private List<Road> destinationRoads;
    /**
     * The traffic volume object.
     */
    private TrafficVolume trafficVolume;
    /**
     * The probability of making a left turn.
     */
    private Map<Integer,Double> leftTurnProb;
    /**
     * The probability of making a right turn.
     */
    private Map<Integer,Double> rightTurnProb;


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a new RandomDestinationSelector from the given Layout.
     *
     * @param map            the Layout from which to create the
     *                       RandomDestinationSelector
     * @param trafficVolume  the traffic volume
     */
    public RatioDestinationSelector(BasicRIMIntersectionMap map, TrafficVolume trafficVolume) {
        destinationRoads = map.getDestinationRoads();
        this.trafficVolume = trafficVolume;
        leftTurnProb = new HashMap<Integer, Double>();
        rightTurnProb = new HashMap<Integer, Double>();

        for(RIMSpawnPoint sp: map.getSpawnPoints()) {
            int laneId = sp.getLane().getId();
            leftTurnProb.put(laneId, trafficVolume.getLeftTurnVolume(laneId) /
                    trafficVolume.getTotalVolume(laneId));
            rightTurnProb.put(laneId, trafficVolume.getRightTurnVolume(laneId) /
                    trafficVolume.getTotalVolume(laneId));
        }
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
        int laneId = currentLane.getId();
        double prob = Util.random.nextDouble();
        if (prob < leftTurnProb.get(laneId)) {
            return trafficVolume.getLeftTurnRoad(currentRoad);
        } else if (prob >= 1.0 - rightTurnProb.get(laneId)) {
            return trafficVolume.getRightTurnRoad(currentRoad);
        } else {
            return currentRoad;
        }
    }
}
