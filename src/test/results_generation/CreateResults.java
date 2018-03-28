package results_generation;

import aim4.config.SimConfig;
import aim4.map.merge.MergeMapUtil;
import aim4.map.merge.RoadNames;
import aim4.sim.results.CoreMergeResult;
import aim4.sim.results.MergeResult;
import aim4.sim.setup.aim.MergeMimicSimSetup;
import aim4.sim.setup.merge.S2SSimSetup;
import aim4.sim.setup.merge.enums.ProtocolType;
import aim4.sim.simulator.aim.AIMSimulator;
import aim4.sim.simulator.merge.MergeSimulator;
import org.json.simple.JSONArray;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by Callum on 28/04/2017.
 */
public class CreateResults {
    private final static int TEST_COUNTS = 20;
    private final static double TIME_LIMIT = 1000;
    private final static double STANDARD_SPEED_LIMIT = 20;
    private final static double STANDARD_TRAFFIC_RATE = 1000;
    private final static double STANDARD_LEAD_IN = 150;
    private final static double STANDARD_ANGLE = 45;
    private final static double STANDARD_TRAFFIC_LEVEL = STANDARD_TRAFFIC_RATE/3600;
    private final static String MEAN_ROW_INDICATOR = "MEAN ROW";
    private final static String TRAFFIC_LEVEL_SCHEDULES_PATH_STEM = new File("").getAbsolutePath() + ("\\schedules\\trafficLevel\\");
    private final static String SPEED_LIMIT_SCHEDULES_PATH_STEM = new File("").getAbsolutePath() + ("\\schedules\\speedLimit\\");
    private final static String RESULTS_STORE_PATH_STEM = new File("").getAbsolutePath() + ("\\results\\");
    private final static String RESULTS_STORE_TRAFFIC_LEVEL_QUEUE_PATH_STEM = RESULTS_STORE_PATH_STEM + "TRAFFIC_LEVEL_QUEUE\\";
    private final static String RESULTS_STORE_TRAFFIC_LEVEL_AIM_PATH_STEM = RESULTS_STORE_PATH_STEM + "TRAFFIC_LEVEL_AIM\\";
    private final static String RESULTS_STORE_SPEED_LIMIT_PATH_STEM = RESULTS_STORE_PATH_STEM + "SPEED_LIMIT\\";
    private static final String RESULTS_STORE_MERGE_ANGLE_PATH_STEM = RESULTS_STORE_PATH_STEM + "MERGE_ANGLE\\";
    private static final String RESULTS_STORE_LEAD_IN_PATH_STEM = RESULTS_STORE_PATH_STEM + "LEAD_IN\\";
    private final static String RESULTS_STORE_GLOBAL_PATH_STEM = RESULTS_STORE_PATH_STEM + "GLOBAL_RESULTS\\";

    private enum TestType {
        TRAFFIC_LEVEL_QUEUE,
        TRAFFIC_LEVEL_AIM,
        SPEED_LIMIT,
        LEAD_IN,
        MERGE_ANGLE
    }

    @Ignore //Change to Test to run individually and create schedules
    public void CreateSpawnSchedules() throws IOException {
        //Create directories
        Path trafficLevelsParentDir = Paths.get(TRAFFIC_LEVEL_SCHEDULES_PATH_STEM);
        if(!Files.exists(trafficLevelsParentDir))
            Files.createDirectories(trafficLevelsParentDir);
        Path speedLimitsParentDir = Paths.get(SPEED_LIMIT_SCHEDULES_PATH_STEM);
        if(!Files.exists(speedLimitsParentDir))
            Files.createDirectories(speedLimitsParentDir);

        //For traffic levels
        double[] trafficRates = new double[]{500,1000,1500,2000,2500};
        for(RoadNames roadType : RoadNames.values()) {
            for (double trafficRate : trafficRates) {

                //Generate Schedules
                double trafficLevel = trafficRate / 3600;
                List<JSONArray> schedules = new ArrayList<JSONArray>();
                for (int i = 1; i <= TEST_COUNTS; i++) {
                    JSONArray schedule = MergeMapUtil.createSpawnSchedule(trafficLevel, TIME_LIMIT, STANDARD_SPEED_LIMIT);
                    schedules.add(schedule);
                }

                //Generate and save JSON Files
                String trafficRateString = Integer.toString(new Double(trafficRate).intValue());
                String roadTypeFirst = roadType.toString().substring(0,1);
                for (int i = 0; i < schedules.size(); i++) {
                    //Generate
                    String jsonString = schedules.get(i).toJSONString();
                    //Prep Save
                    List<String> writeList = new ArrayList<String>();
                    writeList.add(jsonString);
                    String path = new File("").getAbsolutePath();
                    path = TRAFFIC_LEVEL_SCHEDULES_PATH_STEM;
                    path = path.concat(roadTypeFirst + "_" + trafficRateString + "_" + Integer.toString(i+1) + ".json");
                    //Save
                    Files.write(Paths.get(path), writeList, Charset.forName("UTF-8"));
                }
            }
        }

        //For Speed Limits
        double[] speedLimits = new double[]{10,20,30,40};
        for(RoadNames roadType : RoadNames.values()) {
            for (double speedLimit : speedLimits) {
                //Generate Schedules
                List<JSONArray> schedules = new ArrayList<JSONArray>();
                for (int i = 1; i <= TEST_COUNTS; i++) {
                    JSONArray schedule = MergeMapUtil.createSpawnSchedule(STANDARD_TRAFFIC_LEVEL, TIME_LIMIT, speedLimit);
                    schedules.add(schedule);
                }

                //Generate and save JSON Files
                String speedLimitString = Integer.toString(new Double(speedLimit).intValue());
                String roadTypeFirst = roadType.toString().substring(0,1);
                for (int i = 0; i < schedules.size(); i++) {
                    //Generate
                    String jsonString = schedules.get(i).toJSONString();
                    //Prep Save
                    List<String> writeList = new ArrayList<String>();
                    writeList.add(jsonString);
                    String path = new File("").getAbsolutePath();
                    path = SPEED_LIMIT_SCHEDULES_PATH_STEM;
                    path = path.concat(roadTypeFirst + "_" + speedLimitString + "_" + Integer.toString(i+1) + ".json");
                    //Save
                    Files.write(Paths.get(path), writeList, Charset.forName("UTF-8"));
                }
            }
        }
    }

