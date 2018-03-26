package aim4.util;

import aim4.config.Debug;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

/**
 * A tiled area - a subdivision of an area into a grid of small shapes.
 */
public class TiledRimArea {
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * A tile.
     */
    public static class Tile {
        /** The area controlled by this tile. */
        private final Area area;
        /** The start angle of the tile. */
        private final double startAngle;
        /** The angle of the tile. */
        private final double angle;
        /** the id of this tile */
        private final int id;

        /**
         * Create a tile.
         *
         * @param area  the area of the tile
         * @param angle  the angle of the tile
         * @param id    the ID of the tile
         */
        public Tile(Area area, double startAngle, double angle, int id) {
            this.area = area;
            this.startAngle = startAngle;
            this.angle = angle;
            this.id = id;
        }

        /** Get the area controlled by this ReservationTile. */
        public Area getArea() {
            return area;
        }

        /** Get the start angle of the tile. */
        public double getStartAngle (){ return startAngle; }

        /** Get the angle of the tile. */
        public double getAngle() {
            return angle;
        }

        /** Get the id of this tile */
        public int getId() {
            return id;
        }
    }

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The minimal circle of the intersection. */
    private final Ellipse2D minimalCircle;
    /** The maximal circle of the intersection. */
    private final Ellipse2D maximalCircle;
    /** The factor by which the circle is divided. */
    private final double granularity;
    /** A mapping from id to tiles */
    private final ArrayList<Tile> idToTiles;
    /** The number of tiles */
    private int numberOfTiles;

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a tiled area
     *
     * @param minimalCircle     the minimal circle of the intersection
     * @param maximalCircle     the maximal circle of the intersection
     * @param granularity       the factor by which the tiles are divided
     */
    public TiledRimArea(Ellipse2D minimalCircle, Ellipse2D maximalCircle, double granularity) {
        this.minimalCircle = minimalCircle;
        this.maximalCircle = maximalCircle;
        this.granularity = granularity;
        numberOfTiles = (int) granularity * 2;
        idToTiles = new ArrayList<Tile>((int) granularity * 2);
        createTiles();

    }

    /**
     * Create the tiles
     */
    private void createTiles() {
        // Angle in degrees by which the circle will be divided
        double angle = Math.toDegrees(GeomMath.PI) / granularity;

        // The origin of the minimal and maximal circle
        Point2D origin = new Point2D.Double(minimalCircle.getCenterX(), minimalCircle.getCenterY());
        // The radius of the minimal circle
        double minimalRadius = minimalCircle.getWidth() / 2;
        // The radius of the maximal circle
        double maximalRadius = maximalCircle.getWidth() / 2;

        // Construct the tiles starting from zero angle
        for (int id = 0; id < numberOfTiles; id++){
            Arc2D minimalArc = new Arc2D.Double();
            Arc2D maximalArc = new Arc2D.Double();

            // Find starting angle from zero
            double startAngle = id * angle;

            // Find minimal and maximal arc
            minimalArc.setArcByCenter(origin.getX(), origin.getY(), minimalRadius, startAngle, angle, 0);
            maximalArc.setArcByCenter(origin.getX(), origin.getY(), maximalRadius, startAngle + angle, -angle, 0);

            // Create tile shape
            GeneralPath tileShape = new GeneralPath();
            tileShape.moveTo(maximalArc.getEndPoint().getX(), maximalArc.getEndPoint().getY());
            tileShape.lineTo(minimalArc.getStartPoint().getX(), minimalArc.getStartPoint().getY());
            tileShape.append(minimalArc, false);
            tileShape.lineTo(maximalArc.getStartPoint().getX(), maximalArc.getStartPoint().getY());
            tileShape.append(maximalArc, true);

            idToTiles.add(new Tile(new Area(tileShape), startAngle, angle, id));

        }

        //Construct approach & merging entry tiles
        for (Road road : Debug.currentRimMap.getRoads()){
            createEntryTiles( 25.0, road);
            createExitTiles( 25.0, road);

        }

    }

