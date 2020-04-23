package rim;

import aim4.config.SimConfig;
import aim4.gui.setuppanel.RIMSimSetupPanel;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.rim.RimMapUtil;
import aim4.sim.Simulator;
import aim4.sim.results.Result;
import aim4.sim.results.VehicleResult;
import aim4.sim.setup.rim.*;
import aim4.sim.simulator.aim.AIMOptimalSimulator;
import aim4.sim.simulator.rim.AutoDriverOnlySimulator;
import aim4.sim.simulator.rim.RIMOptimalSimulator;
import aim4.util.Util;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.junit.Ignore;
import org.junit.Test;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/*
 Class used to generate the results needed for comparing RIM to AIM.
 */
public class CreateRIMResults {
    private static final int[] VOLUMES = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
    private static final double[] SINGLE_LANE_ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double[] DOUBLE_LANE_ROUNDABOUT_DIAMETER = {50.0, 55.0, 60.0, 65.0};
    private static final double CHOSEN_DIAMETER = 30.0;
    private static final double TIME_LIMIT = 1800;
    private static final double LANE_SPEED_LIMIT = 25.0;
    private static final double SINGLE_LANE_ROUNDABOUT_SPEED_LIMIT = 11.0;
    private static final double DOUBLE_LANE_ROUNDABOUT_SPEED_LIMIT = 13.0;
    private static final double STOP_DISTANCE = 1.0;

    @Ignore
    public void createWorkingSchedule_withTrafficVolumesCsv_savesJsons() {

    	int laneNum = 2;
    	
    	String laneNumString = getLaneNumString(laneNum);
    	double roundaboutSpeedLimit = getRoundaboutSpeedLimit(laneNum);
    	
    	for (int i = 0; i < VOLUMES.length; i++) {
            for (int repetition = 1; repetition <= 10; repetition++) {
		        String trafficLevelVolumeName = 
		        		"traffic_volumes_" + laneNumString + "lane" + Integer.toString(VOLUMES[i]) + ".csv";
		        double timeLimit = TIME_LIMIT;
		        double laneSpeedLimit = LANE_SPEED_LIMIT;
		        JSONArray schedule = RimMapUtil.createWorkingSpawnSchedule(
		                trafficLevelVolumeName,
		                timeLimit,
		                1,
		                1,
		                CHOSEN_DIAMETER,
		                20,
		                4,
		                3.014,
		                laneSpeedLimit,
		                roundaboutSpeedLimit,
		                laneNum,
		                0,
		                0);
		        try {
		            saveJSON(schedule, VOLUMES[i], repetition, laneNum);
		        } catch (IOException ex) {
		            String stackTraceMessage = "";
		            for (StackTraceElement line : ex.getStackTrace())
		                stackTraceMessage += line.toString() + "\n";
		            String errorMessage = String.format(
		                    "Error Occured whilst saving: %s\nStack Trace:\n%s",
		                    ex.getLocalizedMessage(),
		                    stackTraceMessage);
		            throw new RuntimeException(ex);
		        } catch (NullPointerException e1) {
		            String stackTraceMessage = "";
		            for (StackTraceElement line : e1.getStackTrace())
		                stackTraceMessage += line.toString() + "\n";
		            String errorMessage = String.format(
		                    "Error Occured whilst opening csv file: %s\nStack Trace:\n%s",
		                    e1.getLocalizedMessage(),
		                    stackTraceMessage);
		            throw new RuntimeException(e1);
		        }
            }
    	}
    }
    
    
    @Ignore
    public void createRatioTurnBasedSchedule_withTrafficVolumesCsv_savesJsons() {

        for (int i = 0; i < VOLUMES.length; i++) {
            for (int repetition = 1; repetition <= 10; repetition++) {
                String trafficLevelVolumeName = "traffic_volumes" + Integer.toString(VOLUMES[i]) + ".csv";
                double timeLimit = TIME_LIMIT;
                double laneSpeedLimit = LANE_SPEED_LIMIT;
                double roundaboutSpeedLimit = SINGLE_LANE_ROUNDABOUT_SPEED_LIMIT;
                JSONArray schedule = RimMapUtil.createRatioSpawnSchedule(
                        trafficLevelVolumeName,
                        timeLimit,
                        1,
                        1,
                        CHOSEN_DIAMETER,
                        20,
                        4,
                        3.014,
                        laneSpeedLimit,
                        roundaboutSpeedLimit,
                        1,
                        0,
                        0);
                try {
                    saveJSON(schedule, VOLUMES[i], repetition);
                } catch (IOException ex) {
                    String stackTraceMessage = "";
                    for (StackTraceElement line : ex.getStackTrace())
                        stackTraceMessage += line.toString() + "\n";
                    String errorMessage = String.format(
                            "Error Occured whilst saving: %s\nStack Trace:\n%s",
                            ex.getLocalizedMessage(),
                            stackTraceMessage);
                    throw new RuntimeException(ex);
                } catch (NullPointerException e1) {
                    String stackTraceMessage = "";
                    for (StackTraceElement line : e1.getStackTrace())
                        stackTraceMessage += line.toString() + "\n";
                    String errorMessage = String.format(
                            "Error Occured whilst opening csv file: %s\nStack Trace:\n%s",
                            e1.getLocalizedMessage(),
                            stackTraceMessage);
                    throw new RuntimeException(e1);
                }
            }
        }
    }
    
