/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.sim.simulator.aim;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.aim.AIMAutoDriver;
import aim4.driver.aim.ProxyDriver;
import aim4.driver.aim.pilot.V2IPilot;
import aim4.im.aim.IntersectionManager;
import aim4.im.aim.v2i.V2IManager;
import aim4.map.BasicAIMIntersectionMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;
import aim4.map.aim.AIMSpawnPoint.AIMSpawnSpec;
import aim4.map.lane.Lane;
import aim4.map.merge.RoadNames;
import aim4.msg.aim.i2v.I2VMessage;
import aim4.msg.aim.v2i.V2IMessage;
import aim4.sim.results.MergeResult;
import aim4.sim.results.MergeVehicleResult;
import aim4.sim.results.Result;
import aim4.sim.results.VehicleResult;
import aim4.util.Util;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.VehicleUtil;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.aim.*;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * The autonomous drivers only simulator.
 */
public class AutoDriverOnlySimulator implements AIMSimulator {

    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The result of a simulation step.
     */
    public static class AutoDriverOnlySimStepResult implements SimStepResult {

        /** The VIN of the completed vehicles in this time step */
        List<Integer> completedVINs;

        /**
         * Create a result of a simulation step
         *
         * @param completedVINs  the VINs of completed vehicles.
         */
        public AutoDriverOnlySimStepResult(List<Integer> completedVINs) {
            this.completedVINs = completedVINs;
        }

        /**
         * Get the list of VINs of completed vehicles.
         *
         * @return the list of VINs of completed vehicles.
         */
        public List<Integer> getCompletedVINs() {
            return completedVINs;
        }
    }

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The map */
    private BasicAIMIntersectionMap basicAIMIntersectionMap;
    /** All active vehicles, in form of a map from VINs to vehicle objects. */
    private Map<Integer,AIMVehicleSimModel> vinToVehicles;
    /** The current time */
    private double currentTime;
    /** The number of completed vehicles */
    private int numOfCompletedVehicles;
    /** The number of vehicles which could not be spawned*/
    private int numOfVehicleWhichCouldNotBeSpawned;
    /** The number of vehicles spawned*/
    private int numOfVehicleSpawned;
    /** The total number of bits transmitted by the completed vehicles */
    private int totalBitsTransmittedByCompletedVehicles;
    /** The total number of bits received by the completed vehicles */
    private int totalBitsReceivedByCompletedVehicles;

    //Results aids//
    private List<VehicleResult> vehiclesRecord;

    //Results Merge aids//
    private List<MergeVehicleResult> mergeVehiclesRecord;
    private Map<String, Double> specToExpectedTimeMergeLane;
    private Map<String, Double> specToExpectedTimeTargetLane;

    //Merge aids//
    private boolean mergeMode;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create an instance of the simulator.
     *
     * @param basicAIMIntersectionMap             the map of the simulation
     */
    public AutoDriverOnlySimulator(BasicAIMIntersectionMap basicAIMIntersectionMap) {
        this(basicAIMIntersectionMap, false, null, null);
    }

    public AutoDriverOnlySimulator(BasicAIMIntersectionMap basicAIMIntersectionMap, boolean mergeMode) {
        this(basicAIMIntersectionMap, mergeMode, null, null);
    }

