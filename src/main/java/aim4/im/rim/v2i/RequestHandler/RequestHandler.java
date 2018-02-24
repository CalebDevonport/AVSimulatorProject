package aim4.im.rim.v2i.RequestHandler;

import aim4.im.rim.v2i.policy.BasePolicyCallback;
import aim4.msg.rim.v2i.Request;
import aim4.sim.StatCollector;

/**
 * The request handler.
 */
public interface RequestHandler {
    /**
     * Set the base policy call-back.
     *
     * @param basePolicy  the base policy's call-back
     */
    void setBasePolicyCallback(BasePolicyCallback basePolicy);

    /**
     * Let the request handler to act for a given time period.
     *
     * @param timeStep  the time period
     */
    void act(double timeStep);

    /**
     * Process the request message.
     *
     * @param msg the request message
     */
    void processRequestMsg(Request msg);


    /**
     * Get the statistic collector.
     *
     * @return the statistic collector
     */
    StatCollector<?> getStatCollector();
}
