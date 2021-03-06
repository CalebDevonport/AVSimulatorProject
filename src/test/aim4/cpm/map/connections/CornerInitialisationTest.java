package aim4.cpm.map.connections;

import aim4.map.Road;
import aim4.map.connections.Corner;
import aim4.map.cpm.testmaps.CPMMapWithCornersOneLane;
import aim4.map.lane.Lane;
import org.junit.Test;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.Assert.*;

/**
 * TEST SUITE PURPOSE: Check that when 2 roads are given, a corner with the correct attributes is created.
 */
public class CornerInitialisationTest {
    CPMMapWithCornersOneLane map = new CPMMapWithCornersOneLane(4, // laneWidth
            10.0, // speedLimit
            0.0, // initTime
            500, // width
            500); // height
    Corner testCorner = map.getCorners().get(0);

    @Test
    public void testGetEntryLanes() throws Exception {
        List<Lane> entryLanes = testCorner.getEntryLanes();

        // There should be one entry lane
        int size = entryLanes.size();
        assertEquals(1, size);
    }

    @Test
    public void testGetExitLanes() throws Exception {
        List<Lane> exitLanes = testCorner.getExitLanes();

        // There should be one exit lane
        int size = exitLanes.size();
        assertEquals(1, size);
    }

    @Test
    public void testGetRoads() throws Exception {
        List<Road> roads = testCorner.getRoads();

        // There should be 2 roads involved
        int size = roads.size();
        assertEquals(2, size);
    }

    @Test
    public void testGetLanes() throws Exception {
        List<Lane> lanes = testCorner.getLanes();

        // There should be 2 lanes involved
        int size = lanes.size();
        assertEquals(2, size);
    }

    @Test
    public void testGetArea() throws Exception {
        Area area = testCorner.getArea();

        assert(area.isRectangular());
    }

    @Test
    public void testGetCentroid() throws Exception {
        /*double expectedCentroidX = 472;
        double expectedCentroidY = 472;
        Point2D centroid = testCorner.getCentroid();
        assertEquals(expectedCentroidX, centroid.getX(), 0.01);
        assertEquals(expectedCentroidY, centroid.getY(), 0.01);*/

        // TODO CPM WHAT IS THIS DOING
    }

    @Test
    public void testGetEntryRoads() throws Exception {
        List<Road> roads = testCorner.getEntryRoads();

        // There should be only 1 entry
        int size = roads.size();
        assertEquals(1, size);

        // The road should be eastbound
        String roadName = roads.get(0).getName();
        assert(roadName.equals("Eastbound Avenue"));
    }

    @Test
    public void testIsEnteredBy() throws Exception {
        Road entryRoad = map.getRoadByName("Eastbound Avenue");
        Lane entryLane = entryRoad.getOnlyLane();
        assert(testCorner.isEnteredBy(entryLane));

        Road exitRoad = map.getRoadByName("Southbound Avenue");
        Lane exitLane = exitRoad.getOnlyLane();
        assertFalse(testCorner.isEnteredBy(exitLane));
    }

    @Test
    public void testGetEntryPoint() throws Exception {
        Road entryRoad = map.getRoadByName("Eastbound Avenue");
        Lane entryLane = entryRoad.getOnlyLane();
        Point2D entryPoint = testCorner.getEntryPoint(entryLane);

        assertEquals(470.0, entryPoint.getX(), 0.01);
        assertEquals(470.0, entryPoint.getY(), 0.01);
    }

    @Test
    public void testGetEntryHeading() throws Exception {
        // Enter on east
        Road entryRoad = map.getRoadByName("Eastbound Avenue");
        Lane entryLane = entryRoad.getOnlyLane();
        double expectedEntryHeading = 0.0;
        double actualEntryHeading = testCorner.getEntryHeading(entryLane);

        assertEquals(expectedEntryHeading, actualEntryHeading, 0.01);
    }

    @Test
    public void testGetExitRoads() throws Exception {
        List<Road> roads = testCorner.getExitRoads();

        // There should be only 1 exit
        int size = roads.size();
        assertEquals(1, size);

        // The road should be southbound
        String roadName = roads.get(0).getName();
        assert(roadName.equals("Southbound Avenue"));
    }

    @Test
    public void testIsExitedBy() throws Exception {
        Road exitRoad = map.getRoadByName("Southbound Avenue");
        Lane exitLane = exitRoad.getOnlyLane();
        assert(testCorner.isExitedBy(exitLane));

        Road entryRoad = map.getRoadByName("Eastbound Avenue");
        Lane entryLane = entryRoad.getOnlyLane();
        assertFalse(testCorner.isExitedBy(entryLane));
    }

    @Test
    public void testGetExitPoint() throws Exception {
        Road exitRoad = map.getRoadByName("Southbound Avenue");
        Lane exitLane = exitRoad.getOnlyLane();
        Point2D exitPoint = testCorner.getExitPoint(exitLane);

        assertEquals(472.0, exitPoint.getX(), 0.01);
        assertEquals(468.0, exitPoint.getY(), 0.01);
    }

    @Test
    public void testGetExitHeading() throws Exception {
        // Exit on south
        Road exitRoad = map.getRoadByName("Southbound Avenue");
        Lane exitLane = exitRoad.getOnlyLane();
        double expectedExitHeading = Math.toRadians(270.0);
        double actualExitHeading = testCorner.getExitHeading(exitLane);

        assertEquals(expectedExitHeading, actualExitHeading, 0.01);
    }
}