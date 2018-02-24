package aim4.sim.setup.aim;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.aim.pilot.V2IPilot;
import aim4.im.aim.v2i.reservation.ReservationGridManager;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;
import aim4.map.aim.GridAIMIntersectionMap;
import aim4.map.aim.GridMapUtil;
import aim4.sim.simulator.aim.AIMSimulator;
import aim4.sim.simulator.aim.AutoDriverOnlySimulator;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.aim.AIMVehicleSimModel;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Callum on 21/04/2017.
 */
public class MergeMimicSimSetup implements AIMSimSetup {
    // Settings from AutoDriverOnlySimSetup //
    /** The static buffer size */
    private double staticBufferSize = 0.25;
    /** The time buffer for internal tiles */
    private double internalTileTimeBufferSize = 0.1;
    /** The time buffer for edge tiles */
    private double edgeTileTimeBufferSize = 0.25;
    /** Whether the edge time buffer is enabled */
    private boolean isEdgeTileTimeBufferEnabled = true;
    /** The granularity of the reservation grid */
    private double granularity = 1.0;

    private static final double LANE_WIDTH = 4;
    private static final double DEFAULT_DISTANCE_BETWEEN = 150;
    private static final double MEDIAN_SIZE = 1;

    private File mergeSchedule;
    private File targetSchedule;
    private double speedLimit;
    private double leadInDistance;

    public MergeMimicSimSetup(File mergeSchedule, File targetSchedule, double speedLimit, double leadInDistance) {
        this.mergeSchedule = mergeSchedule;
        this.targetSchedule = targetSchedule;
        this.speedLimit = speedLimit;
        this.leadInDistance = leadInDistance;
    }

    public MergeMimicSimSetup(File mergeSchedule, File targetSchedule, double speedLimit) {
        this.mergeSchedule = mergeSchedule;
        this.targetSchedule = targetSchedule;
        this.speedLimit = speedLimit;
        this.leadInDistance = DEFAULT_DISTANCE_BETWEEN;
    }

    @Override
    public void setTrafficLevel(double trafficLevel) {
    }

    @Override
    public void setStopDistBeforeIntersection(double stopDistBeforeIntersection) {
    }

