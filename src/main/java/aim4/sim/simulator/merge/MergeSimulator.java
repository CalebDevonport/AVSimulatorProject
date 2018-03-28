package aim4.sim.simulator.merge;

import aim4.sim.Simulator;
import aim4.sim.results.CoreMergeResult;
import aim4.vehicle.merge.MergeVehicleSimModel;

import java.util.Map;

/**
 * Created by Callum on 13/03/2017.
 */
public interface MergeSimulator extends Simulator {
    public Map<Integer, MergeVehicleSimModel> getVinToVehicles();
    public CoreMergeResult produceMergeResult();
}
