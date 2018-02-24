package aim4.im.rim.v2i.policy;

import aim4.im.rim.v2i.V2IManagerCallback;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.sim.StatCollector;

/**
 * An interface for intersection control policies for V2IManagers.
 */
public interface Policy {
    /**
     * Set the V2I manager call-back.
     *
     * @param im  the V2I manager's call-back
     */
    void setV2IManagerCallback(V2IManagerCallback im);

    /**
     * Give the policy a chance to do any processing it might need to do in
     * order to respond to requests, if it hasn't responded to them already.
     * Only used for policies that don't respond immediately to requests.
     *
     * @param timeStep  the size of the time step to simulate, in seconds
     */
    void act(double timeStep);

    /**
     * Process a V2I message
     *
     * @param msg  the V2I message
     */
    void processV2IMessage(V2IMessage msg);

    /**
     * Get the statistic collector.
     *
     * @return the statistic collector
     */
    StatCollector<?> getStatCollector();
}
