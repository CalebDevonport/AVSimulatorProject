package aim4.vehicle.rim;

import aim4.msg.aim.i2v.I2VMessage;
import aim4.msg.aim.v2i.V2IMessage;

import java.util.List;

public interface RrimAutoVehicleDriverModel {
    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // communications systems (V2I)

    /**
     * Get the list of all messages currently in the queue of I2V messages
     * waiting to be read by this Vehicle.
     *
     * @return the list of all messages currently in the queue of I2V messages.
     */
    List<I2VMessage> pollAllMessagesFromI2VInbox();

    /**
     * Adds a message to the outgoing queue of messages to be delivered to an
     * IntersectionManager.
     *
     * @param msg the message to send to an IntersectionManager
     */
    void send(V2IMessage msg);

    /**
     * Adds a message to the incoming queue of messages received from
     * IntersectionManagers.
     *
     * @param msg the message to send to another Vehicle
     */
    void receive(I2VMessage msg);
}
