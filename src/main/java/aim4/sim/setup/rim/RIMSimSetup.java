package aim4.sim.setup.rim;

import aim4.sim.setup.SimSetup;

public interface RIMSimSetup extends SimSetup {
    /**
     * Set the traffic level.
     *
     * @param trafficLevel  the traffic level
     */
    void setTrafficLevel(double trafficLevel);

    /**
     * Set the stopping distance before intersection.
     *
     * @param stopDistBeforeIntersection  the stopping distance
     */
    void setStopDistBeforeIntersection(double stopDistBeforeIntersection);
}
