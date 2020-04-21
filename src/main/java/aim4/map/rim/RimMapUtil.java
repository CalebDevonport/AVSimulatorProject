package aim4.map.rim;

import aim4.config.Constants;
import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.rim.RIMAutoDriver;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.im.rim.v2i.RequestHandler.FCFSRequestHandler;
import aim4.im.rim.v2i.V2IManager;
import aim4.im.rim.v2i.policy.AcceptAllPolicy;
import aim4.im.rim.v2i.policy.BasePolicy;
import aim4.im.rim.v2i.reservation.ReservationGridManager;
import aim4.map.Road;
import aim4.map.lane.Lane;
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
import java.util.concurrent.ThreadLocalRandom;

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
                im.setPolicy(new BasePolicy(im, new FCFSRequestHandler(),BasePolicy.PolicyType.FCFS));
                layout.setManager(column, row, im);
            }
        }
    }

    /**
     * Set the no protocol managers at all intersections.
     *
     * @param layout       the map
     * @param currentTime  the current time
     * @param config       the reservation grid manager configuration
     */
    public static void setOptimalProtocolManagers(RimIntersectionMap layout,
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
                im.setPolicy(new AcceptAllPolicy(im));
                layout.setManager(column, row, im);
            }
        }
    }

    /**
     * Set the no protocol managers at all intersections.
     *
     * @param layout       the map
     * @param currentTime  the current time
     * @param config       the reservation grid manager configuration
     */
    public static void setStopSignManagers(RimIntersectionMap layout,
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
                im.setPolicy(new BasePolicy(im, new FCFSRequestHandler(), BasePolicy.PolicyType.STOP_SIGN));
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
            proportion = getVehicleSpecProportion();
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
            private String arrivalRoadName;
            private String destinationRoadName;


            public ScheduledSpawn(String specName, Double spawnTime, String arrivalRoadName, String destinationRoadName) {
                this.specName = specName;
                this.spawnTime = spawnTime;
                this.arrivalRoadName = arrivalRoadName;
                this.destinationRoadName = destinationRoadName;
            }

            public String getSpecName() { return specName; }

            public double getSpawnTime() { return spawnTime; }

            public String getArrivalRoadName() { return arrivalRoadName; }

            public String getDestinationRoadName() { return destinationRoadName; }
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
                String spawnArrivalRoadName = (String) jsonSpawn.get("arrivalRoadName");
                String spawnDestinationRoadName = (String) jsonSpawn.get("destinationRoadName");
                int laneIndex = Integer.parseInt((String) jsonSpawn.get("laneIndex"));
                if (Debug.currentRimMap.getRoad(spawnPointLaneId).getName().compareTo(spawnArrivalRoadName) == 0) {
                	if(Debug.currentRimMap.getRoad(spawnPointLaneId).getLaneIndexFromLane(spawnPointLaneId) == laneIndex) {
                		schedule.add(new ScheduledSpawn(specName, spawnTime, spawnArrivalRoadName, spawnDestinationRoadName));	
                	}
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
                    if (time > schedule.peek().getSpawnTime() &&
                    Debug.currentRimMap.getRoad(spawnPoint.getLane().getId()).getName().compareTo(schedule.peek().getArrivalRoadName()) == 0) {
                        Road destinationRoad = ((RimIntersectionMap) Debug.currentRimMap).getRoadByName(schedule.peek().getDestinationRoadName());
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
    @SuppressWarnings("unchecked")
	public static JSONArray createWorkingSpawnSchedule(String trafficVolumeFileName, double timeLimit, int columns, int rows,
                                                       double roundaboutDiameter, double entranceExitRadius, int splitFactor,
                                                       double laneWidth, double laneSpeedLimit, double roundaboutSpeedLimit,
                                                       int lanesPerRoad, double widthBetweenOppositeRoads, double distanceBetween) {
        //Create Map to base the schedule on
        RimIntersectionMap map = new RimIntersectionMap(
                0, columns, rows, roundaboutDiameter, entranceExitRadius, splitFactor, laneWidth,
                laneSpeedLimit, roundaboutSpeedLimit, lanesPerRoad, widthBetweenOppositeRoads, distanceBetween);
        
        TrafficVolume trafficVolume =
                TrafficVolume.makeFromFile(map, trafficVolumeFileName);
        
        JSONArray schedule = new JSONArray();
        
        List<Double> proportion = getVehicleSpecProportion();
        
        List<String> strs = null;
        try {
            strs = Util.readFileToStrArray(trafficVolumeFileName);
        } catch (IOException e) {
            System.err.println("Error: " + e.getMessage());
        }
        if (strs != null) {
        	for (int i = 1; i < strs.size(); i++) {
                String[] tokens = strs.get(i).split(",");

                if (tokens[1].equals("Left")) {
                	int laneIndex = Integer.parseInt(tokens[3]);
                	if (laneIndex < map.getRoads().get(0).getContinuousLanes().size()) {
                		Lane lane = trafficVolume.getRoadToMiddleLanes().get(tokens[0]).get(laneIndex);
                		
                		Double spawnNum = Double.parseDouble(tokens[2]);
                		
                		for (int z = 0; z < spawnNum; z++) {
                			double spawnTime = ThreadLocalRandom.current().nextDouble(0, 1800.0);
                			
                			int j = Util.randomIndex(proportion);
                	        VehicleSpec vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(j);
                	        String vehicleName = vehicleSpec.getName();
                	        
                			JSONObject scheduledSpawn = new JSONObject();
                			scheduledSpawn.put("specName", vehicleName);
                            scheduledSpawn.put("spawnTime", spawnTime);
                            String arrivalRoadName = trafficVolume.getRoadNameTranslation().get(tokens[0]);
                            Road arrivalRoad = trafficVolume.getRoadNameToRoadObj().get(tokens[0]);
                            Road departureRoad = trafficVolume.getLeftTurnRoad(arrivalRoad);
                            String departureRoadName = departureRoad.getName();
                            scheduledSpawn.put("arrivalRoadName",arrivalRoadName);
                            scheduledSpawn.put("destinationRoadName", departureRoadName);
                            scheduledSpawn.put("laneIndex", tokens[3]);
                            schedule.add(scheduledSpawn);
                		}
                	}
                }
                
                if (tokens[1].equals("Through")) {
                	int laneIndex = Integer.parseInt(tokens[3]);
                	if (laneIndex < map.getRoads().get(0).getContinuousLanes().size()) {
                		Lane lane = trafficVolume.getRoadToMiddleLanes().get(tokens[0]).get(laneIndex);
                		
                		Double spawnNum = Double.parseDouble(tokens[2]);
                		
                		for (int z = 0; z < spawnNum; z++) {
                			double spawnTime = ThreadLocalRandom.current().nextDouble(0, 1800.0);
                			
                			int j = Util.randomIndex(proportion);
                	        VehicleSpec vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(j);
                	        String vehicleName = vehicleSpec.getName();
                	        
                			JSONObject scheduledSpawn = new JSONObject();
                			scheduledSpawn.put("specName", vehicleName);
                            scheduledSpawn.put("spawnTime", spawnTime);
                            String arrivalRoadName = trafficVolume.getRoadNameTranslation().get(tokens[0]);
                            String departureRoadName = arrivalRoadName;
                            scheduledSpawn.put("arrivalRoadName",arrivalRoadName);
                            scheduledSpawn.put("destinationRoadName", departureRoadName);
                            scheduledSpawn.put("laneIndex", tokens[3]);
                            schedule.add(scheduledSpawn);
                		}
                	}
                }
                
                if (tokens[1].equals("Right")) {
                	int laneIndex = Integer.parseInt(tokens[3]);
                	if (laneIndex < map.getRoads().get(0).getContinuousLanes().size()) {
                		Lane lane = trafficVolume.getRoadToMiddleLanes().get(tokens[0]).get(laneIndex);
                		
                		Double spawnNum = Double.parseDouble(tokens[2]);
                		
                		for (int z = 0; z < spawnNum; z++) {
                			double spawnTime = ThreadLocalRandom.current().nextDouble(0, 1800.0);
                			
                			int j = Util.randomIndex(proportion);
                	        VehicleSpec vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(j);
                	        String vehicleName = vehicleSpec.getName();
                	        
                			JSONObject scheduledSpawn = new JSONObject();
                			scheduledSpawn.put("specName", vehicleName);
                            scheduledSpawn.put("spawnTime", spawnTime);
                            String arrivalRoadName = trafficVolume.getRoadNameTranslation().get(tokens[0]);
                            Road arrivalRoad = trafficVolume.getRoadNameToRoadObj().get(tokens[0]);
                            Road departureRoad = trafficVolume.getRightTurnRoad(arrivalRoad);
                            String departureRoadName = departureRoad.getName();
                            scheduledSpawn.put("arrivalRoadName",arrivalRoadName);
                            scheduledSpawn.put("destinationRoadName", departureRoadName);
                            scheduledSpawn.put("laneIndex", tokens[3]);
                            schedule.add(scheduledSpawn);
                		}
                	}
                }
        	}
        }
        
        Collections.sort( schedule, new Comparator<JSONObject>() {
            private static final String KEY_NAME = "spawnTime";

            @Override
            public int compare(JSONObject a, JSONObject b) {
                Double valA = (Double) a.get(KEY_NAME);
                Double valB = (Double) b.get(KEY_NAME);

                return valA.compareTo(valB);
            }
        });
        
        return schedule;
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
                    scheduledSpawn.put("arrivalRoadName",Debug.currentRimMap.getRoad(rimVehicleSimModel.getDriver().getCurrentLane().getId()).getName());
                    scheduledSpawn.put("destinationRoadName", ((RIMAutoDriver) rimVehicleSimModel.getDriver()).getDestination().getName());
                    scheduledSpawn.put("laneIndex", 
                    		Debug.currentRimMap.getRoad(rimVehicleSimModel.getDriver().getCurrentLane().getId())
                    									.getLaneIndexFromLane(rimVehicleSimModel.getDriver().getCurrentLane()));
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
                    scheduledSpawn.put("arrivalRoadName",Debug.currentRimMap.getRoad(rimVehicleSimModel.getDriver().getCurrentLane().getId()).getName());
                    scheduledSpawn.put("destinationRoadName", ((RIMAutoDriver) rimVehicleSimModel.getDriver()).getDestination().getName());
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
    
    private static ArrayList<Double> getVehicleSpecProportion() {
    	int n = VehicleSpecDatabase.getNumOfSpec();
        ArrayList<Double> proportion = new ArrayList<Double>(n);
        double p = 1.0 / n;
        for(int i=0; i<n; i++) {
            proportion.add(p);
        }
        return proportion;
    }
}
