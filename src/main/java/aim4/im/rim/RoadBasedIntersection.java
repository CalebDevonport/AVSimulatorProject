package aim4.im.rim;

import aim4.config.Constants;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.track.WayPoint;
import aim4.util.GeomMath;
import aim4.util.Util;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * An intersection that is defined by the intersection of a set of roads.
 */
public class RoadBasedIntersection implements Intersection{
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The distance outside of the strict intersection that the
     * IntersectionManager will control, in meters. {@value} meters.
     * This gives room for things like crosswalks.
     */
    public static final double EXPANSION_DISTANCE = 4; // meters

    /**
     * A small increase in size of the area such that the simulator can
     * determine whether a vehicle is close enough to the intersection.
     */
    private static final double AREA_PLUS_OFFSET = 0.000001;


    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The space governed by this intersection manager.
     */
    private Area area;

    /**
     * An area slightly larger than the area of the intersection.
     */
    private Area areaPlus;

    /**
     * The smallest circle that contains this intersection.
     */
    private Ellipse2D getMinimalCircle;

    /**
     * The biggest circle that contains this intersection.
     */
    private Ellipse2D getMaximalCircle;

    /**
     * The centroid of this intersection.
     */
    private Point2D centroid;


    // road

    /** The roads incident to this intersection. */
    private List<Road> roads = new ArrayList<Road>();

    /** The entry roads incidents to this intersection. */
    private List<Road> entryRoads = new ArrayList<Road>();

    /** The exit roads incidents to this intersection. */
    private List<Road> exitRoads = new ArrayList<Road>();


    // lanes

    /** The lanes incident to this intersection. */
    private List<Lane> lanes = new ArrayList<Lane>();

    // points

    /**
     * A list of the waypoints where lanes either enter or exit the intersection,
     * ordered by angle from the centroid.
     */
    private List<Point2D> points = new ArrayList<Point2D>();

    /**
     * A list of the waypoints where lanes either approach the intersection,
     * ordered by angle from the centroid.
     */
    private List<Point2D> approachPoints = new ArrayList<Point2D>();

    // cache

    /**
     * A map from lanes to the waypoints at which those lanes enter the
     * intersection.
     */
    private Map<Lane,WayPoint> entryPoints = new LinkedHashMap<Lane,WayPoint>();

    /**
     * A map from lanes to the waypoints at which those lanes approach to enter the
     * intersection.
     */
    private Map<Lane,WayPoint> approachEntryPoints = new LinkedHashMap<Lane,WayPoint>();

    /**
     * A map from lanes to the waypoints at which those lanes exit the
     * intersection.
     */
    private Map<Lane,WayPoint> exitPoints = new LinkedHashMap<Lane,WayPoint>();

    /**
     * A map from lanes to the waypoints at which those lanes approach to exit the
     * intersection.
     */
    private Map<Lane,WayPoint> approachExitPoints = new LinkedHashMap<Lane,WayPoint>();

    /**
     * A map from Lanes to the headings, in radians, of those Lanes at the
     * point at which they enter the space governed by this IntersectionManager.
     */
    private Map<Lane,Double> entryHeadings = new HashMap<Lane,Double>();

    /**
     * A map from Lanes to the headings, in radians, of those Lanes at the
     * point at which they approach enter the space governed by this IntersectionManager.
     */
    private Map<Lane,Double> approachEntryHeadings = new HashMap<Lane,Double>();

    /**
     * A map from Lanes to the headings, in radians, of those Lanes at the
     * point at which they exit the space governed by this IntersectionManager.
     */
    private Map<Lane,Double> exitHeadings = new HashMap<Lane,Double>();

