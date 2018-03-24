package aim4.sim.simulator.rim.helper;

import aim4.config.Debug;
import aim4.driver.rim.RIMAutoDriver;
import aim4.im.rim.IntersectionManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.vehicle.rim.RIMAutoVehicleSimModel;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.*;

public class SensorInputHelper {
    BasicRIMIntersectionMap map;
    Map<Integer, RIMVehicleSimModel> vinToVehicles;

    /**
     * Provides sensor input for the
     *
     * @param map
     * @param vinToVehicles
     */
    public SensorInputHelper(BasicRIMIntersectionMap map, Map<Integer, RIMVehicleSimModel> vinToVehicles) {
        this.map = map;
        this.vinToVehicles = vinToVehicles;
    }

    /**
     * Provides sensor input to all of the vehicles on all of the lanes.
     */
    public void provideSensorInput() {
        Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists = computeVehicleLists();
        Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle = computeNextVehicle(vehicleLists);

        provideIntervalInfo(nextVehicle);
        providePrecedingVehicleVIN(nextVehicle);
        provideVehicleTrackingInfo(vehicleLists);
    }

    /**
     * Compute the lists of vehicles of all lanes.
     *
     * @return a mapping from lanes to lists of vehicles sorted by their
     *         distance on their lanes
     */
    private Map<Lane,SortedMap<Double,RIMVehicleSimModel>> computeVehicleLists() {
        // Set up the structure that will hold all the Vehicles as they are
        // currently ordered in the Lanes
        Map<Lane,SortedMap<Double,RIMVehicleSimModel>> vehicleLists =
                new HashMap<Lane,SortedMap<Double,RIMVehicleSimModel>>();
        for(Road road : Debug.currentRimMap.getRoads()) {
            for (Lane lane : road.getContinuousLanes()) {
                if (lane instanceof ArcSegmentLane) {
                    ((ArcSegmentLane) lane).getArcLaneDecomposition().forEach(lineSegmentLane -> {
                        vehicleLists.put(lineSegmentLane, new TreeMap<Double,RIMVehicleSimModel>());
                    });
                }
                else vehicleLists.put(lane, new TreeMap<Double,RIMVehicleSimModel>());
            }

        }
        // Now add each of the Vehicles, but make sure to exclude those that are
        // already inside (partially or entirely) the intersection
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            // Find out what lanes it is in.
            Set<Lane> lanes = vehicle.getDriver().getCurrentlyOccupiedLanes();
            for(Lane lane : lanes) {
                // Find out what IntersectionManager is coming up for this vehicle
                IntersectionManager im =
                        lane.getLaneRIM().nextIntersectionManager(vehicle.getPosition());
                // Only include this Vehicle if it is not in the intersection.
                if(im == null ||
                        !(im.intersectsPoint(vehicle.getPosition()) && im.intersectsPoint(vehicle.getPointAtRear()))) {
                    // Now find how far along the lane it is.
                    double dst = lane.distanceAlongLane(vehicle.getPosition());
                    // Now add it to the map.
                    vehicleLists.get(lane).put(dst, vehicle);
                    // Now check if this vehicle intersects any other lanes
                    for (Road road : Debug.currentRimMap.getRoads()) {
                        for (Lane otherLane : road.getContinuousLanes()) {
                            if (otherLane.getId() != lane.getId() && otherLane.getShape().getBounds2D().intersects(vehicle.getShape().getBounds2D())) {
                                if (otherLane instanceof ArcSegmentLane) {
                                    for (LineSegmentLane otherLineLane : ((ArcSegmentLane) otherLane).getArcLaneDecomposition()){
                                        if (otherLineLane.getId() != lane.getId() && otherLineLane.getShape().getBounds2D().intersects(vehicle.getShape().getBounds2D())){
                                            double interval = Double.MAX_VALUE ;
                                            for(Line2D edge : vehicle.getEdges()) {
                                                double dstAlongOtherLane = edge.ptSegDist(otherLineLane.getStartPoint());
                                                if(dstAlongOtherLane < interval){
                                                    interval = dstAlongOtherLane;
                                                }
                                            }
                                            if (interval < Double.MAX_VALUE) {
                                                vehicleLists.get(otherLineLane).put(interval, vehicle);
                                            }
                                        }
                                    }
                                }
                                else if (otherLane instanceof LineSegmentLane) {
                                    if (otherLane.getId() != lane.getId() && otherLane.getShape().getBounds2D().intersects(vehicle.getShape().getBounds2D())){
                                        double interval = Double.MAX_VALUE ;
                                        for(Line2D edge : vehicle.getEdges()) {
                                            double dstAlongOtherLane = edge.ptSegDist(otherLane.getStartPoint());
                                            if(dstAlongOtherLane < interval){
                                                interval = dstAlongOtherLane;
                                            }
                                        }
                                        if (interval < Double.MAX_VALUE) {
                                            vehicleLists.get(otherLane).put(interval, vehicle);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return vehicleLists;
    }

    /**
     * Compute the next vehicles of all vehicles.
     *
     * @param vehicleLists  a mapping from lanes to lists of vehicles sorted by
     *                      their distance on their lanes
     * @return a mapping from vehicles to next vehicles
     */
    private Map<RIMVehicleSimModel, RIMVehicleSimModel> computeNextVehicle(
            Map<Lane,SortedMap<Double,RIMVehicleSimModel>> vehicleLists) {
        // At this point we should only have mappings for start Lanes, and they
        // should include all the Lanes they run into.  Now we need to turn this
        // into a hash map that maps Vehicles to the next vehicle in the Lane
        // or any Lane the Lane runs into
        Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle =
                new HashMap<RIMVehicleSimModel,RIMVehicleSimModel>();
        // For each of the ordered lists of vehicles
        for(SortedMap<Double,RIMVehicleSimModel> vehicleList : vehicleLists.values()) {
            RIMVehicleSimModel lastVehicle = null;
            // Go through the Vehicles in order of their position in the Lane
            for(RIMVehicleSimModel currVehicle : vehicleList.values()) {
                if(lastVehicle != null) {
                    // Create the mapping from the previous Vehicle to the current one
                    nextVehicle.put(lastVehicle, currVehicle);
                }
                lastVehicle = currVehicle;
            }
        }
        // Now link the vehicles
        for (Lane lane: vehicleLists.keySet()) {
            if (vehicleLists.get(lane).size() > 0) {
                // Means we need to link the last vehicle from this lane
                SortedMap<Double, RIMVehicleSimModel> beforeVehicles = vehicleLists.get(lane);
                Double lastKeyBefore = beforeVehicles.lastKey();
                RIMVehicleSimModel lastVehicleBefore = beforeVehicles.get(lastKeyBefore);
                // With the first vehicle from the next continuous lane we find
                if (lane.hasNextLane() && nextVehicle.get(lastKeyBefore) == null){
                    Lane nextLane;
                    if (lane instanceof ArcSegmentLane) {
                        nextLane = ((ArcSegmentLane) lane).getArcLaneDecomposition().get(0);
                    }
                    else nextLane = lane.getNextLane();
                    if (nextLane instanceof ArcSegmentLane) {
                        nextLane = ((ArcSegmentLane) nextLane).getArcLaneDecomposition().get(0);
                    }
                    boolean found = false;
                    //If there are vehicles in this lane
                    if (vehicleLists.get(nextLane) != null && vehicleLists.get(nextLane).size() > 0) {
                        SortedMap<Double, RIMVehicleSimModel> afterVehicles = vehicleLists.get(nextLane);
                        for (RIMVehicleSimModel firstVehicleAfter : afterVehicles.values()) {
                            if (firstVehicleAfter.getVIN() != lastVehicleBefore.getVIN()) {
                                nextVehicle.put(lastVehicleBefore, firstVehicleAfter);
                                found = true;
                                break;
                            }
                        }
                    }
                    if (!found) {
                        while (nextLane.hasNextLane()) {
                            Lane nextNextLane = nextLane.getNextLane();
                            boolean foundAgain = false;
                            //If there are vehicles in this lane
                            if (vehicleLists.get(nextNextLane) != null && vehicleLists.get(nextNextLane).size() > 0) {
                                SortedMap<Double, RIMVehicleSimModel> afterVehicles = vehicleLists.get(nextNextLane);
                                for (RIMVehicleSimModel firstVehicleAfter : afterVehicles.values()){
                                    if (firstVehicleAfter.getVIN() != lastVehicleBefore.getVIN()) {
                                        nextVehicle.put(lastVehicleBefore, firstVehicleAfter);
                                        foundAgain = true;
                                        break;
                                    }
                                }
                                if (foundAgain) {
                                    break;
                                }
                            }
                            if (!foundAgain) {
                                nextLane = nextNextLane;
                            }

                        }
                    }
                }
            }

        }

        return nextVehicle;
    }

    private void provideIntervalInfo(Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle) {
        for (RIMVehicleSimModel rimVehicle : vinToVehicles.values()) {
            if (rimVehicle instanceof RIMAutoVehicleSimModel) {
                RIMAutoVehicleSimModel autoVehicle = (RIMAutoVehicleSimModel) rimVehicle;

                double interval;
                if (nextVehicle.containsKey(autoVehicle)) {
                    interval = calcInterval(autoVehicle, nextVehicle.get(autoVehicle));
                } else {
                    interval = Double.MAX_VALUE;
                }

                autoVehicle.getIntervalometer().record(interval);
            }
        }

    }

    private void providePrecedingVehicleVIN(Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle) {
        for (RIMVehicleSimModel rimVehicle : vinToVehicles.values()) {
            if(rimVehicle instanceof RIMAutoVehicleSimModel) {
                RIMAutoVehicleSimModel autoVehicle = (RIMAutoVehicleSimModel) rimVehicle;

                if(nextVehicle.containsKey(autoVehicle))
                    autoVehicle.setPrecedingVehicleVIN(nextVehicle.get(autoVehicle).getVIN());
                else
                    autoVehicle.setPrecedingVehicleVIN(0);
            }
        }
    }

    private void provideVehicleTrackingInfo(Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists) {
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            if(vehicle instanceof RIMVehicleSimModel) {
                RIMAutoVehicleSimModel autoVehicle = (RIMAutoVehicleSimModel) vehicle;

                if(autoVehicle.isVehicleTracking()) {
                    RIMAutoDriver driver = autoVehicle.getDriver();
                    Lane targetLaneForTracking = autoVehicle.getTargetLaneForVehicleTracking();
                    Point2D pos = autoVehicle.getPosition();
                    double dst = targetLaneForTracking.distanceAlongLane(pos);

                    //initialise distances to infinity
                    double frontDst = Double.MAX_VALUE;
                    double rearDst = Double.MAX_VALUE;
                    RIMVehicleSimModel frontVehicle = null;
                    RIMVehicleSimModel rearVehicle = null;

                    //only consider the vehicles on the target tracking lane
                    SortedMap<Double, RIMVehicleSimModel> vehiclesOnTargetLane =
                            vehicleLists.get(targetLaneForTracking);

                    // compute the distances and the corresponding vehicles
                    try {
                        double d = vehiclesOnTargetLane.tailMap(dst).firstKey();
                        frontVehicle = vehiclesOnTargetLane.get(d);
                        frontDst = (d-dst)-frontVehicle.getSpec().getLength();
                    } catch(NoSuchElementException e) {
                        frontDst = Double.MAX_VALUE;
                        frontVehicle = null;
                    }
                    try {
                        double d = vehiclesOnTargetLane.headMap(dst).lastKey();
                        rearVehicle = vehiclesOnTargetLane.get(d);
                        rearDst = dst-d;
                    } catch(NoSuchElementException e) {
                        rearDst = Double.MAX_VALUE;
                        rearVehicle = null;
                    }

                    //assign the sensor readings

                    autoVehicle.getFrontVehicleDistanceSensor().record(frontDst);
                    autoVehicle.getRearVehicleDistanceSensor().record(rearDst);

                    //assign the vehicles' velocities
                    if(frontVehicle!=null){
                        autoVehicle.getFrontVehicleSpeedSensor().record(
                                frontVehicle.getVelocity());
                    } else {
                        autoVehicle.getFrontVehicleSpeedSensor().record(Double.MAX_VALUE);
                    }
                    if(rearVehicle!=null) {
                        autoVehicle.getRearVehicleSpeedSensor().record(
                                rearVehicle.getVelocity());
                    } else {
                        autoVehicle.getRearVehicleSpeedSensor().record(Double.MAX_VALUE);
                    }
                }
            }
        }
    }

    private double calcInterval(RIMVehicleSimModel vehicle, RIMVehicleSimModel nextVehicle) {
        Point2D pos = vehicle.getPosition();
        if(nextVehicle.getShape().contains(pos)) {
            return 0.0;
        } else {
            double interval = Double.MAX_VALUE;
            for(Line2D edge : nextVehicle.getEdges()) {
                double dst = edge.ptSegDist(pos);
                if(dst < interval) {
                    interval = dst;
                }
            }
            return interval;
        }
    }
}
