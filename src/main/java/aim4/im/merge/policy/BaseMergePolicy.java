package aim4.im.merge.policy;

import aim4.im.AczManager;
import aim4.im.merge.V2IMergeManager;
import aim4.im.merge.reservation.ReservationMerge;
import aim4.im.merge.reservation.ReservationMergeManager;
import aim4.msg.merge.i2v.Confirm;
import aim4.msg.merge.i2v.Reject;
import aim4.msg.merge.v2i.*;
import aim4.util.HashMapRegistry;
import aim4.util.Registry;
import aim4.vehicle.VehicleUtil;

import java.util.*;

/**
 * Created by Callum on 13/04/2017.
 */
public class BaseMergePolicy implements MergePolicy, BaseMergePolicyCallback {
    // CONSTANTS //
    /**
     * The maximum amount of time, in seconds, to let a vehicle arrive early.
     * {@value} seconds.
     */
    private static final double EARLY_ERROR = 0.1;
    /**
     * The maximum amount of time, in seconds to let a vehicle arrive late.
     * {@value} seconds.
     */
    private static final double LATE_ERROR = 0.1;

    // NESTED CLASSES //
    /**
     * The record of a reservation.
     */
    public static class ReservationRecord {
        /** The VIN of a vehicle */
        private int vin;
        /** The ACZ lane ID */
        private int aczLaneId;
        /**
         * Create a record of a reservation
         *
         * @param vin        the VIN of a vehicle
         * @param aczLaneId  the ACZ lane ID
         */
        public ReservationRecord(int vin, int aczLaneId) {
            this.vin = vin;
            this.aczLaneId = aczLaneId;
        }

        /**
         * Get the VIN of a vehicle.
         *
         * @return the VIN of a vehicle
         */
        public int getVin() {
            return vin;
        }

        /**
         * Get the ACZ lane ID.
         *
         * @return the ACZ lane ID
         */
        public int getAczLaneId() {
            return aczLaneId;
        }
    }

    /**
     * The reservation parameter record generated by processProposals()
     */
    public static class ReserveParam {
        /** The VIN of a vehicle */
        private int vin;
        /** The successful proposal */
        private Request.Proposal successfulProposal;
        /** The reservation plan */
        private ReservationMergeManager.Plan mergePlan;
        /** The ACZ manager */
        private AczManager aczManager;
        /** The ACZ plan */
        private AczManager.Plan aczPlan;

        /**
         * Create a reservation parameter record.
         *
         * @param vin                 the VIN of vehicle
         * @param successfulProposal  the successful proposal
         * @param mergePlan            the reservation plan
         * @param aczManager          the ACZ manager
         * @param aczPlan             the ACZ plan
         */
        public ReserveParam(int vin, Request.Proposal successfulProposal, ReservationMergeManager.Plan mergePlan,
                            AczManager aczManager, AczManager.Plan aczPlan) {
            this.vin = vin;
            this.successfulProposal = successfulProposal;
            this.mergePlan = mergePlan;
            this.aczManager = aczManager;
            this.aczPlan = aczPlan;
        }

        /** Get the VIN of the vehicle */
        public int getVin() {
            return vin;
        }

        /** Get the successful proposal */
        public Request.Proposal getSuccessfulProposal() {
            return successfulProposal;
        }

        /** Get the reservation plan */
        public ReservationMergeManager.Plan getMergePlan() {
            return mergePlan;
        }

        /** Get the ACZ manager */
        public AczManager getAczManager() {
            return aczManager;
        }

        /** Get the ACZ reservation plan */
        public AczManager.Plan getAczPlan() {
            return aczPlan;
        }
    }

    /**
     * The result of the standard proposal filter
     */
    public static class ProposalFilterResult {
        /** The list of proposals */
        private List<Request.Proposal> proposals;
        /** The rejection reason */
        private Reject.Reason reason;

        /**
         * Create the result of the standard proposal filter
         *
         * @param proposals the list of proposals
         */
        public ProposalFilterResult(List<Request.Proposal> proposals) {
            this.proposals = proposals;
            this.reason = null;
        }

