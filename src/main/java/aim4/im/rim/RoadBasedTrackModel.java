package aim4.im.rim;

import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;

import java.util.*;

/**
 * A track model for road based intersections
 */
public class RoadBasedTrackModel implements TrackModel {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The intersection.
     */
    private RoadBasedIntersection intersection;

    /**
     * A map from ordered pairs of (Lane, Road) (implemented as a map to a map)
     * to an ordered List of Lanes indicating the priority of other Lanes in the
     * same Road as the second Lane in the ordered pair, for turning into
     * that Lane from the first Lane in the ordered pair.  That is, if
     * <code>lanePriorities.get(l1).get(road)</code> returns the List
     * <code>{l2, l3, l4, l5}</code>, then a vehicle turning from
     * <code>l1</code> to a lane in <code>road</code> should consider turning
     * into <code>l2</code>, <code>l3</code>, <code>l4</code>, and
     * <code>l5</code> in that order, as that order specifies which lanes'
     * intersection exit points are closest to <code>l1</code>'s intersection
     * entry point.
     */
    private Map<Lane, Map<Road, List<Lane>>> lanePriorities =
            new HashMap<Lane, Map<Road, List<Lane>>>();

    /**
     * Memoization cache for {@link #traversalDistance(Road arrival, Road
     * departure)}.
     */
    private Map<List<Integer>, Double> memoTraversalDistance =
            new HashMap<List<Integer>, Double>();


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Basic class constructor.  Takes an lane-based intersection and construct
     * a track model for the intersection.
     *
     * @param intersection  a lane-based intersection.
     */
    public RoadBasedTrackModel(RoadBasedIntersection intersection) {
        this.intersection = intersection;
        // Determine the priorities for exit lanes
        calculateLanePriorities();
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /**
     * Given the current set of Lanes, calculate the turning lane priorities.
     * That is, for each ordered pair of Lanes, determine which other Lanes
     * in the second Lane's are viable exit Lanes (in the same Road) and order
     * them based on distance from the first Lane.
     */
    private void calculateLanePriorities() {
        for(Lane entryLane : intersection.getEntryLanes()) {
            // For a single lane, the exit lane will be only one, the exit-approach lane of every road
            // e.g getContinuousLanes().get(7)
            Map<Road, List<Lane>> exitPriorities = new HashMap<Road, List<Lane>>();

            for(Road exitRoad : intersection.getExitRoads()) {
                List<Lane> exitLanes = new ArrayList<Lane>();
                for (int i = 0; i < exitRoad.getContinuousLanes().size(); i++) {
                	 exitLanes.add(exitRoad.getExitApproachLane(i));
                }
                // Now put them in the list for entryLane
                exitPriorities.put(exitRoad, exitLanes);
            }
            // Now that we've built up the exit priorities for this
            // lane, add it
            lanePriorities.put(entryLane, exitPriorities);
        }
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////


    /**
     * Get the intersection managed by this track model
     *
     * @return  the intersection managed by this track model
     */
    @Override
    public RoadBasedIntersection getIntersection() {
        return intersection;
    }

    /**
     * Given an arrival Lane and a departure Road, get an ordered List of Lanes
     * that represents the Lanes from highest to lowest priority based on
     * distance from the arrival Lane.
     *
     * @param arrivalLane the Lane in which the vehicle is arriving
     * @param departure   the Road by which the vehicle is departing
     * @return            the ordered List of Lanes, by priority, into which the
     *                    vehicle should try to turn
     */
    @Override
    public List<Lane> getSortedDepartureLanes(Lane arrivalLane, Road departure) {
        return lanePriorities.get(arrivalLane).get(departure);
    }

    /**
     * Get the distance from the entry of the given Road (first arc), to the departure of
     * the other given Road (last arc).
     *
     * @param arrival   the arrival Road
     * @param departure the departure Road
     * @return          the distance from the entry of the arrival Road to the
     *                  exit of the departure Road
     */
    @Override
    public double traversalDistance(Road arrival, Road departure) {
        return traversalDistance(arrival.getEntryApproachLane(0),
                departure.getExitApproachLane(0));
    }

    /**
     * Get the distance from the entry of the given Lane, to the departure of
     * the other given Lane, if traveling along the line segments through their point
     * of intersection.
     *
     * @param arrival   the arrival Lane
     * @param departure the departure Lane
     * @return          the distance from the entry of the arrival Lane to the
     *                  exit of the departure Lane through their intersection
     */
    //todo: hand u turns
    @Override
    public double traversalDistance(Lane arrival, Lane departure) {
        List<Integer> key = Arrays.asList(arrival.getId(),
                departure.getId());
        // First figure out which roads the lanes belong to
        if(!memoTraversalDistance.containsKey(key)) {
            final Road[] arrivalRoad = new Road[1];
            final Road[] departureRoad = new Road[1];
            intersection.getRoads().forEach( road -> {
                if (road.getAllContinuousLanes().contains(arrival)) {
                    arrivalRoad[0] = road;
                }
                if (road.getAllContinuousLanes().contains(departure)) {
                    departureRoad[0] = road;
                }
            });

            ArcSegmentLane arrivalLane = (ArcSegmentLane) arrival;
            ArcSegmentLane mergingEntryLane = (ArcSegmentLane) arrival.getNextLane();
            ArcSegmentLane insideLane = (ArcSegmentLane) mergingEntryLane.getNextLane();
            ArcSegmentLane mergingExitLane = (ArcSegmentLane) departure.getPrevLane();
            ArcSegmentLane departureLane = (ArcSegmentLane) departure;

            double totalDistance =
                            arrivalLane.getLengthArcLaneDecomposition() +
                            mergingEntryLane.getLengthArcLaneDecomposition() +
                            insideLane.getLengthArcLaneDecomposition() +
                            mergingExitLane.getLengthArcLaneDecomposition() +
                                    departureLane.getLengthArcLaneDecomposition();

            // Handle north case
            if (arrivalRoad[0].getName() == "1st Avenue N") {
                // Turn right
                if (departureRoad[0].getName() == "1st Street E"){
                    // Distance already calculated
                }
                // Go straight
                else if (departureRoad[0].getName() == "1st Avenue N") {
                    // Go two lanes north
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                 // Turn left
                } else if (departureRoad[0].getName() == "1st Street W") {
                    // Go two lanes north
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Go two lanes west
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) departureRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                }
            }

            // Handle west case
            else if (arrivalRoad[0].getName() == "1st Street W") {
                // Turn right
                if (departureRoad[0].getName() == "1st Avenue N"){
                    // Distance already calculated
                }
                // Go straight
                else if (departureRoad[0].getName() == "1st Street W") {
                    // Go two lanes west
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Turn left
                } else if (departureRoad[0].getName() == "1st Avenue S") {
                    // Go two lanes west
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Go two lanes south
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) departureRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                }
            }

            // Handle south case
            else if (arrivalRoad[0].getName() == "1st Avenue S") {
                // Turn right
                if (departureRoad[0].getName() == "1st Street W"){
                    // Distance already calculated
                }
                // Go straight
                else if (departureRoad[0].getName() == "1st Avenue S") {
                    // Go two lanes south
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Turn left
                } else if (departureRoad[0].getName() == "1st Street E") {
                    // Go two lanes south
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Go two lanes east
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) departureRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                }
            }

            // Handle east case
            else if (arrivalRoad[0].getName() == "1st Street E") {
                // Turn right
                if (departureRoad[0].getName() == "1st Avenue S"){
                    // Distance already calculated
                }
                // Go straight
                else if (departureRoad[0].getName() == "1st Street E") {
                    // Go three lanes east
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Turn left
                } else if (departureRoad[0].getName() == "1st Avenue N") {
                    // Go two lanes east
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) arrivalRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                    // Go two lanes north
                    for (int index = 4; index <= 5; index++){
                        ArcSegmentLane straightLane = (ArcSegmentLane) departureRoad[0].getContinuousLanesForLane(0).get(index);
                        totalDistance += straightLane.getLengthArcLaneDecomposition();
                    }
                }
            }
            memoTraversalDistance.put(key, totalDistance);
        }
        return memoTraversalDistance.get(key);
    }
}
