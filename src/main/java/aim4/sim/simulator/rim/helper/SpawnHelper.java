package aim4.sim.simulator.rim.helper;

import aim4.config.Debug;
import aim4.driver.rim.RIMAutoDriver;
import aim4.driver.rim.pilot.V2IPilot;
import aim4.im.rim.IntersectionManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.rim.RIMSpawnPoint;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleUtil;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.rim.RIMAutoVehicleSimModel;
import aim4.vehicle.rim.RIMBasicAutoVehicle;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.awt.geom.Rectangle2D;
import java.util.*;

public class SpawnHelper {
    private BasicRIMIntersectionMap map;
    private Map<Integer, RIMVehicleSimModel> vinToVehicles;
    private int numOfVehicleWhichCouldNotBeSpawned;

    public SpawnHelper(BasicRIMIntersectionMap map, Map<Integer, RIMVehicleSimModel> vinToVehicles, int numOfVehicleWhichCouldNotBeSpawned) {
        this.map = map;
        this.vinToVehicles = vinToVehicles;
        this.numOfVehicleWhichCouldNotBeSpawned = numOfVehicleWhichCouldNotBeSpawned;
    }

    /**
     * Spawns vehicles
     *
     * @param timeStep The time step
     * @return A List of the Vehicles spawned. Null if no vehicles spawned.
     */
    public List<RIMVehicleSimModel> generateSpawnedVehicles(double timeStep) {
        List<RIMVehicleSimModel> spawnedVehicles = new ArrayList<RIMVehicleSimModel>();
        for (RIMSpawnPoint spawnPoint : map.getSpawnPoints()) {
            List<RIMSpawnPoint.RIMSpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
            if (!spawnSpecs.isEmpty()) {
                if (canSpawnVehicle(spawnPoint)) {
                    for (RIMSpawnPoint.RIMSpawnSpec spawnSpec : spawnSpecs) {
                        // First check if there is enough space to spawn a new vehicle and still have time to stop before reaching it
                        Lane lane = spawnPoint.getLane();
                        Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists = computeVehicleLists();

                        // If there are some vehicles on this lane
                        if (!vehicleLists.isEmpty() && !vehicleLists.get(lane).isEmpty()) {
                            // Determine whether there is enough distance to stop if spawned with the speed limit
                            double initVelocity = Math.min(spawnSpec.getVehicleSpec().getMaxVelocity(), lane.getSpeedLimit());
                            // The closest vehicle will be the first one on the list
                            double distanceTillNextVehicle = vehicleLists.get(lane).firstKey();
                            double stoppingDistance = VehicleUtil.calcDistanceToStop(initVelocity,
                                    spawnSpec.getVehicleSpec().getMaxDeceleration());
                            double followingDistance = stoppingDistance + V2IPilot.MINIMUM_FOLLOWING_DISTANCE;
                            // Need to subtract the length of the noVehicleZone as the vehicle will be able to slow down
                            // after passing the noVehicleZone area
                            if (distanceTillNextVehicle - Double.max(((Rectangle2D) spawnPoint.getNoVehicleZone()).getHeight(),
                                    ((Rectangle2D) spawnPoint.getNoVehicleZone()).getWidth()) > followingDistance) {
                                RIMVehicleSimModel vehicle = setupVehicle(spawnPoint, spawnSpec);
                                VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                                vinToVehicles.put(vehicle.getVIN(), vehicle);
                                spawnedVehicles.add(vehicle);
                            } // otherwise there is not enough space to slow down so don't spawn this vehicle
                        }
                        // Otherwise this is the first time we spawn vehicles
                        else {
                            RIMVehicleSimModel vehicle = setupVehicle(spawnPoint, spawnSpec);
                            VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                            vinToVehicles.put(vehicle.getVIN(), vehicle);
                            spawnedVehicles.add(vehicle);
                        }
                        break; // Only the first vehicle needed. TODO: FIX THIS
                    }
                }
            }
        }
        return spawnedVehicles;
    }

