package aim4.im.rim;

import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.track.WayPoint;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;

import static aim4.config.Constants.TurnDirection;

/**
 * The interface of an intersection
 */
public interface Intersection {
    /**
     * Get the Area controlled by this IntersectionManager.
     *
     * @return the Area controlled by this IntersectionManager
     */
    Area getArea();

    /**
     * Get the area slightly larger than the area controlled
     * by this IntersectionManager.
     *
     * @return the Area controlled by this IntersectionManager
     */
    Area getAreaPlus();

    /**
     * Get the centroid of the IntersectionManager.
     *
     * @return the centroid of the IntersectionManager
     */
    Point2D getCentroid();

    /**
     * Get the minimal circular region that encloses the intersection.
     *
     * @return the minimal circular region that encloses the intersection
     */
    Ellipse2D getMinimalCircle();

    /**
     * Get the central circular region that encloses the intersection.
     *
     * @return the central circular region that encloses the intersection
     */
    Ellipse2D getCentralCircle();
    
    /**
     * Get the maximal circular region that encloses the intersection.
     *
     * @return the maximal circular region that encloses the intersection
     */
    Ellipse2D getMaximalCircle();


    /**
     * Get the Roads incident to the space governed by this intersection.
     *
     * @return  the roads managed by this intersection.
     */
    List<Road> getRoads();

    /**
     * Get the Lanes incident to the space governed by this intersection.
     *
     * @return  the lanes managed by this intersection.
     */
    List<Lane> getLanes();


    // entry points

    /**
     * Get the Roads that enter the space governed by this IntersectionManager.
     *
     * @return the Roads that enter the space governed by this
     *         IntersectionManager
     */
    List<Road> getEntryRoads();

    /**
     * Get the Lanes that enter the space governed by this IntersectionManager.
     *
     * @return the Lanes that enter the space governed by this
     *         IntersectionManager
     */
    List<Lane> getEntryLanes();

    /**
     * Whether the given Lane enters this intersection.
     *
     * @param l the Lane to consider
     * @return  whether the Lane enters this intersection
     */
    boolean isEnteredBy(Lane l);

    /**
     * Get the Point at which the given Lane enters the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane enters the intersection, or
     *          <code>null</code> if it does not
     */
    WayPoint getEntryPoint(Lane l);

    /**
     * Get the heading at which the given Lane enters the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane enters the intersection
     */
    double getEntryHeading(Lane l);

    /**
     * Get the Point at which the given Lane Approaches the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane approaches the intersection, or
     *          <code>null</code> if it does not
     */
    WayPoint getApproachEntryPoint(Lane l);

    /**
     * Get the heading at which the given Lane approaches the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane approaches the intersection
     */
    double getApproachEntryHeading(Lane l);


    // exit points

    /**
     * Get the Roads that exit the space governed by this IntersectionManager.
     *
     * @return the Roads that exit the space governed by this
     *         IntersectionManager
     */
    List<Road> getExitRoads();

    /**
     * Get the Lanes that exit the space governed by this IntersectionManager.
     *
     * @return the Lanes that exit the space governed by this
     *         IntersectionManager
     */
    List<Lane> getExitLanes();

    /**
     * Whether the given Lane leaves this intersection.
     *
     * @param l the Lane to consider
     * @return  whether the Lane exits this intersection
     */
    boolean isExitedBy(Lane l);

    /**
     * Get the Point at which the given Lane exits the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane exits the intersection, or
     *          <code>null</code> if it does not
     */
    WayPoint getExitPoint(Lane l);

    /**
     * Get the heading at which the given Lane exits the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane exits the intersection
     */
    double getExitHeading(Lane l);

    /**
     * Get the Point at which the given Lane approaches to exit the intersection.
     *
     * @param l the Lane
     * @return  the Point at which the given Lane approaches to exit the intersection, or
     *          <code>null</code> if it does not
     */
    WayPoint getApproachExitPoint(Lane l);

    /**
     * Get the heading at which the given Lane approaches to exit the intersection.
     *
     * @param l the Lane
     * @return  the heading at which the Lane approaches to exit the intersection
     */
    double getApproachExitHeading(Lane l);


    // comparisons

    /**
     * Get the turn direction of the vehicle at the next intersection.
     *
     * @param currentLane    the current lane.
     * @param departureLane  the departure lane.
     * @return the turn direction of the vehicle at the next intersection
     */
    TurnDirection calcTurnDirection(Lane currentLane, Lane departureLane);
    
    /**
     * Get the total number of lanes from left to right in this intersection - for usage in TiledRimArea.
     *
     * @return the total number of lanes per road for this intersection
     */
    public int getLaneNum();

}
