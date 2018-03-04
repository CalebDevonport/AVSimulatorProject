package aim4.rim.im;

import aim4.config.Debug;
import aim4.im.rim.IntersectionManager;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import aim4.util.ArrayListRegistry;
import aim4.util.Registry;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class IntersectionManagerTests {
    private static final double CURRENT_TIME = 0.0;
    private static final List<Double> ROUNDABOUT_DIAMETER = Arrays.asList(30.0, 35.0, 40.0, 45.0);
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  6.04;

    @Test
    public void registerWithLanes_withLaneRim_registersIM() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap();

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create Track Model
        RoadBasedTrackModel roadBasedTrackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        //act
        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, roadBasedTrackModel, CURRENT_TIME, registry);

        //assert
        intersectionManager.getIntersection().getLanes().forEach( lane -> {
            if (lane instanceof ArcSegmentLane) {
                assert lane.getLaneRIM().firstIntersectionManager().getId() == intersectionManager.getId();
                ((ArcSegmentLane) lane).getArcLaneDecomposition().forEach(lineLane -> {
                    assert lineLane.getLaneRIM().firstIntersectionManager().getId() == intersectionManager.getId();
                });
            }
        });
        // Check departure lane for each arrival lane
        // East choices
        assert intersectionManager.getSortedDepartureLanes(
                getEastRoad().getEntryApproachLane(), //arrival lane
                getSouthRoad()).get(0)
                == getSouthRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getEastRoad().getEntryApproachLane(), //arrival lane
                getEastRoad()).get(0)
                == getEastRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getEastRoad().getEntryApproachLane(), //arrival lane
                getNorthRoad()).get(0)
                == getNorthRoad().getExitApproachLane(); // departure road

        // North
        assert intersectionManager.getSortedDepartureLanes(
                getNorthRoad().getEntryApproachLane(), //arrival lane
                getEastRoad()).get(0)
                == getEastRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getNorthRoad().getEntryApproachLane(), //arrival lane
                getNorthRoad()).get(0)
                == getNorthRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getNorthRoad().getEntryApproachLane(), //arrival lane
                getWestRoad()).get(0)
                == getWestRoad().getExitApproachLane(); // departure road

        // West
        assert intersectionManager.getSortedDepartureLanes(
                getWestRoad().getEntryApproachLane(), //arrival lane
                getSouthRoad()).get(0)
                == getSouthRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getWestRoad().getEntryApproachLane(), //arrival lane
                getWestRoad()).get(0)
                == getWestRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getWestRoad().getEntryApproachLane(), //arrival lane
                getNorthRoad()).get(0)
                == getNorthRoad().getExitApproachLane(); // departure road

        // South
        assert intersectionManager.getSortedDepartureLanes(
                getSouthRoad().getEntryApproachLane(), //arrival lane
                getEastRoad()).get(0)
                == getEastRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getSouthRoad().getEntryApproachLane(), //arrival lane
                getSouthRoad()).get(0)
                == getSouthRoad().getExitApproachLane(); // departure road
        assert intersectionManager.getSortedDepartureLanes(
                getSouthRoad().getEntryApproachLane(), //arrival lane
                getWestRoad()).get(0)
                == getWestRoad().getExitApproachLane(); // departure road



    }

    private RimIntersectionMap getRimIntersectionMap() {
        return new RimIntersectionMap(
                0,
                1,
                1,
                ROUNDABOUT_DIAMETER.get(0),
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);
    }
    private Road getNorthRoad(){
        return Debug.currentRimMap.getRoads().get(2);
    }

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
