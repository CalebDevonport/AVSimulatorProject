package aim4.driver.rim;

import aim4.driver.DriverSimModel;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;

public interface RimDriverSimModel extends DriverSimModel {
    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // origin and destination

    /**
     * Get where this driver is coming from.
     *
     * @return the Road where this driver is coming from
     */
    AIMSpawnPoint getSpawnPoint();

    /**
     * Set where this driver agent is coming from.
     *
     * @param spawnPoint the spawn point that generated the driver
     */
    void setSpawnPoint(AIMSpawnPoint spawnPoint);

    /**
     * Get where this driver is going.
     *
     * @return the Road where this driver is going
     */
    Road getDestination();

    /**
     * Set where this driver is going.
     *
     * @param destination the Road where this driver should go
     */
    void setDestination(Road destination);
}