    @Test
    public void QueueTrafficLevelTests() throws IOException {
        //Parameters
        ProtocolType protocolType = ProtocolType.QUEUE;
        double targetLaneSpeedLimit = STANDARD_SPEED_LIMIT;
        double mergingLaneSpeedLimit = STANDARD_SPEED_LIMIT;
        double targetLeadInDistance = STANDARD_LEAD_IN;
        double targetLeadOutDistance = STANDARD_LEAD_IN;
        double mergeLeadInDistance = STANDARD_LEAD_IN;
        double mergingAngle = 90.0;

        List<String> summaryResultsCSV = new ArrayList<String>();

        //Clear out old results
        if(Files.exists(Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_QUEUE_PATH_STEM))) {
            Path rootPath = Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_QUEUE_PATH_STEM);
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.deleteIfExists(Paths.get(RESULTS_STORE_GLOBAL_PATH_STEM + TestType.TRAFFIC_LEVEL_QUEUE.toString() + ".csv"));

        double[] trafficRates = new double[]{500,1000,1500,2000,2500};
        for (double trafficRate : trafficRates) {
            //Setup
            double trafficLevel = trafficRate/3600;
            List<File> targetSpawnSchedules =
                    getSpawnSchedules(RoadNames.TARGET_ROAD, TestType.TRAFFIC_LEVEL_QUEUE, trafficRate);
            List<File> mergeSpawnSchedules =
                    getSpawnSchedules(RoadNames.MERGING_ROAD, TestType.TRAFFIC_LEVEL_QUEUE, trafficRate);

            //Get results
            List<CoreMergeResult> results = new ArrayList<CoreMergeResult>();
            for(int i = 0; i < TEST_COUNTS; i++) {
                File targetSpawnSchedule = targetSpawnSchedules.get(i);
                File mergeSpawnSchedule = mergeSpawnSchedules.get(i);
                S2SSimSetup simSetup = new S2SSimSetup(protocolType, trafficLevel,
                        targetLaneSpeedLimit, mergingLaneSpeedLimit,
                        targetLeadInDistance, targetLeadOutDistance,
                        mergeLeadInDistance, mergingAngle,
                        targetSpawnSchedule, mergeSpawnSchedule
                );
                MergeSimulator sim = simSetup.getSimulator();
                while(sim.getSimulationTime() < (5*TIME_LIMIT)){
                    sim.step(SimConfig.TIME_STEP);
                }
                if(!sim.getVinToVehicles().isEmpty())
                    results.add(null); //Failed to process all vehicles within 5 * TIME_LIMIT
                else {
                    CoreMergeResult result = sim.produceMergeResult();
                    String csvResult = sim.produceResultsCSV();
                    results.add(result);

                    //Save Result
                    saveVehicleResultsFiles(
                            TestType.TRAFFIC_LEVEL_QUEUE,
                            Integer.toString(new Double(trafficRate).intValue()),
                            i + 1, csvResult
                    );
                }
            }
            assert results.size() == TEST_COUNTS;

            //Produce summary results
            summaryResultsCSV.add(
                    String.format(
                            "Results for Traffic Rate: %d",
                            new Double(trafficRate).intValue()
                    )
            );
            summaryResultsCSV.add(CoreMergeResult.produceGlobalStatsCSVHeader());
            for(CoreMergeResult result : results) {
                if(result == null)
                    summaryResultsCSV.add("FAILED TO COMPLETE AFTER " + 5 * TIME_LIMIT + " SECONDS");
                else
                    summaryResultsCSV.add(result.produceGlobalStatsCSV());
            }
            summaryResultsCSV.addAll(produceMeanMergeRow(results,Integer.toString(new Double(trafficRate).intValue())));
            summaryResultsCSV.add("");
            summaryResultsCSV.add("");
        }

