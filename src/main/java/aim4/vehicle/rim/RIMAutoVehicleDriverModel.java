package aim4.vehicle.rim;

import aim4.driver.rim.RIMAutoDriver;
import aim4.msg.rim.i2v.I2VMessage;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.vehicle.AutoVehicleDriverModel;

import java.util.List;

public interface RIMAutoVehicleDriverModel extends AutoVehicleDriverModel{
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

    @Override
    RIMAutoDriver getDriver();

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // debug

    /**
     * Get the last V2I message
     *
     * @return the last V2I message
     */
    V2IMessage getLastV2IMessage();

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // communications system

    /**
     * Get the Vehicle's transmission power.
     *
     * @return the Vehicle's transmission power, in meters
     */
    double getTransmissionPower();

    // communications systems (statistics)

    /**
     * Get the number of bits this Vehicle has received.
     *
     * @return the number of bits this Vehicle has received
     */
    int getBitsReceived();

    /**
     * Get the number of bits this Vehicle has transmitted.
     *
     * @return the number of bits this Vehicle has transmitted
     */
    int getBitsTransmitted();

    void setPrecedingVehicleVIN(int vin);

    int getPrecedingVehicleVIN();
}
