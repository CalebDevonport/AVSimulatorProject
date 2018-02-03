package aim4.map.rim;

import aim4.config.Debug;
import aim4.im.aim.IntersectionManager;
import aim4.map.BasicIntersectionMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.util.ArrayListRegistry;
import aim4.util.GeomMath;
import aim4.util.Registry;
import aim4.vehicle.VinRegistry;

import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

/**
 * The grid layout map for RIM.
 */
public class RimIntersectionMap implements BasicIntersectionMap {
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The length of the no vehicle zone
     */
    private static final double NO_VEHICLE_ZONE_LENGTH = 28.0;
    // private static final double NO_VEHICLE_ZONE_LENGTH = 10.0;

    /**
     * The position of the data collection line on a lane
     */
    private static final double DATA_COLLECTION_LINE_POSITION =
            NO_VEHICLE_ZONE_LENGTH;

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The number of rows
     */
    private int rows;
    /**
     * The number of columns
     */
    private int columns;
    /**
     * The dimensions of the map
     */
    private Rectangle2D dimensions;
    /**
     * The set of roads
     */
    private List<Road> roads;
    /**
     * The set of horizontal roads
     */
    private List<Road> horizontalRoads = new ArrayList<Road>();
    /**
     * The set of vertical roads
     */
    private List<Road> verticalRoads = new ArrayList<Road>();
    //todo: rim intersection manager
    /**
     * The list of intersection managers
     */
    private List<IntersectionManager> intersectionManagers;
    /**
     * The array of intersection managers
     */
    private IntersectionManager[][] intersectionManagerGrid;
    /** The maximum speed limit  */
    private double memoMaximumSpeedLimit = -1;
    /**
     * The maximum lane speed limit
     */
    private double memoMaximumLaneSpeedLimit = -1;
    /**
     * The maximum roundabout speed limit
     */
    private double memoMaximumRoundaboutSpeedLimit = -1;
    /**
     * The data collection lines
     */
    private List<DataCollectionLine> dataCollectionLines;
    /**
     * The spawn points
     */
    private List<RIMSpawnPoint> spawnPoints;
    /**
     * The horizontal spawn points
     */
    private List<RIMSpawnPoint> horizontalSpawnPoints;
    /**
     * The vertical spawn points
     */
    private List<RIMSpawnPoint> verticalSpawnPoints;
    /**
     * The lane registry
     */
    private Registry<Lane> laneRegistry =
            new ArrayListRegistry<Lane>();
    /**
     * The IM registry
     */
    private Registry<IntersectionManager> imRegistry =
            new ArrayListRegistry<IntersectionManager>();
    /**
     * A mapping form lanes to roads they belong
     */
    private Map<Lane, Road> laneToRoad = new HashMap<Lane, Road>();

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    //todo: given Breath of vehicle, roundabout width, diameter roundabout and radius of entrance/exit circles

