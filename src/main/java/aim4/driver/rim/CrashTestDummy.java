package aim4.driver.rim;

import aim4.driver.BasicDriver;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import aim4.vehicle.VehicleDriverModel;

import java.awt.geom.Point2D;

import static aim4.config.Debug.currentRimMap;

/**
 * A driver agent that only steers and changes lanes when appropriate.
 */
public class CrashTestDummy extends BasicDriver {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The vehicle this driver will control */
    private VehicleDriverModel vehicle;

    /** The Lane in which the vehicle should exit the intersection. */
    private Lane departureLane;

    private boolean isFinalLane;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Construct a new CrashTestDummy to pilot the simulated vehicle across
     * an intersection.
     *
     * @param vehicle       the simulated vehicle to pilot
     * @param arrivalLane   the Lane in which the vehicle should enter the
     *                      intersection
     * @param departureLane the Lane in which the vehicle should depart the
     *                      intersection
     */
    public CrashTestDummy(VehicleDriverModel vehicle,
                          Lane arrivalLane, Lane departureLane) {
        this.vehicle = vehicle;
        setCurrentLane(arrivalLane);
        this.departureLane = departureLane;
        this.isFinalLane = false;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Take steering actions to guide a test vehicle through a simulated
     * traversal of the intersection.
     */
    @Override
    public void act() {
        // todo: handle u turns
        super.act();
        LineSegmentLane departureLineLane;

        isFinalLane = getCurrentLane() == departureLane;

        Road currentRoad = ((RimIntersectionMap) currentRimMap).getRoadByDecompositionLane(getCurrentLane());
        int laneIndex = currentRoad.findLaneIndex(getCurrentLane());
        
        if (getCurrentLane() instanceof LineSegmentLane && departureLane instanceof ArcSegmentLane) {
            departureLineLane = ((ArcSegmentLane) departureLane).getArcLaneDecomposition().get(0);
            isFinalLane = getCurrentLane().getEndPoint().distance(departureLineLane.getStartPoint()) < 0.001;
        }

        // If we're not already in the departure lane
        if(!isFinalLane)
        {
            Lane nextLane = getCurrentLane().getNextLane();
            // If next lane is an arc then we are entering the intersection
            if (nextLane instanceof ArcSegmentLane) {
                LineSegmentLane arrivalLineLane = ((ArcSegmentLane) nextLane).getArcLaneDecomposition().get(0);
                setCurrentLane(arrivalLineLane);
            }
            // Else we are already inside the intersection
            else if (nextLane instanceof LineSegmentLane) {
                // If we are really close to the next lane
                double remainingDistanceAlongCurrentLane = getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());
                if (remainingDistanceAlongCurrentLane < 0.001) {
                    // Check if need to change roads
                    Road departureRoad = ((RimIntersectionMap) currentRimMap).getRoadByDecompositionLane(departureLane);

                    // Means we may have to change the road
                    ArcSegmentLane firstExitDepartureLane = ((ArcSegmentLane) departureRoad.getExitMergingLane(laneIndex));
                    LineSegmentLane firstExitDepartureLineLane = firstExitDepartureLane.getArcLaneDecomposition().get(0);

                    if (getCurrentLane().getEndPoint().distance(firstExitDepartureLineLane.getStartPoint()) < 0.001) {
                        // We need to exit at the fist exit
                        nextLane = firstExitDepartureLineLane;
                        setCurrentLane(nextLane);
                    }

                    else {
                        // Check if we need to exit at the second roundabout exit
                        ArcSegmentLane secondExitDepartureLane = ((ArcSegmentLane) departureRoad.getContinuousLanesForLane(laneIndex).get(4));
                        LineSegmentLane secondExitDepartureLineLane = secondExitDepartureLane.getArcLaneDecomposition().get(0);

                        if (getCurrentLane().getEndPoint().distance(secondExitDepartureLineLane.getStartPoint()) < 0.001) {
                            nextLane = secondExitDepartureLineLane;
                            setCurrentLane(nextLane);
                        }

                        else { // We haven't reached an exit point yet
                            setCurrentLane(nextLane);
                        }
                    }

                }
            }
        }
        if (isFinalLane) {
            double remainingDistanceAlongCurrentLane = getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());
            if (remainingDistanceAlongCurrentLane < 0.001 && getCurrentLane().hasNextLane()) {
                setCurrentLane(getCurrentLane().getNextLane());
            }
        }
        // Use the basic lane-following behavior
        followCurrentLane();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public VehicleDriverModel getVehicle() {
        return vehicle;
    }

    // TODO: think about merging the following code with those in AutoV2IPilot
    // actually we don't need this because we will eventually remove this
    // class

    /**
     * Turn the wheels to follow the current lane, using the given lead time.
     * This involves first projecting the Vehicle's current position onto the
     * lane, and then projecting forward by a distance equal to the Vehicle's
     * velocity multiplied by the lead time.
     *
     */
    private void followCurrentLane() {
        // Lead distance will always be half of every LineLane
        double leadDist = getCurrentLane().getLength() / 4;
        Point2D aimPoint;
        // Knowing how much of this lane is left
        double remaining = getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());

        // If nothing left then we may have to change the lane
        if (remaining < 0.001) {
            // We may have to change the lane
            act();
            return;
        }
        else {
            aimPoint = getCurrentLane().getLeadPoint(vehicle.gaugePosition(), leadDist);
            turnTowardPoint(aimPoint);
        }
    }


    /**
     * Turn the wheels toward a given Point.
     *
     * @param p the Point toward which to turn the wheels
     */
    private void turnTowardPoint(Point2D p) {
        vehicle.turnTowardPoint(p);
    }

}
