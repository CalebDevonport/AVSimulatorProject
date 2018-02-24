package aim4.vehicle.rim;

import aim4.msg.rim.v2i.V2IMessage;
import aim4.vehicle.VehicleSimModel;

import java.util.Queue;

/**
 * The interface of a vehicle from the viewpoint of a simulator.
 */
public interface RIMVehicleSimModel extends VehicleSimModel, aim4.vehicle.rim.ResultsEnabledVehicle {
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