    @Override
    public AIMSimulator getSimulator() {
        double currentTime = 0.0;
        GridAIMIntersectionMap layout = new GridAIMIntersectionMap(
                currentTime, //Current time
                1, //Columns
                1, //Rows
                LANE_WIDTH, //Lane width
                speedLimit, //Speed limit
                1, //Lanes per road
                MEDIAN_SIZE, //Width between roads on same side
                leadInDistance //Distance between intersections
        );
        ReservationGridManager.Config gridConfig =
                new ReservationGridManager.Config(SimConfig.TIME_STEP,
                        SimConfig.GRID_TIME_STEP,
                        staticBufferSize,
                        internalTileTimeBufferSize,
                        edgeTileTimeBufferSize,
                        isEdgeTileTimeBufferEnabled,
                        granularity);  // granularity
        Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = true;

        try {
            GridMapUtil.setJSONScheduleSpawnSpecGenerator(layout, mergeSchedule, targetSchedule);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
        GridMapUtil.setFCFSManagers(layout, currentTime, gridConfig);

        V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION = 1.0;

        Map<String, Double> specToExpectedTimeMergeLane = simulateExpectedMergeLaneTimes(layout);
        Map<String, Double> specToExpectedTimeTargetLane = simulateExpectedTargetLaneTimes(layout);
        Debug.currentAimMap = layout;

        return new AutoDriverOnlySimulator(layout, true, specToExpectedTimeMergeLane,specToExpectedTimeTargetLane);
    }

    private Map<String, Double> simulateExpectedMergeLaneTimes(GridAIMIntersectionMap mapOriginal) {
        Map<String, Double> specToExpectedTime = new HashMap<String, Double>();
        for(int specID = 0; specID < VehicleSpecDatabase.getNumOfSpec(); specID++) {
            GridAIMIntersectionMap map = new GridAIMIntersectionMap(mapOriginal, LANE_WIDTH, MEDIAN_SIZE, DEFAULT_DISTANCE_BETWEEN);
            VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecById(specID);
            Road destination = null;
            for(Road r : map.getDestinationRoads())
                if(r.getIndexLane().getInitialHeading() == 0 || r.getIndexLane().getInitialHeading() == 2*Math.PI) {
                    destination = r;
                    break;
                }
            for(AIMSpawnPoint spawn : map.getSpawnPoints()) {
                if(spawn.getHeading() == 2*Math.PI - Math.PI/2)
                    spawn.setVehicleSpecChooser(new GridMapUtil.SingleSpawnSpecGenerator(destination, spec));
                else
                    spawn.setVehicleSpecChooser(GridMapUtil.nullSpawnSpecGenerator);
            }
            AutoDriverOnlySimulator sim = new AutoDriverOnlySimulator(map, true);
            ReservationGridManager.Config gridConfig =
                    new ReservationGridManager.Config(SimConfig.TIME_STEP,
                            SimConfig.GRID_TIME_STEP,
                            staticBufferSize,
                            internalTileTimeBufferSize,
                            edgeTileTimeBufferSize,
                            isEdgeTileTimeBufferEnabled,
                            granularity);  // granularity
            GridMapUtil.setFCFSManagers(map, 0, gridConfig);
            AutoDriverOnlySimulator.AutoDriverOnlySimStepResult simStepResult =
                    new AutoDriverOnlySimulator.AutoDriverOnlySimStepResult(new ArrayList<Integer>());
            AIMVehicleSimModel vehicle = null;
            while(simStepResult.getCompletedVINs().size() < 1) {
                simStepResult = sim.step(SimConfig.TIME_STEP);
                if(vehicle == null && sim.getActiveVehicles().size() > 0)
                    for(AIMVehicleSimModel v : sim.getActiveVehicles())
                        vehicle = v;
            }
            assert vehicle != null;
            specToExpectedTime.put(spec.getName(),vehicle.getFinishTime() - vehicle.getStartTime());
        }
        return specToExpectedTime;
    }

    private Map<String, Double> simulateExpectedTargetLaneTimes(GridAIMIntersectionMap mapOriginal) {
        Map<String, Double> specToExpectedTime = new HashMap<String, Double>();
        for(int specID = 0; specID < VehicleSpecDatabase.getNumOfSpec(); specID++) {
            GridAIMIntersectionMap map = new GridAIMIntersectionMap(mapOriginal, LANE_WIDTH, MEDIAN_SIZE, DEFAULT_DISTANCE_BETWEEN);
            VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecById(specID);
            Road destination = null;
            for(Road r : map.getDestinationRoads())
                if(r.getIndexLane().getInitialHeading() == 0 || r.getIndexLane().getInitialHeading() == 2*Math.PI) {
                    destination = r;
                    break;
                }
            for(AIMSpawnPoint spawn : map.getSpawnPoints()) {
                if(spawn.getHeading() == 0)
                    spawn.setVehicleSpecChooser(new GridMapUtil.SingleSpawnSpecGenerator(destination, spec));
                else
                    spawn.setVehicleSpecChooser(GridMapUtil.nullSpawnSpecGenerator);
            }
            AutoDriverOnlySimulator sim = new AutoDriverOnlySimulator(map, true);
            ReservationGridManager.Config gridConfig =
                    new ReservationGridManager.Config(SimConfig.TIME_STEP,
                            SimConfig.GRID_TIME_STEP,
                            staticBufferSize,
                            internalTileTimeBufferSize,
                            edgeTileTimeBufferSize,
                            isEdgeTileTimeBufferEnabled,
                            granularity);  // granularity
            GridMapUtil.setFCFSManagers(map, 0, gridConfig);
            AutoDriverOnlySimulator.AutoDriverOnlySimStepResult simStepResult =
                    new AutoDriverOnlySimulator.AutoDriverOnlySimStepResult(new ArrayList<Integer>());
            AIMVehicleSimModel vehicle = null;
            while(simStepResult.getCompletedVINs().size() < 1) {
                simStepResult = sim.step(SimConfig.TIME_STEP);
                if(vehicle == null && sim.getActiveVehicles().size() > 0)
                    for(AIMVehicleSimModel v : sim.getActiveVehicles())
                        vehicle = v;
            }
            assert vehicle != null;
            specToExpectedTime.put(spec.getName(),vehicle.getFinishTime() - vehicle.getStartTime());
        }
        return specToExpectedTime;
    }
}
