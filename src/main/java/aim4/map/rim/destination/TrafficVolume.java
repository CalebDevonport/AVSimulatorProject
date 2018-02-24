package aim4.map.rim.destination;

import aim4.config.Constants;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.util.Util;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The record for traffic volume.
 */
public class TrafficVolume {
    /** The left turn volumes */
    private Map<Integer, Double> leftTurnVolumes;
    /** The through volumes */
    private Map<Integer, Double> throughVolumes;
    /** The right turn volumes */
    private Map<Integer, Double> rightTurnVolumes;
    /** The total volumes */
    private Map<Integer, Double> totalVolumes;
    /** The left turn road */
    private Map<Road, Road> leftTurnRoad;
    /** The right turn road */
    private Map<Road, Road> rightTurnRoad;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a new traffic volume object from the data in a file for AIM.
     *
     * @param map   the map
     * @param strs  the data in a file
     */
    public TrafficVolume(BasicRIMIntersectionMap map, List<String> strs) {
        leftTurnVolumes = new HashMap<Integer, Double>();
        throughVolumes = new HashMap<Integer, Double>();
        rightTurnVolumes = new HashMap<Integer, Double>();
        totalVolumes = new HashMap<Integer, Double>();

        Map<String,String> roadNameTranslation = new HashMap<String,String>();
        roadNameTranslation.put("NB", "1st Avenue N");
        roadNameTranslation.put("SB", "1st Avenue S");
        roadNameTranslation.put("EB", "1st Street E");
        roadNameTranslation.put("WB", "1st Street W");

        Map<String, Road> roadNameToRoadObj = new HashMap<String, Road>();
        for (String roadName : roadNameTranslation.keySet()) {
            for (Road road : map.getRoads()) {
                if (road.getName().equals(roadNameTranslation.get(roadName))) {
                    roadNameToRoadObj.put(roadName, road);
                }
            }
        }


        leftTurnRoad = new HashMap<Road, Road>();
        rightTurnRoad = new HashMap<Road, Road>();

        leftTurnRoad.put(roadNameToRoadObj.get("NB"), roadNameToRoadObj.get("WB"));
        rightTurnRoad.put(roadNameToRoadObj.get("NB"), roadNameToRoadObj.get("EB"));
        leftTurnRoad.put(roadNameToRoadObj.get("SB"), roadNameToRoadObj.get("EB"));
        rightTurnRoad.put(roadNameToRoadObj.get("SB"), roadNameToRoadObj.get("WB"));
        leftTurnRoad.put(roadNameToRoadObj.get("EB"), roadNameToRoadObj.get("NB"));
        rightTurnRoad.put(roadNameToRoadObj.get("EB"), roadNameToRoadObj.get("SB"));
        leftTurnRoad.put(roadNameToRoadObj.get("WB"), roadNameToRoadObj.get("SB"));
        rightTurnRoad.put(roadNameToRoadObj.get("WB"), roadNameToRoadObj.get("NB"));

        Map<String, List<Lane>> roadToLeftmostLanes =
                new HashMap<String, List<Lane>>();
        Map<String, List<Lane>> roadToMiddleLanes =
                new HashMap<String, List<Lane>>();
        Map<String, List<Lane>> roadToRightmostLanes =
                new HashMap<String, List<Lane>>();

        for(String roadName : roadNameTranslation.keySet()) {
            for (Road road : map.getRoads()) {
                if (road.getName().equals(roadNameTranslation.get(roadName))) {
                    roadToLeftmostLanes.put(roadName, new LinkedList<Lane>());
                    roadToMiddleLanes.put(roadName, new LinkedList<Lane>());
                    roadToRightmostLanes.put(roadName, new LinkedList<Lane>());

                    Lane lane = road.getFirstLane();
                    if (!lane.hasLeftNeighbor()) {
                        roadToLeftmostLanes.get(roadName).add(lane);
                    } else if (!lane.hasRightNeighbor()) {
                        roadToRightmostLanes.get(roadName).add(lane);
                    } else {
                        roadToMiddleLanes.get(roadName).add(lane);
                    }
                }
            }
        }

        for (int i = 1; i < strs.size(); i++) {
            String[] tokens = strs.get(i).split(",");

            if (tokens[1].equals("Left")) {
                for(Lane lane : roadToLeftmostLanes.get(tokens[0])) {
                    leftTurnVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[2]) / Constants.numOfSecondPerHour);
                }
                for (Lane lane : roadToMiddleLanes.get(tokens[0])) {
                    leftTurnVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[3]) / Constants.numOfSecondPerHour);
                }
                for (Lane lane : roadToRightmostLanes.get(tokens[0])) {
                    leftTurnVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[4]) / Constants.numOfSecondPerHour);
                }
            } else if (tokens[1].equals("Through")) {
                for (Lane lane : roadToLeftmostLanes.get(tokens[0])) {
                    throughVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[2]) / Constants.numOfSecondPerHour);
                }
                for (Lane lane : roadToMiddleLanes.get(tokens[0])) {
                    throughVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[3]) / Constants.numOfSecondPerHour);
                }
                for (Lane lane : roadToRightmostLanes.get(tokens[0])) {
                    throughVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[4]) / Constants.numOfSecondPerHour);
                }
            } else if (tokens[1].equals("Right")) {
                for (Lane lane : roadToLeftmostLanes.get(tokens[0])) {
                    rightTurnVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[2]) / Constants.numOfSecondPerHour);
                }
                for (Lane lane : roadToMiddleLanes.get(tokens[0])) {
                    rightTurnVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[3]) / Constants.numOfSecondPerHour);
                }
                for (Lane lane : roadToRightmostLanes.get(tokens[0])) {
                    rightTurnVolumes.put(lane.getId(),
                            Double.parseDouble(tokens[4]) / Constants.numOfSecondPerHour);
                }
            } else {
                throw new RuntimeException("Invalid data file.\n");
            }
        }
        for (Road road : map.getRoads()) {
            Lane lane = road.getFirstLane();
            int laneId = lane.getId();
            double v =
                    leftTurnVolumes.get(laneId) +
                            throughVolumes.get(laneId) +
                            rightTurnVolumes.get(laneId);
            totalVolumes.put(laneId, v);
        }
    }


    /////////////////////////////////
    // PUBLIC STATIC METHODS
    /////////////////////////////////

    /**
     * Create a new traffic volume object from file.
     *
     * @param map          the map
     * @param csvFileName  the file name of the CSV file
     * @return the traffic volume object
     */
    public static TrafficVolume makeFromFile(BasicRIMIntersectionMap map,
                                             String csvFileName) {
        List<String> strs = null;
        try {
            strs = Util.readFileToStrArray(csvFileName);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        if (strs != null) {
            return new TrafficVolume(map, strs);
        } else {
            return null;
        }
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the left turn volume of a lane.
     *
     * @param laneId  the ID of the lane
     * @return the left turn volume of the lane
     */
    public double getLeftTurnVolume(int laneId) {
        return leftTurnVolumes.get(laneId);
    }

    /**
     * Get the left turn volume of a lane.
     *
     * @param laneId  the ID of the lane
     * @return the left turn volume of the lane
     */
    public double getThroughVolume(int laneId) {
        return throughVolumes.get(laneId);
    }

    /**
     * Get the right turn volume of a lane.
     *
     * @param laneId  the ID of the lane
     * @return the right turn volume of the lane
     */
    public double getRightTurnVolume(int laneId) {
        return rightTurnVolumes.get(laneId);
    }

    /**
     * Get the total volume of a lane.
     *
     * @param laneId  the ID of the lane
     * @return the total volume of the lane
     */
    public double getTotalVolume(int laneId) {
        return totalVolumes.get(laneId);
    }

    /**
     * Get the left turn volume of a road.
     *
     * @param road  the road
     * @return the left turn volume of the road
     */
    public Road getLeftTurnRoad(Road road) {
        return leftTurnRoad.get(road);
    }

    /**
     * Get the right turn volume of a road.
     *
     * @param road  the road
     * @return the right turn volume of the road
     */
    public Road getRightTurnRoad(Road road) {
        return rightTurnRoad.get(road);
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "leftTurnVolumes: " + leftTurnVolumes + ",\n" +
                "throughVolumes: " + throughVolumes + ",\n" +
                "rightTurnVolumes" + rightTurnVolumes + ".\n";
    }
}
