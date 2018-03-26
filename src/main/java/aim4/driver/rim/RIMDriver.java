package aim4.driver.rim;

import aim4.driver.BasicDriver;
import aim4.im.rim.IntersectionManager;
import aim4.map.Road;
import aim4.map.rim.RIMSpawnPoint;

public abstract class RIMDriver extends BasicDriver implements RIMDriverSimModel {
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    // origin and destination

    /** Where this DriverAgent is coming from. */
    private RIMSpawnPoint spawnPoint;

    /** Where this DriverAgent is headed. */
    private Road destination;

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // origin and destination

    /**
     * Get where this DriverAgent is coming from.
     *
     * @return the Road where this DriverAgent is coming from
     */
    @Override
    public RIMSpawnPoint getSpawnPoint() {
        if(spawnPoint == null) {
            throw new RuntimeException("Driver is without origin!");
        }
        return spawnPoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnPoint(RIMSpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Road getDestination() {
        if(destination == null) {
            throw new RuntimeException("Driver is without destination!");
        }
        return destination;
    }

    /**
     * Set where this driver agent is going.
     *
     * @param destination the Road where this DriverAgent should go
     */
    @Override
    public void setDestination(Road destination) {
        this.destination = destination;
    }

    /////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////

    // IM

    /**
     * Find the next IntersectionManager that the Vehicle will need to
     * interact with, in this Lane.
     *
     * @return the nextIntersectionManager that the Vehicle will need
     *         to interact with, in this Lane
     */
    protected IntersectionManager nextIntersectionManager() {
        return getCurrentLane().getLaneRIM().
                nextIntersectionManager(getVehicle().gaugePosition());
    }

    /**
     * Find the distance to the next intersection in the Lane in which
     * the Vehicle is, from the position at which the Vehicle is.
     *
     * @return the distance to the next intersection given the current Lane
     *         and position of the Vehicle.
     */
    protected double distanceToNextIntersection() {
        return getCurrentLane().getLaneRIM().
                distanceToNextIntersection(getVehicle().gaugePosition());
    }

    /**
     * Find the distance from the previous intersection in the Lane in which
     * the Vehicle is, from the position at which the Vehicle is.  This
     * subtracts the length of the Vehicle from the distance from the front
     * of the Vehicle.
     *
     * @return the distance from the previous intersection given the current
     *         Lane and position of the Vehicle.
     */
    protected double distanceFromPrevIntersection() {
        double d = getCurrentLane().getLaneRIM().
                distanceFromPrevIntersection(getVehicle().gaugePosition());
        return Math.max(0.0, d - getVehicle().getSpec().getLength());
    }


}