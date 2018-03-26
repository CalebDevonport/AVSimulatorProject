package aim4.map.rim;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.rim.RIMAutoDriver;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.im.rim.v2i.RequestHandler.FCFSRequestHandler;
import aim4.im.rim.v2i.V2IManager;
import aim4.im.rim.v2i.policy.BasePolicy;
import aim4.im.rim.v2i.reservation.ReservationGridManager;
import aim4.map.Road;
import aim4.map.rim.destination.*;
import aim4.sim.simulator.rim.helper.SensorInputHelper;
import aim4.sim.simulator.rim.helper.SpawnHelper;
import aim4.util.Util;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.rim.RIMVehicleSimModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * The utility class for RimIntersectionMap.
 */
public class RimMapUtil {
    // Set rim Managers

    /**
     * Set the FCFS managers at all intersections.
     *
     * @param layout       the map
     * @param currentTime  the current time
     * @param config       the reservation grid manager configuration
     */
    public static void setFCFSManagers(RimIntersectionMap layout,
                                       double currentTime,
                                       ReservationGridManager.Config config) {
        layout.removeAllManagers();
        for(int column = 0; column < layout.getColumns(); column++) {
            for(int row = 0; row < layout.getRows(); row++) {
                List<Road> roads = layout.getRoads(column, row);
                RoadBasedIntersection intersection = new RoadBasedIntersection(roads);
                RoadBasedTrackModel trajectoryModel =
                        new RoadBasedTrackModel(intersection);
                V2IManager im =
                        new V2IManager(intersection, trajectoryModel, currentTime,
                                config, layout.getImRegistry());
                im.setPolicy(new BasePolicy(im, new FCFSRequestHandler()));
                layout.setManager(column, row, im);
            }
        }
    }

    /**
     * The uniform distributed spawn spec generator.
     */
    public static class UniformSpawnSpecGenerator implements RIMSpawnPoint.RIMSpawnSpecGenerator {
        /** The proportion of each spec */
        private List<Double> proportion;
        /** The destination selector */
        private DestinationSelector destinationSelector;
        /** probability of generating a vehicle in each spawn time step */
        private double prob;

        /**
         * Create an uniform spawn specification generator.
         *
         * @param trafficLevel         the traffic level
         * @param destinationSelector  the destination selector
         */
        public UniformSpawnSpecGenerator(double trafficLevel,
                                         DestinationSelector destinationSelector) {
            int n = VehicleSpecDatabase.getNumOfSpec();
            proportion = new ArrayList<Double>(n);
            double p = 1.0 / n;
            for(int i=0; i<n; i++) {
                proportion.add(p);
            }
            this.destinationSelector = destinationSelector;

            prob = trafficLevel * SimConfig.SPAWN_TIME_STEP;
            // Cannot generate more than one vehicle in each spawn time step
            assert prob <= 1.0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<RIMSpawnPoint.RIMSpawnSpec> act(RIMSpawnPoint spawnPoint, double timeStep) {
            List<RIMSpawnPoint.RIMSpawnSpec> result = new LinkedList<RIMSpawnPoint.RIMSpawnSpec>();

            double initTime = spawnPoint.getCurrentTime();
            for(double time = initTime; time < initTime + timeStep;
                time += SimConfig.SPAWN_TIME_STEP) {
                if (Util.random.nextDouble() < prob) {
                    int i = Util.randomIndex(proportion);
                    VehicleSpec vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(i);
                    Road destinationRoad = destinationSelector.selectDestination(spawnPoint.getLane());

                    // maybe spawnPoint.getCurrentTime() is incorrect
                    // Diana: i think it's correct
                    result.add(new RIMSpawnPoint.RIMSpawnSpec(spawnPoint.getCurrentTime(),
                            vehicleSpec,
                            destinationRoad));
                }
            }

            return result;
        }
    }

