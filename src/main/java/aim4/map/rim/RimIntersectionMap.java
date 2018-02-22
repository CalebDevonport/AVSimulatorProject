package aim4.map.rim;

import aim4.config.Debug;
import aim4.im.aim.IntersectionManager;
import aim4.map.BasicIntersectionMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.aim.AIMSpawnPoint;
import aim4.map.connections.RimConnection;
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
 * The grid layout map for rim.
 */
public class RimIntersectionMap implements BasicIntersectionMap {
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The length of the no vehicle zone
     */
    private static final double NO_VEHICLE_ZONE_LENGTH = 16.0;

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
    /**
     * The list of intersection managers
     */
    private List<IntersectionManager> intersectionManagers;
    /**
     * The array of intersection managers
     */
    private IntersectionManager[][] intersectionManagerGrid;
    /**
     * The maximum speed limit
     */
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
    private List<AIMSpawnPoint> spawnPoints;
    /**
     * The horizontal spawn points
     */
    private List<AIMSpawnPoint> horizontalSpawnPoints;
    /**
     * The vertical spawn points
     */
    private List<AIMSpawnPoint> verticalSpawnPoints;
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
     * A mapping from lanes to roads they belong
     */
    private Map<Lane, Road> laneToRoad = new HashMap<Lane, Road>();
    /**
     * A mapping from all lanes (including line lanes decomposition) to roads they belong
     */
    private Map<Lane, Road> laneDecompositionToRoad = new HashMap<Lane, Road>();

    /** The RIM connection */
    private List<RimConnection> rimConnections = new ArrayList<RimConnection>();

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    //todo: For each road have only one segment lane for inside roundabout c1-c4

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
        dataCollectionLines = new ArrayList<DataCollectionLine>();

        // Create roundabout path. Detailed map available here: http..
        // Calculate roundabout width (should be bigger than lane width)
        double roundaboutWidth = 1.5 * laneWidth;
        // Set the lane width to be same size as roundabout width. Should not affect the results
        //todo: add distance between roads to be able to use lane width different than roundabout width
        laneWidth = roundaboutWidth;
        // Calculate roundabout radius
        double roundaboutRadius = (roundaboutDiameter - roundaboutWidth) / 2;

        // Calculate a,b,c,d parameters to construct the roundabout
        double a = entranceExitRadius + laneWidth / 2;
        double b = Math.sqrt(Math.pow(roundaboutRadius + entranceExitRadius, 2) - Math.pow(entranceExitRadius + laneWidth / 2, 2));
        double c = (roundaboutRadius / (roundaboutRadius + entranceExitRadius)) * a;
        double d = (roundaboutRadius / (roundaboutRadius + entranceExitRadius)) * b;
        double e = b - d - laneWidth;
        double alpha = Math.toDegrees(Math.asin(a / (entranceExitRadius + roundaboutRadius)));
        double beta = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - 2 * alpha;
        double theta = Math.toDegrees(Math.asin(e / (entranceExitRadius + laneWidth / 2)));

        // Setup map centre (all coordinates are positive):
        Point2D mapOrigin = new Point2D.Double(height / 2, width / 2);

        // Calculate origins of roundabout entrance/exit circles
        Point2D O1 = new Point2D.Double(mapOrigin.getX() + a, mapOrigin.getY() + b);
        Point2D O2 = new Point2D.Double(mapOrigin.getX() - a, mapOrigin.getY() + b);
        Point2D O3 = new Point2D.Double(mapOrigin.getX() - a, mapOrigin.getY() - b);
        Point2D O4 = new Point2D.Double(mapOrigin.getX() + a, mapOrigin.getY() - b);
        Point2D O5 = new Point2D.Double(mapOrigin.getX() + b, mapOrigin.getY() - a);
        Point2D O6 = new Point2D.Double(mapOrigin.getX() + b, mapOrigin.getY() + a);
        Point2D O7 = new Point2D.Double(mapOrigin.getX() - b, mapOrigin.getY() + a);
        Point2D O8 = new Point2D.Double(mapOrigin.getX() - b, mapOrigin.getY() - a);
        Point2D O = new Point2D.Double(mapOrigin.getX(), mapOrigin.getY());

