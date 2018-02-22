package aim4.driver.rim;

import aim4.driver.BasicDriver;
import aim4.driver.DriverUtil;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import aim4.vehicle.VehicleDriverModel;

import java.awt.geom.Point2D;

import static aim4.config.Debug.currentMap;

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
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Take steering actions to guide a test vehicle through a simulated
     * traversal of the intersection.
     */
    public void act(Point2D position) {
        // todo: handle u turns
        super.act();
        // If we're not already in the departure lane
        if(getCurrentLane() != departureLane ||
                // and we are not already in the first line lane of the departure lane
                (getCurrentLane() instanceof LineSegmentLane && departureLane instanceof ArcSegmentLane &&
                        ((ArcSegmentLane) departureLane).getArcLaneDecomposition().get(0) != getCurrentLane())) {

            Lane nextLane = getCurrentLane().getNextLane();
            // If next lane is an arc then we are entering the intersection
            if (nextLane instanceof ArcSegmentLane) {
                setCurrentLane(((ArcSegmentLane) nextLane).getArcLaneDecomposition().get(0));
            }
            // Else we are already inside the intersection
            else if (nextLane instanceof LineSegmentLane) {
                // If we still have remaining distance along the current lane
                if ((float) position.getX() != (float) getCurrentLane().getEndPoint().getX() &&
                        (float) position.getY() != (float) getCurrentLane().getEndPoint().getY()){
                    // do nothing as current lane does not change
                }
                // we don't need to change directions
                else setCurrentLane(nextLane);
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
     * Turn the wheels to follow the current lane, using the
     * <code>DEFAULT_LEAD_TIME</code>. This involves first projecting
     * the Vehicle's current position onto the lane, and then projecting
     * forward by a distance equal to the Vehicle's velocity multiplied
     * by the lead time.
     */
    private void followCurrentLane() {
        followCurrentLane(DriverUtil.DEFAULT_LEAD_TIME);
    }


    /**
     * Turn the wheels to follow the current lane, using the given lead time.
     * This involves first projecting the Vehicle's current position onto the
     * lane, and then projecting forward by a distance equal to the Vehicle's
     * velocity multiplied by the lead time.
     *
     * @param leadTime the lead time to use
     */
    private void followCurrentLane(double leadTime) {
        double leadDist = leadTime * vehicle.gaugeVelocity() +
                DriverUtil.MIN_LEAD_DIST;
        Point2D aimPoint;
        double remaining = getCurrentLane().
                remainingDistanceAlongLane(vehicle.gaugePosition());
        // If there's not enough room in this Lane and there is a Lane that this
        // Lane leads into, use the next Lane
        if((leadDist > remaining) && (getCurrentLane().hasNextLane())) {
            // First make sure we shouldn't transfer to the next lane
            if(remaining <= 0) {
                // Check if needed to exit road at first disjunction
                if ((float) (getCurrentLane().getEndPoint().getX()) == (float) (((RimIntersectionMap) currentMap).getRoadByDecompositionLane(departureLane).getExitMergingLane().getStartPoint().getX())
                        && (float) (getCurrentLane().getEndPoint().getY()) == (float) (((RimIntersectionMap) currentMap).getRoadByDecompositionLane(departureLane).getExitMergingLane().getStartPoint().getY())) {
                    setCurrentLane(((ArcSegmentLane) ((RimIntersectionMap) currentMap)
                            .getRoadByDecompositionLane(departureLane)
                            .getExitMergingLane())
                            .getArcLaneDecomposition()
                            .get(0));
                    // Check if needed to exit road at second disjunction
                } else if ((float) (getCurrentLane().getEndPoint().getX()) == (float) (((RimIntersectionMap) currentMap).getRoadByDecompositionLane(getCurrentLane()).getExitMergingLane().getStartPoint().getX())
                        && (float) (getCurrentLane().getEndPoint().getY()) == (float) (((RimIntersectionMap) currentMap).getRoadByDecompositionLane(getCurrentLane()).getExitMergingLane().getStartPoint().getY()) &&
                        ((RimIntersectionMap) currentMap).getRoadByDecompositionLane(getCurrentLane()).getName() != ((RimIntersectionMap) currentMap).getRoadByDecompositionLane(departureLane).getName()) {
                    setCurrentLane(((ArcSegmentLane) ((RimIntersectionMap) currentMap)
                            .getRoadByDecompositionLane(departureLane)
                            .getContinuousLanes().get(4))
                            .getArcLaneDecomposition()
                            .get(0));
                }   // Keep going on the same road
                    else {
                    // Switch to the next Lane
                    setCurrentLane(getCurrentLane().getNextLane());
                }
                // And do this over
                followCurrentLane(leadTime);
                return;
            }
            // Use what's left over after this Lane to go into the next one.
            aimPoint = getCurrentLane().getNextLane().getLeadPoint(
                    getCurrentLane().getNextLane().getStartPoint(),
                    leadDist - remaining);
        } else { // Otherwise, use the current Lane
            aimPoint = getCurrentLane().getLeadPoint(
                    vehicle.gaugePosition(), leadDist);
        }
        turnTowardPoint(aimPoint);
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
