package aim4.map.connections;

import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.track.WayPoint;
import aim4.util.GeomMath;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.*;

/**
 * Connect roads together to create a roundabout intersection.
 */
public class RimConnection implements RoadConnection {
    /////////////////////////////////
    // PROTECTED FIELDS
    /////////////////////////////////

    // area

    /**
     * The space governed by this connection.
     */
    protected Area areaOfConnection;

    /**
     * The centroid of this connection.
     */
    protected Point2D centroid;

    // road

    /**
     * The roads which meet to make this connection.
     */
    protected List<Road> roads = new ArrayList<Road>();

    /**
     * The entry roads incidents to this connection.
     */
    protected List<Road> entryRoads = new ArrayList<Road>();

    /**
     * The exit roads incidents to this connection.
     */
    protected List<Road> exitRoads = new ArrayList<Road>();

    // lanes

    /**
     * The lanes which meet to make this connection.
     */
    protected List<Lane> lanes = new ArrayList<Lane>();

    // points

    /**
     * A list of the coordinates where lanes enter or exit the connection,
     * ordered by angle from the centroid.
     */
    protected List<Point2D> points = new ArrayList<Point2D>();

    /**
     * A list of the coordinates where lanes approach the connection,
     * ordered by angle from the centroid.
     */
    protected List<Point2D> approachPoints = new ArrayList<Point2D>();

    /**
     * A map from lanes to the coordinates at which those lanes enter the
     * connection.
     */
    protected Map<Lane, WayPoint> entryPoints = new LinkedHashMap<Lane, WayPoint>();

    /**
     * A map from lanes to the coordinates at which those lanes approach the
     * connection.
     */
    protected Map<Lane, WayPoint> entryApproachPoints = new LinkedHashMap<Lane, WayPoint>();

    /**
     * A map from lanes to the coordinates at which those lanes exit the
     * connection.
     */
    protected Map<Lane, WayPoint> exitPoints = new LinkedHashMap<Lane, WayPoint>();

    /**
     * A map from lanes to the coordinates at which those lanes approach the
     * exit.
     */
    protected Map<Lane, WayPoint> exitApproachPoints = new LinkedHashMap<Lane, WayPoint>();

    // headings

    /**
     * A map from Lanes to the headings, in radians, of those Lanes at the
     * point at which they enter the space governed by this connection.
     */
    protected Map<Lane, Double> entryHeadings = new HashMap<Lane, Double>();

    /**
     * A map from Lanes to the headings, in radians, of those Lanes at the
     * point at which they exit the space governed by this connection.
     */
    protected Map<Lane, Double> exitHeadings = new HashMap<Lane, Double>();

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Basic class constructor.
     * Takes the Roads which meet to make this connection.
     *
     * @param roads the roads involved in this connection.
     */
    public RimConnection(List<Road> roads) {
        this.roads = roads;
        // Get the list of Lanes we are using.
        extractLanes(roads);
        // Find the area of the connection.
        this.areaOfConnection = findAreaOfConnection(roads);
        //todo: validate(roads);
        // The centroid of the connection is the origin of the central island (which is the origin of the
        // second arc in every road
        centroid = findOriginOfConnection(roads);
        // Calculate entry and approach points
        for(Road road : roads) {
            // The entry points will always be on the first Arc Lane of a Road
            establishAsEntryAndApproachPoint(road, road.getContinuousLanes().get(1));
            // The entry points will always be on the last Arc Lane of a Road
            establishAsExitAndApproachPoint(road, road.getContinuousLanes().get(5));
        }
        calcWayPoints();
        calcApproachWayPoints();
    }

    /////////////////////////////////
    // PROTECTED METHODS
    /////////////////////////////////

    /**
     * Given a List of Roads, pull out all the individual lanes.
     *
     * @param roads a list of Roads
     */
    protected void extractLanes(List<Road> roads) {
        for(Road road : roads) {
            for(Lane lane : road.getContinuousLanes()) {
                lanes.add(lane);
            }
        }
    }

    /**
     * Find the Area that represents the connection of Roads.
     * The area will be the circle which represents the central island.
     * This will be the 3 arcLanes of every road from the second till fourth.
     * E.g. for North: C1-C6, C6-C5, C5-C4
     *
     * @param roads a list of Roads that make the connection
     * @return the area which represents the area of connection.
     */
    protected Area findAreaOfConnection(List<Road> roads) {
        Area areaOfConnection = new Area();
        for (Road road: roads){
            for (int index = 2; index <= 4; index ++){
                areaOfConnection.add(new Area(road.getContinuousLanes().get(index).getShape()));
            }
        }
        return areaOfConnection;
    }

    // Given a set of Rim Roads, the origin is the origin of the second arc lane in every road.
    protected Point2D findOriginOfConnection(List<Road> roads){
        ArcSegmentLane secondArcLane = (ArcSegmentLane)roads.get(0).getContinuousLanes().get(2);
        Point2D origin = new Point2D.Double(secondArcLane.getArc().getX() + secondArcLane.getArc().getWidth() / 2,
                secondArcLane.getArc().getY() + secondArcLane.getArc().getHeight() / 2);
        return origin;
    }

    /**
     * Establish a new entry point to the connection.
     * The approach point is the start point of the lane.
     * The exit point is the end point of the lane.
     * @param road The road which enters the connection.
     * @param lane The lane we are adding an entry point for, which is part of the given road. Should be the first Arc Lane.
     */
    public void establishAsEntryAndApproachPoint(Road road, Lane lane){
        ArcSegmentLane arcLane = (ArcSegmentLane) lane;
        entryApproachPoints.put(lane, new WayPoint(lane.getStartPoint()));
        entryPoints.put(lane, new WayPoint(lane.getEndPoint()));
        // Here we are assuming that the heading of the lane is the same throughout.
        entryHeadings.put(lane, arcLane.getArcLaneDecomposition().get(arcLane.getArcLaneDecomposition().size()-1).getInitialHeading());
        if (!entryRoads.contains(road)){
            entryRoads.add(road);
        }
    }

