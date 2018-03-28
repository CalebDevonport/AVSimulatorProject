package aim4.sim.simulator.cpm;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.cpm.CPMV2VDriver;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.cpm.*;
import aim4.map.cpm.CPMSpawnPoint.CPMSpawnSpec;
import aim4.map.cpm.parking.ParkingLane;
import aim4.map.cpm.parking.SensoredLine;
import aim4.map.cpm.parking.StatusMonitor;
import aim4.map.lane.Lane;
import aim4.sim.Simulator;
import aim4.sim.results.SimulatorResult;
import aim4.vehicle.VehicleSimModel;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.cpm.CPMBasicAutoVehicle;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;

/**
 * The simulator of AVs in an AV specific car park which are self-organising.
 */
public class CPMAutoDriverSimulator implements Simulator {
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The result of a simulation step.
     */
    public static class CPMAutoDriverSimStepResult implements SimStepResult {

        /** The VIN of the completed vehicles in this time step */
        List<CPMBasicAutoVehicle> completedVehicles;

        /**
         * Create a result of a simulation step
         *
         * @param completedVehicles  the completed vehicles.
         */
        public CPMAutoDriverSimStepResult(List<CPMBasicAutoVehicle> completedVehicles) {
            this.completedVehicles = completedVehicles;
        }

        /**
         * Get the list of completed vehicles.
         *
         * @return the list of completed vehicles.
         */
        public List<CPMBasicAutoVehicle> getCompletedVehicles() {
            return completedVehicles;
        }
    }

    // TODO CPM find this value, must be defined somewhere
    public static final double MIN_DISTANCE_BETWEEN_PARKED_VEHICLES = 0.2;

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The map */
    protected CPMBasicMap map;
    /** All active vehicles, in form of a map from VINs to vehicle objects. */
    protected Map<Integer,CPMBasicAutoVehicle> vinToVehicles;
    /** The current time */
    protected double currentTime;
    /** The number of completed vehicles */
    protected int numOfCompletedVehicles;
    /** A list of parked vehicles */
    protected List<CPMBasicAutoVehicle> parkedVehicles;
    /** The total number of bits transmitted by the completed vehicles */
    private int totalBitsTransmittedByCompletedVehicles;
    /** The total number of bits received by the completed vehicles */
    private int totalBitsReceivedByCompletedVehicles;

    public CPMAutoDriverSimulator(CPMBasicMap map){
        this.map = map;
        this.vinToVehicles = new HashMap<Integer,CPMBasicAutoVehicle>();
        this.parkedVehicles = new ArrayList<CPMBasicAutoVehicle>();

        currentTime = 0.0;
        numOfCompletedVehicles = 0;
        totalBitsTransmittedByCompletedVehicles = 0;
        totalBitsReceivedByCompletedVehicles = 0;

        System.out.println("CPM Simulator created!");
    }

    @Override
    public SimStepResult step(double timeStep) {
        spawnVehicles(timeStep);
        provideSensorInput();
        findNextVehicles();
        letDriversAct();
        moveVehicles(timeStep);
        observeParkedVehicles();
        observeNumberOfVehiclesInCarPark();
        List<CPMBasicAutoVehicle> completedVehicles = cleanUpCompletedVehicles();
        currentTime += timeStep;
        return new CPMAutoDriverSimStepResult(completedVehicles);
    }

    /////////////////////////////////
    // STEP 1
    /////////////////////////////////