    /**
     * Spawns vehicles for step simulator
     *
     * @param timeStep The time step
     */
    public void spawnVehicles(double timeStep) {
        for (RIMSpawnPoint spawnPoint : map.getSpawnPoints()) {
            List<RIMSpawnPoint.RIMSpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
            if (!spawnSpecs.isEmpty()) {
                if (canSpawnVehicle(spawnPoint)) {
                    for (RIMSpawnPoint.RIMSpawnSpec spawnSpec : spawnSpecs) {
                        // First check if there is enough space to spawn a new vehicle and still have time to stop before reaching it
                        Lane lane = spawnPoint.getLane();
                        Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists = computeVehicleLists();

                        // If there are some vehicles on this lane
                        if (!vehicleLists.isEmpty() && !vehicleLists.get(lane).isEmpty()) {
                            // Determine whether there is enough distance to stop if spawned with the speed limit
                            double initVelocity = Math.min(spawnSpec.getVehicleSpec().getMaxVelocity(), lane.getSpeedLimit());
                            // The closest vehicle will be the first one on the list
                            double distanceTillNextVehicle = vehicleLists.get(lane).firstKey();
                            double stoppingDistance = VehicleUtil.calcDistanceToStop(initVelocity,
                                    spawnSpec.getVehicleSpec().getMaxDeceleration());
                            double followingDistance = stoppingDistance + V2IPilot.MINIMUM_FOLLOWING_DISTANCE;
                            // Need to subtract the length of the noVehicleZone as the vehicle will be able to slow down
                            // after passing the noVehicleZone area
                            if (distanceTillNextVehicle - Double.max(((Rectangle2D) spawnPoint.getNoVehicleZone()).getHeight(),
                                    ((Rectangle2D) spawnPoint.getNoVehicleZone()).getWidth()) > followingDistance) {
                                RIMVehicleSimModel vehicle = setupVehicle(spawnPoint, spawnSpec);
                                VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                                vinToVehicles.put(vehicle.getVIN(), vehicle);
                            } else {
                                numOfVehicleWhichCouldNotBeSpawned++;
                            }
                        }
                        // Otherwise this is the first time we spawn vehicles
                        else {
                            RIMVehicleSimModel vehicle = setupVehicle(spawnPoint, spawnSpec);
                            VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                            vinToVehicles.put(vehicle.getVIN(), vehicle);
                        }
                        break; // Only the first vehicle needed. TODO: FIX THIS
                    }
                } else {
                    numOfVehicleWhichCouldNotBeSpawned++;
                }
            }
        }
    }

    /**
     * Checks if the spawn point can spawn a vehicle, based on the size of it's no spawn zone.
     * @param spawnPoint
     * @return
     */
    public boolean canSpawnVehicle(RIMSpawnPoint spawnPoint) {
        assert spawnPoint.getNoVehicleZone() instanceof Rectangle2D;
        Rectangle2D noVehicleZone = (Rectangle2D) spawnPoint.getNoVehicleZone();
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            if (noVehicleZone.intersects(vehicle.getShape().getBounds2D())) {
                return false;
            }
        }
        return true;
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
                            if (otherLane.getId() != lane.getId() &&
                                    otherLane.getShape().getBounds2D().intersects(vehicle.getShape().getBounds2D())) {
                                if (otherLane instanceof ArcSegmentLane) {
                                    for (LineSegmentLane otherLineLane : ((ArcSegmentLane) otherLane).getArcLaneDecomposition()){
                                        if (otherLineLane.getId() != lane.getId()){
                                            if (otherLineLane.getShape().getBounds2D().intersects(vehicle.getShape().getBounds2D())) {
                                                double dstAlongOtherLane = otherLineLane.distanceAlongLane(vehicle.getPosition());
                                                vehicleLists.get(otherLineLane).put(dstAlongOtherLane, vehicle);
                                            }
                                        }
                                    }
                                }
                                else if (otherLane instanceof LineSegmentLane) {
                                    double dstAlongOtherLane = otherLane.distanceAlongLane(vehicle.getPosition());
                                    vehicleLists.get(otherLane).put(dstAlongOtherLane, vehicle);
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
     * Creates a vehicle at the spawn point.
     * @param spawnPoint
     * @param spawnSpec
     * @return
     */
    private RIMVehicleSimModel setupVehicle(
            RIMSpawnPoint spawnPoint,
            RIMSpawnPoint.RIMSpawnSpec spawnSpec) {
        VehicleSpec spec = spawnSpec.getVehicleSpec();
        Lane lane = spawnPoint.getLane();
        double initVelocity = Math.min(spec.getMaxVelocity(), lane.getSpeedLimit());
        return makeAutoVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec);
    }

    private RIMVehicleSimModel  makeAutoVehicle(
            RIMSpawnPoint spawnPoint,
            double initVelocity, Lane lane,
            VehicleSpec spec,
            RIMSpawnPoint.RIMSpawnSpec spawnSpec) {
        RIMAutoVehicleSimModel vehicle =
                new RIMBasicAutoVehicle(spec,
                        spawnPoint.getPosition(),
                        spawnPoint.getHeading(),
                        spawnPoint.getSteeringAngle(),
                        initVelocity,
                        initVelocity,
                        spawnPoint.getAcceleration(),
                        spawnSpec.getSpawnTime());
        vehicle.setStartTime(spawnSpec.getSpawnTime());
        vehicle.setMaxVelocity(initVelocity);
        vehicle.setMinVelocity(initVelocity);

        RIMAutoDriver driver = new RIMAutoDriver(vehicle, map);
        driver.setDestination(spawnSpec.getDestinationRoad());
        driver.setCurrentLane(lane);
        driver.setSpawnPoint(spawnPoint);
        vehicle.setDriver(driver);

        return vehicle;
    }

    public BasicRIMIntersectionMap getMap() {
        return map;
    }

    public Map<Integer, RIMVehicleSimModel> getVinToVehicles() {
        return vinToVehicles;
    }

    public int getNumOfVehicleWhichCouldNotBeSpawned() {
        return numOfVehicleWhichCouldNotBeSpawned;
    }
}