        /**
         * Create the result of the standard proposal filter
         *
         * @param reason  the rejection reason
         */
        public ProposalFilterResult(Reject.Reason reason) {
            this.proposals = null;
            this.reason = reason;
        }

        /**
         * Whether any proposal is left.
         *
         * @return whether any proposal is left
         */
        public boolean isNoProposalLeft() {
            return proposals == null;
        }

        /**
         * Get the proposals.
         *
         * @return the proposals
         */
        public List<Request.Proposal> getProposals() {
            return proposals;
        }


        /**
         * Get the rejection reason.
         *
         * @return the rejection reason
         */
        public Reject.Reason getReason() {
            return reason;
        }

    }

    // PUBLIC STATIC METHODS //
    /**
     * Remove the proposals that are either too early or too late.
     *
     * @param proposals    the list of proposals
     * @param currentTime  the current time
     *
     * @return the proposal filter result
     */
    public static ProposalFilterResult standardProposalsFilter(
            List<Request.Proposal> proposals,
            double currentTime) {
        // copy the proposals to a list first.
        List<Request.Proposal> myProposals =
                new LinkedList<Request.Proposal>(proposals);

        // Remove proposals whose arrival time is smaller than or equal to the
        // the current time.
        BaseMergePolicy.removeProposalWithLateArrivalTime(myProposals, currentTime);
        if (myProposals.isEmpty()) {
            return new ProposalFilterResult(Reject.Reason.ARRIVAL_TIME_TOO_LATE);
        }
        // Check to see if not all of the arrival times in this reservation
        // request are too far in the future
        BaseMergePolicy.removeProposalWithLargeArrivalTime(
                myProposals, currentTime + V2IMergeManager.MAXIMUM_FUTURE_RESERVATION_TIME);
        if (myProposals.isEmpty()) {
            return new ProposalFilterResult(Reject.Reason.ARRIVAL_TIME_TOO_LARGE);
        }
        // return the remaining proposals
        return new ProposalFilterResult(myProposals);
    }

    /**
     * Remove proposals whose arrival time is small than or equal to the
     * current time.
     *
     * @param proposals    a list of proposals
     * @param currentTime  the current time
     */
    private static void removeProposalWithLateArrivalTime(
            List<Request.Proposal> proposals,
            double currentTime) {
        for(Iterator<Request.Proposal> tpIter = proposals.listIterator();
            tpIter.hasNext();) {
            Request.Proposal prop = tpIter.next();
            // If this one is in the past
            if (prop.getArrivalTime() <= currentTime) {
                tpIter.remove();
            }
        }
    }

    /**
     * Remove proposals whose arrival time is larger than the current time plus
     * the maximum future reservation time.
     *
     * @param proposals   a set of proposals
     * @param futureTime  the future arrival time beyond which a proposal is
     *                    invalid.
     */
    private static void removeProposalWithLargeArrivalTime(
            List<Request.Proposal> proposals,
            double futureTime) {
        for(Iterator<Request.Proposal> tpIter = proposals.listIterator();
            tpIter.hasNext();) {
            Request.Proposal prop = tpIter.next();
            // If this one is in the past
            if(prop.getArrivalTime() > futureTime) {
                tpIter.remove();
            }
        }
    }

    // PRIVATE FIELDS //
    /**
     * The V2IMergeManager of which this MergePolicy is a part
     */
    protected V2IMergeManagerCallback mm;
    /**
     * The proposal handler
     */
    private MergeRequestHandler requestHandler;
    /**
     * The confirm message registry
     */
    private Registry<ReservationRecord> reservationRecordRegistry =
            new HashMapRegistry<ReservationRecord>();
    /**
     * A mapping from VIN numbers to reservation Id
     */
    private Map<Integer,Integer> vinToReservationId =
            new HashMap<Integer,Integer>();

    // CONSTRUCTORS //
    public BaseMergePolicy(V2IMergeManagerCallback mm, MergeRequestHandler requestHandler) {
        this.mm = mm;
        setRequestHandler(requestHandler);
    }

