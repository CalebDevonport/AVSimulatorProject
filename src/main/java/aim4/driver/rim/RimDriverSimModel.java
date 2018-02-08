package aim4.driver.rim;

import aim4.driver.DriverSimModel;
import aim4.map.aim.AIMSpawnPoint;

public interface RimDriverSimModel extends DriverSimModel {
    /**
     * Sets where the driver agent is coming from
     * @param spawnPoint the spawn point that generated the driver.
     */
    void setSpawnPoint(AIMSpawnPoint spawnPoint);

    /**
     * Returns a String representing the state of the Driver.
     * @return
     */
    String getStateString();
}