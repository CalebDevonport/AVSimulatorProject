package aim4.rim.map;

import aim4.map.lane.ArcSegmentLane;
import aim4.util.GeomMath;
import org.junit.Test;

import java.awt.geom.Arc2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import static java.lang.Math.toDegrees;
import static org.junit.Assert.*;

public class ArcSegmentLaneTests {
    private final static double ROUNDABOUT_RADIUS = 30.0;
    private final static double WIDTH = 20;
    private final static double MAIN_ARC_RADIUS = ROUNDABOUT_RADIUS - WIDTH / 2;
    private final static double SPEED_LIMIT = 10;
    private static final double DELTA = 0.5e-2; // necessary for assertEquals for doubles

    private static Arc2D createArc2D(double radius, double startAngle, double extentAngle) {
        Point2D origin = new Point2D.Double(radius, radius * Math.sqrt(3));
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(origin.getX(), origin.getY(), radius, toDegrees(startAngle), toDegrees(extentAngle), 0);
        return arc;
    }

    private static Arc2D createArc2DFromOrigin(Point2D origin, double radius, double startAngle, double extentAngle) {
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(origin.getX(), origin.getY(), radius, toDegrees(startAngle), toDegrees(extentAngle), 0);
        return arc;
    }

    @Test
    public void calculateLaneBorders_withSameDirection_returnsLeftBorderToTheLeftOfStartPoint() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 4, true);

        //assert
        assertTrue(lane.leftBorder().getStartPoint().getX() < lane.getArc().getStartPoint().getX());
        assertEquals(lane.leftBorder().getStartPoint().getY(), lane.getArc().getStartPoint().getY(), DELTA);
        assertTrue(lane.rightBorder().getStartPoint().getX() > lane.getArc().getStartPoint().getX());
        assertEquals(lane.rightBorder().getStartPoint().getY(), lane.getArc().getStartPoint().getY(), DELTA);
        assertTrue(lane.leftBorder().getEndPoint().getX() < lane.getArc().getEndPoint().getX());
        assertTrue(lane.leftBorder().getEndPoint().getY() < lane.getArc().getEndPoint().getY());
        assertTrue(lane.rightBorder().getEndPoint().getX() > lane.getArc().getEndPoint().getX());
        assertTrue(lane.rightBorder().getEndPoint().getY() > lane.getArc().getEndPoint().getY());
    }

    @Test
    public void calculateLaneBorders_withOppositeDirection_returnsLeftBorderToTheRightOfStartPoint() {
        //arrange
        Point2D origin = new Point2D.Double(0, 0);
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.TWO_PI - GeomMath.SIXTH_PI_30_DEGREES, -GeomMath.SIXTH_PI_30_DEGREES);
        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 4, false);

        //assert
        assertTrue(lane.leftBorder().getStartPoint().getX() < lane.getArc().getStartPoint().getX());
        assertTrue(lane.leftBorder().getStartPoint().getY() < lane.getArc().getStartPoint().getY());
        assertTrue(lane.rightBorder().getStartPoint().getX() > lane.getArc().getStartPoint().getX());
        assertTrue(lane.rightBorder().getStartPoint().getY() > lane.getArc().getStartPoint().getY());
        assertTrue(lane.leftBorder().getEndPoint().getX() < lane.getArc().getEndPoint().getX());
        assertTrue(lane.leftBorder().getEndPoint().getY() < lane.getArc().getEndPoint().getY());
        assertTrue(lane.rightBorder().getEndPoint().getX() > lane.getArc().getEndPoint().getX());
        assertTrue(lane.rightBorder().getEndPoint().getY() > lane.getArc().getEndPoint().getY());
    }

    @Test
    public void calculateArcLaneDecomposition_withPositiveSplitFactor_returnsLineSegmentLanes() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 4);

        //assert
        assertFalse(lane.getArcLaneDecomposition().isEmpty());
        assertEquals(lane.getArcLaneDecomposition().size(), 4);
        lane.getArcLaneDecomposition().forEach(
                line -> {
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLine().getP1(), arc));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLine().getP2(), arc));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLeftBorder().getP1(), lane.leftBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLeftBorder().getP2(), lane.leftBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getRightBorder().getP1(), lane.rightBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getRightBorder().getP2(), lane.rightBorder()));

                });
    }

    @Test
    public void calculateArcLaneDecomposition_withPositiveSplitFactorAndOppositeDirection_returnsLineSegmentLanes() {
        //arrange
        Point2D origin = new Point2D.Double(0, 0);
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.TWO_PI - GeomMath.SIXTH_PI_30_DEGREES, -GeomMath.SIXTH_PI_30_DEGREES);
        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 4, false);

        //assert
        assertFalse(lane.getArcLaneDecomposition().isEmpty());
        assertEquals(lane.getArcLaneDecomposition().size(), 4);
        lane.getArcLaneDecomposition().forEach(
                line -> {
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLine().getP1(), arc));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLine().getP2(), arc));
                    assertTrue(line.getLine().getP1().getY() <= arc.getEndPoint().getY());
                    assertTrue(line.getLine().getP1().getX() >= arc.getEndPoint().getX());
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLeftBorder().getP1(), lane.leftBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLeftBorder().getP2(), lane.leftBorder()));
                    assertTrue(line.getLeftBorder().getP1().getY() < line.getLine().getP1().getY());
                    assertTrue(line.getLeftBorder().getP1().getX() < line.getLine().getP1().getX());
                    assertTrue(GeomMath.isPointIntersectingArc(line.getRightBorder().getP1(), lane.rightBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getRightBorder().getP2(), lane.rightBorder()));
                    assertTrue(line.getRightBorder().getP1().getY() > line.getLine().getP1().getY());
                    assertTrue(line.getRightBorder().getP1().getX() > line.getLine().getP1().getX());

                });
    }

    @Test
    public void calculateArcLaneDecomposition_withNegativeOrZeroSplitFactor_returnsOneLineSegmentLane() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 0);

        //assert
        assertFalse(lane.getArcLaneDecomposition().isEmpty());
        assertEquals(lane.getArcLaneDecomposition().size(), 1);
        lane.getArcLaneDecomposition().forEach(
                line -> {
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLine().getP1(), arc));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLine().getP2(), arc));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLeftBorder().getP1(), lane.leftBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getLeftBorder().getP2(), lane.leftBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getRightBorder().getP1(), lane.rightBorder()));
                    assertTrue(GeomMath.isPointIntersectingArc(line.getRightBorder().getP2(), lane.rightBorder()));

                });
    }

    @Test
    public void calculateLaneShape_withValidPoints_returnsLaneShape() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);

        Arc2D leftArc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS + WIDTH / 2, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        Arc2D rightArc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS - WIDTH / 2, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);

        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 0);

        //assert
        double[] coords = new double[6];
        ArrayList<double[]> areaPoints = new ArrayList<double[]>();
        for (PathIterator pi = lane.getShape().getPathIterator(null); !pi.isDone(); pi.next()) {
            // The type will be SEG_MOVETO = 0, SEG_LINETO = 1, SEG_CUBICTO = 3 or SEG_CLOSE = 4
            // Because the Area is composed of points, lines and curves
            int type = pi.currentSegment(coords);
            // We record a double array of {segment type, x coord, y coord, x coord, y coord, x coord, y coord}
            // The last two point with coordinates only needed for curves
            double[] pathIteratorCoords = {type, coords[0], coords[1], coords[2], coords[3], coords[4], coords[5]};
            areaPoints.add(pathIteratorCoords);
        }
        // Fist we MOVE TO left of the arc
        assertEquals(areaPoints.get(0)[0], 0, DELTA);
        assertEquals(areaPoints.get(0)[1], leftArc.getStartPoint().getX(), DELTA);
        assertEquals(areaPoints.get(0)[2], leftArc.getStartPoint().getY(), DELTA);

        //Then append left arc recorded as a curve
        assertEquals(areaPoints.get(1)[0], 3, DELTA);
        assertEquals(areaPoints.get(1)[5], leftArc.getEndPoint().getX(), DELTA);
        assertEquals(areaPoints.get(1)[6], leftArc.getEndPoint().getY(), DELTA);

        //Then LINE TO right till end of second arc
        assertEquals(areaPoints.get(2)[0], 1, DELTA);
        assertEquals(areaPoints.get(2)[1], rightArc.getEndPoint().getX(), DELTA);
        assertEquals(areaPoints.get(2)[2], rightArc.getEndPoint().getY(), DELTA);

        //Then MOVE TO start of right border
        assertEquals(areaPoints.get(3)[0], 0, DELTA);
        assertEquals(areaPoints.get(3)[1], rightArc.getStartPoint().getX(), DELTA);
        assertEquals(areaPoints.get(3)[2], rightArc.getStartPoint().getY(), DELTA);

        //Then append the right arc recorded as a curve
        assertEquals(areaPoints.get(4)[0], 3, DELTA);
        assertEquals(areaPoints.get(4)[5], rightArc.getEndPoint().getX(), DELTA);
        assertEquals(areaPoints.get(4)[6], rightArc.getEndPoint().getY(), DELTA);

        //Then MOVE TO start of right border
        assertEquals(areaPoints.get(5)[0], 0, DELTA);
        assertEquals(areaPoints.get(5)[1], rightArc.getStartPoint().getX(), DELTA);
        assertEquals(areaPoints.get(5)[2], rightArc.getStartPoint().getY(), DELTA);

        //Then LINE TO start of left border (which should close the path)
        assertEquals(areaPoints.get(6)[0], 1, DELTA);
        assertEquals(areaPoints.get(6)[1], leftArc.getStartPoint().getX(), DELTA);

        //Then CLOSE Path (should close in itself)
        assertEquals(areaPoints.get(7)[0], 4, DELTA);
        assertEquals(areaPoints.get(7)[1], leftArc.getStartPoint().getX(), DELTA);
        assertEquals(areaPoints.get(7)[2], leftArc.getStartPoint().getY(), DELTA);
    }

    @Test
    public void calculateLaneDecompositionShape_withMoreThanOneLaneDecomposition_returnsLaneShape() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);

        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 3);

        //assert
        double[] coords = new double[6];
        ArrayList<double[]> areaPoints = new ArrayList<double[]>();
        for (PathIterator pi = lane.getLaneDecompositionShape().getPathIterator(null); !pi.isDone(); pi.next()) {
            // The type will be SEG_MOVETO = 0, SEG_LINETO = 1, SEG_CUBICTO = 3 or SEG_CLOSE = 4
            // Because the Area is composed of points, lines and curves
            int type = pi.currentSegment(coords);
            // We record a double array of {segment type, x coord, y coord}
            // The last two point with coordinates only needed for curves
            double[] pathIteratorCoords = {type, coords[0], coords[1]};
            areaPoints.add(pathIteratorCoords);
        }
        // Fist we MOVE TO left of the left border
        assertEquals(areaPoints.get(0)[0], 0, DELTA);
        assertEquals(areaPoints.get(0)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(0)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);

        //Then LINE TO till end of first line lane left border
        assertEquals(areaPoints.get(1)[0], 1, DELTA);
        assertEquals(areaPoints.get(1)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(1)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY2(), DELTA);

        //Then LINE TO till end of second line lane left border
        assertEquals(areaPoints.get(2)[0], 1, DELTA);
        assertEquals(areaPoints.get(2)[1], lane.getArcLaneDecomposition().get(1).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(2)[2], lane.getArcLaneDecomposition().get(1).getLeftBorder().getY2(), DELTA);

        //Then LINE TO  till end of third line lane left border
        assertEquals(areaPoints.get(3)[0], 1, DELTA);
        assertEquals(areaPoints.get(3)[1], lane.getArcLaneDecomposition().get(2).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(3)[2], lane.getArcLaneDecomposition().get(2).getLeftBorder().getY2(), DELTA);

        //Then LINE TO right till end of third line lane right border
        assertEquals(areaPoints.get(4)[0], 1, DELTA);
        assertEquals(areaPoints.get(4)[1], lane.getArcLaneDecomposition().get(2).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(4)[2], lane.getArcLaneDecomposition().get(2).getRightBorder().getY2(), DELTA);

        //Then LINE TO till end of second line lane right border
        assertEquals(areaPoints.get(5)[0], 1, DELTA);
        assertEquals(areaPoints.get(5)[1], lane.getArcLaneDecomposition().get(1).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(5)[2], lane.getArcLaneDecomposition().get(1).getRightBorder().getY2(), DELTA);

        //Then LINE TO right till end of first line lane right border
        assertEquals(areaPoints.get(6)[0], 1, DELTA);
        assertEquals(areaPoints.get(6)[1], lane.getArcLaneDecomposition().get(0).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(6)[2], lane.getArcLaneDecomposition().get(0).getRightBorder().getY2(), DELTA);

        //Then LINE TO till start of first line lane right border
        assertEquals(areaPoints.get(7)[0], 1, DELTA);
        assertEquals(areaPoints.get(7)[1], lane.getArcLaneDecomposition().get(0).getRightBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(7)[2], lane.getArcLaneDecomposition().get(0).getRightBorder().getY1(), DELTA);

        //Then LINE TO left till start of first line lane right border to close the shape
        assertEquals(areaPoints.get(8)[0], 1, DELTA);
        assertEquals(areaPoints.get(8)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(8)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);

        //Then CLOSE Path (should close in itself)
        assertEquals(areaPoints.get(9)[0], 4, DELTA);
        assertEquals(areaPoints.get(9)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(9)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);
    }

    @Test
    public void calculateLaneDecompositionShape_withOneLaneDecomposition_returnsLaneShape() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);

        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 1);

        //assert
        double[] coords = new double[6];
        ArrayList<double[]> areaPoints = new ArrayList<double[]>();
        for (PathIterator pi = lane.getLaneDecompositionShape().getPathIterator(null); !pi.isDone(); pi.next()) {
            // The type will be SEG_MOVETO = 0, SEG_LINETO = 1, SEG_CUBICTO = 3 or SEG_CLOSE = 4
            // Because the Area is composed of points, lines and curves
            int type = pi.currentSegment(coords);
            // We record a double array of {segment type, x coord, y coord}
            // The last two point with coordinates only needed for curves
            double[] pathIteratorCoords = {type, coords[0], coords[1]};
            areaPoints.add(pathIteratorCoords);
        }
        // Fist we MOVE TO left of the left border
        assertEquals(areaPoints.get(0)[0], 0, DELTA);
        assertEquals(areaPoints.get(0)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(0)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);

        //Then LINE TO till end of first line lane left border
        assertEquals(areaPoints.get(1)[0], 1, DELTA);
        assertEquals(areaPoints.get(1)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(1)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY2(), DELTA);

        //Then LINE TO right till end of third line lane right border
        assertEquals(areaPoints.get(2)[0], 1, DELTA);
        assertEquals(areaPoints.get(2)[1], lane.getArcLaneDecomposition().get(0).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(2)[2], lane.getArcLaneDecomposition().get(0).getRightBorder().getY2(), DELTA);

        //Then LINE TO right till start of first line lane right border
        assertEquals(areaPoints.get(3)[0], 1, DELTA);
        assertEquals(areaPoints.get(3)[1], lane.getArcLaneDecomposition().get(0).getRightBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(3)[2], lane.getArcLaneDecomposition().get(0).getRightBorder().getY1(), DELTA);

        //Then LINE TO left till start of first line lane right border to close the shape
        assertEquals(areaPoints.get(4)[0], 1, DELTA);
        assertEquals(areaPoints.get(4)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(4)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);

        //Then CLOSE Path (should close in itself)
        assertEquals(areaPoints.get(5)[0], 4, DELTA);
        assertEquals(areaPoints.get(5)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(5)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);
    }

    @Test
    public void calculateLaneDecompositionShape_withMoreThanOneLaneDecompositionAndOppositeDirection_returnsLaneShape() {
        //arrange
        Point2D origin = new Point2D.Double(ROUNDABOUT_RADIUS, ROUNDABOUT_RADIUS * Math.sqrt(3));
        Arc2D arc = createArc2DFromOrigin(origin, MAIN_ARC_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);

        //act
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 3, false);

        //assert
        double[] coords = new double[6];
        ArrayList<double[]> areaPoints = new ArrayList<double[]>();
        for (PathIterator pi = lane.getLaneDecompositionShape().getPathIterator(null); !pi.isDone(); pi.next()) {
            // The type will be SEG_MOVETO = 0, SEG_LINETO = 1, SEG_CUBICTO = 3 or SEG_CLOSE = 4
            // Because the Area is composed of points, lines and curves
            int type = pi.currentSegment(coords);
            // We record a double array of {segment type, x coord, y coord}
            // The last two point with coordinates only needed for curves
            double[] pathIteratorCoords = {type, coords[0], coords[1]};
            areaPoints.add(pathIteratorCoords);
        }
        // Fist we MOVE TO left of the left border
        assertEquals(areaPoints.get(0)[0], 0, DELTA);
        assertEquals(areaPoints.get(0)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(0)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);

        //Then LINE TO till end of first line lane left border
        assertEquals(areaPoints.get(1)[0], 1, DELTA);
        assertEquals(areaPoints.get(1)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(1)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY2(), DELTA);

        //Then LINE TO till end of second line lane left border
        assertEquals(areaPoints.get(2)[0], 1, DELTA);
        assertEquals(areaPoints.get(2)[1], lane.getArcLaneDecomposition().get(1).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(2)[2], lane.getArcLaneDecomposition().get(1).getLeftBorder().getY2(), DELTA);

        //Then LINE TO  till end of third line lane left border
        assertEquals(areaPoints.get(3)[0], 1, DELTA);
        assertEquals(areaPoints.get(3)[1], lane.getArcLaneDecomposition().get(2).getLeftBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(3)[2], lane.getArcLaneDecomposition().get(2).getLeftBorder().getY2(), DELTA);

        //Then LINE TO right till end of third line lane right border
        assertEquals(areaPoints.get(4)[0], 1, DELTA);
        assertEquals(areaPoints.get(4)[1], lane.getArcLaneDecomposition().get(2).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(4)[2], lane.getArcLaneDecomposition().get(2).getRightBorder().getY2(), DELTA);

        //Then LINE TO till end of second line lane right border
        assertEquals(areaPoints.get(5)[0], 1, DELTA);
        assertEquals(areaPoints.get(5)[1], lane.getArcLaneDecomposition().get(1).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(5)[2], lane.getArcLaneDecomposition().get(1).getRightBorder().getY2(), DELTA);

        //Then LINE TO right till end of first line lane right border
        assertEquals(areaPoints.get(6)[0], 1, DELTA);
        assertEquals(areaPoints.get(6)[1], lane.getArcLaneDecomposition().get(0).getRightBorder().getX2(), DELTA);
        assertEquals(areaPoints.get(6)[2], lane.getArcLaneDecomposition().get(0).getRightBorder().getY2(), DELTA);

        //Then LINE TO till start of first line lane right border
        assertEquals(areaPoints.get(7)[0], 1, DELTA);
        assertEquals(areaPoints.get(7)[1], lane.getArcLaneDecomposition().get(0).getRightBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(7)[2], lane.getArcLaneDecomposition().get(0).getRightBorder().getY1(), DELTA);

        //Then LINE TO left till start of first line lane right border to close the shape
        assertEquals(areaPoints.get(8)[0], 1, DELTA);
        assertEquals(areaPoints.get(8)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(8)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);

        //Then CLOSE Path (should close in itself)
        assertEquals(areaPoints.get(9)[0], 4, DELTA);
        assertEquals(areaPoints.get(9)[1], lane.getArcLaneDecomposition().get(0).getLeftBorder().getX1(), DELTA);
        assertEquals(areaPoints.get(9)[2], lane.getArcLaneDecomposition().get(0).getLeftBorder().getY1(), DELTA);
    }

    @Test
    public void calculateArcLength_withHalfArc_returnsArcLength() {
        //arrange
        Arc2D arc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        Arc2D halfArc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.SIXTH_PI_30_DEGREES);

        //act
        double lengthFullArc = GeomMath.calculateArcLaneLength(arc);
        double lengthHalfArc = GeomMath.calculateArcLaneLength(halfArc);

        //assert
        assertEquals(lengthHalfArc, lengthFullArc / 2, DELTA);
    }

    @Test
    public void getPointAtNormalizedDistance_withDistanceWithinArc_returnsPointOnArc() {
        //arrange
        Arc2D arc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 0);
        double arcLength = GeomMath.calculateArcLaneLength(arc);

        Arc2D halfArc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.SIXTH_PI_30_DEGREES);
        double halfArcLength = GeomMath.calculateArcLaneLength(halfArc);
        Point2D expectedPoint = halfArc.getEndPoint();

        //act
        Point2D actualPoint = lane.getPointAtNormalizedDistance(halfArcLength);

        //assert
        assertEquals(expectedPoint.getX(), actualPoint.getX(), DELTA);
        assertEquals(expectedPoint.getY(), actualPoint.getY(), DELTA);
        assertTrue(GeomMath.isPointIntersectingArc(actualPoint, arc));
    }

    @Test
    public void getPointAtNormalizedDistance_withDistanceOutsideArc_returnsPointOnArc() {
        //arrange
        Arc2D arc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 0);
        double arcLength = GeomMath.calculateArcLaneLength(arc);

        //act
        Point2D pointOnArc = lane.getPointAtNormalizedDistance(arcLength + 1);

        //assert
        assertFalse(GeomMath.isPointIntersectingArc(pointOnArc, arc));
    }

    @Test
    public void normalizedDistanceAlongLane_withPointOnArc_returnsNormalizedDistance() {
        //arrange
        Arc2D arc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 0);
        double arcLength = GeomMath.calculateArcLaneLength(arc);

        Arc2D halfArc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.SIXTH_PI_30_DEGREES);
        double expectedLength1 = GeomMath.calculateArcLaneLength(halfArc);

        //act
        double actualLength1 = lane.normalizedDistanceAlongLane(halfArc.getEndPoint());
        double actualLength2 = lane.normalizedDistanceAlongLane(halfArc.getStartPoint());

        //assert
        assertEquals(expectedLength1, actualLength1, DELTA);
        assertEquals(0, actualLength2, DELTA);
    }

    @Test
    public void nearestDistance_withPointOnArc_returns0() {
        //arrange
        Arc2D arc = createArc2D(ROUNDABOUT_RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT, 0);

        //act
        double actualDistance = lane.nearestDistance(arc.getEndPoint());

        //assert
        assertEquals(actualDistance, 0, DELTA);
    }
}