    /**
     * Establish a new exit point to the connection.
     * The approach point is the end point of the lane.
     * The exit point is the end point of the lane.
     * @param road The road which exits the connection.
     * @param lane The lane we are adding an exit point for, which is part of the given road. Should be last Arc Lane.
     */
    public void establishAsExitAndApproachPoint(Road road, Lane lane){
        ArcSegmentLane arcLane = (ArcSegmentLane) lane;
        exitApproachPoints.put(lane, new WayPoint(lane.getEndPoint()));
        exitPoints.put(lane, new WayPoint(lane.getStartPoint()));
        // Here we are assuming that the heading of the lane is the same throughout.
        exitHeadings.put(lane, arcLane.getArcLaneDecomposition().get(0).getInitialHeading());
        if (!exitRoads.contains(road)){
            exitRoads.add(road);
        }

    }

    /**
     * Calculate the list of points, ordered by angle to the centroid, where
     * Lanes either enter or exit the corner.
     */
    public void calcWayPoints() {
        SortedMap<Double, Point2D> circumferentialPointsByAngle =
                new TreeMap<Double, Point2D>();
        for(Point2D p : exitPoints.values()) {
            circumferentialPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : entryPoints.values()) {
            circumferentialPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : circumferentialPointsByAngle.values()) {
            points.add(p);
        }
    }

    /**
     * Calculate the list of points, ordered by angle to the centroid, where
     * Lanes either enter or exit the corner.
     */
    public void calcApproachWayPoints() {
        SortedMap<Double, Point2D> circumferentialPointsByAngle =
                new TreeMap<Double, Point2D>();
        for(Point2D p : exitApproachPoints.values()) {
            circumferentialPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : entryApproachPoints.values()) {
            circumferentialPointsByAngle.put(GeomMath.angleToPoint(p,centroid),p);
        }
        for(Point2D p : circumferentialPointsByAngle.values()) {
            approachPoints.add(p);
        }
    }

    /**
     * Ensure that the roads given can be used to make the connection.
     * Throw an exception if the given roads are invalid.
     * @param roads
     */
    protected void validate(List<Road> roads) {

    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Find the entry lanes.
     */
    public List<Lane> getEntryLanes() {
        return new ArrayList<Lane>(entryPoints.keySet());
    }

    /**
     * Find the exit lanes.
     */
    public List<Lane> getExitLanes() {
        return new ArrayList<Lane>(exitPoints.keySet());
    }

    /**
     * Get the Roads incident to the corner.
     *
     * @return the roads involved in this corner.
     */
    public List<Road> getRoads() {
        return roads;
    }

    /**
     * Get the Lanes incident to the corner.
     *
     * @return the lanes involved in this corner.
     */
    public List<Lane> getLanes() {
        return lanes;
    }

    /**
     * Get the Area of this Corner.
     *
     * @return the Area of the corner
     */
    public Area getArea() {
        return areaOfConnection;
    }

    /**
     * Get the centroid of the corner.
     *
     * @return the centroid of the corner
     */
    public Point2D getCentroid() {
        return centroid;
    }

    /**
     * Get the Roads that enter the corner.
     *
     * @return the Roads that enter the corner.
     */
    public List<Road> getEntryRoads() {
        return entryRoads;
    }

    /**
     * Whether the given Lane enters this Corner.
     *
     * @param l the Lane to consider
     * @return  whether the Lane enters this Corner
     */
    public boolean isEnteredBy(Lane l) {
        return entryPoints.containsKey(l);
    }

    /**
     * Get the Point at which the given Lane approaches the corner.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane approaches the corner, or
     *          <code>null</code> if it does not
     */
    public WayPoint getEntryApproachPoint(Lane l) {
        return entryApproachPoints.get(l);
    }

    /**
     * Get the Point at which the given Lane enters the corner.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane enters the corner, or
     *          <code>null</code> if it does not
     */
    public WayPoint getEntryPoint(Lane l) {
        return entryPoints.get(l);
    }

    /**
     * Get the heading at which the given lane enters the corner.
     *
     * @param l the Lane
     * @return  the heading at which the Lane enters the corner
     */
    public double getEntryHeading(Lane l) {
        return entryHeadings.get(l);
    }

    /**
     * Get the Roads that exit the corner.
     *
     * @return the Roads that exit the corner
     */
    public List<Road> getExitRoads() {
        return exitRoads;
    }

    /**
     * Whether the given Lane exits this corner.
     *
     * @param l the Lane to consider
     * @return  whether the Lane exits this corner
     */
    public boolean isExitedBy(Lane l) {
        return exitPoints.containsKey(l);
    }

    /**
     * Get the Point at which the given Lane approaches the exit of the corner.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane approaches the exit the corner, or
     *          <code>null</code> if it does not
     */
    public WayPoint getExitApproachPoint(Lane l) {
        return exitApproachPoints.get(l);
    }

    /**
     * Get the Point at which the given Lane exits the corner.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane exits the corner, or
     *          <code>null</code> if it does not
     */
    public WayPoint getExitPoint(Lane l) {
        return exitPoints.get(l);
    }

    /**
     * Get the heading at which the given Lane exits the corner.
     *
     * @param l the Lane
     * @return  the heading at which the Lane exits the corner
     */
    public double getExitHeading(Lane l) {
        return exitHeadings.get(l);
    }
}