    /**
     * Spawn vehicles.
     *
     * @param timeStep  the time step
     */
    protected void spawnVehicles(double timeStep) {
        for(CPMSpawnPoint spawnPoint : map.getSpawnPoints()) {
            if (canSpawnVehicle(spawnPoint)) {
                List<CPMSpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
                for(CPMSpawnSpec spawnSpec : spawnSpecs) {
                    // Check that the car park caters for vehicles this wide
                    double vehicleWidth = spawnSpec.getVehicleSpec().getWidth();
                    double parkingLaneWidth = map.getParkingArea().getParkingLaneWidth();
                    if (parkingLaneWidth < (vehicleWidth+MIN_DISTANCE_BETWEEN_PARKED_VEHICLES)) {
                        System.out.println("Spawned vehicle discarded: car park doesn't cater for vehicles this wide.");
                    } else {
                        // Only create the vehicle if there is room in the car park
                        double vehicleLength = spawnSpec.getVehicleSpec().getLength();
                        if (map.getStatusMonitor().roomForVehicle(vehicleLength)) {
                            CPMBasicAutoVehicle vehicle = makeVehicle(spawnPoint, spawnSpec);
                            VinRegistry.registerVehicle(vehicle); // Get vehicle a VIN number
                            vinToVehicles.put(vehicle.getVIN(), vehicle);
                            map.addVehicleToMap(vehicle);
                            break; // only handle the first spawn vehicle
                        } else {
                            System.out.println("Spawned vehicle discarded: not enough room.");
                        }
                    }
                }
            } // else ignore the spawnSpecs and do nothingSystem.out.println("No vehicle spawned: canSpawn = False.");
        }
    }

