package aim4.driver.rim.pilot;

import aim4.config.SimConfig;
import aim4.driver.DriverUtil;
import aim4.driver.rim.RIMAutoDriver;
import aim4.driver.rim.coordinator.V2ICoordinator.ReservationParameter;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import aim4.vehicle.AutoVehicleDriverModel;
import aim4.vehicle.VehicleUtil;
import aim4.vehicle.rim.RIMAutoVehicleDriverModel;

import java.awt.geom.Point2D;
import java.util.Iterator;
import java.util.Queue;

import static aim4.config.Debug.currentRimMap;

/**
 * An agent that pilots a {@link AutoVehicleDriverModel} autonomously. This agent
 * attempts to emulate the behavior of a real-world autonomous driver agent in
 * terms of physically controlling the Vehicle.
 */
public class V2IPilot {
    // ///////////////////////////////
    // CONSTANTS
    // ///////////////////////////////

    /**
     * The minimum distance to maintain between the Vehicle controlled by this
     * AutonomousPilot and the one in front of it. {@value} meters.
     */
    public static double MINIMUM_FOLLOWING_DISTANCE; // meters

    /**
     * The default shortest distance before an intersection at which the vehicle
     * stops if the vehicle can't enter the intersection immediately.
     */
    public static double DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

    /**
     * The distance, expressed in units of the Vehicle's velocity, at which to
     * switch to a new lane when turning. {@value} seconds.
     */
    public static final double TRAVERSING_LANE_CHANGE_LEAD_TIME = 1.5; // sec

    // ///////////////////////////////
    // PRIVATE FIELDS
    // ///////////////////////////////

    private double stopDistanceBeforeIntersection;

    private RIMAutoVehicleDriverModel vehicle;

    private RIMAutoDriver driver;

    // ///////////////////////////////
    // CONSTRUCTORS
    // ///////////////////////////////

    /**
     * Create an pilot to control a vehicle.
     *
     * @param vehicle      the vehicle to control
     * @param driver       the driver
     */
    public V2IPilot(RIMAutoVehicleDriverModel vehicle, RIMAutoDriver driver) {
        this.vehicle = vehicle;
        this.driver = driver;
        stopDistanceBeforeIntersection = DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION;
    }

    // ///////////////////////////////
    // PUBLIC METHODS
    // ///////////////////////////////


    /**
     * Get the vehicle this pilot controls.
     */
    public AutoVehicleDriverModel getVehicle() {
        return vehicle;
    }