    //Only used with MergeMimicSimSetup
    public AutoDriverOnlySimulator(BasicAIMIntersectionMap basicAIMIntersectionMap,
                                   boolean mergeMode,
                                   Map<String, Double> specToExpectedTimeMergeLane,
                                   Map<String, Double> specToExpectedTimeTargetLane){
        this.mergeMode = mergeMode;
        this.basicAIMIntersectionMap = basicAIMIntersectionMap;
        this.vinToVehicles = new HashMap<Integer,AIMVehicleSimModel>();
        this.vehiclesRecord = new ArrayList<VehicleResult>();
        if(mergeMode) {
            Map<String, Double> fakeDelayTimes = new HashMap<String, Double>();
            for(int specID = 0; specID < VehicleSpecDatabase.getNumOfSpec(); specID++)
                fakeDelayTimes.put(VehicleSpecDatabase.getVehicleSpecById(specID).getName(), new Double(0));
            this.mergeVehiclesRecord = new ArrayList<MergeVehicleResult>();
            if(specToExpectedTimeMergeLane != null)
                this.specToExpectedTimeMergeLane = specToExpectedTimeMergeLane;
            else
                this.specToExpectedTimeMergeLane = fakeDelayTimes;
            if(specToExpectedTimeTargetLane != null)
                this.specToExpectedTimeTargetLane = specToExpectedTimeMergeLane;
            else
                this.specToExpectedTimeTargetLane = fakeDelayTimes;
            this.specToExpectedTimeTargetLane = specToExpectedTimeTargetLane;
        }

        currentTime = 0.0;
        numOfCompletedVehicles = 0;
        numOfVehicleWhichCouldNotBeSpawned = 0;
        numOfVehicleSpawned = 0;
        totalBitsTransmittedByCompletedVehicles = 0;
        totalBitsReceivedByCompletedVehicles = 0;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // the main loop

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized AutoDriverOnlySimStepResult step(double timeStep) {
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("--------------------------------------\n");
            System.err.printf("------SIM:spawnVehicles---------------\n");
        }
        spawnVehicles(timeStep);
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:provideSensorInput---------------\n");
        }
        provideSensorInput();
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:letDriversAct---------------\n");
        }
        letDriversAct();
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:letIntersectionManagersAct--------------\n");
        }
        letIntersectionManagersAct(timeStep);
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:communication---------------\n");
        }
        communication();
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:moveVehicles---------------\n");
        }
        moveVehicles(timeStep);
        if (Debug.CHECK_FOR_COLLISIONS) {
            System.err.printf("------SIM:checkForCollisions---------------\n");
            checkForCollisions();
        }
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:cleanUpCompletedVehicles---------------\n");
        }
        List<AIMVehicleSimModel> completedVehicles = calculateCompletedVehicles();
        provideCompletedVehiclesWithResultsInfo(completedVehicles);
        recordCompletedVehicles(completedVehicles);
        updateMaxMinVelocities();

        if(mergeMode) {
            provideMergeCompletedVehiclesWithResultsInfo(completedVehicles);
            recordMergeCompletedVehicles(completedVehicles);
            updateMaxMinVelocities();
        }

        List<Integer> completedVINs = cleanUpCompletedVehicles();
        currentTime += timeStep;
        // debug
        checkClocks();

        return new AutoDriverOnlySimStepResult(completedVINs);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // information retrieval

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized BasicAIMIntersectionMap getMap() {
        return basicAIMIntersectionMap;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized double getSimulationTime() {
        return currentTime;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized int getNumCompletedVehicles() {
        return numOfCompletedVehicles;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized double getAvgBitsTransmittedByCompletedVehicles() {
        if (numOfCompletedVehicles > 0) {
            return ((double)totalBitsTransmittedByCompletedVehicles)
                    / numOfCompletedVehicles;
        } else {
            return 0.0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized double getAvgBitsReceivedByCompletedVehicles() {
        if (numOfCompletedVehicles > 0) {
            return ((double)totalBitsReceivedByCompletedVehicles)
                    / numOfCompletedVehicles;
        } else {
            return 0.0;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized Set<AIMVehicleSimModel> getActiveVehicles() {
        return new HashSet<AIMVehicleSimModel>(vinToVehicles.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AIMVehicleSimModel getActiveVehicle(int vin) {
        return vinToVehicles.get(vin);
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void addProxyVehicle(ProxyVehicleSimModel vehicle) {
        Point2D pos = vehicle.getPosition();
        Lane minLane = null;
        double minDistance = -1.0;

        for(Road road : basicAIMIntersectionMap.getRoads()) {
            for(Lane lane : road.getLanes()) {
                double d = lane.nearestDistance(pos);
                if (minLane == null || d < minDistance) {
                    minLane = lane;
                    minDistance = d;
                }
            }
        }
        assert minLane != null;

        ProxyDriver driver = vehicle.getDriver();
        if (driver != null) {
            driver.setCurrentLane(minLane);
            driver.setSpawnPoint(null);
            driver.setDestination(null);
        }

        vinToVehicles.put(vehicle.getVIN(), vehicle);
    }


    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /////////////////////////////////
    // STEP 1
    /////////////////////////////////

    /**
     * Spawn vehicles.
     *
     * @param timeStep  the time step
     */
    private void spawnVehicles(double timeStep) {
        for(AIMSpawnPoint spawnPoint : basicAIMIntersectionMap.getSpawnPoints()) {
            List<AIMSpawnPoint.AIMSpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
            if (!spawnSpecs.isEmpty()) {
                if (canSpawnVehicle(spawnPoint)) {
                    for(AIMSpawnPoint.AIMSpawnSpec spawnSpec : spawnSpecs) {

                        // First check if there is enough space to spawn a new vehicle and still have time to stop before reaching it
                        Lane lane = spawnPoint.getLane();
                        Map<Lane,SortedMap<Double,AIMVehicleSimModel>> vehicleLists = computeVehicleLists();

                        // If there are some vehicles on this lane
                        if (!vehicleLists.isEmpty() && !vehicleLists.get(lane).isEmpty()){
                            // Determine whether there is enough distance to stop if spawned with the speed limit
                            double initVelocity = Math.min(spawnSpec.getVehicleSpec().getMaxVelocity(), lane.getSpeedLimit());
                            // The closest vehicle will be the first one on the list
                            double distanceTillNextVehicle = vehicleLists.get(lane).firstKey();
                            double stoppingDistance = VehicleUtil.calcDistanceToStop(initVelocity,
                                    spawnSpec.getVehicleSpec().getMaxDeceleration());
                            double followingDistance = stoppingDistance + V2IPilot.MINIMUM_FOLLOWING_DISTANCE;
                            // Need to subtract the length of the noVehicleZone as the vehicle will be able to slow down
                            // after passing the noVehicleZone area
                            if (distanceTillNextVehicle - Double.max(
                                    spawnPoint.getNoVehicleZone().getBounds2D().getHeight(),spawnPoint.getNoVehicleZone().getBounds2D().getWidth()) >
                                    followingDistance){
                                AIMVehicleSimModel vehicle = makeVehicle(spawnPoint, spawnSpec);
                                VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                                vinToVehicles.put(vehicle.getVIN(), vehicle);
                                numOfVehicleSpawned++;
                            } // otherwise there is not enough space to slow down so don't spawn this vehicle
                            else numOfVehicleWhichCouldNotBeSpawned++;
                        }
                        // Otherwise this is the first time we spawn vehicles
                        else {
                            AIMVehicleSimModel vehicle = makeVehicle(spawnPoint, spawnSpec);
                            VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                            vinToVehicles.put(vehicle.getVIN(), vehicle);
                            numOfVehicleSpawned++;
                        }
                        break; // Only the first vehicle needed. TODO: FIX THIS
                    }
                } // else ignore the spawnSpecs and do nothing
                else {
                    numOfVehicleWhichCouldNotBeSpawned++;
                }
            }
        }
    }


    /**
     * Whether a spawn point can spawn any vehicle
     *
     * @param spawnPoint  the spawn point
     * @return Whether the spawn point can spawn any vehicle
     */
    private boolean canSpawnVehicle(AIMSpawnPoint spawnPoint) {
        // TODO: can be made much faster.
        assert spawnPoint.getNoVehicleZone() instanceof Rectangle2D;
        Rectangle2D noVehicleZone = (Rectangle2D) spawnPoint.getNoVehicleZone();
        for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
            if (vehicle.getShape().intersects(noVehicleZone)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a vehicle at a spawn point.
     *
     * @param spawnPoint  the spawn point
     * @param spawnSpec   the spawn specification
     * @return the vehicle
     */
    private AIMVehicleSimModel makeVehicle(AIMSpawnPoint spawnPoint,
                                           AIMSpawnSpec spawnSpec) {
        VehicleSpec spec = spawnSpec.getVehicleSpec();
        Lane lane = spawnPoint.getLane();
        // Now just take the minimum of the max velocity of the vehicle, and
        // the speed limit in the lane
        double initVelocity = Math.min(spec.getMaxVelocity(), lane.getSpeedLimit());
        // Obtain a Vehicle
        AIMAutoVehicleSimModel vehicle =
                new AIMBasicAutoVehicle(spec,
                        spawnPoint.getPosition(),
                        spawnPoint.getHeading(),
                        spawnPoint.getSteeringAngle(),
                        initVelocity, // velocity
                        initVelocity,  // target velocity
                        spawnPoint.getAcceleration(),
                        spawnSpec.getSpawnTime());
        vehicle.setStartTime(spawnSpec.getSpawnTime());
        vehicle.setMinVelocity(initVelocity);
        vehicle.setMaxVelocity(initVelocity);
        if(spawnPoint.getHeading() == 0)
            vehicle.setStartingRoad(RoadNames.TARGET_ROAD);
        else
            vehicle.setStartingRoad(RoadNames.MERGING_ROAD);
        // Set the driver
        AIMAutoDriver driver = new AIMAutoDriver(vehicle, basicAIMIntersectionMap);
        driver.setCurrentLane(lane);
        driver.setSpawnPoint(spawnPoint);
        driver.setDestination(spawnSpec.getDestinationRoad());
        vehicle.setDriver(driver);

        return vehicle;
    }


    /////////////////////////////////
    // STEP 2
    /////////////////////////////////


    /**
     * Compute the lists of vehicles of all lanes.
     *
     * @return a mapping from lanes to lists of vehicles sorted by their
     *         distance on their lanes
     */
    private Map<Lane,SortedMap<Double,AIMVehicleSimModel>> computeVehicleLists() {
        // Set up the structure that will hold all the Vehicles as they are
        // currently ordered in the Lanes
        Map<Lane,SortedMap<Double,AIMVehicleSimModel>> vehicleLists =
                new HashMap<Lane,SortedMap<Double,AIMVehicleSimModel>>();
        for(Road road : basicAIMIntersectionMap.getRoads()) {
            for(Lane lane : road.getLanes()) {
                vehicleLists.put(lane, new TreeMap<Double,AIMVehicleSimModel>());
            }
        }
        // Now add each of the Vehicles, but make sure to exclude those that are
        // already inside (partially or entirely) the intersection
        for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
            // Find out what lanes it is in.
            Set<Lane> lanes = vehicle.getDriver().getCurrentlyOccupiedLanes();
            for(Lane lane : lanes) {
                // Find out what IntersectionManager is coming up for this vehicle
                IntersectionManager im =
                        lane.getLaneIM().nextIntersectionManager(vehicle.getPosition());
                // Only include this Vehicle if it is not entirely in the intersection.
                if(im == null ||
                        !(im.intersectsPoint(vehicle.getPosition()) && im.intersectsPoint(vehicle.getPointAtRear()))) {
                    // Now find how far along the lane it is.
                    double dst = lane.distanceAlongLane(vehicle.getPosition());
                    // Now add it to the map.
                    vehicleLists.get(lane).put(dst, vehicle);
                    // Now check if this vehicle intersects any other lanes
                    for (Road road : Debug.currentAimMap.getRoads()) {
                        for (Lane otherLane : road.getLanes()) {
                            if (otherLane.getId() != lane.getId()) {
                                if (otherLane.getShape().getBounds2D().intersects(vehicle.getShape().getBounds2D())) {
                                    double dstAlongOtherLane = otherLane.distanceAlongLane(vehicle.getPosition());
                                    vehicleLists.get(otherLane).put(dstAlongOtherLane, vehicle);
                                }
                            }
                        }
                    }
                }
            }
        }
        // Now consolidate the lists based on lanes
        for(Road road : basicAIMIntersectionMap.getRoads()) {
            for(Lane lane : road.getLanes()) {
                // We may have already removed this Lane from the map
                if(vehicleLists.containsKey(lane)) {
                    Lane currLane = lane;
                    // Now run through the lanes
                    while(currLane.hasNextLane()) {
                        currLane = currLane.getNextLane();
                        // Put everything from the next lane into the original lane
                        // and remove the mapping for the next lane
                        vehicleLists.get(lane).putAll(vehicleLists.remove(currLane));
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
    private Map<AIMVehicleSimModel, AIMVehicleSimModel> computeNextVehicle(
            Map<Lane,SortedMap<Double,AIMVehicleSimModel>> vehicleLists) {
        // At this point we should only have mappings for start Lanes, and they
        // should include all the Lanes they run into.  Now we need to turn this
        // into a hash map that maps Vehicles to the next vehicle in the Lane
        // or any Lane the Lane runs into
        Map<AIMVehicleSimModel, AIMVehicleSimModel> nextVehicle =
                new HashMap<AIMVehicleSimModel,AIMVehicleSimModel>();
        // For each of the ordered lists of vehicles
        for(SortedMap<Double,AIMVehicleSimModel> vehicleList : vehicleLists.values()) {
            AIMVehicleSimModel lastVehicle = null;
            // Go through the Vehicles in order of their position in the Lane
            for(AIMVehicleSimModel currVehicle : vehicleList.values()) {
                if(lastVehicle != null) {
                    // Create the mapping from the previous Vehicle to the current one
                    nextVehicle.put(lastVehicle,currVehicle);
                }
                lastVehicle = currVehicle;
            }
        }

        return nextVehicle;
    }

    /**
     * Provide each vehicle with sensor information to allow it to make
     * decisions.  This works first by making an ordered list for each Lane of
     * all the vehicles in that Lane, in order from the start of the Lane to
     * the end of the Lane.  We must make sure to leave out all vehicles that
     * are in the intersection.  We must also concatenate the lists for lanes
     * that feed into one another.  Then, for each vehicle, depending on the
     * state of its sensors, we provide it with the appropriate sensor input.
     */
    private void provideSensorInput() {
        Map<Lane,SortedMap<Double,AIMVehicleSimModel>> vehicleLists =
                computeVehicleLists();
        Map<AIMVehicleSimModel, AIMVehicleSimModel> nextVehicle =
                computeNextVehicle(vehicleLists);

        provideIntervalInfo(nextVehicle);
        provideVehicleTrackingInfo(vehicleLists);
        provideTrafficSignal();
    }

    /**
     * Provide sensing information to the intervalometers of all vehicles.
     *
     * @param nextVehicle  a mapping from vehicles to next vehicles
     */
    private void provideIntervalInfo(
            Map<AIMVehicleSimModel, AIMVehicleSimModel> nextVehicle) {

        // Now that we have this list set up, let's provide input to all the
        // Vehicles.
        for(AIMVehicleSimModel vehicle: vinToVehicles.values()) {
            // If the vehicle is autonomous
            if (vehicle instanceof AIMAutoVehicleSimModel) {
                AIMAutoVehicleSimModel autoVehicle = (AIMAutoVehicleSimModel)vehicle;

                switch(autoVehicle.getLRFMode()) {
                    case DISABLED:
                        // Find the interval to the next vehicle
                        double interval;
                        // If there is a next vehicle, then calculate it
                        if(nextVehicle.containsKey(autoVehicle)) {
                            // It's the distance from the front of this Vehicle to the point
                            // at the rear of the Vehicle in front of it
                            interval = calcInterval(autoVehicle, nextVehicle.get(autoVehicle));
                        } else { // Otherwise, just set it to the maximum possible value
                            interval = Double.MAX_VALUE;
                        }
                        // Now actually record it in the vehicle
                        autoVehicle.getIntervalometer().record(interval);
                        autoVehicle.setLRFSensing(false); // Vehicle is not using
                        // the LRF sensor
                        break;
                    case LIMITED:
                        // FIXME
                        autoVehicle.setLRFSensing(true); // Vehicle is using the LRF sensor
                        break;
                    case ENABLED:
                        // FIXME
                        autoVehicle.setLRFSensing(true); // Vehicle is using the LRF sensor
                        break;
                    default:
                        throw new RuntimeException("Unknown LRF Mode: " +
                                autoVehicle.getLRFMode().toString());
                }
            }
        }
    }

    /**
     * Provide tracking information to vehicles.
     *
     * @param vehicleLists  a mapping from lanes to lists of vehicles sorted by
     *                      their distance on their lanes
     */
    private void provideVehicleTrackingInfo(
            Map<Lane, SortedMap<Double, AIMVehicleSimModel>> vehicleLists) {
        // Vehicle Tracking
        for(AIMVehicleSimModel vehicle: vinToVehicles.values()) {
            // If the vehicle is autonomous
            if (vehicle instanceof AIMAutoVehicleSimModel) {
                AIMAutoVehicleSimModel autoVehicle = (AIMAutoVehicleSimModel)vehicle;

                if (autoVehicle.isVehicleTracking()) {
                    AIMAutoDriver driver = autoVehicle.getDriver();
                    Lane targetLane = autoVehicle.getTargetLaneForVehicleTracking();
                    Point2D pos = autoVehicle.getPosition();
                    double dst = targetLane.distanceAlongLane(pos);

                    // initialize the distances to infinity
                    double frontDst = Double.MAX_VALUE;
                    double rearDst = Double.MAX_VALUE;
                    AIMVehicleSimModel frontVehicle = null ;
                    AIMVehicleSimModel rearVehicle = null ;

                    // only consider the vehicles on the target lane
                    SortedMap<Double,AIMVehicleSimModel> vehiclesOnTargetLane =
                            vehicleLists.get(targetLane);

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

                    // assign the sensor readings

                    autoVehicle.getFrontVehicleDistanceSensor().record(frontDst);
                    autoVehicle.getRearVehicleDistanceSensor().record(rearDst);

                    // assign the vehicles' velocities

                    if(frontVehicle!=null) {
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

                    // show the section on the viewer
                    if (Debug.isTargetVIN(driver.getVehicle().getVIN())) {
                        Point2D p1 = targetLane.getPointAtNormalizedDistance(
                                Math.max((dst-rearDst)/targetLane.getLength(),0.0));
                        Point2D p2 = targetLane.getPointAtNormalizedDistance(
                                Math.min((frontDst+dst)/targetLane.getLength(),1.0));
                        Debug.addLongTermDebugPoint(
                                new DebugPoint(p2, p1, "cl", Color.RED.brighter()));
                    }
                }
            }
        }

    }

    /**
     * Provide traffic signals.
     */
    private void provideTrafficSignal() {
        for(AIMVehicleSimModel vehicle: vinToVehicles.values()) {
            if (vehicle instanceof HumanDrivenVehicleSimModel) {
                HumanDrivenVehicleSimModel manualVehicle =
                        (HumanDrivenVehicleSimModel)vehicle;
                provideTrafficLightSignal(manualVehicle);
            }
        }
    }

    /**
     * Calculate the distance between vehicle and the next vehicle on a lane.
     *
     * @param vehicle      the vehicle
     * @param nextVehicle  the next vehicle
     * @return the distance between vehicle and the next vehicle on a lane
     */
    private double calcInterval(AIMVehicleSimModel vehicle,
                                AIMVehicleSimModel nextVehicle) {
        // From Chiu: Kurt, if you think this function is not okay, probably
        // we should talk to see what to do.
        Point2D pos = vehicle.getPosition();
        if(nextVehicle.getShape().contains(pos)) {
            return 0.0;
        } else {
            // TODO: make it more efficient
            double interval = Double.MAX_VALUE ;
            for(Line2D edge : nextVehicle.getEdges()) {
                double dst = edge.ptSegDist(pos);
                if(dst < interval){
                    interval = dst;
                }
            }
            return interval;
        }
    }
    // Kurt's code:
    // interval = vehicle.getPosition().
    //   distance(nextVehicle.get(vehicle).getPointAtRear());

    /**
     * Provide traffic light signals to a vehicle.
     *
     * @param vehicle  the vehicle
     */
    private void provideTrafficLightSignal(HumanDrivenVehicleSimModel vehicle) {
        // TODO: implement it later
//    DriverSimModel driver = vehicle.getDriver();
//    Lane currentLane = driver.getCurrentLane();
//    Point2D pos = vehicle.getPosition();
//    IntersectionManager im = currentLane.getLaneIM().
//                             nextIntersectionManager(pos);
//    if (im != null) {
//      if (im instanceof LightOnlyManager) {
//        LightOnlyManager lightOnlyIM = (LightOnlyManager)im;
//        if (!im.getIntersection().getArea().contains(pos)) {
//          LightState s = lightOnlyIM.getLightState(currentLane);
//          vehicle.setLightState(s);
//          if (driver instanceof HumanDriver) {
//            ((HumanDriver)driver).setLightState(s);
//          }
//        } else {
//          vehicle.setLightState(null);
//          if (driver instanceof HumanDriver) {
//            ((HumanDriver)driver).setLightState(null);
//          }
//        }
//      }
//    } else {
//      vehicle.setLightState(null);
//      if (driver instanceof HumanDriver) {
//        ((HumanDriver)driver).setLightState(null);
//      }
//    }
    }

    /////////////////////////////////
    // STEP 3
    /////////////////////////////////

    /**
     * Allow each driver to act.
     */
    private void letDriversAct() {
        for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
            vehicle.getDriver().act();
        }
    }

    /////////////////////////////////
    // STEP 4
    /////////////////////////////////

    /**
     * Allow each intersection manager to act.
     *
     * @param timeStep  the time step
     */
    private void letIntersectionManagersAct(double timeStep) {
        for(IntersectionManager im : basicAIMIntersectionMap.getIntersectionManagers()) {
            im.act(timeStep);
        }
    }

    /////////////////////////////////
    // STEP 5
    /////////////////////////////////

    /**
     * Deliver the V2I and I2V messages.
     */
    private void communication() {
        deliverV2IMessages();
        deliverI2VMessages();
//    deliverV2VMessages();
    }

    /**
     * Deliver the V2I messages.
     */
    private void deliverV2IMessages() {
        // Go through each vehicle and deliver each of its messages
        for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
            // Start with V2I messages
            if (vehicle instanceof AIMAutoVehicleSimModel) {
                AIMAutoVehicleSimModel sender = (AIMAutoVehicleSimModel)vehicle;
                Queue<V2IMessage> v2iOutbox = sender.getV2IOutbox();
                while(!v2iOutbox.isEmpty()) {
                    V2IMessage msg = v2iOutbox.poll();
                    V2IManager receiver =
                            (V2IManager) basicAIMIntersectionMap.getImRegistry().get(msg.getImId());
                    // Calculate the distance the message must travel
                    double txDistance =
                            sender.getPosition().distance(
                                    receiver.getIntersection().getCentroid());
                    // Find out if the message will make it that far
                    if(transmit(txDistance, sender.getTransmissionPower())) {
                        // Actually deliver the message
                        receiver.receive(msg);
                        // Add the delivery to the debugging information
                    }
                    // Either way, we increment the number of transmitted messages
                }
            }
        }
    }

    /**
     * Deliver the I2V messages.
     */
    private void deliverI2VMessages() {
        // Now deliver all the I2V messages
        for(IntersectionManager im : basicAIMIntersectionMap.getIntersectionManagers()) {
            V2IManager senderIM = (V2IManager)im;
            for(Iterator<I2VMessage> i2vIter = senderIM.outboxIterator();
                i2vIter.hasNext();) {
                I2VMessage msg = i2vIter.next();
                AIMAutoVehicleSimModel vehicle =
                        (AIMAutoVehicleSimModel)VinRegistry.getVehicleFromVIN(
                                msg.getVin());
                // Calculate the distance the message must travel
                double txDistance =
                        senderIM.getIntersection().getCentroid().distance(
                                vehicle.getPosition());
                // Find out if the message will make it that far
                if(transmit(txDistance, senderIM.getTransmissionPower())) {
                    // Actually deliver the message
                    vehicle.receive(msg);
                }
            }
            // Done delivering the IntersectionManager's messages, so clear the
            // outbox.
            senderIM.clearOutbox();
        }
    }

//  private void deliverV2VMessages() {
//
//    // Create a place to store broadcast messages until they can be sent so
//    // that we don't have to run through the list of Vehicles for each one
//    List<V2VMessage> broadcastMessages = new ArrayList<V2VMessage>();
//
//    // Go through each vehicle and deliver each of its messages
//    for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
//      // Then do V2V messages.
//      if (vehicle instanceof AIMAutoVehicleSimModel) {
//        AIMAutoVehicleSimModel sender = (AIMAutoVehicleSimModel)vehicle;
//        for(V2VMessage msg : sender.getV2VOutbox()) {
//          if(msg.isBroadcast()) {
//            // If it's a broadcast message, we save it and worry about it later
//            broadcastMessages.add(msg);
//          } else {
//            // Otherwise, we just deliver it! Woo!
//            // TODO: need to check whether the vehicle is AutoVehicleSpec
//            AIMAutoVehicleSimModel receiver =
//              (AIMAutoVehicleSimModel)VinRegistry.getVehicleFromVIN(
//                msg.getDestinationID());
//            // Calculate the distance the message must travel
//            double txDistance =
//              sender.getPosition().distance(receiver.getPosition());
//            // Find out if the message will make it that far
//            if(transmit(txDistance, sender.getTransmissionPower())) {
//              // Actually deliver the message
//              receiver.receive(msg);
//              // Add the delivery to the debugging information
//            }
//          }
//        }
//        // Done delivering the V2V messages (except broadcast which we will
//        // handle in a moment), so clear the outbox
//        sender.getV2VOutbox().clear();
//      }
//    }
//    // Now go through the vehicles and deliver the broadcast messages
//    for(V2VMessage msg : broadcastMessages) {
//      // Send a copy to the IM for debugging/statistics purposes
//      IntersectionManager im =
//        basicAIMIntersectionMap.getImRegistry().get(msg.getIntersectionManagerID());
////      if(im != null) {
////        switch(im.getIntersectionType()) {
////        case V2V:
////          ((V2VManager) im).logBroadcast(msg);
////        }
////      }
//      // Determine who sent this message
//      // TODO: need to check whether the vehicle is AutoVehicleSpec
//      AIMAutoVehicleSimModel sender =
//        (AIMAutoVehicleSimModel)VinRegistry.getVehicleFromVIN(
//          msg.getSourceID());
//      // Deliver to each vehicle
//      for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
//        if (vehicle instanceof AIMAutoVehicleSimModel) {
//          AIMAutoVehicleSimModel receiver = (AIMAutoVehicleSimModel)vehicle;
//          // Except the one that sent it
//          if(sender != receiver) {
//            // Find out how far away they are
//            double txDistance =
//              sender.getPosition().distance(receiver.getPosition());
//            // See if this Vehicle is close enough to receive the message
//            if(transmit(txDistance, sender.getTransmissionPower())) {
//              // Actually deliver the message
//              receiver.receive(msg);
//            }
//          }
//        } // else ignore other non-autonomous vehicle
//      }
//    }
//  }


    /**
     * Whether the transmission of a message is successful
     *
     * @param distance  the distance of the transmission
     * @param power     the power of the transmission
     * @return whether the transmission of a messsage is successful
     */
    private boolean transmit(double distance, double power) {
        // Simple for now
        return distance <= power;
    }


    /////////////////////////////////
    // STEP 6
    /////////////////////////////////

    /**
     * Move all the vehicles.
     *
     * @param timeStep  the time step
     */
    private void moveVehicles(double timeStep) {
        for(AIMVehicleSimModel vehicle : vinToVehicles.values()) {
            Point2D p1 = vehicle.getPosition();
            vehicle.move(timeStep);
            Point2D p2 = vehicle.getPosition();
            for(DataCollectionLine line : basicAIMIntersectionMap.getDataCollectionLines()) {
                line.intersect(vehicle, currentTime, p1, p2);
            }
            if (Debug.isPrintVehicleStateOfVIN(vehicle.getVIN())) {
                vehicle.printState();
            }
        }
    }

    /**
     * Detects collisions. Currently not used because vehicles collide - Go figure.
     */
    private void checkForCollisions() {
        Integer[] keys = vinToVehicles.keySet().toArray(new Integer[]{});
        for(int i = 0; i < keys.length - 1; i++) { //-1 because we won't compare the last element with anything.
            Integer[] keysToCompare = Arrays.copyOfRange(keys, i + 1, keys.length);
            AIMVehicleSimModel vehicle1 = vinToVehicles.get(keys[i]);
            for(int j = 0; j < keysToCompare.length; j++) {
                AIMVehicleSimModel vehicle2 = vinToVehicles.get(keysToCompare[j]);
                if(VehicleUtil.collision(vehicle1, vehicle2)) {
                    throw new RuntimeException(String.format("There was a collision between vehicles %d and %d",
                            vehicle1.getVIN(),
                            vehicle2.getVIN()));
                }
            }
        }
    }


    /////////////////////////////////
    // STEP 7
    /////////////////////////////////

    /**
     * Remove all completed vehicles.
     *
     * @return the VINs of the completed vehicles
     */
    private List<Integer> cleanUpCompletedVehicles() {
        List<Integer> completedVINs = new LinkedList<Integer>();

        Rectangle2D mapBoundary = basicAIMIntersectionMap.getDimensions();

        List<Integer> removedVINs = new ArrayList<Integer>(vinToVehicles.size());
        for(int vin : vinToVehicles.keySet()) {
            AIMVehicleSimModel v = vinToVehicles.get(vin);
            // If the vehicle is no longer in the layout
            // TODO: this should be replaced with destination zone.
            if(!v.getShape().intersects(mapBoundary)) {
                // Process all the things we need to from this vehicle
                if (v instanceof AIMAutoVehicleSimModel) {
                    AIMAutoVehicleSimModel v2 = (AIMAutoVehicleSimModel)v;
                    totalBitsTransmittedByCompletedVehicles += v2.getBitsTransmitted();
                    totalBitsReceivedByCompletedVehicles += v2.getBitsReceived();
                }
                removedVINs.add(vin);
            }
        }
        // Remove the marked vehicles
        for(int vin : removedVINs) {
            vinToVehicles.remove(vin);
            completedVINs.add(vin);
            numOfCompletedVehicles++;
        }

        return completedVINs;
    }

    // RESULTS //
    private List<AIMVehicleSimModel> calculateCompletedVehicles() {
        List<AIMVehicleSimModel> completedVehicles = new LinkedList<AIMVehicleSimModel>();

        Rectangle2D mapBoundary = basicAIMIntersectionMap.getDimensions();
        for(int vin : vinToVehicles.keySet()) {
            if(!vinToVehicles.get(vin).getShape().intersects(mapBoundary))
                completedVehicles.add(vinToVehicles.get(vin));
        }

        return completedVehicles;
    }

    private void recordCompletedVehicles(List<AIMVehicleSimModel> completedVehicles) {
        for(AIMVehicleSimModel vehicle : completedVehicles) {
            vehiclesRecord.add(new VehicleResult(
                    vehicle.getVIN(),
                    vehicle.getSpec().getName(),
                    vehicle.getStartTime(),
                    vehicle.getFinishTime(),
                    vehicle.getFinalVelocity(),
                    vehicle.getMaxVelocity(),
                    vehicle.getMinVelocity()
            ));
        }
    }

    private void recordMergeCompletedVehicles(List<AIMVehicleSimModel> completedVehicles) {
        for(AIMVehicleSimModel vehicle : completedVehicles) {
            mergeVehiclesRecord.add(new MergeVehicleResult(
                    vehicle.getVIN(),
                    vehicle.getStartingRoad().toString(),
                    vehicle.getSpec().getName(),
                    vehicle.getStartTime(),
                    vehicle.getFinishTime(),
                    vehicle.getDelay(),
                    vehicle.getFinalVelocity(),
                    vehicle.getMaxVelocity(),
                    vehicle.getMinVelocity(),
                    vehicle.getFinalXPos(),
                    vehicle.getFinalYPos()
            ));
        }
    }

    private void provideCompletedVehiclesWithResultsInfo(List<AIMVehicleSimModel> completedVehicles) {
        for(AIMVehicleSimModel vehicle : completedVehicles) {
            vehicle.setFinishTime(currentTime);
            vehicle.setFinalVelocity(vehicle.getVelocity());
        }
    }

    private void provideMergeCompletedVehiclesWithResultsInfo(List<AIMVehicleSimModel> completedVehicles) {
        provideCompletedVehiclesWithResultsInfo(completedVehicles);
        for(AIMVehicleSimModel vehicle : completedVehicles) {
            vehicle.setDelay(calculateDelay(vehicle));
            vehicle.setFinalXPos(vehicle.getPosition().getX());
            vehicle.setFinalYPos(vehicle.getPosition().getY());
        }
    }

    private void updateMaxMinVelocities() {
        for(int vin : vinToVehicles.keySet()) {
            AIMVehicleSimModel vehicle = vinToVehicles.get(vin);
            if(vehicle.getVelocity() > vehicle.getMaxVelocity())
                vehicle.setMaxVelocity(vehicle.getVelocity());
            else if(vehicle.getVelocity() < vehicle.getMinVelocity()) {
                if (Util.isDoubleZero(vehicle.getVelocity())){
                    vehicle.setMinVelocity(0.0);
                }
                else vehicle.setMinVelocity(vehicle.getVelocity());
            }
        }
    }

    private double calculateDelay(AIMVehicleSimModel vehicle) {
        if(vehicle.getStartingRoad() == RoadNames.TARGET_ROAD) {
            if (specToExpectedTimeTargetLane != null) {
                double delay = vehicle.getFinishTime() -
                        vehicle.getStartTime() -
                        specToExpectedTimeTargetLane.get(vehicle.getSpec().getName()).doubleValue();
                if (delay < 0)
                    delay = 0;
                return delay;
            }
        }
        else if(vehicle.getStartingRoad() == RoadNames.MERGING_ROAD) {
            if (specToExpectedTimeMergeLane != null) {
                double delay = vehicle.getFinishTime() -
                        vehicle.getStartTime() -
                        specToExpectedTimeMergeLane.get(vehicle.getSpec().getName()).doubleValue();
                if (delay < 0)
                    delay = 0;
                return delay;
            }
        }
        return Double.MAX_VALUE;
    }

    public String produceResultsCSV(){
        return produceResult().produceCSVString();
    }

    public MergeResult produceMergeResult() {
        return new MergeResult(mergeVehiclesRecord);
    }

    public Result produceResult() {
        return new Result(vehiclesRecord);
    }

    public int getNumOfVehiclesWhichCouldNotBeSpawned(){ return numOfVehicleWhichCouldNotBeSpawned; }

    public int getNumOfVehiclesSpawned(){ return numOfVehicleSpawned; }


    protected String resultsToCSV(MergeResult result) {
        StringBuilder sb = new StringBuilder();
        //Global Stats
        sb.append("Maximum Delay");
        sb.append(',');
        sb.append("Average Delay");
        sb.append(',');
        sb.append("Minimum Delay");
        sb.append(',');
        sb.append("No. Completed Vehicles");
        sb.append(',');
        sb.append("Throughput");
        sb.append('\n');
        sb.append(result.getMaxDelay());
        sb.append(',');
        sb.append(result.getAverageDelay());
        sb.append(',');
        sb.append(result.getMinDelay());
        sb.append(',');
        sb.append(result.getCompletedVehicles());
        sb.append(',');
        sb.append(result.getThroughput());
        sb.append('\n');
        sb.append('\n');
        //Headings
        sb.append("VIN");
        sb.append(',');
        sb.append("Starting Road");
        sb.append(',');
        sb.append("Vehicle Spec");
        sb.append(',');
        sb.append("Start Time");
        sb.append(',');
        sb.append("Finish Time");
        sb.append(',');
        sb.append("Delay");
        sb.append(',');
        sb.append("Final Velocity");
        sb.append(',');
        sb.append("Max Velocity");
        sb.append(',');
        sb.append("Min Velocity");
        sb.append(',');
        sb.append("Final X Position");
        sb.append(',');
        sb.append("Final Y Position");
        sb.append('\n');
        //Vehicle Data
        for(MergeVehicleResult vr : result.getVehicleResults()){
            sb.append(vr.getVin());
            sb.append(',');
            sb.append(vr.getStartingRoad());
            sb.append(',');
            sb.append(vr.getSpecType());
            sb.append(',');
            sb.append(vr.getStartTime());
            sb.append(',');
            sb.append(vr.getFinishTime());
            sb.append(',');
            sb.append(vr.getDelayTime());
            sb.append(',');
            sb.append(vr.getFinalVelocity());
            sb.append(',');
            sb.append(vr.getMaxVelocity());
            sb.append(',');
            sb.append(vr.getMinVelocity());
            sb.append(',');
            sb.append(vr.getFinalXPos());
            sb.append(',');
            sb.append(vr.getFinalYPos());
            sb.append('\n');
        }
        return sb.toString();
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * Check whether the clocks are in sync.
     */
    private void checkClocks() {
        // Check the clocks for all autonomous vehicles.
        for(AIMVehicleSimModel vehicle: vinToVehicles.values()) {
            vehicle.checkCurrentTime(currentTime);
        }
        // Check the clocks for all the intersection managers.
        for(IntersectionManager im : basicAIMIntersectionMap.getIntersectionManagers()) {
            im.checkCurrentTime(currentTime);
        }
    }


}
