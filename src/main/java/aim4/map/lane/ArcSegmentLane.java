package aim4.map.lane;

import aim4.util.GeomMath;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

/**
 * A lane class that can be represented by a directed arc segment.
 */
public class ArcSegmentLane extends AbstractLane {
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The length of the arc Lane.
     */
    private double length;

    /**
     * The length of the lanes that sum up the Arc Lane.
     */
    private double lengthArcLaneDecomposition;

    /**
     * The width of the Lane, in meters.
     */
    private double width;

    /**
     * Half the width of the Lane, in meters.
     */
    private double halfWidth;

    /**
     * The arc segment that represents the Arc Lane.
     */
    private Arc2D arc;

    /**
     * The list of Line Lanes that represent the Arc Lane ordered from
     * start of the Arc Lane to end of Arc Lane.
     */
    private ArrayList<LineSegmentLane> arcLaneDecomposition;

    /**
     * A Shape describing the Arc Lane, including its width.
     */
    private Shape laneShape;

    /**
     * A Shape describing the Arc Lane Decomposition, including its width.
     */
    private Shape laneDecompositionShape;

    /**
     * The arc that represents the left border of this Lane.
     */
    private Arc2D leftBorder;

    /**
     * The arc that represents the right border of this Lane.
     */
    private Arc2D rightBorder;