    /**
     * Set the uniform turn based spawn points.
     *
     * @param map           the map
     * @param trafficLevel  the traffic level
     */
    public static void setUniformTurnBasedSpawnPoints(RimIntersectionMap map, double trafficLevel) {
        for(RIMSpawnPoint sp : map.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new UniformSpawnSpecGenerator(trafficLevel, new TurnBasedDestinationSelector(map)));
        }
    }

    /**
     * Set the uniform random spawn points.
     *
     * @param map           the map
     * @param trafficLevel  the traffic level
     */
    public static void setUniformRandomSpawnPoints(final RimIntersectionMap map, double trafficLevel) {
        for(RIMSpawnPoint sp : map.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new UniformSpawnSpecGenerator(trafficLevel,new RandomDestinationSelector(map)));
        }
    }

    /**
     * Set the uniform ratio spawn points with various traffic volume.
     *
     * @param map                    the map
     * @param trafficVolumeFileName  the traffic volume filename
     */
    public static void setUniformRatioSpawnPoints(RimIntersectionMap map,
                                                  String trafficVolumeFileName) {

        TrafficVolume trafficVolume =
                TrafficVolume.makeFromFile(map, trafficVolumeFileName);

        DestinationSelector selector = new RatioDestinationSelector(map,
                trafficVolume);

        for (RIMSpawnPoint sp : map.getSpawnPoints()) {
            int laneId = sp.getLane().getId();
            double trafficLevel =
                    trafficVolume.getLeftTurnVolume(laneId) +
                            trafficVolume.getThroughVolume(laneId) +
                            trafficVolume.getRightTurnVolume(laneId);
            sp.setVehicleSpecChooser(
                    new UniformSpawnSpecGenerator(trafficLevel, selector));
        }
    }

    /**
     * The uniform distributed spawn spec generator.
     */
    public static class JsonScheduleSpawnSpecGenerator implements RIMSpawnPoint.RIMSpawnSpecGenerator {
        // NESTED CLASSES //
        public static class ScheduledSpawn {
            private String specName;
            private Double spawnTime;
            private Long spawnArrivalLaneId;
            private String spawnDestinationRoadName;


            public ScheduledSpawn(String specName, Double spawnTime, Long spawnArrivalLaneId, String spawnDestinationRoadName) {
                this.specName = specName;
                this.spawnTime = spawnTime;
                this.spawnArrivalLaneId = spawnArrivalLaneId;
                this.spawnDestinationRoadName = spawnDestinationRoadName;
            }

            public String getSpecName() { return specName; }

            public double getSpawnTime() { return spawnTime; }

            public Long getSpawnArrivalLaneId() { return spawnArrivalLaneId; }

            public String getSpawnDestinationRoadName() { return spawnDestinationRoadName; }
        }

        // PRIVATE FIELDS //
        Queue<RimMapUtil.JsonScheduleSpawnSpecGenerator.ScheduledSpawn> schedule;

        // CONSTRUCTOR //
        public JsonScheduleSpawnSpecGenerator(File jsonFile, int spawnPointLaneId) throws IOException, ParseException {
            this.schedule = processJson(jsonFile, spawnPointLaneId);
        }

        private Queue<ScheduledSpawn> processJson(File jsonFile, int spawnPointLaneId) throws IOException, ParseException {
            JSONParser parser = new JSONParser();

            Object array = parser.parse(new FileReader(jsonFile));
            JSONArray jsonSchedule = (JSONArray) array;

            Queue<ScheduledSpawn> schedule = new LinkedList<ScheduledSpawn>();
            for(Object spawnObj : jsonSchedule) {
                JSONObject jsonSpawn = (JSONObject) spawnObj;
                String specName = (String) jsonSpawn.get("specName");
                Double spawnTime = (Double) jsonSpawn.get("spawnTime");
                Long spawnArrivalLaneId = (Long) jsonSpawn.get("specArrival");
                String spawnDestinationRoadName = (String) jsonSpawn.get("specDestination");
                if (spawnPointLaneId == Math.toIntExact(spawnArrivalLaneId)) {
                    schedule.add(new ScheduledSpawn(specName, spawnTime, spawnArrivalLaneId, spawnDestinationRoadName));
                }
            }
            return schedule;
        }

        // ACTION //
        @Override
        public List<RIMSpawnPoint.RIMSpawnSpec> act(RIMSpawnPoint spawnPoint, double timestep) {
            double initTime = spawnPoint.getCurrentTime();
            List<RIMSpawnPoint.RIMSpawnSpec> specs = new ArrayList<RIMSpawnPoint.RIMSpawnSpec>();
            for (double time = initTime; time < initTime + timestep; time += SimConfig.SPAWN_TIME_STEP) {
                if(!schedule.isEmpty()) {
                    if (time > schedule.peek().getSpawnTime() && spawnPoint.getLane().getId() == Math.toIntExact(schedule.peek().getSpawnArrivalLaneId())) {
                        Road destinationRoad = ((RimIntersectionMap) Debug.currentRimMap).getRoadByName(schedule.peek().getSpawnDestinationRoadName());
                        specs.add(new RIMSpawnPoint.RIMSpawnSpec(
                                spawnPoint.getCurrentTime(),
                                VehicleSpecDatabase.getVehicleSpecByName(schedule.poll().getSpecName()),
                                destinationRoad
                        ));
                    }
                }
            }
            return specs;
        }
    }

    public static void setJSONScheduleSpawnSpecGenerator(RimIntersectionMap map, File uploadedTrafficSchedule) {
        try {
            for(RIMSpawnPoint sp : map.getSpawnPoints()) {
                sp.setVehicleSpecChooser(
                        new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                uploadedTrafficSchedule,
                                sp.getLane().getId()
                        ));
            }
        } catch (Exception e) {
            throw new RuntimeException(
                    String.format(
                            "One of the files for the spawn schedules could not be used: %s\n",
                            e.getMessage()),
                    e
            );
        }

    }

    // SPAWN SCHEDULE GENERATOR //
    public static JSONArray createUniformSpawnSchedule(double trafficLevel, double timeLimit, int columns, int rows,
                                                       double roundaboutDiameter, double entranceExitRadius, int splitFactor,
                                                       double laneWidth, double laneSpeedLimit, double roundaboutSpeedLimit,
                                                       int lanesPerRoad, double widthBetweenOppositeRoads, double distanceBetween) {
        //Create Map to base the schedule on
        RimIntersectionMap map = new RimIntersectionMap(
                0, columns, rows, roundaboutDiameter, entranceExitRadius, splitFactor, laneWidth,
                laneSpeedLimit, roundaboutSpeedLimit, lanesPerRoad, widthBetweenOppositeRoads, distanceBetween);
       RimMapUtil.setUniformRandomSpawnPoints(map, trafficLevel);

        //Create SpawnHelper
        Map<Integer, RIMVehicleSimModel> vinToVehicles = new HashMap<Integer, RIMVehicleSimModel>();
        SpawnHelper spawnHelper = new SpawnHelper(map, vinToVehicles);
        SensorInputHelper sensorInputHelper = new SensorInputHelper(map, vinToVehicles);

        //Create schedule
        JSONArray schedule = new JSONArray();
        double currentTime = 0;
        while (currentTime < timeLimit) {
            //Spawn Vehicles
            List<RIMVehicleSimModel> spawnedVehicles =
                    spawnHelper.generateSpawnedVehicles(SimConfig.RIM_TIME_STEP);
            if (spawnedVehicles != null) {
                for (RIMVehicleSimModel rimVehicleSimModel : spawnedVehicles){
                    VehicleSpec vSpec = rimVehicleSimModel.getSpec();
                    JSONObject scheduledSpawn = new JSONObject();
                    scheduledSpawn.put("specName", vSpec.getName());
                    scheduledSpawn.put("spawnTime", currentTime);
                    scheduledSpawn.put("specArrival",rimVehicleSimModel.getDriver().getCurrentLane().getId());
                    scheduledSpawn.put("specDestination", ((RIMAutoDriver) rimVehicleSimModel.getDriver()).getDestination().getName());
                    schedule.add(scheduledSpawn);
                }
            }

            //Provide sensor input
            sensorInputHelper.provideSensorInput();

            //Vehicle movement
            for(RIMVehicleSimModel vehicle : vinToVehicles.values()){
                vehicle.getDriver().act();
            }
            for(RIMVehicleSimModel vehicle : vinToVehicles.values()){
                vehicle.move(SimConfig.TIME_STEP);
            }
            List<RIMVehicleSimModel> removedVehicles = new ArrayList<RIMVehicleSimModel>(vinToVehicles.size());
            for(RIMVehicleSimModel vehicle : vinToVehicles.values()){
                if(!vehicle.getShape().intersects(map.getDimensions()))
                    removedVehicles.add(vehicle);
            }
            for(RIMVehicleSimModel vehicle : removedVehicles) {
                vinToVehicles.remove(vehicle.getVIN());
            }
            currentTime += SimConfig.TIME_STEP;
        }


        return schedule;
    }

    public static JSONArray createRatioSpawnSchedule(String trafficVolumeFileName, double timeLimit, int columns, int rows,
                                                       double roundaboutDiameter, double entranceExitRadius, int splitFactor,
                                                       double laneWidth, double laneSpeedLimit, double roundaboutSpeedLimit,
                                                       int lanesPerRoad, double widthBetweenOppositeRoads, double distanceBetween) {
        //Create Map to base the schedule on
        RimIntersectionMap map = new RimIntersectionMap(
                0, columns, rows, roundaboutDiameter, entranceExitRadius, splitFactor, laneWidth,
                laneSpeedLimit, roundaboutSpeedLimit, lanesPerRoad, widthBetweenOppositeRoads, distanceBetween);
        RimMapUtil.setUniformRatioSpawnPoints(map, trafficVolumeFileName);

        //Create SpawnHelper
        Map<Integer, RIMVehicleSimModel> vinToVehicles = new HashMap<Integer, RIMVehicleSimModel>();
        SpawnHelper spawnHelper = new SpawnHelper(map, vinToVehicles);
        SensorInputHelper sensorInputHelper = new SensorInputHelper(map, vinToVehicles);

        //Create schedule
        JSONArray schedule = new JSONArray();
        double currentTime = 0;
        while (currentTime < timeLimit) {
            //Spawn Vehicles
            List<RIMVehicleSimModel> spawnedVehicles =
                    spawnHelper.generateSpawnedVehicles(SimConfig.RIM_TIME_STEP);
            if (spawnedVehicles != null) {
                for (RIMVehicleSimModel rimVehicleSimModel : spawnedVehicles){
                    VehicleSpec vSpec = rimVehicleSimModel.getSpec();
                    JSONObject scheduledSpawn = new JSONObject();
                    scheduledSpawn.put("specName", vSpec.getName());
                    scheduledSpawn.put("spawnTime", currentTime);
                    scheduledSpawn.put("specArrival",rimVehicleSimModel.getDriver().getCurrentLane().getId());
                    scheduledSpawn.put("specDestination", ((RIMAutoDriver) rimVehicleSimModel.getDriver()).getDestination().getName());
                    schedule.add(scheduledSpawn);
                }

            }

            //Provide sensor input
            sensorInputHelper.provideSensorInput();
            //Vehicle movement
            for(RIMVehicleSimModel vehicle : vinToVehicles.values()){
                vehicle.getDriver().act();
            }
            for(RIMVehicleSimModel vehicle : vinToVehicles.values()){
                vehicle.move(SimConfig.TIME_STEP);
            }
            List<RIMVehicleSimModel> removedVehicles = new ArrayList<RIMVehicleSimModel>(vinToVehicles.size());
            for(RIMVehicleSimModel vehicle : vinToVehicles.values()){
                if(!vehicle.getShape().intersects(map.getDimensions()))
                    removedVehicles.add(vehicle);
            }
            for(RIMVehicleSimModel vehicle : removedVehicles) {
                vinToVehicles.remove(vehicle.getVIN());
            }
            currentTime += SimConfig.TIME_STEP;
        }


        return schedule;
    }
}
