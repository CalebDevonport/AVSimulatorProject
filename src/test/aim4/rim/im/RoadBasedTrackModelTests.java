package aim4.rim.im;

import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class RoadBasedTrackModelTests {
    private static final double[] ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  31.0686;
    private static final double ROUNDABOUT_SPEED_LIMIT =  21.748;
    private static final double DELTA = 0.5e-2; // necessary for assertEquals for doubles

    @Test
    public void calculateLanePriorities_withEntryLanes_returnsExitLanes() {
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
        Road north = roadBasedIntersection.getRoadByName("1st Avenue N");
        Road west = roadBasedIntersection.getRoadByName("1st Street W");
        Road south = roadBasedIntersection.getRoadByName("1st Avenue S");
        Road east = roadBasedIntersection.getRoadByName("1st Street E");

        //act
        RoadBasedTrackModel roadBasedTrackModel = new RoadBasedTrackModel(roadBasedIntersection);
        //north
        List<Lane> actualNorthToNorth = roadBasedTrackModel.getSortedDepartureLanes(north.getEntryApproachLane(), north);
        List<Lane> actualNorthToEast = roadBasedTrackModel.getSortedDepartureLanes(north.getEntryApproachLane(), east);
        List<Lane> actualNorthToWest = roadBasedTrackModel.getSortedDepartureLanes(north.getEntryApproachLane(), west);
        //west
        List<Lane> actualWestToNorth = roadBasedTrackModel.getSortedDepartureLanes(west.getEntryApproachLane(), north);
        List<Lane> actualWestToSouth = roadBasedTrackModel.getSortedDepartureLanes(west.getEntryApproachLane(), south);
        List<Lane> actualWestToWest = roadBasedTrackModel.getSortedDepartureLanes(west.getEntryApproachLane(), west);
        //east
        List<Lane> actualEastToNorth = roadBasedTrackModel.getSortedDepartureLanes(east.getEntryApproachLane(), north);
        List<Lane> actualEastToSouth = roadBasedTrackModel.getSortedDepartureLanes(east.getEntryApproachLane(), south);
        List<Lane> actualEastToEast = roadBasedTrackModel.getSortedDepartureLanes(east.getEntryApproachLane(), east);
        //south
        List<Lane> actualSouthToSouth = roadBasedTrackModel.getSortedDepartureLanes(south.getEntryApproachLane(), south);
        List<Lane> actualSouthToEast = roadBasedTrackModel.getSortedDepartureLanes(south.getEntryApproachLane(), east);
        List<Lane> actualSouthToWest = roadBasedTrackModel.getSortedDepartureLanes(south.getEntryApproachLane(), west);

        //assert
        assert roadBasedIntersection.getEntryLanes().size() == 4;
        assert  actualNorthToNorth.get(0).getId() == north.getExitApproachLane().getId();
        assert  actualNorthToEast.get(0).getId() == east.getExitApproachLane().getId();
        assert  actualNorthToWest.get(0).getId() == west.getExitApproachLane().getId();

        assert  actualWestToNorth.get(0).getId() == north.getExitApproachLane().getId();
        assert  actualWestToSouth.get(0).getId() == south.getExitApproachLane().getId();
        assert  actualWestToWest.get(0).getId() == west.getExitApproachLane().getId();

        assert  actualEastToNorth.get(0).getId() == north.getExitApproachLane().getId();
        assert  actualEastToSouth.get(0).getId() == south.getExitApproachLane().getId();
        assert  actualEastToEast.get(0).getId() == east.getExitApproachLane().getId();

        assert  actualSouthToSouth.get(0).getId() == south.getExitApproachLane().getId();
        assert  actualSouthToEast.get(0).getId() == east.getExitApproachLane().getId();
        assert  actualSouthToWest.get(0).getId() == west.getExitApproachLane().getId();

    }

    @Test
    public void traversalDistance_withArrivalAndDepartureRoads_returnsExitLanes() {
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
        Road north = roadBasedIntersection.getRoadByName("1st Avenue N");
        Road west = roadBasedIntersection.getRoadByName("1st Street W");
        Road south = roadBasedIntersection.getRoadByName("1st Avenue S");
        Road east = roadBasedIntersection.getRoadByName("1st Street E");

        //act
        RoadBasedTrackModel roadBasedTrackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //assert
        assert roadBasedIntersection.getEntryLanes().size() == 4;
        assertEquals(roadBasedTrackModel.traversalDistance(north, north), roadBasedTrackModel.traversalDistance(south, south), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(east, east), roadBasedTrackModel.traversalDistance(west, west), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(east, east), roadBasedTrackModel.traversalDistance(north, north), DELTA);

        assertEquals(roadBasedTrackModel.traversalDistance(north.getEntryApproachLane(), north.getExitApproachLane()),
                roadBasedTrackModel.traversalDistance(south.getEntryApproachLane(), south.getExitApproachLane()), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(east.getEntryApproachLane(), east.getExitApproachLane()),
                roadBasedTrackModel.traversalDistance(west.getEntryApproachLane(), west.getExitApproachLane()), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(east.getEntryApproachLane(), east.getExitApproachLane()),
                roadBasedTrackModel.traversalDistance(north.getEntryApproachLane(), north.getExitApproachLane()), DELTA);

        assertEquals(roadBasedTrackModel.traversalDistance(north, east), roadBasedTrackModel.traversalDistance(west, north), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(west, north), roadBasedTrackModel.traversalDistance(south, west), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(south, west), roadBasedTrackModel.traversalDistance(east, south), DELTA);

        assertEquals(roadBasedTrackModel.traversalDistance(north, west), roadBasedTrackModel.traversalDistance(west, south), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(west, south), roadBasedTrackModel.traversalDistance(south, east), DELTA);
        assertEquals(roadBasedTrackModel.traversalDistance(south, east), roadBasedTrackModel.traversalDistance(east, north), DELTA);

        assertTrue(roadBasedTrackModel.traversalDistance(north, west) > roadBasedTrackModel.traversalDistance(north, north));
        assertTrue(roadBasedTrackModel.traversalDistance(north, north) > roadBasedTrackModel.traversalDistance(north, east));
    }
}
