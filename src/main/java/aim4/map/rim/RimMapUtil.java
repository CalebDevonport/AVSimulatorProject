package aim4.map.rim;

import aim4.config.SimConfig;
import aim4.im.aim.RoadBasedIntersection;
import aim4.im.aim.RoadBasedTrackModel;
import aim4.im.aim.v2i.RequestHandler.FCFSRequestHandler;
import aim4.im.aim.v2i.V2IManager;
import aim4.im.aim.v2i.policy.BasePolicy;
import aim4.im.aim.v2i.reservation.ReservationGridManager;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;
import aim4.map.destination.DestinationSelector;
import aim4.util.Util;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.rim.RimVehicleSimModel;
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
//todo: REWRITE THE SET MANAGERS BASED ON rim, SEE BELOW
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

    // Set spawn points
    public static void setSingleSpawnPoints(RimIntersectionMap map) {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new RimMapUtil.SingleSpawnSpecGenerator()
            );
        }
    }

    public static void setSingleSpawnPoints(RimIntersectionMap map, VehicleSpec spec) {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new RimMapUtil.SingleSpawnSpecGenerator(spec)
            );
        }
    }
    public static void setUniformSpawnSpecGenerator(RimIntersectionMap map, double trafficLevel) {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new RimMapUtil.UniformSpawnSpecGenerator(trafficLevel)
            );
        }
    }

    // Spawn points generators
    public static AIMSpawnPoint.AIMSpawnSpecGenerator nullSpawnSpecGenerator =
            (spawnPoint, timeStep) -> new ArrayList<AIMSpawnPoint.AIMSpawnSpec>();

    public static class SingleSpawnSpecGenerator implements AIMSpawnPoint.AIMSpawnSpecGenerator {
        private List<Double> proportion;
        private Road destination;
        private VehicleSpec spec;
        private List<AIMSpawnPoint> spawnPointAlreadySpawned;

        /**
         * Call to spawn random vehicle spec during act.
         */
        public SingleSpawnSpecGenerator() {
            int n = VehicleSpecDatabase.getNumOfSpec();
            proportion = new ArrayList<Double>(n);
            double p = 1.0 / n;
            for(int i=0; i<n; i++) {
                proportion.add(p);
            }

            spawnPointAlreadySpawned = new ArrayList<AIMSpawnPoint>();
        }

        /**
         * Call to spawn specific vehicle spec during act.
         * @param spec The vehicle spec to spawn
         */
        public SingleSpawnSpecGenerator(VehicleSpec spec) {
            this.spec = spec;
            spawnPointAlreadySpawned = new ArrayList<AIMSpawnPoint>();
        }

        /**
         * Call to spawn random vehicle spec during act.
         */
        public SingleSpawnSpecGenerator(Road destination) {
            int n = VehicleSpecDatabase.getNumOfSpec();
            proportion = new ArrayList<Double>(n);
            double p = 1.0 / n;
            for(int i=0; i<n; i++) {
                proportion.add(p);
            }

            this.destination = destination;
            spawnPointAlreadySpawned = new ArrayList<AIMSpawnPoint>();
        }

        /**
         * Call to spawn specific vehicle spec during act.
         * @param spec The vehicle spec to spawn
         */
        public SingleSpawnSpecGenerator(Road destination, VehicleSpec spec) {
            this.spec = spec;
            this.destination = destination;
            spawnPointAlreadySpawned = new ArrayList<AIMSpawnPoint>();
        }

        /**
         * Creates single SpawnSpec for a given spawnPoint. Will never Spawn more than once for a given SpawnPoint.
         * @param spawnPoint
         * @param timestep
         * @return
         */
        @Override
        public List<AIMSpawnPoint.AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timestep) {
            List<AIMSpawnPoint.AIMSpawnSpec> result = new LinkedList<AIMSpawnPoint.AIMSpawnSpec>();

            if(!spawnPointAlreadySpawned.contains(spawnPoint)) {
                spawnPointAlreadySpawned.add(spawnPoint);
                double initTime = spawnPoint.getCurrentTime();
                if (this.spec == null) {
                    int i = Util.randomIndex(proportion);
                    this.spec = VehicleSpecDatabase.getVehicleSpecById(i);
                }

                result.add(new AIMSpawnPoint.AIMSpawnSpec(spawnPoint.getCurrentTime(), spec, destination));
            }

            return result;
        }
    }

    /**
     * The uniform distributed spawn spec generator.
     */
    public static class UniformSpawnSpecGenerator implements AIMSpawnPoint.AIMSpawnSpecGenerator {
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
         */
        public UniformSpawnSpecGenerator(double trafficLevel) {
            int n = VehicleSpecDatabase.getNumOfSpec();
            proportion = new ArrayList<Double>(n);
            double p = 1.0 / n;
            for(int i=0; i<n; i++) {
                proportion.add(p);
            }

            prob = trafficLevel * SimConfig.SPAWN_TIME_STEP;
            // Cannot generate more than one vehicle in each spawn time step
            assert prob <= 1.0;
        }

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
        public List<AIMSpawnPoint.AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
            List<AIMSpawnPoint.AIMSpawnSpec> result = new LinkedList<AIMSpawnPoint.AIMSpawnSpec>();

            double initTime = spawnPoint.getCurrentTime();
            for(double time = initTime; time < initTime + timeStep;
                time += SimConfig.SPAWN_TIME_STEP) {
                if (Util.random.nextDouble() < prob) {
                    int i = Util.randomIndex(proportion);
                    VehicleSpec vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(i);
                    Road destinationRoad =
                            destinationSelector.selectDestination(spawnPoint.getLane());

                    // maybe spawnPoint.getCurrentTime() is incorrect
                    result.add(new AIMSpawnPoint.AIMSpawnSpec(spawnPoint.getCurrentTime(),
                            vehicleSpec,
                            destinationRoad));
                }
            }

            return result;
        }
    }

    /**
     * The uniform distributed spawn spec generator.
     */
    public static class JsonScheduleSpawnSpecGenerator implements AIMSpawnPoint.AIMSpawnSpecGenerator {
        // NESTED CLASSES //
        public static class ScheduledSpawn {
            private String specName;
            private Double spawnTime;

            public ScheduledSpawn(String specName, Double spawnTime) {
                this.specName = specName;
                this.spawnTime = spawnTime;
            }

            public String getSpecName() {
                return specName;
            }

            public double getSpawnTime() {
                return spawnTime;
            }
        }

        // PRIVATE FIELDS //
        Queue<RimMapUtil.JsonScheduleSpawnSpecGenerator.ScheduledSpawn> schedule;
        Road destinationRoad;

        // CONSTRUCTOR //
        public JsonScheduleSpawnSpecGenerator(File jsonFile) throws IOException, ParseException {
            this.schedule = processJson(jsonFile);
        }

        public JsonScheduleSpawnSpecGenerator(File jsonFile, Road destinationRoad) throws IOException, ParseException {
            this.schedule = processJson(jsonFile);
            this.destinationRoad = destinationRoad;
        }

        private Queue<ScheduledSpawn> processJson(File jsonFile) throws IOException, ParseException {
            JSONParser parser = new JSONParser();

            Object array = parser.parse(new FileReader(jsonFile));
            JSONArray jsonSchedule = (JSONArray) array;

            Queue<ScheduledSpawn> schedule = new LinkedList<ScheduledSpawn>();
            for(Object spawnObj : jsonSchedule) {
                JSONObject jsonSpawn = (JSONObject) spawnObj;
                String specName = (String) jsonSpawn.get("specName");
                Double spawnTime = (Double) jsonSpawn.get("spawnTime");

                schedule.add(new ScheduledSpawn(specName, spawnTime));
            }
            return schedule;
        }

        // ACTION //
        @Override
        public List<AIMSpawnPoint.AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timestep) {
            double initTime = spawnPoint.getCurrentTime();
            List<AIMSpawnPoint.AIMSpawnSpec> specs = new ArrayList<AIMSpawnPoint.AIMSpawnSpec>();
            for (double time = initTime; time < initTime + timestep; time += SimConfig.SPAWN_TIME_STEP) {
                if(!schedule.isEmpty()) {
                    if (time > schedule.peek().getSpawnTime()) {
                        specs.add(new AIMSpawnPoint.AIMSpawnSpec(
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

    public static void setJSONScheduleSpawnSpecGenerator(RimIntersectionMap map, File leftSchedule, File rightSchedule, File straightSchedule) throws IOException, ParseException {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
            if(sp.getHeading() == 0) { // is going east
                Road targetRoad = null;
                for(Road r : map.getDestinationRoads()) {
                    if (r.getFirstLane().getInitialHeading() == 0) { // will go east (straight)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        straightSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI + Math.PI/2) { //will go north (left)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        leftSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI/2) { //will go south (right)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        rightSchedule,
                                        targetRoad
                                ));
                    }
                }
            } else if(sp.getHeading() == Math.PI/2) { //is going south
                Road targetRoad = null;
                for(Road r : map.getDestinationRoads()) {
                    if (r.getFirstLane().getInitialHeading() == 0) { // will go east (left)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        leftSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI) { //will go west (right)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        rightSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI/2) { //will go south (straight)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        straightSchedule,
                                        targetRoad
                                ));
                    }
                }
            }
            else if(sp.getHeading() == Math.PI + Math.PI/2) { //is going north
                Road targetRoad = null;
                for(Road r : map.getDestinationRoads()) {
                    if (r.getFirstLane().getInitialHeading() == 0) { // will go east (right)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        rightSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI) { //will go west (left)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        leftSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI+ Math.PI/2) { //will go north (straight)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        straightSchedule,
                                        targetRoad
                                ));
                    }
                }
            }
            else if(sp.getHeading() == Math.PI) { //is going west
                Road targetRoad = null;
                for(Road r : map.getDestinationRoads()) {
                    if (r.getFirstLane().getInitialHeading() == Math.PI/2) { // will go south (left)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        leftSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI) { //will go west (straight)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        straightSchedule,
                                        targetRoad
                                ));
                    }
                    if(r.getFirstLane().getInitialHeading() == Math.PI+ Math.PI/2) { //will go north (right)
                        targetRoad = r;
                        assert targetRoad != null;
                        sp.setVehicleSpecChooser(
                                new RimMapUtil.JsonScheduleSpawnSpecGenerator(
                                        rightSchedule,
                                        targetRoad
                                ));
                    }
                }
            }
            else  {
                sp.setVehicleSpecChooser(new AIMSpawnPoint.AIMSpawnSpecGenerator() {
                    @Override
                    public List<AIMSpawnPoint.AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
                        return new ArrayList<AIMSpawnPoint.AIMSpawnSpec>();
                    }
                });
            }
        }
    }
    // SPAWN SCHEDULE GENERATOR //
    public static JSONArray createSpawnSchedule(double trafficLevel, double timeLimit, double roundaboutDiameter,
                                                double entranceExitRadius, int splitFactor, double laneWidth,
                                                double laneSpeedLimit, double roundaboutSpeedLimit) {
        //Create Map to base the schedule on
        RimIntersectionMap map = new RimIntersectionMap(
                0, 1, 1, roundaboutDiameter, entranceExitRadius, splitFactor, laneWidth,
                laneSpeedLimit, roundaboutSpeedLimit, 1, 0, 0);
        RimMapUtil.setUniformSpawnSpecGenerator(map, trafficLevel);

        //Create SpawnHelper
        Map<Integer, RimVehicleSimModel> vinToVehicles = new HashMap<Integer, RimVehicleSimModel>();
//        SpawnHelper spawnHelper = new SpawnHelper(map, vinToVehicles);
//        SensorInputHelper sensorInputHelper = new SensorInputHelper(map, vinToVehicles);
//
//        //Create schedule
//        JSONArray schedule = new JSONArray();
//        double currentTime = 0;
//        while (currentTime < timeLimit) {
//            //Spawn Vehicles
//            List<MergeVehicleSimModel> spawnedVehicles =
//                    spawnHelper.spawnVehicles(SimConfig.MERGE_TIME_STEP, ProtocolType.NONE);
//            if (spawnedVehicles != null) {
//                VehicleSpec vSpec = spawnedVehicles.get(0).getSpec(); //Only expecting one.
//                JSONObject scheduledSpawn = new JSONObject();
//                scheduledSpawn.put("specName", vSpec.getName());
//                scheduledSpawn.put("spawnTime", currentTime);
//                schedule.add(scheduledSpawn);
//            }
//
//            //Provide sensor input
//            sensorInputHelper.provideSensorInput();
//
//            //Vehicle movement
//            for(MergeVehicleSimModel vehicle : vinToVehicles.values()){
//                vehicle.getDriver().act();
//            }
//            for(MergeVehicleSimModel vehicle : vinToVehicles.values()){
//                vehicle.move(SimConfig.TIME_STEP);
//            }
//            List<MergeVehicleSimModel> removedVehicles = new ArrayList<MergeVehicleSimModel>(vinToVehicles.size());
//            for(MergeVehicleSimModel vehicle : vinToVehicles.values()){
//                if(!vehicle.getShape().intersects(map.getDimensions()))
//                    removedVehicles.add(vehicle);
//            }
//            for(MergeVehicleSimModel vehicle : removedVehicles) {
//                vinToVehicles.remove(vehicle.getVIN());
//            }
//            currentTime += SimConfig.TIME_STEP;
//        }
//
//
//        return schedule;
        return null;
    }
}