    // PUBLIC METHODS //
    // ACCESSORS
    /**
     * {@inheritDoc}
     */
    @Override
    public void setV2IMergeManagerCallback(V2IMergeManagerCallback mm) {
        this.mm = mm;
    }

    /**
     * Get the request handler.
     *
     * @return the request handler.
     */
    public MergeRequestHandler getRequestHandler() {
        return requestHandler;
    }

    /**
     * Set the request handler.
     *
     * @param RequestHandler  the request handler.
     */
    public void setRequestHandler(MergeRequestHandler RequestHandler) {
        this.requestHandler = RequestHandler;
        requestHandler.setBaseMergePolicyCallback(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getCurrentTime() {
        return mm.getCurrentTime();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ReservationMerge getReservationMerge() {
        return mm.getReservationMerge();
    }

    // ACTION
    @Override
    public void act(double timeStep) {
        requestHandler.act(timeStep);
    }

    //COMMUNICATION
    /**
     * {@inheritDoc}
     */
    @Override
    public void sendConfirmMessage(int latestRequestId,
                                   BaseMergePolicy.ReserveParam reserveParam) {
        int vin = reserveParam.getVin();

        // make sure that there is no other confirm message is in effect
        // when the request handler sends the confirm message.
        assert !vinToReservationId.containsKey(vin);
        // actually make the reservation
        Integer mergeTicket =
                mm.getReservationMergeManager().accept(reserveParam.getMergePlan());
        Integer aczTicket =
                reserveParam.getAczManager().accept(reserveParam.getAczPlan());
        assert mergeTicket == vin;
        assert aczTicket == vin;

        // send the confirm message
        int reservationId = reservationRecordRegistry.getNewId();
        Confirm confirmMsg =
                new Confirm(mm.getId(),
                        vin,
                        reservationId,
                        latestRequestId,
                        reserveParam.getSuccessfulProposal().getArrivalTime(),
                        EARLY_ERROR, LATE_ERROR,
                        reserveParam.getSuccessfulProposal().getArrivalVelocity(),
                        reserveParam.getSuccessfulProposal().getArrivalLaneID(),
                        reserveParam.getSuccessfulProposal().getDepartureLaneID(),
                        mm.getACZ(reserveParam.getSuccessfulProposal()
                                .getDepartureLaneID()).getMaxSize(),
                        reserveParam.getMergePlan().getAccelerationProfile());
        mm.sendI2VMessage(confirmMsg);

        // bookkeeping
        ReservationRecord r =
                new ReservationRecord(
                        vin,
                        reserveParam.getSuccessfulProposal().getDepartureLaneID());
        reservationRecordRegistry.set(reservationId, r);
        vinToReservationId.put(vin, reservationId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void sendRejectMsg(int vin, int latestRequestId, Reject.Reason reason){
        mm.sendI2VMessage(new Reject(mm.getId(),
                vin,
                latestRequestId,
                mm.getCurrentTime(), // can re-send request
                // immediately
                reason));
    }

    /**
     * Process a V2I message
     *
     * @param msg  the V2I message
     */
    @Override
    public void processV2IMergeMessage(V2IMergeMessage msg) {
        if (msg instanceof Request) {
            requestHandler.processRequestMsg((Request)msg);
        } else if (msg instanceof Cancel) {
            processCancelMsg((Cancel) msg);
        } else if (msg instanceof Done) {
            processDoneMsg((Done) msg);
        } else if (msg instanceof Away) {
            processAwayMsg((Away) msg);
        } else {
            throw new RuntimeException("Unhandled message type: " + msg);
        }
    }

    /**
     * Submit a cancel message to the policy.
     *
     * @param msg  the cancel message
     */
    public void processCancelMsg(Cancel msg) {
        ReservationRecord r = reservationRecordRegistry.get(msg.getReservationID());
        if (r != null) {
            int vin = r.getVin();    // don't use the VIN in msg in case
            // vin != msg.getVin()
            if (vin != msg.getVin()) {
                System.err.printf("BasePolicy::processCancelMsg(): " +
                        "The VIN of the message is different from the VIN " +
                        "on the record.\n");
            }
            // release the resources
            mm.getReservationMergeManager().cancel(vin);
            mm.getAczManager(r.getAczLaneId()).cancel(vin);
            // remove the reservation record
            reservationRecordRegistry.setNull(msg.getReservationID());
            vinToReservationId.remove(vin);
        } else {
            System.err.printf("BasePolicy::processCancelMsg(): " +
                    "record not found\n");
        }
    }

    /**
     * Submit a done message to the policy.
     *
     * @param msg  the done message
     */
    public void processDoneMsg(Done msg) {
        ReservationRecord r = reservationRecordRegistry.get(msg.getReservationID());
        if (r != null) {
            int vin = r.getVin();   // don't use the VIN in msg.
            if (vin != msg.getVin()) {
                System.err.printf("BasePolicy::processCancelMsg(): " +
                        "The VIN of the message is different from the VIN " +
                        "on the record.\n");
            }
            // do nothing with the done message since the reservation grid is
            // automatically cleaned.
        } else {
            System.err.printf("BasePolicy::processDoneMsg(): " +
                    "record not found");
        }
    }

    /**
     * Submit an away message to the policy.
     *
     * @param msg  the away message
     */
    public void processAwayMsg(Away msg) {
        ReservationRecord r = reservationRecordRegistry.get(msg.getReservationID());
        if (r != null) {
            int vin = r.getVin();  // don't use the VIN in msg.
            if (vin != msg.getVin()) {
                System.err.printf("BasePolicy::processCancelMsg(): " +
                        "The VIN of the message is different from the VIN " +
                        "on the record.\n");
            }
            // clear the reservation in ACZ.
            mm.getACZ(r.getAczLaneId()).away(vin);
            // remove the reservation record
            reservationRecordRegistry.setNull(msg.getReservationID());
            vinToReservationId.remove(vin);
        } else {
            System.err.printf("BasePolicy::processAwayMsg(): record not found");
        }
    }

    // RESERVATION
    /**
     * {@inheritDoc}
     */
    @Override
    public ReserveParam findReserveParam(Request msg,
                                         List<Request.Proposal> proposals) {
        int vin = msg.getVin();

        // Okay, now let's actually try some of these proposals
        Request.Proposal successfulProposal = null;
        ReservationMergeManager.Plan mergePlan = null;
        AczManager aczManager = null;
        AczManager.Plan aczPlan = null;

        for(Request.Proposal proposal : proposals) {
            ReservationMergeManager.Query mergeQuery =
                    new ReservationMergeManager.Query(vin,
                            proposal.getArrivalTime(),
                            proposal.getArrivalVelocity(),
                            proposal.getArrivalLaneID(),
                            proposal.getDepartureLaneID(),
                            msg.getSpec(),
                            proposal.getMaximumTurnVelocity(),
                            true);
            mergePlan = mm.getReservationMergeManager().query(mergeQuery);
            if (mergePlan != null) {
                double stopDist =
                        VehicleUtil.calcDistanceToStop(mergePlan.getExitVelocity(),
                                msg.getSpec().getMaxDeceleration());

                aczManager = mm.getAczManager(proposal.getDepartureLaneID());
                if (aczManager == null) {
                    System.err.printf("FCFSPolicy::processRequestMsg(): " +
                            "aczManager should not be null.\n");
                    System.err.printf("proposal.getDepartureLaneID() = %d\n",
                            proposal.getDepartureLaneID());
                    aczPlan = null;
                } else {
                    AczManager.Query aczQuery =
                            new AczManager.Query(vin,
                                    mergePlan.getExitTime(),
                                    mergePlan.getExitVelocity(),
                                    msg.getSpec().getLength(),
                                    stopDist);
                    aczPlan = aczManager.query(aczQuery);
                    if (aczPlan != null) {
                        successfulProposal = proposal;  // reservation succeeds!
                        break;
                    }
                }
            }
        }

        if (successfulProposal != null) {
            return new ReserveParam(vin, successfulProposal, mergePlan, aczManager,
                    aczPlan);
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasReservation(int vin) {
        return vinToReservationId.containsKey(vin);
    }


}