    /**
     * Get the driver this pilot controls.
     */
    public RIMAutoDriver getDriver() {
        return driver;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // steering

    /**
     * Set the steering action when the vehicle is traversing an intersection.
     */
    public void takeSteeringActionForTraversing(ReservationParameter rp) {
        LineSegmentLane departureLineLane;

        Road currentRoad = ((RimIntersectionMap) currentRimMap).getRoadByDecompositionLane(driver.getCurrentLane());
        int laneIndex = currentRoad.findLaneIndex(driver.getCurrentLane());
        
        boolean isFinalLane = driver.getCurrentLane() == rp.getDepartureLane();

        if (driver.getCurrentLane() instanceof LineSegmentLane && rp.getDepartureLane() instanceof ArcSegmentLane) {
            departureLineLane = ((ArcSegmentLane) rp.getDepartureLane()).getArcLaneDecomposition().get(0);
            isFinalLane = driver.getCurrentLane().getEndPoint().distance(departureLineLane.getStartPoint()) < 0.001;
        }

        // If we're not already in the departure lane
        if(!isFinalLane)
        {
            Lane nextLane = driver.getCurrentLane().getNextLane();
            // If next lane is an arc then we are entering the intersection
            if (nextLane instanceof ArcSegmentLane) {
                LineSegmentLane arrivalLineLane = ((ArcSegmentLane) nextLane).getArcLaneDecomposition().get(0);
                driver.setCurrentLane(arrivalLineLane);
            }
            // Else we are already inside the intersection
            else if (nextLane instanceof LineSegmentLane) {
                // If we are really close to the next lane
                double remainingDistanceAlongCurrentLane = driver.getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());
                if (remainingDistanceAlongCurrentLane < 0.001) {
                    // Check if need to change roads
                    Road departureRoad = ((RimIntersectionMap) currentRimMap).getRoadByDecompositionLane(rp.getDepartureLane());

                    // Means we may have to change the road
                    ArcSegmentLane firstExitDepartureLane = ((ArcSegmentLane) departureRoad.getExitMergingLane(laneIndex));
                    LineSegmentLane firstExitDepartureLineLane = firstExitDepartureLane.getArcLaneDecomposition().get(0);

                    if (driver.getCurrentLane().getEndPoint().distance(firstExitDepartureLineLane.getStartPoint()) < 0.001) {
                        // We need to exit at the fist exit
                        nextLane = firstExitDepartureLineLane;
                        driver.setCurrentLane(nextLane);
                    }

                    else {
                        // Check if we need to exit at the second roundabout exit
                        ArcSegmentLane secondExitDepartureLane = ((ArcSegmentLane) departureRoad.getContinuousLanesForLane(laneIndex).get(4));
                        LineSegmentLane secondExitDepartureLineLane = secondExitDepartureLane.getArcLaneDecomposition().get(0);

                        if (driver.getCurrentLane().getEndPoint().distance(secondExitDepartureLineLane.getStartPoint()) < 0.001) {
                            nextLane = secondExitDepartureLineLane;
                            driver.setCurrentLane(nextLane);
                        }

                        else { // We haven't reached an exit point yet
                            driver.setCurrentLane(nextLane);
                        }
                    }

                }
            }
        }
        if (isFinalLane) {
            double remainingDistanceAlongCurrentLane = driver.getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());
            if (remainingDistanceAlongCurrentLane < 0.001 && driver.getCurrentLane().hasNextLane()) {
                driver.setCurrentLane(driver.getCurrentLane().getNextLane());
            }
        }
        // Use the basic lane-following behavior
        followCurrentLane(rp);
    }

    /**
     * Set the steering action when the vehicle is approaching the intersection.
     */
    public void takeSteeringActionForThrottle() {

        double remainingDistanceAlongCurrentLane = driver.getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());
        if (remainingDistanceAlongCurrentLane < 0.001 && driver.getCurrentLane().hasNextLane()) {
            Lane nextLane;
            if (driver.getCurrentLane().getNextLane() instanceof ArcSegmentLane) {
                nextLane = ((ArcSegmentLane) driver.getCurrentLane().getNextLane()).getArcLaneDecomposition().get(0);
            }
            else {
                nextLane = driver.getCurrentLane().getNextLane();
            }
            driver.setCurrentLane(nextLane);
        }

        // Use the basic lane-following behavior
        followCurrentLane(null);
    }


    /**
     * Turn the wheels to follow the current lane, using the given lead time.
     * This involves first projecting the Vehicle's current position onto the
     * lane, and then projecting forward by a distance equal to the Vehicle's
     * velocity multiplied by the lead time.
     *
     */
    public void followCurrentLane(ReservationParameter rp) {
        // Lead distance will always be half of every LineLane
        double leadDist = driver.getCurrentLane().getLength() / 4;
        Point2D aimPoint;
        // Knowing how much of this lane is left
        double remaining = driver.getCurrentLane().remainingDistanceAlongLane(vehicle.gaugePosition());

        // If nothing left then we may have to change the lane
//        if (rp == null && remaining > 0){
//            aimPoint = driver.getCurrentLane().getNextLane().getStartPoint();
//            getVehicle().turnTowardPoint(aimPoint);
//            return;
//        }
        if (remaining < 0.001) {
            if (rp == null){
                if (remaining < 0 && driver.getCurrentLane().hasNextLane()) {
                    takeSteeringActionForThrottle();
                    return;
                } else if (remaining >= 0.0) {
                    return;
                }
            } else {
                // We may have to change the lane
                takeSteeringActionForTraversing(rp);
                return;
            }
        }
        else {
            aimPoint = driver.getCurrentLane().getLeadPoint(vehicle.gaugePosition(), leadDist);
            getVehicle().turnTowardPoint(aimPoint);
        }
    }

    /**
     * Maintain a cruising speed.
     */
    private void cruise() {
        getVehicle().setTargetVelocityWithMaxAccel(
                DriverUtil.calculateMaxFeasibleVelocity(getVehicle()));
    }

    // throttle actions

    /**
     * Follow the acceleration profile received as part of a reservation
     * confirmation from an IntersectionManager. If none exists, or if it is
     * empty, just cruise. Modifies the acceleration profile to reflect the
     * portion it has consumed.
     *
     * TODO: do not modify the acceleration profile
     */
    public void followAccelerationProfile(ReservationParameter rp) {
        Queue<double[]> accelProf = rp.getAccelerationProfile();
        // If we have no profile or we have finished with it, then just do our
        // best to maintain a cruising speed
        if ((accelProf == null) || (accelProf.isEmpty())) {
            // Maintain a cruising speed while in the intersection, but slow for
            // other vehicles. Also do not go above the maximum turn velocity.
            vehicle.setTargetVelocityWithMaxAccel(calculateIntersectionVelocity(rp));
        } else {
            // Otherwise, we need to figure out what the next directive in the
            // profile is - peek at the front of the list
            double[] currentDirective = accelProf.element();
            // Now, we have three cases. Either there is more than enough duration
            // left at this acceleration to do only this acceleration:
            if (currentDirective[1] > SimConfig.TIME_STEP) {
                // This is easy, just do the requested acceleration and decrement
                // the duration
                vehicle.setAccelWithMaxTargetVelocity(currentDirective[0]);
                currentDirective[1] -= SimConfig.TIME_STEP;
            } else if (currentDirective[1] < SimConfig.TIME_STEP) {
                // Or we have to do a weighted average
                double totalAccel = 0.0;
                double remainingWeight = SimConfig.TIME_STEP;
                // Go through each of the acceleration, duration pairs and do a
                // weighted average of the first time step's worth of accelerations
                for (Iterator<double[]> iter = accelProf.iterator(); iter.hasNext();) {
                    currentDirective = iter.next();
                    if (currentDirective[1] > remainingWeight) {
                        // Yay! More than enough here to finish out
                        totalAccel += remainingWeight * currentDirective[0];
                        // Make sure to record the fact that we used up some of it
                        currentDirective[1] -= remainingWeight;
                        // And that we satisfied the whole time step
                        remainingWeight = 0.0;
                        break;
                    } else if (currentDirective[1] < remainingWeight) {
                        // Ugh, we have to do it again
                        totalAccel += currentDirective[1] * currentDirective[0];
                        remainingWeight -= currentDirective[1];
                        iter.remove(); // done with this one
                    } else { // currentDirective[1] == remainingWeight
                        // This finishes off the list perfectly
                        totalAccel += currentDirective[1] * currentDirective[0];
                        // And completes our requirements for a whole time step
                        remainingWeight = 0.0;
                        iter.remove(); // done with this oneo
                        break;
                    }
                }
                // Take care of the case in which we didn't have enough for the
                // whole time step
                if (remainingWeight > 0.0) {
                    totalAccel += remainingWeight * currentDirective[0];
                }
                // Okay, totalAccel should now have our total acceleration in it
                // So we need to divide by the total weight to get an actual
                // acceleration
                vehicle.setAccelWithMaxTargetVelocity(totalAccel
                        / SimConfig.TIME_STEP);
            } else { // Or things work out perfectly and we use this one up
                // This is easy, just do the requested acceleration and remove the
                // element from the queue
                accelProf.remove();
                vehicle
                        .setAccelWithMaxTargetVelocity(currentDirective[0]);
            }
        }
    }

    /**
     * Determine the maximum velocity at which the Vehicle should travel in the
     * intersection given the Lanes in which it is.
     *
     * @return the maximum velocity at which the Vehicle should travel in the
     *         intersection given the Lane in which it is
     */
    private double calculateIntersectionVelocity(ReservationParameter rp) {
        return VehicleUtil.maxTurnVelocity(vehicle.getSpec(),
                rp.getArrivalLane(),
                rp.getDepartureLane(),
                driver.getCurrentRIM());
    }

    /**
     * The simple throttle action.
     */
    public void simpleThrottleAction() {
        cruise();
        dontHitVehicleInFront();
        dontEnterIntersection();
    }

    /**
     * Stop before hitting the car in front of us.
     *
     */
    private void dontHitVehicleInFront() {
//    double stoppingDistance = distIfStopNextTimeStep(vehicle);
        double stoppingDistance =
                VehicleUtil.calcDistanceToStop(vehicle.gaugeVelocity(),
                        vehicle.getSpec().getMaxDeceleration());
        double followingDistance = stoppingDistance + MINIMUM_FOLLOWING_DISTANCE;
        if (VehicleUtil.distanceToCarInFront(vehicle) < followingDistance) {
            vehicle.slowToStop();
        }
    }

    /**
     * Stop before entering the intersection.
     */
    private void dontEnterIntersection() {
        double stoppingDistance = distIfStopNextTimeStep();
//    double stoppingDistance =
//      VehicleUtil.calcDistanceToStop(vehicle.gaugeVelocity(),
//                                     vehicle.getSpec().getMaxDeceleration());
        double minDistanceToIntersection =
                stoppingDistance + DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION;
        if (vehicle.getDriver().distanceToNextIntersection() <
                minDistanceToIntersection) {
//      if (Debug.isTargetVIN(vehicle.getVIN())) {
//        System.err.printf("at time %.2f, slow down since %.5f < %.5f\n",
//                          vehicle.gaugeTime(),
//                          vehicle.getDriver().distanceToNextIntersection(),
//                          minDistanceToIntersection);
//        System.err.printf("v = %.5f\n", vehicle.gaugeVelocity());
//      }
            vehicle.slowToStop();
//    } else {
//      if (Debug.isTargetVIN(vehicle.getVIN())) {
//        System.err.printf("Skipped update of acceleration\n");
//        System.err.printf(" %.5f < %.5f\n",
//                          vehicle.getDriver().distanceToNextIntersection(),
//                          minDistanceToIntersection);
//      }
        }
    }

    /**
     * Determine how far the vehicle will go if it waits until the next time
     * step to stop.
     *
     * @return How far the vehicle will go if it waits until the next time
     *         step to stop
     */
    private double distIfStopNextTimeStep() {
        double distIfAccel = VehicleUtil.calcDistanceIfAccel(
                vehicle.gaugeVelocity(),
                vehicle.getSpec().getMaxAcceleration(),  // TODO: why max accel here?
                DriverUtil.calculateMaxFeasibleVelocity(vehicle),
                SimConfig.TIME_STEP);
        double distToStop = VehicleUtil.calcDistanceToStop(
                speedNextTimeStepIfAccel(),
                vehicle.getSpec().getMaxDeceleration());
        return distIfAccel + distToStop;
    }


    /**
     * Calculate the velocity of the vehicle at the next time step, if we choose
     * to accelerate at this time step.
     *
     * @return the velocity of the vehicle at the next time step, if we choose
     *         to accelerate at this time step
     */
    private double speedNextTimeStepIfAccel(){
        // Our speed at the next time step will be either the target speed
        // or as fast as we can go, whichever is smaller
        return Math.min(DriverUtil.calculateMaxFeasibleVelocity(vehicle),
                vehicle.gaugeVelocity() +
                        vehicle.getSpec().getMaxAcceleration() *
                                SimConfig.TIME_STEP);
    }
}
