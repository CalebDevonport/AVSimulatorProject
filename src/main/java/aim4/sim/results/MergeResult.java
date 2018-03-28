package aim4.sim.results;

import aim4.map.merge.RoadNames;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Callum on 21/04/2017.
 */
public class MergeResult implements SimulatorResult {
    private List<MergeVehicleResult> vehicleResults;
    private double maxDelay;
    private double maxTargetDelay;
    private double maxMergeDelay;
    private double minDelay;
    private double minTargetDelay;
    private double minMergeDelay;
    private double averageDelay;
    private double averageTargetDelay;
    private double averageMergeDelay;
    private double stdDevDelay;
    private double stdDevTargetDelay;
    private double stdDevMergeDelay;
    private double throughput;
    private double throughputTarget;
    private double throughputMerge;
    private int completedVehicles;
    private int completedTargetVehicles;
    private int completedMergeVehicles;

    public MergeResult(List<MergeVehicleResult> vehicleResults) {
        this.vehicleResults = vehicleResults;
        List<MergeVehicleResult> targetVehicleResults = new ArrayList<MergeVehicleResult>();
        List<MergeVehicleResult> mergeVehicleResults = new ArrayList<MergeVehicleResult>();

        this.completedVehicles = 0;
        this.completedMergeVehicles = 0;
        this.completedTargetVehicles = 0;
        double lastVehicleTime = 0;
        double lastTargetVehicleTime = 0;
        double lastMergeVehicleTime = 0;

        this.maxDelay = Double.MIN_VALUE;
        this.maxTargetDelay = Double.MIN_VALUE;
        this.maxMergeDelay = Double.MIN_VALUE;
        this.minDelay = Double.MAX_VALUE;
        this.minTargetDelay = Double.MAX_VALUE;
        this.minMergeDelay = Double.MAX_VALUE;
        double totalDelay = 0;
        double totalTargetDelay = 0;
        double totalMergeDelay = 0;

        for(MergeVehicleResult result : vehicleResults) {
            //Split Vehicles
            if(result.getStartingRoad() == RoadNames.TARGET_ROAD.toString()) {
                targetVehicleResults.add(result);
                //Completed Vehicles
                this.completedTargetVehicles++;
                //Throughput Help
                if(lastTargetVehicleTime < result.getFinishTime())
                    lastTargetVehicleTime = result.getFinishTime();
                //Delay
                if(maxTargetDelay < result.getDelayTime())
                    maxTargetDelay = result.getDelayTime();
                if(minTargetDelay > result.getDelayTime())
                    minTargetDelay = result.getDelayTime();
                totalTargetDelay += result.getDelayTime();

            }
            else if(result.getStartingRoad() == RoadNames.MERGING_ROAD.toString()) {
                mergeVehicleResults.add(result);
                //Completed Vehicles
                this.completedMergeVehicles++;
                //Throughput Help
                if(lastMergeVehicleTime < result.getFinishTime())
                    lastMergeVehicleTime = result.getFinishTime();
                //Delay
                if(maxMergeDelay < result.getDelayTime())
                    maxMergeDelay = result.getDelayTime();
                if(minMergeDelay > result.getDelayTime())
                    minMergeDelay = result.getDelayTime();
                totalMergeDelay += result.getDelayTime();
            }
            //Completed Vehicles
            this.completedVehicles++;
            //Throughput Help
            if(lastVehicleTime < result.getFinishTime())
                lastVehicleTime = result.getFinishTime();
            //Delay
            if(maxDelay < result.getDelayTime())
                maxDelay = result.getDelayTime();
            if(minDelay > result.getDelayTime())
                minDelay = result.getDelayTime();
            totalDelay += result.getDelayTime();
        }
        //Average Delay
        this.averageDelay = totalDelay / completedVehicles;
        this.averageTargetDelay = totalTargetDelay / completedTargetVehicles;
        this.averageMergeDelay = totalMergeDelay / completedMergeVehicles;

        //Std. Dev Delay
        this.stdDevDelay = calculateStdDeviationDelay(vehicleResults, averageDelay);
        this.stdDevTargetDelay = calculateStdDeviationDelay(targetVehicleResults, averageTargetDelay);
        this.stdDevMergeDelay = calculateStdDeviationDelay(mergeVehicleResults, averageMergeDelay);

        //Throughput
        this.throughput = completedVehicles / lastVehicleTime;
        this.throughputTarget = completedTargetVehicles / lastTargetVehicleTime;
        this.throughputMerge = completedMergeVehicles / lastMergeVehicleTime;
    }

    public List<MergeVehicleResult> getVehicleResults() {
        return vehicleResults;
    }

    public double getMaxDelay() {
        return maxDelay;
    }

    public double getMaxTargetDelay() {
        return maxTargetDelay;
    }