    @Ignore
    public void chooseDiameterSimulations_withTrafficVolumesCsv_savesJCSVs() {
    	
    	int laneNum = 1;
    	
    	String laneNumString = getLaneNumString(laneNum);
    	
    	double roundaboutDiameterArray[] = getRoundaboutDiameterArray(laneNum);
    	double roundaboutSpeedLimit = getRoundaboutSpeedLimit(laneNum);
    	
        //For every roundabout diameter
    	for (int roundaboutDiameterIndex = 0; roundaboutDiameterIndex < roundaboutDiameterArray.length; roundaboutDiameterIndex++) {
            StringBuilder sb = new StringBuilder();
            //Global Stats for the overall csv
            sb.append("Traffic Volume");
            sb.append(',');
            sb.append("Repetition No.");
            sb.append(',');
            sb.append("Average Delay");
            sb.append('\n');
            //For every traffic volume
            for (int trafficVolumeIndex = 0; trafficVolumeIndex < VOLUMES.length; trafficVolumeIndex++) {
                for (int repetition = 1; repetition <= 10; repetition++) {
                	File uploadTrafficSchedule = new File("C:\\Users\\Caleb\\Documents\\Uni\\volumes\\" +
                			laneNumString + "lane_" + Integer.toString(VOLUMES[trafficVolumeIndex]) +
                            "_"+Double.toString(LANE_SPEED_LIMIT)+"ls_10.0rs_1800.0s_unbalanced" + "_" +Integer.toString(repetition)+ ".json");
                    RIMSimSetupPanel rimSimSetupPanel = new RIMSimSetupPanel(new BasicSimSetup(
                            1, // columns
                            1, // rows
                            roundaboutDiameterArray[roundaboutDiameterIndex], // roundabout diameter
                            20.0, // entrance & exit circle radius
                            4, // split factor
                            3.014, // lane width
                            LANE_SPEED_LIMIT, // speed limit
                            roundaboutSpeedLimit, // roundabout speed limit
                            laneNum, // lanes per road
                            1, // median size
                            150, // distance between
                            0.28, // traffic level
                            1.0 // stop distance before intersection
                    ), new aim4.sim.setup.aim.BasicSimSetup(1, // columns
                            1, // rows
                            4, // lane width
                            LANE_SPEED_LIMIT, // speed limit
                            laneNum, // lanes per road
                            1, // median size
                            150, // distance between
                            0.28, // traffic level
                            1.0 // stop distance before intersection
                    ));
                    // Set up RIM Simulator
                    AutoDriverOnlySimSetup rimProtocolSimSetup = new AutoDriverOnlySimSetup(rimSimSetupPanel.getRimSimSetup());
                    rimProtocolSimSetup.setRoundaboutDiameter(roundaboutDiameterArray[roundaboutDiameterIndex]);
                    rimProtocolSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                    rimProtocolSimSetup.setRoundaboutSpeedLimit(roundaboutSpeedLimit);
                    rimProtocolSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
                    rimProtocolSimSetup.setNumOfColumns(1);
                    rimProtocolSimSetup.setNumOfRows(1);
                    rimProtocolSimSetup.setLanesPerRoad(laneNum);
                    rimProtocolSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);

                    // Set up rim Optimal policy Simulator
                    RIMOptimalSimSetup rimOptimalProtocolSimSetup = new RIMOptimalSimSetup(rimSimSetupPanel.getRimSimSetup());
                    rimOptimalProtocolSimSetup.setRoundaboutDiameter(roundaboutDiameterArray[roundaboutDiameterIndex]);
                    rimOptimalProtocolSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                    rimOptimalProtocolSimSetup.setRoundaboutSpeedLimit(roundaboutSpeedLimit);
                    rimOptimalProtocolSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
                    rimOptimalProtocolSimSetup.setNumOfColumns(1);
                    rimOptimalProtocolSimSetup.setNumOfRows(1);
                    rimOptimalProtocolSimSetup.setLanesPerRoad(laneNum);
                    rimOptimalProtocolSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);

                    //Run the rim simulator and store the result
                    Simulator rimSimSetup = rimProtocolSimSetup.getSimulator();
                    while (rimSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                        rimSimSetup.step(SimConfig.TIME_STEP);
                    }
                    Result rimResult = ((AutoDriverOnlySimulator) rimSimSetup).produceResult();
                    int rimNumOfVehiclesWhichCouldNotBeSpawned = ((AutoDriverOnlySimulator) rimSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();
                    int rimNumOfVehiclesSpawned = ((AutoDriverOnlySimulator) rimSimSetup).getNumOfVehiclesSpawned();

                    //Run the rim Optimal simulator and store the results
                    Simulator optimalSimSetup = rimOptimalProtocolSimSetup.getSimulator();
                    while (optimalSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                        optimalSimSetup.step(SimConfig.TIME_STEP);
                    }
                    Result optimalResult = ((RIMOptimalSimulator) optimalSimSetup).produceResult();
                    int rimOptimalNumOfVehiclesWhichCouldNotBeSpawned = ((RIMOptimalSimulator) optimalSimSetup).getNoOfVehiclesWhichCouldNotBeSpawned();
                    int rimOptimalNumOfVehiclesSpawned = ((RIMOptimalSimulator) optimalSimSetup).getNumOfVehiclesSpawned();

                    // Combine the results in one csv
                    Result combinedResult = new Result(null);
                    String combinedCsv = combinedResult.produceRIMVsRIMOptimalCSVString(
                            "RIM",
                            rimResult,
                            rimNumOfVehiclesWhichCouldNotBeSpawned,
                            rimNumOfVehiclesSpawned,
                            "RIM-Optimal",
                            optimalResult,
                            rimOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                            rimOptimalNumOfVehiclesSpawned);
                    List<String> csvResultAsList = new ArrayList<String>();
                    csvResultAsList.add(combinedCsv);

                    //Save
                    final JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(new File("C:\\Users\\Caleb\\Documents\\Uni\\JCSV\\" +
                    		laneNumString + "lane" + 
                    		Integer.toString(VOLUMES[trafficVolumeIndex]) + "veh_lane_hour"
                            + "_" + Double.toString(roundaboutDiameterArray[roundaboutDiameterIndex]) +
                            "m_" + Double.toString(LANE_SPEED_LIMIT) + "ls_" + Double.toString(roundaboutSpeedLimit) + "s_1800.0s_unbalanced" + "_" +
                            Integer.toString(repetition)+ ".csv"));
                    File file = fc.getSelectedFile();
                    try {
                        Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
                    } catch (IOException e1) {
                        //nothing
                    }

                    //Append to final overall csv
                    sb.append(Integer.toString(VOLUMES[trafficVolumeIndex]));
                    sb.append(',');
                    sb.append(Integer.toString(repetition));
                    sb.append(',');
                    sb.append(calculateAverageDelay(rimResult.getVehicleResults(), rimResult, optimalResult));
                    sb.append('\n');
                }
            }
            String finalCSV = sb.toString();
            List<String> csvResultAsList = new ArrayList<String>();
            csvResultAsList.add(finalCSV);

            //Save the overall csv
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("C:\\Users\\Caleb\\Documents\\" +
            		laneNumString + "lane" + 
            		Double.toString(roundaboutDiameterArray[roundaboutDiameterIndex]) +
                    "m_"+Double.toString(LANE_SPEED_LIMIT)+"ls_"+Double.toString(roundaboutSpeedLimit)+"rs_1800.0s_unbalanced" + ".csv"));
            File file = fc.getSelectedFile();
            try {
                Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
            } catch (IOException e1) {
                //nothing
            }

        }

    }

    @Ignore
    public void allSimulationsForChosenDiameter_withTrafficVolumesCsv_savesJCSVs() {
    	
    	int laneNum = 1;
    	double roundaboutSpeedLimit = getRoundaboutSpeedLimit(laneNum);
    	
        StringBuilder sb = new StringBuilder();
        //Global Stats for the overall csv
        sb.append("Traffic Volume");
        sb.append(',');
        sb.append("Repetition");
        sb.append(',');
        sb.append("(RIM vs. RIM-Optimal) Avg. Delay");
        sb.append(',');
        sb.append("(RIM-StopSign vs. RIM-Optimal) Avg. Delay");
        sb.append(',');
        sb.append(',');
        sb.append("(AIMCross vs. AIMCross-Optimal) Avg. Delay");
        sb.append(',');
        sb.append("(AIMCross-StopSign vs. AIMCross-Optimal) Avg. Delay");
        sb.append(',');
        sb.append(',');
        sb.append("RIM Total Vehicles Spawned");
        sb.append(',');
        sb.append("RIM Completed Vehicles");
        sb.append(',');
        sb.append("RIM Remained Vehicles");
        sb.append(',');
        sb.append("RIM-Optimal Total Vehicles Spawned");
        sb.append(',');
        sb.append("RIM-Optimal Completed Vehicles");
        sb.append(',');
        sb.append("RIM-Optimal Remained Vehicles");
        sb.append(',');
        sb.append("RIM-StopSign Total Vehicles Spawned");
        sb.append(',');
        sb.append("RIM-StopSign Completed Vehicles");
        sb.append(',');
        sb.append("RIM-StopSign Remained Vehicles");
        sb.append(',');
        sb.append("AIM Total Vehicles Spawned");
        sb.append(',');
        sb.append("AIM Completed Vehicles");
        sb.append(',');
        sb.append("AIM Remained Vehicles");
        sb.append(',');
        sb.append("AIM-Optimal Total Vehicles Spawned");
        sb.append(',');
        sb.append("AIM-Optimal Completed Vehicles");
        sb.append(',');
        sb.append("AIM-Optimal Remained Vehicles");
        sb.append(',');
        sb.append("AIM-StopSign Total Vehicles Spawned");
        sb.append(',');
        sb.append("AIM-StopSign Completed Vehicles");
        sb.append(',');
        sb.append("AIM-StopSign Remained Vehicles");
        sb.append('\n');
        // For every traffic volume and chosen diameter
        for (int trafficVolumeIndex = 0; trafficVolumeIndex < VOLUMES.length; trafficVolumeIndex++) {
            for (int repetition = 1; repetition <= 10; repetition++) {
                File uploadTrafficSchedule = new File("C:\\Users\\Caleb\\Documents\\volumes\\" +
                        Integer.toString(VOLUMES[trafficVolumeIndex]) +
                        "_" + Double.toString(LANE_SPEED_LIMIT) + "ls_10.0rs_1800.0s_unbalanced_"+Integer.toString(repetition)+".json");
                RIMSimSetupPanel rimSimSetupPanel = new RIMSimSetupPanel(new BasicSimSetup(
                        1, // columns
                        1, // rows
                        CHOSEN_DIAMETER, // roundabout diameter
                        20.0, // entrance & exit circle radius
                        4, // split factor
                        3.014, // lane width
                        LANE_SPEED_LIMIT, // speed limit
                        roundaboutSpeedLimit, // roundabout speed limit
                        1, // lanes per road
                        1, // median size
                        150, // distance between
                        0.28, // traffic level
                        1.0 // stop distance before intersection
                ), new aim4.sim.setup.aim.BasicSimSetup(1, // columns
                        1, // rows
                        4, // lane width
                        LANE_SPEED_LIMIT, // speed limit
                        1, // lanes per road
                        1, // median size
                        150, // distance between
                        0.28, // traffic level
                        1.0 // stop distance before intersection
                ));

                // Set up RIM Simulator
                AutoDriverOnlySimSetup rimProtocolSimSetup = new AutoDriverOnlySimSetup(rimSimSetupPanel.getRimSimSetup());
                rimProtocolSimSetup.setRoundaboutDiameter(CHOSEN_DIAMETER);
                rimProtocolSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                rimProtocolSimSetup.setRoundaboutSpeedLimit(roundaboutSpeedLimit);
                rimProtocolSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
                rimProtocolSimSetup.setNumOfColumns(1);
                rimProtocolSimSetup.setNumOfRows(1);
                rimProtocolSimSetup.setLanesPerRoad(laneNum);
                rimProtocolSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);

                // Set up Optimal policy Simulator
                RIMOptimalSimSetup rimOptimalSimSetup = new RIMOptimalSimSetup(rimSimSetupPanel.getRimSimSetup());
                rimOptimalSimSetup.setRoundaboutDiameter(CHOSEN_DIAMETER);
                rimOptimalSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                rimOptimalSimSetup.setRoundaboutSpeedLimit(roundaboutSpeedLimit);
                rimOptimalSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
                rimOptimalSimSetup.setNumOfColumns(1);
                rimOptimalSimSetup.setNumOfRows(1);
                rimOptimalSimSetup.setLanesPerRoad(laneNum);
                rimOptimalSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);


                // Set up Stop Sign simulator
                AutoDriverOnlySimSetup stopSignSimulator = new AutoDriverOnlySimSetup(rimSimSetupPanel.getRimSimSetup());
                stopSignSimulator.setRoundaboutDiameter(CHOSEN_DIAMETER);
                stopSignSimulator.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                stopSignSimulator.setRoundaboutSpeedLimit(roundaboutSpeedLimit);
                stopSignSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
                stopSignSimulator.setNumOfColumns(1);
                stopSignSimulator.setNumOfRows(1);
                stopSignSimulator.setLanesPerRoad(laneNum);
                stopSignSimulator.setIsStopSignMode(true);
                stopSignSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);

                // Set up AIM Cross Intersection Simulator
                AIMCrossSimSetup aimCrossIntersectionSimulator = new AIMCrossSimSetup(rimSimSetupPanel.getAimSimSetup());
                aimCrossIntersectionSimulator.setSpeedLimit(LANE_SPEED_LIMIT);
                aimCrossIntersectionSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
                aimCrossIntersectionSimulator.setNumOfColumns(1);
                aimCrossIntersectionSimulator.setNumOfRows(1);
                aimCrossIntersectionSimulator.setLanesPerRoad(laneNum);
                aimCrossIntersectionSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);

                // Set up AIM Cross Optimal Intersection Simulator
                AIMCrossOptimalSimSetup aimCrossOptimalIntersectionSimulator = new AIMCrossOptimalSimSetup(rimSimSetupPanel.getAimSimSetup());
                aimCrossOptimalIntersectionSimulator.setSpeedLimit(LANE_SPEED_LIMIT);
                aimCrossOptimalIntersectionSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
                aimCrossOptimalIntersectionSimulator.setNumOfColumns(1);
                aimCrossOptimalIntersectionSimulator.setNumOfRows(1);
                aimCrossOptimalIntersectionSimulator.setLanesPerRoad(laneNum);
                aimCrossOptimalIntersectionSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);

                // Set up AIM Cross Stop Sign Intersection Simulator
                AIMCrossSimSetup aimCrossStopSignIntersectionSimulator = new AIMCrossSimSetup(rimSimSetupPanel.getAimSimSetup());
                aimCrossStopSignIntersectionSimulator.setSpeedLimit(LANE_SPEED_LIMIT);
                aimCrossStopSignIntersectionSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
                aimCrossStopSignIntersectionSimulator.setNumOfColumns(1);
                aimCrossStopSignIntersectionSimulator.setNumOfRows(1);
                aimCrossStopSignIntersectionSimulator.setLanesPerRoad(laneNum);
                aimCrossStopSignIntersectionSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);
                aimCrossStopSignIntersectionSimulator.setIsStopSignMode(true);

                //Run RIM simulator
                Simulator rimSimSetup = rimProtocolSimSetup.getSimulator();
                while (rimSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    rimSimSetup.step(SimConfig.TIME_STEP);
                }
                Result rimResult = ((AutoDriverOnlySimulator) rimSimSetup).produceResult();
                int rimNumOfVehiclesWhichCouldNotBeSpawned = ((AutoDriverOnlySimulator) rimSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();
                int rimNumOfVehiclesSpawned = ((AutoDriverOnlySimulator) rimSimSetup).getNumOfVehiclesSpawned();

                //Run Optimal simulator
                Simulator optimalSimSetup = rimOptimalSimSetup.getSimulator();
                while (optimalSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    optimalSimSetup.step(SimConfig.TIME_STEP);
                }
                Result rimOptimalResult = ((RIMOptimalSimulator) optimalSimSetup).produceResult();
                int rimOptimalNumOfVehiclesWhichCouldNotBeSpawned = ((RIMOptimalSimulator) optimalSimSetup).getNoOfVehiclesWhichCouldNotBeSpawned();
                int rimOptimalNumOfVehiclesSpawned = ((RIMOptimalSimulator) optimalSimSetup).getNumOfVehiclesSpawned();

                //Run Stop Sign simulator
                Simulator stopSignSimSetup = stopSignSimulator.getSimulator();
                while (stopSignSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    stopSignSimSetup.step(SimConfig.TIME_STEP);
                }
                Result rimStopSignResult = ((AutoDriverOnlySimulator) stopSignSimSetup).produceResult();
                int rimStopSignNumOfVehiclesWhichCouldNotBeSpawned = ((AutoDriverOnlySimulator) stopSignSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();
                int rimStopSignNumOfVehiclesSpawned = ((AutoDriverOnlySimulator) stopSignSimSetup).getNumOfVehiclesSpawned();

                //Run AIM Cross simulator
                Simulator aimCrossSimSetup = aimCrossIntersectionSimulator.getSimulator();
                while (aimCrossSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    aimCrossSimSetup.step(SimConfig.TIME_STEP);
                }
                Result aimCrossResult = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossSimSetup).produceResult();
                int aimNumOfVehiclesWhichCouldNotBeSpawned = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();
                int aimNumOfVehiclesSpawned = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossSimSetup).getNumOfVehiclesSpawned();

                //Run AIM Cross optimal simulator
                Simulator aimCrossOptimalSimSetup = aimCrossOptimalIntersectionSimulator.getSimulator();
                while (aimCrossOptimalSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    aimCrossOptimalSimSetup.step(SimConfig.TIME_STEP);
                }
                Result aimCrossOptimalResult = ((AIMOptimalSimulator) aimCrossOptimalSimSetup).produceResult();
                int aimCrossOptimalNumOfVehiclesWhichCouldNotBeSpawned = ((AIMOptimalSimulator) aimCrossOptimalSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();
                int aimCrossOptimalNumOfVehiclesSpawned = ((AIMOptimalSimulator) aimCrossOptimalSimSetup).getNumOfVehiclesSpawned();

                //Run AIM Cross simulator
                Simulator aimCrossStopSignSimSetup = aimCrossStopSignIntersectionSimulator.getSimulator();
                while (aimCrossStopSignSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    aimCrossStopSignSimSetup.step(SimConfig.TIME_STEP);
                }
                Result aimCrossStopSignResult = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossStopSignSimSetup).produceResult();
                int aimStopSignNumOfVehiclesWhichCouldNotBeSpawned = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossStopSignSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();
                int aimStopSignNumOfVehiclesSpawned = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossStopSignSimSetup).getNumOfVehiclesSpawned();


                Result combinedResult = new Result(null);
                String combinedCsv = combinedResult.produceChosenDiameterCSVString(
                        "RIM", rimResult, rimNumOfVehiclesWhichCouldNotBeSpawned, rimNumOfVehiclesSpawned,
                        "RIM-Optimal", rimOptimalResult, rimOptimalNumOfVehiclesWhichCouldNotBeSpawned, rimOptimalNumOfVehiclesSpawned,
                        "RIM-StopSign", rimStopSignResult, rimStopSignNumOfVehiclesWhichCouldNotBeSpawned, rimStopSignNumOfVehiclesSpawned,
                        "AIMCross", aimCrossResult, aimNumOfVehiclesWhichCouldNotBeSpawned, aimNumOfVehiclesSpawned,
                        "AIMCross-Optimal", aimCrossOptimalResult, aimCrossOptimalNumOfVehiclesWhichCouldNotBeSpawned, aimCrossOptimalNumOfVehiclesSpawned,
                        "AIMCross-StopSign", aimCrossStopSignResult, aimStopSignNumOfVehiclesWhichCouldNotBeSpawned, aimStopSignNumOfVehiclesSpawned);
                List<String> csvResultAsList = new ArrayList<String>();
                csvResultAsList.add(combinedCsv);

                //Save
                final JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("C:\\Users\\Caleb\\Documents\\further_entry_point\\" + Integer.toString(VOLUMES[trafficVolumeIndex])
                        + "_" + Double.toString(CHOSEN_DIAMETER) +
                        "m_" + Double.toString(LANE_SPEED_LIMIT) + "ls_" + Double.toString(roundaboutSpeedLimit) + "rs_1800.0s_unbalanced_chosenDiameter_" +
                        Integer.toString(repetition)+ ".csv"));
                File file = fc.getSelectedFile();
                try {
                    Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
                } catch (IOException e1) {
                    //nothing
                }

                //Append to final csv
                sb.append(Integer.toString(VOLUMES[trafficVolumeIndex]));
                sb.append(',');
                sb.append(Integer.toString(repetition));
                sb.append(',');
                sb.append(calculateAverageDelay(rimResult.getVehicleResults(), rimResult, rimOptimalResult));
                sb.append(',');
                sb.append(calculateAverageDelay(rimStopSignResult.getVehicleResults(), rimStopSignResult, rimOptimalResult));
                sb.append(',');
                sb.append(',');
                sb.append(calculateAverageDelay(aimCrossResult.getVehicleResults(), aimCrossResult, aimCrossOptimalResult));
                sb.append(',');
                sb.append(calculateAverageDelay(aimCrossStopSignResult.getVehicleResults(), aimCrossStopSignResult, aimCrossOptimalResult));
                sb.append(',');
                sb.append(rimNumOfVehiclesSpawned);
                sb.append(',');
                sb.append(rimResult.getCompletedVehicles());
                sb.append(',');
                sb.append(rimNumOfVehiclesWhichCouldNotBeSpawned);
                sb.append(',');
                sb.append(rimOptimalNumOfVehiclesSpawned);
                sb.append(',');
                sb.append(rimOptimalResult.getCompletedVehicles());
                sb.append(',');
                sb.append(rimOptimalNumOfVehiclesWhichCouldNotBeSpawned);
                sb.append(',');
                sb.append(rimStopSignNumOfVehiclesSpawned);
                sb.append(',');
                sb.append(rimStopSignResult.getCompletedVehicles());
                sb.append(',');
                sb.append(rimStopSignNumOfVehiclesWhichCouldNotBeSpawned);
                sb.append(',');
                sb.append(aimNumOfVehiclesSpawned);
                sb.append(',');
                sb.append(aimCrossResult.getCompletedVehicles());
                sb.append(',');
                sb.append(aimNumOfVehiclesWhichCouldNotBeSpawned);
                sb.append(',');
                sb.append(aimCrossOptimalNumOfVehiclesSpawned);
                sb.append(',');
                sb.append(aimCrossOptimalResult.getCompletedVehicles());
                sb.append(',');
                sb.append(aimCrossOptimalNumOfVehiclesWhichCouldNotBeSpawned);
                sb.append(',');
                sb.append(aimStopSignNumOfVehiclesSpawned);
                sb.append(',');
                sb.append(aimCrossStopSignResult.getCompletedVehicles());
                sb.append(',');
                sb.append(aimStopSignNumOfVehiclesWhichCouldNotBeSpawned);
                sb.append('\n');
            }
        }
        String finalCSV = sb.toString();
        List<String> csvResultAsList = new ArrayList<String>();
        csvResultAsList.add(finalCSV);

        //Save
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("C:\\Users\\Caleb\\Documents\\further_entry_point\\" + Double.toString(CHOSEN_DIAMETER) +
                "m_"+Double.toString(LANE_SPEED_LIMIT)+"ls_"+Double.toString(roundaboutSpeedLimit)+"rs_1800.0s_unbalanced_chosen_diameter_10repetitions" + ".csv"));
        File file = fc.getSelectedFile();
        try {
            Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
        } catch (IOException e1) {
            //nothing
        }

    }
    
    @Test
    public void QueuedVehicles() {
    	
		  int laneNum = 2;
		  String laneNumString = getLaneNumString(laneNum);
		  double roundaboutSpeedLimit = getRoundaboutSpeedLimit(laneNum);
		  double[] roundaboutDiameterArray = getRoundaboutDiameterArray(laneNum);
		  int roundaboutDiameterIndex = 3;
		
		  StringBuilder sb = new StringBuilder();
		  sb = appendHeaders(sb);
      
		  int[] completedArray = new int[10];
		  int completed = 0;
		  int[] remainArray = new int[10];
		  int remain = 0;
		  
		  int trafficVolumeIndex = 0;
		  while (trafficVolumeIndex < VOLUMES.length) {
	          for (int repetition = 1; repetition <= 10; repetition++) {		        	  
	        	  String resultsCSV =  laneNumString + "lane" + Integer.toString(VOLUMES[trafficVolumeIndex]) + "veh_lane_hour_" +
	      			Double.toString(roundaboutDiameterArray[roundaboutDiameterIndex]) + "m_" + Double.toString(LANE_SPEED_LIMIT)+"ls_" + 
	      			Double.toString(roundaboutSpeedLimit) + "s_1800.0s_unbalanced" + "_" +Integer.toString(repetition)+ ".csv";
	          
	          List<String> strs = null;
	          try {
	  			strs = Util.readFileToStrArray(resultsCSV);
	  		} catch (IOException e) {
	      			// TODO Auto-generated catch block
	      			e.printStackTrace();
	  		}
	          
	          if (strs != null) {
	        	  String[] tokens = strs.get(2).split(",");
	        	  int rimCompleted = (int) Double.parseDouble(tokens[1]);
	        	  completed += rimCompleted;
	        	  int rimRemain = (int) Double.parseDouble(tokens[2]);
	        	  int optimalRemain = (int) Double.parseDouble(tokens[6]);
	        	  int actualRemain = rimRemain - optimalRemain;
	        	  remain += actualRemain;
	          
	    	      sb = appendRow(
	    	    		  sb, 
	    	    		  (int) roundaboutDiameterArray[roundaboutDiameterIndex], 
	    	    		  VOLUMES[trafficVolumeIndex], 
	    	    		  repetition, 
	    	    		  rimCompleted, 
	    	    		  actualRemain
		    		  );
	              }
	          }
	          
        		  completedArray[trafficVolumeIndex] = completed / 10;
        		  completed = 0;
	    		  remainArray[trafficVolumeIndex] = remain / 10;
	    		  remain = 0;
	    		  trafficVolumeIndex += 1;
	      }
		  
		  if (completed != 0) {
    		  completedArray[9] = completed / 10;
    		  completed = 0;
    	  }
		  if (remain != 0) {
    		  remainArray[9] = remain / 10;
    		  remain = 0;
    	  }
		  
		  sb = appendFinalValues(sb, "Completed Values", completedArray);
		  sb = appendFinalValues(sb, "Remaining Values", remainArray);
		  
		  String CSVString = sb.toString();
	      
	      List<String> csvResultAsList = new ArrayList<String>();
	      csvResultAsList.add(CSVString);
	      
	      final JFileChooser fc = new JFileChooser();
	      fc.setSelectedFile(new File("C:\\Users\\Caleb\\Documents\\Uni\\JCSV\\Stats\\" + laneNumString + roundaboutDiameterArray[roundaboutDiameterIndex] + ".csv"));
	      File file = fc.getSelectedFile();
	      try {
	          Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
	      } catch (IOException e1) {
	          //nothing
    	      }
    }
    
    private StringBuilder appendHeaders(StringBuilder sb) {
        //Global Stats
        sb.append("Diameter");
        sb.append(',');
        sb.append("TrafficVolume");
        sb.append(',');
        sb.append("Repetition");
        sb.append(',');
        sb.append("Completed");
        sb.append(',');
        sb.append("Actual queue");
        sb.append('\n');
        return sb;
    }
    
    private StringBuilder appendRow(StringBuilder sb, int diameter, int volume, int repetition, int completed, int queue) {
        //Global Stats
        sb.append(diameter);
        sb.append(',');
        sb.append(volume);
        sb.append(',');
        sb.append(volume);
        sb.append(',');
        sb.append(completed);
        sb.append(',');
        sb.append(queue);
        sb.append('\n');
        return sb;
    }
    
    private StringBuilder appendFinalValues(StringBuilder sb, String header, int[] completedArray) {
    	sb.append('\n');
    	sb.append(header);
    	sb.append('\n');
    	for (int i = 0; i < completedArray.length; i++) {
    		sb.append(completedArray[i]);
    		sb.append(',');
    	}
    	return sb;
    }
    
    private void saveJSON(JSONArray schedule, int trafficVolume, int repetition, int laneNum) throws IOException {
    	String laneNumString = getLaneNumString(laneNum);
    	double roundaboutSpeedLimit = getRoundaboutSpeedLimit(laneNum);
    	
        final JFileChooser fc = new JFileChooser();
        String pathExtension = "C:\\Users\\Caleb\\Documents\\Uni\\volumes\\"
        		+ laneNumString + "lane_" + Integer.toString(trafficVolume) +
                "_" + Double.toString(LANE_SPEED_LIMIT) + "ls_"
                + Double.toString(roundaboutSpeedLimit) + "rs_" + Double.toString(TIME_LIMIT) + "s_unbalanced_" + Integer.toString(repetition);
        String jsonString = schedule.toJSONString();
        List<String> writeList = new ArrayList<String>();
        writeList.add(jsonString);
        String path = pathExtension;
        if (pathExtension.endsWith(".json")) {
            int lastIndex = path.lastIndexOf(".json");
            if (lastIndex > -1)
                path = path.substring(0, lastIndex) + ".json";
        } else {
            path = path.substring(0, path.length()) + ".json";
        }
        Files.write(Paths.get(path), writeList, Charset.forName("UTF-8"));
    }
    
    private void saveJSON(JSONArray schedule, int trafficVolume, int repetition) throws IOException {
    	saveJSON(schedule, trafficVolume, repetition, 1);
    }
    
    private String getLaneNumString(int laneNum) {
    	if (laneNum == 1) {
    		return "single";
    	}
    	else if (laneNum == 2) {
    		return "double";
    	}
    	else {
    		return "";
    	}
    }
    
    private double getRoundaboutSpeedLimit(int laneNum) {
    	return laneNum == 1 ? SINGLE_LANE_ROUNDABOUT_SPEED_LIMIT : DOUBLE_LANE_ROUNDABOUT_SPEED_LIMIT;
    }
    
    private double[] getRoundaboutDiameterArray(int laneNum) {
    	return laneNum == 1 ? SINGLE_LANE_ROUNDABOUT_DIAMETER : DOUBLE_LANE_ROUNDABOUT_DIAMETER;
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
}
