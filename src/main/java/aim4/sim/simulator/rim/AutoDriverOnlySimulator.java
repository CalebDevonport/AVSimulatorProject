package aim4.sim.simulator.rim;

import aim4.im.rim.IntersectionManager;
import aim4.im.rim.v2i.V2IManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.msg.rim.i2v.I2VMessage;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.sim.results.RIMVehicleResult;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.rim.RIMAutoVehicleSimModel;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.util.*;

import static aim4.sim.Simulator.SimStepResult;

/**
 * The autonomous drivers only simulator.
 */
public class AutoDriverOnlySimulator {

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
    private List<RIMVehicleResult> vehiclesRecord;
//    private Map<String, Double> specToExpectedTimeMergeLane;
//    private Map<String, Double> specToExpectedTimeTargetLane;

//    //Merge aids//
//    private boolean mergeMode;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create an instance of the simulator.
     *
     * @param basicRIMIntersectionMap             the map of the simulation
     */
    public AutoDriverOnlySimulator(BasicRIMIntersectionMap basicRIMIntersectionMap) {
        this(basicRIMIntersectionMap, false, null, null);
    }

    public AutoDriverOnlySimulator(BasicRIMIntersectionMap basicRIMIntersectionMap, boolean mergeMode) {
        this(basicRIMIntersectionMap, mergeMode, null, null);
    }

    //Only used with MergeMimicSimSetup
    public AutoDriverOnlySimulator(BasicRIMIntersectionMap basicRIMIntersectionMap,
                                   boolean mergeMode,
                                   Map<String, Double> specToExpectedTimeMergeLane,
                                   Map<String, Double> specToExpectedTimeTargetLane){
//        this.mergeMode = mergeMode;
        this.basicRIMIntersectionMap = basicRIMIntersectionMap;
        this.vinToVehicles = new HashMap<Integer,RIMVehicleSimModel>();
//        if(mergeMode) {
//            Map<String, Double> fakeDelayTimes = new HashMap<String, Double>();
//            for(int specID = 0; specID < VehicleSpecDatabase.getNumOfSpec(); specID++)
//                fakeDelayTimes.put(VehicleSpecDatabase.getVehicleSpecById(specID).getName(), new Double(0));
//            this.vehiclesRecord = new ArrayList<RIMVehicleResult>();
//            if(specToExpectedTimeMergeLane != null)
//                this.specToExpectedTimeMergeLane = specToExpectedTimeMergeLane;
//            else
//                this.specToExpectedTimeMergeLane = fakeDelayTimes;
//            if(specToExpectedTimeTargetLane != null)
//                this.specToExpectedTimeTargetLane = specToExpectedTimeMergeLane;
//            else
//                this.specToExpectedTimeTargetLane = fakeDelayTimes;
//            this.specToExpectedTimeTargetLane = specToExpectedTimeTargetLane;
//        }

        currentTime = 0.0;
        numOfCompletedVehicles = 0;
        totalBitsTransmittedByCompletedVehicles = 0;
        totalBitsReceivedByCompletedVehicles = 0;
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
}