    private void createEntryTiles(double angle, Road road) {
        // The approach entry lane to be divided
        ArcSegmentLane approachEntryLane = (ArcSegmentLane) road.getEntryApproachLane();

        // The origin of the minimal and maximal circle
        Point2D origin = new Point2D.Double(approachEntryLane.getArc().getCenterX(), approachEntryLane.getArc().getCenterY());
        // The radius of the minimal circle
        double minimalRadius = approachEntryLane.rightBorder().getWidth() / 2;
        // The radius of the maximal circle
        double maximalRadius = approachEntryLane.leftBorder().getWidth() / 2;

        int offset = numberOfTiles;

        //Create the entry tiles
        for (int id = offset; id < offset + 2; id ++){
            // Create first tile
            Arc2D minimalArc = new Arc2D.Double();
            Arc2D maximalArc = new Arc2D.Double();

            double startAngle = approachEntryLane.getArc().getAngleStart() - (id- offset) * angle;

            // Find minimal and maximal arc
            minimalArc.setArcByCenter(origin.getX(), origin.getY(), minimalRadius, startAngle, -angle, 0);
            maximalArc.setArcByCenter(origin.getX(), origin.getY(), maximalRadius, startAngle - angle, +angle, 0);

            // Create tile shape
            GeneralPath tileShape = new GeneralPath();
            tileShape.moveTo(maximalArc.getEndPoint().getX(), maximalArc.getEndPoint().getY());
            tileShape.lineTo(minimalArc.getStartPoint().getX(), minimalArc.getStartPoint().getY());
            tileShape.append(minimalArc, false);
            tileShape.lineTo(maximalArc.getStartPoint().getX(), maximalArc.getStartPoint().getY());
            tileShape.append(maximalArc, true);

            idToTiles.add(new Tile(new Area(tileShape), startAngle, angle, id));
            numberOfTiles++;
        }
    }

    private void createExitTiles(double angle, Road road) {
        // The approach exit lane to be divided
        ArcSegmentLane approachExitLane = (ArcSegmentLane) road.getExitApproachLane();

        // The origin of the minimal and maximal circle
        Point2D origin = new Point2D.Double(approachExitLane.getArc().getCenterX(), approachExitLane.getArc().getCenterY());
        // The radius of the minimal circle
        double minimalRadius = approachExitLane.rightBorder().getWidth() / 2;
        // The radius of the maximal circle
        double maximalRadius = approachExitLane.leftBorder().getWidth() / 2;

        int offset = numberOfTiles;

        //Create the exit tiles
        for (int id = offset; id < offset + 2; id ++){
            // Create first tile
            Arc2D minimalArc = new Arc2D.Double();
            Arc2D maximalArc = new Arc2D.Double();

            double startAngle = approachExitLane.getArc().getAngleStart() + approachExitLane.getArc().getAngleExtent()
                    + (id- offset) * angle;

            // Find minimal and maximal arc
            minimalArc.setArcByCenter(origin.getX(), origin.getY(), minimalRadius, startAngle, angle, 0);
            maximalArc.setArcByCenter(origin.getX(), origin.getY(), maximalRadius, startAngle + angle, -angle, 0);

            // Create tile shape
            GeneralPath tileShape = new GeneralPath();
            tileShape.moveTo(maximalArc.getEndPoint().getX(), maximalArc.getEndPoint().getY());
            tileShape.lineTo(minimalArc.getStartPoint().getX(), minimalArc.getStartPoint().getY());
            tileShape.append(minimalArc, false);
            tileShape.lineTo(maximalArc.getStartPoint().getX(), maximalArc.getStartPoint().getY());
            tileShape.append(maximalArc, true);

            idToTiles.add(new Tile(new Area(tileShape), startAngle, angle, id));
            numberOfTiles++;
        }
    }

    //////////////////////////////////////////
    // PUBLIC METHODS (getters and setters)
    //////////////////////////////////////////

    /**
     * Get the minimal circle.
     *
     * @return the minimal circle
     */
    public Ellipse2D getMinimalCircle() {
        return minimalCircle;
    }

    /**
     * Get the maximal circle.
     *
     * @return the maximal circle
     */
    public Ellipse2D getMaximalCircle() {
        return maximalCircle;
    }

    /**
     * Get a tile according to its id.
     *
     * @return the tile
     */
    public ArrayList<Tile> getAllTilesById() {
        return idToTiles;
    }

    /**
     * Get the granularity of this area.
     *
     * @return the granularity
     */
    public double getGranularity() {
        return granularity;
    }

    /**
     * Get the number of tiles.
     *
     * @return the number of tiles
     */
    public int getNumberOfTiles() {
        return numberOfTiles;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get a tile according to its id.
     *
     * @param id  the id of the tile
     * @return the tile
     */
    public Tile getTileById(int id) {
        return idToTiles.get(id);
    }

    /**
     * Get the list of tiles that are occupied by the given Shape.
     *
     * @param shape   the Shape for which to find occupied tiles
     * @return  the List of tiles that are occupied by the given Shape
     */
    public List<Tile> findOccupiedTiles(Shape shape) {
        // A place to store the answer
        List<Tile> occupiedTiles = new ArrayList<Tile>();
        // We only need to check the tiles that are within the bounding box
        Rectangle2D boundingBox = shape.getBounds2D();
        idToTiles.forEach(tile -> {
            if (tile.getArea().intersects(boundingBox)) {
                occupiedTiles.add(tile);
            }
        });
        return occupiedTiles;
    }
}
