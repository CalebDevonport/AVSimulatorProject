package aim4.rim.map;

import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class RimIntersectionMapTests {
    private static final double NO_VEHICLE_ZONE_LENGTH = 28.0;
    private static final double[] ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  31.0686;
    private static final double ROUNDABOUT_SPEED_LIMIT =  21.748;
    private static final double DELTA = 0.5e-2; // necessary for assertEquals for doubles



    @Test
    public void mapConstruction_withFields_returnsRimIntersectionMap() {
        //arrange

        //act
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
        //assert
        assert map.getColumns() == 1;
        assert map.getRows() == 1;
        assert map.getDimensions().getWidth() == 250;
        assert map.getDimensions().getHeight() == 250;
        assert map.getDataCollectionLines().size() == 8;
        List<Road> verticalRoads = map.getVerticalRoads();
        List<Road> horizontalRoads = map.getHorizontalRoads();
        assert !verticalRoads.isEmpty();
        assert verticalRoads.size() == 2;
        assert !horizontalRoads.isEmpty();
        assert horizontalRoads.size() == 2;
    }

    @Test
    public void mapConstructionNorthRoad_withFields_returnsRimIntersectionMap() {
        //arrange

        //act
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
        //assert
        List<Road> verticalRoads = map.getVerticalRoads();
        Road north = verticalRoads.get(0);
        assert north.getContinuousLanes().size() == 7;
        Lane northLine1 = north.getContinuousLanes().get(0);
        Lane northArc2 = north.getContinuousLanes().get(1);
        Lane northArc3 = north.getContinuousLanes().get(2);
        Lane northArc4 = north.getContinuousLanes().get(3);
        Lane northArc5 = north.getContinuousLanes().get(4);
        Lane northArc6 = north.getContinuousLanes().get(5);
        Lane northLine7 = north.getContinuousLanes().get(6);

        assert northLine1.getNextLane() == northArc2;
        assert northArc2.getNextLane() == northArc3;
        assert northArc3.getNextLane() == northArc4;
        assert northArc4.getNextLane() == northArc5;
        assert northArc5.getNextLane() == northArc6;
        assert northArc6.getNextLane() == northLine7;

        assertEquals(northLine1.getEndPoint().getX(),northArc2.getStartPoint().getX(), DELTA);
        assertEquals(northLine1.getEndPoint().getY(),northArc2.getStartPoint().getY(), DELTA);
        assertEquals(northArc2.getEndPoint().getX(),northArc3.getEndPoint().getX(), DELTA);
        assertEquals(northArc2.getEndPoint().getY(),northArc3.getEndPoint().getY(), DELTA);
        assertEquals(northArc3.getStartPoint().getX(),northArc4.getEndPoint().getX(), DELTA);
        assertEquals(northArc3.getStartPoint().getY(),northArc4.getEndPoint().getY(), DELTA);
        assertEquals(northArc4.getStartPoint().getX(),northArc5.getEndPoint().getX(), DELTA);
        assertEquals(northArc4.getStartPoint().getY(),northArc5.getEndPoint().getY(), DELTA);
        assertEquals(northArc5.getStartPoint().getX(),northArc6.getStartPoint().getX(), DELTA);
        assertEquals(northArc5.getStartPoint().getY(),northArc6.getStartPoint().getY(), DELTA);
        assertEquals(northArc6.getEndPoint().getX(),northLine7.getStartPoint().getX(), DELTA);
        assertEquals(northArc6.getEndPoint().getY(),northLine7.getStartPoint().getY(), DELTA);

        assert north.getDual() == verticalRoads.get(1);
    }

    @Test
    public void mapConstructionSouthRoad_withFields_returnsRimIntersectionMap() {
        //arrange

        //act
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
        //assert
        List<Road> verticalRoads = map.getVerticalRoads();
        Road south = verticalRoads.get(1);
        assert south.getContinuousLanes().size() == 7;
        Lane southLine1 = south.getContinuousLanes().get(0);
        Lane southArc2 = south.getContinuousLanes().get(1);
        Lane southArc3 = south.getContinuousLanes().get(2);
        Lane southArc4 = south.getContinuousLanes().get(3);
        Lane southArc5 = south.getContinuousLanes().get(4);
        Lane southArc6 = south.getContinuousLanes().get(5);
        Lane southLine7 = south.getContinuousLanes().get(6);

        assert southLine1.getNextLane() == southArc2;
        assert southArc2.getNextLane() == southArc3;
        assert southArc3.getNextLane() == southArc4;
        assert southArc4.getNextLane() == southArc5;
        assert southArc5.getNextLane() == southArc6;
        assert southArc6.getNextLane() == southLine7;

        assertEquals(southLine1.getEndPoint().getX(),southArc2.getStartPoint().getX(), DELTA);
        assertEquals(southLine1.getEndPoint().getY(),southArc2.getStartPoint().getY(), DELTA);
        assertEquals(southArc2.getEndPoint().getX(),southArc3.getEndPoint().getX(), DELTA);
        assertEquals(southArc2.getEndPoint().getY(),southArc3.getEndPoint().getY(), DELTA);
        assertEquals(southArc3.getStartPoint().getX(),southArc4.getEndPoint().getX(), DELTA);
        assertEquals(southArc3.getStartPoint().getY(),southArc4.getEndPoint().getY(), DELTA);
        assertEquals(southArc4.getStartPoint().getX(),southArc5.getEndPoint().getX(), DELTA);
        assertEquals(southArc4.getStartPoint().getY(),southArc5.getEndPoint().getY(), DELTA);
        assertEquals(southArc5.getStartPoint().getX(),southArc6.getStartPoint().getX(), DELTA);
        assertEquals(southArc5.getStartPoint().getY(),southArc6.getStartPoint().getY(), DELTA);
        assertEquals(southArc6.getEndPoint().getX(),southLine7.getStartPoint().getX(), DELTA);
        assertEquals(southArc6.getEndPoint().getY(),southLine7.getStartPoint().getY(), DELTA);
        assert south.getDual() == verticalRoads.get(0);
    }

    @Test
    public void mapConstructionEastRoad_withFields_returnsRimIntersectionMap() {
        //arrange

        //act
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
        //assert
        List<Road> horizontalRoads = map.getHorizontalRoads();
        Road east = horizontalRoads.get(0);
        assert east.getContinuousLanes().size() == 7;
        Lane eastLine1 = east.getContinuousLanes().get(0);
        Lane eastArc2 = east.getContinuousLanes().get(1);
        Lane eastArc3 = east.getContinuousLanes().get(2);
        Lane eastArc4 = east.getContinuousLanes().get(3);
        Lane eastArc5 = east.getContinuousLanes().get(4);
        Lane eastArc6 = east.getContinuousLanes().get(5);
        Lane eastArc7 = east.getContinuousLanes().get(6);

        assert eastLine1.getNextLane() == eastArc2;
        assert eastArc2.getNextLane() == eastArc3;
        assert eastArc3.getNextLane() == eastArc4;
        assert eastArc4.getNextLane() == eastArc5;
        assert eastArc5.getNextLane() == eastArc6;
        assert eastArc6.getNextLane() == eastArc7;

        assertEquals(eastLine1.getEndPoint().getX(),eastArc2.getStartPoint().getX(), DELTA);
        assertEquals(eastLine1.getEndPoint().getY(),eastArc2.getStartPoint().getY(), DELTA);
        assertEquals(eastArc2.getEndPoint().getX(),eastArc3.getEndPoint().getX(), DELTA);
        assertEquals(eastArc2.getEndPoint().getY(),eastArc3.getEndPoint().getY(), DELTA);
        assertEquals(eastArc3.getStartPoint().getX(),eastArc4.getEndPoint().getX(), DELTA);
        assertEquals(eastArc3.getStartPoint().getY(),eastArc4.getEndPoint().getY(), DELTA);
        assertEquals(eastArc4.getStartPoint().getX(),eastArc5.getEndPoint().getX(), DELTA);
        assertEquals(eastArc4.getStartPoint().getY(),eastArc5.getEndPoint().getY(), DELTA);
        assertEquals(eastArc5.getStartPoint().getX(),eastArc6.getStartPoint().getX(), DELTA);
        assertEquals(eastArc5.getStartPoint().getY(),eastArc6.getStartPoint().getY(), DELTA);
        assertEquals(eastArc6.getEndPoint().getX(),eastArc7.getStartPoint().getX(), DELTA);
        assertEquals(eastArc6.getEndPoint().getY(),eastArc7.getStartPoint().getY(), DELTA);
        assert east.getDual() == horizontalRoads.get(1);
    }

    @Test
    public void mapConstructionWestRoad_withFields_returnsRimIntersectionMap() {
        //arrange

        //act
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
        //assert
        List<Road> horizontalRoads = map.getHorizontalRoads();
        Road west = horizontalRoads.get(1);
        assert west.getContinuousLanes().size() == 7;
        Lane westLine1 = west.getContinuousLanes().get(0);
        Lane westArc2 = west.getContinuousLanes().get(1);
        Lane westArc3 = west.getContinuousLanes().get(2);
        Lane westArc4 = west.getContinuousLanes().get(3);
        Lane westArc5 = west.getContinuousLanes().get(4);
        Lane westArc6 = west.getContinuousLanes().get(5);
        Lane westArc7 = west.getContinuousLanes().get(6);

        assert westLine1.getNextLane() == westArc2;
        assert westArc2.getNextLane() == westArc3;
        assert westArc3.getNextLane() == westArc4;
        assert westArc4.getNextLane() == westArc5;
        assert westArc5.getNextLane() == westArc6;
        assert westArc6.getNextLane() == westArc7;

        assertEquals(westLine1.getEndPoint().getX(),westArc2.getStartPoint().getX(), DELTA);
        assertEquals(westLine1.getEndPoint().getY(),westArc2.getStartPoint().getY(), DELTA);
        assertEquals(westArc2.getEndPoint().getX(),westArc3.getEndPoint().getX(), DELTA);
        assertEquals(westArc2.getEndPoint().getY(),westArc3.getEndPoint().getY(), DELTA);
        assertEquals(westArc3.getStartPoint().getX(),westArc4.getEndPoint().getX(), DELTA);
        assertEquals(westArc3.getStartPoint().getY(),westArc4.getEndPoint().getY(), DELTA);
        assertEquals(westArc4.getStartPoint().getX(),westArc5.getEndPoint().getX(), DELTA);
        assertEquals(westArc4.getStartPoint().getY(),westArc5.getEndPoint().getY(), DELTA);
        assertEquals(westArc5.getStartPoint().getX(),westArc6.getStartPoint().getX(), DELTA);
        assertEquals(westArc5.getStartPoint().getY(),westArc6.getStartPoint().getY(), DELTA);
        assertEquals(westArc6.getEndPoint().getX(),westArc7.getStartPoint().getX(), DELTA);
        assertEquals(westArc6.getEndPoint().getY(),westArc7.getStartPoint().getY(), DELTA);
        assert west.getDual() == horizontalRoads.get(0);
    }
}
