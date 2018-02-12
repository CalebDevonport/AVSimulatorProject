package aim4.driver.rim;

import aim4.driver.BasicDriver;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;

public abstract class RimDriver extends BasicDriver implements RimDriverSimModel {
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    // origin and destination

    /** Where this DriverAgent is coming from. */
    private AIMSpawnPoint spawnPoint;

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
    public AIMSpawnPoint getSpawnPoint() {
        if(spawnPoint == null) {
            throw new RuntimeException("Driver is without origin!");
        }
        return spawnPoint;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnPoint(AIMSpawnPoint spawnPoint) {
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

}