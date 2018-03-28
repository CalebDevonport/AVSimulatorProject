package aim4.sim.simulator.aim;

import aim4.map.BasicAIMIntersectionMap;
import aim4.sim.Simulator;
import aim4.sim.results.MergeResult;
import aim4.sim.results.Result;
import aim4.vehicle.aim.AIMVehicleSimModel;
import aim4.vehicle.aim.ProxyVehicleSimModel;

import java.util.Set;

/**
 * Created by Callum on 28/11/2016.
 */
public interface AIMSimulator extends Simulator {
    /**
     * Get the set of all active vehicles in the simulation.
     *
     * @return the set of all active vehicles in the simulation
     */
    Set<AIMVehicleSimModel> getActiveVehicles();

    @Override
    BasicAIMIntersectionMap getMap();

    /**
     * Add the proxy vehicle to the simulator for the mixed reality experiments.
     *
     * @param vehicle  the proxy vehicle
     */
    void addProxyVehicle(ProxyVehicleSimModel vehicle);

    MergeResult produceMergeResult();

    Result produceResult();
}
