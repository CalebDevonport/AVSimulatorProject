package aim4.driver.rim.navigator;

import aim4.driver.rim.RIMAutoDriver;
import aim4.im.rim.IntersectionManager;
import aim4.map.Road;

/**
 * An agent that chooses which way a vehicle should go, and uses information
 * from a {@link RIMAutoDriver} to do so.
 */
public interface Navigator {
    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Given the current Road, the IntersectionManager being approached, and
     * a destination Road, find a road that leave the IntersectionManager that
     * will lead to the destination Road.
     *
     * @param current     the Road on which the vehicle is currently traveling
     * @param im          the IntersectionManager the vehicle is approaching
     * @param destination the Road on which the vehicle would ultimately like to
     *                    end up
     * @return            a road to take out of the intersection governed by
     *                    the given IntersectionManager
     */
    Road navigate(Road current, IntersectionManager im, Road destination);
}