    /**
     * Create a grid map.
     *
     * @param initTime                  the initial time
     * @param columns                   the number of columns (number of vertical sets of roads)
     * @param rows                      the number of rows (number of horizontal sets of roads)
     * @param roundaboutDiameter        the diameter of the roundabout (should include width) D = 2 * Radius Central Island + laneWidth
     * @param entranceExitRadius        the radius of the entrance/exit circles
     * @param splitFactor               the split factor to divide the arc by / gives granularity
     * @param laneWidth                 the lane width
     * @param laneSpeedLimit            the lane speed limit
     * @param roundaboutSpeedLimit      the speed limit inside the roundabout
     * @param lanesPerRoad              the number of lanes per road
     * @param widthBetweenOppositeRoads the width of the area between the roads in opposite
     *                                  direction
     * @param distanceBetween           the distance between the adjacent intersections
     */
    public RimIntersectionMap(double initTime, int columns, int rows, double roundaboutDiameter, double entranceExitRadius,
                              int splitFactor, double laneWidth, double laneSpeedLimit, double roundaboutSpeedLimit,
                              int lanesPerRoad, double widthBetweenOppositeRoads, double distanceBetween) {
        // Can't make these unless there is at least one row and column
        if (rows < 1 || columns < 1) {
            throw new IllegalArgumentException("Must have at least one column " +
                    "and row!");
        }
        //Overwriting parameters to apply to a single lane roundabout
        columns = 1; // 1 set of vertical roads (N-S)
        rows = 1; //1 set of horizontal roads (E-W)
        this.columns = columns;
        this.rows = rows;
        lanesPerRoad = 1; // only one lane per road
        widthBetweenOppositeRoads = 0; // borders of roads coincide
        distanceBetween = 0; // the case for multiple intersections is not handled


        // One intersection roundabout map size is
        double height = 250;
        double width = 250;
        dimensions = new Rectangle2D.Double(0, 0, width, height);

        // Set size of array for the data collection lines.
        // 2 per sets of roads, one at either end.
        dataCollectionLines = new ArrayList<DataCollectionLine>(2 * (columns + rows));

        // Create roundabout path. Detailed map available here: http..
        // Calculate roundabout width (should be bigger than lane width)
        double roundaboutWidth = 1.2 * laneWidth;
        // Calculate roundabout radius
        double roundaboutRadius = (roundaboutDiameter - roundaboutWidth) / 2;

        // Calculate a,b,c,d parameters to construct the roundabout
        double a = entranceExitRadius + laneWidth / 2;
        double b = Math.sqrt(Math.pow(roundaboutRadius + entranceExitRadius, 2) - Math.pow(entranceExitRadius + laneWidth / 2, 2));
        double c = roundaboutRadius * a / (roundaboutRadius + entranceExitRadius);
        double d = roundaboutRadius * b / (roundaboutRadius + entranceExitRadius);
        double alpha = Math.toDegrees(Math.asin(a/(entranceExitRadius+roundaboutRadius)));
        double beta = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - 2 * alpha;

        // Setup map centre (all coordinates are positive):
        Point2D mapOrigin = new Point2D.Double(height / 2, width / 2);

        // Calculate origins of roundabout entrance/exit circles
        Point2D o1 = new Point2D.Double(mapOrigin.getX() + a, mapOrigin.getY() + b);
        Point2D o2 = new Point2D.Double(mapOrigin.getX() - a, mapOrigin.getY() + b);
        Point2D o3 = new Point2D.Double(mapOrigin.getX() - a, mapOrigin.getY() - b);
        Point2D o4 = new Point2D.Double(mapOrigin.getX() + a, mapOrigin.getY() - b);
        Point2D o5 = new Point2D.Double(mapOrigin.getX() + b, mapOrigin.getY() - a);
        Point2D o6 = new Point2D.Double(mapOrigin.getX() + b, mapOrigin.getY() + a);
        Point2D o7 = new Point2D.Double(mapOrigin.getX() - b, mapOrigin.getY() + a);
        Point2D o8 = new Point2D.Double(mapOrigin.getX() - b, mapOrigin.getY() - a);
        Point2D o = new Point2D.Double(mapOrigin.getX(), mapOrigin.getY());

        // ------------------------------------------------------------------------------------------------------------
        // Create the north vertical road
        Road right = new Road("Avenue N", this);

        // First line Lane going North A1-B1
        LineSegmentLane lineLaneNorth1 = new LineSegmentLane(
                mapOrigin.getX() + laneWidth / 2,
                height,
                mapOrigin.getX() + laneWidth / 2,
                height - b,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneNorth1.setId(laneRegistry.register(lineLaneNorth1));
        right.addTheUpMostLane(lineLaneNorth1);
        laneToRoad.put(lineLaneNorth1, right);

        // Second arc Lane entering roundabout B1-C1
        Arc2D arcNorth2 = new Arc2D.Double();
        double arcNorth2ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcNorth2StartAngle = Math.toDegrees(Math.PI) - arcNorth2ExtentAngle;
        arcNorth2.setArcByCenter(o1.getX(), o1.getY(), entranceExitRadius, arcNorth2StartAngle, - arcNorth2ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth2 = new ArcSegmentLane(arcNorth2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth2.setId(laneRegistry.register(arcLaneNorth2));
        right.addTheUpMostLane(arcLaneNorth2);
        laneToRoad.put(arcLaneNorth2, right);

        // Third arc Lane inside roundabout C1-C6
        Arc2D arcNorth3 = new Arc2D.Double();
        double arcNorth3ExtentAngle = beta;
        double arcNorth3StartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha;
        arcNorth3.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcNorth3StartAngle, - arcNorth3ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth3 = new ArcSegmentLane(arcNorth3, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneNorth3.setId(laneRegistry.register(arcLaneNorth3));
        right.addTheUpMostLane(arcLaneNorth3);
        laneToRoad.put(arcLaneNorth3, right);

        // Fourth arc Lane inside roundabout C6-C5
        Arc2D arcNorth4 = new Arc2D.Double();
        double arcNorth4ExtentAngle = 2 * alpha;
        double arcNorth4StartAngle = Math.toDegrees(GeomMath.TWO_PI) + alpha;
        arcNorth4.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcNorth4StartAngle, - arcNorth4ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth4 = new ArcSegmentLane(arcNorth4, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneNorth4.setId(laneRegistry.register(arcLaneNorth4));
        right.addTheUpMostLane(arcLaneNorth4);
        laneToRoad.put(arcLaneNorth4, right);

        // Fifth arc Lane inside roundabout C5-C4
        Arc2D arcNorth5 = new Arc2D.Double();
        double arcNorth5ExtentAngle = beta;
        double arcNorth5StartAngle = alpha + beta;
        arcNorth4.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcNorth5StartAngle, - arcNorth5ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth5 = new ArcSegmentLane(arcNorth5, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneNorth5.setId(laneRegistry.register(arcLaneNorth5));
        right.addTheUpMostLane(arcLaneNorth5);
        laneToRoad.put(arcLaneNorth5, right);

        // Sixth arc Lane exiting roundabout C4-B4
        Arc2D arcNorth6 = new Arc2D.Double();
        double arcNorth6ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcNorth6StartAngle = Math.toDegrees(Math.PI) + arcNorth6ExtentAngle;
        arcNorth4.setArcByCenter(o4.getX(), o4.getY(), entranceExitRadius, arcNorth6StartAngle, - arcNorth6ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth6 = new ArcSegmentLane(arcNorth6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth6.setId(laneRegistry.register(arcLaneNorth6));
        right.addTheUpMostLane(arcLaneNorth6);
        laneToRoad.put(arcLaneNorth6, right);

        // Seventh line Lane exiting roundabout B4-A4
        LineSegmentLane lineLaneNorth7 = new LineSegmentLane(
                mapOrigin.getX() + laneWidth / 2,
                mapOrigin.getY() - b,
                mapOrigin.getX() + laneWidth / 2,
                0,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneNorth7.setId(laneRegistry.register(lineLaneNorth7));
        right.addTheUpMostLane(lineLaneNorth7);
        laneToRoad.put(lineLaneNorth7, right);

        verticalRoads.add(right);
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "NorthBound" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                height - DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() + laneWidth,
                                height - DATA_COLLECTION_LINE_POSITION),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "NorthBound" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() + laneWidth,
                                DATA_COLLECTION_LINE_POSITION),
                        true));

        // ------------------------------------------------------------------------------------------------------------
        // Create the south vertical road
        Road left = new Road("Avenue S", this);
        // First line Lane going South A3-B3
        LineSegmentLane lineLaneSouth1 = new LineSegmentLane(
                mapOrigin.getX() - laneWidth / 2,
                0,
                mapOrigin.getX() - laneWidth / 2,
                mapOrigin.getY() - b,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneSouth1.setId(laneRegistry.register(lineLaneSouth1));
        left.addTheUpMostLane(lineLaneSouth1);
        laneToRoad.put(lineLaneSouth1, left);

        // Second arc Lane entering roundabout B3-C3
        Arc2D arcSouth2 = new Arc2D.Double();
        double arcSouth2ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcSouth2StartAngle = Math.toDegrees(GeomMath.TWO_PI);
        arcSouth2.setArcByCenter(o3.getX(), o3.getY(), entranceExitRadius, arcSouth2StartAngle, - arcSouth2ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth2 = new ArcSegmentLane(arcSouth2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth2.setId(laneRegistry.register(arcLaneSouth2));
        left.addTheUpMostLane(arcLaneSouth2);
        laneToRoad.put(arcLaneSouth2, left);

        // Third arc Lane inside roundabout C3-C8
        Arc2D arcSouth3 = new Arc2D.Double();
        double arcSouth3ExtentAngle = beta;
        double arcSouth3StartAngle = Math.toDegrees(GeomMath.PI) - alpha;
        arcSouth3.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcSouth3StartAngle, - arcSouth3ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth3 = new ArcSegmentLane(arcSouth3, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneSouth3.setId(laneRegistry.register(arcLaneSouth3));
        left.addTheUpMostLane(arcLaneSouth3);
        laneToRoad.put(arcLaneSouth3, left);

        // Fourth arc Lane inside roundabout C8-C7
        Arc2D arcSouth4 = new Arc2D.Double();
        double arcSouth4ExtentAngle = 2 * alpha;
        double arcSouth4StartAngle = Math.toDegrees(GeomMath.PI) + alpha;
        arcSouth4.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcSouth4StartAngle, - arcSouth4ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth4 = new ArcSegmentLane(arcSouth4, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneSouth4.setId(laneRegistry.register(arcLaneSouth4));
        left.addTheUpMostLane(arcLaneSouth4);
        laneToRoad.put(arcLaneSouth4, left);

        // Fifth arc Lane inside roundabout C7-C2
        Arc2D arcSouth5 = new Arc2D.Double();
        double arcSouth5ExtentAngle = beta;
        double arcSouth5StartAngle = Math.toDegrees(GeomMath.PI) + alpha + beta;
        arcSouth5.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcSouth5StartAngle, - arcSouth5ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth5 = new ArcSegmentLane(arcSouth5, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneSouth5.setId(laneRegistry.register(arcLaneSouth5));
        left.addTheUpMostLane(arcLaneSouth5);
        laneToRoad.put(arcLaneSouth5, left);

        // Sixth arc Lane exiting roundabout C2-B2
        Arc2D arcSouth6 = new Arc2D.Double();
        double arcSouth6ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcSouth6StartAngle = Math.toDegrees(GeomMath.TWO_PI) + arcSouth6ExtentAngle;
        arcSouth6.setArcByCenter(o2.getX(), o2.getY(), entranceExitRadius, arcSouth6StartAngle, - arcSouth6ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth6 = new ArcSegmentLane(arcSouth6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth6.setId(laneRegistry.register(arcLaneSouth6));
        left.addTheUpMostLane(arcLaneSouth6);
        laneToRoad.put(arcLaneSouth6, left);

        // Seventh line Lane outside roundabout B2-A2
        LineSegmentLane lineLaneSouth7 = new LineSegmentLane(
                mapOrigin.getX() - laneWidth / 2,
                mapOrigin.getY() + b,
                mapOrigin.getX() - laneWidth / 2,
                height,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneSouth7.setId(laneRegistry.register(lineLaneSouth7));
        left.addTheUpMostLane(lineLaneSouth7);
        laneToRoad.put(lineLaneSouth7, left);

        verticalRoads.add(left);
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "SouthBound" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() - laneWidth,
                                DATA_COLLECTION_LINE_POSITION),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "SouthBound" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                height - DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() - laneWidth,
                                height - DATA_COLLECTION_LINE_POSITION),
                        true));

        // Set up the "dual" relationship
        right.setDual(left);

        // ------------------------------------------------------------------------------------------------------------
        // Create the east horizontal road
        Road lower = new Road("Avenue E", this);

        // First line Lane going East A7-B7
        LineSegmentLane lineLaneEast1 = new LineSegmentLane(
                0,
                mapOrigin.getY() + laneWidth/2,
                mapOrigin.getX() - b,
                mapOrigin.getY() + laneWidth/2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneEast1.setId(laneRegistry.register(lineLaneEast1));
        lower.addTheUpMostLane(lineLaneEast1);
        laneToRoad.put(lineLaneEast1, lower);

        // Second arc Lane entering roundabout B7-C7
        Arc2D arcEast2 = new Arc2D.Double();
        double arcEast2ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcEast2StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
        arcEast2.setArcByCenter(o7.getX(), o7.getY(), entranceExitRadius, arcEast2StartAngle, - arcEast2ExtentAngle, 0);
        ArcSegmentLane arcLaneEast2 = new ArcSegmentLane(arcEast2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast2.setId(laneRegistry.register(arcLaneEast2));
        lower.addTheUpMostLane(arcLaneEast2);
        laneToRoad.put(arcLaneEast2, lower);

        // Third arc Lane inside roundabout C7-C2
        Arc2D arcEast3 = new Arc2D.Double();
        double arcEast3ExtentAngle = beta;
        double arcEast3StartAngle = Math.toDegrees(GeomMath.PI) + alpha + beta;
        arcEast3.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcEast3StartAngle, - arcEast3ExtentAngle, 0);
        ArcSegmentLane arcLaneEast3 = new ArcSegmentLane(arcEast3, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneEast3.setId(laneRegistry.register(arcLaneEast3));
        lower.addTheUpMostLane(arcLaneEast3);
        laneToRoad.put(arcLaneEast3, lower);

        // Fourth arc Lane inside roundabout C2-C1
        Arc2D arcEast4 = new Arc2D.Double();
        double arcEast4ExtentAngle = 2 * alpha;
        double arcEast4StartAngle = Math.toDegrees(GeomMath.TWO_PI) - beta - alpha;
        arcEast4.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcEast4StartAngle, - arcEast4ExtentAngle, 0);
        ArcSegmentLane arcLaneEast4 = new ArcSegmentLane(arcEast4, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneEast4.setId(laneRegistry.register(arcLaneEast4));
        lower.addTheUpMostLane(arcLaneEast4);
        laneToRoad.put(arcLaneEast4, lower);

        // Third arc Lane inside roundabout C1-C6
        Arc2D arcEast5 = new Arc2D.Double();
        double arcEast5ExtentAngle = beta;
        double arcEast5StartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha;
        arcEast5.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcEast5StartAngle, - arcEast5ExtentAngle, 0);
        ArcSegmentLane arcLaneEast5 = new ArcSegmentLane(arcEast5, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneEast5.setId(laneRegistry.register(arcLaneEast5));
        lower.addTheUpMostLane(arcLaneEast5);
        laneToRoad.put(arcLaneEast5, lower);

        // Sixth arc Lane exiting roundabout C6-B6
        Arc2D arcEast6 = new Arc2D.Double();
        double arcEast6ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcEast6StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + arcNorth6ExtentAngle;
        arcEast6.setArcByCenter(o6.getX(), o6.getY(), entranceExitRadius, arcEast6StartAngle, - arcEast6ExtentAngle, 0);
        ArcSegmentLane arcLaneEast6 = new ArcSegmentLane(arcEast6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast6.setId(laneRegistry.register(arcLaneEast6));
        lower.addTheUpMostLane(arcLaneEast6);
        laneToRoad.put(arcLaneEast6, lower);

        // Seventh line Lane exiting roundabout B6-A6
        LineSegmentLane lineLaneEast7 = new LineSegmentLane(
                mapOrigin.getX() + b,
                mapOrigin.getY() + laneWidth / 2,
                width,
                mapOrigin.getY() + laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneEast7.setId(laneRegistry.register(lineLaneEast7));
        lower.addTheUpMostLane(lineLaneEast7);
        laneToRoad.put(lineLaneEast7, lower);

        horizontalRoads.add(lower);
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "EastBound" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() + laneWidth),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "EastBound" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() + laneWidth),
                        true));

        // ------------------------------------------------------------------------------------------------------------
        // Now we create the west horizontal road
        Road upper = new Road("Avenue W", this);

        // First line Lane going West A5-B5
        LineSegmentLane lineLaneWest1 = new LineSegmentLane(
                width,
                mapOrigin.getY() - laneWidth / 2,
                mapOrigin.getX() + b,
                mapOrigin.getY() - laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneWest1.setId(laneRegistry.register(lineLaneWest1));
        upper.addTheUpMostLane(lineLaneWest1);
        laneToRoad.put(lineLaneWest1, upper);

        // Second arc Lane entering roundabout B5-C5
        Arc2D arcWest2 = new Arc2D.Double();
        double arcWest2ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcWest2StartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
        arcWest2.setArcByCenter(o5.getX(), o5.getY(), entranceExitRadius, arcWest2StartAngle, - arcWest2ExtentAngle, 0);
        ArcSegmentLane arcLaneWest2 = new ArcSegmentLane(arcWest2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest2.setId(laneRegistry.register(arcLaneWest2));
        upper.addTheUpMostLane(arcLaneWest2);
        laneToRoad.put(arcLaneWest2, upper);

        // Third arc Lane inside roundabout C5-C4
        Arc2D arcWest3 = new Arc2D.Double();
        double arcWest3ExtentAngle = beta;
        double arcWest3StartAngle = alpha + beta;
        arcWest3.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcWest3StartAngle, - arcWest3ExtentAngle, 0);
        ArcSegmentLane arcLaneWest3 = new ArcSegmentLane(arcWest3, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneWest3.setId(laneRegistry.register(arcLaneWest3));
        upper.addTheUpMostLane(arcLaneWest3);
        laneToRoad.put(arcLaneWest3, upper);

        // Fourth arc Lane inside roundabout C4-C3
        Arc2D arcWest4 = new Arc2D.Double();
        double arcWest4ExtentAngle = 2 * alpha;
        double arcWest4StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + alpha;
        arcWest4.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcWest4StartAngle, - arcWest4ExtentAngle, 0);
        ArcSegmentLane arcLaneWest4 = new ArcSegmentLane(arcWest4, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneWest4.setId(laneRegistry.register(arcLaneWest4));
        upper.addTheUpMostLane(arcLaneWest4);
        laneToRoad.put(arcLaneWest4, upper);

        // Fifth arc Lane inside roundabout C3-C8
        Arc2D arcWest5 = new Arc2D.Double();
        double arcWest5ExtentAngle = beta;
        double arcWest5StartAngle = Math.toDegrees(GeomMath.PI) - alpha;
        arcWest5.setArcByCenter(o.getX(), o.getY(), roundaboutRadius, arcWest5StartAngle, - arcWest5ExtentAngle, 0);
        ArcSegmentLane arcLaneWest5 = new ArcSegmentLane(arcWest5, roundaboutWidth, roundaboutSpeedLimit, splitFactor, false);
        arcLaneWest5.setId(laneRegistry.register(arcLaneWest5));
        upper.addTheUpMostLane(arcLaneWest5);
        laneToRoad.put(arcLaneWest5, upper);

        // Sixth arc Lane exiting roundabout C8-B8
        Arc2D arcWest6 = new Arc2D.Double();
        double arcWest6ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        double arcWest6StartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + arcWest6ExtentAngle;
        arcWest6.setArcByCenter(o8.getX(), o8.getY(), entranceExitRadius, arcWest6StartAngle, - arcWest6ExtentAngle, 0);
        ArcSegmentLane arcLaneWest6 = new ArcSegmentLane(arcWest6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest6.setId(laneRegistry.register(arcLaneWest6));
        upper.addTheUpMostLane(arcLaneWest6);
        laneToRoad.put(arcLaneWest6, upper);

        // Seventh line Lane outside roundabout B8-A8
        LineSegmentLane lineLaneWest7 = new LineSegmentLane(
                mapOrigin.getX() - b,
                mapOrigin.getY() - laneWidth / 2,
                0,
                mapOrigin.getY() - laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneWest7.setId(laneRegistry.register(lineLaneWest7));
        upper.addTheUpMostLane(lineLaneWest7);
        laneToRoad.put(lineLaneWest7, upper);

        horizontalRoads.add(upper);
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "WestBound" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() - laneWidth),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "WestBound" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() - laneWidth),
                        true));

        // Set up the "dual" relationship
        lower.setDual(upper);

        roads = new ArrayList<Road>(horizontalRoads);
        roads.addAll(verticalRoads);
        roads = Collections.unmodifiableList(roads);

        // We should have columns * rows intersections, so make space for 'em
        intersectionManagers = new ArrayList<IntersectionManager>(columns * rows);
        intersectionManagerGrid = new IntersectionManager[columns][rows];

        initializeSpawnPoints(initTime);
    }
    public RimIntersectionMap(RimIntersectionMap map, double laneWidth, double roundaboutDiameter, double entranceExitRadius,
                               int splitFactor, double widthBetweenOppositeRoads, double distanceBetween) {
        this(0, map.getColumns(), map.getRows(), roundaboutDiameter, entranceExitRadius, splitFactor,
                laneWidth, map.getMaximumLaneSpeedLimit(), map.getMaximumRoundaboutSpeedLimit(),
                map.getRoads().get(0).getLanes().size(),
                widthBetweenOppositeRoads, distanceBetween);
    }

    /**
     * Initialize spawn points.
     *
     * @param initTime  the initial time
     */
    private void initializeSpawnPoints(double initTime) {
        spawnPoints = new ArrayList<RIMSpawnPoint>(columns+rows);
        horizontalSpawnPoints = new ArrayList<RIMSpawnPoint>(rows);
        verticalSpawnPoints = new ArrayList<RIMSpawnPoint>(columns);

        for(Road road : horizontalRoads) {
            horizontalSpawnPoints.add(makeSpawnPoint(initTime, road.getContinuousLanes().get(0)));
        }

        for(Road road : verticalRoads) {
            verticalSpawnPoints.add(makeSpawnPoint(initTime, road.getContinuousLanes().get(0)));
        }

        spawnPoints.addAll(horizontalSpawnPoints);
        spawnPoints.addAll(verticalSpawnPoints);

        Debug.currentMap = this;
    }

    /**
     * Initialize one spawn point on the eastbound road.
     * Added this method so we can follow one driver agent when debugging.
     * Useful to understand more how their driver agent works.
     *
     * @param initTime  the initial time
     */
    private void initializeOneSpawnPoint(double initTime) {
        if(rows > 1 || columns > 1) {
            throw new IllegalArgumentException("Undefined behaviour with one spawn point");
        }
        spawnPoints = new ArrayList<RIMSpawnPoint>(1);
        horizontalSpawnPoints = new ArrayList<RIMSpawnPoint>(1);

        Lane lane = horizontalRoads.get(0).getContinuousLanes().get(0);
        horizontalSpawnPoints.add(makeSpawnPoint(initTime, lane));

        spawnPoints.addAll(horizontalSpawnPoints);

        Debug.currentMap = this;
    }

    /**
     * Make spawn points.
     *
     * @param initTime  the initial time
     * @param lane      the lane
     * @return the spawn point
     */
    private RIMSpawnPoint makeSpawnPoint(double initTime, Lane lane) {
        double startDistance = 0.0;
        double normalizedStartDistance = lane.normalizedDistance(startDistance);
        Point2D pos = lane.getPointAtNormalizedDistance(normalizedStartDistance);
        double heading = lane.getInitialHeading();
        double steeringAngle = 0.0;
        double acceleration = 0.0;
        double d = lane.normalizedDistance(startDistance + NO_VEHICLE_ZONE_LENGTH);
        Rectangle2D noVehicleZone =
                lane.getShape(normalizedStartDistance, d).getBounds2D();

        return new RIMSpawnPoint(initTime, pos, heading, steeringAngle, acceleration,
                lane, noVehicleZone);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Road> getRoads() {
        return roads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Road> getDestinationRoads() {
        return roads;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getDimensions() {
        return dimensions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaximumSpeedLimit() {
        if(memoMaximumSpeedLimit < 0) {
            for(Road r : getRoads()) {
                for(Lane l : r.getLanes()) {
                    if(l.getSpeedLimit() > memoMaximumSpeedLimit) {
                        memoMaximumSpeedLimit = l.getSpeedLimit();
                    }
                }
            }
        }
        return memoMaximumSpeedLimit;
    }

    /**
     * Get the maximum speed limit in the lane.
     */
    public double getMaximumLaneSpeedLimit() {
        if(memoMaximumLaneSpeedLimit < 0) {
            memoMaximumLaneSpeedLimit = getRoads().get(0).getContinuousLanes().get(0).getSpeedLimit();
        }
        return memoMaximumLaneSpeedLimit;
    }

    /**
     * Get the maximum speed limit in the roundabout.
     */
    public double getMaximumRoundaboutSpeedLimit() {
        if(memoMaximumRoundaboutSpeedLimit < 0) {
            memoMaximumRoundaboutSpeedLimit = getRoads().get(0).getContinuousLanes().get(1).getSpeedLimit();
        }
        return memoMaximumRoundaboutSpeedLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IntersectionManager> getIntersectionManagers() {
        return Collections.unmodifiableList(intersectionManagers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataCollectionLine> getDataCollectionLines() {
        return dataCollectionLines;
    }

    @Override
    public List<AIMSpawnPoint> getSpawnPoints() {
        // Not needed
        return null;
    }


    /**
     * Return RIM Spawn Points
     */

    public List<RIMSpawnPoint> getRIMSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Get the list of horizontal spawn points.
     *
     * @return the list of horizontal spawn points
     */
    public List<RIMSpawnPoint> getHorizontalSpawnPoints() {
        return horizontalSpawnPoints;
    }


    /**
     * Get the list of vertical spawn points.
     *
     * @return the list of vertical spawn points
     */
    public List<RIMSpawnPoint> getVerticalSpawnPoints() {
        return verticalSpawnPoints;
    }

    /////////////////////////////////////////////
    // PUBLIC METHODS  (specific to Grid Layout)
    /////////////////////////////////////////////

    /**
     * Get the number of rows in this grid layout.
     *
     * @return the number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the number of columns in this grid layout.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
    }


    /**
     * Get the list of all roads that enter a particular intersection.
     *
     * @param column  the column of the intersection
     * @param row     the row of the intersection
     * @return the list of roads that enter the intersection at (column, row)
     */
    public List<Road> getRoads(int column, int row) {
        // First some error checking
        if(row >= rows || column >= columns || row < 0 || column < 0) {
            throw new ArrayIndexOutOfBoundsException("(" + column + "," + row +
                    " are not valid indices. " +
                    "This GridLayout only has " +
                    column + " columns and " +
                    row + " rows.");
        }
        List<Road> answer = new ArrayList<Road>();
        answer.add(verticalRoads.get(2 * column));
        answer.add(verticalRoads.get(2 * column + 1));
        answer.add(horizontalRoads.get(2 * row));
        answer.add(horizontalRoads.get(2 * row + 1));
        return answer;
    }

    /**
     * Get the set of horizontal roads.
     *
     * @return the set of horizontal roads
     */
    public List<Road> getHorizontalRoads() {
        return horizontalRoads;
    }

    /**
     * Get the set of vertical roads.
     *
     * @return the set of vertical roads
     */
    public List<Road> getVerticalRoads() {
        return verticalRoads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<IntersectionManager> getImRegistry() {
        return imRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<Lane> getLaneRegistry() {
        return laneRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Road getRoad(Lane lane) {
        return laneToRoad.get(lane);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Road getRoad(int laneID) {
        return laneToRoad.get(laneRegistry.get(laneID));
    }

    /**
     * Get the intersection manager of a particular intersection.
     *
     * @param column  the column of the intersection
     * @param row     the row of the intersection
     */
    public IntersectionManager getManager(int column, int row) {
        return intersectionManagerGrid[column][row];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(int column, int row, IntersectionManager im) {
        // Barf if this is already set
        if (intersectionManagerGrid[column][row] != null) {
            throw new RuntimeException("The intersection manager at (" + column +
                    ", " + row + ") has already been set!");
        }
        intersectionManagerGrid[column][row] = im;
        intersectionManagers.add(im);
    }

    /**
     * Remove managers in all intersections.
     */
    public void removeAllManagers() {
        for(int column = 0; column < columns; column++) {
            for(int row = 0; row < rows; row++) {
                intersectionManagerGrid[column][row] = null;
            }
        }
        intersectionManagers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printDataCollectionLinesData(String outFileName) {
        PrintStream outfile = null;
        try {
            outfile = new PrintStream(outFileName);
        } catch (FileNotFoundException e) {
            System.err.printf("Cannot open file %s\n", outFileName);
            return;
        }
        // TODO: sort by time and LineId and VIN
        outfile.printf("VIN,Time,DCLname,vType,startLaneId,destRoad\n");
        for (DataCollectionLine line : dataCollectionLines) {
            for (int vin : line.getAllVIN()) {
                for(double time : line.getTimes(vin)) {
                    outfile.printf("%d,%.4f,%s,%s,%d,%s\n",
                            vin, time, line.getName(),
                            VinRegistry.getVehicleSpecFromVIN(vin).getName(),
                            VinRegistry.getSpawnPointFromVIN(vin).getLane().getId(),
                            VinRegistry.getDestRoadFromVIN(vin).getName());
                }
            }
        }

        outfile.close();
    }
}