    public double getMaxMergeDelay() {
        return maxMergeDelay;
    }

    public double getMinDelay() {
        return minDelay;
    }

    public double getMinTargetDelay() {
        return minTargetDelay;
    }

    public double getMinMergeDelay() {
        return minMergeDelay;
    }

    public double getAverageDelay() {
        return averageDelay;
    }

    public double getAverageTargetDelay() {
        return averageTargetDelay;
    }

    public double getAverageMergeDelay() {
        return averageMergeDelay;
    }

    public double getStdDevDelay() { return stdDevDelay; }

    public double getStdDevTargetDelay() { return stdDevTargetDelay; }

    public double getStdDevMergeDelay() { return stdDevMergeDelay; }

    public double getThroughput() {
        return throughput;
    }

    public double getThroughputTarget() {
        return throughputTarget;
    }

    public double getThroughputMerge() {
        return throughputMerge;
    }

    public double getCompletedVehicles() {
        return completedVehicles;
    }

    public double getCompletedTargetVehicles() {
        return completedTargetVehicles;
    }

    public double getCompletedMergeVehicles() {
        return completedMergeVehicles;
    }

    public String produceCSVString() {
        StringBuilder sb = new StringBuilder();
        //Global Stats
        sb.append(produceGlobalStatsCSVHeader());
        sb.append('\n');
        sb.append(produceGlobalStatsCSV());
        sb.append('\n');
        sb.append('\n');
        //Vehicles
        sb.append(produceVehicleStatsCSVHeader());
        sb.append('\n');
        sb.append(produceVehicleStatsCSV());
        sb.append('\n');

        return sb.toString();
    }

    public static String produceGlobalStatsCSVHeader() {
        StringBuilder sb = new StringBuilder();
        sb.append("Max Delay");
        sb.append(',');
        sb.append("Max Target Delay");
        sb.append(',');
        sb.append("Max Merge Delay");
        sb.append(',');
        sb.append("Min Delay");
        sb.append(',');
        sb.append("Min Target Delay");
        sb.append(',');
        sb.append("Min Merge Delay");
        sb.append(',');
        sb.append("Average Delay");
        sb.append(',');
        sb.append("Average Target Delay");
        sb.append(',');
        sb.append("Average Merge Delay");
        sb.append(',');
        sb.append("Std Dev Delay");
        sb.append(',');
        sb.append("Std Dev Target Delay");
        sb.append(',');
        sb.append("Std Dev Merge Delay");
        sb.append(',');
        sb.append("Throughput");
        sb.append(',');
        sb.append("Throughput Target");
        sb.append(',');
        sb.append("Throughput Merge");
        sb.append(',');
        sb.append("Completed Vehicles");
        sb.append(',');
        sb.append("Completed Target Vehicles");
        sb.append(',');
        sb.append("Completed Merge Vehicles");

        return sb.toString();
    }

    public String produceGlobalStatsCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(getMaxDelay());
        sb.append(',');
        sb.append(getMaxTargetDelay());
        sb.append(',');
        sb.append(getMaxMergeDelay());
        sb.append(',');
        sb.append(getMinDelay());
        sb.append(',');
        sb.append(getMinTargetDelay());
        sb.append(',');
        sb.append(getMinMergeDelay());
        sb.append(',');
        sb.append(getAverageDelay());
        sb.append(',');
        sb.append(getAverageTargetDelay());
        sb.append(',');
        sb.append(getAverageMergeDelay());
        sb.append(',');
        sb.append(getStdDevDelay());
        sb.append(',');
        sb.append(getStdDevTargetDelay());
        sb.append(',');
        sb.append(getStdDevMergeDelay());
        sb.append(',');
        sb.append(getThroughput());
        sb.append(',');
        sb.append(getThroughputTarget());
        sb.append(',');
        sb.append(getThroughputMerge());
        sb.append(',');
        sb.append(getCompletedVehicles());
        sb.append(',');
        sb.append(getCompletedTargetVehicles());
        sb.append(',');
        sb.append(getCompletedMergeVehicles());

        return sb.toString();
    }

    public static String produceVehicleStatsCSVHeader(){
        StringBuilder sb = new StringBuilder();
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
        return sb.toString();
    }

    public String produceVehicleStatsCSV(){
        StringBuilder sb = new StringBuilder();
        for(MergeVehicleResult vr : vehicleResults){
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

    private double calculateStdDeviationDelay(List<MergeVehicleResult> results, double mean) {
        double variance = 0;

        double temp = 0;
        for(MergeVehicleResult result : results) {
            temp += (result.getDelayTime() - mean)*(result.getDelayTime() - mean);
        }
        variance = temp / results.size();

        return Math.sqrt(variance);
    }
}
