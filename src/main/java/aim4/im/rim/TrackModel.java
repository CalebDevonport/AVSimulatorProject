package aim4.im.rim;

import aim4.map.Road;
import aim4.map.lane.Lane;

import java.util.List;

/**
 * The interface of track models
 */
public interface TrackModel {

    /**
     * Get the intersection managed by this track model
     *
     * @return  the intersection managed by this track model
     */
    RoadBasedIntersection getIntersection();

    /**
     * Get the distance from the entry of the given Road, to the departure of
     * the other given Road.
     *
     * @param arrival   the arrival Road
     * @param departure the departure Road
     * @return          the distance from the entry of the arrival Road to the
     *                  exit of the departure Road
     */
    double traversalDistance(Road arrival, Road departure);

    /**
     * Get the distance from the entry of the given Lane, to the departure of
     * the other given Lane, if traveling along segments through their point
     * of intersection.
     *
     * @param arrival   the arrival Lane
     * @param departure the departure Lane
     * @return          the distance from the entry of the arrival Lane to the
     *                  exit of the departure Lane through their intersection
     */
    double traversalDistance(Lane arrival, Lane departure);

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
    List<Lane> getSortedDepartureLanes(Lane arrivalLane, Road departure);

}
