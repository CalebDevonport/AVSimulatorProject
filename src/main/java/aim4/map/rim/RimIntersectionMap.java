package aim4.map.rim;

import aim4.config.Debug;
import aim4.im.rim.IntersectionManager;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
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
 * The grid layout map for rim. The design can be found here:
 * https://drive.google.com/file/d/1C5xu8IF-myXM257b444pTfig6p0pOyPS/view?usp=sharing
 * (open with GeoGebra Geometry tool)
 */
public class RimIntersectionMap implements BasicRIMIntersectionMap {
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
     * A mapping from lanes to roads they belong
     */
    private Map<Lane, Road> laneToRoad = new HashMap<Lane, Road>();
    /**
     * A mapping from all lanes (including line lanes decomposition) to roads they belong
     */
    private Map<Lane, Road> laneDecompositionToRoad = new HashMap<Lane, Road>();

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

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
        lanesPerRoad = 2; // only one lane per road
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
//        double roundaboutRadius = (roundaboutDiameter - roundaboutWidth) / 2;
        double roundaboutRadius = (roundaboutDiameter / 2) - roundaboutWidth;

		  // Calculate a,b,c,d parameters to construct the roundabout 
        //double a = entranceExitRadius + laneWidth / 2; 
        //double b = Math.sqrt(Math.pow(roundaboutRadius + entranceExitRadius, 2) - Math.pow(a, 2)); 
        //double c = (roundaboutRadius / (roundaboutRadius + entranceExitRadius)) * a; 
        //double d = (roundaboutRadius / (roundaboutRadius + entranceExitRadius)) * b;
        //double e = b - d - laneWidth - 0.5;  // 0.5 buffer needed as entry points would result in front of vehicle getting inside the intersection
        //double alpha = Math.toDegrees(Math.asin(a / (entranceExitRadius + roundaboutRadius)));
        //double beta = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - 2 * alpha;
        //double theta = Math.toDegrees(Math.asin(e / (a)));
		 
     // Calculate a,b,c,d parameters to construct the roundabout
        double a = entranceExitRadius;
        double b = Math.sqrt(Math.pow(roundaboutRadius + entranceExitRadius, 2) - Math.pow(a, 2));
        double c = (roundaboutRadius / (roundaboutRadius + entranceExitRadius)) * a;
        double d = (roundaboutRadius / (roundaboutRadius + entranceExitRadius)) * b;
        double e = b - d - laneWidth - 0.5; // 0.5 buffer needed as entry points would result in front of vehicle getting inside the intersection
        double alpha = Math.toDegrees(Math.asin(a / (entranceExitRadius + roundaboutRadius)));
        double beta = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - 2 * alpha;
        double theta = Math.toDegrees(Math.asin(e / (a)));

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
        
        double innerLaneEntryRadius = entranceExitRadius - (roundaboutWidth / 2);
        double innerLaneRadius = roundaboutRadius + (roundaboutWidth / 2);
        double outerLaneEntryRadius = entranceExitRadius - (3 * roundaboutWidth / 2);
        double outerLaneRadius = roundaboutRadius + (3 * roundaboutWidth / 2);

        
        // ------------------------------------------------------------------------------------------------------------
        // Create the north vertical road inner lane
        Road right = new Road("1st Avenue N", this);
        right.setUpContinuousLanes(lanesPerRoad);

