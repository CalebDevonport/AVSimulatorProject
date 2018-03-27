package aim4.rim.im;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.im.rim.TrackModel;
import aim4.im.rim.v2i.RequestHandler.FCFSRequestHandler;
import aim4.im.rim.v2i.V2IManager;
import aim4.im.rim.v2i.policy.BasePolicy;
import aim4.im.rim.v2i.policy.Policy;
import aim4.im.rim.v2i.reservation.ReservationGridManager.Config;
import aim4.map.Road;
import aim4.map.rim.RimIntersectionMap;
import aim4.msg.aim.i2v.I2VMessage;
import aim4.msg.rim.i2v.Confirm;
import aim4.msg.rim.i2v.Reject;
import aim4.msg.rim.v2i.Cancel;
import aim4.msg.rim.v2i.Request;
import aim4.msg.rim.v2i.Request.Proposal;
import aim4.msg.rim.v2i.Request.VehicleSpecForRequestMsg;
import aim4.util.ArrayListRegistry;
import aim4.vehicle.VehicleSpecDatabase;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class V2IManagerTests {
    private static final double ARRIVAL_TIME = 6.0;
    private static final double CURRENT_TIME = 1.0;
    private static final double STATIC_BUFFER_SIZE = 0.25;
    private static final double INTERNAL_TILE_TIME_BUFFER_SIZE = 0.1;
    private static final double GRANULARITY = 6.0;
    private static final List<Double> ROUNDABOUT_DIAMETER = Arrays.asList(30.0, 35.0, 40.0, 45.0);
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  6.04;

    @Test
    public void processRequestMessage_withOneVehicleAndNewRequest_returnsCONFIRM() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle spec
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));

        // Set Proposal
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = ARRIVAL_TIME;
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set request
        int vin = 1; // the vehicle id
        int imId = 0; // the im id to which to send the message
        int requestId = 1; // request id
        List<Proposal> proposals = new ArrayList<>();
        proposals.add(proposal);

        Request request = new Request(vin, imId, requestId, vehicleSpecForRequestMsg, proposals);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive message (add it to the inbox)
        im.receive(request);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        assert im.outboxIterator().next().getVin() == vin;
        assert im.outboxIterator().next().getImId() == imId;
        assert im.outboxIterator().next().getMessageType().name() == I2VMessage.Type.CONFIRM.name();


    }

    @Test
    public void processRequestMessage_withOneVehicleAndAlreadyRequest_returnsCONFIRMED_ANOTHER_REQUEST() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle spec
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));

        // Set Proposal
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = ARRIVAL_TIME;
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set requests
        int vin = 1; // the vehicle id
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        int requestId2 = 2; // request id of second request
        List<Proposal> proposals = new ArrayList<>();
        proposals.add(proposal);

        Request request1 = new Request(vin, imId, requestId1, vehicleSpecForRequestMsg, proposals);
        Request request2 = new Request(vin, imId, requestId2, vehicleSpecForRequestMsg, proposals);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);
        im.receive(request2);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        // First request confirmed
        aim4.msg.rim.i2v.I2VMessage firstMessage = iter.next();
        assert firstMessage.getVin() == vin;
        assert firstMessage instanceof Confirm;
        assert firstMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();

        // Second request rejected
        assert iter.hasNext();
        if (iter.hasNext()) {
            aim4.msg.rim.i2v.I2VMessage secondMessage = iter.next();
            // Second request rejected
            assert secondMessage.getVin() == vin;
            assert secondMessage instanceof Reject;
            assert ((Reject) secondMessage).getReason() == Reject.Reason.CONFIRMED_ANOTHER_REQUEST;
            assert secondMessage.getMessageType().name() == I2VMessage.Type.REJECT.name();
        }
    }

    @Test
    public void processRequestMessage_withOneVehicleAndArrivalTimeInThePast_returnsARRIVAL_TIME_TOO_LATE() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle spec
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));

        // Set Proposal
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = CURRENT_TIME - 1; // arrival in the past
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set requests
        int vin = 1; // the vehicle id
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        List<Proposal> proposals = new ArrayList<>();
        proposals.add(proposal);

        Request request1 = new Request(vin, imId, requestId1, vehicleSpecForRequestMsg, proposals);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        aim4.msg.rim.i2v.I2VMessage response = iter.next();
        assert response.getVin() == vin;
        assert response instanceof Reject;
        assert ((Reject) response).getReason() == Reject.Reason.ARRIVAL_TIME_TOO_LATE;
        assert response.getMessageType().name() == I2VMessage.Type.REJECT.name();
    }

    @Test
    public void processRequestMessage_withOneVehicleAndArrivalTimeInFarFuture_returnsARRIVAL_TIME_TOO_LARGE() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle spec
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));

        // Set Proposal
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = CURRENT_TIME + 12; // too far in the future
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set requests
        int vin = 1; // the vehicle id
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        List<Proposal> proposals = new ArrayList<>();
        proposals.add(proposal);

        Request request1 = new Request(vin, imId, requestId1, vehicleSpecForRequestMsg, proposals);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        aim4.msg.rim.i2v.I2VMessage response = iter.next();
        assert response.getVin() == vin;
        assert response instanceof Reject;
        assert ((Reject) response).getReason() == Reject.Reason.ARRIVAL_TIME_TOO_LARGE;
        assert response.getMessageType().name() == I2VMessage.Type.REJECT.name();
    }

    @Test
    public void processRequestMessage_withTwoVehiclesAndSamePath_returnsNO_CLEAR_PATH() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle specs
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg1 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("VAN"));
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg2 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));

        // Set Proposal 1
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = ARRIVAL_TIME;
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set Proposal 2
        int arrivalLaneId2 = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId2 = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity2 = 6.04;
        double arrivalTime2 = ARRIVAL_TIME + 1.0;
        double maxTurnVelocity2 = arrivalVelocity;
        Proposal proposal2 = new Proposal(arrivalLaneId2, departureLaneId2, arrivalTime2, arrivalVelocity2, maxTurnVelocity2, false);

        // Set requests
        int vin1 = 1; // the vehicle id 1
        int vin2 = 2; // the vehicle id 2
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        int requestId2 = 2; // request id of second request
        List<Proposal> proposals1 = new ArrayList<>();
        List<Proposal> proposals2 = new ArrayList<>();
        proposals1.add(proposal);
        proposals2.add(proposal2);

        Request request1 = new Request(vin1, imId, requestId1, vehicleSpecForRequestMsg1, proposals1);
        Request request2 = new Request(vin2, imId, requestId2, vehicleSpecForRequestMsg2, proposals2);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);
        im.receive(request2);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        // First request confirmed
        aim4.msg.rim.i2v.I2VMessage firstMessage = iter.next();
        assert firstMessage.getVin() == vin1;
        assert firstMessage instanceof Confirm;
        assert firstMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();

        // Second request rejected
        assert iter.hasNext();
        if (iter.hasNext()) {
            aim4.msg.rim.i2v.I2VMessage secondMessage = iter.next();
            // Second request rejected
            assert secondMessage.getVin() == vin2;
            assert secondMessage instanceof Reject;
            assert ((Reject) secondMessage).getReason() == Reject.Reason.NO_CLEAR_PATH;
            assert secondMessage.getMessageType().name() == I2VMessage.Type.REJECT.name();
        }
    }

    @Test
    public void processRequestMessage_withTwoVehiclesAndDifferentIntersectingPaths_returnsNO_CLEAR_PATH() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle specs
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg1 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg2 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("VAN"));

        // Set Proposal 1
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = ARRIVAL_TIME;
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set Proposal 2
        int arrivalLaneId2 = getEastRoad().getEntryApproachLane().getId();
        int departureLaneId2 = getNorthRoad().getExitApproachLane().getId();
        double arrivalVelocity2 = 6.04;
        double arrivalTime2 = ARRIVAL_TIME - 4.0;
        double maxTurnVelocity2 = arrivalVelocity;
        Proposal proposal2 = new Proposal(arrivalLaneId2, departureLaneId2, arrivalTime2, arrivalVelocity2, maxTurnVelocity2, false);

        // Set requests
        int vin1 = 1; // the vehicle id 1
        int vin2 = 2; // the vehicle id 2
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        int requestId2 = 2; // request id of second request
        List<Proposal> proposals1 = new ArrayList<>();
        List<Proposal> proposals2 = new ArrayList<>();
        proposals1.add(proposal);
        proposals2.add(proposal2);

        Request request1 = new Request(vin1, imId, requestId1, vehicleSpecForRequestMsg1, proposals1);
        Request request2 = new Request(vin2, imId, requestId2, vehicleSpecForRequestMsg2, proposals2);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);
        im.receive(request2);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        // First request confirmed
        aim4.msg.rim.i2v.I2VMessage firstMessage = iter.next();
        assert firstMessage.getVin() == vin1;
        assert firstMessage instanceof Confirm;
        assert firstMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();

        // Second request rejected
        assert iter.hasNext();
        if (iter.hasNext()) {
            aim4.msg.rim.i2v.I2VMessage secondMessage = iter.next();
            // Second request rejected
            assert secondMessage.getVin() == vin2;
            assert secondMessage instanceof Reject;
            assert ((Reject) secondMessage).getReason() == Reject.Reason.NO_CLEAR_PATH;
            assert secondMessage.getMessageType().name() == I2VMessage.Type.REJECT.name();
        }
    }

    @Test
    public void processRequestMessage_withTwoVehiclesAndDifferentNotIntersectingPaths_returnsCONFIRM() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle specs
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg1 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg2 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("VAN"));

        // Set Proposal 1
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = ARRIVAL_TIME;
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set Proposal 2
        int arrivalLaneId2 = getEastRoad().getEntryApproachLane().getId();
        int departureLaneId2 = getNorthRoad().getExitApproachLane().getId();
        double arrivalVelocity2 = 6.04;
        double arrivalTime2 = ARRIVAL_TIME + 1.0;
        double maxTurnVelocity2 = arrivalVelocity;
        Proposal proposal2 = new Proposal(arrivalLaneId2, departureLaneId2, arrivalTime2, arrivalVelocity2, maxTurnVelocity2, false);

        // Set requests
        int vin1 = 1; // the vehicle id 1
        int vin2 = 2; // the vehicle id 2
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        int requestId2 = 2; // request id of second request
        List<Proposal> proposals1 = new ArrayList<>();
        List<Proposal> proposals2 = new ArrayList<>();
        proposals1.add(proposal);
        proposals2.add(proposal2);

        Request request1 = new Request(vin1, imId, requestId1, vehicleSpecForRequestMsg1, proposals1);
        Request request2 = new Request(vin2, imId, requestId2, vehicleSpecForRequestMsg2, proposals2);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);
        im.receive(request2);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        // First request confirmed
        aim4.msg.rim.i2v.I2VMessage firstMessage = iter.next();
        assert firstMessage.getVin() == vin1;
        assert firstMessage instanceof Confirm;
        assert firstMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();

        // Second request rejected
        assert iter.hasNext();
        if (iter.hasNext()) {
            aim4.msg.rim.i2v.I2VMessage secondMessage = iter.next();
            // Second request rejected
            assert secondMessage.getVin() == vin2;
            assert secondMessage instanceof Confirm;
            assert secondMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();
        }
    }

    @Test
    public void processRequestMessage_withTwoVehiclesAndDifferentIntersectingPathsButOneVehicleCancels_returnsCONFIRM() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // Create config
        Config config = getConfig();

        // Create request handler
        FCFSRequestHandler fcfsRequestHandler = new FCFSRequestHandler();

        // Set vehicle specs
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg1 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));
        VehicleSpecForRequestMsg vehicleSpecForRequestMsg2 =
                new VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("VAN"));

        // Set Proposal 1
        int arrivalLaneId = getNorthRoad().getEntryApproachLane().getId();
        int departureLaneId = getWestRoad().getExitApproachLane().getId();
        double arrivalVelocity = 6.04;
        double arrivalTime = ARRIVAL_TIME;
        double maxTurnVelocity = arrivalVelocity;
        Proposal proposal = new Proposal(arrivalLaneId, departureLaneId, arrivalTime, arrivalVelocity, maxTurnVelocity, false);

        // Set Proposal 2
        int arrivalLaneId2 = getEastRoad().getEntryApproachLane().getId();
        int departureLaneId2 = getNorthRoad().getExitApproachLane().getId();
        double arrivalVelocity2 = 6.04;
        double arrivalTime2 = ARRIVAL_TIME - 4.0;
        double maxTurnVelocity2 = arrivalVelocity;
        Proposal proposal2 = new Proposal(arrivalLaneId2, departureLaneId2, arrivalTime2, arrivalVelocity2, maxTurnVelocity2, false);

        // Set requests
        int vin1 = 1; // the vehicle id 1
        int vin2 = 2; // the vehicle id 2
        int imId = 0; // the im id to which to send the message
        int requestId1 = 1; // request id of first request
        int reservationId = 0; // first reservation so id = 0
        int requestId2 = 2; // request id of second request
        List<Proposal> proposals1 = new ArrayList<>();
        List<Proposal> proposals2 = new ArrayList<>();
        proposals1.add(proposal);
        proposals2.add(proposal2);

        Request request1 = new Request(vin1, imId, requestId1, vehicleSpecForRequestMsg1, proposals1);
        Cancel cancel1 = new Cancel(vin1, imId, reservationId);
        Request request2 = new Request(vin2, imId, requestId2, vehicleSpecForRequestMsg2, proposals2);

        // Set V21Manager
        V2IManager im = new V2IManager(roadBasedIntersection, trackModel, CURRENT_TIME, config, new ArrayListRegistry<>());

        // Set policy
        Policy policy = new BasePolicy(im, fcfsRequestHandler, BasePolicy.PolicyType.FCFS);
        im.setPolicy(policy);

        // Receive messages (add it to the inbox)
        im.receive(request1);
        // Second vehicle tries to request
        im.receive(request2);
        // First vehicle cancels
        im.receive(cancel1);
        // Second vehicle tries to request
        im.receive(request2);

        //act
        // Process the inbox messages
        im.act(SimConfig.TIME_STEP);

        //assert request confirmed
        Iterator<aim4.msg.rim.i2v.I2VMessage> iter = im.outboxIterator();
        // First request confirmed
        aim4.msg.rim.i2v.I2VMessage firstMessage = iter.next();
        assert firstMessage.getVin() == vin1;
        assert firstMessage instanceof Confirm;
        assert firstMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();

        // Second req rejected as it collides
        aim4.msg.rim.i2v.I2VMessage secondMessage = iter.next();
        // Second request rejected
        assert secondMessage.getVin() == vin2;
        assert secondMessage instanceof Reject;
        assert ((Reject) secondMessage).getReason() == Reject.Reason.NO_CLEAR_PATH;
        assert secondMessage.getMessageType().name() == I2VMessage.Type.REJECT.name();

        // First vehicle canceled, so now free to go
        aim4.msg.rim.i2v.I2VMessage thirdMessage = iter.next();
        // Second request rejected
        assert thirdMessage.getVin() == vin2;
        assert firstMessage instanceof Confirm;
        assert firstMessage.getMessageType().name() == I2VMessage.Type.CONFIRM.name();


    }

    private RimIntersectionMap getRimIntersectionMap() {
        return new RimIntersectionMap(
                0,
                1,
                1,
                ROUNDABOUT_DIAMETER.get(3),
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);
    }

    private Config getConfig() {
        return new Config(SimConfig.TIME_STEP,
                SimConfig.GRID_TIME_STEP,
                STATIC_BUFFER_SIZE,
                INTERNAL_TILE_TIME_BUFFER_SIZE,
                GRANULARITY);
    }

    private Road getNorthRoad(){ return Debug.currentRimMap.getRoads().get(2); }

    private Road getEastRoad(){
        return Debug.currentRimMap.getRoads().get(0);
    }

    private Road getSouthRoad(){
        return Debug.currentRimMap.getRoads().get(3);
    }

    private Road getWestRoad(){
        return Debug.currentRimMap.getRoads().get(1);
    }
}
