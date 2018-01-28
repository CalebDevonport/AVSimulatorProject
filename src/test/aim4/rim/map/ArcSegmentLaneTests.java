package aim4.rim.map;

import aim4.map.lane.ArcSegmentLane;
import aim4.util.GeomMath;
import org.junit.Test;
import sun.nio.cs.ext.MS874;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

import static java.lang.Math.toDegrees;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ArcSegmentLaneTests {
    private final static double RADIUS = 30.0;
    private final static double WIDTH = 10.0;
    private final static double SPEED_LIMIT = 10;
    private static final double DELTA = 1e-15; // necessary for assertEquals for doubles

    private static Arc2D createArc2D (double RADIUS, double startAngle, double extentAngle){
        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
        Arc2D arc = new Arc2D.Double();
        arc.setArcByCenter(origin.getX(), origin.getY(), RADIUS, toDegrees(startAngle), toDegrees(extentAngle), 0);
        return arc;
    }
//    @Test
//    public void calculateLaneShape_withValidPoints_returnsLaneShape(){
//        //arrange
//        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
//        Arc2D arc = new Arc2D.Double();
//        arc.setArcByCenter(origin.getX(), origin.getY(), RADIUS, toDegrees(GeomMath.PI), toDegrees(-GeomMath.THIRD_PI_60_DEGREES), 0);
//
//        Arc2D halfMiddleArc = new Arc2D.Double();
//        halfMiddleArc.setArcByCenter(origin.getX(), origin.getY(), RADIUS, toDegrees(GeomMath.PI-GeomMath.SIXTH_PI_30_DEGREES), toDegrees(-GeomMath.SIXTH_PI_30_DEGREES), 0);
//
//        Arc2D halfLeftArc = new Arc2D.Double();
//        halfLeftArc.setArcByCenter(origin.getX(), origin.getY(), RADIUS+WIDTH/2, toDegrees(GeomMath.PI-GeomMath.SIXTH_PI_30_DEGREES), (-GeomMath.SIXTH_PI_30_DEGREES), 0);
//
//        Arc2D halfRightArc = new Arc2D.Double();
//        halfRightArc.setArcByCenter(origin.getX(), origin.getY(), RADIUS-WIDTH/2, toDegrees(GeomMath.PI-GeomMath.SIXTH_PI_30_DEGREES), toDegrees(-GeomMath.SIXTH_PI_30_DEGREES), 0);
//
//        Arc2D notInsideArc = new Arc2D.Double();
//        notInsideArc.setArcByCenter(origin.getX(), origin.getY(), RADIUS-WIDTH/2-1, toDegrees(GeomMath.PI-GeomMath.SIXTH_PI_30_DEGREES), toDegrees(-GeomMath.SIXTH_PI_30_DEGREES), 0);
//
//
//        //act
//        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT);
//
//        //assert
//        assertTrue(lane.getShape().contains(arc.getStartPoint()));
//        assertTrue(lane.getShape().contains(lane.leftBorder().getStartPoint()));
//        assertTrue(lane.getShape().contains(lane.leftBorder().getEndPoint()));
//        assertTrue(lane.getShape().contains(lane.rightBorder().getStartPoint()));
//        assertTrue(lane.getShape().contains(lane.rightBorder().getEndPoint()));
//        assertTrue(lane.getShape().contains(halfMiddleArc.getStartPoint()));
//        assertTrue(lane.getShape().contains(halfMiddleArc.getEndPoint()));
//        assertTrue(lane.getShape().contains(halfLeftArc.getStartPoint()));
//        assertTrue(lane.getShape().contains(halfLeftArc.getEndPoint()));
//        assertTrue(lane.getShape().contains(halfRightArc.getStartPoint()));
//        assertTrue(lane.getShape().contains(halfRightArc.getEndPoint()));
//        assertFalse(lane.getShape().contains(notInsideArc.getStartPoint()));
//        assertFalse(lane.getShape().contains(notInsideArc.getEndPoint()));
//    }
    @Test
    public void calculateArcLength_withHalfArc_returnsArcLength(){
        //arrange
        Arc2D arc = createArc2D(RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        Arc2D halfArc = createArc2D(RADIUS, GeomMath.PI, -GeomMath.SIXTH_PI_30_DEGREES);

        //act
        double lengthFullArc = GeomMath.calculateArcLength(arc);
        double lengthHalfArc = GeomMath.calculateArcLength(halfArc);

        //assert
        assertEquals(lengthHalfArc, lengthFullArc/2, DELTA);
    }

    @Test
    public void getPointAtNormalizedDistance_withDistanceWithinTheArc_returnsPointOnArc(){
        //arrange
        Arc2D arc = createArc2D(RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT);
        double arcLength = GeomMath.calculateArcLength(arc);

        Arc2D halfArc = createArc2D(RADIUS, GeomMath.PI, -GeomMath.SIXTH_PI_30_DEGREES);
        double halfArcLength = GeomMath.calculateArcLength(halfArc);
        Point2D expectedPoint = halfArc.getEndPoint();

        //act
        Point2D actualPoint = lane.getPointAtNormalizedDistance(halfArcLength);

        //assert
        assertEquals(expectedPoint.getX(),actualPoint.getX(), DELTA);
        assertEquals(expectedPoint.getY(),actualPoint.getY(), DELTA);
    }

    @Test
    public void getPointAtNormalizedDistance_withDistanceOutsideTheArc_returnsPointOnArc(){
        //arrange
        Arc2D arc = createArc2D(RADIUS, GeomMath.PI, -GeomMath.THIRD_PI_60_DEGREES);
        ArcSegmentLane lane = new ArcSegmentLane(arc, WIDTH, SPEED_LIMIT);
        double arcLength = GeomMath.calculateArcLength(arc);

        //act
        Point2D pointOnArc = lane.getPointAtNormalizedDistance(arcLength+1);

        //assert
        assertFalse(arc.contains(pointOnArc.getX(), pointOnArc.getY()));
    }
}