        //Save summary results
        summaryResultsCSV.addAll(produceMeanTable(summaryResultsCSV));
        saveGlobalResultsFile(TestType.TRAFFIC_LEVEL_QUEUE, summaryResultsCSV);
    }

    @Test
    public void AimTrafficLevelTests() throws IOException {
        //Parameters
        double speedLimit = STANDARD_SPEED_LIMIT;
        double leadInDistance = STANDARD_LEAD_IN;

        List<String> summaryResultsCSV = new ArrayList<String>();

        //Clear out old results
        if(Files.exists(Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_AIM_PATH_STEM))) {
            Path rootPath = Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_AIM_PATH_STEM);
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.deleteIfExists(Paths.get(RESULTS_STORE_GLOBAL_PATH_STEM + TestType.TRAFFIC_LEVEL_AIM.toString() + ".csv"));

        double[] trafficRates = new double[]{500,1000,1500,2000,2500};
        for (double trafficRate : trafficRates) {
            //Setup
            double trafficLevel = trafficRate/3600;
            List<File> targetSpawnSchedules =
                    getSpawnSchedules(RoadNames.TARGET_ROAD, TestType.TRAFFIC_LEVEL_AIM, trafficRate);
            List<File> mergeSpawnSchedules =
                    getSpawnSchedules(RoadNames.MERGING_ROAD, TestType.TRAFFIC_LEVEL_AIM, trafficRate);

            //Get results
            List<MergeResult> results = new ArrayList<MergeResult>();
            for(int i = 0; i < TEST_COUNTS; i++) {
                File targetSpawnSchedule = targetSpawnSchedules.get(i);
                File mergeSpawnSchedule = mergeSpawnSchedules.get(i);
                MergeMimicSimSetup simSetup = new MergeMimicSimSetup(
                        mergeSpawnSchedule,
                        targetSpawnSchedule,
                        speedLimit,
                        leadInDistance
                );
                AIMSimulator sim = simSetup.getSimulator();
                while(sim.getSimulationTime() < (5*TIME_LIMIT)){
                    sim.step(SimConfig.TIME_STEP);
                }
                if(!sim.getActiveVehicles().isEmpty())
                    results.add(null); //Failed to process all vehicles within 5 * TIME_LIMIT
                else {
                    MergeResult result = sim.produceMergeResult();
                    String csvResult = sim.produceResultsCSV();
                    results.add(result);

                    //Save Result
                    saveVehicleResultsFiles(
                            TestType.TRAFFIC_LEVEL_AIM,
                            Integer.toString(new Double(trafficRate).intValue()),
                            i + 1, csvResult
                    );
                }
            }
            assert results.size() == TEST_COUNTS;

            //Produce summary results
            summaryResultsCSV.add(
                    String.format(
                            "Results for Traffic Rate: %d",
                            new Double(trafficRate).intValue()
                    )
            );
            summaryResultsCSV.add(MergeResult.produceGlobalStatsCSVHeader());
            for(MergeResult result : results) {
                if(result == null)
                    summaryResultsCSV.add("FAILED TO COMPLETE AFTER " + 5 * TIME_LIMIT + " SECONDS");
                else
                    summaryResultsCSV.add(result.produceGlobalStatsCSV());
            }
            summaryResultsCSV.addAll(produceMeanAIMRow(results, Integer.toString(new Double(trafficRate).intValue())));
            summaryResultsCSV.add("");
            summaryResultsCSV.add("");
        }

        //Save summary results
        summaryResultsCSV.addAll(produceMeanTable(summaryResultsCSV));
        saveGlobalResultsFile(TestType.TRAFFIC_LEVEL_AIM, summaryResultsCSV);
    }

    @Test
    public void MergingAngleTests() throws IOException {
        //Parameters
        ProtocolType protocolType = ProtocolType.QUEUE;
        double trafficLevel = STANDARD_TRAFFIC_LEVEL;
        double targetLaneSpeedLimit = STANDARD_SPEED_LIMIT;
        double mergingLaneSpeedLimit = STANDARD_SPEED_LIMIT;
        double targetLeadInDistance = STANDARD_LEAD_IN;
        double targetLeadOutDistance = STANDARD_LEAD_IN;
        double mergeLeadInDistance = STANDARD_LEAD_IN;

        List<String> summaryResultsCSV = new ArrayList<String>();

        //Clear out old results
        if(Files.exists(Paths.get(RESULTS_STORE_MERGE_ANGLE_PATH_STEM))) {
            Path rootPath = Paths.get(RESULTS_STORE_MERGE_ANGLE_PATH_STEM);
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.deleteIfExists(Paths.get(RESULTS_STORE_GLOBAL_PATH_STEM + TestType.MERGE_ANGLE.toString() + ".csv"));

        double[] mergeAngles = new double[]{5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80,85,90};
        for (double mergingAngle : mergeAngles) {
            List<File> targetSpawnSchedules =
                    getSpawnSchedules(RoadNames.TARGET_ROAD, TestType.MERGE_ANGLE, mergingAngle);
            List<File> mergeSpawnSchedules =
                    getSpawnSchedules(RoadNames.MERGING_ROAD, TestType.MERGE_ANGLE, mergingAngle);

            //Get results
            List<CoreMergeResult> results = new ArrayList<CoreMergeResult>();
            for(int i = 0; i < TEST_COUNTS; i++) {
                File targetSpawnSchedule = targetSpawnSchedules.get(i);
                File mergeSpawnSchedule = mergeSpawnSchedules.get(i);
                S2SSimSetup simSetup = new S2SSimSetup(protocolType, trafficLevel,
                        targetLaneSpeedLimit, mergingLaneSpeedLimit,
                        targetLeadInDistance, targetLeadOutDistance,
                        mergeLeadInDistance, mergingAngle,
                        targetSpawnSchedule, mergeSpawnSchedule
                );
                MergeSimulator sim = simSetup.getSimulator();
                while(sim.getSimulationTime() < (5*TIME_LIMIT)){
                    sim.step(SimConfig.TIME_STEP);
                }
                if(!sim.getVinToVehicles().isEmpty())
                    results.add(null); //Failed to process all vehicles within 5 * TIME_LIMIT
                else {
                    CoreMergeResult result = sim.produceMergeResult();
                    String csvResult = sim.produceResultsCSV();
                    results.add(result);

                    //Save Result
                    saveVehicleResultsFiles(
                            TestType.MERGE_ANGLE,
                            Integer.toString(new Double(mergingAngle).intValue()),
                            i + 1, csvResult
                    );
                }
            }
            assert results.size() == TEST_COUNTS;

            //Produce summary results
            summaryResultsCSV.add(
                    String.format(
                            "Results for Merging Angle: %d",
                            new Double(mergingAngle).intValue()
                    )
            );
            summaryResultsCSV.add(CoreMergeResult.produceGlobalStatsCSVHeader());
            for(CoreMergeResult result : results) {
                if(result == null)
                    summaryResultsCSV.add("FAILED TO COMPLETE AFTER " + 5 * TIME_LIMIT + " SECONDS");
                else
                    summaryResultsCSV.add(result.produceGlobalStatsCSV());
            }
            summaryResultsCSV.addAll(produceMeanMergeRow(results, Integer.toString(new Double(mergingAngle).intValue())));
            summaryResultsCSV.add("");
            summaryResultsCSV.add("");
        }

        //Save summary results
        summaryResultsCSV.addAll(produceMeanTable(summaryResultsCSV));
        saveGlobalResultsFile(TestType.MERGE_ANGLE, summaryResultsCSV);
    }

    @Test
    public void SpeedLimitTests() throws IOException {
        //Parameters
        ProtocolType protocolType = ProtocolType.QUEUE;
        double trafficLevel = STANDARD_TRAFFIC_LEVEL;
        double targetLeadInDistance = STANDARD_LEAD_IN;
        double targetLeadOutDistance = STANDARD_LEAD_IN;
        double mergeLeadInDistance = STANDARD_LEAD_IN;
        double mergingAngle = STANDARD_ANGLE;

        List<String> summaryResultsCSV = new ArrayList<String>();

        //Clear out old results
        if(Files.exists(Paths.get(RESULTS_STORE_SPEED_LIMIT_PATH_STEM))) {
            Path rootPath = Paths.get(RESULTS_STORE_SPEED_LIMIT_PATH_STEM);
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.deleteIfExists(Paths.get(RESULTS_STORE_GLOBAL_PATH_STEM + TestType.SPEED_LIMIT + ".csv"));

        double[] targetSpeedLimits = new double[]{10,20,30,40}; //22mph, 45mph, 67mph, 89mph
        double[] mergeSpeedLimits = new double[]{10,20,30,40}; //22mph, 45mph, 67mph, 89mph
        for (double targetSpeedLimit : targetSpeedLimits) {
            for (double mergeSpeedLimit : mergeSpeedLimits) {
                //Setup
                List<File> targetSpawnSchedules =
                        getSpawnSchedules(RoadNames.TARGET_ROAD, TestType.SPEED_LIMIT, targetSpeedLimit);
                List<File> mergeSpawnSchedules =
                        getSpawnSchedules(RoadNames.MERGING_ROAD, TestType.SPEED_LIMIT, mergeSpeedLimit);

                //Get results
                List<CoreMergeResult> results = new ArrayList<CoreMergeResult>();
                for (int i = 0; i < TEST_COUNTS; i++) {
                    File targetSpawnSchedule = targetSpawnSchedules.get(i);
                    File mergeSpawnSchedule = mergeSpawnSchedules.get(i);
                    S2SSimSetup simSetup = new S2SSimSetup(protocolType, trafficLevel,
                            targetSpeedLimit, mergeSpeedLimit,
                            targetLeadInDistance, targetLeadOutDistance,
                            mergeLeadInDistance, mergingAngle,
                            targetSpawnSchedule, mergeSpawnSchedule
                    );
                    MergeSimulator sim = simSetup.getSimulator();
                    while (sim.getSimulationTime() < (5 * TIME_LIMIT)) {
                        sim.step(SimConfig.TIME_STEP);
                    }
                    if (!sim.getVinToVehicles().isEmpty())
                        results.add(null); //Failed to process all vehicles within 5 * TIME_LIMIT
                    else {
                        CoreMergeResult result = sim.produceMergeResult();
                        String csvResult = sim.produceResultsCSV();
                        results.add(result);

                        //Save Result
                        saveVehicleResultsFiles(
                                TestType.SPEED_LIMIT,
                                String.format(
                                        "%d_%d",
                                        new Double(mergeSpeedLimit).intValue(),
                                        new Double(targetSpeedLimit).intValue()
                                ),
                                i + 1,
                                csvResult);
                    }
                }
                assert results.size() == TEST_COUNTS;

                //Produce summary results
                summaryResultsCSV.add(String.format(
                        "Results for Speed Limit: Merge:%d Target:%d",
                        new Double(mergeSpeedLimit).intValue(),
                        new Double(targetSpeedLimit).intValue())
                );
                summaryResultsCSV.add(CoreMergeResult.produceGlobalStatsCSVHeader());
                for (CoreMergeResult result : results) {
                    if (result == null)
                        summaryResultsCSV.add("FAILED TO COMPLETE AFTER " + 5 * TIME_LIMIT + " SECONDS");
                    else
                        summaryResultsCSV.add(result.produceGlobalStatsCSV());
                }
                summaryResultsCSV.addAll(produceMeanMergeRow(results, String.format(
                        "%d,%d",
                        new Double(mergeSpeedLimit).intValue(),
                        new Double(targetSpeedLimit).intValue())
                ));
                summaryResultsCSV.add("");
                summaryResultsCSV.add("");
            }
        }

        //Save summary results
        summaryResultsCSV.addAll(produceMeanTable(summaryResultsCSV));
        saveGlobalResultsFile(TestType.SPEED_LIMIT, summaryResultsCSV);
    }

    @Test
    public void LeadInTests() throws IOException {
        //Parameters
        ProtocolType protocolType = ProtocolType.QUEUE;
        double trafficLevel = STANDARD_TRAFFIC_LEVEL;
        double targetSpeedLimit = STANDARD_SPEED_LIMIT;
        double mergeSpeedLimit = STANDARD_SPEED_LIMIT;
        double targetLeadOutDistance = STANDARD_LEAD_IN;
        double mergingAngle = STANDARD_ANGLE;

        List<String> summaryResultsCSV = new ArrayList<String>();

        //Clear out old results
        if(Files.exists(Paths.get(RESULTS_STORE_LEAD_IN_PATH_STEM))) {
            Path rootPath = Paths.get(RESULTS_STORE_LEAD_IN_PATH_STEM);
            Files.walk(rootPath)
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
        }
        Files.deleteIfExists(Paths.get(RESULTS_STORE_GLOBAL_PATH_STEM + TestType.LEAD_IN + ".csv"));

        double[] targetLeadInDistances = new double[]{100,150,200,250,300};
        double[] mergeLeadInDistances = new double[]{100,150,200,250,300};
        for (double targetLeadInDistance : targetLeadInDistances) {
            for (double mergeLeadInDistance : mergeLeadInDistances) {
                //Setup
                List<File> targetSpawnSchedules =
                        getSpawnSchedules(RoadNames.TARGET_ROAD, TestType.LEAD_IN, targetLeadInDistance);
                List<File> mergeSpawnSchedules =
                        getSpawnSchedules(RoadNames.MERGING_ROAD, TestType.LEAD_IN, mergeLeadInDistance);

                //Get results
                List<CoreMergeResult> results = new ArrayList<CoreMergeResult>();
                for (int i = 0; i < TEST_COUNTS; i++) {
                    File targetSpawnSchedule = targetSpawnSchedules.get(i);
                    File mergeSpawnSchedule = mergeSpawnSchedules.get(i);
                    S2SSimSetup simSetup = new S2SSimSetup(protocolType, trafficLevel,
                            targetSpeedLimit, mergeSpeedLimit,
                            targetLeadInDistance, targetLeadOutDistance,
                            mergeLeadInDistance, mergingAngle,
                            targetSpawnSchedule, mergeSpawnSchedule
                    );
                    MergeSimulator sim = simSetup.getSimulator();
                    while (sim.getSimulationTime() < (5 * TIME_LIMIT)) {
                        sim.step(SimConfig.TIME_STEP);
                    }
                    if (!sim.getVinToVehicles().isEmpty())
                        results.add(null); //Failed to process all vehicles within 5 * TIME_LIMIT
                    else {
                        CoreMergeResult result = sim.produceMergeResult();
                        String csvResult = sim.produceResultsCSV();
                        results.add(result);

                        //Save Result
                        saveVehicleResultsFiles(
                                TestType.LEAD_IN,
                                String.format(
                                        "%d_%d",
                                        new Double(mergeLeadInDistance).intValue(),
                                        new Double(targetLeadInDistance).intValue()
                                ),
                                i + 1,
                                csvResult);
                    }
                }
                assert results.size() == TEST_COUNTS;

                //Produce summary results
                summaryResultsCSV.add(String.format(
                        "Results for Lead In: Merge:%d Target:%d",
                        new Double(mergeLeadInDistance).intValue(),
                        new Double(targetLeadInDistance).intValue())
                );
                summaryResultsCSV.add(CoreMergeResult.produceGlobalStatsCSVHeader());
                for (CoreMergeResult result : results) {
                    if (result == null)
                        summaryResultsCSV.add("FAILED TO COMPLETE AFTER " + 5 * TIME_LIMIT + " SECONDS");
                    else
                        summaryResultsCSV.add(result.produceGlobalStatsCSV());
                }
                summaryResultsCSV.addAll(produceMeanMergeRow(results, String.format(
                        "%d,%d",
                        new Double(mergeLeadInDistance).intValue(),
                        new Double(targetLeadInDistance).intValue())
                ));
                summaryResultsCSV.add("");
                summaryResultsCSV.add("");
            }
        }

        //Save summary results
        summaryResultsCSV.addAll(produceMeanTable(summaryResultsCSV));
        saveGlobalResultsFile(TestType.LEAD_IN, summaryResultsCSV);
    }

    private List<String> produceMeanAIMRow(List<MergeResult> results, String dataTitle) {
        double totalMaxDelay = 0;
        double totalMaxTargetDelay = 0;
        double totalMaxMergeDelay = 0;
        double totalMinDelay = 0;
        double totalMinTargetDelay = 0;
        double totalMinMergeDelay = 0;
        double totalAverageDelay = 0;
        double totalAverageTargetDelay = 0;
        double totalAverageMergeDelay = 0;
        double totalStdDevDelay = 0;
        double totalStdDevTargetDelay = 0;
        double totalStdDevMergeDelay = 0;
        double totalThroughput = 0;
        double totalThroughputTarget = 0;
        double totalThroughputMerge = 0;
        int totalCompletedVehicles = 0;
        int totalCompletedTargetVehicles = 0;
        int totalCompletedMergeVehicles = 0;
        int nonNullResults = 0;
        for(MergeResult result : results) {
            if(result == null)
                continue;
            nonNullResults++;
            totalMaxDelay += result.getMaxDelay();
            totalMaxTargetDelay += result.getMaxTargetDelay();
            totalMaxMergeDelay += result.getMaxMergeDelay();
            totalMinDelay += result.getMinDelay();
            totalMinTargetDelay += result.getMinTargetDelay();
            totalMinMergeDelay += result.getMinMergeDelay();
            totalAverageDelay += result.getAverageDelay();
            totalAverageTargetDelay += result.getAverageTargetDelay();
            totalAverageMergeDelay += result.getAverageMergeDelay();
            totalStdDevDelay += result.getStdDevDelay();
            totalStdDevTargetDelay += result.getStdDevTargetDelay();
            totalStdDevMergeDelay += result.getStdDevMergeDelay();
            totalThroughput += result.getThroughput();
            totalThroughputTarget += result.getThroughputTarget();
            totalThroughputMerge += result.getThroughputMerge();
            totalCompletedVehicles += result.getCompletedVehicles();
            totalCompletedTargetVehicles += result.getCompletedTargetVehicles();
            totalCompletedMergeVehicles += result.getCompletedMergeVehicles();
        }
        if(nonNullResults == 0) {
            List<String> response = new ArrayList<>();
            response.add("All results failed to complete");
            return response;
        }
        double meanMaxDelay = totalMaxDelay / nonNullResults;
        double meanMaxTargetDelay = totalMaxTargetDelay / nonNullResults;
        double meanMaxMergeDelay = totalMaxMergeDelay / nonNullResults;
        double meanMinDelay = totalMinDelay / nonNullResults;
        double meanMinTargetDelay = totalMinTargetDelay / nonNullResults;
        double meanMinMergeDelay = totalMinMergeDelay / nonNullResults;
        double meanAverageDelay = totalAverageDelay / nonNullResults;
        double meanAverageTargetDelay = totalAverageTargetDelay / nonNullResults;
        double meanAverageMergeDelay = totalAverageMergeDelay / nonNullResults;
        double meanStdDevDelay = totalStdDevDelay / nonNullResults;
        double meanStdDevTargetDelay = totalStdDevTargetDelay / nonNullResults;
        double meanStdDevMergeDelay = totalStdDevMergeDelay / nonNullResults;
        double meanThroughput = totalThroughput / nonNullResults;
        double meanThroughputTarget = totalThroughputTarget / nonNullResults;
        double meanThroughputMerge = totalThroughputMerge / nonNullResults;
        double meanCompletedVehicles = totalCompletedVehicles / nonNullResults;
        double meanCompletedTargetVehicles = totalCompletedTargetVehicles / nonNullResults;
        double meanCompletedMergeVehicles = totalCompletedMergeVehicles / nonNullResults;

        StringBuilder sb = new StringBuilder();
        sb.append(meanMaxDelay);
        sb.append(',');
        sb.append(meanMaxTargetDelay);
        sb.append(',');
        sb.append(meanMaxMergeDelay);
        sb.append(',');
        sb.append(meanMinDelay);
        sb.append(',');
        sb.append(meanMinTargetDelay);
        sb.append(',');
        sb.append(meanMinMergeDelay);
        sb.append(',');
        sb.append(meanAverageDelay);
        sb.append(',');
        sb.append(meanAverageTargetDelay);
        sb.append(',');
        sb.append(meanAverageMergeDelay);
        sb.append(',');
        sb.append(meanStdDevDelay);
        sb.append(',');
        sb.append(meanStdDevTargetDelay);
        sb.append(',');
        sb.append(meanStdDevMergeDelay);
        sb.append(',');
        sb.append(meanThroughput);
        sb.append(',');
        sb.append(meanThroughputTarget);
        sb.append(',');
        sb.append(meanThroughputMerge);
        sb.append(',');
        sb.append(meanCompletedVehicles);
        sb.append(',');
        sb.append(meanCompletedTargetVehicles);
        sb.append(',');
        sb.append(meanCompletedMergeVehicles);

        List<String> meanRow = new ArrayList<String>();
        meanRow.add(MEAN_ROW_INDICATOR + "," + dataTitle);
        meanRow.add(sb.toString());
        return meanRow;
    }

    private List<String> produceMeanMergeRow(List<CoreMergeResult> results, String dataTitle) {
        double totalMaxDelay = 0;
        double totalMaxTargetDelay = 0;
        double totalMaxMergeDelay = 0;
        double totalMinDelay = 0;
        double totalMinTargetDelay = 0;
        double totalMinMergeDelay = 0;
        double totalAverageDelay = 0;
        double totalAverageTargetDelay = 0;
        double totalAverageMergeDelay = 0;
        double totalStdDevDelay = 0;
        double totalStdDevTargetDelay = 0;
        double totalStdDevMergeDelay = 0;
        double totalThroughput = 0;
        double totalThroughputTarget = 0;
        double totalThroughputMerge = 0;
        int totalCompletedVehicles = 0;
        int totalCompletedTargetVehicles = 0;
        int totalCompletedMergeVehicles = 0;
        int nonNullResults = 0;
        for(CoreMergeResult result : results) {
            if(result == null)
                continue;
            nonNullResults++;
            totalMaxDelay += result.getMaxDelay();
            totalMaxTargetDelay += result.getMaxTargetDelay();
            totalMaxMergeDelay += result.getMaxMergeDelay();
            totalMinDelay += result.getMinDelay();
            totalMinTargetDelay += result.getMinTargetDelay();
            totalMinMergeDelay += result.getMinMergeDelay();
            totalAverageDelay += result.getAverageDelay();
            totalAverageTargetDelay += result.getAverageTargetDelay();
            totalAverageMergeDelay += result.getAverageMergeDelay();
            totalStdDevDelay += result.getStdDevDelay();
            totalStdDevTargetDelay += result.getStdDevTargetDelay();
            totalStdDevMergeDelay += result.getStdDevMergeDelay();
            totalThroughput += result.getThroughput();
            totalThroughputTarget += result.getThroughputTarget();
            totalThroughputMerge += result.getThroughputMerge();
            totalCompletedVehicles += result.getCompletedVehicles();
            totalCompletedTargetVehicles += result.getCompletedTargetVehicles();
            totalCompletedMergeVehicles += result.getCompletedMergeVehicles();
        }
        if(nonNullResults == 0) {
            List<String> response = new ArrayList<>();
            response.add("All results failed to complete");
            return response;
        }
        double meanMaxDelay = totalMaxDelay / nonNullResults;
        double meanMaxTargetDelay = totalMaxTargetDelay / nonNullResults;
        double meanMaxMergeDelay = totalMaxMergeDelay / nonNullResults;
        double meanMinDelay = totalMinDelay / nonNullResults;
        double meanMinTargetDelay = totalMinTargetDelay / nonNullResults;
        double meanMinMergeDelay = totalMinMergeDelay / nonNullResults;
        double meanAverageDelay = totalAverageDelay / nonNullResults;
        double meanAverageTargetDelay = totalAverageTargetDelay / nonNullResults;
        double meanAverageMergeDelay = totalAverageMergeDelay / nonNullResults;
        double meanStdDevDelay = totalStdDevDelay / nonNullResults;
        double meanStdDevTargetDelay = totalStdDevTargetDelay / nonNullResults;
        double meanStdDevMergeDelay = totalStdDevMergeDelay / nonNullResults;
        double meanThroughput = totalThroughput / nonNullResults;
        double meanThroughputTarget = totalThroughputTarget / nonNullResults;
        double meanThroughputMerge = totalThroughputMerge / nonNullResults;
        double meanCompletedVehicles = totalCompletedVehicles / nonNullResults;
        double meanCompletedTargetVehicles = totalCompletedTargetVehicles / nonNullResults;
        double meanCompletedMergeVehicles = totalCompletedMergeVehicles / nonNullResults;

        StringBuilder sb = new StringBuilder();
        sb.append(meanMaxDelay);
        sb.append(',');
        sb.append(meanMaxTargetDelay);
        sb.append(',');
        sb.append(meanMaxMergeDelay);
        sb.append(',');
        sb.append(meanMinDelay);
        sb.append(',');
        sb.append(meanMinTargetDelay);
        sb.append(',');
        sb.append(meanMinMergeDelay);
        sb.append(',');
        sb.append(meanAverageDelay);
        sb.append(',');
        sb.append(meanAverageTargetDelay);
        sb.append(',');
        sb.append(meanAverageMergeDelay);
        sb.append(',');
        sb.append(meanStdDevDelay);
        sb.append(',');
        sb.append(meanStdDevTargetDelay);
        sb.append(',');
        sb.append(meanStdDevMergeDelay);
        sb.append(',');
        sb.append(meanThroughput);
        sb.append(',');
        sb.append(meanThroughputTarget);
        sb.append(',');
        sb.append(meanThroughputMerge);
        sb.append(',');
        sb.append(meanCompletedVehicles);
        sb.append(',');
        sb.append(meanCompletedTargetVehicles);
        sb.append(',');
        sb.append(meanCompletedMergeVehicles);

        List<String> meanRow = new ArrayList<String>();
        meanRow.add(MEAN_ROW_INDICATOR + "," + dataTitle);
        meanRow.add(sb.toString());
        return meanRow;
    }

    private List<String> produceMeanTable(List<String> summaryTable) {
        List<String> meanTable = new ArrayList<String>();
        meanTable.add("," + CoreMergeResult.produceGlobalStatsCSVHeader());
        for(int i = 0; i < summaryTable.size(); i++) {
            if(summaryTable.get(i).contains(MEAN_ROW_INDICATOR)) {
                String dataTitle = summaryTable.get(i).substring(
                        summaryTable.get(i).indexOf(',') + 1,
                        summaryTable.get(i).length());
                meanTable.add(dataTitle + ',' + summaryTable.get(i + 1));
            }
        }
        return meanTable;
    }

    private List<File> getSpawnSchedules(RoadNames roadName, TestType testType, double setParameter) {
        String roadTypeFirst = roadName.toString().substring(0,1);
        String parameterString = Integer.toString(new Double(setParameter).intValue());

        List<File> schedules = new ArrayList<File>();
        for(int i = 0; i < TEST_COUNTS; i++) {
            String fileName = roadTypeFirst + "_" + parameterString + "_" + Integer.toString(i+1) + ".json";
            String path = "";
            switch(testType) {
                case TRAFFIC_LEVEL_QUEUE:
                    path = TRAFFIC_LEVEL_SCHEDULES_PATH_STEM;
                    path = path.concat(fileName);
                    break;
                case TRAFFIC_LEVEL_AIM:
                    path = TRAFFIC_LEVEL_SCHEDULES_PATH_STEM;
                    path = path.concat(fileName);
                    break;
                case SPEED_LIMIT:
                    path = SPEED_LIMIT_SCHEDULES_PATH_STEM;
                    path = path.concat(fileName);
                    break;
                case MERGE_ANGLE:
                    path = TRAFFIC_LEVEL_SCHEDULES_PATH_STEM;
                    path = path.concat(
                            roadTypeFirst
                                    + "_"
                                    + Integer.toString(new Double(STANDARD_TRAFFIC_RATE).intValue())
                                    + "_"
                                    + Integer.toString(i+1)
                                    + ".json"
                    );
                    break;
                case LEAD_IN:
                    path = TRAFFIC_LEVEL_SCHEDULES_PATH_STEM;
                    path = path.concat(
                            roadTypeFirst
                                    + "_"
                                    + Integer.toString(new Double(STANDARD_TRAFFIC_RATE).intValue())
                                    + "_"
                                    + Integer.toString(i+1)
                                    + ".json"
                    );
                    break;
            }

            File file = new File(path);
            schedules.add(file);
        }

        return schedules;
    }

    private void saveVehicleResultsFiles(TestType testType,
                                         String parameterString, int resultSetNumber,
                                         String resultsCSV) throws IOException {
        //Create directories
        Path trafficLevelsQueueParentDir = Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_QUEUE_PATH_STEM);
        if(!Files.exists(trafficLevelsQueueParentDir))
            Files.createDirectories(trafficLevelsQueueParentDir);
        Path trafficLevelsAIMParentDir = Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_AIM_PATH_STEM);
        if(!Files.exists(trafficLevelsAIMParentDir))
            Files.createDirectories(trafficLevelsAIMParentDir);
        Path speedLimitsParentDir = Paths.get(RESULTS_STORE_SPEED_LIMIT_PATH_STEM);
        if(!Files.exists(speedLimitsParentDir))
            Files.createDirectories(speedLimitsParentDir);
        Path mergeAngleParentDir = Paths.get(RESULTS_STORE_MERGE_ANGLE_PATH_STEM);
        if(!Files.exists(mergeAngleParentDir))
            Files.createDirectories(mergeAngleParentDir);
        Path leadInParentDir = Paths.get(RESULTS_STORE_LEAD_IN_PATH_STEM);
        if(!Files.exists(leadInParentDir))
            Files.createDirectories(leadInParentDir);

        String fileName = testType.toString() + "_" + parameterString + "_" + resultSetNumber + ".csv";
        String path = "";
        switch(testType) {
            case TRAFFIC_LEVEL_QUEUE:
                Path trafficLevelQueueSubDir = Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_QUEUE_PATH_STEM + parameterString + "\\");
                if(!Files.exists(trafficLevelQueueSubDir))
                    Files.createDirectories(trafficLevelQueueSubDir);
                path = RESULTS_STORE_TRAFFIC_LEVEL_QUEUE_PATH_STEM + parameterString + "\\" + fileName;
                break;
            case TRAFFIC_LEVEL_AIM:
                Path trafficLevelAIMSubDir = Paths.get(RESULTS_STORE_TRAFFIC_LEVEL_AIM_PATH_STEM + parameterString + "\\");
                if(!Files.exists(trafficLevelAIMSubDir))
                    Files.createDirectories(trafficLevelAIMSubDir);
                path = RESULTS_STORE_TRAFFIC_LEVEL_AIM_PATH_STEM + parameterString + "\\" + fileName;
                break;
            case SPEED_LIMIT:
                Path speedLimitSubDir = Paths.get(RESULTS_STORE_SPEED_LIMIT_PATH_STEM + parameterString + "\\");
                if(!Files.exists(speedLimitSubDir))
                    Files.createDirectories(speedLimitSubDir);
                path = RESULTS_STORE_SPEED_LIMIT_PATH_STEM + parameterString + "\\" + fileName;
                break;
            case MERGE_ANGLE:
                Path mergeAngleSubDir = Paths.get(RESULTS_STORE_MERGE_ANGLE_PATH_STEM + parameterString + "\\");
                if(!Files.exists(mergeAngleSubDir))
                    Files.createDirectories(mergeAngleSubDir);
                path = RESULTS_STORE_MERGE_ANGLE_PATH_STEM + parameterString + "\\" + fileName;
                break;
            case LEAD_IN:
                Path leadInSubDir = Paths.get(RESULTS_STORE_LEAD_IN_PATH_STEM + parameterString + "\\");
                if(!Files.exists(leadInSubDir))
                    Files.createDirectories(leadInSubDir);
                path = RESULTS_STORE_LEAD_IN_PATH_STEM + parameterString + "\\" + fileName;
                break;
        }

        List<String> writeList = new ArrayList<String>();
        writeList.add(resultsCSV);
        Files.write(Paths.get(path), writeList, Charset.forName("UTF-8"));
    }

    private void saveGlobalResultsFile(TestType testType, List<String> results) throws IOException {
        Path parentDir = Paths.get(RESULTS_STORE_GLOBAL_PATH_STEM);
        if(!Files.exists(parentDir))
            Files.createDirectories(parentDir);

        String path = RESULTS_STORE_GLOBAL_PATH_STEM + testType.toString() + ".csv";

        Files.write(Paths.get(path), results, Charset.forName("UTF-8"));
    }


}
