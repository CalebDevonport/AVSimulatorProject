package aim4.sim.simulator.rim;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.driver.rim.ProxyDriver;
import aim4.driver.rim.RIMAutoDriver;
import aim4.im.rim.IntersectionManager;
import aim4.im.rim.v2i.V2IManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.msg.rim.i2v.I2VMessage;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.sim.results.Result;
import aim4.sim.results.VehicleResult;
import aim4.sim.simulator.rim.helper.SpawnHelper;
import aim4.util.Util;
import aim4.vehicle.VehicleUtil;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.rim.ProxyVehicleSimModel;
import aim4.vehicle.rim.RIMAutoVehicleSimModel;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 * The autonomous drivers only simulator.
 */
public class AutoDriverOnlySimulator implements RIMSimulator{

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
    private BasicRIMIntersectionMap basicRIMIntersectionMap;
    /** All active vehicles, in form of a map from VINs to vehicle objects. */
    public Map<Integer,RIMVehicleSimModel> vinToVehicles;
    /** The current time */
    private double currentTime;
    /** The number of completed vehicles */
    private int numOfCompletedVehicles;
    /** The total number of bits transmitted by the completed vehicles */
    private int totalBitsTransmittedByCompletedVehicles;
    /** The total number of bits received by the completed vehicles */
    private int totalBitsReceivedByCompletedVehicles;

    //Results aids//
    private List<VehicleResult> vehiclesRecord;

