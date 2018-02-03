/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.map.aim;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.config.TrafficSignalPhase;
import aim4.im.aim.RoadBasedIntersection;
import aim4.im.aim.RoadBasedTrackModel;
import aim4.im.aim.v2i.RequestHandler.*;
import aim4.im.aim.v2i.RequestHandler.ApproxNPhasesTrafficSignalRequestHandler.CyclicSignalController;
import aim4.im.aim.v2i.V2IManager;
import aim4.im.aim.v2i.batch.RoadBasedReordering;
import aim4.im.aim.v2i.policy.BasePolicy;
import aim4.im.aim.v2i.reservation.ReservationGridManager;
import aim4.map.BasicIntersectionMap;
import aim4.map.BasicMap;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint.AIMSpawnSpec;
import aim4.map.aim.AIMSpawnPoint.AIMSpawnSpecGenerator;
import aim4.map.aim.destination.DestinationSelector;
import aim4.map.aim.destination.RandomDestinationSelector;
import aim4.map.aim.destination.RatioDestinationSelector;
import aim4.map.aim.destination.TurnBasedDestinationSelector;
import aim4.map.lane.Lane;
import aim4.util.Util;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * The utility class for GridIntersectionMap.
 */
public class GridMapUtil {

    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The null spawn spec generator that generates nothing.
     */
    public static AIMSpawnSpecGenerator nullSpawnSpecGenerator =
            new AIMSpawnSpecGenerator() {
                @Override
                public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
                    return new ArrayList<AIMSpawnSpec>();
                }
            };

    /**
     * The uniform distributed spawn spec generator.
     */
    public static class UniformSpawnSpecGenerator implements AIMSpawnSpecGenerator {
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
        public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
            List<AIMSpawnSpec> result = new LinkedList<AIMSpawnSpec>();

            double initTime = spawnPoint.getCurrentTime();
            for(double time = initTime; time < initTime + timeStep;
                time += SimConfig.SPAWN_TIME_STEP) {
                if (Util.random.nextDouble() < prob) {
                    int i = Util.randomIndex(proportion);
                    VehicleSpec vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(i);
                    Road destinationRoad =
                            destinationSelector.selectDestination(spawnPoint.getLane());

                    // maybe spawnPoint.getCurrentTime() is incorrect
                    result.add(new AIMSpawnSpec(spawnPoint.getCurrentTime(),
                            vehicleSpec,
                            destinationRoad));
                }
            }

            return result;
        }
    }

    /**
     * The spawn spec generator that generates only one spec.
     */
    public static class OneSpawnSpecGenerator implements AIMSpawnSpecGenerator {
        /** The vehicle specification */
        private VehicleSpec vehicleSpec;
        /** The destination selector */
        private DestinationSelector destinationSelector;
        /** the probability of generating a vehicle in each spawn time step */
        private double prob;

        /**
         * Create a spawn spec generator that generates only one spec.
         *
         * @param vehicleSpecId        the vehicle spec ID
         * @param trafficLevel         the traffic level
         * @param destinationSelector  the destination selector
         */
        public OneSpawnSpecGenerator(int vehicleSpecId,
                                     double trafficLevel,
                                     DestinationSelector destinationSelector) {
            vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(vehicleSpecId);
            this.destinationSelector = destinationSelector;

            prob = trafficLevel * SimConfig.SPAWN_TIME_STEP;
            // Cannot generate more than one vehicle in each spawn time step
            assert prob <= 1.0;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
            List<AIMSpawnSpec> result = new LinkedList<AIMSpawnSpec>();

            double initTime = spawnPoint.getCurrentTime();
            for(double time = initTime; time < initTime + timeStep;
                time += SimConfig.SPAWN_TIME_STEP) {
                if (Util.random.nextDouble() < prob) {
                    Road destinationRoad =
                            destinationSelector.selectDestination(spawnPoint.getLane());

                    result.add(new AIMSpawnSpec(spawnPoint.getCurrentTime(),
                            vehicleSpec,
                            destinationRoad));
                }
            }

            return result;
        }
    }

    /**
     * The spec generator that generates just one vehicle in the entire
     * simulation.
     */
    public static class OnlyOneSpawnSpecGenerator implements AIMSpawnSpecGenerator {
        /** The vehicle specification */
        private VehicleSpec vehicleSpec;
        /** The destination road */
        private Road destinationRoad;
        /**The destination selector to generate a destination.*/
        private DestinationSelector destinationSelector;
        /** Whether the spec has been generated */
        private boolean isDone;
        /**The map that spawn point will belong to. */
        private BasicMap map;

        /**
         * Create a spec generator that generates just one vehicle in the entire
         * simulation.
         *
         * @param vehicleSpecId    the vehicle spec ID
         * @param destinationRoad  the destination road
         */
        public OnlyOneSpawnSpecGenerator(int vehicleSpecId, Road destinationRoad) {
            vehicleSpec = VehicleSpecDatabase.getVehicleSpecById(vehicleSpecId);
            this.destinationRoad = destinationRoad;
            isDone = false;
        }

        /**
         * Create a spec generator that generates just one vehicle in the entire
         * simulation.
         */
        public OnlyOneSpawnSpecGenerator(BasicIntersectionMap map) {
            vehicleSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");
            isDone = false;
            destinationSelector = new RandomDestinationSelector(map);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
            List<AIMSpawnSpec> result = new ArrayList<AIMSpawnSpec>(1);
            if (!isDone) {
                isDone = true;
                result.add(new AIMSpawnSpec(spawnPoint.getCurrentTime(),
                        vehicleSpec,
                        destinationRoad));
            }
            return result;
        }
    }

    /**
     * The spawn spec generator that enumerates spawn spec.
     */
    public static class EnumerateSpawnSpecGenerator implements AIMSpawnSpecGenerator{
        /** The list of destination roads */
        private List<Road> destinationRoads;
        /** The vehicle spec ID */
        int vehicleSpecId;
        /** The destination road ID */
        int destinationRoadId;
        /** The next spawn time */
        double nextSpawnTime;
        /** The spawn period */
        double spawnPeriod;

        /**
         * Create a spawn spec generator that enumerates spawn spec.
         *
         * @param spawnPoint        the spawn point
         * @param destinationRoads  the list of destination roads
         * @param initSpawnTime     the initial spawn time
         * @param spawnPeriod       the spawn period
         */
        public EnumerateSpawnSpecGenerator(AIMSpawnPoint spawnPoint,
                                           List<Road> destinationRoads,
                                           double initSpawnTime,
                                           double spawnPeriod) {
            this.destinationRoads = new ArrayList<Road>(destinationRoads.size());
            for(Road road : destinationRoads) {
                if (Debug.currentMap.getRoad(spawnPoint.getLane()).getDual() != road) {
                    this.destinationRoads.add(road);
                }
            }
            this.vehicleSpecId = 0;
            this.destinationRoadId = 0;
            this.nextSpawnTime = initSpawnTime;
            this.spawnPeriod = spawnPeriod;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
            List<AIMSpawnSpec> result = new ArrayList<AIMSpawnSpec>(1);
            if (spawnPoint.getCurrentTime() >= nextSpawnTime) {
                if (vehicleSpecId < VehicleSpecDatabase.getNumOfSpec()) {
                    VehicleSpec vehicleSpec =
                            VehicleSpecDatabase.getVehicleSpecById(vehicleSpecId);
                    Road destinationRoad =
                            destinationRoads.get(destinationRoadId);
                    result.add(new AIMSpawnSpec(spawnPoint.getCurrentTime(),
                            vehicleSpec,
                            destinationRoad));
                    nextSpawnTime += spawnPeriod;
                    destinationRoadId++;
                    if (destinationRoadId >= destinationRoads.size()) {
                        destinationRoadId = 0;
                        vehicleSpecId++;
                    }
                }  // else don't spawn any vehicle
            } // else wait until next spawn time
            return result;
        }
    }

    /**
     * The uniform distributed spawn spec generator.
     */
    public static class JsonScheduleSpawnSpecGenerator implements AIMSpawnSpecGenerator {
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
        Queue<ScheduledSpawn> schedule;
        Road destinationRoad;

        // CONSTRUCTOR //
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
        public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timestep) {
            double initTime = spawnPoint.getCurrentTime();
            List<AIMSpawnSpec> specs = new ArrayList<AIMSpawnSpec>();
            for (double time = initTime; time < initTime + timestep; time += SimConfig.SPAWN_TIME_STEP) {
                if(!schedule.isEmpty()) {
                    if (time > schedule.peek().getSpawnTime()) {
                        specs.add(new AIMSpawnSpec(
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

    public static class SingleSpawnSpecGenerator implements AIMSpawnSpecGenerator {
        private List<Double> proportion;
        private Road destination;
        private VehicleSpec spec;
        private List<AIMSpawnPoint> spawnPointAlreadySpawned;

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
        public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timestep) {
            List<AIMSpawnSpec> result = new LinkedList<AIMSpawnSpec>();

            if(!spawnPointAlreadySpawned.contains(spawnPoint)) {
                spawnPointAlreadySpawned.add(spawnPoint);
                double initTime = spawnPoint.getCurrentTime();
                if (this.spec == null) {
                    int i = Util.randomIndex(proportion);
                    this.spec = VehicleSpecDatabase.getVehicleSpecById(i);
                }

                result.add(new AIMSpawnSpec(spawnPoint.getCurrentTime(), spec, destination));
            }

            return result;
        }
    }


    /////////////////////////////////
    // PUBLIC STATIC METHODS
    /////////////////////////////////


    /**
     * Set the FCFS managers at all intersections.
     *
     * @param layout       the map
     * @param currentTime  the current time
     * @param config       the reservation grid manager configuration
     */
    public static void setFCFSManagers(GridIntersectionMap layout,
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
     * Set the bath managers at all intersections.
     *
     * @param layout              the map
     * @param currentTime         the current time
     * @param config              the reservation grid manager configuration
     * @param processingInterval  the processing interval
     */
    public static void setBatchManagers(GridIntersectionMap layout,
                                        double currentTime,
                                        ReservationGridManager.Config config,
                                        double processingInterval) {
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
                RequestHandler rh =
                        new BatchModeRequestHandler(
                                new RoadBasedReordering(processingInterval),
                                new BatchModeRequestHandler.RequestStatCollector());
                im.setPolicy(new BasePolicy(im, rh));
                layout.setManager(column, row, im);
            }
        }
    }


    /**
     * Set the approximate simple traffic light managers at all intersections.
     *
     * @param layout               the map
     * @param currentTime          the current time
     * @param config               the reservation grid manager configuration
     * @param greenLightDuration   the green light duration
     * @param yellowLightDuration  the yellow light duration
     */
    public static void setApproxSimpleTrafficLightManagers(
            GridIntersectionMap layout,
            double currentTime,
            ReservationGridManager.Config config,
            double greenLightDuration,
            double yellowLightDuration) {

        layout.removeAllManagers();
        for (int column = 0; column < layout.getColumns(); column++) {
            for (int row = 0; row < layout.getRows(); row++) {
                List<Road> roads = layout.getRoads(column, row);
                RoadBasedIntersection intersection = new RoadBasedIntersection(roads);
                RoadBasedTrackModel trajectoryModel =
                        new RoadBasedTrackModel(intersection);
                V2IManager im =
                        new V2IManager(intersection, trajectoryModel, currentTime,
                                config, layout.getImRegistry());
                ApproxSimpleTrafficSignalRequestHandler requestHandler =
                        new ApproxSimpleTrafficSignalRequestHandler(greenLightDuration,
                                yellowLightDuration);
                im.setPolicy(new BasePolicy(im, requestHandler));
                layout.setManager(column, row, im);
            }
        }
    }


    /**
     * Set the approximate 4 phases traffic light managers at all intersections.
     *
     * @param layout               the map
     * @param currentTime          the current time
     * @param config               the reservation grid manager configuration
     * @param greenLightDuration   the green light duration
     * @param yellowLightDuration  the yellow light duration
     */
    public static void setApprox4PhasesTrafficLightManagers(
            GridIntersectionMap layout,
            double currentTime,
            ReservationGridManager.Config config,
            double greenLightDuration,
            double yellowLightDuration) {
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
                Approx4PhasesTrafficSignalRequestHandler requestHandler =
                        new Approx4PhasesTrafficSignalRequestHandler(greenLightDuration,
                                yellowLightDuration);
                im.setPolicy(new BasePolicy(im, requestHandler));
                layout.setManager(column, row, im);
            }
        }
    }

    /**
     * Set the approximate N phases traffic light managers at all intersections.
     *
     * @param layout                      the map
     * @param currentTime                 the current time
     * @param config                      the reservation grid manager
     *                                    configuration
     * @param trafficSignalPhaseFileName  the name of the file contains the
     *                                    traffic signals duration information
     */
    public static void setApproxNPhasesTrafficLightManagers(
            GridIntersectionMap layout,
            double currentTime,
            ReservationGridManager.Config config,
            String trafficSignalPhaseFileName) {

        layout.removeAllManagers();
        for (int column = 0; column < layout.getColumns(); column++) {
            for (int row = 0; row < layout.getRows(); row++) {
                List<Road> roads = layout.getRoads(column, row);
                RoadBasedIntersection intersection = new RoadBasedIntersection(roads);
                RoadBasedTrackModel trajectoryModel =
                        new RoadBasedTrackModel(intersection);
                V2IManager im =
                        new V2IManager(intersection, trajectoryModel, currentTime,
                                config, layout.getImRegistry());
                ApproxNPhasesTrafficSignalRequestHandler requestHandler =
                        new ApproxNPhasesTrafficSignalRequestHandler();

                TrafficSignalPhase phase =
                        TrafficSignalPhase.makeFromFile(layout, trafficSignalPhaseFileName);

                for(Road road : im.getIntersection().getEntryRoads()) {
                    for(Lane lane : road.getLanes()) {
                        CyclicSignalController controller =
                                phase.calcCyclicSignalController(road);
                        requestHandler.setSignalControllers(lane.getId(), controller);
                    }
                }

                im.setPolicy(new BasePolicy(im, requestHandler));
                layout.setManager(column, row, im);
            }
        }
    }

    /**
     * Set the approximate N phases traffic light managers at all intersections.
     *
     * @param layout       the map
     * @param currentTime  the current time
     * @param config       the reservation grid manager configuration
     */
    public static void setApproxStopSignManagers(GridIntersectionMap layout,
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
                ApproxStopSignRequestHandler requestHandler =
                        new ApproxStopSignRequestHandler();
                im.setPolicy(new BasePolicy(im, requestHandler));
                layout.setManager(column, row, im);
            }
        }
    }

    /**
     * Set the uniform random spawn points.
     *
     * @param map           the map
     * @param trafficLevel  the traffic level
     */
    public static void setUniformRandomSpawnPoints(final GridIntersectionMap map, //TODO: Remove finality
                                                   double trafficLevel) {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
                sp.setVehicleSpecChooser(
                        new UniformSpawnSpecGenerator(trafficLevel,new RandomDestinationSelector(map)));
        }
    }

    /**
     * Set the uniform turn based spawn points.
     *
     * @param map           the map
     * @param trafficLevel  the traffic level
     */
    public static void setUniformTurnBasedSpawnPoints(GridIntersectionMap map,
                                                      double trafficLevel) {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new UniformSpawnSpecGenerator(trafficLevel,
                            new TurnBasedDestinationSelector(map)));
        }
    }

    /**
     * Set the uniform ratio spawn points with various traffic volume.
     *
     * @param map                    the map
     * @param trafficVolumeFileName  the traffic volume filename
     */
    public static void setUniformRatioSpawnPoints(GridIntersectionMap map,
                                                  String trafficVolumeFileName) {

        TrafficVolume trafficVolume =
                TrafficVolume.makeFromFile(map, trafficVolumeFileName);

        DestinationSelector selector = new RatioDestinationSelector(map,
                trafficVolume);

        for (AIMSpawnPoint sp : map.getSpawnPoints()) {
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
     * Set the directional spawn points which has different traffic volumes
     * in different directions.
     *
     * @param layout         the map
     * @param hTrafficLevel  the traffic level in the horizontal direction
     * @param vTrafficLevel  the traffic level in the vertical direction
     */
    public static void setDirectionalSpawnPoints(GridIntersectionMap layout,
                                                 double hTrafficLevel,
                                                 double vTrafficLevel) {
        for(AIMSpawnPoint sp : layout.getHorizontalSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new UniformSpawnSpecGenerator(hTrafficLevel,
                            new RandomDestinationSelector(layout)));
        }
        for(AIMSpawnPoint sp : layout.getVerticalSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new UniformSpawnSpecGenerator(vTrafficLevel,
                            new RandomDestinationSelector(layout)));
        }
    }

    /**
     * Set the baseline spawn points.
     *
     * @param layout          the map
     * @param traversalTime   the traversal time
     */
    public static void setBaselineSpawnPoints(GridIntersectionMap layout,
                                              double traversalTime) {
        int totalNumOfLanes = 0;
        int minNumOfLanes = Integer.MAX_VALUE;
        for(Road r : layout.getRoads()) {
            int n = r.getLanes().size();
            totalNumOfLanes += n;
            if (n < minNumOfLanes) {
                minNumOfLanes = n;
            }
        }
        double numOfTraversals =
                VehicleSpecDatabase.getNumOfSpec() * (totalNumOfLanes - minNumOfLanes);

        for(AIMSpawnPoint sp : layout.getSpawnPoints()) {
            sp.setVehicleSpecChooser(
                    new EnumerateSpawnSpecGenerator(
                            sp,
                            layout.getDestinationRoads(),
                            sp.getLane().getId() * traversalTime * numOfTraversals,
                            traversalTime));
        }
    }

    public static void setJSONScheduleSpawnSpecGenerator(GridIntersectionMap map, File mergeSchedule, File targetSchedule) throws IOException, ParseException {
        for(AIMSpawnPoint sp : map.getSpawnPoints()) {
            if(sp.getHeading() == 0) {
                Road targetRoad = null;
                for(Road r : map.getDestinationRoads())
                    if(r.getIndexLane().getInitialHeading() == 0)
                        targetRoad = r;
                assert targetRoad != null;
                sp.setVehicleSpecChooser(
                        new JsonScheduleSpawnSpecGenerator(
                                targetSchedule,
                                targetRoad
                        ));
            } else if(sp.getHeading() == Math.PI + Math.PI/2) {
                Road mergeRoad = null;
                for(Road r : map.getDestinationRoads())
                    if(r.getIndexLane().getInitialHeading() == 0)
                        mergeRoad = r;
                assert mergeRoad != null;
                sp.setVehicleSpecChooser(
                        new JsonScheduleSpawnSpecGenerator(
                                mergeSchedule,
                                mergeRoad
                        ));
            } else  {
                sp.setVehicleSpecChooser(new AIMSpawnSpecGenerator() {
                    @Override
                    public List<AIMSpawnSpec> act(AIMSpawnPoint spawnPoint, double timeStep) {
                        return new ArrayList<AIMSpawnSpec>();
                    }
                });
            }
        }
    }
}