    /**
     * By how much the arc extent angle of the Arc Lane is divided by.
     * Gives the number of sections and Line Segment Lanes.
     */
    private int splitFactor;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Constructs an arc-segment lane using an Arc. Assumes Line lanes same direction as the arc.
     *
     * @param arc         the arc segment representing the center of the Lane
     * @param width       the width of the Lane, in meters
     * @param speedLimit  the speed limit of the Lane, in meters per second
     * @param splitFactor the number by which the extent angle of the arc Lane is divided.
     */
    public ArcSegmentLane(Arc2D arc, double width, double speedLimit, int splitFactor) {
        super(speedLimit);

        this.arc = arc;
        this.width = width;
        this.halfWidth = width / 2;
        this.splitFactor = splitFactor;
        length = GeomMath.calculateArcLaneLength(arc);
        calculateLaneBorders(arc);
        arcLaneDecomposition = calculateArcLaneDecomposition(arc, splitFactor);
        lengthArcLaneDecomposition = calculateLengthArcLaneDecomposition(arcLaneDecomposition);
        laneShape = calculateLaneShape(leftBorder, rightBorder);
        setContinuousLanes();
        laneDecompositionShape = calculateLaneDecompositionShape(arcLaneDecomposition);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // lanes as lines

    /**
     * Get the line that represents this Lane.
     */
    public Arc2D getArc() {
        return arc;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getLength() { return length; }

    /**
     * Get the length of all lanes that represent the Arc Lane.
     */
    public double getLengthArcLaneDecomposition() { return lengthArcLaneDecomposition; }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getStartPoint() {
        return arc.getStartPoint();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getEndPoint() { return arc.getEndPoint(); }

    /**
     * Get the list of lanes that represent the arc.
     */
    public ArrayList<LineSegmentLane> getArcLaneDecomposition() { return arcLaneDecomposition; }

    /**
     * Given a normalized distance, get the coordinates of a point on the arc
     * by moving along the arc.
     */
    @Override
    public Point2D getPointAtNormalizedDistance(double normalizedDistance) {
        double radius = arc.getWidth() / 2.0;
        double originX = arc.getX() + radius;
        double originY = arc.getY() + radius;
        double startAngle = arc.getAngleStart();
        //Angle between x axes and point according to distance
        double theta = startAngle - (normalizedDistance * Math.toDegrees(GeomMath.PI)) / (GeomMath.PI * radius);

        double xCoordinate = originX + radius * Math.cos(Math.toRadians(theta));
        double yCoordinate = originY - radius * Math.sin(Math.toRadians(theta));
        return new Point2D.Double(xCoordinate, yCoordinate);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D nearestPoint(Point2D p) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double nearestDistance(Point2D pos) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D getLeadPoint(Point2D pos, double leadDist) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return null;
    }

    // distance along lane
    /**
     * {@inheritDoc}
     */
    @Override
    public double distanceAlongLane(Point2D pos) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double remainingDistanceAlongLane(Point2D pos) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double normalizedDistanceAlongLane(Point2D pos) {
        double radius = arc.getWidth() / 2.0;
        double originX = arc.getX() + radius;
        //Calculate angle between start point, origin and point on arc
        double theta = Math.toDegrees(Math.acos((pos.getX() - originX) / radius));
        double startAngle = arc.getAngleStart();
        return ((startAngle - theta) * GeomMath.PI * radius) / Math.toDegrees(GeomMath.PI);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double normalizedDistance(double distance) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0;
    }

    // heading

    /**
     * {@inheritDoc}
     */
    @Override
    public double getInitialHeading() {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getTerminalHeading() {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getHeadingAtNormalizedDistance(double normalizedDistance) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return 0.0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D intersectionPoint(Line2D l) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return null;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // lanes as shapes

    /**
     * {@inheritDoc}
     */
    @Override
    public Shape getShape() {
        return laneShape;
    }

    @Override
    public Shape getShape(double startFraction, double endFraction) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return null;
    }

    /**
     * Get the shape of the arc lane based on the line lanes that made the arc.
     */
    public Shape getLaneDecompositionShape() {
        return laneDecompositionShape;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getWidth() {
        return width;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public boolean contains(Point2D pos) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Arc2D leftBorder() {
        return leftBorder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Arc2D rightBorder() {
        return rightBorder;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D leftIntersectionPoint(Line2D l) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Point2D rightIntersectionPoint(Line2D l) {
        // not needed as Arc Lane is decomposed in Line Lanes
        return null;
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /**
     * Calculate list of lines that make up the arc lane based on
     * a split factor.
     */
    private ArrayList<LineSegmentLane> calculateArcLaneDecomposition(Arc2D arc, int splitFactor) {
        ArrayList<LineSegmentLane> lineSegmentLanes = new ArrayList<>();
        double splitAngle;
        // If the split factor is negative or zero, we consider the arc is not divided at all.
        if (splitFactor <= 0) {
            splitFactor = 1;
        }
        splitAngle = arc.getAngleExtent() / splitFactor;
        // For each split (inner arc)
        for (int split = 0; split < splitFactor; split++) {
            // Calculate inner arc based on split (order from left to right) and splitAngle
            Arc2D arcSegment = calculateArcSegment(arc, split, splitAngle);
            Point2D startPoint = arcSegment.getStartPoint();
            Point2D endPoint = arcSegment.getEndPoint();

            // Create a LineSegmentLane with the start point = start point of arc and end point = end point of arc)
            LineSegmentLane lineSegmentLane = new LineSegmentLane(startPoint, endPoint, width, getSpeedLimit());

            // Re-calculate the left border of the LineSegment to intersect the leftBorder arc.
            Arc2D arcSegmentLeftBorder = calculateArcSegment(leftBorder, split, splitAngle);
            Point2D leftBorderStartPoint = arcSegmentLeftBorder.getStartPoint();
            Point2D leftBorderEndPoint = arcSegmentLeftBorder.getEndPoint();
            lineSegmentLane.setLeftBorder(new Line2D.Double(leftBorderStartPoint, leftBorderEndPoint));

            // Re-calculate the right border of the LineSegment to intersect the rightBorder arc.
            Arc2D arcSegmentRightBorder = calculateArcSegment(rightBorder, split, splitAngle);
            Point2D rightBorderStartPoint = arcSegmentRightBorder.getStartPoint();
            Point2D rightBorderEndPoint = arcSegmentRightBorder.getEndPoint();
            lineSegmentLane.setRightBorder(new Line2D.Double(rightBorderStartPoint, rightBorderEndPoint));

            lineSegmentLanes.add(lineSegmentLane);
        }
        return lineSegmentLanes;
    }

    /**
     * Calculate an arc based on a base arc, split and splitAngle.
     */
    private Arc2D calculateArcSegment(Arc2D baseArc, int split, double splitAngle) {
        return new Arc2D.Double(
                baseArc.getX(),
                baseArc.getY(),
                baseArc.getWidth(),
                baseArc.getHeight(),
                baseArc.getAngleStart() - split * Math.abs(splitAngle),
                splitAngle,
                baseArc.getArcType());
    }

    /**
     * Calculate length of all lines that define the arc.
     */
    private double calculateLengthArcLaneDecomposition(ArrayList<LineSegmentLane> lineSegmentLanes) {
        double linesLength = 0.0;
        if (lineSegmentLanes.isEmpty()) {
            return linesLength;
        }
        for (LineSegmentLane line : lineSegmentLanes) {
            linesLength += line.getLength();
        }
        return linesLength;
    }

    /**
     * Calculate lane left and right borders based on lane's width and orientation.
     */
    private void calculateLaneBorders(Arc2D arc) {
        Point2D origin = new Point2D.Double(arc.getX() + arc.getWidth() / 2, arc.getY() + arc.getWidth() / 2);
        this.leftBorder = new Arc2D.Double();
        this.rightBorder = new Arc2D.Double();

        // Calculate orientation of the lane
        double angle = arc.getAngleExtent();

        // Negative angle means left border has bigger radius than right border
        if (angle < 0) {
            leftBorder.setArcByCenter(origin.getX(), origin.getY(),
                    arc.getWidth() / 2 + halfWidth, arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
            rightBorder.setArcByCenter(origin.getX(), origin.getY(),
                    arc.getWidth() / 2 - halfWidth, arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
        } else {
            leftBorder.setArcByCenter(origin.getX(), origin.getY(),
                    arc.getWidth() / 2 - halfWidth, arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
            rightBorder.setArcByCenter(origin.getX(), origin.getY(),
                    arc.getWidth() / 2 + halfWidth, arc.getAngleStart(), arc.getAngleExtent(), arc.getArcType());
        }
    }

    /**
     * Calculate lane shape, including the width of the lane.
     *
     * @return a Shape describing the lane, including its width.
     */
    private Shape calculateLaneShape(Arc2D leftBorder, Arc2D rightBorder) {
        GeneralPath result = new GeneralPath();

        Point2D origin = new Point2D.Double(leftBorder.getX() + leftBorder.getWidth() / 2, leftBorder.getY() + leftBorder.getHeight() / 2);
        // Calculate orientation of the lane
        double angle = leftBorder.getAngleExtent();

        // If angle is negative then start drawing the shape by appending the right border and then the left border.
        if (angle < 0){
            // Move to left border start
            result.moveTo((float) (leftBorder.getStartPoint().getX()),
                    (float) (leftBorder.getStartPoint().getY()));
            // Add a line to right border start
            result.lineTo(rightBorder.getStartPoint().getX(), rightBorder.getStartPoint().getY());
            // Append the right border arc
            result.append(rightBorder,false);
            // Add a line to the end of left border
            result.lineTo(leftBorder.getEndPoint().getX(), leftBorder.getEndPoint().getY());
            // Now need to append the left border. This is done by appending the start point and then the end point of
            // the arc. Need to reverse the arc start point and end point. The append closes the shape.
            Arc2D leftBorderReversedStartAndEnd = new Arc2D.Double();
            leftBorderReversedStartAndEnd.setArcByCenter(origin.getX(), origin.getY(),
                    leftBorder.getWidth() / 2, leftBorder.getAngleStart() - Math.abs(leftBorder.getAngleExtent()), -leftBorder.getAngleExtent(), 0);
            result.append(leftBorderReversedStartAndEnd,true);
        }
        // If angle is positive then start drawing the shape by appending the left border and then the right border.
        else {
            // Move to right border start
            result.moveTo((float) (rightBorder.getStartPoint().getX()),
                    (float) (rightBorder.getStartPoint().getY()));
            // Add a line to left border start
            result.lineTo(leftBorder.getStartPoint().getX(), leftBorder.getStartPoint().getY());
            // Append the left border arc
            result.append(leftBorder,false);
            // Add a line to the end of right border
            result.lineTo(rightBorder.getEndPoint().getX(), rightBorder.getEndPoint().getY());
            // Now need to append the right border. This is done by appending the start point and then the end point of
            // the arc. Need to reverse the arc start point and end point. The append closes the shape.
            Arc2D rightBorderReversedStartAndEnd = new Arc2D.Double();
            rightBorderReversedStartAndEnd.setArcByCenter(origin.getX(), origin.getY(),
                    rightBorder.getWidth() / 2, rightBorder.getAngleStart() + Math.abs(leftBorder.getAngleExtent()), -leftBorder.getAngleExtent(), 0);
            result.append(rightBorderReversedStartAndEnd,true);
        }

        return result;
    }

    /**
     * Calculate lane shape, including the width of the lane based on the line lanes that made the arc.
     *
     * @return a Shape describing the lane, including its width.
     */
    private Shape calculateLaneDecompositionShape(ArrayList<LineSegmentLane> arcLaneDecomposition) {
        GeneralPath result = new GeneralPath();

        //Start on left border (down left)
        result.moveTo((float) (arcLaneDecomposition.get(0).getLeftBorder().getX1()),
                (float) (arcLaneDecomposition.get(0).getLeftBorder().getY1()));
        //Shape the left border till up left
        for (LineSegmentLane lineLane : arcLaneDecomposition) {
            result.lineTo(lineLane.getLeftBorder().getX2(), lineLane.getLeftBorder().getY2());
        }
        //Shape the right border till down right
        for (int index = arcLaneDecomposition.size() - 1; index >= 0; index--) {
            result.lineTo(arcLaneDecomposition.get(index).getRightBorder().getX2(), arcLaneDecomposition.get(index).getRightBorder().getY2());
        }
        // Shape case of last line right border
        result.lineTo(arcLaneDecomposition.get(0).getRightBorder().getX1(), arcLaneDecomposition.get(0).getRightBorder().getY1());

        // Close the shape
        result.lineTo(arcLaneDecomposition.get(0).getLeftBorder().getX1(), arcLaneDecomposition.get(0).getLeftBorder().getY1());

        //Close path (ensures path is closed)
        result.closePath();
        return result;
    }

    /**
     * Set previous and next lanes.
     */
    public void setContinuousLanes() {
        for (int index = 0; index < arcLaneDecomposition.size(); index++) {

            // If first line lane then the previous lane is the previous lane of the arc lane.
            if (index == 0) {
                arcLaneDecomposition.get(index).setPrevLane(this.getPrevLane());
                // If there are at least two line lanes composing the arc lane, the next lane will be another decomposition line lane
                if (arcLaneDecomposition.size() > 1) {
                    arcLaneDecomposition.get(index).setNextLane(arcLaneDecomposition.get(index + 1));
                    // Otherwise, the next lane will be the next lane of the arc lane
                } else {
                    arcLaneDecomposition.get(index).setNextLane(this.getNextLane());
                }
            // If not an edge line lane, then set previous lane and next lane according to the decomposition lanes
            } else if (index < arcLaneDecomposition.size() - 1) {
                arcLaneDecomposition.get(index).setPrevLane(arcLaneDecomposition.get(index - 1));
                arcLaneDecomposition.get(index).setNextLane(arcLaneDecomposition.get(index + 1));
            // Else reached the last line lane from decomposition. The next lane will be the next lane of the arc lane.
            } else {
                arcLaneDecomposition.get(index).setPrevLane(arcLaneDecomposition.get(index - 1));
                arcLaneDecomposition.get(index).setNextLane(this.getNextLane());
            }
        }
    }
}
