package aim4.map;

import aim4.im.rim.IntersectionManager;
import aim4.map.rim.RIMSpawnPoint;
import aim4.util.Registry;

import java.util.List;

/**
 * Essentially a structured grouping of RIM Roads and IntersectionManagers that
 * allows a unified interface so that we can re-use certain layouts and
 * create classes of layouts.
 */
public interface BasicRIMIntersectionMap extends BasicMap{
    /**
     * Get the Roads that exit this Layout.
     *
     * @return the Roads exit this Layout
     */
    List<Road> getDestinationRoads();

    /**
     * Get the intersection manager registry.
     *
     * @return the intersection manager registry.
     */
    Registry<IntersectionManager> getImRegistry();

    /**
     * Get the IntersectionManagers that are part of this Layout.
     *
     * @return the IntersectionManagers that are part of this Layout
     */
    List<IntersectionManager> getIntersectionManagers();

    /**
     * Set the intersection manager of a particular intersection.
     *
     * @param column  the column of the intersection
     * @param row     the row of the intersection
     * @param im      the intersection manager
     */
    void setManager(int column, int row, IntersectionManager im);

    /**
     * Get the list of spawn points.
     *
     * @return the list of spawn points
     */
    List<RIMSpawnPoint> getSpawnPoints();
}
