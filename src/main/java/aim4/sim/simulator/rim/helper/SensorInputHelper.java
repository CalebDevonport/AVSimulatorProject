package aim4.sim.simulator.rim.helper;

import aim4.driver.rim.RIMAutoDriver;
import aim4.im.rim.IntersectionManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
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
     * Computes a list of all of the vehicles on the lanes.
     *
     * @return a mapping from lanes to lists of vehicles sorted by their distance on their lanes.
     */
    private Map<Lane, SortedMap<Double, RIMVehicleSimModel>> computeVehicleLists() {
        //Creating the structure for the vehicle mapping
        Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists =
                new HashMap<Lane, SortedMap<Double, RIMVehicleSimModel>>();
        for (Road road : map.getRoads()) {
            for (Lane lane : road.getContinuousLanes()) {
                if (lane instanceof ArcSegmentLane) {
                    ((ArcSegmentLane) lane).getArcLaneDecomposition().forEach(lineSegmentLane -> {
                        vehicleLists.put(lineSegmentLane, new TreeMap<Double,RIMVehicleSimModel>());
                    });
                }
                else vehicleLists.put(lane, new TreeMap<Double,RIMVehicleSimModel>());
            }
        }

        //Adding all of the vehicles
        for (RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            Set<Lane> lanes = vehicle.getDriver().getCurrentlyOccupiedLanes();
            for(Lane lane : lanes) {
                // Find out what IntersectionManager is coming up for this vehicle
                IntersectionManager im =
                        lane.getLaneRIM().nextIntersectionManager(vehicle.getPosition());
                // Only include this Vehicle if it is not in the intersection.
                if((lane.getLaneRIM().distanceToNextIntersection(vehicle.getPosition())>0 &&
                        lane.getLaneRIM().distanceToNextIntersection(vehicle.getPosition())<Double.MAX_VALUE)
                        || im == null || !im.intersects(vehicle.getShape().getBounds2D())) {
                    // Now find how far along the lane it is.
                    double dst = lane.distanceAlongLane(vehicle.getPosition());
                    // Now add it to the map.
                    vehicleLists.get(lane).put(dst, vehicle);
                }
            }
        }

        return vehicleLists;
    }

    /**
     * Finds the preceding vehicle for all of the vehicles. This is the preceding vehicle for the lane the vehicle is
     * currently on.
     *
     * @param vehicleLists
     * @return
     */
    private Map<RIMVehicleSimModel, RIMVehicleSimModel> computeNextVehicle(
            Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists) {
        Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle =
                new HashMap<RIMVehicleSimModel, RIMVehicleSimModel>();
        for (SortedMap<Double, RIMVehicleSimModel> vehicleList : vehicleLists.values()) {
            RIMVehicleSimModel lastVehicle = null;
            for (RIMVehicleSimModel currVehicle : vehicleList.values()) {
                if (lastVehicle != null) {
                    nextVehicle.put(lastVehicle, currVehicle);
                }
                lastVehicle = currVehicle;
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
