package aim4.rim;

import aim4.config.SimConfig;
import aim4.gui.setuppanel.RIMSimSetupPanel;
import aim4.map.rim.RimMapUtil;
import aim4.sim.Simulator;
import aim4.sim.results.Result;
import aim4.sim.results.VehicleResult;
import aim4.sim.setup.rim.*;
import aim4.sim.simulator.aim.AIMOptimalSimulator;
import aim4.sim.simulator.rim.AutoDriverOnlySimulator;
import aim4.sim.simulator.rim.RIMOptimalSimulator;
import aim4.util.Util;
import org.json.simple.JSONArray;
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

/*
 Class used to generate the results needed for comparing RIM to AIM.
 */
public class CreateRIMResults {
    private static final int[] VOLUMES = {100, 200, 300, 400, 500, 600, 700, 800, 900, 1000};
    private static final double[] ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double CHOSEN_DIAMETER = 30.0;
    private static final double TIME_LIMIT = 1800;
    private static final double LANE_SPEED_LIMIT = 25.0;
    private static final double ROUNDABOUT_SPEED_LIMIT = 10.0;
    private static final double STOP_DISTANCE = 1.0;

    @Ignore
    public void createRatioTurnBasedSchedule_withTrafficVolumesCsv_savesJsons() {

        for (int i = 0; i < 10; i++) {
            String trafficLevelVolumeName = "traffic_volumes" + Integer.toString(VOLUMES[i]) + ".csv";
            double timeLimit = TIME_LIMIT;
            double laneSpeedLimit = LANE_SPEED_LIMIT;
            double roundaboutSpeedLimit = ROUNDABOUT_SPEED_LIMIT;
            for (int j = 0; j < 4; j++) {
                double roundaboutDiameter = ROUNDABOUT_DIAMETER[j];
                JSONArray schedule = RimMapUtil.createRatioSpawnSchedule(
                        trafficLevelVolumeName,
                        timeLimit,
                        1,
                        1,
                        roundaboutDiameter,
                        20,
                        4,
                        3.014,
                        laneSpeedLimit,
                        roundaboutSpeedLimit,
                        1,
                        0,
                        0);
                try {
                    saveJSON(schedule, VOLUMES[i]);
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
        //For every roundabout diameter
        for (int roundaboutDiameterIndex = 0; roundaboutDiameterIndex < 4; roundaboutDiameterIndex++) {
            StringBuilder sb = new StringBuilder();
            //Global Stats for the overall csv
            sb.append("Traffic Volume");
            sb.append(',');
            sb.append("Average Delay");
            sb.append('\n');
            //For every traffic volume
            for (int trafficVolumeIndex = 0; trafficVolumeIndex < 10; trafficVolumeIndex++) {
                File uploadTrafficSchedule = new File("C:\\Users\\dydi_\\Documents\\" +
                        Integer.toString(VOLUMES[trafficVolumeIndex]) +
                        "_15.0ls_10.0rs_1800.0s_unbalanced.json");
                RIMSimSetupPanel rimSimSetupPanel = new RIMSimSetupPanel(new BasicSimSetup(
                        1, // columns
                        1, // rows
                        ROUNDABOUT_DIAMETER[roundaboutDiameterIndex], // roundabout diameter
                        20.0, // entrance & exit circle radius
                        4, // split factor
                        3.014, // lane width
                        LANE_SPEED_LIMIT, // speed limit
                        ROUNDABOUT_SPEED_LIMIT, // roundabout speed limit
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
                rimProtocolSimSetup.setRoundaboutDiameter(ROUNDABOUT_DIAMETER[roundaboutDiameterIndex]);
                rimProtocolSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                rimProtocolSimSetup.setRoundaboutSpeedLimit(ROUNDABOUT_SPEED_LIMIT);
                rimProtocolSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
                rimProtocolSimSetup.setNumOfColumns(1);
                rimProtocolSimSetup.setNumOfRows(1);
                rimProtocolSimSetup.setLanesPerRoad(1);
                rimProtocolSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);

                // Set up rim Optimal policy Simulator
                RIMOptimalSimSetup rimOptimalProtocolSimSetup = new RIMOptimalSimSetup(rimSimSetupPanel.getRimSimSetup());
                rimOptimalProtocolSimSetup.setRoundaboutDiameter(ROUNDABOUT_DIAMETER[roundaboutDiameterIndex]);
                rimOptimalProtocolSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
                rimOptimalProtocolSimSetup.setRoundaboutSpeedLimit(ROUNDABOUT_SPEED_LIMIT);
                rimOptimalProtocolSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
                rimOptimalProtocolSimSetup.setNumOfColumns(1);
                rimOptimalProtocolSimSetup.setNumOfRows(1);
                rimOptimalProtocolSimSetup.setLanesPerRoad(1);
                rimOptimalProtocolSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);

                //Run the rim simulator and store the result
                Simulator rimSimSetup = rimProtocolSimSetup.getSimulator();
                while (rimSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    rimSimSetup.step(SimConfig.TIME_STEP);
                }
                Result rimResult = ((AutoDriverOnlySimulator) rimSimSetup).produceResult();

                //Run the rim Optimal simulator and store the results
                Simulator optimalSimSetup = rimOptimalProtocolSimSetup.getSimulator();
                while (optimalSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                    optimalSimSetup.step(SimConfig.TIME_STEP);
                }
                Result optimalResult = ((RIMOptimalSimulator) optimalSimSetup).produceResult();

                // Combine the results in one csv
                Result combinedResult = new Result(null);
                String combinedCsv = combinedResult.produceRIMVsRIMOptimalCSVString("RIM", rimResult, "RIM-Optimal", optimalResult);
                List<String> csvResultAsList = new ArrayList<String>();
                csvResultAsList.add(combinedCsv);

                //Save
                final JFileChooser fc = new JFileChooser();
                fc.setSelectedFile(new File("C:\\Users\\dydi_\\Documents\\" + Integer.toString(VOLUMES[trafficVolumeIndex])
                        + "_" + Double.toString(ROUNDABOUT_DIAMETER[roundaboutDiameterIndex]) +
                        "m_"+Double.toString(LANE_SPEED_LIMIT)+"ls_"+Double.toString(ROUNDABOUT_SPEED_LIMIT)+"s_1800.0s_unbalanced" + ".csv"));
                File file = fc.getSelectedFile();
                try {
                    Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
                } catch (IOException e1) {
                    //nothing
                }

                //Append to final overall csv
                sb.append(Integer.toString(VOLUMES[trafficVolumeIndex]));
                sb.append(',');
                sb.append(calculateAverageDelay(rimResult.getVehicleResults(),rimResult, optimalResult));
                sb.append('\n');
            }
            String finalCSV = sb.toString();
            List<String> csvResultAsList = new ArrayList<String>();
            csvResultAsList.add(finalCSV);

            //Save the overall csv
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("C:\\Users\\dydi_\\Documents\\" + Double.toString(ROUNDABOUT_DIAMETER[roundaboutDiameterIndex]) +
                    "m_"+Double.toString(LANE_SPEED_LIMIT)+"ls_"+Double.toString(ROUNDABOUT_SPEED_LIMIT)+"rs_1800.0s_unbalanced" + ".csv"));
            File file = fc.getSelectedFile();
            try {
                Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
            } catch (IOException e1) {
                //nothing
            }

        }

    }

    @Test
    public void allSimulationsForChosenDiameter_withTrafficVolumesCsv_savesJCSVs() {
        StringBuilder sb = new StringBuilder();
        //Global Stats for the overall csv
        sb.append("Traffic Volume");
        sb.append(',');
        sb.append("(RIM vs. RIM-Optimal) Avg. Delay");
        sb.append(',');
        sb.append("(RIM-StopSign vs. RIM-Optimal) Avg. Delay");
        sb.append(',');
        sb.append(',');
        sb.append("(AIMCross vs. AIMCross-Optimal) Avg. Delay");
        sb.append(',');
        sb.append("(AIMCross-StopSign vs. AIMCross-Optimal) Avg. Delay");
        sb.append('\n');
        // For every traffic volume and chosen diameter
        for (int trafficVolumeIndex = 0; trafficVolumeIndex < 10; trafficVolumeIndex++) {
            File uploadTrafficSchedule = new File("C:\\Users\\dydi_\\Documents\\" +
                    Integer.toString(VOLUMES[trafficVolumeIndex]) +
                    "_15.0ls_10.0rs_1800.0s_unbalanced.json");
            RIMSimSetupPanel rimSimSetupPanel = new RIMSimSetupPanel(new BasicSimSetup(
                    1, // columns
                    1, // rows
                    CHOSEN_DIAMETER, // roundabout diameter
                    20.0, // entrance & exit circle radius
                    4, // split factor
                    3.014, // lane width
                    LANE_SPEED_LIMIT, // speed limit
                    ROUNDABOUT_SPEED_LIMIT, // roundabout speed limit
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
            rimProtocolSimSetup.setRoundaboutSpeedLimit(ROUNDABOUT_SPEED_LIMIT);
//            rimProtocolSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
            rimProtocolSimSetup.setNumOfColumns(1);
            rimProtocolSimSetup.setNumOfRows(1);
            rimProtocolSimSetup.setLanesPerRoad(1);
            rimProtocolSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);

            // Set up Optimal policy Simulator
            RIMOptimalSimSetup rimOptimalSimSetup = new RIMOptimalSimSetup(rimSimSetupPanel.getRimSimSetup());
            rimOptimalSimSetup.setRoundaboutDiameter(CHOSEN_DIAMETER);
            rimOptimalSimSetup.setLaneSpeedLimit(LANE_SPEED_LIMIT);
            rimOptimalSimSetup.setRoundaboutSpeedLimit(ROUNDABOUT_SPEED_LIMIT);
//            rimOptimalSimSetup.setStopDistBeforeIntersection(STOP_DISTANCE);
            rimOptimalSimSetup.setNumOfColumns(1);
            rimOptimalSimSetup.setNumOfRows(1);
            rimOptimalSimSetup.setLanesPerRoad(1);
            rimOptimalSimSetup.setUploadTrafficSchedule(uploadTrafficSchedule);


            // Set up Stop Sign simulator
            AutoDriverOnlySimSetup stopSignSimulator = new AutoDriverOnlySimSetup(rimSimSetupPanel.getRimSimSetup());
            stopSignSimulator.setRoundaboutDiameter(CHOSEN_DIAMETER);
            stopSignSimulator.setLaneSpeedLimit(LANE_SPEED_LIMIT);
            stopSignSimulator.setRoundaboutSpeedLimit(ROUNDABOUT_SPEED_LIMIT);
//            stopSignSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
            stopSignSimulator.setNumOfColumns(1);
            stopSignSimulator.setNumOfRows(1);
            stopSignSimulator.setLanesPerRoad(1);
            stopSignSimulator.setIsStopSignMode(true);
            stopSignSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);

            // Set up AIM Cross Intersection Simulator
            AIMCrossSimSetup aimCrossIntersectionSimulator = new AIMCrossSimSetup(rimSimSetupPanel.getAimSimSetup());
            aimCrossIntersectionSimulator.setSpeedLimit(LANE_SPEED_LIMIT);
            aimCrossIntersectionSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
            aimCrossIntersectionSimulator.setNumOfColumns(1);
            aimCrossIntersectionSimulator.setNumOfRows(1);
            aimCrossIntersectionSimulator.setLanesPerRoad(1);
            aimCrossIntersectionSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);

            // Set up AIM Cross Optimal Intersection Simulator
            AIMCrossOptimalSimSetup aimCrossOptimalIntersectionSimulator = new AIMCrossOptimalSimSetup(rimSimSetupPanel.getAimSimSetup());
            aimCrossOptimalIntersectionSimulator.setSpeedLimit(LANE_SPEED_LIMIT);
            aimCrossOptimalIntersectionSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
            aimCrossOptimalIntersectionSimulator.setNumOfColumns(1);
            aimCrossOptimalIntersectionSimulator.setNumOfRows(1);
            aimCrossOptimalIntersectionSimulator.setLanesPerRoad(1);
            aimCrossOptimalIntersectionSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);

            // Set up AIM Cross Stop Sign Intersection Simulator
            AIMCrossSimSetup aimCrossStopSignIntersectionSimulator = new AIMCrossSimSetup(rimSimSetupPanel.getAimSimSetup());
            aimCrossStopSignIntersectionSimulator.setSpeedLimit(LANE_SPEED_LIMIT);
            aimCrossStopSignIntersectionSimulator.setStopDistBeforeIntersection(STOP_DISTANCE);
            aimCrossStopSignIntersectionSimulator.setNumOfColumns(1);
            aimCrossStopSignIntersectionSimulator.setNumOfRows(1);
            aimCrossStopSignIntersectionSimulator.setLanesPerRoad(1);
            aimCrossStopSignIntersectionSimulator.setUploadTrafficSchedule(uploadTrafficSchedule);
            aimCrossStopSignIntersectionSimulator.setIsStopSignMode(true);

            //Run RIM simulator
            Simulator rimSimSetup = rimProtocolSimSetup.getSimulator();
            while (rimSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                rimSimSetup.step(SimConfig.TIME_STEP);
            }
            Result rimResult = ((AutoDriverOnlySimulator) rimSimSetup).produceResult();
            int rimNumOfVehiclesWhichCouldNotBeSpawned = ((AutoDriverOnlySimulator) rimSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();

            //Run Optimal simulator
            Simulator optimalSimSetup = rimOptimalSimSetup.getSimulator();
            while (optimalSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                optimalSimSetup.step(SimConfig.TIME_STEP);
            }
            Result rimOptimalResult = ((RIMOptimalSimulator) optimalSimSetup).produceResult();
            int rimOptimalNumOfVehiclesWhichCouldNotBeSpawned = ((RIMOptimalSimulator) optimalSimSetup).getNoOfVehiclesWhichCouldNotBeSpawned();

            //Run Stop Sign simulator
            Simulator stopSignSimSetup = stopSignSimulator.getSimulator();
            while (stopSignSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                stopSignSimSetup.step(SimConfig.TIME_STEP);
            }
            Result rimStopSignResult = ((AutoDriverOnlySimulator) stopSignSimSetup).produceResult();
            int rimStopSignNumOfVehiclesWhichCouldNotBeSpawned = ((AutoDriverOnlySimulator) stopSignSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();

            //Run AIM Cross simulator
            Simulator aimCrossSimSetup = aimCrossIntersectionSimulator.getSimulator();
            while (aimCrossSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                aimCrossSimSetup.step(SimConfig.TIME_STEP);
            }
            Result aimCrossResult = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossSimSetup).produceResult();
            int aimNumOfVehiclesWhichCouldNotBeSpawned = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();

            //Run AIM Cross optimal simulator
            Simulator aimCrossOptimalSimSetup = aimCrossOptimalIntersectionSimulator.getSimulator();
            while (aimCrossOptimalSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                aimCrossOptimalSimSetup.step(SimConfig.TIME_STEP);
            }
            Result aimCrossOptimalResult = ((AIMOptimalSimulator) aimCrossOptimalSimSetup).produceResult();
            int aimCrossOptimalNumOfVehiclesWhichCouldNotBeSpawned = ((AIMOptimalSimulator) aimCrossOptimalSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();

            //Run AIM Cross simulator
            Simulator aimCrossStopSignSimSetup = aimCrossStopSignIntersectionSimulator.getSimulator();
            while (aimCrossStopSignSimSetup.getSimulationTime() < (TIME_LIMIT)) {
                aimCrossStopSignSimSetup.step(SimConfig.TIME_STEP);
            }
            Result aimCrossStopSignResult = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossStopSignSimSetup).produceResult();
            int aimStopSignNumOfVehiclesWhichCouldNotBeSpawned = ((aim4.sim.simulator.aim.AutoDriverOnlySimulator) aimCrossStopSignSimSetup).getNumOfVehiclesWhichCouldNotBeSpawned();



            Result combinedResult = new Result(null);
            String combinedCsv = combinedResult.produceChosenDiameterCSVString(
                    "RIM", rimResult, rimNumOfVehiclesWhichCouldNotBeSpawned,
                    "RIM-Optimal", rimOptimalResult, rimOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                    "RIM-StopSign", rimStopSignResult, rimStopSignNumOfVehiclesWhichCouldNotBeSpawned,
                    "AIMCross", aimCrossResult, aimNumOfVehiclesWhichCouldNotBeSpawned,
                    "AIMCross-Optimal", aimCrossOptimalResult, aimCrossOptimalNumOfVehiclesWhichCouldNotBeSpawned,
                    "AIMCross-StopSign", aimCrossStopSignResult, aimStopSignNumOfVehiclesWhichCouldNotBeSpawned);
            List<String> csvResultAsList = new ArrayList<String>();
            csvResultAsList.add(combinedCsv);

            //Save
            final JFileChooser fc = new JFileChooser();
            fc.setSelectedFile(new File("C:\\Users\\dydi_\\Documents\\" + Integer.toString(VOLUMES[trafficVolumeIndex])
                    + "_" + Double.toString(CHOSEN_DIAMETER) +
                    "m_"+Double.toString(LANE_SPEED_LIMIT)+"ls_"+Double.toString(ROUNDABOUT_SPEED_LIMIT)+"rs_1800.0s_unbalanced_chosenDiameter" + ".csv"));
            File file = fc.getSelectedFile();
            try {
                Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
            } catch (IOException e1) {
                //nothing
            }

            //Append to final csv
            sb.append(Integer.toString(VOLUMES[trafficVolumeIndex]));
            sb.append(',');
            sb.append(calculateAverageDelay(rimResult.getVehicleResults(),rimResult, rimOptimalResult));
            sb.append(',');
            sb.append(calculateAverageDelay(rimStopSignResult.getVehicleResults(),rimStopSignResult, rimOptimalResult));
            sb.append(',');
            sb.append(',');
            sb.append(calculateAverageDelay(aimCrossResult.getVehicleResults(),aimCrossResult, aimCrossOptimalResult));
            sb.append(',');
            sb.append(calculateAverageDelay(aimCrossStopSignResult.getVehicleResults(),aimCrossStopSignResult, aimCrossOptimalResult));
            sb.append('\n');
        }
        String finalCSV = sb.toString();
        List<String> csvResultAsList = new ArrayList<String>();
        csvResultAsList.add(finalCSV);

        //Save
        final JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new File("C:\\Users\\dydi_\\Documents\\" + Double.toString(CHOSEN_DIAMETER) +
                "m_"+Double.toString(LANE_SPEED_LIMIT)+"ls_"+Double.toString(ROUNDABOUT_SPEED_LIMIT)+"rs_1800.0s_unbalanced_chosen_diameter" + ".csv"));
        File file = fc.getSelectedFile();
        try {
            Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
        } catch (IOException e1) {
            //nothing
        }


    }

    private void saveJSON(JSONArray schedule, int trafficVolume) throws IOException {
        final JFileChooser fc = new JFileChooser();
        String pathExtension = "C:\\Users\\dydi_\\Documents\\" + Integer.toString(trafficVolume) +
                "_" + Double.toString(LANE_SPEED_LIMIT) + "ls_"
                + Double.toString(ROUNDABOUT_SPEED_LIMIT) + "rs_" + Double.toString(TIME_LIMIT) + "s_unbalanced";
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
