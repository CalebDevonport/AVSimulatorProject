package aim4.rim.map;

import aim4.im.rim.IntersectionManager;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.im.rim.TrackModel;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class LaneRIMTests {
    private static final double CURRENT_TIME = 1.0;
    private static final double ARRIVAL_TIME = 6.0;
    private static final double STATIC_BUFFER_SIZE = 0.25;
    private static final double INTERNAL_TILE_TIME_BUFFER_SIZE = 0.1;
    private static final double GRANULARITY = 6.0;
    private static final List<Double> ROUNDABOUT_DIAMETER = Arrays.asList(30.0, 35.0, 40.0, 45.0);
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  9.7222;
    private static final double DELTA = 0.5e-2; // necessary for assertEquals for doubles

    @Test
    public void registerIntersectionManager_withLanesAndIM_doesRegistration() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());

        //assert
        intersectionManager.getIntersection().getLanes().forEach( lane -> {
            if (lane instanceof ArcSegmentLane) {
                ((ArcSegmentLane) lane).getArcLaneDecomposition().forEach(lineSegmentLane -> {
                    assert lineSegmentLane.getLaneRIM().firstIntersectionManager() != null;
                });
            } else {
                assert lane.getLaneRIM().firstIntersectionManager() != null;
            }
        });

    }

    @Test
    public void nextIntersectionManager_withPointOnStartOfLane_returnsNextIntersectionManager() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());

                //assert
        intersectionManager.getIntersection().getLanes().forEach( lane -> {
            AtomicReference<Point2D> point = new AtomicReference<>(lane.getStartPoint());
            if (lane instanceof ArcSegmentLane) {
                ((ArcSegmentLane) lane).getArcLaneDecomposition().forEach(lineSegmentLane -> {
                    point.set(lineSegmentLane.getStartPoint());
                    assert lineSegmentLane.getLaneRIM().nextIntersectionManager(point.get()) != null;
                });
            } else {
                assert lane.getLaneRIM().nextIntersectionManager(point.get()) != null;
            }
        });

    }

    @Test
    public void nextIntersectionManager_withPointOnEndOfLane_returnsNextIntersectionManager() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());

        //assert
        intersectionManager.getIntersection().getLanes().forEach( lane -> {
            AtomicReference<Point2D> point = new AtomicReference<>(lane.getEndPoint());
            if (lane instanceof ArcSegmentLane) {
                ((ArcSegmentLane) lane).getArcLaneDecomposition().forEach(lineSegmentLane -> {
                    point.set(lineSegmentLane.getEndPoint());
                    assert lineSegmentLane.getLaneRIM().nextIntersectionManager(point.get()) != null;
                });
            } else {
                assert lane.getLaneRIM().nextIntersectionManager(point.get()) != null;
            }
        });

    }

    @Test
    public void distanceToFirstIntersection_withLaneInBeginning_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane beginningLane = intersectionManager.getIntersection().getEntryRoads().get(0).getContinuousLanes().get(0);
        ArcSegmentLane entryApproachLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane();

        double expected =  beginningLane.getLength() + entryApproachLane.getLengthArcLaneDecomposition();
        double actual = beginningLane.getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToFirstIntersection_withLaneApproaching_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        ArcSegmentLane entryApproachLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane();

        double expected =  entryApproachLane.getLengthArcLaneDecomposition();
        double actual = entryApproachLane.getArcLaneDecomposition().get(0).getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToFirstIntersection_withLaneInsideIntersectionInBeginning_returns0Distance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        ArcSegmentLane entryMergingLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getEntryMergingLane();

        double expected =  0.0;
        double actual = entryMergingLane.getArcLaneDecomposition().get(0).getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToFirstIntersection_withLaneInsideIntersectionInEnd_returns0Distance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        ArcSegmentLane exitMergingLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getExitMergingLane();

        double expected =  0.0;
        double actual = exitMergingLane.getArcLaneDecomposition().get(exitMergingLane.getArcLaneDecomposition().size() - 1).getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToFirstIntersection_withLaneOutsideAndPassedIntersection_returnsFarAwayDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        ArcSegmentLane exitApproachLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();

        double expected =  Double.MAX_VALUE;
        double actual = exitApproachLane.getArcLaneDecomposition().get(0).getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToFirstIntersection_withLaneOutsideAndPassedIntersectionByMore_returnsFarAwayDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        ArcSegmentLane exitApproachLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();

        double expected =  Double.MAX_VALUE;
        double actual = exitApproachLane.getArcLaneDecomposition().get(exitApproachLane.getArcLaneDecomposition().size() - 1).getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToFirstIntersection_withLaneOutsideAndPassedIntersectionByMoreMore_returnsFarAwayDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane lineSegmentLane = intersectionManager.getIntersection().getEntryRoads().get(0).getContinuousLanes().get
                (intersectionManager.getIntersection().getEntryRoads().get(0).getContinuousLanes().size() - 1);

        double expected =  Double.MAX_VALUE;
        double actual = lineSegmentLane.getLaneRIM().distanceToFirstIntersection();

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToNextIntersection_withLaneInBeginningAndPointAtStart_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane beginningLane = intersectionManager.getIntersection().getEntryRoads().get(0).getContinuousLanes().get(0);
        ArcSegmentLane entryApproachLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane();
        Point2D point = beginningLane.getStartPoint();

        double expected =  beginningLane.getLength() + entryApproachLane.getLengthArcLaneDecomposition();
        double actual = beginningLane.getLaneRIM().distanceToNextIntersection(point);

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void distanceToNextIntersection_withLaneInBeginningAndPointAtEnd_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane beginningLane = intersectionManager.getIntersection().getEntryRoads().get(0).getContinuousLanes().get(0);
        ArcSegmentLane entryApproachLane = (ArcSegmentLane) intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane();
        Point2D point = beginningLane.getEndPoint();

        double expected =  entryApproachLane.getLengthArcLaneDecomposition();
        double actual = beginningLane.getLaneRIM().distanceToNextIntersection(point);

        //assert
        assertEquals(expected,actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneFarAfterTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane().getNextLane();

        double expected = endingLane.getLength() +
                ((ArcSegmentLane)intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane()).getLengthArcLaneDecomposition();
        double actual = endingLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneAfterTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() -1);

        double expected = ((ArcSegmentLane) endingLane).getLengthArcLaneDecomposition();
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneJustAfterTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);

        double expected = endingLaneLane.getLength();
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneJustBeforeExitingTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitMergingLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1);

        double expected = 0.0;
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneBeforeExitingTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitMergingLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);

        double expected = 0.0;
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneInsideTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitMergingLane().getPrevLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);

        double expected = 0.0;
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneJustAfterEntryTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryMergingLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);

        double expected = 0.0;
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneJustBeforeEntryTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1);

        double expected = Double.MAX_VALUE;
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneBeforeEntryTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane();
        LineSegmentLane endingLaneLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);

        double expected = Double.MAX_VALUE;
        double actual = endingLaneLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void remainingDistanceFromLastIntersection_withLaneWayBeforeEntryTheIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryApproachLane().getPrevLane();

        double expected = Double.MAX_VALUE;
        double actual = endingLane.getLaneRIM().remainingDistanceFromLastIntersection();

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnEndOfLaneAndAfterIntersectionLane_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane().getNextLane();
        Point2D point = endingLane.getEndPoint();

        double expected = endingLane.getLength() +
                ((ArcSegmentLane)intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane()).getLengthArcLaneDecomposition();
        double actual = endingLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnStartOfLaneAndAfterIntersectionLane_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane().getNextLane();
        Point2D point = endingLane.getStartPoint();

        double expected =
                ((ArcSegmentLane)intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane()).getLengthArcLaneDecomposition();
        double actual = endingLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnEndOfLaneAndJustAfterIntersectionLane_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();
        Point2D point = endingLane.getEndPoint();

        double expected =
                ((ArcSegmentLane)intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane()).getLengthArcLaneDecomposition();
        double actual = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1).getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnStartOfLaneAndABitAfterIntersectionLane_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1);
        Point2D point = endingLineLane.getStartPoint();

        double expected = endingLineLane.getLength() * 3;
        double actual = endingLineLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnStartOfLaneAndJustAfterIntersectionLane_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitApproachLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);
        Point2D point = endingLineLane.getStartPoint();

        double expected = 0.0;
        double actual = endingLineLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnEndInsideIntersectionLaneCloseToExit_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getExitMergingLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1);
        Point2D point = endingLineLane.getEndPoint();

        double expected = 0.0;
        double actual = endingLineLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnEndInsideIntersectionCloseToEntry_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryMergingLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);
        Point2D point = endingLineLane.getEndPoint();

        double expected = 0.0;
        double actual = endingLineLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnStartInsideIntersectionCloseToEntry_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryMergingLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(0);
        Point2D point = endingLineLane.getStartPoint();

        double expected = 0.0;
        double actual = endingLineLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void distanceFromPrevIntersection_withPointOnEndBeforeIntersection_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryMergingLane().getPrevLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1);
        Point2D point = endingLineLane.getEndPoint();

        double expected = Double.MAX_VALUE;
        double actual = endingLineLane.getLaneRIM().distanceFromPrevIntersection(point);

        //assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    public void nextIntersectionManager_withGivenIntersectionManager_returnsDistance() {
        //arrange
        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        //act

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, map.getImRegistry());
        Lane endingLane = intersectionManager.getIntersection().getEntryRoads().get(0).getEntryMergingLane().getPrevLane();
        LineSegmentLane endingLineLane = ((ArcSegmentLane) endingLane).getArcLaneDecomposition().get(((ArcSegmentLane) endingLane).getArcLaneDecomposition().size() - 1);

        IntersectionManager actual = endingLineLane.getLaneRIM().nextIntersectionManager(endingLineLane.getLaneRIM().firstIntersectionManager());

        //assert
        assert actual == null;
    }



    private RimIntersectionMap getRimIntersectionMap(double roundaboutDiameter) {
        return new RimIntersectionMap(
                0,
                1,
                1,
                roundaboutDiameter,
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);
    }
}
