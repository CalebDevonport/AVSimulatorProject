package aim4.driver.rim;

import aim4.driver.BasicDriver;
import aim4.map.aim.AIMSpawnPoint;

public abstract class RimDriver extends BasicDriver implements RimDriverSimModel {
    private AIMSpawnPoint spawnPoint;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSpawnPoint(AIMSpawnPoint spawnPoint) {
        this.spawnPoint = spawnPoint;
    }
}