package aim4.vehicle.rim;

import aim4.msg.aim.v2i.V2IMessage;
import aim4.vehicle.ResultsEnabledVehicle;
import aim4.vehicle.VehicleSimModel;

import java.util.Queue;

/**
 * The interface of a vehicle from the viewpoint of a simulator.
 */
public interface RimVehicleSimModel extends VehicleSimModel, ResultsEnabledVehicle {
    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // messaging

    /**
     * Get the queue of V2I messages waiting to be delivered from this
     * Vehicle.
     *
     * @return the queue of V2I messages to be delivered from this Vehicle
     */
    Queue<V2IMessage> getV2IOutbox();
}
