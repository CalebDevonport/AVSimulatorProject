package aim4.rim.driver;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.Driver;
import aim4.driver.rim.CrashTestDummy;
import aim4.im.rim.RoadBasedIntersection;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;
import aim4.msg.rim.v2i.Request;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.aim.AIMBasicAutoVehicle;
import org.junit.Test;

import static aim4.im.rim.v2i.reservation.ReservationGridManager.Config;
import static aim4.im.rim.v2i.reservation.ReservationGridManager.Query;

public class CrashTestDummyTests {
    private static final double ARRIVAL_TIME = 4.0;
    private static final double STATIC_BUFFER_SIZE = 0.25;
    private static final double INTERNAL_TILE_TIME_BUFFER_SIZE = 0.1;
    private static final double GRANULARITY = 6.0;
    private static final double[] ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  6.04;
    private static final int VIN =  1;
    private static  RoadBasedIntersection INTERSECTION;

    @Test
    public void act_withArrivalEastAndDepartureEast_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(0,0);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalEastAndDepartureSouth_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(0,3);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalEastAndDepartureNorth_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(0,2);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalNorthAndDepartureEast_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(2,0);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalNorthAndDepartureNorth_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(2,2);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);
    }

    @Test
    public void act_withArrivalNorthAndDepartureWest_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(2,1);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalWestAndDepartureWest_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(1,1);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);
    }

    @Test
    public void act_withArrivalWestAndDepartureNorth_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(1,2);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);
    }

    @Test
    public void act_withArrivalWestAndDepartureSouth_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(1,3);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalSouthAndDepartureSouth_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(3,3);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    @Test
    public void act_withArrivalSouthAndDepartureEast_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(3,0);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);
    }

    @Test
    public void act_withArrivalSouthAndDepartureWest_setsCurrentLaneAndSteering(){
        //arrange
        Query query = setupQuery(3,1);
        ArcSegmentLane arrivalLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getArrivalLaneId());
        ArcSegmentLane departureLane = (ArcSegmentLane) Debug.currentRimMap.getLaneRegistry().get(query.getDepartureLaneId());
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLane);

        //act
        CrashTestDummy dummy = new CrashTestDummy(testVehicle, arrivalLane, departureLane);
        dummy.act();
        ArcSegmentLane nextLane = (ArcSegmentLane) arrivalLane.getNextLane();

        //assert
        assert dummy.getCurrentLane() == nextLane.getArcLaneDecomposition().get(0);

    }

    private Query setupQuery(int entryRoadIndex, int exitRoadIndex) {
        Config config = new Config(SimConfig.TIME_STEP,
                SimConfig.GRID_TIME_STEP,
                STATIC_BUFFER_SIZE,
                INTERNAL_TILE_TIME_BUFFER_SIZE,
                GRANULARITY);
        RimIntersectionMap map = new RimIntersectionMap(
                0,
                1,
                1,
                ROUNDABOUT_DIAMETER[0],
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());
        INTERSECTION = roadBasedIntersection;
        Lane arrivalLane = roadBasedIntersection.getRoads().get(entryRoadIndex).getEntryApproachLane();
        Lane exitLane = roadBasedIntersection.getRoads().get(exitRoadIndex).getExitApproachLane();
        Request.VehicleSpecForRequestMsg vehicleSpecForRequestMsg = new Request.VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("COUPE"));
        Query query = new Query(
                VIN,
                ARRIVAL_TIME,
                ROUNDABOUT_SPEED_LIMIT, // arrival velocity
                arrivalLane.getId(),
                exitLane.getId(),
                vehicleSpecForRequestMsg,
                ROUNDABOUT_SPEED_LIMIT, // max turn velocity
                false); // acceleration allowed
        return query;
    }

    private AIMBasicAutoVehicle createTestVehicle(
            Request.VehicleSpecForRequestMsg spec,
            double arrivalVelocity,
            double maxVelocity,
            ArcSegmentLane arrivalLane) {

        VehicleSpec newSpec = new VehicleSpec(
                "TestVehicle",
                spec.getMaxAcceleration(),
                spec.getMaxDeceleration(),
                maxVelocity,  // TODO: why not one in msg.getSpec().getMaxVelocity()
                spec.getMinVelocity(),
                spec.getLength(),
                spec.getWidth(),
                spec.getFrontAxleDisplacement(),
                spec.getRearAxleDisplacement(),
                0.0, // wheelSpan
                0.0, // wheelRadius
                0.0, // wheelWidth
                spec.getMaxSteeringAngle(),
                spec.getMaxTurnPerSecond());

        AIMBasicAutoVehicle testVehicle = new AIMBasicAutoVehicle(
                newSpec,
                INTERSECTION.getEntryPoint(arrivalLane), // Position
                INTERSECTION.getEntryHeading(arrivalLane), // Heading
                0.0, // Steering angle
                arrivalVelocity, // velocity
                0.0, // target velocity
                0.0, // Acceleration
                0.0); // the current time   // TODO: need to think about the appropriate
        // current time

        return testVehicle;
    }
}
