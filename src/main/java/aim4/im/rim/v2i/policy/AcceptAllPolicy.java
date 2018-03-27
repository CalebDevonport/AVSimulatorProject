package aim4.im.rim.v2i.policy;

import aim4.im.aim.v2i.policy.AllStopPolicy;
import aim4.im.rim.v2i.V2IManagerCallback;
import aim4.msg.rim.i2v.Confirm;
import aim4.msg.rim.v2i.Request;
import aim4.msg.rim.v2i.Request.Proposal;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.sim.StatCollector;

/**
 * The policy used for the no protocol intersection in which all requests are accepted.
 */
public class AcceptAllPolicy implements Policy {
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The maximum amount of time, in seconds, to let a vehicle arrive early.
     * {@value} seconds.
     */
    private static final double EARLY_ERROR = 0.01;
    /**
     * The maximum amount of time, in seconds to let a vehicle arrive late.
     * {@value} seconds.
     */
    private static final double LATE_ERROR = 0.01;
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The V2IManager of which this Policy is a part.
     */
    private V2IManagerCallback im;


    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create an all stop policy
     *
     * @param im  the intersection manager
     */
    public AcceptAllPolicy(V2IManagerCallback im){
        this.im = im;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public void setV2IManagerCallback(V2IManagerCallback im) {
        this.im = im;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void act(double timeStep) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void processV2IMessage(V2IMessage msg) {
        if (msg instanceof Request) {
            Proposal proposal = ((Request) msg).getProposals().get(0);
            im.sendI2VMessage(
                    new Confirm(im.getId(),
                            msg.getVin(),
                            ((Request) msg).getRequestId(),
                            ((Request) msg).getRequestId(),
                            proposal.getArrivalTime(),
                            EARLY_ERROR, LATE_ERROR,
                            proposal.getArrivalVelocity(),
                            proposal.getArrivalLaneID(),
                            proposal.getDepartureLaneID(),
                            im.getACZ(proposal.getDepartureLaneID()).getMaxSize(),
                            null
                            ));
        } // else do nothing
    }


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public StatCollector<AllStopPolicy> getStatCollector() {
        return null;
    }
}
