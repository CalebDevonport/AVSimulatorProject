package aim4.map.rim.destination;

import aim4.config.Debug;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.util.Util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    
    Map<Road, Road> leftTurnRoad;
    /** The right turn road */
    Map<Road, Road> rightTurnRoad;

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
    	Map<String,String> roadNameTranslation = new HashMap<String,String>();
        roadNameTranslation.put("NB", "1st Avenue N");
        roadNameTranslation.put("SB", "1st Avenue S");
        roadNameTranslation.put("EB", "1st Street E");
        roadNameTranslation.put("WB", "1st Street W");

        Map<String, Road> roadNameToRoadObj = new HashMap<String, Road>();
        for (String roadName : roadNameTranslation.keySet()) {
            for (Road road : this.destinationRoads) {
                if (road.getName().equals(roadNameTranslation.get(roadName))) {
                    roadNameToRoadObj.put(roadName, road);
                }
            }
        }
    	
    	
    	this.leftTurnRoad = new HashMap<Road, Road>();
        this.rightTurnRoad = new HashMap<Road, Road>();

        leftTurnRoad.put(roadNameToRoadObj.get("NB"), roadNameToRoadObj.get("WB"));
        rightTurnRoad.put(roadNameToRoadObj.get("NB"), roadNameToRoadObj.get("EB"));
        leftTurnRoad.put(roadNameToRoadObj.get("SB"), roadNameToRoadObj.get("EB"));
        rightTurnRoad.put(roadNameToRoadObj.get("SB"), roadNameToRoadObj.get("WB"));
        leftTurnRoad.put(roadNameToRoadObj.get("EB"), roadNameToRoadObj.get("NB"));
        rightTurnRoad.put(roadNameToRoadObj.get("EB"), roadNameToRoadObj.get("SB"));
        leftTurnRoad.put(roadNameToRoadObj.get("WB"), roadNameToRoadObj.get("SB"));
        rightTurnRoad.put(roadNameToRoadObj.get("WB"), roadNameToRoadObj.get("NB"));
        
        Road dest;
        
        Road currentRoad = Debug.currentRimMap.getRoad(currentLane);
        
        dest = getDest(currentRoad, currentLane);
        while(dest.getDual() == currentRoad) {
        	dest = getDest(currentRoad, currentLane);
        }
        return dest;
    }
    
    private Road getDest(Road currentRoad, Lane currentLane) {
    	if (currentRoad.getLaneIndexFromLane(currentLane) == 1) {
        	int random = Util.random.nextInt(2);
        	if (random == 1) {
        		return currentRoad;
        	}
        	else {
        		return getRightTurnRoad(currentRoad);
        	}
        }
        else {
        	return destinationRoads.get(Util.random.nextInt(destinationRoads.size()));
        }
    }
    
    public Road getLeftTurnRoad(Road road) {
        return leftTurnRoad.get(road);
    }
    
    public Road getRightTurnRoad(Road road) {
        return rightTurnRoad.get(road);
    }
}