        // ------------------------------------------------------------------------------------------------------------
        // Create the north vertical road
        Road right = new Road("1st Avenue N", this);

        // First line Lane going North A1-B1
        LineSegmentLane lineLaneNorth1 = new LineSegmentLane(
                mapOrigin.getX() + laneWidth / 2,
                height,
                mapOrigin.getX() + laneWidth / 2,
                mapOrigin.getY() + b,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneNorth1.setId(laneRegistry.register(lineLaneNorth1));
        right.addTheUpMostLane(lineLaneNorth1);
        laneToRoad.put(lineLaneNorth1, right);
        laneDecompositionToRoad.put(lineLaneNorth1, right);

        // Second arc Lane approaching the roundabout B1-E1
        Arc2D arcNorth2 = new Arc2D.Double();
        double arcNorth2ExtentAngle = theta;
        double arcNorth2StartAngle = Math.toDegrees(GeomMath.PI);
        arcNorth2.setArcByCenter(O1.getX(), O1.getY(), entranceExitRadius, arcNorth2StartAngle, -arcNorth2ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth2 = new ArcSegmentLane(arcNorth2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth2.setId(laneRegistry.register(arcLaneNorth2));
        right.addTheUpMostLane(arcLaneNorth2);
        laneToRoad.put(arcLaneNorth2, right);
        arcLaneNorth2.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Third arc Lane entering roundabout E1-C1
        Arc2D arcNorth3 = new Arc2D.Double();
        double arcNorth3ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcNorth3StartAngle = Math.toDegrees(GeomMath.PI) - theta;
        arcNorth3.setArcByCenter(O1.getX(), O1.getY(), entranceExitRadius, arcNorth3StartAngle, -arcNorth3ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth3 = new ArcSegmentLane(arcNorth3, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth3.setId(laneRegistry.register(arcLaneNorth3));
        right.addTheUpMostLane(arcLaneNorth3);
        laneToRoad.put(arcLaneNorth3, right);
        arcLaneNorth3.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Fourth arc Lane inside roundabout C1-C6
        Arc2D arcNorth4 = new Arc2D.Double();
        double arcNorth4ExtentAngle = beta;
        double arcNorth4StartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha - beta;
        arcNorth4.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcNorth4StartAngle, arcNorth4ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth4 = new ArcSegmentLane(arcNorth4, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth4.setId(laneRegistry.register(arcLaneNorth4));
        right.addTheUpMostLane(arcLaneNorth4);
        laneToRoad.put(arcLaneNorth4, right);
        arcLaneNorth4.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Fifth arc Lane inside roundabout C6-C5
        Arc2D arcNorth5 = new Arc2D.Double();
        double arcNorth5ExtentAngle = 2 * alpha;
        double arcNorth5StartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha;
        arcNorth5.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcNorth5StartAngle, arcNorth5ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth5 = new ArcSegmentLane(arcNorth5, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneNorth5.setId(laneRegistry.register(arcLaneNorth5));
        right.addTheUpMostLane(arcLaneNorth5);
        laneToRoad.put(arcLaneNorth5, right);
        arcLaneNorth5.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Sixth arc Lane inside roundabout C5-C4
        Arc2D arcNorth6 = new Arc2D.Double();
        double arcNorth6ExtentAngle = beta;
        double arcNorth6StartAngle = alpha;
        arcNorth6.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcNorth6StartAngle, arcNorth6ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth6 = new ArcSegmentLane(arcNorth6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth6.setId(laneRegistry.register(arcLaneNorth6));
        right.addTheUpMostLane(arcLaneNorth6);
        laneToRoad.put(arcLaneNorth6, right);
        arcLaneNorth6.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Seventh arc Lane exiting roundabout C4-E4
        Arc2D arcNorth7 = new Arc2D.Double();
        double arcNorth7ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcNorth7StartAngle = Math.toDegrees(GeomMath.PI) + Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        arcNorth7.setArcByCenter(O4.getX(), O4.getY(), entranceExitRadius, arcNorth7StartAngle, - arcNorth7ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth7 = new ArcSegmentLane(arcNorth7, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth7.setId(laneRegistry.register(arcLaneNorth7));
        right.addTheUpMostLane(arcLaneNorth7);
        laneToRoad.put(arcLaneNorth7, right);
        arcLaneNorth7.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Eight arc Lane exiting on approach the roundabout E4-B4
        Arc2D arcNorth8 = new Arc2D.Double();
        double arcNorth8ExtentAngle = theta;
        double arcNorth8StartAngle = Math.toDegrees(Math.PI) + arcNorth8ExtentAngle;
        arcNorth8.setArcByCenter(O4.getX(), O4.getY(), entranceExitRadius, arcNorth8StartAngle, - arcNorth8ExtentAngle, 0);
        ArcSegmentLane arcLaneNorth8 = new ArcSegmentLane(arcNorth8, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth8.setId(laneRegistry.register(arcLaneNorth8));
        right.addTheUpMostLane(arcLaneNorth8);
        laneToRoad.put(arcLaneNorth8, right);
        arcLaneNorth8.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Ninth line Lane exiting roundabout B4-A4
        LineSegmentLane lineLaneNorth9 = new LineSegmentLane(
                mapOrigin.getX() + laneWidth / 2,
                mapOrigin.getY() - b,
                mapOrigin.getX() + laneWidth / 2,
                0,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneNorth9.setId(laneRegistry.register(lineLaneNorth9));
        right.addTheUpMostLane(lineLaneNorth9);
        laneToRoad.put(lineLaneNorth9, right);
        laneDecompositionToRoad.put(lineLaneNorth9, right);

        // Set continuous lanes for all arc lanes
        arcLaneNorth2.setContinuousLanes();
        arcLaneNorth3.setContinuousLanes();
        arcLaneNorth4.setContinuousLanes();
        arcLaneNorth5.setContinuousLanes();
        arcLaneNorth6.setContinuousLanes();
        arcLaneNorth7.setContinuousLanes();
        arcLaneNorth8.setContinuousLanes();

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
        Road left = new Road("1st Avenue S", this);
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
        laneDecompositionToRoad.put(lineLaneSouth1, left);

        // Second arc Lane approaching roundabout B3-E3
        Arc2D arcSouth2 = new Arc2D.Double();
        double arcSouth2ExtentAngle = theta;
        double arcSouth2StartAngle = Math.toDegrees(GeomMath.TWO_PI);
        arcSouth2.setArcByCenter(O3.getX(), O3.getY(), entranceExitRadius, arcSouth2StartAngle, -arcSouth2ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth2 = new ArcSegmentLane(arcSouth2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth2.setId(laneRegistry.register(arcLaneSouth2));
        left.addTheUpMostLane(arcLaneSouth2);
        laneToRoad.put(arcLaneSouth2, left);
        arcLaneSouth2.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Third arc Lane entering roundabout E3-C3
        Arc2D arcSouth3 = new Arc2D.Double();
        double arcSouth3ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcSouth3StartAngle = Math.toDegrees(GeomMath.TWO_PI) - theta;
        arcSouth3.setArcByCenter(O3.getX(), O3.getY(), entranceExitRadius, arcSouth3StartAngle, -arcSouth3ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth3 = new ArcSegmentLane(arcSouth3, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth3.setId(laneRegistry.register(arcLaneSouth3));
        left.addTheUpMostLane(arcLaneSouth3);
        laneToRoad.put(arcLaneSouth3, left);
        arcLaneSouth3.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Fourth arc Lane inside roundabout C3-C8
        Arc2D arcSouth4 = new Arc2D.Double();
        double arcSouth4ExtentAngle = beta;
        double arcSouth4StartAngle = Math.toDegrees(GeomMath.PI) - alpha - beta;
        arcSouth4.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcSouth4StartAngle, arcSouth4ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth4 = new ArcSegmentLane(arcSouth4, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth4.setId(laneRegistry.register(arcLaneSouth4));
        left.addTheUpMostLane(arcLaneSouth4);
        laneToRoad.put(arcLaneSouth4, left);
        arcLaneSouth4.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Fifth arc Lane inside roundabout C8-C7
        Arc2D arcSouth5 = new Arc2D.Double();
        double arcSouth5ExtentAngle = 2 * alpha;
        double arcSouth5StartAngle = Math.toDegrees(GeomMath.PI) - alpha;
        arcSouth5.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcSouth5StartAngle, arcSouth5ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth5 = new ArcSegmentLane(arcSouth5, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneSouth5.setId(laneRegistry.register(arcLaneSouth5));
        left.addTheUpMostLane(arcLaneSouth5);
        laneToRoad.put(arcLaneSouth5, left);
        arcLaneSouth5.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Sixth arc Lane inside roundabout C7-C2
        Arc2D arcSouth6 = new Arc2D.Double();
        double arcSouth6ExtentAngle = beta;
        double arcSouth6StartAngle = Math.toDegrees(GeomMath.PI) + alpha;
        arcSouth6.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcSouth6StartAngle, arcSouth6ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth6 = new ArcSegmentLane(arcSouth6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth6.setId(laneRegistry.register(arcLaneSouth6));
        left.addTheUpMostLane(arcLaneSouth6);
        laneToRoad.put(arcLaneSouth6, left);
        arcLaneSouth6.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Seventh arc Lane exiting roundabout C2-E2
        Arc2D arcSouth7 = new Arc2D.Double();
        double arcSouth7ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcSouth7StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        arcSouth7.setArcByCenter(O2.getX(), O2.getY(), entranceExitRadius, arcSouth7StartAngle, -arcSouth7ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth7 = new ArcSegmentLane(arcSouth7, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth7.setId(laneRegistry.register(arcLaneSouth7));
        left.addTheUpMostLane(arcLaneSouth7);
        laneToRoad.put(arcLaneSouth7, left);
        arcLaneSouth7.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Eight arc Lane exiting on approach the roundabout E2-B2
        Arc2D arcSouth8 = new Arc2D.Double();
        double arcSouth8ExtentAngle = theta;
        double arcSouth8StartAngle = arcSouth8ExtentAngle;
        arcSouth8.setArcByCenter(O2.getX(), O2.getY(), entranceExitRadius, arcSouth8StartAngle, -arcSouth8ExtentAngle, 0);
        ArcSegmentLane arcLaneSouth8 = new ArcSegmentLane(arcSouth8, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth8.setId(laneRegistry.register(arcLaneSouth8));
        left.addTheUpMostLane(arcLaneSouth8);
        laneToRoad.put(arcLaneSouth8, left);
        arcLaneSouth8.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });

        // Seventh line Lane outside roundabout B2-A2
        LineSegmentLane lineLaneSouth9 = new LineSegmentLane(
                mapOrigin.getX() - laneWidth / 2,
                mapOrigin.getY() + b,
                mapOrigin.getX() - laneWidth / 2,
                height,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneSouth9.setId(laneRegistry.register(lineLaneSouth9));
        left.addTheUpMostLane(lineLaneSouth9);
        laneToRoad.put(lineLaneSouth9, left);
        laneDecompositionToRoad.put(lineLaneSouth9, left);

        // Set continuous lanes for all arc lanes
        arcLaneSouth2.setContinuousLanes();
        arcLaneSouth3.setContinuousLanes();
        arcLaneSouth4.setContinuousLanes();
        arcLaneSouth5.setContinuousLanes();
        arcLaneSouth6.setContinuousLanes();
        arcLaneSouth7.setContinuousLanes();
        arcLaneSouth8.setContinuousLanes();

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
        Road lower = new Road("1st Street E", this);

        // First line Lane going East A7-B7
        LineSegmentLane lineLaneEast1 = new LineSegmentLane(
                0,
                mapOrigin.getY() + laneWidth / 2,
                mapOrigin.getX() - b,
                mapOrigin.getY() + laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneEast1.setId(laneRegistry.register(lineLaneEast1));
        lower.addTheUpMostLane(lineLaneEast1);
        laneToRoad.put(lineLaneEast1, lower);
        laneDecompositionToRoad.put(lineLaneEast1, lower);

        // Second arc Lane approaching roundabout B7-E7
        Arc2D arcEast2 = new Arc2D.Double();
        double arcEast2ExtentAngle = theta;
        double arcEast2StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
        arcEast2.setArcByCenter(O7.getX(), O7.getY(), entranceExitRadius, arcEast2StartAngle, -arcEast2ExtentAngle, 0);
        ArcSegmentLane arcLaneEast2 = new ArcSegmentLane(arcEast2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast2.setId(laneRegistry.register(arcLaneEast2));
        lower.addTheUpMostLane(arcLaneEast2);
        laneToRoad.put(arcLaneEast2, lower);
        arcLaneEast2.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Third arc Lane entering roundabout E7-C7
        Arc2D arcEast3 = new Arc2D.Double();
        double arcEast3ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcEast3StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - theta;
        arcEast3.setArcByCenter(O7.getX(), O7.getY(), entranceExitRadius, arcEast3StartAngle, -arcEast3ExtentAngle, 0);
        ArcSegmentLane arcLaneEast3 = new ArcSegmentLane(arcEast3, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast3.setId(laneRegistry.register(arcLaneEast3));
        lower.addTheUpMostLane(arcLaneEast3);
        laneToRoad.put(arcLaneEast3, lower);
        arcLaneEast3.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Fourth arc Lane inside roundabout C7-C2
        Arc2D arcEast4 = new Arc2D.Double();
        double arcEast4ExtentAngle = beta;
        double arcEast4StartAngle = Math.toDegrees(GeomMath.PI) + alpha;
        arcEast4.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcEast4StartAngle, arcEast4ExtentAngle, 0);
        ArcSegmentLane arcLaneEast4 = new ArcSegmentLane(arcEast4, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast4.setId(laneRegistry.register(arcLaneEast4));
        lower.addTheUpMostLane(arcLaneEast4);
        laneToRoad.put(arcLaneEast4, lower);
        arcLaneEast4.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Fifth arc Lane inside roundabout C2-C1
        Arc2D arcEast5 = new Arc2D.Double();
        double arcEast5ExtentAngle = 2 * alpha;
        double arcEast5StartAngle = Math.toDegrees(GeomMath.TWO_PI) - beta - 3 * alpha;
        arcEast5.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcEast5StartAngle, arcEast5ExtentAngle, 0);
        ArcSegmentLane arcLaneEast5 = new ArcSegmentLane(arcEast5, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneEast5.setId(laneRegistry.register(arcLaneEast5));
        lower.addTheUpMostLane(arcLaneEast5);
        laneToRoad.put(arcLaneEast5, lower);
        arcLaneEast5.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Sixth arc Lane inside roundabout C1-C6
        Arc2D arcEast6 = new Arc2D.Double();
        double arcEast6ExtentAngle = beta;
        double arcEast6StartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha - beta;
        arcEast6.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcEast6StartAngle, arcEast6ExtentAngle, 0);
        ArcSegmentLane arcLaneEast6 = new ArcSegmentLane(arcEast6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast6.setId(laneRegistry.register(arcLaneEast6));
        lower.addTheUpMostLane(arcLaneEast6);
        laneToRoad.put(arcLaneEast6, lower);
        arcLaneEast6.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Seventh arc Lane exiting roundabout C6-E6
        Arc2D arcEast7 = new Arc2D.Double();
        double arcEast7ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcEast7StartAngle = Math.toDegrees(GeomMath.PI) - alpha;
        arcEast7.setArcByCenter(O6.getX(), O6.getY(), entranceExitRadius, arcEast7StartAngle, -arcEast7ExtentAngle, 0);
        ArcSegmentLane arcLaneEast7 = new ArcSegmentLane(arcEast7, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast7.setId(laneRegistry.register(arcLaneEast7));
        lower.addTheUpMostLane(arcLaneEast7);
        laneToRoad.put(arcLaneEast7, lower);
        arcLaneEast7.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Eight arc Lane exiting on approach the roundabout E6-B6
        Arc2D arcEast8 = new Arc2D.Double();
        double arcEast8ExtentAngle = theta;
        double arcEast8StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + theta;
        arcEast8.setArcByCenter(O6.getX(), O6.getY(), entranceExitRadius, arcEast8StartAngle, -arcEast8ExtentAngle, 0);
        ArcSegmentLane arcLaneEast8 = new ArcSegmentLane(arcEast8, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast8.setId(laneRegistry.register(arcLaneEast8));
        lower.addTheUpMostLane(arcLaneEast8);
        laneToRoad.put(arcLaneEast8, lower);
        arcLaneEast8.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });

        // Ninth line Lane exiting roundabout B6-A6
        LineSegmentLane lineLaneEast9 = new LineSegmentLane(
                mapOrigin.getX() + b,
                mapOrigin.getY() + laneWidth / 2,
                width,
                mapOrigin.getY() + laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneEast9.setId(laneRegistry.register(lineLaneEast9));
        lower.addTheUpMostLane(lineLaneEast9);
        laneToRoad.put(lineLaneEast9, lower);
        laneDecompositionToRoad.put(lineLaneEast9, lower);

        // Set continuous lanes for all arc lanes
        arcLaneEast2.setContinuousLanes();
        arcLaneEast3.setContinuousLanes();
        arcLaneEast4.setContinuousLanes();
        arcLaneEast5.setContinuousLanes();
        arcLaneEast6.setContinuousLanes();
        arcLaneEast7.setContinuousLanes();
        arcLaneEast8.setContinuousLanes();

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
        Road upper = new Road("1st Street W", this);

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
        laneDecompositionToRoad.put(lineLaneWest1, upper);

        // Second arc Lane entering roundabout B5-E5
        Arc2D arcWest2 = new Arc2D.Double();
        double arcWest2ExtentAngle = theta;
        double arcWest2StartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
        arcWest2.setArcByCenter(O5.getX(), O5.getY(), entranceExitRadius, arcWest2StartAngle, -arcWest2ExtentAngle, 0);
        ArcSegmentLane arcLaneWest2 = new ArcSegmentLane(arcWest2, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest2.setId(laneRegistry.register(arcLaneWest2));
        upper.addTheUpMostLane(arcLaneWest2);
        laneToRoad.put(arcLaneWest2, upper);
        arcLaneWest2.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Third arc Lane entering roundabout E5-C5
        Arc2D arcWest3 = new Arc2D.Double();
        double arcWest3ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcWest3StartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - theta;
        arcWest3.setArcByCenter(O5.getX(), O5.getY(), entranceExitRadius, arcWest3StartAngle, -arcWest3ExtentAngle, 0);
        ArcSegmentLane arcLaneWest3 = new ArcSegmentLane(arcWest3, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest3.setId(laneRegistry.register(arcLaneWest3));
        upper.addTheUpMostLane(arcLaneWest3);
        laneToRoad.put(arcLaneWest3, upper);
        arcLaneWest3.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Fourth arc Lane inside roundabout C5-C4
        Arc2D arcWest4 = new Arc2D.Double();
        double arcWest4ExtentAngle = beta;
        double arcWest4StartAngle = alpha;
        arcWest4.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcWest4StartAngle, arcWest4ExtentAngle, 0);
        ArcSegmentLane arcLaneWest4 = new ArcSegmentLane(arcWest4, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest4.setId(laneRegistry.register(arcLaneWest4));
        upper.addTheUpMostLane(arcLaneWest4);
        laneToRoad.put(arcLaneWest4, upper);
        arcLaneWest4.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Fifth arc Lane inside roundabout C4-C3
        Arc2D arcWest5 = new Arc2D.Double();
        double arcWest5ExtentAngle = 2 * alpha;
        double arcWest5StartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        arcWest5.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcWest5StartAngle, arcWest5ExtentAngle, 0);
        ArcSegmentLane arcLaneWest5 = new ArcSegmentLane(arcWest5, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneWest5.setId(laneRegistry.register(arcLaneWest5));
        upper.addTheUpMostLane(arcLaneWest5);
        laneToRoad.put(arcLaneWest5, upper);
        arcLaneWest5.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Sixth arc Lane inside roundabout C3-C8
        Arc2D arcWest6 = new Arc2D.Double();
        double arcWest6ExtentAngle = beta;
        double arcWest6StartAngle = Math.toDegrees(GeomMath.PI) - alpha - beta;
        arcWest6.setArcByCenter(O.getX(), O.getY(), roundaboutRadius, arcWest6StartAngle, arcWest6ExtentAngle, 0);
        ArcSegmentLane arcLaneWest6 = new ArcSegmentLane(arcWest6, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest6.setId(laneRegistry.register(arcLaneWest6));
        upper.addTheUpMostLane(arcLaneWest6);
        laneToRoad.put(arcLaneWest6, upper);
        arcLaneWest6.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Seventh arc Lane exiting roundabout C8-E8
        Arc2D arcWest7 = new Arc2D.Double();
        double arcWest7ExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcWest7StartAngle = 3 * Math.toDegrees(GeomMath.TWO_PI) - alpha;
        arcWest7.setArcByCenter(O8.getX(), O8.getY(), entranceExitRadius, arcWest7StartAngle, -arcWest7ExtentAngle, 0);
        ArcSegmentLane arcLaneWest7 = new ArcSegmentLane(arcWest7, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest7.setId(laneRegistry.register(arcLaneWest7));
        upper.addTheUpMostLane(arcLaneWest7);
        laneToRoad.put(arcLaneWest7, upper);
        arcLaneWest7.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Eight arc Lane exiting roundabout E8-B8
        Arc2D arcWest8 = new Arc2D.Double();
        double arcWest8ExtentAngle = theta;
        double arcWest8StartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + arcWest8ExtentAngle;
        arcWest8.setArcByCenter(O8.getX(), O8.getY(), entranceExitRadius, arcWest8StartAngle, -arcWest8ExtentAngle, 0);
        ArcSegmentLane arcLaneWest8 = new ArcSegmentLane(arcWest8, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest8.setId(laneRegistry.register(arcLaneWest8));
        upper.addTheUpMostLane(arcLaneWest8);
        laneToRoad.put(arcLaneWest8, upper);
        arcLaneWest8.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });

        // Ninth line Lane outside roundabout B8-A8
        LineSegmentLane lineLaneWest9 = new LineSegmentLane(
                mapOrigin.getX() - b,
                mapOrigin.getY() - laneWidth / 2,
                0,
                mapOrigin.getY() - laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneWest9.setId(laneRegistry.register(lineLaneWest9));
        upper.addTheUpMostLane(lineLaneWest9);
        laneToRoad.put(lineLaneWest9, upper);
        laneDecompositionToRoad.put(lineLaneWest9, upper);

        // Set continuous lanes for all arc lanes
        arcLaneWest2.setContinuousLanes();
        arcLaneWest3.setContinuousLanes();
        arcLaneWest4.setContinuousLanes();
        arcLaneWest5.setContinuousLanes();
        arcLaneWest6.setContinuousLanes();
        arcLaneWest7.setContinuousLanes();
        arcLaneWest8.setContinuousLanes();

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

        laneDecompositionToRoad.putAll(laneToRoad);
        roads = new ArrayList<Road>(horizontalRoads);
        roads.addAll(verticalRoads);
        roads = Collections.unmodifiableList(roads);

        // We should have columns * rows intersections, so make space for 'em
        intersectionManagers = new ArrayList<IntersectionManager>(columns * rows);
        intersectionManagerGrid = new IntersectionManager[columns][rows];

        initializeSpawnPoints(initTime);
        RimConnection rimConnection =
                new RimConnection(getRoads());

        addRimConnection(rimConnection);
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
     * @param initTime the initial time
     */
    private void initializeSpawnPoints(double initTime) {
        spawnPoints = new ArrayList<AIMSpawnPoint>(columns + rows);
        horizontalSpawnPoints = new ArrayList<AIMSpawnPoint>(rows);
        verticalSpawnPoints = new ArrayList<AIMSpawnPoint>(columns);

        // Make spawn points only on the first line lane of each road
        for (Road road : horizontalRoads) {
            horizontalSpawnPoints.add(makeSpawnPoint(initTime, road.getContinuousLanes().get(0)));
        }

        for (Road road : verticalRoads) {
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
     * @param initTime the initial time
     */
    private void initializeOneSpawnPoint(double initTime) {
        if (rows > 1 || columns > 1) {
            throw new IllegalArgumentException("Undefined behaviour with one spawn point");
        }
        spawnPoints = new ArrayList<AIMSpawnPoint>(1);
        horizontalSpawnPoints = new ArrayList<AIMSpawnPoint>(1);

        Lane lane = horizontalRoads.get(0).getContinuousLanes().get(0);
        horizontalSpawnPoints.add(makeSpawnPoint(initTime, lane));

        spawnPoints.addAll(horizontalSpawnPoints);

        Debug.currentMap = this;
    }

    /**
     * Make spawn points.
     *
     * @param initTime the initial time
     * @param lane     the lane
     * @return the spawn point
     */
    private AIMSpawnPoint makeSpawnPoint(double initTime, Lane lane) {
        double startDistance = 0.0;
        double normalizedStartDistance = lane.normalizedDistance(startDistance);
        Point2D pos = lane.getPointAtNormalizedDistance(normalizedStartDistance);
        double heading = lane.getInitialHeading();
        double steeringAngle = 0.0;
        double acceleration = 0.0;
        double d = lane.normalizedDistance(startDistance + NO_VEHICLE_ZONE_LENGTH);
        Rectangle2D noVehicleZone =
                lane.getShape(normalizedStartDistance, d).getBounds2D();

        return new AIMSpawnPoint(initTime, pos, heading, steeringAngle, acceleration,
                lane, noVehicleZone);
    }

    protected void addRimConnection(RimConnection connection) { rimConnections.add(connection); }

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
        if (memoMaximumSpeedLimit < 0) {
            for (Road r : getRoads()) {
                for (Lane l : r.getLanes()) {
                    if (l.getSpeedLimit() > memoMaximumSpeedLimit) {
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
        if (memoMaximumLaneSpeedLimit < 0) {
            memoMaximumLaneSpeedLimit = getRoads().get(0).getContinuousLanes().get(0).getSpeedLimit();
        }
        return memoMaximumLaneSpeedLimit;
    }

    /**
     * Get the maximum speed limit in the roundabout.
     */
    public double getMaximumRoundaboutSpeedLimit() {
        if (memoMaximumRoundaboutSpeedLimit < 0) {
            memoMaximumRoundaboutSpeedLimit = getRoads().get(0).getEntryApproachLane().getSpeedLimit();
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
    public List<AIMSpawnPoint> getSpawnPoints() { return spawnPoints; }

    /**
     * Get the list of horizontal spawn points.
     *
     * @return the list of horizontal spawn points
     */
    public List<AIMSpawnPoint> getHorizontalSpawnPoints() {
        return horizontalSpawnPoints;
    }


    /**
     * Get the list of vertical spawn points.
     *
     * @return the list of vertical spawn points
     */
    public List<AIMSpawnPoint> getVerticalSpawnPoints() {
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
     * @param column the column of the intersection
     * @param row    the row of the intersection
     * @return the list of roads that enter the intersection at (column, row)
     */
    public List<Road> getRoads(int column, int row) {
        // First some error checking
        if (row >= rows || column >= columns || row < 0 || column < 0) {
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
     * Given any lane in the intersection, get the road it belongs.
     * Applies to all lanes (including line lanes from arc lanes).
     */
    public Road getRoadByDecompositionLane(Lane lane) {
        return laneDecompositionToRoad.get(lane);
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
     * @param column the column of the intersection
     * @param row    the row of the intersection
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
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
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
                for (double time : line.getTimes(vin)) {
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
