package aim4.sim.results;

import aim4.util.Util;

import java.util.List;

public class Result implements SimulatorResult{
    private List<VehicleResult> vehicleResults;
    private double throughput;
    private int completedVehicles;
    public Result(List<VehicleResult> vehicleResults) {
        this.vehicleResults = vehicleResults;

        this.completedVehicles = 0;
        double lastVehicleTime = 0;

        if (vehicleResults != null) {
            for (VehicleResult result : vehicleResults) {
                //Completed Vehicles
                this.completedVehicles++;
                //Throughput Help
                if (lastVehicleTime < result.getFinishTime())
                    lastVehicleTime = result.getFinishTime();

                //Throughput
                this.throughput = completedVehicles / lastVehicleTime;
            }
        }
    }

    public List<VehicleResult> getVehicleResults() {
        return vehicleResults;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getCompletedVehicles() {
        return completedVehicles;
    }

    public String produceRIMVsRIMOptimalCSVString(String rimProtocol, Result rimProtocolResult, String optimalProtocol, Result optimalProtocolResult) {
        StringBuilder sb = new StringBuilder();
        //Global Stats
        //Append the average delay
        sb.append("Average Delay");
        sb.append(',');
        sb.append(calculateAverageDelay(rimProtocolResult.getVehicleResults(),rimProtocolResult,optimalProtocolResult));
        sb.append('\n');
        //Append the throughput and completed vehicles
        sb.append(produceRIMVsRIMOptimalStatsCSVHeader(rimProtocol,optimalProtocol));
        sb.append('\n');
        sb.append(produceRIMVsRIMOptimalGlobalStatsCSV(rimProtocolResult,optimalProtocolResult));
        sb.append('\n');
        sb.append('\n');
        //Append Vehicles data
        sb.append(produceRIMVsRIMOptimalVehicleStatsCSVHeader(rimProtocol,optimalProtocol));
        sb.append('\n');
        sb.append(produceRIMVsRIMOptimalVehicleStatsCSV(rimProtocolResult,optimalProtocolResult));
        sb.append('\n');

        return sb.toString();
    }

    public String produceChosenDiameterCSVString(String rimProtocol, Result rimProtocolResult, int rimNumOfVehiclesWhichCouldNotBeSpawned,
                                                 String rimOptimalProtocol, Result rimOptimalRimProtocolResult, int rimOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                                                 String rimStopSignProtocol, Result rimStopSignProtocolResult, int rimStopSignNumOfVehiclesWhichCouldNotBeSpawned,
                                                 String aimCrossProtocol, Result aimCrossProtocolResult, int aimNumOfVehiclesWhichCouldNotBeSpawned,
                                                 String aimCrossOptimalProtocol, Result aimCrossOptimalProtocolResult, int aimOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                                                 String aimCrossStopSignProtocol, Result aimCrossStopSignProtocolResult, int aimStopSignNumOfVehiclesWhichCouldNotBeSpawned) {
        StringBuilder sb = new StringBuilder();
        //Global Stats
        //Append the average delay
        sb.append("(RIM vs. RIM-Optimal) Avg. Delay");
        sb.append(',');
        sb.append("(RIM-StopSign vs. RIM-Optimal) Avg. Delay");
        sb.append(',');
        sb.append(',');
        sb.append("(AIMCross vs. AIMCross-Optimal) Avg. Delay");
        sb.append(',');
        sb.append("(AIMCross-StopSign vs. AIMCross-Optimal) Avg. Delay");
        sb.append('\n');

        sb.append(calculateAverageDelay(rimProtocolResult.getVehicleResults(),rimProtocolResult,rimOptimalRimProtocolResult));
        sb.append(',');
        sb.append(calculateAverageDelay(rimStopSignProtocolResult.getVehicleResults(),rimStopSignProtocolResult,rimOptimalRimProtocolResult));
        sb.append(',');
        sb.append(',');
        sb.append(calculateAverageDelay(aimCrossProtocolResult.getVehicleResults(),aimCrossProtocolResult,aimCrossOptimalProtocolResult));
        sb.append(',');
        sb.append(calculateAverageDelay(aimCrossStopSignProtocolResult.getVehicleResults(),aimCrossStopSignProtocolResult,aimCrossOptimalProtocolResult));
        sb.append('\n');

        //Append the completed vehicles and no. of vehicles which could not be spawned
        sb.append(produceChosenDiameterGlobalStatsCSVHeader(rimProtocol,rimOptimalProtocol,rimStopSignProtocol,aimCrossProtocol,aimCrossOptimalProtocol,aimCrossStopSignProtocol));
        sb.append('\n');
        sb.append(produceChosenDiameterGlobalStatsCSV(rimProtocolResult, rimNumOfVehiclesWhichCouldNotBeSpawned,
                rimOptimalRimProtocolResult, rimOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                rimStopSignProtocolResult, rimStopSignNumOfVehiclesWhichCouldNotBeSpawned,
                aimCrossProtocolResult, aimNumOfVehiclesWhichCouldNotBeSpawned,
                aimCrossOptimalProtocolResult, aimOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                aimCrossStopSignProtocolResult, aimStopSignNumOfVehiclesWhichCouldNotBeSpawned));
        sb.append('\n');
        sb.append('\n');

        //Append Vehicles data
        sb.append(produceChosenDiameterVehicleStatsCSVHeader(rimProtocol,rimOptimalProtocol,rimStopSignProtocol,aimCrossProtocol,aimCrossOptimalProtocol,aimCrossStopSignProtocol));
        sb.append('\n');
        sb.append(produceChosenDiameterVehicleStatsCSV(rimProtocolResult,rimOptimalRimProtocolResult,rimStopSignProtocolResult,aimCrossProtocolResult,aimCrossOptimalProtocolResult,
                aimCrossStopSignProtocolResult));
        sb.append('\n');

        return sb.toString();
    }

    private static String calculateAverageDelay(List<VehicleResult> vehicleResults, Result firstProtocolResult, Result secondProtocolResult) {
        double sum = 0.0;
        int count = 0;
        for (VehicleResult vr : vehicleResults) {
            VehicleResult firstProtocolMatchingVehicle = null;
            VehicleResult secondProtocolMatchingVehicle = null;
            for (VehicleResult match : firstProtocolResult.getVehicleResults()) {
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())) {
                    firstProtocolMatchingVehicle = match;
                    break;
                }
            }
            for (VehicleResult match : secondProtocolResult.getVehicleResults()) {
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())) {
                    secondProtocolMatchingVehicle = match;
                    break;
                }
            }
            if (firstProtocolMatchingVehicle!=null && secondProtocolMatchingVehicle != null){
                sum += firstProtocolMatchingVehicle.getFinishTime() - secondProtocolMatchingVehicle.getFinishTime();
                count++;
            }

        }
        return Double.toString(sum / count);
    }

    public static String produceRIMVsRIMOptimalStatsCSVHeader(String firstProtocol, String secondProtocol) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstProtocol + "Throughput" );
        sb.append(',');
        sb.append(firstProtocol + "Completed Vehicles");
        sb.append(',');
        sb.append(',');
        sb.append(secondProtocol + "Throughput" );
        sb.append(',');
        sb.append(secondProtocol + "Completed Vehicles");

        return sb.toString();
    }

    public static String produceChosenDiameterGlobalStatsCSVHeader(String rimProtocol, String rimOptimalProtocol, String rimStopSignProtocol,
                                                                   String aimCrossProtocol, String aimCrossOptimalProtocol, String aimCrossStopSignProtocol) {
        StringBuilder sb = new StringBuilder();
        sb.append(rimProtocol + " Completed Vehicles");
        sb.append(',');
        sb.append(rimProtocol + " Remained Vehicles" );
        sb.append(',');
        sb.append(',');
        sb.append(rimOptimalProtocol + " Completed Vehicles");
        sb.append(',');
        sb.append(rimOptimalProtocol + " Remained Vehicles" );
        sb.append(',');
        sb.append(',');
        sb.append(rimStopSignProtocol + " Completed Vehicles" );
        sb.append(',');
        sb.append(rimStopSignProtocol + " Remained Vehicles");
        sb.append(',');
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossProtocol + " Completed Vehicles");
        sb.append(',');
        sb.append(aimCrossProtocol + " Remained Vehicles");
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + " Completed Vehicles" );
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + " Remained Vehicles");
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + " Completed Vehicles" );
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + " Remained Vehicles");

        return sb.toString();
    }

    public String produceRIMVsRIMOptimalGlobalStatsCSV(Result firstProtocolResult, Result secondProtocolResult) {
        StringBuilder sb = new StringBuilder();
        sb.append(firstProtocolResult.getThroughput());
        sb.append(',');
        sb.append(firstProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(',');
        sb.append(secondProtocolResult.getThroughput());
        sb.append(',');
        sb.append(secondProtocolResult.getCompletedVehicles());

        return sb.toString();
    }

    public String produceChosenDiameterGlobalStatsCSV(Result rimProtocolResult, int rimRemained, Result rimOptimalProtocolResult, int rimOptimalRemained,
                                                      Result rimStopSignProtocolResult, int rimStopSignRemained,
                                                      Result aimCrossProtocolResult, int aimRemained, Result aimCrossOptimalProtocolResult, int aimOptimalRemained,
                                                      Result aimCrossStopSignsProtocolResult, int aimStopSignsRemained) {
        StringBuilder sb = new StringBuilder();
        sb.append(rimProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(rimRemained);
        sb.append(',');
        sb.append(',');
        sb.append(rimOptimalProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(rimOptimalRemained);
        sb.append(',');
        sb.append(',');
        sb.append(rimStopSignProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(rimStopSignRemained);
        sb.append(',');
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(aimRemained);
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossOptimalProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(aimOptimalRemained);
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossStopSignsProtocolResult.getCompletedVehicles());
        sb.append(',');
        sb.append(aimStopSignsRemained);


        return sb.toString();
    }

    public static String produceRIMVsRIMOptimalVehicleStatsCSVHeader(String firstProtocol, String secondProtocol){
        StringBuilder sb = new StringBuilder();
        //Headings
        sb.append(firstProtocol + "VIN");
        sb.append(',');
        sb.append(firstProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(firstProtocol + "Start Time");
        sb.append(',');
        sb.append(firstProtocol + "Finish Time");
        sb.append(',');
        sb.append(firstProtocol +"Final Velocity");
        sb.append(',');
        sb.append(firstProtocol + "Max Velocity");
        sb.append(',');
        sb.append(firstProtocol + "Min Velocity");
        sb.append(',');
        sb.append(',');
        sb.append(secondProtocol + "VIN");
        sb.append(',');
        sb.append(secondProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(secondProtocol + "Start Time");
        sb.append(',');
        sb.append(secondProtocol + "Finish Time");
        sb.append(',');
        sb.append(secondProtocol +"Final Velocity");
        sb.append(',');
        sb.append(secondProtocol + "Max Velocity");
        sb.append(',');
        sb.append(secondProtocol + "Min Velocity");
        sb.append(',');
        sb.append(',');
        sb.append("Delay");
        return sb.toString();
    }

    public static String produceChosenDiameterVehicleStatsCSVHeader(String rimProtocol, String rimOptimalProtocol, String rimStopSignProtocol, String aimCrossProtocol,
                                                                    String aimCrossOptimalProtocol, String aimCrossStopSignProtocol){
        StringBuilder sb = new StringBuilder();
        //Headings
        sb.append(rimProtocol + "VIN");
        sb.append(',');
        sb.append(rimProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(rimProtocol + "Start Time");
        sb.append(',');
        sb.append(rimProtocol + "Finish Time");
        sb.append(',');
        sb.append(rimProtocol +"Final Velocity");
        sb.append(',');
        sb.append(rimProtocol + "Max Velocity");
        sb.append(',');
        sb.append(rimProtocol + "Min Velocity");
        sb.append(',');
        sb.append(',');
        sb.append(rimOptimalProtocol + "VIN");
        sb.append(',');
        sb.append(rimOptimalProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(rimOptimalProtocol + "Start Time");
        sb.append(',');
        sb.append(rimOptimalProtocol + "Finish Time");
        sb.append(',');
        sb.append(rimOptimalProtocol +"Final Velocity");
        sb.append(',');
        sb.append(rimOptimalProtocol + "Max Velocity");
        sb.append(',');
        sb.append(rimOptimalProtocol + "Min Velocity");
        sb.append(',');
        sb.append(rimProtocol + "-" + rimOptimalProtocol +"Delay");
        sb.append(',');
        sb.append(',');
        sb.append(rimStopSignProtocol + "VIN");
        sb.append(',');
        sb.append(rimStopSignProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(rimStopSignProtocol + "Start Time");
        sb.append(',');
        sb.append(rimStopSignProtocol + "Finish Time");
        sb.append(',');
        sb.append(rimStopSignProtocol +"Final Velocity");
        sb.append(',');
        sb.append(rimStopSignProtocol + "Max Velocity");
        sb.append(',');
        sb.append(rimStopSignProtocol + "Min Velocity");
        sb.append(',');
        sb.append(rimStopSignProtocol + "-" + rimOptimalProtocol +"Delay");
        sb.append(',');
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossProtocol + "VIN");
        sb.append(',');
        sb.append(aimCrossProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(aimCrossProtocol + "Start Time");
        sb.append(',');
        sb.append(aimCrossProtocol + "Finish Time");
        sb.append(',');
        sb.append(aimCrossProtocol +"Final Velocity");
        sb.append(',');
        sb.append(aimCrossProtocol + "Max Velocity");
        sb.append(',');
        sb.append(aimCrossProtocol + "Min Velocity");
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + "VIN");
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + "Start Time");
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + "Finish Time");
        sb.append(',');
        sb.append(aimCrossOptimalProtocol +"Final Velocity");
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + "Max Velocity");
        sb.append(',');
        sb.append(aimCrossOptimalProtocol + "Min Velocity");
        sb.append(',');
        sb.append(aimCrossProtocol + "-" + aimCrossOptimalProtocol +"Delay");
        sb.append(',');
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "VIN");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "Vehicle Spec");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "Start Time");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "Finish Time");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol +"Final Velocity");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "Max Velocity");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "Min Velocity");
        sb.append(',');
        sb.append(aimCrossStopSignProtocol + "-" + aimCrossOptimalProtocol +"Delay");
        return sb.toString();
    }

    public String produceRIMVsRIMOptimalVehicleStatsCSV(Result firstProtocolResult, Result secondProtocolResult){
        StringBuilder sb = new StringBuilder();
        for(VehicleResult vr : firstProtocolResult.getVehicleResults()){
            int index = firstProtocolResult.getVehicleResults().indexOf(vr);
            sb.append(vr.getVin());
            sb.append(',');
            sb.append(vr.getSpecType());
            sb.append(',');
            sb.append(vr.getStartTime());
            sb.append(',');
            sb.append(vr.getFinishTime());
            sb.append(',');
            sb.append(vr.getFinalVelocity());
            sb.append(',');
            sb.append(vr.getMaxVelocity());
            sb.append(',');
            sb.append(vr.getMinVelocity());

            sb.append(',');
            sb.append(',');

            VehicleResult matchingVehicle = null;
            for (VehicleResult match: secondProtocolResult.getVehicleResults()){
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())){
                    matchingVehicle = match;
                    break;
                }
            }
            sb.append(matchingVehicle.getVin());
            sb.append(',');
            sb.append(matchingVehicle.getSpecType());
            sb.append(',');
            sb.append(matchingVehicle.getStartTime());
            sb.append(',');
            sb.append(matchingVehicle.getFinishTime());
            sb.append(',');
            sb.append(matchingVehicle.getFinalVelocity());
            sb.append(',');
            sb.append(matchingVehicle.getMaxVelocity());
            sb.append(',');
            sb.append(matchingVehicle.getMinVelocity());
            sb.append(',');
            sb.append(',');
            sb.append(vr.getFinishTime() - matchingVehicle.getFinishTime());
            sb.append('\n');
        }
        return sb.toString();
    }

    public String produceChosenDiameterVehicleStatsCSV(Result rimProtocolResult, Result rimOptimalProtocolResult, Result rimStopSignProtocolResult, Result aimCrossProtocolResult,
                                                       Result aimCrossOptimalProtocolResult, Result aimCrossStopSignProtocolResult){
        StringBuilder sb = new StringBuilder();
        for(VehicleResult vr : rimProtocolResult.getVehicleResults()){
            sb.append(vr.getVin());
            sb.append(',');
            sb.append(vr.getSpecType());
            sb.append(',');
            sb.append(vr.getStartTime());
            sb.append(',');
            sb.append(vr.getFinishTime());
            sb.append(',');
            sb.append(vr.getFinalVelocity());
            sb.append(',');
            sb.append(vr.getMaxVelocity());
            sb.append(',');
            sb.append(vr.getMinVelocity());

            sb.append(',');
            sb.append(',');

            VehicleResult rimOptimalMatchingVehicle = null;
            VehicleResult rimStopSignMatchingVehicle = null;
            VehicleResult aimCrossMatchingVehicle = null;
            VehicleResult aimCrossOptimalMatchingVehicle = null;
            VehicleResult aimCrossStopSignMatchingVehicle = null;
            for (VehicleResult match: rimOptimalProtocolResult.getVehicleResults()){
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())){
                    rimOptimalMatchingVehicle = match;
                    break;
                }
            }
            for (VehicleResult match: rimStopSignProtocolResult.getVehicleResults()){
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())){
                    rimStopSignMatchingVehicle = match;
                    break;
                }
            }
            for (VehicleResult match: aimCrossProtocolResult.getVehicleResults()){
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())){
                    aimCrossMatchingVehicle = match;
                    break;
                }
            }
            for (VehicleResult match: aimCrossOptimalProtocolResult.getVehicleResults()){
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())){
                    aimCrossOptimalMatchingVehicle = match;
                    break;
                }
            }
            for (VehicleResult match: aimCrossStopSignProtocolResult.getVehicleResults()){
                if (Util.isDoubleEqual(vr.getStartTime(), match.getStartTime())){
                    aimCrossStopSignMatchingVehicle = match;
                    break;
                }
            }
            sb.append(rimOptimalMatchingVehicle.getVin());
            sb.append(',');
            sb.append(rimOptimalMatchingVehicle.getSpecType());
            sb.append(',');
            sb.append(rimOptimalMatchingVehicle.getStartTime());
            sb.append(',');
            sb.append(rimOptimalMatchingVehicle.getFinishTime());
            sb.append(',');
            sb.append(rimOptimalMatchingVehicle.getFinalVelocity());
            sb.append(',');
            sb.append(rimOptimalMatchingVehicle.getMaxVelocity());
            sb.append(',');
            sb.append(rimOptimalMatchingVehicle.getMinVelocity());
            sb.append(',');
            sb.append(vr.getFinishTime() - rimOptimalMatchingVehicle.getFinishTime());
            sb.append(',');
            sb.append(',');
            if (rimStopSignMatchingVehicle != null) {
                sb.append(rimStopSignMatchingVehicle.getVin());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getSpecType());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getStartTime());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getFinishTime());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getFinalVelocity());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getMaxVelocity());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getMinVelocity());
                sb.append(',');
                sb.append(rimStopSignMatchingVehicle.getFinishTime() - rimOptimalMatchingVehicle.getFinishTime());
            } else {
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
            }
            sb.append(',');
            sb.append(',');
            sb.append(',');
            if (aimCrossMatchingVehicle != null) {
                sb.append(aimCrossMatchingVehicle.getVin());
                sb.append(',');
                sb.append(aimCrossMatchingVehicle.getSpecType());
                sb.append(',');
                sb.append(aimCrossMatchingVehicle.getStartTime());
                sb.append(',');
                sb.append(aimCrossMatchingVehicle.getFinishTime());
                sb.append(',');
                sb.append(aimCrossMatchingVehicle.getFinalVelocity());
                sb.append(',');
                sb.append(aimCrossMatchingVehicle.getMaxVelocity());
                sb.append(',');
                sb.append(aimCrossMatchingVehicle.getMinVelocity());

            } else {
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
            }
            sb.append(',');
            sb.append(',');
            if (aimCrossOptimalMatchingVehicle != null) {
                sb.append(aimCrossOptimalMatchingVehicle.getVin());
                sb.append(',');
                sb.append(aimCrossOptimalMatchingVehicle.getSpecType());
                sb.append(',');
                sb.append(aimCrossOptimalMatchingVehicle.getStartTime());
                sb.append(',');
                sb.append(aimCrossOptimalMatchingVehicle.getFinishTime());
                sb.append(',');
                sb.append(aimCrossOptimalMatchingVehicle.getFinalVelocity());
                sb.append(',');
                sb.append(aimCrossOptimalMatchingVehicle.getMaxVelocity());
                sb.append(',');
                sb.append(aimCrossOptimalMatchingVehicle.getMinVelocity());
                sb.append(',');
                if (aimCrossMatchingVehicle != null) {
                    sb.append(aimCrossMatchingVehicle.getFinishTime() - aimCrossOptimalMatchingVehicle.getFinishTime());
                }
                else sb.append("N/A");

            } else {
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");

            }
            sb.append(',');
            sb.append(',');
            if (aimCrossStopSignMatchingVehicle != null) {
                sb.append(aimCrossStopSignMatchingVehicle.getVin());
                sb.append(',');
                sb.append(aimCrossStopSignMatchingVehicle.getSpecType());
                sb.append(',');
                sb.append(aimCrossStopSignMatchingVehicle.getStartTime());
                sb.append(',');
                sb.append(aimCrossStopSignMatchingVehicle.getFinishTime());
                sb.append(',');
                sb.append(aimCrossStopSignMatchingVehicle.getFinalVelocity());
                sb.append(',');
                sb.append(aimCrossStopSignMatchingVehicle.getMaxVelocity());
                sb.append(',');
                sb.append(aimCrossStopSignMatchingVehicle.getMinVelocity());
                sb.append(',');
                if (aimCrossOptimalMatchingVehicle != null) {
                    sb.append(aimCrossStopSignMatchingVehicle.getFinishTime() - aimCrossOptimalMatchingVehicle.getFinishTime());
                }
                else sb.append("N/A");

            } else {
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");
                sb.append(',');
                sb.append("N/A");

            }
            sb.append('\n');
        }
        return sb.toString();
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
        sb.append("Throughput");
        sb.append(',');
        sb.append("Completed Vehicles");

        return sb.toString();
    }

    public String produceGlobalStatsCSV() {
        StringBuilder sb = new StringBuilder();
        sb.append(getThroughput());
        sb.append(',');
        sb.append(getCompletedVehicles());

        return sb.toString();
    }

    public static String produceVehicleStatsCSVHeader(){
        StringBuilder sb = new StringBuilder();
        //Headings
        sb.append("VIN");
        sb.append(',');
        sb.append("Vehicle Spec");
        sb.append(',');
        sb.append("Start Time");
        sb.append(',');
        sb.append("Finish Time");
        sb.append(',');
        sb.append("Final Velocity");
        sb.append(',');
        sb.append("Max Velocity");
        sb.append(',');
        sb.append("Min Velocity");
        return sb.toString();
    }

    public String produceVehicleStatsCSV(){
        StringBuilder sb = new StringBuilder();
        for(VehicleResult vr : vehicleResults){
            sb.append(vr.getVin());
            sb.append(',');
            sb.append(vr.getSpecType());
            sb.append(',');
            sb.append(vr.getStartTime());
            sb.append(',');
            sb.append(vr.getFinishTime());
            sb.append(',');
            sb.append(vr.getFinalVelocity());
            sb.append(',');
            sb.append(vr.getMaxVelocity());
            sb.append(',');
            sb.append(vr.getMinVelocity());
            sb.append('\n');
        }
        return sb.toString();
    }

}
