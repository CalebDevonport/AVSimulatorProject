package aim4.rim.map;

import aim4.map.lane.LineSegmentLane;
import org.junit.Test;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LineSegmentLaneTests {
    private final static double WIDTH = 20;
    private final static double SPEED_LIMIT = 10;
    private static final double DELTA = 0.5e-2; // necessary for assertEquals for doubles

    @Test
    public void calculateInitialHeading_withEast_returnsZeroRadians() {
        //arrange

        //act
        LineSegmentLane east1 = new LineSegmentLane(new Line2D.Double(
                new Point2D.Double(0, 125),
                new Point2D.Double(250, 125)),
                WIDTH,
                SPEED_LIMIT);
        LineSegmentLane east2 = new LineSegmentLane(new Line2D.Double(
                new Point2D.Double(125, 125),
                new Point2D.Double(250, 125)),
                WIDTH,
                SPEED_LIMIT);

        //assert
        assert  east1.getInitialHeading() == 0;
        assert  east2.getInitialHeading() == 0;

    }

    @Test
    public void calculateInitialHeading_withNorth_returns270Degrees() {
        //arrange

        //act
        LineSegmentLane north1 = new LineSegmentLane(new Line2D.Double(
                new Point2D.Double(125, 250),
                new Point2D.Double(125, 0)),
                WIDTH,
                SPEED_LIMIT);
        LineSegmentLane north2 = new LineSegmentLane(new Line2D.Double(
                new Point2D.Double(125, 125),
                new Point2D.Double(125, 0)),
                WIDTH,
                SPEED_LIMIT);

        //assert
        assertEquals(Math.toDegrees(north1.getInitialHeading()), 270.0, DELTA);
        assertEquals(Math.toDegrees(north2.getInitialHeading()), 270.0, DELTA);

    }

    @Test
    public void calculateInitialHeading_withEntryPoint1_returnsMoreThan270() {
        //arrange

        //act
        LineSegmentLane entryPointLine = new LineSegmentLane(new Line2D.Double(
                new Point2D.Double(128.50, 141.95),
                new Point2D.Double(129.79, 139.51)),
                WIDTH,
                SPEED_LIMIT);

        //assert
        assertTrue(Math.toDegrees(entryPointLine.getInitialHeading()) > 270.0);
    }
}