    //HELPERS//
    SpawnHelper spawnHelper;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create an instance of the simulator.
     *
     * @param basicRIMIntersectionMap             the map of the simulation
     */
    public AutoDriverOnlySimulator(BasicRIMIntersectionMap basicRIMIntersectionMap) {
        this.basicRIMIntersectionMap = basicRIMIntersectionMap;
        this.vinToVehicles = new HashMap<Integer,RIMVehicleSimModel>();
        this.spawnHelper = new SpawnHelper(basicRIMIntersectionMap, vinToVehicles);
        this.vehiclesRecord = new ArrayList<VehicleResult>();

        currentTime = 0.0;
        numOfCompletedVehicles = 0;
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
        spawnHelper.spawnVehicles(timeStep);
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
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:cleanUpCompletedVehicles---------------\n");
        }
        if (Debug.CHECK_FOR_COLLISIONS) {
            System.err.printf("------SIM:checkForCollisions---------------\n");
            checkForCollisions();
        }
        if (Debug.PRINT_SIMULATOR_STAGE){
            System.err.printf("------SIM:calculateCompletedVehicles---------------\n");
        }
        List<RIMVehicleSimModel> completedVehicles = calculateCompletedVehicles();
        provideCompletedVehiclesWithResultsInfo(completedVehicles);
        recordCompletedVehicles(completedVehicles);
        updateMaxMinVelocities();
        if (Debug.PRINT_SIMULATOR_STAGE) {
            System.err.printf("------SIM:cleanUpCompletedVehicles---------------\n");
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
    public synchronized BasicRIMIntersectionMap getMap() {
        return basicRIMIntersectionMap;
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
    public synchronized Set<RIMVehicleSimModel> getActiveVehicles() {
        return new HashSet<RIMVehicleSimModel>(vinToVehicles.values());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RIMVehicleSimModel getActiveVehicle(int vin) {
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

        for(Road road : basicRIMIntersectionMap.getRoads()) {
            for(Lane lane : road.getContinuousLanes()) {
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
    // STEP 2
    /////////////////////////////////

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
        for(Road road : basicRIMIntersectionMap.getRoads()) {
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
                                            if (interval < 0) {
                                                int i = 2;
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
                                        if (interval < 0) {
                                            int i = 2;
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
     * Determine whether the given point intersects the Area governed
     * by this IntersectionManager.
     *
     * @return          whether the point intersects the Area governed by
     *                  this IntersectionManager
     */
    public boolean intersectsEntirely(Rectangle2D bounds, Shape vehicle) {
        Area laneArea = new Area(bounds);
        return laneArea.contains(vehicle.getBounds2D()) && laneArea.intersects(vehicle.getBounds2D());
    }


    /**
     * Determine whether the given point intersects the Area governed
     * by this IntersectionManager.
     *
     * @param point     the Point
     * @return          whether the point intersects the Area governed by
     *                  this IntersectionManager
     */
    public boolean intersectsPoint(Rectangle2D bounds, Point2D point) {
        return (bounds.getX() < point.getX() && bounds.getY() < point.getY() &&
                bounds.getX() + bounds.getWidth() > point.getX()  &&
                bounds.getY() + bounds.getHeight() > point.getY());
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
        Map<Lane,SortedMap<Double,RIMVehicleSimModel>> vehicleLists =
                computeVehicleLists();
        Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle =
                computeNextVehicle(vehicleLists);

        provideIntervalInfo(nextVehicle);
        provideVehicleTrackingInfo(vehicleLists);
    }

    /**
     * Provide sensing information to the intervalometers of all vehicles.
     *
     * @param nextVehicle  a mapping from vehicles to next vehicles
     */
    private void provideIntervalInfo(
            Map<RIMVehicleSimModel, RIMVehicleSimModel> nextVehicle) {

        // Now that we have this list set up, let's provide input to all the
        // Vehicles.
        for(RIMVehicleSimModel vehicle: vinToVehicles.values()) {
            // If the vehicle is autonomous
            if (vehicle instanceof RIMAutoVehicleSimModel) {
                RIMAutoVehicleSimModel autoVehicle = (RIMAutoVehicleSimModel)vehicle;

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
            Map<Lane, SortedMap<Double, RIMVehicleSimModel>> vehicleLists) {
        // Vehicle Tracking
        for(RIMVehicleSimModel vehicle: vinToVehicles.values()) {
            // If the vehicle is autonomous
            if (vehicle instanceof RIMAutoVehicleSimModel) {
                RIMAutoVehicleSimModel autoVehicle = (RIMAutoVehicleSimModel)vehicle;

                if (autoVehicle.isVehicleTracking()) {
                    RIMAutoDriver driver = autoVehicle.getDriver();
                    Lane targetLane = autoVehicle.getTargetLaneForVehicleTracking();
                    Point2D pos = autoVehicle.getPosition();
                    assert targetLane instanceof LineSegmentLane;
                    double dst = targetLane.distanceAlongLane(pos);

                    // initialize the distances to infinity
                    double frontDst = Double.MAX_VALUE;
                    double rearDst = Double.MAX_VALUE;
                    RIMVehicleSimModel frontVehicle = null ;
                    RIMVehicleSimModel rearVehicle = null ;

                    // only consider the vehicles on the target lane
                    SortedMap<Double,RIMVehicleSimModel> vehiclesOnTargetLane =
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
     * Calculate the distance between vehicle and the next vehicle on a lane.
     *
     * @param vehicle      the vehicle
     * @param nextVehicle  the next vehicle
     * @return the distance between vehicle and the next vehicle on a lane
     */
    private double calcInterval(RIMVehicleSimModel vehicle,
                                RIMVehicleSimModel nextVehicle) {
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

    /////////////////////////////////
    // STEP 3
    /////////////////////////////////

    /**
     * Allow each driver to act.
     */
    private void letDriversAct() {
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
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
        for(IntersectionManager im : basicRIMIntersectionMap.getIntersectionManagers()) {
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
    public void deliverV2IMessages() {
        // Go through each vehicle and deliver each of its messages
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            // Start with V2I messages
            if (vehicle instanceof RIMAutoVehicleSimModel) {
                RIMAutoVehicleSimModel sender = (RIMAutoVehicleSimModel)vehicle;
                Queue<V2IMessage> v2iOutbox = sender.getV2IOutbox();
                while(!v2iOutbox.isEmpty()) {
                    V2IMessage msg = v2iOutbox.poll();
                    V2IManager receiver =
                            (V2IManager) basicRIMIntersectionMap.getImRegistry().get(msg.getImId());
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
    public void deliverI2VMessages() {
        // Now deliver all the I2V messages
        for(IntersectionManager im : basicRIMIntersectionMap.getIntersectionManagers()) {
            V2IManager senderIM = (V2IManager)im;
            for(Iterator<I2VMessage> i2vIter = senderIM.outboxIterator();
                i2vIter.hasNext();) {
                I2VMessage msg = i2vIter.next();
                RIMAutoVehicleSimModel vehicle =
                        (RIMAutoVehicleSimModel)VinRegistry.getVehicleFromVIN(
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
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            Point2D p1 = vehicle.getPosition();
            vehicle.move(timeStep);
            Point2D p2 = vehicle.getPosition();
            for(DataCollectionLine line : basicRIMIntersectionMap.getDataCollectionLines()) {
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
            RIMVehicleSimModel vehicle1 = vinToVehicles.get(keys[i]);
            for(int j = 0; j < keysToCompare.length; j++) {
                RIMVehicleSimModel vehicle2 = vinToVehicles.get(keysToCompare[j]);
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

        Rectangle2D mapBoundary = basicRIMIntersectionMap.getDimensions();

        List<Integer> removedVINs = new ArrayList<Integer>(vinToVehicles.size());
        for(int vin : vinToVehicles.keySet()) {
            RIMVehicleSimModel v = vinToVehicles.get(vin);
            // If the vehicle is no longer in the layout
            // TODO: this should be replaced with destination zone.
            if(!v.getShape().intersects(mapBoundary)) {
                // Process all the things we need to from this vehicle
                if (v instanceof RIMAutoVehicleSimModel) {
                    RIMAutoVehicleSimModel v2 = (RIMAutoVehicleSimModel)v;
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
    private List<RIMVehicleSimModel> calculateCompletedVehicles() {
        List<RIMVehicleSimModel> completedVehicles = new LinkedList<RIMVehicleSimModel>();

        Rectangle2D mapBoundary = basicRIMIntersectionMap.getDimensions();
        for(int vin : vinToVehicles.keySet()) {
            if(!vinToVehicles.get(vin).getShape().intersects(mapBoundary))
                completedVehicles.add(vinToVehicles.get(vin));
        }

        return completedVehicles;
    }

    private void recordCompletedVehicles(List<RIMVehicleSimModel> completedVehicles) {
        for(RIMVehicleSimModel vehicle : completedVehicles) {
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

    private void provideCompletedVehiclesWithResultsInfo(List<RIMVehicleSimModel> completedVehicles) {
        for(RIMVehicleSimModel vehicle : completedVehicles) {
            vehicle.setFinishTime(currentTime);
            vehicle.setFinalVelocity(vehicle.getVelocity());
            vehicle.setFinalXPos(vehicle.getPosition().getX());
            vehicle.setFinalYPos(vehicle.getPosition().getY());
        }
    }

    private void updateMaxMinVelocities() {
        for(int vin : vinToVehicles.keySet()) {
            RIMVehicleSimModel vehicle = vinToVehicles.get(vin);
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

    public String produceResultsCSV(){
        return produceResult().produceCSVString();
    }

    public Result produceResult() {
        return new Result(vehiclesRecord);
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * Check whether the clocks are in sync.
     */
    private void checkClocks() {
        // Check the clocks for all autonomous vehicles.
        for (RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            vehicle.checkCurrentTime(currentTime);
        }
        // Check the clocks for all the intersection managers.
        for (IntersectionManager im : basicRIMIntersectionMap.getIntersectionManagers()) {
            im.checkCurrentTime(currentTime);
        }
    }
}