    /**
     * Whether a spawn point can spawn any vehicle
     *
     * @param spawnPoint  the spawn point
     * @return Whether the spawn point can spawn a vehicle
     */
    protected boolean canSpawnVehicle(CPMSpawnPoint spawnPoint) {
        // TODO: can be made much faster.
        assert spawnPoint.getNoVehicleZone() instanceof Rectangle2D;
        Rectangle2D noVehicleZone = (Rectangle2D) spawnPoint.getNoVehicleZone();
        for(CPMBasicAutoVehicle vehicle : vinToVehicles.values()) {
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
    protected CPMBasicAutoVehicle makeVehicle(CPMSpawnPoint spawnPoint,
                                           CPMSpawnSpec spawnSpec) {
        VehicleSpec spec = spawnSpec.getVehicleSpec();
        Lane lane = spawnPoint.getLane();
        // Now just take the minimum of the max velocity of the vehicle, and
        // the speed limit in the lane
        double initVelocity = Math.min(spec.getMaxVelocity(), lane.getSpeedLimit());
        // Generate a length of time that this car should park for
        // This is from entering to when the EXITING state is set.


        // Obtain a Vehicle
        CPMBasicAutoVehicle vehicle =
                new CPMBasicAutoVehicle(spec,
                        spawnPoint.getPosition(),
                        spawnPoint.getHeading(),
                        spawnPoint.getSteeringAngle(),
                        initVelocity, // velocity
                        initVelocity,  // target velocity
                        spawnPoint.getAcceleration(),
                        spawnSpec.getSpawnTime(),
                        spawnSpec.getParkingTime());
        // Set the driver
        CPMV2VDriver driver = new CPMV2VDriver(vehicle, map);
        driver.setCurrentLane(lane);
        driver.setSpawnPoint(spawnPoint);
        vehicle.setDriver(driver);

        return vehicle;
    }

    /////////////////////////////////
    // STEP 2
    /////////////////////////////////

    /**
     * Provide each vehicle with sensor information to allow it to make
     * decisions.  This works first by making an ordered list for each Lane of
     * all the vehicles in that Lane, in order from the start of the Lane to
     * the end of the Lane.  We must make sure to leave out all vehicles that
     * are in the intersection.  We must also concatenate the lists for lanes
     * that feed into one another.  Then, for each vehicle, depending on the
     * state of its sensors, we provide it with the appropriate sensor input.
     */
    protected void provideSensorInput() {
        Map<Lane,SortedMap<Double,CPMBasicAutoVehicle>> vehicleLists =
                computeVehicleLists();
        Map<CPMBasicAutoVehicle, CPMBasicAutoVehicle> nextVehicle =
                computeNextVehicle(vehicleLists);

        provideIntervalInfo(nextVehicle);
        provideVehicleTrackingInfo(vehicleLists);
    }

    /**
     * Compute the lists of vehicles of all lanes.
     *
     * @return a mapping from lanes to lists of vehicles sorted by their
     *         distance on their lanes
     */
    private Map<Lane,SortedMap<Double,CPMBasicAutoVehicle>> computeVehicleLists() {
        // Set up the structure that will hold all the Vehicles as they are
        // currently ordered in the Lanes
        Map<Lane,SortedMap<Double,CPMBasicAutoVehicle>> vehicleLists =
                new HashMap<Lane,SortedMap<Double,CPMBasicAutoVehicle>>();
        for(Road road : map.getRoads()) {
            for(Lane lane : road.getLanes()) {
                vehicleLists.put(lane, new TreeMap<Double,CPMBasicAutoVehicle>());
            }
        }
        // Now add each of the Vehicles, but make sure to exclude those that are
        // already inside (partially or entirely) the intersection
        for(CPMBasicAutoVehicle vehicle : vinToVehicles.values()) {
            // Find out what lanes it is in.
            Set<Lane> lanes = vehicle.getDriver().getCurrentlyOccupiedLanes();
            for(Lane lane : lanes) {
                // Now find how far along the lane it is.
                double dst = lane.distanceAlongLane(vehicle.getPosition());
                // Now add it to the map.
                vehicleLists.get(lane).put(dst, vehicle);
            }
        }
        // Now consolidate the lists based on lanes
        for(Road road : map.getRoads()) {
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
    private Map<CPMBasicAutoVehicle, CPMBasicAutoVehicle> computeNextVehicle(
            Map<Lane,SortedMap<Double,CPMBasicAutoVehicle>> vehicleLists) {
        // At this point we should only have mappings for start Lanes, and they
        // should include all the Lanes they run into.  Now we need to turn this
        // into a hash map that maps Vehicles to the next vehicle in the Lane
        // or any Lane the Lane runs into
        Map<CPMBasicAutoVehicle, CPMBasicAutoVehicle> nextVehicle =
                new HashMap<CPMBasicAutoVehicle,CPMBasicAutoVehicle>();
        // For each of the ordered lists of vehicles
        for(SortedMap<Double,CPMBasicAutoVehicle> vehicleList : vehicleLists.values()) {
            CPMBasicAutoVehicle lastVehicle = null;
            // Go through the Vehicles in order of their position in the Lane
            for(CPMBasicAutoVehicle currVehicle : vehicleList.values()) {
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
     * Provide sensing information to the intervalometers of all vehicles.
     *
     * @param nextVehicle  a mapping from vehicles to next vehicles
     */
    private void provideIntervalInfo(
            Map<CPMBasicAutoVehicle, CPMBasicAutoVehicle> nextVehicle) {

        // Now that we have this list set up, let's provide input to all the
        // Vehicles.
        for(CPMBasicAutoVehicle vehicle: vinToVehicles.values()) {
            // If the vehicle is autonomous
            if (vehicle != null) {
                switch(vehicle.getLRFMode()) {
                    case DISABLED:
                        // Find the interval to the next vehicle
                        double interval;
                        // If there is a next vehicle, then calculate it
                        if(nextVehicle.containsKey(vehicle)) {
                            // It's the distance from the front of this Vehicle to the point
                            // at the rear of the Vehicle in front of it
                            interval = calcInterval(vehicle, nextVehicle.get(vehicle));
                        } else { // Otherwise, just set it to the maximum possible value
                            interval = Double.MAX_VALUE;
                        }
                        // Now actually record it in the vehicle
                        vehicle.getIntervalometer().record(interval);
                        vehicle.setLRFSensing(false); // Vehicle is not using
                        // the LRF sensor
                        break;
                    case LIMITED:
                        // FIXME
                        vehicle.setLRFSensing(true); // Vehicle is using the LRF sensor
                        break;
                    case ENABLED:
                        // FIXME
                        vehicle.setLRFSensing(true); // Vehicle is using the LRF sensor
                        break;
                    default:
                        throw new RuntimeException("Unknown LRF Mode: " +
                                vehicle.getLRFMode().toString());
                }
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
    private double calcInterval(CPMBasicAutoVehicle vehicle,
                                CPMBasicAutoVehicle nextVehicle) {
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

    /**
     * Provide tracking information to vehicles.
     *
     * @param vehicleLists  a mapping from lanes to lists of vehicles sorted by
     *                      their distance on their lanes
     */
    private void provideVehicleTrackingInfo(
            Map<Lane, SortedMap<Double, CPMBasicAutoVehicle>> vehicleLists) {
        // Vehicle Tracking
        for(CPMBasicAutoVehicle vehicle: vinToVehicles.values()) {
            // If the vehicle is autonomous
            if (vehicle != null) {
                CPMBasicAutoVehicle autoVehicle = vehicle;

                if (autoVehicle.isVehicleTracking()) {
                    CPMV2VDriver driver = (CPMV2VDriver)autoVehicle.getDriver();
                    Lane targetLane = autoVehicle.getTargetLaneForVehicleTracking();
                    Point2D pos = autoVehicle.getPosition();
                    double dst = targetLane.distanceAlongLane(pos);

                    // initialize the distances to infinity
                    double frontDst = Double.MAX_VALUE;
                    double rearDst = Double.MAX_VALUE;
                    CPMBasicAutoVehicle frontVehicle = null ;
                    CPMBasicAutoVehicle rearVehicle = null ;

                    // only consider the vehicles on the target lane
                    SortedMap<Double,CPMBasicAutoVehicle> vehiclesOnTargetLane =
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

    /////////////////////////////////
    // STEP 3
    /////////////////////////////////

    /**
     * Find the vehicle that is directly in front of each vehicle.
     * Ideally, would like to use sensors, but lack of time and
     * current understanding of LRF means we need a workaround.
     */
    protected void findNextVehicles() {
        for (CPMBasicAutoVehicle vehicle : map.getVehicles()){
            vehicle.setVehicleInFront(getVehicleInFront(vehicle, map));
        }
    }

    /**
     * Get the vehicle that is directly in front of the given vehicle,
     * on the same parking lane.
     *
     * @param vehicle the vehicle we want to find the vehicle in front for.
     * @param map the map that the vehicle belongs to.
     * @return the vehicle in front of the one given.
     */
    public CPMBasicAutoVehicle getVehicleInFront(CPMBasicAutoVehicle vehicle,
                                                        CPMMap map) {
        if (!(vehicle.getDriver().getCurrentLane() instanceof ParkingLane)) {
            return null;
        }

        // Get all vehicles on the same lane
        Lane lane = vehicle.getDriver().getCurrentLane();
        List<CPMBasicAutoVehicle> vehiclesInFront = new ArrayList<CPMBasicAutoVehicle>();

        for (CPMBasicAutoVehicle v : map.getVehicles()) {
            if (v.getDriver().getCurrentLane() == lane) {
                // Check if the vehicle is in front
                if (v.getPosition().getX() > vehicle.getPosition().getX()) {
                    vehiclesInFront.add(v);
                }
            }
        }

        // Now get the one that is directly in front
        CPMBasicAutoVehicle closest = null;
        for (CPMBasicAutoVehicle v : vehiclesInFront) {
            if (closest == null || v.getPosition().getX() < closest.getPosition().getX()){
                closest = v;
            }
        }

        return closest;
    }

    /////////////////////////////////
    // STEP 4
    /////////////////////////////////

    /**
     * Allow each driver to act.
     */
    protected void letDriversAct() {
        for(CPMBasicAutoVehicle vehicle : vinToVehicles.values()) {
            vehicle.getDriver().act();
        }
    }

    /////////////////////////////////
    // STEP 5
    /////////////////////////////////

    /**
     * Move all the vehicles.
     *
     * @param timeStep  the time step
     */
    protected void moveVehicles(double timeStep) {
        for(CPMBasicAutoVehicle vehicle : vinToVehicles.values()) {
            Point2D p1 = vehicle.getPosition();
            vehicle.move(timeStep);
            Point2D p2 = vehicle.getPosition();

            CPMMapUtil.checkVehicleStillOnMap(map, p2, vehicle.getDriver().getCurrentLane());

            // Check if we've gone through a data collection line
            for(DataCollectionLine line : map.getDataCollectionLines()) {
                line.intersect(vehicle, currentTime, p1, p2);
            }

            // Check if we've gone through a sensored line
            // TODO CPM try remove the need for this assertion
            assert map instanceof CPMCarParkWithStatus;
            for (SensoredLine line : ((CPMCarParkWithStatus) map).getSensoredLines()) {
                 if (line.intersect(vehicle, currentTime, p1, p2)) {
                     StatusMonitor statusMonitor = map.getStatusMonitor();
                     if (line.getType() == SensoredLine.SensoredLineType.ENTRY) {
                         System.out.println("Vehicle is entering.");
                         vehicle.setEntryTime(currentTime);
                         statusMonitor.vehicleOnEntry(vehicle);
                     }
                     if (line.getType() == SensoredLine.SensoredLineType.REENTRY) {
                         System.out.println("Vehicle is re-entering.");
                         statusMonitor.vehicleOnReEntry(vehicle);
                     }
                     if (line.getType() == SensoredLine.SensoredLineType.EXIT) {
                         System.out.println("Vehicle is exiting.");
                         vehicle.setExitTime(currentTime);
                         statusMonitor.vehicleOnExit(vehicle);
                     }
                 }
            }

            // Update the time left for the vehicle to be parked.
            if (vehicle.hasEnteredCarPark()) {
                vehicle.updateTimeToExit(timeStep);
            }

            if (Debug.isPrintVehicleStateOfVIN(vehicle.getVIN())) {
                vehicle.printState();
            }
        }
    }

    /////////////////////////////////
    // STEP 6
    /////////////////////////////////

    protected void observeParkedVehicles() {
        parkedVehicles.clear();
        List<CPMBasicAutoVehicle> vehicles = map.getVehicles();
        for (CPMBasicAutoVehicle vehicle : vehicles) {
            // Check if the vehicle is in a parking lane.
            CPMV2VDriver driver = (CPMV2VDriver) vehicle.getDriver();
            // Check the vehicle is not moving.
            if (driver.inParkingLane() && vehicle.getVelocity() == 0) {
                parkedVehicles.add(vehicle);
            }
        }
    }

    /////////////////////////////////
    // STEP 7
    /////////////////////////////////

    protected void observeNumberOfVehiclesInCarPark() {
        if (map.getStatusMonitor() != null) {
            map.getStatusMonitor().updateMostNumberOfVehicles();
        }
    }

    /////////////////////////////////
    // STEP 8
    /////////////////////////////////

    /**
     * Remove all completed vehicles.
     *
     * @return the VINs of the completed vehicles
     */
    protected List<CPMBasicAutoVehicle> cleanUpCompletedVehicles() {
        List<CPMBasicAutoVehicle> completedVehicles = new LinkedList<CPMBasicAutoVehicle>();
        Rectangle2D mapBoundary = map.getDimensions();
        List<Integer> removedVINs = new ArrayList<Integer>(vinToVehicles.size());
        for(int vin : vinToVehicles.keySet()) {
            CPMBasicAutoVehicle vehicle = vinToVehicles.get(vin);
            // If the vehicle is no longer in the layout
            if(!vehicle.getShape().intersects(mapBoundary)) {
                // Process anything we need to from this vehicle
                // TODO CPM Do we need to get anything? Maybe distance travelled
                map.removeCompletedVehicle(vehicle);
                removedVINs.add(vin);
            }
        }
        // Remove the marked vehicles
        for(int vin : removedVINs) {
            completedVehicles.add(vinToVehicles.get(vin));
            vinToVehicles.remove(vin);
            numOfCompletedVehicles++;
        }
        return completedVehicles;
    }


    @Override
    public CPMMap getMap() {
        return map;
    }

    @Override
    public double getSimulationTime() {
        return currentTime;
    }

    @Override
    public int getNumCompletedVehicles() {
        return numOfCompletedVehicles;
    }

    public List<CPMBasicAutoVehicle> getParkedVehicles() { return parkedVehicles; }

    @Override
    public double getAvgBitsTransmittedByCompletedVehicles() {
        return 0;
    }

    @Override
    public double getAvgBitsReceivedByCompletedVehicles() {
        return 0;
    }

    @Override
    public VehicleSimModel getActiveVehicle(int vin) {
        return null;
    }

    public Map<Integer, CPMBasicAutoVehicle> getVinToVehicles() { return vinToVehicles; }

    public String produceResultsCSV(){
        StringBuilder sb = new StringBuilder();
        sb.append("Sorry, CPM does not produce a results file");
        return sb.toString();
    }

    @Override
    public SimulatorResult produceResult() {
        //Sorry, CPM does not produce results in this way.
        throw new NotImplementedException();
    }

    public SimulatorResult produceMergeResult(){
        //Sorry, CPM does not produce results in this way.
        throw new NotImplementedException();
    }
}
