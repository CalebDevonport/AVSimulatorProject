package aim4.sim.simulator.rim.helper;

import aim4.driver.rim.RIMAutoDriver;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.lane.Lane;
import aim4.map.rim.RIMSpawnPoint;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VinRegistry;
import aim4.vehicle.rim.RIMAutoVehicleSimModel;
import aim4.vehicle.rim.RIMBasicAutoVehicle;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SpawnHelper {
    private BasicRIMIntersectionMap map;
    private Map<Integer, RIMVehicleSimModel> vinToVehicles;

    public SpawnHelper(BasicRIMIntersectionMap map, Map<Integer, RIMVehicleSimModel> vinToVehicles){
        this.map = map;
        this.vinToVehicles = vinToVehicles;
    }

    /**
     * Spawns vehicles
     * @param timeStep The time step
     * @return A List of the Vehicles spawned. Null if no vehicles spawned.
     */
    public List<RIMVehicleSimModel> spawnVehicles(double timeStep) {
        for(RIMSpawnPoint spawnPoint : map.getSpawnPoints()) {
            List<RIMSpawnPoint.RIMSpawnSpec> spawnSpecs = spawnPoint.act(timeStep);
            if(!spawnSpecs.isEmpty()){
                if(canSpawnVehicle(spawnPoint)) {
                    List<RIMVehicleSimModel> spawnedVehicles = new ArrayList<RIMVehicleSimModel>();
                    for(RIMSpawnPoint.RIMSpawnSpec spawnSpec : spawnSpecs) {
                        RIMVehicleSimModel vehicle = setupVehicle(spawnPoint, spawnSpec);
                        VinRegistry.registerVehicle(vehicle);
                        vinToVehicles.put(vehicle.getVIN(), vehicle);
                        spawnedVehicles.add(vehicle);
                        break; // only handle the first spawn vehicle
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
    public boolean canSpawnVehicle(RIMSpawnPoint spawnPoint) {
        assert spawnPoint.getNoVehicleZone() instanceof Rectangle2D;
        Rectangle2D noVehicleZone = (Rectangle2D) spawnPoint.getNoVehicleZone();
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
            RIMSpawnPoint.RIMSpawnSpec spawnSpec) {
        VehicleSpec spec = spawnSpec.getVehicleSpec();
        Lane lane = spawnPoint.getLane();
        double initVelocity = Math.min(spec.getMaxVelocity(), lane.getSpeedLimit());
        return makeAutoVehicle(spawnPoint,initVelocity,lane,spec,spawnSpec);
    }

    private RIMVehicleSimModel  makeAutoVehicle(
            RIMSpawnPoint spawnPoint,
            double initVelocity, Lane lane,
            VehicleSpec spec,
            RIMSpawnPoint.RIMSpawnSpec spawnSpec) {
        RIMAutoVehicleSimModel vehicle =
                new RIMBasicAutoVehicle(spec,
                        spawnPoint.getPosition(),
                        spawnPoint.getHeading(),
                        spawnPoint.getSteeringAngle(),
                        initVelocity,
                        initVelocity,
                        spawnPoint.getAcceleration(),
                        spawnSpec.getSpawnTime());
        vehicle.setStartTime(spawnPoint.getCurrentTime());
        vehicle.setMaxVelocity(initVelocity);
        vehicle.setMinVelocity(initVelocity);

        RIMAutoDriver driver = new RIMAutoDriver(vehicle, map);
        driver.setDestination(spawnSpec.getDestinationRoad());
        driver.setCurrentLane(lane);
        driver.setSpawnPoint(spawnPoint);
        vehicle.setDriver(driver);

        return vehicle;
    }
}
