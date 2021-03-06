package aim4.vehicle.merge;

import aim4.driver.merge.MergeDriver;
import aim4.vehicle.ResultsEnabledVehicle;
import aim4.vehicle.VehicleSimModel;

/**
 * Created by Callum on 13/03/2017.
 */
public interface MergeVehicleSimModel extends VehicleSimModel, ResultsEnabledVehicle {
    MergeDriver getDriver();
}
