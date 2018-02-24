package aim4.sim.simulator.rim.helper;

import aim4.map.lane.Lane;
import aim4.map.rim.RIMSpawnPoint;
import aim4.map.rim.RimIntersectionMap;
import aim4.sim.setup.rim.enums.ProtocolType;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnHelper {
    private RimIntersectionMap map;
    private Map<Integer, RIMVehicleSimModel> vinToVehicles;

    public SpawnHelper(RimIntersectionMap map, Map<Integer, RIMVehicleSimModel> vinToVehicles){
        this.map = map;
        this.vinToVehicles = vinToVehicles;
    }

    /**
     * Spawns vehicles
     * @param timeStep The time step
     * @param protocolType The protocol type for the given simulation
     * @return A List of the Vehicles spawned. Null if no vehicles spawned.
     */
    public List<RIMVehicleSimModel> spawnVehicles(double timeStep, ProtocolType protocolType) {
        for(RIMSpawnPoint spawnPoint : map.getSpawnPoints()) {
            List<RIMSpawnPoint.RIMSpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
            if(!spawnSpecs.isEmpty()){
                if(canSpawnVehicle(spawnPoint)) {
                    List<RIMVehicleSimModel> spawnedVehicles = new ArrayList<RIMVehicleSimModel>();
                    for(RIMSpawnPoint.RIMSpawnSpec spawnSpec : spawnSpecs) {
                        RIMVehicleSimModel vehicle = setupVehicle(spawnPoint, spawnSpec, protocolType);
                        VinRegistry.registerVehicle(vehicle);
                        vinToVehicles.put(vehicle.getVIN(), vehicle);
                        spawnedVehicles.add(vehicle);
                        if(!canSpawnVehicle(spawnPoint))
                            break;
                    }
                    return spawnedVehicles;
                }
            }
        }
        return null;
    }

    /**
     * Checks if the spawn point can spawn a vehicle, based on the size of it's no spawn zone.
     * @param spawnPoint
     * @return
     */
    private boolean canSpawnVehicle(RIMSpawnPoint spawnPoint) {
        assert spawnPoint.getNoVehicleZone() instanceof Path2D;
        Path2D noVehicleZone = (Path2D) spawnPoint.getNoVehicleZone();
        for(RIMVehicleSimModel vehicle : vinToVehicles.values()) {
            if (noVehicleZone.intersects(vehicle.getShape().getBounds2D())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Creates a vehicle at the spawn point.
     * @param spawnPoint
     * @param spawnSpec
     * @return
     */
    private RIMVehicleSimModel setupVehicle(
            RIMSpawnPoint spawnPoint,
            RIMSpawnPoint.RIMSpawnSpec spawnSpec,
            ProtocolType protocolType) {
        VehicleSpec spec = spawnSpec.getVehicleSpec();
        Lane lane = spawnPoint.getLane();
        double initVelocity = Math.min(spec.getMaxVelocity(), lane.getSpeedLimit());

//        switch(protocolType){
//            case AIM_GRID:
//                return makeV2IVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec,protocolType);
//            case AIM_NO_GRID:
//                return makeV2IVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec,protocolType);
//            case QUEUE:
//                return makeV2IVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec,protocolType);
//            case TEST_MERGE:
//                return makeAutoVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec);
//            case TEST_TARGET:
//                return makeAutoVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec);
//            case NONE:
//                return makeAutoVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec);
//            default:
//                throw new UnsupportedOperationException(String.format(
//                        "ProtocolType %s not supported",
//                        protocolType.toString())
//                );
//        }
        return null;
    }

//    private RIMVehicleSimModel  makeAutoVehicle(
//            AIMSpawnPoint spawnPoint,
//            double initVelocity, Lane lane,
//            VehicleSpec spec,
//            AIMSpawnPoint.AIMSpawnSpec spawnSpec) {
//        MergeAutoVehicleSimModel vehicle =
//                new MergeBasicAutoVehicle(spec,
//                        spawnPoint.getPosition(),
//                        spawnPoint.getHeading(),
//                        initVelocity,
//                        spawnPoint.getSteeringAngle(),
//                        spawnPoint.getAcceleration(),
//                        lane.getSpeedLimit(),
//                        spawnSpec.getSpawnTime());
//        vehicle.setStartTime(spawnPoint.getCurrentTime());
//        vehicle.setMaxVelocity(initVelocity);
//        vehicle.setMinVelocity(initVelocity);
//        if(spawnPoint.getHeading() == 0)
//            vehicle.setStartingRoad(RoadNames.TARGET_ROAD);
//        else
//            vehicle.setStartingRoad(RoadNames.MERGING_ROAD);
//
//        MergeAutoDriver driver = new MergeAutoDriver(vehicle, map);
//        driver.setCurrentLane(lane);
//        driver.setSpawnPoint(spawnPoint);
//        vehicle.setDriver(driver);
//
//        return vehicle;
//    }

//    private MergeVehicleSimModel makeV2IVehicle(
//            MergeSpawnPoint spawnPoint,
//            double initVelocity, Lane lane,
//            VehicleSpec spec,
//            MergeSpawnPoint.MergeSpawnSpec spawnSpec,
//            ProtocolType protocolType) {
//        MergeV2IAutoVehicleSimModel vehicle =
//                new MergeV2IAutoVehicle(spec,
//                        spawnPoint.getPosition(),
//                        spawnPoint.getHeading(),
//                        initVelocity,
//                        spawnPoint.getSteeringAngle(),
//                        spawnPoint.getAcceleration(),
//                        lane.getSpeedLimit(),
//                        spawnSpec.getSpawnTime());
//        vehicle.setStartTime(spawnPoint.getCurrentTime());
//        vehicle.setMaxVelocity(initVelocity);
//        vehicle.setMinVelocity(initVelocity);
//        if(spawnPoint.getHeading() == 0)
//            vehicle.setStartingRoad(RoadNames.TARGET_ROAD);
//        else
//            vehicle.setStartingRoad(RoadNames.MERGING_ROAD);
//
//        MergeV2IAutoDriver driver = new MergeV2IAutoDriver(vehicle, map, protocolType);
//        driver.setCurrentLane(lane);
//        driver.setSpawnPoint(spawnPoint);
//        vehicle.setDriver(driver);
//
//        return vehicle;
//    }
}
