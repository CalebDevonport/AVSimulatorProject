package aim4.vehicle.rim;

import aim4.driver.rim.RimDriver;
import aim4.vehicle.ResultsEnabledVehicle;
import aim4.vehicle.VehicleSimModel;

public interface RimVehicleSimModel extends VehicleSimModel, ResultsEnabledVehicle {
    RimDriver getDriver();
}