    /**
     * A map from Lanes to the headings, in radians, of those Lanes at the
     * point at which they approach exit the space governed by this IntersectionManager.
     */
    private Map<Lane,Double> approachExitHeadings = new HashMap<Lane,Double>();


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Basic class constructor.  Takes the Roads for which
     * an IntersectionManager is needed and extracts all the necessary
     * information.
     *
     * @param roads a list of Roads whose intersection this IntersectionManager
     *              will manage
     */
    public RoadBasedIntersection(List<Road> roads) {
        this.roads = roads;
        // Get the list of Lanes we are using.
        extractLanes(roads);
        // Get the shape of the intersection
        findStrictIntersectionArea(roads);
        // Now get the entry, exit and approach points for each of the lanes.
        establishPoints(roads);
        // Find the centroid of the intersection
        centroid = findOriginOfConnection(roads);
        // Calculate the waypoints.
        calcWayPoints();
        // Calculate the minimal circular region
        getMinimalCircle = findMinimalCircle(centroid, roads);
        // Calculate the maximal circular region
        getMaximalCircle = findMaximalCircle(centroid, roads);

        calcEntryRoads(roads);
        calcExitRoads(roads);
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////

    /**
     * Given a List of Roads, pull out all the individual lanes.
     *
     * @param roads a list of Roads
     */
    private void extractLanes(List<Road> roads) {
        for(Road road : roads) {
            for(Lane lane : road.getContinuousLanes()) {
                lanes.add(lane);
            }
        }
    }

    /**
     * Find the Area that represents the intersection of the Roads.
     * The area will be the circle which represents the central island of the roundabout.
     * This will composed of the 3 arcLanes of every road (inside circles)
     * E.g. for North: C1-C6, C6-C5, C5-C4
     *
     * @param roads a list of Roads that enter the intersection
     * @return the area in which any two of these Roads intersect
     */
    private void findStrictIntersectionArea(List<Road> roads) {
        Area areaOfConnection = new Area();
        for (Road road: roads){
            for (int index = 3; index <= 5; index ++){
                areaOfConnection.add(new Area(road.getContinuousLanes().get(index).getShape()));
            }
        }
        this.area = areaOfConnection;
        // todo: take areaPlus different than area
        this.areaPlus = areaOfConnection;
    }

    /**
     * Determine the points at which each Lane enters, exits or approaches the intersection
     * and record them, along with the headings of the Lanes at those points.
     * Also, extend the space governed by the IntersectionManager to include
     * the each Lane out to these points.
     */
    private void establishPoints(List<Road> roads) {

        //Establish entry and approach entry points. This will correspond to first arc lane of every road
        for (Road road : roads){
            ArcSegmentLane approachEntryLane = (ArcSegmentLane) road.getContinuousLanes().get(1);

            // Approach entry point
            this.approachEntryPoints.put(approachEntryLane,
                    new WayPoint(approachEntryLane.getStartPoint()));

            // Approach heading corresponds to heading of first Line Lane of the respective arc Lane
            this.approachEntryHeadings.put(approachEntryLane,
                    approachEntryLane.getArcLaneDecomposition().get(0).getInitialHeading());

            // Entry point
            this.entryPoints.put(approachEntryLane,
                    new WayPoint(approachEntryLane.getEndPoint()));

            // Entry heading corresponds to heading of last Line Lane of the respective arc Lane
            this.entryHeadings.put(approachEntryLane,
                    approachEntryLane.getArcLaneDecomposition().get(approachEntryLane.getArcLaneDecomposition().size()-1).getInitialHeading());
        }

        //Establish exit and approach exit points. This will correspond to last arc lane of every road
        for (Road road : roads){
            ArcSegmentLane approachExitLane = (ArcSegmentLane) road.getExitApproachLane();

            // Approach exit point
            this.approachExitPoints.put(approachExitLane,
                    new WayPoint(approachExitLane.getEndPoint()));

            // Approach heading corresponds to heading of last Line Lane of the respective arc Lane
            this.approachExitHeadings.put(approachExitLane,
                    approachExitLane.getArcLaneDecomposition().get(approachExitLane.getArcLaneDecomposition().size()-1).getInitialHeading());

            // Exit point
            this.exitPoints.put(approachExitLane,
                    new WayPoint(approachExitLane.getStartPoint()));

            // Exit heading corresponds to heading of first Line Lane of the respective arc Lane
            this.exitHeadings.put(approachExitLane,
                    approachExitLane.getArcLaneDecomposition().get(0).getInitialHeading());
        }
    }

    // Get the origin of the intersection given a set of Roads. The origin will be the origin of the inside arcs of every road.
    protected Point2D findOriginOfConnection(List<Road> roads){
        ArcSegmentLane secondArcLane = (ArcSegmentLane)roads.get(0).getContinuousLanes().get(3);
        Point2D origin = new Point2D.Double(secondArcLane.getArc().getX() + secondArcLane.getArc().getWidth() / 2,
                secondArcLane.getArc().getY() + secondArcLane.getArc().getHeight() / 2);
        return origin;
    }

    /**
     * Calculate the list of points, ordered by angle to the centroid, where
     * Lanes either enter or exit the intersection.
     */
    private void calcWayPoints() {
        SortedMap<Double, Point2D> circumferentialEntryExitPointsByAngle =
                new TreeMap<Double, Point2D>();
        for(Point2D p : exitPoints.values()) {
            circumferentialEntryExitPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : entryPoints.values()) {
            circumferentialEntryExitPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : circumferentialEntryExitPointsByAngle.values()) {
            points.add(p);
        }

        SortedMap<Double, Point2D> circumferentialApproachPointsByAngle =
                new TreeMap<Double, Point2D>();
        for(Point2D p : approachExitPoints.values()) {
            circumferentialApproachPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : approachEntryPoints.values()) {
            circumferentialApproachPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : circumferentialApproachPointsByAngle.values()) {
            approachPoints.add(p);
        }
    }

    /**
     * Find the minimal circular region that represents the intersection.
     */
    private Ellipse2D findMinimalCircle(Point2D centroid, List<Road> roads) {
        ArcSegmentLane insideArcLane = (ArcSegmentLane) roads.get(0).getContinuousLanes().get(3);
        // The radius of the circle will be the radius of the left border of every inside arc
        double minimalRadius = insideArcLane.leftBorder().getWidth();
        return new Ellipse2D.Double(
                centroid.getX() - minimalRadius / 2,
                centroid.getY() - minimalRadius / 2,
                minimalRadius,
                minimalRadius);
    }

    /**
     * Find the maximal circular region that represents the intersection.
     */
    private Ellipse2D findMaximalCircle(Point2D centroid, List<Road> roads) {
        ArcSegmentLane insideArcLane = (ArcSegmentLane) roads.get(0).getContinuousLanes().get(3);
        // The radius of the circle will be the radius of the right border of every inside arc
        double maximalRadius = insideArcLane.rightBorder().getWidth();
        return new Ellipse2D.Double(
                centroid.getX() - maximalRadius / 2,
                centroid.getY() - maximalRadius / 2,
                maximalRadius,
                maximalRadius);
    }

    /**
     * Create the entry roads.
     */
    private void calcEntryRoads(List<Road> roads) {
        entryRoads.addAll(roads);
    }

    private void calcExitRoads(List<Road> roads) {
        exitRoads.addAll(roads);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the Roads incident to the space governed by this intersection.
     *
     * @return the roads managed by this intersection.
     */
    @Override
    public List<Road> getRoads() {
        return roads;
    }

    /**
     * Get the Roads incident to the space governed by this intersection by name.
     *
     * @return the roads managed by this intersection.
     */
    public Road getRoadByName(String name) {
        Road roadByName = null;
        for (Road road : roads) {
            if (road.getName() == name){
                roadByName = road;
            }
        }
        return roadByName;
    }

    /**
     * Get the Lanes incident to the space governed by this intersection.
     *
     * @return the lanes managed by this intersection.
     */
    @Override
    public List<Lane> getLanes() {
        return lanes;
    }

    /**
     * Get the Area controlled by this intersection manager.
     *
     * @return the Area controlled by this intersection manager
     */
    @Override
    public Area getArea() {
        return area;
    }

    /**
     * Get the area slightly larger than the area controlled
     * by this intersection manager.
     *
     * @return the Area controlled by this intersection manager
     */
    @Override
    public Area getAreaPlus() {
        return areaPlus;
    }

    /**
     * Get the centroid of the intersection manager.
     *
     * @return the centroid of the intersection manager
     */
    @Override
    public Point2D getCentroid() {
        return centroid;
    }

    /**
     * Get the minimal circular region that encloses the intersection.
     *
     * @return the minimal v region that encloses the intersection
     */
    @Override
    public Ellipse2D getMinimalCircle() {
        return getMinimalCircle;
    }

    /**
     * Get the maximal circular region that encloses the intersection.
     *
     * @return the minimal circular region that encloses the intersection
     */
    @Override
    public Ellipse2D getMaximalCircle() {
        return getMaximalCircle;
    }

    /**
     * Get the Roads that enter the space governed by this intersection manager.
     *
     * @return the Roads that enter the space governed by this
     *         IntersectionManager
     */
    @Override
    public List<Road> getEntryRoads() {
        return entryRoads;
    }

    /**
     * Get the Lanes that enter the space governed by this intersection manager.
     *
     * @return the Lanes that enter the space governed by this
     *         IntersectionManager
     */
    @Override
    public List<Lane> getEntryLanes() {
        return new ArrayList<Lane>(entryPoints.keySet());
    }

    /**
     * Whether the given Lane enters this intersection.
     *
     * @param l the Lane to consider
     * @return  whether the Lane enters this intersection
     */
    @Override
    public boolean isEnteredBy(Lane l) {
        return entryPoints.containsKey(l);
    }

    /**
     * Get the Point at which the given Lane enters the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane enters the intersection, or
     *          <code>null</code> if it does not
     */
    @Override
    public WayPoint getEntryPoint(Lane l) {
        return entryPoints.get(l);
    }

    /**
     * Get the Point at which the given Lane approach enters the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane enters the intersection, or
     *          <code>null</code> if it does not
     */
    @Override
    public WayPoint getApproachEntryPoint(Lane l) {
        return approachEntryPoints.get(l);
    }

    /**
     * Get the heading at which the given lane enters the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane enters the intersection
     */
    @Override
    public double getEntryHeading(Lane l) {
        return entryHeadings.get(l);
    }

    /**
     * Get the heading at which the given lane approach enters the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane enters the intersection
     */
    @Override
    public double getApproachEntryHeading(Lane l) {
        return approachEntryHeadings.get(l);
    }

    /**
     * Get the Roads that exit the space governed by this intersection manager.
     *
     * @return the Roads that exit the space governed by this
     *         IntersectionManager
     */
    @Override
    public List<Road> getExitRoads() {
        return exitRoads;
    }

    /**
     * Get the Lanes that exit the space governed by this intersection manager.
     *
     * @return the Lanes that exit the space governed by this
     *         IntersectionManager
     */
    @Override
    public List<Lane> getExitLanes() {
        return new ArrayList<Lane>(exitPoints.keySet());
    }

    /**
     * Whether the given Lane leaves this intersection.
     *
     * @param l the Lane to consider
     * @return  whether the Lane exits this intersection
     */
    @Override
    public boolean isExitedBy(Lane l) {
        return exitPoints.containsKey(l);
    }

    /**
     * Get the Point at which the given Lane exits the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane exits the intersection, or
     *          <code>null</code> if it does not
     */
    @Override
    public WayPoint getExitPoint(Lane l) {
        return exitPoints.get(l);
    }

    /**
     * Get the Point at which the given Lane approach exits the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane exits the intersection, or
     *          <code>null</code> if it does not
     */
    @Override
    public WayPoint getApproachExitPoint(Lane l) {
        return approachExitPoints.get(l);
    }

    /**
     * Get the heading at which the given Lane exits the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane exits the intersection
     */
    @Override
    public double getExitHeading(Lane l) {
        return exitHeadings.get(l);
    }

    /**
     * Get the heading at which the given Lane exits the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane exits the intersection
     */
    @Override
    public double getApproachExitHeading(Lane l) {
        return approachExitHeadings.get(l);
    }

    /**
     * Get the turn direction of the vehicle at the next intersection.
     *
     * @param currentLane    the current lane.
     * @param departureLane  the departure lane.
     * @return the turn direction of the vehicle at the next intersection
     */
    @Override
    public Constants.TurnDirection calcTurnDirection(Lane currentLane, Lane departureLane) {
        Road currentRoad = new Road();
        Road departureRoad = new Road();;
        for (Road road : roads){
            if (road.getContinuousLanes().contains(currentLane))
                currentRoad = road;
        }
        for (Road road : roads){
            if (road.getContinuousLanes().contains(departureLane))
                departureRoad = road;
        }
        assert currentRoad.getName() != null;
        assert departureRoad.getName() != null;

        if(departureRoad == currentRoad) {
            return Constants.TurnDirection.STRAIGHT;
        } else if(departureRoad == currentRoad.getDual()) {
            return Constants.TurnDirection.U_TURN;
        } else {
            double entryHeading = getEntryHeading(currentLane);
            double exitHeading = getExitHeading(departureLane);
            double theta = GeomMath.canonicalAngle(exitHeading-entryHeading);
            if(Util.isDoubleZero(theta)) {
                return Constants.TurnDirection.STRAIGHT; // despite they are different roads
            } else if(theta < Math.PI) {
                return Constants.TurnDirection.RIGHT;
            } else if(theta > Math.PI) {
                return Constants.TurnDirection.LEFT;
            } else {  // theta = Math.PI
                return Constants.TurnDirection.U_TURN;  // pretty unlikely.
            }
        }
    }
}
