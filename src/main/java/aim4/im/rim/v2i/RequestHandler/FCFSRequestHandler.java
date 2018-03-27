package aim4.im.rim.v2i.RequestHandler;

import aim4.im.rim.v2i.policy.BasePolicy;
import aim4.im.rim.v2i.policy.BasePolicy.ProposalFilterResult;
import aim4.im.rim.v2i.policy.BasePolicy.ReserveParam;
import aim4.im.rim.v2i.policy.BasePolicyCallback;
import aim4.msg.rim.i2v.Reject;
import aim4.msg.rim.v2i.Request;
import aim4.sim.StatCollector;

/**
 * The "First Come, First Served" request handler.
 */
public class FCFSRequestHandler implements RequestHandler{
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The base policy */
    private BasePolicyCallback basePolicy = null;


    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Set the base policy call-back.
     *
     * @param basePolicy  the base policy's call-back
     */
    @Override
    public void setBasePolicyCallback(BasePolicyCallback basePolicy) {
        this.basePolicy = basePolicy;
    }

    /**
     * Let the request handler to act for a given time period.
     *
     * @param timeStep  the time period
     */
    @Override
    public void act(double timeStep) {
        // do nothing
    }

    /**
     * Process the request message.
     *
     * @param msg the request message
     */
    @Override
    public void processRequestMsg(Request msg) {
        int vin = msg.getVin();

        // If the vehicle has got a reservation already, reject it.
        if (basePolicy.hasReservation(vin)) {
            basePolicy.sendRejectMsg(vin,
                    msg.getRequestId(),
                    Reject.Reason.CONFIRMED_ANOTHER_REQUEST);
            return;
        }

        // filter the proposals
        ProposalFilterResult filterResult =
                BasePolicy.standardProposalsFilter(msg.getProposals(),
                        basePolicy.getCurrentTime());
        // If STOP_SIGN policy filter all proposals which are not stopped at the intersection
        if (basePolicy instanceof BasePolicy && ((BasePolicy) basePolicy).getPolicyType() == BasePolicy.PolicyType.STOP_SIGN){
            filterResult = ((BasePolicy) basePolicy).removeProposalWithVehicleNotStoppedAtIntersection(filterResult.getProposals());
        }
        if (filterResult.isNoProposalLeft()) {
            basePolicy.sendRejectMsg(vin,
                    msg.getRequestId(),
                    filterResult.getReason());
        }
        else {
            // try to see if reservation is possible for the remaining proposals.
            ReserveParam reserveParam =
                    basePolicy.findReserveParam(msg, filterResult.getProposals());
            if (reserveParam != null) {
                basePolicy.sendConfirmMsg(msg.getRequestId(), reserveParam);
            } else {
                basePolicy.sendRejectMsg(vin, msg.getRequestId(),
                        Reject.Reason.NO_CLEAR_PATH);
            }
        }
    }

    /**
     * Get the statistic collector.
     *
     * @return the statistic collector
     */
    @Override
    public StatCollector<?> getStatCollector() {
        return null;
    }

}