        // First line Lane going North IA1-IB1
        LineSegmentLane lineLaneNorth1Inner = new LineSegmentLane(
                mapOrigin.getX() + laneWidth / 2,
                height,
                mapOrigin.getX() + laneWidth / 2,
                mapOrigin.getY() + b,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneNorth1Inner.setId(laneRegistry.register(lineLaneNorth1Inner));
        right.addTheUpMostLane(0, lineLaneNorth1Inner);
        laneToRoad.put(lineLaneNorth1Inner, right);
        laneDecompositionToRoad.put(lineLaneNorth1Inner, right);

        // Second arc Lane approaching the roundabout IB1-IE1
        Arc2D arcNorth2Inner = new Arc2D.Double();
        double arcNorth2InnerExtentAngle = theta;
        double arcNorth2InnerStartAngle = Math.toDegrees(GeomMath.PI);
        arcNorth2Inner.setArcByCenter(O1.getX(), O1.getY(), innerLaneEntryRadius, arcNorth2InnerStartAngle, -arcNorth2InnerExtentAngle, 0);
        ArcSegmentLane arcLaneNorth2Inner = new ArcSegmentLane(arcNorth2Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth2Inner.setId(laneRegistry.register(arcLaneNorth2Inner));
        right.addTheUpMostLane(0, arcLaneNorth2Inner);
        laneToRoad.put(arcLaneNorth2Inner, right);
        arcLaneNorth2Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Third arc Lane entering roundabout IE1-IC1
        Arc2D arcNorth3Inner = new Arc2D.Double();
        double arcNorth3InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcNorth3InnerStartAngle = Math.toDegrees(GeomMath.PI) - theta;
        arcNorth3Inner.setArcByCenter(O1.getX(), O1.getY(), innerLaneEntryRadius, arcNorth3InnerStartAngle, -arcNorth3InnerExtentAngle, 0);
        ArcSegmentLane arcLaneNorth3Inner = new ArcSegmentLane(arcNorth3Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth3Inner.setId(laneRegistry.register(arcLaneNorth3Inner));
        right.addTheUpMostLane(0, arcLaneNorth3Inner);
        laneToRoad.put(arcLaneNorth3Inner, right);
        arcLaneNorth3Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Fourth arc Lane inside roundabout IC1-IC6
        Arc2D arcNorth4Inner = new Arc2D.Double();
        double arcNorth4InnerExtentAngle = beta;
        double arcNorth4InnerStartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha - beta;
        arcNorth4Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcNorth4InnerStartAngle, arcNorth4InnerExtentAngle, 0);
        ArcSegmentLane arcLaneNorth4Inner = new ArcSegmentLane(arcNorth4Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth4Inner.setId(laneRegistry.register(arcLaneNorth4Inner));
        right.addTheUpMostLane(0, arcLaneNorth4Inner);
        laneToRoad.put(arcLaneNorth4Inner, right);
        arcLaneNorth4Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Fifth arc Lane inside roundabout IC6-IC5
        Arc2D arcNorth5Inner = new Arc2D.Double();
        double arcNorth5InnerExtentAngle = 2 * alpha;
        double arcNorth5InnerStartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha;
        arcNorth5Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcNorth5InnerStartAngle, arcNorth5InnerExtentAngle, 0);
        ArcSegmentLane arcLaneNorth5Inner = new ArcSegmentLane(arcNorth5Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneNorth5Inner.setId(laneRegistry.register(arcLaneNorth5Inner));
        right.addTheUpMostLane(0, arcLaneNorth5Inner);
        laneToRoad.put(arcLaneNorth5Inner, right);
        arcLaneNorth5Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Sixth arc Lane inside roundabout IC5-IC4
        Arc2D arcNorth6Inner = new Arc2D.Double();
        double arcNorth6InnerExtentAngle = beta;
        double arcNorth6InnerStartAngle = alpha;
        arcNorth6Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcNorth6InnerStartAngle, arcNorth6InnerExtentAngle, 0);
        ArcSegmentLane arcLaneNorth6Inner = new ArcSegmentLane(arcNorth6Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth6Inner.setId(laneRegistry.register(arcLaneNorth6Inner));
        right.addTheUpMostLane(0, arcLaneNorth6Inner);
        laneToRoad.put(arcLaneNorth6Inner, right);
        arcLaneNorth6Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Seventh arc Lane exiting roundabout IC4-IE4
        Arc2D arcNorth7Inner = new Arc2D.Double();
        double arcNorth7InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcNorth7InnerStartAngle = Math.toDegrees(GeomMath.PI) + Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        arcNorth7Inner.setArcByCenter(O4.getX(), O4.getY(), innerLaneEntryRadius, arcNorth7InnerStartAngle, - arcNorth7InnerExtentAngle, 0);
        ArcSegmentLane arcLaneNorth7Inner = new ArcSegmentLane(arcNorth7Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth7Inner.setId(laneRegistry.register(arcLaneNorth7Inner));
        right.addTheUpMostLane(0, arcLaneNorth7Inner);
        laneToRoad.put(arcLaneNorth7Inner, right);
        arcLaneNorth7Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Eight arc Lane exiting on approach the roundabout IE4-IB4
        Arc2D arcNorth8Inner = new Arc2D.Double();
        double arcNorth8ExtentAngleInner = theta;
        double arcNorth8StartAngleInner = Math.toDegrees(Math.PI) + arcNorth8ExtentAngleInner;
        arcNorth8Inner.setArcByCenter(O4.getX(), O4.getY(), innerLaneEntryRadius, arcNorth8StartAngleInner, - arcNorth8ExtentAngleInner, 0);
        ArcSegmentLane arcLaneNorth8Inner = new ArcSegmentLane(arcNorth8Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneNorth8Inner.setId(laneRegistry.register(arcLaneNorth8Inner));
        right.addTheUpMostLane(0, arcLaneNorth8Inner);
        laneToRoad.put(arcLaneNorth8Inner, right);
        arcLaneNorth8Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, right);
        });

        // Ninth line Lane exiting roundabout IB4-IA4
        LineSegmentLane lineLaneNorth9Inner = new LineSegmentLane(
                mapOrigin.getX() + laneWidth / 2,
                mapOrigin.getY() - b,
                mapOrigin.getX() + laneWidth / 2,
                0,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneNorth9Inner.setId(laneRegistry.register(lineLaneNorth9Inner));
        right.addTheUpMostLane(0, lineLaneNorth9Inner);
        laneToRoad.put(lineLaneNorth9Inner, right);
        laneDecompositionToRoad.put(lineLaneNorth9Inner, right);

        // Set continuous lanes for all arc lanes
        arcLaneNorth2Inner.setContinuousLanes();
        arcLaneNorth3Inner.setContinuousLanes();
        arcLaneNorth4Inner.setContinuousLanes();
        arcLaneNorth5Inner.setContinuousLanes();
        arcLaneNorth6Inner.setContinuousLanes();
        arcLaneNorth7Inner.setContinuousLanes();
        arcLaneNorth8Inner.setContinuousLanes();
        
        
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "NorthBound Inner" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                height - DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() + laneWidth,
                                height - DATA_COLLECTION_LINE_POSITION),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "NorthBound Inner" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() + laneWidth,
                                DATA_COLLECTION_LINE_POSITION),
                        true));
        
        
     // ------------------------------------------------------------------------------------------------------------
        // Create the north vertical road outer lane
        if (lanesPerRoad == 2) {
	        // First line Lane going North OA1-OB1
	        LineSegmentLane lineLaneNorth1Outer = new LineSegmentLane(
	                mapOrigin.getX() + (3 * laneWidth / 2),
	                height,
	                mapOrigin.getX() + (3 * laneWidth / 2),
	                mapOrigin.getY() + b,
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneNorth1Outer.setId(laneRegistry.register(lineLaneNorth1Outer));
	        right.addTheUpMostLane(1, lineLaneNorth1Outer);
	        laneToRoad.put(lineLaneNorth1Outer, right);
	        laneDecompositionToRoad.put(lineLaneNorth1Outer, right);
	
	        // Second arc Lane approaching the roundabout OB1-OE1
	        Arc2D arcNorth2Outer = new Arc2D.Double();
	        double arcNorth2OuterExtentAngle = theta;
	        double arcNorth2OuterStartAngle = Math.toDegrees(GeomMath.PI);
	        arcNorth2Outer.setArcByCenter(O1.getX(), O1.getY(), outerLaneEntryRadius, arcNorth2OuterStartAngle, -arcNorth2OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneNorth2Outer = new ArcSegmentLane(arcNorth2Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneNorth2Outer.setId(laneRegistry.register(arcLaneNorth2Outer));
	        right.addTheUpMostLane(1, arcLaneNorth2Outer);
	        laneToRoad.put(arcLaneNorth2Outer, right);
	        arcLaneNorth2Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Third arc Lane entering roundabout OE1-OC1
	        Arc2D arcNorth3Outer = new Arc2D.Double();
	        double arcNorth3OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcNorth3OuterStartAngle = Math.toDegrees(GeomMath.PI) - theta;
	        arcNorth3Outer.setArcByCenter(O1.getX(), O1.getY(), outerLaneEntryRadius, arcNorth3OuterStartAngle, -arcNorth3OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneNorth3Outer = new ArcSegmentLane(arcNorth3Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneNorth3Outer.setId(laneRegistry.register(arcLaneNorth3Outer));
	        right.addTheUpMostLane(1, arcLaneNorth3Outer);
	        laneToRoad.put(arcLaneNorth3Outer, right);
	        arcLaneNorth3Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Fourth arc Lane inside roundabout OC1-OC6
	        Arc2D arcNorth4Outer = new Arc2D.Double();
	        double arcNorth4OuterExtentAngle = beta;
	        double arcNorth4OuterStartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha - beta;
	        arcNorth4Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcNorth4OuterStartAngle, arcNorth4OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneNorth4Outer = new ArcSegmentLane(arcNorth4Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneNorth4Outer.setId(laneRegistry.register(arcLaneNorth4Outer));
	        right.addTheUpMostLane(1, arcLaneNorth4Outer);
	        laneToRoad.put(arcLaneNorth4Outer, right);
	        arcLaneNorth4Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Fifth arc Lane inside roundabout OC6-OC5
	        Arc2D arcNorth5Outer = new Arc2D.Double();
	        double arcNorth5OuterExtentAngle = 2 * alpha;
	        double arcNorth5OuterStartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha;
	        arcNorth5Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcNorth5OuterStartAngle, arcNorth5OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneNorth5Outer = new ArcSegmentLane(arcNorth5Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
	        arcLaneNorth5Outer.setId(laneRegistry.register(arcLaneNorth5Outer));
	        right.addTheUpMostLane(1, arcLaneNorth5Outer);
	        laneToRoad.put(arcLaneNorth5Outer, right);
	        arcLaneNorth5Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Sixth arc Lane inside roundabout OC5-OC4
	        Arc2D arcNorth6Outer = new Arc2D.Double();
	        double arcNorth6OuterExtentAngle = beta;
	        double arcNorth6OuterStartAngle = alpha;
	        arcNorth6Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcNorth6OuterStartAngle, arcNorth6OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneNorth6Outer = new ArcSegmentLane(arcNorth6Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneNorth6Outer.setId(laneRegistry.register(arcLaneNorth6Outer));
	        right.addTheUpMostLane(1, arcLaneNorth6Outer);
	        laneToRoad.put(arcLaneNorth6Outer, right);
	        arcLaneNorth6Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Seventh arc Lane exiting roundabout OC4-OE4
	        Arc2D arcNorth7Outer = new Arc2D.Double();
	        double arcNorth7OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcNorth7OuterStartAngle = Math.toDegrees(GeomMath.PI) + Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
	        arcNorth7Outer.setArcByCenter(O4.getX(), O4.getY(), outerLaneEntryRadius, arcNorth7OuterStartAngle, - arcNorth7OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneNorth7Outer = new ArcSegmentLane(arcNorth7Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneNorth7Outer.setId(laneRegistry.register(arcLaneNorth7Outer));
	        right.addTheUpMostLane(1, arcLaneNorth7Outer);
	        laneToRoad.put(arcLaneNorth7Outer, right);
	        arcLaneNorth7Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Eight arc Lane exiting on approach the roundabout OE4-OB4
	        Arc2D arcNorth8Outer = new Arc2D.Double();
	        double arcNorth8ExtentAngleOuter = theta;
	        double arcNorth8StartAngleOuter = Math.toDegrees(Math.PI) + arcNorth8ExtentAngleOuter;
	        arcNorth8Outer.setArcByCenter(O4.getX(), O4.getY(), outerLaneEntryRadius, arcNorth8StartAngleOuter, - arcNorth8ExtentAngleOuter, 0);
	        ArcSegmentLane arcLaneNorth8Outer = new ArcSegmentLane(arcNorth8Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneNorth8Outer.setId(laneRegistry.register(arcLaneNorth8Outer));
	        right.addTheUpMostLane(1, arcLaneNorth8Outer);
	        laneToRoad.put(arcLaneNorth8Outer, right);
	        arcLaneNorth8Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, right);
	        });
	
	        // Ninth line Lane exiting roundabout OB4-OA4
	        LineSegmentLane lineLaneNorth9Outer = new LineSegmentLane(
	                mapOrigin.getX() + (3 * laneWidth / 2),
	                mapOrigin.getY() - b,
	                mapOrigin.getX() + (3 * laneWidth / 2),
	                0,
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneNorth9Outer.setId(laneRegistry.register(lineLaneNorth9Outer));
	        right.addTheUpMostLane(1, lineLaneNorth9Outer);
	        laneToRoad.put(lineLaneNorth9Outer, right);
	        laneDecompositionToRoad.put(lineLaneNorth9Outer, right);
	
	        // Set continuous lanes for all arc lanes
	        arcLaneNorth2Outer.setContinuousLanes();
	        arcLaneNorth3Outer.setContinuousLanes();
	        arcLaneNorth4Outer.setContinuousLanes();
	        arcLaneNorth5Outer.setContinuousLanes();
	        arcLaneNorth6Outer.setContinuousLanes();
	        arcLaneNorth7Outer.setContinuousLanes();
	        arcLaneNorth8Outer.setContinuousLanes();
	
	        // generate the data collection lines
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "NorthBound Outer" + "Entrance",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(mapOrigin.getX() + laneWidth,
	                                height - DATA_COLLECTION_LINE_POSITION),
	                        new Point2D.Double(mapOrigin.getX() + (2 * laneWidth),
	                                height - DATA_COLLECTION_LINE_POSITION),
	                        true));
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "NorthBound Outer" + "Exit",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(mapOrigin.getX() + laneWidth,
	                                DATA_COLLECTION_LINE_POSITION),
	                        new Point2D.Double(mapOrigin.getX() + (2 * laneWidth),
	                                DATA_COLLECTION_LINE_POSITION),
	                        true));
        }
        
        verticalRoads.add(right);
        
        // ------------------------------------------------------------------------------------------------------------
        // Create the south vertical road inner lane
        Road left = new Road("1st Avenue S", this);
        left.setUpContinuousLanes(lanesPerRoad);
        // First line Lane going South IA3-IB3
        LineSegmentLane lineLaneSouth1Inner = new LineSegmentLane(
                mapOrigin.getX() - laneWidth / 2,
                0,
                mapOrigin.getX() - laneWidth / 2,
                mapOrigin.getY() - b,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneSouth1Inner.setId(laneRegistry.register(lineLaneSouth1Inner));
        left.addTheUpMostLane(0, lineLaneSouth1Inner);
        laneToRoad.put(lineLaneSouth1Inner, left);
        laneDecompositionToRoad.put(lineLaneSouth1Inner, left);
        
        // Second arc Lane approaching roundabout IB3-IE3
        Arc2D arcSouth2Inner = new Arc2D.Double();
        double arcSouth2InnerExtentAngle = theta;
        double arcSouth2InnerStartAngle = Math.toDegrees(GeomMath.TWO_PI);
        arcSouth2Inner.setArcByCenter(O3.getX(), O3.getY(), innerLaneEntryRadius, arcSouth2InnerStartAngle, -arcSouth2InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth2Inner = new ArcSegmentLane(arcSouth2Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth2Inner.setId(laneRegistry.register(arcLaneSouth2Inner));
        left.addTheUpMostLane(0, arcLaneSouth2Inner);
        laneToRoad.put(arcLaneSouth2Inner, left);
        arcLaneSouth2Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Third arc Lane entering roundabout IE3-IC3
        Arc2D arcSouth3Inner = new Arc2D.Double();
        double arcSouth3InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcSouth3InnerStartAngle = Math.toDegrees(GeomMath.TWO_PI) - theta;
        arcSouth3Inner.setArcByCenter(O3.getX(), O3.getY(), innerLaneEntryRadius, arcSouth3InnerStartAngle, -arcSouth3InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth3Inner = new ArcSegmentLane(arcSouth3Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth3Inner.setId(laneRegistry.register(arcLaneSouth3Inner));
        left.addTheUpMostLane(0, arcLaneSouth3Inner);
        laneToRoad.put(arcLaneSouth3Inner, left);
        arcLaneSouth3Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Fourth arc Lane inside roundabout IC3-IC8
        Arc2D arcSouth4Inner = new Arc2D.Double();
        double arcSouth4InnerExtentAngle = beta;
        double arcSouth4InnerStartAngle = Math.toDegrees(GeomMath.PI) - alpha - beta;
        arcSouth4Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcSouth4InnerStartAngle, arcSouth4InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth4Inner = new ArcSegmentLane(arcSouth4Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth4Inner.setId(laneRegistry.register(arcLaneSouth4Inner));
        left.addTheUpMostLane(0, arcLaneSouth4Inner);
        laneToRoad.put(arcLaneSouth4Inner, left);
        arcLaneSouth4Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Fifth arc Lane inside roundabout IC8-IC7
        Arc2D arcSouth5Inner = new Arc2D.Double();
        double arcSouth5InnerExtentAngle = 2 * alpha;
        double arcSouth5InnerStartAngle = Math.toDegrees(GeomMath.PI) - alpha;
        arcSouth5Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcSouth5InnerStartAngle, arcSouth5InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth5Inner = new ArcSegmentLane(arcSouth5Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneSouth5Inner.setId(laneRegistry.register(arcLaneSouth5Inner));
        left.addTheUpMostLane(0, arcLaneSouth5Inner);
        laneToRoad.put(arcLaneSouth5Inner, left);
        arcLaneSouth5Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Sixth arc Lane inside roundabout IC7-IC2
        Arc2D arcSouth6Inner = new Arc2D.Double();
        double arcSouth6InnerExtentAngle = beta;
        double arcSouth6InnerStartAngle = Math.toDegrees(GeomMath.PI) + alpha;
        arcSouth6Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcSouth6InnerStartAngle, arcSouth6InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth6Inner = new ArcSegmentLane(arcSouth6Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth6Inner.setId(laneRegistry.register(arcLaneSouth6Inner));
        left.addTheUpMostLane(0, arcLaneSouth6Inner);
        laneToRoad.put(arcLaneSouth6Inner, left);
        arcLaneSouth6Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Seventh arc Lane exiting roundabout IC2-IE2
        Arc2D arcSouth7Inner = new Arc2D.Double();
        double arcSouth7InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcSouth7InnerStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        arcSouth7Inner.setArcByCenter(O2.getX(), O2.getY(), innerLaneEntryRadius, arcSouth7InnerStartAngle, -arcSouth7InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth7Inner = new ArcSegmentLane(arcSouth7Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth7Inner.setId(laneRegistry.register(arcLaneSouth7Inner));
        left.addTheUpMostLane(0, arcLaneSouth7Inner);
        laneToRoad.put(arcLaneSouth7Inner, left);
        arcLaneSouth7Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Eight arc Lane exiting on approach the roundabout IE2-IB2
        Arc2D arcSouth8Inner = new Arc2D.Double();
        double arcSouth8InnerExtentAngle = theta;
        double arcSouth8InnerStartAngle = arcSouth8InnerExtentAngle;
        arcSouth8Inner.setArcByCenter(O2.getX(), O2.getY(), innerLaneEntryRadius, arcSouth8InnerStartAngle, -arcSouth8InnerExtentAngle, 0);
        ArcSegmentLane arcLaneSouth8Inner = new ArcSegmentLane(arcSouth8Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneSouth8Inner.setId(laneRegistry.register(arcLaneSouth8Inner));
        left.addTheUpMostLane(0, arcLaneSouth8Inner);
        laneToRoad.put(arcLaneSouth8Inner, left);
        arcLaneSouth8Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, left);
        });
        
        // Seventh line Lane outside roundabout IB2-IA2
        LineSegmentLane lineLaneSouth9Inner = new LineSegmentLane(
                mapOrigin.getX() - laneWidth / 2,
                mapOrigin.getY() + b,
                mapOrigin.getX() - laneWidth / 2,
                height,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneSouth9Inner.setId(laneRegistry.register(lineLaneSouth9Inner));
        left.addTheUpMostLane(0, lineLaneSouth9Inner);
        laneToRoad.put(lineLaneSouth9Inner, left);
        laneDecompositionToRoad.put(lineLaneSouth9Inner, left);
        
        // Set continuous lanes for all arc lanes
        arcLaneSouth2Inner.setContinuousLanes();
        arcLaneSouth3Inner.setContinuousLanes();
        arcLaneSouth4Inner.setContinuousLanes();
        arcLaneSouth5Inner.setContinuousLanes();
        arcLaneSouth6Inner.setContinuousLanes();
        arcLaneSouth7Inner.setContinuousLanes();
        arcLaneSouth8Inner.setContinuousLanes();
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "SouthBound Inner" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() - laneWidth,
                                DATA_COLLECTION_LINE_POSITION),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "SouthBound Inner" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(mapOrigin.getX(),
                                height - DATA_COLLECTION_LINE_POSITION),
                        new Point2D.Double(mapOrigin.getX() - laneWidth,
                                height - DATA_COLLECTION_LINE_POSITION),
                        true));
        
        
        // Create the south vertical road outer lane
        if (lanesPerRoad == 2) {
	        // First line Lane going South OA3-OB3
	        LineSegmentLane lineLaneSouth1Outer = new LineSegmentLane(
	                mapOrigin.getX() - (3 * laneWidth / 2),
	                0,
	                mapOrigin.getX() - (3 * laneWidth / 2),
	                mapOrigin.getY() - b,
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneSouth1Outer.setId(laneRegistry.register(lineLaneSouth1Outer));
	        left.addTheUpMostLane(1, lineLaneSouth1Outer);
	        laneToRoad.put(lineLaneSouth1Outer, left);
	        laneDecompositionToRoad.put(lineLaneSouth1Outer, left);
	        
	        // Second arc Lane approaching roundabout OB3-OE3
	        Arc2D arcSouth2Outer = new Arc2D.Double();
	        double arcSouth2OuterExtentAngle = theta;
	        double arcSouth2OuterStartAngle = Math.toDegrees(GeomMath.TWO_PI);
	        arcSouth2Outer.setArcByCenter(O3.getX(), O3.getY(), outerLaneEntryRadius, arcSouth2OuterStartAngle, -arcSouth2OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth2Outer = new ArcSegmentLane(arcSouth2Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneSouth2Outer.setId(laneRegistry.register(arcLaneSouth2Outer));
	        left.addTheUpMostLane(1, arcLaneSouth2Outer);
	        laneToRoad.put(arcLaneSouth2Outer, left);
	        arcLaneSouth2Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Third arc Lane entering roundabout OE3-OC3
	        Arc2D arcSouth3Outer = new Arc2D.Double();
	        double arcSouth3OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcSouth3OuterStartAngle = Math.toDegrees(GeomMath.TWO_PI) - theta;
	        arcSouth3Outer.setArcByCenter(O3.getX(), O3.getY(), outerLaneEntryRadius, arcSouth3OuterStartAngle, -arcSouth3OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth3Outer = new ArcSegmentLane(arcSouth3Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneSouth3Outer.setId(laneRegistry.register(arcLaneSouth3Outer));
	        left.addTheUpMostLane(1, arcLaneSouth3Outer);
	        laneToRoad.put(arcLaneSouth3Outer, left);
	        arcLaneSouth3Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Fourth arc Lane inside roundabout OC3-OC8
	        Arc2D arcSouth4Outer = new Arc2D.Double();
	        double arcSouth4OuterExtentAngle = beta;
	        double arcSouth4OuterStartAngle = Math.toDegrees(GeomMath.PI) - alpha - beta;
	        arcSouth4Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcSouth4OuterStartAngle, arcSouth4OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth4Outer = new ArcSegmentLane(arcSouth4Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneSouth4Outer.setId(laneRegistry.register(arcLaneSouth4Outer));
	        left.addTheUpMostLane(1, arcLaneSouth4Outer);
	        laneToRoad.put(arcLaneSouth4Outer, left);
	        arcLaneSouth4Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Fifth arc Lane inside roundabout OC8-OC7
	        Arc2D arcSouth5Outer = new Arc2D.Double();
	        double arcSouth5OuterExtentAngle = 2 * alpha;
	        double arcSouth5OuterStartAngle = Math.toDegrees(GeomMath.PI) - alpha;
	        arcSouth5Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcSouth5OuterStartAngle, arcSouth5OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth5Outer = new ArcSegmentLane(arcSouth5Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
	        arcLaneSouth5Outer.setId(laneRegistry.register(arcLaneSouth5Outer));
	        left.addTheUpMostLane(1, arcLaneSouth5Outer);
	        laneToRoad.put(arcLaneSouth5Outer, left);
	        arcLaneSouth5Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Sixth arc Lane inside roundabout OC7-OC2
	        Arc2D arcSouth6Outer = new Arc2D.Double();
	        double arcSouth6OuterExtentAngle = beta;
	        double arcSouth6OuterStartAngle = Math.toDegrees(GeomMath.PI) + alpha;
	        arcSouth6Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcSouth6OuterStartAngle, arcSouth6OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth6Outer = new ArcSegmentLane(arcSouth6Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneSouth6Outer.setId(laneRegistry.register(arcLaneSouth6Outer));
	        left.addTheUpMostLane(1, arcLaneSouth6Outer);
	        laneToRoad.put(arcLaneSouth6Outer, left);
	        arcLaneSouth6Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Seventh arc Lane exiting roundabout OC2-OE2
	        Arc2D arcSouth7Outer = new Arc2D.Double();
	        double arcSouth7OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcSouth7OuterStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
	        arcSouth7Outer.setArcByCenter(O2.getX(), O2.getY(), outerLaneEntryRadius, arcSouth7OuterStartAngle, -arcSouth7OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth7Outer = new ArcSegmentLane(arcSouth7Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneSouth7Outer.setId(laneRegistry.register(arcLaneSouth7Outer));
	        left.addTheUpMostLane(1, arcLaneSouth7Outer);
	        laneToRoad.put(arcLaneSouth7Outer, left);
	        arcLaneSouth7Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Eight arc Lane exiting on approach the roundabout OE2-OB2
	        Arc2D arcSouth8Outer = new Arc2D.Double();
	        double arcSouth8OuterExtentAngle = theta;
	        double arcSouth8OuterStartAngle = arcSouth8OuterExtentAngle;
	        arcSouth8Outer.setArcByCenter(O2.getX(), O2.getY(), outerLaneEntryRadius, arcSouth8OuterStartAngle, -arcSouth8OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneSouth8Outer = new ArcSegmentLane(arcSouth8Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneSouth8Outer.setId(laneRegistry.register(arcLaneSouth8Outer));
	        left.addTheUpMostLane(1, arcLaneSouth8Outer);
	        laneToRoad.put(arcLaneSouth8Outer, left);
	        arcLaneSouth8Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, left);
	        });
	        
	        // Seventh line Lane outside roundabout OB2-OA2
	        LineSegmentLane lineLaneSouth9Outer = new LineSegmentLane(
	                mapOrigin.getX() - (3 * laneWidth / 2),
	                mapOrigin.getY() + b,
	                mapOrigin.getX() - (3 * laneWidth / 2),
	                height,
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneSouth9Outer.setId(laneRegistry.register(lineLaneSouth9Outer));
	        left.addTheUpMostLane(1, lineLaneSouth9Outer);
	        laneToRoad.put(lineLaneSouth9Outer, left);
	        laneDecompositionToRoad.put(lineLaneSouth9Outer, left);
	        
	        // Set continuous lanes for all arc lanes
	        arcLaneSouth2Outer.setContinuousLanes();
	        arcLaneSouth3Outer.setContinuousLanes();
	        arcLaneSouth4Outer.setContinuousLanes();
	        arcLaneSouth5Outer.setContinuousLanes();
	        arcLaneSouth6Outer.setContinuousLanes();
	        arcLaneSouth7Outer.setContinuousLanes();
	        arcLaneSouth8Outer.setContinuousLanes();
	        
	     // generate the data collection lines
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "SouthBound Outer" + "Entrance",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(mapOrigin.getX() - laneWidth,
	                                DATA_COLLECTION_LINE_POSITION),
	                        new Point2D.Double(mapOrigin.getX() - (2 * laneWidth),
	                                DATA_COLLECTION_LINE_POSITION),
	                        true));
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "SouthBound Outer" + "Exit",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(mapOrigin.getX() - laneWidth,
	                                height - DATA_COLLECTION_LINE_POSITION),
	                        new Point2D.Double(mapOrigin.getX() - (2 * laneWidth),
	                                height - DATA_COLLECTION_LINE_POSITION),
	                        true));
        }
        
        verticalRoads.add(left);
        
        // Set up the "dual" relationship
        right.setDual(left);
        
        // ------------------------------------------------------------------------------------------------------------
        // Create the east horizontal road inner lane
        Road lower = new Road("1st Street E", this);
        lower.setUpContinuousLanes(lanesPerRoad);
        
        // First line Lane going East IA7-IB7
        LineSegmentLane lineLaneEast1Inner = new LineSegmentLane(
                0,
                mapOrigin.getY() + laneWidth / 2,
                mapOrigin.getX() - b,
                mapOrigin.getY() + laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneEast1Inner.setId(laneRegistry.register(lineLaneEast1Inner));
        lower.addTheUpMostLane(0, lineLaneEast1Inner);
        laneToRoad.put(lineLaneEast1Inner, lower);
        laneDecompositionToRoad.put(lineLaneEast1Inner, lower);
        
        // Second arc Lane approaching roundabout IB7-IE7
        Arc2D arcEast2Inner = new Arc2D.Double();
        double arcEast2InnerExtentAngle = theta;
        double arcEast2InnerStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
        arcEast2Inner.setArcByCenter(O7.getX(), O7.getY(), innerLaneEntryRadius, arcEast2InnerStartAngle, -arcEast2InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast2Inner = new ArcSegmentLane(arcEast2Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast2Inner.setId(laneRegistry.register(arcLaneEast2Inner));
        lower.addTheUpMostLane(0, arcLaneEast2Inner);
        laneToRoad.put(arcLaneEast2Inner, lower);
        arcLaneEast2Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Third arc Lane entering roundabout IE7-IC7
        Arc2D arcEast3Inner = new Arc2D.Double();
        double arcEast3InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcEast3InnerStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - theta;
        arcEast3Inner.setArcByCenter(O7.getX(), O7.getY(), innerLaneEntryRadius, arcEast3InnerStartAngle, -arcEast3InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast3Inner = new ArcSegmentLane(arcEast3Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast3Inner.setId(laneRegistry.register(arcLaneEast3Inner));
        lower.addTheUpMostLane(0, arcLaneEast3Inner);
        laneToRoad.put(arcLaneEast3Inner, lower);
        arcLaneEast3Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Fourth arc Lane inside roundabout IC7-IC2
        Arc2D arcEast4Inner = new Arc2D.Double();
        double arcEast4InnerExtentAngle = beta;
        double arcEast4InnerStartAngle = Math.toDegrees(GeomMath.PI) + alpha;
        arcEast4Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcEast4InnerStartAngle, arcEast4InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast4Inner = new ArcSegmentLane(arcEast4Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast4Inner.setId(laneRegistry.register(arcLaneEast4Inner));
        lower.addTheUpMostLane(0, arcLaneEast4Inner);
        laneToRoad.put(arcLaneEast4Inner, lower);
        arcLaneEast4Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Fifth arc Lane inside roundabout IC2-IC1
        Arc2D arcEast5Inner = new Arc2D.Double();
        double arcEast5InnerExtentAngle = 2 * alpha;
        double arcEast5InnerStartAngle = Math.toDegrees(GeomMath.TWO_PI) - beta - 3 * alpha;
        arcEast5Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcEast5InnerStartAngle, arcEast5InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast5Inner = new ArcSegmentLane(arcEast5Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneEast5Inner.setId(laneRegistry.register(arcLaneEast5Inner));
        lower.addTheUpMostLane(0, arcLaneEast5Inner);
        laneToRoad.put(arcLaneEast5Inner, lower);
        arcLaneEast5Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Sixth arc Lane inside roundabout IC1-IC6
        Arc2D arcEast6Inner = new Arc2D.Double();
        double arcEast6InnerExtentAngle = beta;
        double arcEast6InnerStartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha - beta;
        arcEast6Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcEast6InnerStartAngle, arcEast6InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast6Inner = new ArcSegmentLane(arcEast6Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast6Inner.setId(laneRegistry.register(arcLaneEast6Inner));
        lower.addTheUpMostLane(0, arcLaneEast6Inner);
        laneToRoad.put(arcLaneEast6Inner, lower);
        arcLaneEast6Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Seventh arc Lane exiting roundabout IC6-IE6
        Arc2D arcEast7Inner = new Arc2D.Double();
        double arcEast7InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcEast7InnerStartAngle = Math.toDegrees(GeomMath.PI) - alpha;
        arcEast7Inner.setArcByCenter(O6.getX(), O6.getY(), innerLaneEntryRadius, arcEast7InnerStartAngle, -arcEast7InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast7Inner = new ArcSegmentLane(arcEast7Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast7Inner.setId(laneRegistry.register(arcLaneEast7Inner));
        lower.addTheUpMostLane(0, arcLaneEast7Inner);
        laneToRoad.put(arcLaneEast7Inner, lower);
        arcLaneEast7Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Eight arc Lane exiting on approach the roundabout IE6-IB6
        Arc2D arcEast8Inner = new Arc2D.Double();
        double arcEast8InnerExtentAngle = theta;
        double arcEast8InnerStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + theta;
        arcEast8Inner.setArcByCenter(O6.getX(), O6.getY(), innerLaneEntryRadius, arcEast8InnerStartAngle, -arcEast8InnerExtentAngle, 0);
        ArcSegmentLane arcLaneEast8Inner = new ArcSegmentLane(arcEast8Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneEast8Inner.setId(laneRegistry.register(arcLaneEast8Inner));
        lower.addTheUpMostLane(0, arcLaneEast8Inner);
        laneToRoad.put(arcLaneEast8Inner, lower);
        arcLaneEast8Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, lower);
        });
        
        // Ninth line Lane exiting roundabout IB6-IA6
        LineSegmentLane lineLaneEast9Inner = new LineSegmentLane(
                mapOrigin.getX() + b,
                mapOrigin.getY() + laneWidth / 2,
                width,
                mapOrigin.getY() + laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneEast9Inner.setId(laneRegistry.register(lineLaneEast9Inner));
        lower.addTheUpMostLane(0, lineLaneEast9Inner);
        laneToRoad.put(lineLaneEast9Inner, lower);
        laneDecompositionToRoad.put(lineLaneEast9Inner, lower);
        
        // Set continuous lanes for all arc lanes
        arcLaneEast2Inner.setContinuousLanes();
        arcLaneEast3Inner.setContinuousLanes();
        arcLaneEast4Inner.setContinuousLanes();
        arcLaneEast5Inner.setContinuousLanes();
        arcLaneEast6Inner.setContinuousLanes();
        arcLaneEast7Inner.setContinuousLanes();
        arcLaneEast8Inner.setContinuousLanes();
        
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "EastBound Inner" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() + laneWidth),
                        true));
        
        dataCollectionLines.add(
                new DataCollectionLine(
                        "EastBound Inner" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() + laneWidth),
                        true));
        
        // Create the east horizontal road outer lane
        if (lanesPerRoad == 2) {
	        // First line Lane going East OA7-OB7
	        LineSegmentLane lineLaneEast1Outer = new LineSegmentLane(
	                0,
	                mapOrigin.getY() + (3 * laneWidth / 2),
	                mapOrigin.getX() - b,
	                mapOrigin.getY() + (3 * laneWidth / 2),
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneEast1Outer.setId(laneRegistry.register(lineLaneEast1Outer));
	        lower.addTheUpMostLane(1, lineLaneEast1Outer);
	        laneToRoad.put(lineLaneEast1Outer, lower);
	        laneDecompositionToRoad.put(lineLaneEast1Outer, lower);
	        
	        // Second arc Lane approaching roundabout OB7-OE7
	        Arc2D arcEast2Outer = new Arc2D.Double();
	        double arcEast2OuterExtentAngle = theta;
	        double arcEast2OuterStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
	        arcEast2Outer.setArcByCenter(O7.getX(), O7.getY(), outerLaneEntryRadius, arcEast2OuterStartAngle, -arcEast2OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast2Outer = new ArcSegmentLane(arcEast2Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneEast2Outer.setId(laneRegistry.register(arcLaneEast2Outer));
	        lower.addTheUpMostLane(1, arcLaneEast2Outer);
	        laneToRoad.put(arcLaneEast2Outer, lower);
	        arcLaneEast2Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Third arc Lane entering roundabout OE7-OC7
	        Arc2D arcEast3Outer = new Arc2D.Double();
	        double arcEast3OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcEast3OuterStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - theta;
	        arcEast3Outer.setArcByCenter(O7.getX(), O7.getY(), outerLaneEntryRadius, arcEast3OuterStartAngle, -arcEast3OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast3Outer = new ArcSegmentLane(arcEast3Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneEast3Outer.setId(laneRegistry.register(arcLaneEast3Outer));
	        lower.addTheUpMostLane(1, arcLaneEast3Outer);
	        laneToRoad.put(arcLaneEast3Outer, lower);
	        arcLaneEast3Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Fourth arc Lane inside roundabout OC7-OC2
	        Arc2D arcEast4Outer = new Arc2D.Double();
	        double arcEast4OuterExtentAngle = beta;
	        double arcEast4OuterStartAngle = Math.toDegrees(GeomMath.PI) + alpha;
	        arcEast4Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcEast4OuterStartAngle, arcEast4OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast4Outer = new ArcSegmentLane(arcEast4Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneEast4Outer.setId(laneRegistry.register(arcLaneEast4Outer));
	        lower.addTheUpMostLane(1, arcLaneEast4Outer);
	        laneToRoad.put(arcLaneEast4Outer, lower);
	        arcLaneEast4Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Fifth arc Lane inside roundabout OC2-OC1
	        Arc2D arcEast5Outer = new Arc2D.Double();
	        double arcEast5OuterExtentAngle = 2 * alpha;
	        double arcEast5OuterStartAngle = Math.toDegrees(GeomMath.TWO_PI) - beta - 3 * alpha;
	        arcEast5Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcEast5OuterStartAngle, arcEast5OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast5Outer = new ArcSegmentLane(arcEast5Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
	        arcLaneEast5Outer.setId(laneRegistry.register(arcLaneEast5Outer));
	        lower.addTheUpMostLane(1, arcLaneEast5Outer);
	        laneToRoad.put(arcLaneEast5Outer, lower);
	        arcLaneEast5Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Sixth arc Lane inside roundabout OC1-OC6
	        Arc2D arcEast6Outer = new Arc2D.Double();
	        double arcEast6OuterExtentAngle = beta;
	        double arcEast6OuterStartAngle = Math.toDegrees(GeomMath.TWO_PI) - alpha - beta;
	        arcEast6Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcEast6OuterStartAngle, arcEast6OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast6Outer = new ArcSegmentLane(arcEast6Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneEast6Outer.setId(laneRegistry.register(arcLaneEast6Outer));
	        lower.addTheUpMostLane(1, arcLaneEast6Outer);
	        laneToRoad.put(arcLaneEast6Outer, lower);
	        arcLaneEast6Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Seventh arc Lane exiting roundabout OC6-OE6
	        Arc2D arcEast7Outer = new Arc2D.Double();
	        double arcEast7OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcEast7OuterStartAngle = Math.toDegrees(GeomMath.PI) - alpha;
	        arcEast7Outer.setArcByCenter(O6.getX(), O6.getY(), outerLaneEntryRadius, arcEast7OuterStartAngle, -arcEast7OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast7Outer = new ArcSegmentLane(arcEast7Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneEast7Outer.setId(laneRegistry.register(arcLaneEast7Outer));
	        lower.addTheUpMostLane(1, arcLaneEast7Outer);
	        laneToRoad.put(arcLaneEast7Outer, lower);
	        arcLaneEast7Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Eight arc Lane exiting on approach the roundabout OE6-OB6
	        Arc2D arcEast8Outer = new Arc2D.Double();
	        double arcEast8OuterExtentAngle = theta;
	        double arcEast8OuterStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + theta;
	        arcEast8Outer.setArcByCenter(O6.getX(), O6.getY(), outerLaneEntryRadius, arcEast8OuterStartAngle, -arcEast8OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneEast8Outer = new ArcSegmentLane(arcEast8Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneEast8Outer.setId(laneRegistry.register(arcLaneEast8Outer));
	        lower.addTheUpMostLane(1, arcLaneEast8Outer);
	        laneToRoad.put(arcLaneEast8Outer, lower);
	        arcLaneEast8Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, lower);
	        });
	        
	        // Ninth line Lane exiting roundabout OB6-OA6
	        LineSegmentLane lineLaneEast9Outer = new LineSegmentLane(
	                mapOrigin.getX() + b,
	                mapOrigin.getY() + (3 * laneWidth / 2),
	                width,
	                mapOrigin.getY() + (3 * laneWidth / 2),
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneEast9Outer.setId(laneRegistry.register(lineLaneEast9Outer));
	        lower.addTheUpMostLane(1, lineLaneEast9Outer);
	        laneToRoad.put(lineLaneEast9Outer, lower);
	        laneDecompositionToRoad.put(lineLaneEast9Outer, lower);
	        
	        // Set continuous lanes for all arc lanes
	        arcLaneEast2Outer.setContinuousLanes();
	        arcLaneEast3Outer.setContinuousLanes();
	        arcLaneEast4Outer.setContinuousLanes();
	        arcLaneEast5Outer.setContinuousLanes();
	        arcLaneEast6Outer.setContinuousLanes();
	        arcLaneEast7Outer.setContinuousLanes();
	        arcLaneEast8Outer.setContinuousLanes();
	        
	     // generate the data collection lines
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "EastBound Outer" + "Entrance",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() + laneWidth),
	                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() + (2 * laneWidth)),
	                        true));
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "EastBound Outer" + "Exit",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() + laneWidth),
	                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() + (2 * laneWidth)),
	                        true));
        }
        
        horizontalRoads.add(lower);
        
        
        // ------------------------------------------------------------------------------------------------------------
        // Now we create the west horizontal road inner lane
        Road upper = new Road("1st Street W", this);
        upper.setUpContinuousLanes(lanesPerRoad);
        
        // First line Lane going West IA5-IB5
        LineSegmentLane lineLaneWest1Inner = new LineSegmentLane(
                width,
                mapOrigin.getY() - laneWidth / 2,
                mapOrigin.getX() + b,
                mapOrigin.getY() - laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneWest1Inner.setId(laneRegistry.register(lineLaneWest1Inner));
        upper.addTheUpMostLane(0, lineLaneWest1Inner);
        laneToRoad.put(lineLaneWest1Inner, upper);
        laneDecompositionToRoad.put(lineLaneWest1Inner, upper);
        
        // Second arc Lane entering roundabout IB5-IE5
        Arc2D arcWest2Inner = new Arc2D.Double();
        double arcWest2InnerExtentAngle = theta;
        double arcWest2InnerStartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
        arcWest2Inner.setArcByCenter(O5.getX(), O5.getY(), innerLaneEntryRadius, arcWest2InnerStartAngle, -arcWest2InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest2Inner = new ArcSegmentLane(arcWest2Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest2Inner.setId(laneRegistry.register(arcLaneWest2Inner));
        upper.addTheUpMostLane(0, arcLaneWest2Inner);
        laneToRoad.put(arcLaneWest2Inner, upper);
        arcLaneWest2Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Third arc Lane entering roundabout IE5-IC5
        Arc2D arcWest3Inner = new Arc2D.Double();
        double arcWest3InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcWest3InnerStartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - theta;
        arcWest3Inner.setArcByCenter(O5.getX(), O5.getY(), innerLaneEntryRadius, arcWest3InnerStartAngle, -arcWest3InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest3Inner = new ArcSegmentLane(arcWest3Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest3Inner.setId(laneRegistry.register(arcLaneWest3Inner));
        upper.addTheUpMostLane(0, arcLaneWest3Inner);
        laneToRoad.put(arcLaneWest3Inner, upper);
        arcLaneWest3Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Fourth arc Lane inside roundabout IC5-IC4
        Arc2D arcWest4Inner = new Arc2D.Double();
        double arcWest4InnerExtentAngle = beta;
        double arcWest4InnerStartAngle = alpha;
        arcWest4Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcWest4InnerStartAngle, arcWest4InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest4Inner = new ArcSegmentLane(arcWest4Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest4Inner.setId(laneRegistry.register(arcLaneWest4Inner));
        upper.addTheUpMostLane(0, arcLaneWest4Inner);
        laneToRoad.put(arcLaneWest4Inner, upper);
        arcLaneWest4Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Fifth arc Lane inside roundabout IC4-IC3
        Arc2D arcWest5Inner = new Arc2D.Double();
        double arcWest5InnerExtentAngle = 2 * alpha;
        double arcWest5InnerStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
        arcWest5Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcWest5InnerStartAngle, arcWest5InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest5Inner = new ArcSegmentLane(arcWest5Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
        arcLaneWest5Inner.setId(laneRegistry.register(arcLaneWest5Inner));
        upper.addTheUpMostLane(0, arcLaneWest5Inner);
        laneToRoad.put(arcLaneWest5Inner, upper);
        arcLaneWest5Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Sixth arc Lane inside roundabout IC3-IC8
        Arc2D arcWest6Inner = new Arc2D.Double();
        double arcWest6InnerExtentAngle = beta;
        double arcWest6InnerStartAngle = Math.toDegrees(GeomMath.PI) - alpha - beta;
        arcWest6Inner.setArcByCenter(O.getX(), O.getY(), innerLaneRadius, arcWest6InnerStartAngle, arcWest6InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest6Inner = new ArcSegmentLane(arcWest6Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest6Inner.setId(laneRegistry.register(arcLaneWest6Inner));
        upper.addTheUpMostLane(0, arcLaneWest6Inner);
        laneToRoad.put(arcLaneWest6Inner, upper);
        arcLaneWest6Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Seventh arc Lane exiting roundabout IC8-IE8
        Arc2D arcWest7Inner = new Arc2D.Double();
        double arcWest7InnerExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
        double arcWest7InnerStartAngle = 3 * Math.toDegrees(GeomMath.TWO_PI) - alpha;
        arcWest7Inner.setArcByCenter(O8.getX(), O8.getY(), innerLaneEntryRadius, arcWest7InnerStartAngle, -arcWest7InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest7Inner = new ArcSegmentLane(arcWest7Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest7Inner.setId(laneRegistry.register(arcLaneWest7Inner));
        upper.addTheUpMostLane(0, arcLaneWest7Inner);
        laneToRoad.put(arcLaneWest7Inner, upper);
        arcLaneWest7Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Eight arc Lane exiting roundabout IE8-IB8
        Arc2D arcWest8Inner = new Arc2D.Double();
        double arcWest8InnerExtentAngle = theta;
        double arcWest8InnerStartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + arcWest8InnerExtentAngle;
        arcWest8Inner.setArcByCenter(O8.getX(), O8.getY(), innerLaneEntryRadius, arcWest8InnerStartAngle, -arcWest8InnerExtentAngle, 0);
        ArcSegmentLane arcLaneWest8Inner = new ArcSegmentLane(arcWest8Inner, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
        arcLaneWest8Inner.setId(laneRegistry.register(arcLaneWest8Inner));
        upper.addTheUpMostLane(0, arcLaneWest8Inner);
        laneToRoad.put(arcLaneWest8Inner, upper);
        arcLaneWest8Inner.getArcLaneDecomposition().forEach(lineDecomposition -> {
            laneDecompositionToRoad.put(lineDecomposition, upper);
        });
        
        // Ninth line Lane outside roundabout IB8-IA8
        LineSegmentLane lineLaneWest9Inner = new LineSegmentLane(
                mapOrigin.getX() - b,
                mapOrigin.getY() - laneWidth / 2,
                0,
                mapOrigin.getY() - laneWidth / 2,
                laneWidth,
                laneSpeedLimit
        );
        lineLaneWest9Inner.setId(laneRegistry.register(lineLaneWest9Inner));
        upper.addTheUpMostLane(0, lineLaneWest9Inner);
        laneToRoad.put(lineLaneWest9Inner, upper);
        laneDecompositionToRoad.put(lineLaneWest9Inner, upper);
        
        // Set continuous lanes for all arc lanes
        arcLaneWest2Inner.setContinuousLanes();
        arcLaneWest3Inner.setContinuousLanes();
        arcLaneWest4Inner.setContinuousLanes();
        arcLaneWest5Inner.setContinuousLanes();
        arcLaneWest6Inner.setContinuousLanes();
        arcLaneWest7Inner.setContinuousLanes();
        arcLaneWest8Inner.setContinuousLanes();
        
        // generate the data collection lines
        dataCollectionLines.add(
                new DataCollectionLine(
                        "WestBound Inner" + "Entrance",
                        dataCollectionLines.size(),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() - laneWidth),
                        true));
        dataCollectionLines.add(
                new DataCollectionLine(
                        "WestBound Inner" + "Exit",
                        dataCollectionLines.size(),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY()),
                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                mapOrigin.getY() - laneWidth),
                        true));
        
    	// Now we create the west horizontal road outer lane
        if (lanesPerRoad == 2 ) {	        
	        // First line Lane going West OA5-OB5
	        LineSegmentLane lineLaneWest1Outer = new LineSegmentLane(
	                width,
	                mapOrigin.getY() - (3 * laneWidth / 2),
	                mapOrigin.getX() + b,
	                mapOrigin.getY() - (3 * laneWidth / 2),
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneWest1Outer.setId(laneRegistry.register(lineLaneWest1Outer));
	        upper.addTheUpMostLane(1, lineLaneWest1Outer);
	        laneToRoad.put(lineLaneWest1Outer, upper);
	        laneDecompositionToRoad.put(lineLaneWest1Outer, upper);
	        
	        // Second arc Lane entering roundabout OB5-OE5
	        Arc2D arcWest2Outer = new Arc2D.Double();
	        double arcWest2OuterExtentAngle = theta;
	        double arcWest2OuterStartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES);
	        arcWest2Outer.setArcByCenter(O5.getX(), O5.getY(), outerLaneEntryRadius, arcWest2OuterStartAngle, -arcWest2OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest2Outer = new ArcSegmentLane(arcWest2Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneWest2Outer.setId(laneRegistry.register(arcLaneWest2Outer));
	        upper.addTheUpMostLane(1, arcLaneWest2Outer);
	        laneToRoad.put(arcLaneWest2Outer, upper);
	        arcLaneWest2Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Third arc Lane entering roundabout OE5-OC5
	        Arc2D arcWest3Outer = new Arc2D.Double();
	        double arcWest3OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcWest3OuterStartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - theta;
	        arcWest3Outer.setArcByCenter(O5.getX(), O5.getY(), outerLaneEntryRadius, arcWest3OuterStartAngle, -arcWest3OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest3Outer = new ArcSegmentLane(arcWest3Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneWest3Outer.setId(laneRegistry.register(arcLaneWest3Outer));
	        upper.addTheUpMostLane(1, arcLaneWest3Outer);
	        laneToRoad.put(arcLaneWest3Outer, upper);
	        arcLaneWest3Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Fourth arc Lane inside roundabout OC5-OC4
	        Arc2D arcWest4Outer = new Arc2D.Double();
	        double arcWest4OuterExtentAngle = beta;
	        double arcWest4OuterStartAngle = alpha;
	        arcWest4Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcWest4OuterStartAngle, arcWest4OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest4Outer = new ArcSegmentLane(arcWest4Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneWest4Outer.setId(laneRegistry.register(arcLaneWest4Outer));
	        upper.addTheUpMostLane(1, arcLaneWest4Outer);
	        laneToRoad.put(arcLaneWest4Outer, upper);
	        arcLaneWest4Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Fifth arc Lane inside roundabout OC4-OC3
	        Arc2D arcWest5Outer = new Arc2D.Double();
	        double arcWest5OuterExtentAngle = 2 * alpha;
	        double arcWest5OuterStartAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha;
	        arcWest5Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcWest5OuterStartAngle, arcWest5OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest5Outer = new ArcSegmentLane(arcWest5Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor * 2);
	        arcLaneWest5Outer.setId(laneRegistry.register(arcLaneWest5Outer));
	        upper.addTheUpMostLane(1, arcLaneWest5Outer);
	        laneToRoad.put(arcLaneWest5Outer, upper);
	        arcLaneWest5Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Sixth arc Lane inside roundabout OC3-OC8
	        Arc2D arcWest6Outer = new Arc2D.Double();
	        double arcWest6OuterExtentAngle = beta;
	        double arcWest6OuterStartAngle = Math.toDegrees(GeomMath.PI) - alpha - beta;
	        arcWest6Outer.setArcByCenter(O.getX(), O.getY(), outerLaneRadius, arcWest6OuterStartAngle, arcWest6OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest6Outer = new ArcSegmentLane(arcWest6Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneWest6Outer.setId(laneRegistry.register(arcLaneWest6Outer));
	        upper.addTheUpMostLane(1, arcLaneWest6Outer);
	        laneToRoad.put(arcLaneWest6Outer, upper);
	        arcLaneWest6Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Seventh arc Lane exiting roundabout OC8-OE8
	        Arc2D arcWest7Outer = new Arc2D.Double();
	        double arcWest7OuterExtentAngle = Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) - alpha - theta;
	        double arcWest7OuterStartAngle = 3 * Math.toDegrees(GeomMath.TWO_PI) - alpha;
	        arcWest7Outer.setArcByCenter(O8.getX(), O8.getY(), outerLaneEntryRadius, arcWest7OuterStartAngle, -arcWest7OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest7Outer = new ArcSegmentLane(arcWest7Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneWest7Outer.setId(laneRegistry.register(arcLaneWest7Outer));
	        upper.addTheUpMostLane(1, arcLaneWest7Outer);
	        laneToRoad.put(arcLaneWest7Outer, upper);
	        arcLaneWest7Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Eight arc Lane exiting roundabout OE8-OB8
	        Arc2D arcWest8Outer = new Arc2D.Double();
	        double arcWest8OuterExtentAngle = theta;
	        double arcWest8OuterStartAngle = 3 * Math.toDegrees(GeomMath.HALF_PI_90_DEGREES) + arcWest8OuterExtentAngle;
	        arcWest8Outer.setArcByCenter(O8.getX(), O8.getY(), outerLaneEntryRadius, arcWest8OuterStartAngle, -arcWest8OuterExtentAngle, 0);
	        ArcSegmentLane arcLaneWest8Outer = new ArcSegmentLane(arcWest8Outer, roundaboutWidth, roundaboutSpeedLimit, splitFactor);
	        arcLaneWest8Outer.setId(laneRegistry.register(arcLaneWest8Outer));
	        upper.addTheUpMostLane(1, arcLaneWest8Outer);
	        laneToRoad.put(arcLaneWest8Outer, upper);
	        arcLaneWest8Outer.getArcLaneDecomposition().forEach(lineDecomposition -> {
	            laneDecompositionToRoad.put(lineDecomposition, upper);
	        });
	        
	        // Ninth line Lane outside roundabout OB8-OA8
	        LineSegmentLane lineLaneWest9Outer = new LineSegmentLane(
	                mapOrigin.getX() - b,
	                mapOrigin.getY() - (3 * laneWidth / 2),
	                0,
	                mapOrigin.getY() - (3 * laneWidth / 2),
	                laneWidth,
	                laneSpeedLimit
	        );
	        lineLaneWest9Outer.setId(laneRegistry.register(lineLaneWest9Outer));
	        upper.addTheUpMostLane(1, lineLaneWest9Outer);
	        laneToRoad.put(lineLaneWest9Outer, upper);
	        laneDecompositionToRoad.put(lineLaneWest9Outer, upper);
	        
	        // Set continuous lanes for all arc lanes
	        arcLaneWest2Outer.setContinuousLanes();
	        arcLaneWest3Outer.setContinuousLanes();
	        arcLaneWest4Outer.setContinuousLanes();
	        arcLaneWest5Outer.setContinuousLanes();
	        arcLaneWest6Outer.setContinuousLanes();
	        arcLaneWest7Outer.setContinuousLanes();
	        arcLaneWest8Outer.setContinuousLanes();
	        
	        // generate the data collection lines
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "WestBound Outer" + "Entrance",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() - laneWidth),
	                        new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() - (2 * laneWidth)),
	                        true));
	        dataCollectionLines.add(
	                new DataCollectionLine(
	                        "WestBound Outer" + "Exit",
	                        dataCollectionLines.size(),
	                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY()),
	                        new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
	                                mapOrigin.getY() - (2 * laneWidth)),
	                        true));
        }
        
        horizontalRoads.add(upper);        
        
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
        spawnPoints = new ArrayList<RIMSpawnPoint>(columns + rows);
        horizontalSpawnPoints = new ArrayList<RIMSpawnPoint>(rows);
        verticalSpawnPoints = new ArrayList<RIMSpawnPoint>(columns);

        // Make spawn points only on the first line lane of each road
        for (Road road : horizontalRoads) {
            horizontalSpawnPoints.add(makeSpawnPoint(initTime, road.getFirstLane(0)));
        }

        for (Road road : verticalRoads) {
            verticalSpawnPoints.add(makeSpawnPoint(initTime, road.getFirstLane(0)));
        }

        spawnPoints.addAll(horizontalSpawnPoints);
        spawnPoints.addAll(verticalSpawnPoints);

        Debug.currentRimMap = this;
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
        spawnPoints = new ArrayList<RIMSpawnPoint>(1);
        horizontalSpawnPoints = new ArrayList<RIMSpawnPoint>(1);

        Lane lane = horizontalRoads.get(0).getContinuousLanesForLane(0).get(0);
        horizontalSpawnPoints.add(makeSpawnPoint(initTime, lane));

        spawnPoints.addAll(horizontalSpawnPoints);

        Debug.currentRimMap = this;
    }

    /**
     * Make spawn points.
     *
     * @param initTime the initial time
     * @param lane     the lane
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
            memoMaximumLaneSpeedLimit = getRoads().get(0).getContinuousLanesForLane(0).get(0).getSpeedLimit();
        }
        return memoMaximumLaneSpeedLimit;
    }

    /**
     * Get the maximum speed limit in the roundabout.
     */
    public double getMaximumRoundaboutSpeedLimit() {
        if (memoMaximumRoundaboutSpeedLimit < 0) {
            memoMaximumRoundaboutSpeedLimit = getRoads().get(0).getEntryApproachLane(0).getSpeedLimit();
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
    public List<RIMSpawnPoint> getSpawnPoints() { return spawnPoints; }

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

    public Road getRoadByName(String nameOfRoad) {
        final Road[] roadByName = {null};
        roads.forEach(road -> {
            if (road.getName().compareTo(nameOfRoad) == 0) {
                roadByName[0] = road;
            }
        });
        return roadByName[0];
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
