package aim4.im.rim.v2i.policy;

import aim4.im.rim.TrackModel;
import aim4.msg.rim.i2v.Reject;
import aim4.msg.rim.v2i.Request;

import java.util.List;

/**
 * The base policy's callback interface.
 */
public interface BasePolicyCallback {
    /**
     * Send a confirm message
     *
     * @param latestRequestId  the latest request id of the vehicle
     * @param reserveParam     the reservation parameter
     */
    void sendConfirmMsg(int latestRequestId,
                        BasePolicy.ReserveParam reserveParam);
    /**
     * Send a reject message
     *
     * @param vin              the VIN
     * @param latestRequestId  the latest request id of the vehicle
     * @param reason           the reason of rejection
     */
    void sendRejectMsg(int vin, int latestRequestId, Reject.Reason reason);

    /**
     * Compute the reservation parameter given the request message and a
     * set of proposals.
     *
     * @param msg        the request message
     * @param proposals  the set of proposals
     * @return the reservation parameters; null if the reservation is infeasible.
     */
    BasePolicy.ReserveParam findReserveParam(Request msg, List<Request.Proposal> proposals);

    /**
     * Get the current time
     *
     * @return the current time
     */
    double getCurrentTime();

    /**
     * Check whether the vehicle currently has a reservation.
     *
     * @param vin  the VIN of the vehicle
     * @return whether the vehicle currently has a reservation.
     */
    boolean hasReservation(int vin);


    // TODO: remove this function
    TrackModel getTrackMode();
}
