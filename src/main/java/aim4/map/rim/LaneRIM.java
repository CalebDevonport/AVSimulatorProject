package aim4.map.rim;

import aim4.im.rim.IntersectionManager;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;

import java.awt.geom.Point2D;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public class LaneRIM {
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The lane
     */
    private Lane lane;

    /**
     * A map from normalized distances of exit points to intersection managers.
     */
    private SortedMap<Double, IntersectionManager> intersectionManagers =
            new TreeMap<Double, IntersectionManager>();

    /**
     * Memoization cache.
     */
    private Map<IntersectionManager, IntersectionManager>
            memoGetSubsequentIntersectionManager = null;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a lane and intersection manager relationship object.
     *
     * @param lane  the lane.
     */
    public LaneRIM(Lane lane) {
        this.lane = lane;
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // intersection manager

    /**
     * Register an {@link IntersectionManager} with this Lane.  If the Lane does
     * not intersect the area controlled by the IntersectionManager, it has no
     * effect. Otherwise, the IntersectionManager is stored and returned under
     * certain circumstances by the <code>nextIntersectionManager</code> method.
     *
     * @param im the IntersectionManager to register
     */
    public void registerIntersectionManager(IntersectionManager im, ArcSegmentLane parentLane) {
        // Only do this if this lane is managed by this intersection
        if((parentLane ==  null && im.manages(lane)) || (parentLane != null && im.manages(parentLane))) {
            // Reset this cache
            memoGetSubsequentIntersectionManager = null;
            // Find out where this lane exits the intersection
            Point2D exitPoint = im.getIntersection().getExitPoint(lane);
            // If it's null, that means it doesn't exit.
            if(exitPoint == null) {
                exitPoint = lane.getEndPoint();
            }
            double normalizedDistanceToExit =
                    lane.normalizedDistanceAlongLane(exitPoint);
            // Add the normalized distance to the exit point to the map
            // that gives us the "next intersection" for any point in the lane.
            intersectionManagers.put(normalizedDistanceToExit, im);
        }
    }

    /**
     * Get the first IntersectionManager that this Lane, or any Lane it leads
     * into enters. Recursively searches through all subsequent Lanes.
     *
     * @return the first IntersectionManager this Lane, or any Lane it leads
     *         into enters
     */
    public IntersectionManager firstIntersectionManager() {
        if (intersectionManagers.isEmpty())
            return null;
        else
            return intersectionManagers.get(intersectionManagers.firstKey());
    }

    /**
     * Get the distance from the start of this Lane to the first
     * IntersectionManager that this Lane, or any Lane it leads into intersects.
     * Recursively searches through all subsequent Lanes. Returns
     * <code>Double.MAX_VALUE</code> if no such IntersectionManager exists.
     *
     * @return the distance from the start of this Lane to the first
     *         IntersectionManager this Lane, or any lane it leads into
     *         intersects, or <code>Double.MAX_VALUE</code> if no such
     *         IntersectionManager exists
     */
    public double distanceToFirstIntersection() {
        if(intersectionManagers.isEmpty()) {
            if(lane.hasNextLane()) {
                return lane.getLength() +
                        lane.getNextLane().getLaneRIM().distanceToFirstIntersection();
            }
            return Double.MAX_VALUE;
        }
        IntersectionManager im = intersectionManagers.get(intersectionManagers.firstKey());
        Point2D entry = im.getIntersection().getEntryPoint(lane);
        Point2D exit = im.getIntersection().getExitPoint(lane);
        // Means we exited the intersection
        if (exit != null) {
            return Double.MAX_VALUE;
        }
        double distance = 0.0;
        // If we haven't reached the entry
        if(entry == null) {
            distance += lane.getLength();
            if (lane.hasNextLane()) {
                Lane nextLane = lane.getNextLane();
                if (nextLane instanceof  ArcSegmentLane){
                    nextLane = ((ArcSegmentLane) nextLane).getArcLaneDecomposition().get(0);
                }
                distance += nextLane.getLength();
                entry = im.getIntersection().getEntryPoint(nextLane);
                exit = im.getIntersection().getExitPoint(nextLane);
                while (nextLane.hasNextLane() && entry == null && exit == null) {
                    nextLane = nextLane.getNextLane();
                    distance += nextLane.getLength();
                    entry = im.getIntersection().getEntryPoint(nextLane);
                    exit = im.getIntersection().getExitPoint(nextLane);
                }
            }

            // Means we are inside the intersection
            if (entry == null && exit != null) {
                return 0;
            }
            // Means we passed the intersection
            if (entry == null && exit == null) {
                return Double.MAX_VALUE;
            }
            else return distance;
        }
        // Otherwise just return the distance from the start of this Lane to
        // the place it enters the first intersection
        return lane.getStartPoint().distance(entry);
    }

    /**
     * Get the distance from the start of this Lane to the first
     * approaching lane that this Lane, or any Lane it leads into intersects.
     * Recursively searches through all subsequent Lanes. Returns
     * <code>Double.MAX_VALUE</code> if no such lane exists.
     *
     * @return the distance from the start of this Lane to the first
     *         approaching lane this Lane, or any lane it leads into
     *         intersects, or <code>Double.MAX_VALUE</code> if no such
     *         lane exists
     */
    public double distanceToFirstApproachingLane() {
        assert lane instanceof LineSegmentLane;
        assert !intersectionManagers.isEmpty();
        IntersectionManager im = intersectionManagers.get(intersectionManagers.firstKey());

        //Find the approaching point for the road the current lane is part of
        Point2D approachEntry = im.getIntersection().getApproachEntryPoint(lane);

        double distance = 0.0;
        // If we haven't reached the entry
        if(approachEntry == null) {
            distance += lane.getLength();
            if (lane.hasNextLane()) {
                Lane nextLane = lane.getNextLane();
                approachEntry = im.getIntersection().getApproachEntryPoint(nextLane);
                while (nextLane.hasNextLane() && approachEntry == null) {
                    distance += nextLane.getLength();
                    nextLane = nextLane.getNextLane();
                    distance += nextLane.getLength();
                    approachEntry = im.getIntersection().getApproachEntryPoint(nextLane);
                }
            }

            // Means we there is no entry approach point
            if (approachEntry == null) {
                return Double.MAX_VALUE;
            }
            else return distance;
        }
        // Otherwise just return the distance from the start of this Lane to
        // the place it enters the approach lane
        return lane.getStartPoint().distance(approachEntry);
    }

    /**
     * Find the next Lane, including this one, that enters an intersection at
     * any point.
     *
     * @return  the next Lane, following the chain of Lanes in which this Lane
     *          is, that enters an intersection, at any point
     */
    public Lane laneToFirstIntersection() {
        assert lane instanceof LineSegmentLane;
        // If there aren't any more in this lane
        if(intersectionManagers.isEmpty()) {
            // Check the next Lane
            if(lane.hasNextLane()) {
                // Pass the buck to the next Lane after this one
                return lane.getNextLane().getLaneRIM().laneToFirstIntersection();
            }
            // Otherwise, there are none.
            return null;
        }
        // Otherwise, it is this one.
        return lane;
    }

    /**
     * Get the distance from the end of this Lane to the last
     * IntersectionManager that this Lane, or any Lane that leads into it
     * entered.  Recursively searches through all previous Lanes.  Returns
     * <code>Double.MAX_VALUE</code> if no such IntersectionManager exists.
     *
     * @return the distance from the end of this Lane to the last
     *         IntersectionManager this Lane, or any lane that leads into it
     *         entered, or <code>Double.MAX_VALUE</code> if no such
     *         IntersectionManager exists
     */
    public double remainingDistanceFromLastIntersection() {
        assert lane instanceof LineSegmentLane;
        assert !intersectionManagers.isEmpty();
        IntersectionManager im = intersectionManagers.get(intersectionManagers.firstKey());
        Point2D entry = im.getIntersection().getEntryPoint(lane);
        Point2D exit = im.getIntersection().getExitPoint(lane);
        double distance = 0.0;
        // Means we just entered the intersection
        if (entry != null) {
            return Double.MAX_VALUE;
        }
        // If we haven't reached the intersection
        if(exit == null) {
            distance += lane.getLength();
            if (lane.hasPrevLane()) {
                Lane prevLane = lane.getPrevLane();
                if (prevLane instanceof  ArcSegmentLane){
                    prevLane = ((ArcSegmentLane) prevLane).getArcLaneDecomposition().get(((ArcSegmentLane) prevLane).getArcLaneDecomposition().size() - 1);
                }
                distance += prevLane.getLength();
                entry = im.getIntersection().getEntryPoint(prevLane);
                exit = im.getIntersection().getExitPoint(prevLane);
                while (prevLane.hasPrevLane() && entry == null && exit == null) {
                    prevLane = prevLane.getPrevLane();
                    distance += prevLane.getLength();
                    entry = im.getIntersection().getEntryPoint(prevLane);
                    exit = im.getIntersection().getExitPoint(prevLane);
                }
            }

            // Means we reached the intersection
            if (entry == null && exit != null) {
                return distance;
            }
            // Means we haven't even entered the intersection
            if (entry == null && exit == null) {
                return Double.MAX_VALUE;
            }
            // Means we were inside the intersection the whole time
           else return 0;
        }
        else {
            distance = lane.getLength();
            return distance;
        }

    }


    // given a point -> next im

    /**
     * Find the next IntersectionManager a vehicle at the given position will
     * encounter. These are indexed based on how far along the lane the vehicle
     * is, from 0 (at the start) to 1 (at the end).
     *
     * @param p the location of the hypothetical vehicle
     * @return  the next IntersectionManager the vehicle will encounter, or
     *          <code>null</code> if none
     */
    public IntersectionManager nextIntersectionManager(Point2D p) {
        // First find how far along the point is.
        double index = lane.normalizedDistanceAlongLane(p);
        SortedMap<Double, IntersectionManager> remaining =
                intersectionManagers.tailMap(index);
        // If nothing left, then no more IntersectionManagers
        if (remaining.isEmpty()) {
            if (lane.hasNextLane()) {
                return lane.getNextLane().getLaneRIM().firstIntersectionManager();
            } else {
                return null;
            }
        } else {
            return remaining.get(remaining.firstKey());
        }
    }

    /**
     * Find the distance to the next IntersectionManager a vehicle at the given
     * position will encounter.  First projects the point onto the Lane.
     *
     * @param p the current location of the vehicle
     * @return  the distance along the Lane from the point on the Lane nearest
     *          to the given point to the next IntersectionManager a vehicle
     *          at the given point will encounter; if there is no next
     *          intersection, return Double.MAX_VALUE
     */
    public double distanceToNextIntersection(Point2D p) {
        // Will always be the first intersection manager if we have just one intersection
        // First determine how far along the Lane we are
        Lane lineLane = lane;
        if (lane instanceof ArcSegmentLane){
            lineLane = ((ArcSegmentLane) lane).getArcLaneDecomposition().get(0);
        }
        assert lineLane instanceof LineSegmentLane;
        double index = lineLane.normalizedDistanceAlongLane(p);
        if (index >= 0) {
            return lineLane.getLaneRIM().distanceToFirstIntersection() - index * lineLane.getLength();
        }
        // Else we may have not reached the intersection
        else {
            return lineLane.getLaneRIM().distanceToFirstIntersection();
        }

    }


    // given a point -> prev im

    /**
     * Find the distance from a point, projected onto the Lane, to the previous
     * intersection that a vehicle at that position on the Lane would have
     * encountered. Returns Double.MAX_VALUE if point not on the lane or no previous intersection
     *
     * @param p the current location of the vehicle
     * @return  the distance from a point, projected onto the Lane, to the
     *          previous intersection that a vehicle at that position on the
     *          Lane would have encountered
     */
    public double distanceFromPrevIntersection(Point2D p) {
        assert lane instanceof LineSegmentLane;
        assert !intersectionManagers.isEmpty();
        // First determine how far along the Lane we are
        double index = lane.normalizedDistanceAlongLane(p);
        if (index >= 0) {
            double distance = lane.getLaneRIM().remainingDistanceFromLastIntersection() - (1-index) * lane.getLength();
            // Means we just got into the intersection
            if (distance < 0) {
                return 0.0;
            }
            else return distance;
        }
        else return Double.MAX_VALUE;
    }

    // given an im

    /**
     * Get the IntersectionManager that this Lane, or any Lane it leads into
     * enters, after the given IntersectionManager.
     *
     * @param im the IntersectionManager to which we would like the successor
     * @return   the IntersectionManager that this Lane, or any Lane it leads
     *           into enters, after the given IntersectionManager
     */
    public IntersectionManager nextIntersectionManager(IntersectionManager im) {
        // There are no more intersection managers after this one
        return null;
    }

    /**
     * Get the approximate time from the given IntersectionManager to the next
     * one that that this Lane, or any Lane it leads into enters, based on
     * distances and speed limits.
     *
     * @param im          the IntersectionManager at which to start
     * @param maxVelocity the maximum velocity of the vehicle
     * @return            the time, in seconds, that it should take once
     *                    departing the given IntersectionManager, to reach the
     *                    next IntersectionManager
     */
    public double timeToNextIntersectionManager(IntersectionManager im,
                                                double maxVelocity) {
        // Two cases: either the next intersection is in this Lane, or it is
        // in a Lane connected to this one
        IntersectionManager nextIM = nextIntersectionManager(im);
        if(nextIM == null) {
            // If there's no next intersection, we just return 0 since the
            // behavior isn't well defined
            return 0;
        }
        if(nextIM.getIntersection().isEnteredBy(lane)) {
            // This is the easy case: just find the distance to the next
            // intersection and divide by the speed limit
            return im.getIntersection().getExitPoint(lane).distance(
                    nextIM.getIntersection().getEntryPoint(lane)) /
                    Math.min(lane.getSpeedLimit(), maxVelocity);
        } else {
            // This is more challenging.  We need to keep adding it up the Lanes
            // in between until we find it
            // Start with the distance to the end of this Lane
            double totalTime = remainingDistanceFromLastIntersection() /
                    lane.getSpeedLimit();
            Lane currLane = lane.getNextLane();
            // Okay, add up all the lanes until the IM
            while(!nextIM.getIntersection().isEnteredBy(currLane)) {
                totalTime += currLane.getLength() /
                        Math.min(currLane.getSpeedLimit(), maxVelocity);
                currLane = currLane.getNextLane();
            }
            // Now we're at the Lane that actually enters the next IM
            totalTime += currLane.getLaneRIM().distanceToFirstIntersection() /
                    Math.min(currLane.getSpeedLimit(), maxVelocity);
            return totalTime;
        }
    }


}
