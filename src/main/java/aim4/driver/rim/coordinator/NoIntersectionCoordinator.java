package aim4.driver.rim.coordinator;

import aim4.driver.Coordinator;
import aim4.driver.rim.RIMAutoDriver;
import aim4.driver.rim.pilot.V2IPilot;
import aim4.vehicle.rim.RIMAutoVehicleDriverModel;

/**
 * The coordinator when there is no intersection.
 */
public class NoIntersectionCoordinator implements Coordinator{
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    // vehicle and agents

    /**
     * The sub-agent that controls physical manipulation of the vehicle
     */
    private V2IPilot pilot;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create an coordinator to coordinate a vehicle.
     *
     * @param vehicle  the Vehicle to coordinate
     * @param driver   the driver
     */
    public NoIntersectionCoordinator(RIMAutoVehicleDriverModel vehicle,
                                     RIMAutoDriver driver) {
        pilot = new V2IPilot(vehicle, driver);
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void act() {
        pilot.simpleThrottleAction();
        // TODO:  think how to remove dontEnterIntersection()
        // in simpleThrottleAction()
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isTerminated() {
        return false;
    }
}
