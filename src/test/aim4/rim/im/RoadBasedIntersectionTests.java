package aim4.rim.im;

import aim4.config.Constants;
import aim4.im.rim.RoadBasedIntersection;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class RoadBasedIntersectionTests {
    private static final double[] ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  31.0686;
    private static final double ROUNDABOUT_SPEED_LIMIT =  21.748;

    @Test
    public void getEntryPoint_withEntryApproachLane_returnsEntryPoint() {
        //arrange
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
        Lane entryApproachLane = map.getRoads().get(2).getEntryApproachLane();

        //act


        //assert
        assert roadBasedIntersection.getEntryPoint(entryApproachLane) != null;
    }

    @Test
    public void getEntryPoint_withNonEntryApproachLane_returnsNull() {
        //arrange
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
        Lane nonEntryApproachLane = map.getRoads().get(2).getEntryMergingLane();

        //act


        //assert
        assert roadBasedIntersection.getEntryPoint(nonEntryApproachLane) == null;
    }

    @Test
    public void getEntryPoint_withLineSegmentLaneOnEntry_returnsEntryPoint() {
        //arrange
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
        ArcSegmentLane entryApproachLane = ((ArcSegmentLane) map.getRoads().get(2).getEntryApproachLane());
        Lane lane = entryApproachLane.getArcLaneDecomposition().get(0);

        //act


        //assert
        assert roadBasedIntersection.getEntryPoint(lane) != null;
    }

    @Test
    public void getEntryPoint_withNotLineSegmentLaneOnEntry_returnsNull() {
        //arrange
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
        ArcSegmentLane entryApproachLane = ((ArcSegmentLane) map.getRoads().get(2).getEntryApproachLane());
        Lane lane = entryApproachLane.getArcLaneDecomposition().get(entryApproachLane.getArcLaneDecomposition().size() - 2);

        //act


        //assert
        assert roadBasedIntersection.getEntryPoint(lane) == null;
    }

    @Test
    public void getExitPoint_withExitApproachLane_returnsExitPoint() {
        //arrange
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
        Lane exitApproachLane = map.getRoads().get(2).getExitApproachLane();

        //act


        //assert
        assert roadBasedIntersection.getExitPoint(exitApproachLane) != null;
    }

    @Test
    public void getExitPoint_withNotExitApproachLane_returnsNull() {
        //arrange
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
        Lane nonEntryApproachLane = map.getRoads().get(2).getExitMergingLane();

        //act


        //assert
        assert roadBasedIntersection.getExitPoint(nonEntryApproachLane) == null;
    }

    @Test
    public void getExitPoint_withLineSegmentLaneOnExit_returnsExitPoint() {
        //arrange
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
        ArcSegmentLane exitApproachLane = ((ArcSegmentLane) map.getRoads().get(2).getExitApproachLane());
        Lane lane = exitApproachLane.getArcLaneDecomposition().get(exitApproachLane.getArcLaneDecomposition().size() - 1);

        //act


        //assert
        assert roadBasedIntersection.getExitPoint(lane) != null;
    }

    @Test
    public void getExitPoint_withNotLineSegmentLaneOnExit_returnsNull() {
        //arrange
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
        ArcSegmentLane exitApproachLane = ((ArcSegmentLane) map.getRoads().get(2).getExitApproachLane());
        Lane lane = exitApproachLane.getArcLaneDecomposition().get(0);

        //act


        //assert
        assert roadBasedIntersection.getExitPoint(lane) == null;
    }


    @Test
    public void calcTurnDirection_withNorthAndSameRoad_returnsStraightTurnDirection() {
        //arrange
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
        // Starting north Road
        Road northRoad = map.getRoads().get(2);
        // Current lane
        Lane currentLane = northRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = northRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.STRAIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withEastAndSameRoad_returnsStraightTurnDirection() {
        //arrange
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
        // Starting north Road
        Road eastRoad = map.getRoads().get(0);
        // Current lane
        Lane currentLane = eastRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = eastRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.STRAIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withWestAndSameRoad_returnsStraightTurnDirection() {
        //arrange
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
        // Starting north Road
        Road westRoad = map.getRoads().get(1);
        // Current lane
        Lane currentLane = westRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = westRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.STRAIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withSouthAndSameRoad_returnsStraightTurnDirection() {
        //arrange
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
        // Starting north Road
        Road southRoad = map.getRoads().get(3);
        // Current lane
        Lane currentLane = southRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = southRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.STRAIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withNorthAndDualRoad_returnsUTurnDirection() {
        //arrange
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
        // Starting north Road
        Road northRoad = map.getRoads().get(2);
        // Destination south Road
        Road southRoad = map.getRoads().get(3);
        // Current lane
        Lane currentLane = northRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = southRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.U_TURN;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withEastAndDualRoad_returnsUTurnDirection() {
        //arrange
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
        // Starting north Road
        Road eastRoad = map.getRoads().get(0);
        // Destination south Road
        Road westRoad = map.getRoads().get(1);
        // Current lane
        Lane currentLane = eastRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = westRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.U_TURN;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withWestAndDualRoad_returnsUTurnDirection() {
        //arrange
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
        // Starting north Road
        Road westRoad = map.getRoads().get(1);
        // Destination south Road
        Road eastRoad = map.getRoads().get(0);
        // Current lane
        Lane currentLane = westRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = eastRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.U_TURN;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withSouthAndDualRoad_returnsUTurnDirection() {
        //arrange
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
        // Starting north Road
        Road southRoad = map.getRoads().get(3);
        // Destination south Road
        Road northRoad = map.getRoads().get(2);
        // Current lane
        Lane currentLane = southRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = northRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.U_TURN;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withNorthAndEastRoad_returnsRightDirection() {
        //arrange
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
        // Starting north Road
        Road northRoad = map.getRoads().get(2);
        // Destination east Road
        Road eastRoad = map.getRoads().get(0);
        // Current lane
        Lane currentLane = northRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = eastRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.RIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withEastAndSouthRoad_returnsRightDirection() {
        //arrange
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
        // Starting north Road
        Road eastRoad = map.getRoads().get(0);
        // Destination east Road
        Road southRoad = map.getRoads().get(3);
        // Current lane
        Lane currentLane = eastRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = southRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.RIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withWestAndNorthRoad_returnsRightDirection() {
        //arrange
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
        // Starting north Road
        Road westRoad = map.getRoads().get(1);
        // Destination east Road
        Road northRoad = map.getRoads().get(2);
        // Current lane
        Lane currentLane = westRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = northRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.RIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withSouthAndWestRoad_returnsRightDirection() {
        //arrange
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
        // Starting north Road
        Road southRoad = map.getRoads().get(3);
        // Destination east Road
        Road westRoad = map.getRoads().get(1);
        // Current lane
        Lane currentLane = southRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = westRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.RIGHT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withNorthAndWestRoad_returnsLeftDirection() {
        //arrange
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
        // Starting north Road
        Road northRoad = map.getRoads().get(2);
        // Destination east Road
        Road westRoad = map.getRoads().get(1);
        // Current lane
        Lane currentLane = northRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = westRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.LEFT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withEastAndNorthRoad_returnsLeftDirection() {
        //arrange
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
        // Starting north Road
        Road eastRoad = map.getRoads().get(0);
        // Destination east Road
        Road northRoad = map.getRoads().get(2);
        // Current lane
        Lane currentLane = eastRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = northRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.LEFT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withWestAndSouthRoad_returnsLeftDirection() {
        //arrange
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
        // Starting north Road
        Road westRoad = map.getRoads().get(1);
        // Destination east Road
        Road southRoad = map.getRoads().get(3);
        // Current lane
        Lane currentLane = westRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = southRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.LEFT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }

    @Test
    public void calcTurnDirection_withSouthAndEastRoad_returnsLeftDirection() {
        //arrange
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
        // Starting north Road
        Road southRoad = map.getRoads().get(3);
        // Destination east Road
        Road eastRoad = map.getRoads().get(0);
        // Current lane
        Lane currentLane = southRoad.getContinuousLanes().get(1);
        // Destination lane
        Lane destinationLane = eastRoad.getContinuousLanes().get(7);
        Constants.TurnDirection expected = Constants.TurnDirection.LEFT;

        //act
        Constants.TurnDirection actual = roadBasedIntersection.calcTurnDirection(currentLane, destinationLane);

        //assert
        assertEquals(expected, actual);
    }
}
