package aim4.rim.map;

import aim4.util.GeomMath;
import org.junit.Test;

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;

import static aim4.util.GeomMath.isPointIntersectingArc;
import static org.junit.Assert.assertEquals;

public class DataCollectionArcTests {
    private final static double RADIUS = 30;
    private static final double DELTA = 1e-15; // necessary for assertEquals for doubles

    @Test
    public void setArcByCenter_withValidParameters_setsArc(){
        //arrange
        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
        Arc2D arc = new Arc2D.Double();

        //act
        arc.setArcByCenter(origin.getX(), origin.getY(), RADIUS, GeomMath.TWO_PI, -GeomMath.THIRD_PI_60_DEGREES, 0);

        //assert
        assertEquals(arc.getX(), origin.getX()-RADIUS,DELTA);
        assertEquals(arc.getY(), origin.getY()-RADIUS,DELTA);
        assertEquals(arc.getWidth(), RADIUS*2,DELTA);
        assertEquals(arc.getHeight(), RADIUS*2,DELTA);
        assertEquals(arc.getAngleStart(), GeomMath.TWO_PI,DELTA);
        assertEquals(arc.getAngleExtent(), -GeomMath.THIRD_PI_60_DEGREES,DELTA);
    }

    @Test
    public void isPointIntersectingArc_withPointIntersectingArc_returnsTrue(){
        //arrange
        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
        Arc2D arc1 = new Arc2D.Double();
        arc1.setArcByCenter(origin.getX(), origin.getY(), RADIUS, GeomMath.TWO_PI, -GeomMath.THIRD_PI_60_DEGREES, 0);
        Arc2D arc2 = new Arc2D.Double();
        arc2.setArcByCenter(origin.getX(), origin.getY(), RADIUS, GeomMath.TWO_PI, -GeomMath.SIXTH_PI_30_DEGREES, 0);
        Point2D pointToCheck = new Point2D.Double(arc2.getEndPoint().getX(),arc2.getY());

        //act

        //assert
        isPointIntersectingArc(pointToCheck, arc1);
    }

    @Test
    public void isPointIntersectingArc_withPointNotIntersectingArc_returnsFalse(){
        //arrange
        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
        Arc2D arc1 = new Arc2D.Double();
        arc1.setArcByCenter(origin.getX(), origin.getY(), RADIUS, GeomMath.TWO_PI, -GeomMath.THIRD_PI_60_DEGREES, 0);
        Point2D pointToCheck = new Point2D.Double(-3,-2);

        //act

        //assert
        assert !isPointIntersectingArc(pointToCheck, arc1);
    }

    @Test
    public void isPointIntersectingArc_withPointNotIntersectingArcButOnCircleAndLeftToArc_returnsFalse(){
        //arrange
        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
        Arc2D arc1 = new Arc2D.Double();
        arc1.setArcByCenter(origin.getX(), origin.getY(), RADIUS, GeomMath.TWO_PI, -GeomMath.THIRD_PI_60_DEGREES, 0);
        Arc2D arc2 = new Arc2D.Double();
        arc2.setArcByCenter(origin.getX(), origin.getY(), RADIUS,
                GeomMath.TWO_PI+ GeomMath.HALF_PI_90_DEGREES,
                -GeomMath.THIRD_PI_60_DEGREES-GeomMath.HALF_PI_90_DEGREES, 0);
        Point2D pointToCheck = new Point2D.Double(arc2.getStartPoint().getX(),arc2.getStartPoint().getY());

        //act

        //assert
        assert !isPointIntersectingArc(pointToCheck, arc1);
    }

    @Test
    public void isPointIntersectingArc_withPointNotIntersectingArcButOnCircleAndRightToArc_returnsFalse(){
        //arrange
        Point2D origin = new Point2D.Double(RADIUS,RADIUS*Math.sqrt(3));
        Arc2D arc1 = new Arc2D.Double();
        arc1.setArcByCenter(origin.getX(), origin.getY(), RADIUS, GeomMath.TWO_PI, -GeomMath.THIRD_PI_60_DEGREES, 0);
        Arc2D arc2 = new Arc2D.Double();
        arc2.setArcByCenter(origin.getX(), origin.getY(), RADIUS,
                GeomMath.TWO_PI,
                -GeomMath.THIRD_PI_60_DEGREES-GeomMath.SIXTH_PI_30_DEGREES, 0);
        Point2D pointToCheck = new Point2D.Double(arc2.getEndPoint().getX(),arc2.getEndPoint().getY());

        //act

        //assert
        assert !isPointIntersectingArc(pointToCheck, arc1);
    }
}
