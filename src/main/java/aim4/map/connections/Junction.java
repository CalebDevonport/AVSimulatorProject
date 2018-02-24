package aim4.map.connections;

import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.util.GeomMath;

import java.awt.geom.Area;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * Create a right-angled junction which connects
 * 2 or 3 roads, each with one lane.
 */
// TODO Rename to make it more specific, TJunctionRightAngleOneLane
public class Junction extends BasicConnection{

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Basic class constructor.
     * Takes the Roads which meet at a right angle and
     * straight line to make this junction.
     *
     * @param roads The roads involved in this junction.
     */
    public Junction(List<Road> roads) {
        super(roads);
        // Ensure that the roads given can be used to create a Junction
        validate(roads);
        // Now get the entry and exit points for each of the lanes.
        establishEntryAndExitPoints(areaOfConnection);
        // TODO These 2 following methods should be in the superclass, but
        // TODO require use of entry points and exit points.
        // TODO Is there a way around this? Or enforce to be called by subclasses?
        // Calculate the waypoints.
        calcWayPoints();
        // Now build a GeneralPath using the waypoints.
        addWayPointsPath();

    }

    @Override
    protected void validate(List<Road> roads) {
        // There can only be 2 or 3 Roads involved in this Junction
        if (roads.size() != 2 && roads.size() != 3){
            throw new IllegalArgumentException("There can only be 2 or 3 Roads " +
                    "involved in a Junction. Number of " +
                    "roads given: " + roads.size());
        }
        // There must be one lane in each road
        if (!hasOneLane(roads)){
            throw new IllegalArgumentException("All roads in the Junction " +
                    "must have 1 lane.");
        }
        // All lanes must be LineSegmentLanes
        for (Road road : roads){
            for (Lane lane : road.getLanes()){
                if (!(lane instanceof LineSegmentLane)){
                    throw new IllegalArgumentException("The lanes in each road " +
                            "must be a LineSegmentLane to create a Junction.");
                }
            }
        }
        // The roads must meet at 90 degrees
        if (!atNinetyDegrees(roads)){
            throw new IllegalArgumentException("Roads in a Junction must be at ninety degrees.");
        }
    }

    @Override
    protected void establishEntryAndExitPoints(Area areaOfJunction) {
        List<Line2D> perimeterSegments =
                GeomMath.polygonalShapePerimeterSegments(areaOfJunction);

        for (Road road : roads) {
            Lane lane = road.getOnlyLane();
            boolean startsInJunction = doesLaneStartInPerimeter(lane, perimeterSegments);
            boolean endsInJunction = doesLaneEndInPerimeter(lane, perimeterSegments);

            // A lane can only start or end in the junction, not both
            if (startsInJunction && endsInJunction) {
                throw new RuntimeException("A lane in a junction cannot start and end in the junction.");
            }

            Point2D entryPoint;
            Point2D exitPoint;
            if (startsInJunction) {
                // If this lane starts in the junction, then it must have an exit point
                for (Line2D segment : perimeterSegments) {
                    // If the lane intersects (but not with it's start point)
                    if (lane.intersectionPoint(segment) != null &&
                            !isPointOnPerimeterSegment(lane.getStartPoint(), segment)) {
                        exitPoint = lane.intersectionPoint(segment);
                        establishAsExitPoint(road, lane, exitPoint);
                        break;
                    }
                }
            } else if (endsInJunction) {
                // If this lane ends in the junction, then it must have an entry point
                for (Line2D segment : perimeterSegments) {
                    // If the lane intersects (but not with it's end point)
                    if (lane.intersectionPoint(segment) != null &&
                            !isPointOnPerimeterSegment(lane.getEndPoint(), segment)) {
                        entryPoint = lane.intersectionPoint(segment);
                        establishAsEntryPoint(road, lane, entryPoint);
                        break;
                    }
                }
            } else { // if (!endsInJunction && !startsInJunction)
                // If this lane runs straight through the junction, then it must have an entry and an exit point
                List<Point2D> intersectionPoints = new ArrayList<Point2D>();
                for (Line2D segment : perimeterSegments) {
                    if (lane.intersectionPoint(segment) != null) {
                        intersectionPoints.add(lane.intersectionPoint(segment));
                    }
                }
                assert(intersectionPoints.size() == 2);

                // Now decide which point is the entry and which is the exit
                // The point closest to the lane start point will be the entry point
                double distance1 = intersectionPoints.get(0).distance(lane.getStartPoint());
                double distance2 = intersectionPoints.get(1).distance(lane.getStartPoint());
                if (distance1 < distance2) {
                    // Point used in distance 1 is the entry point
                    entryPoint = intersectionPoints.get(0);
                    exitPoint = intersectionPoints.get(1);
                } else {
                    // Point used in distance 2 is the entry point
                    entryPoint = intersectionPoints.get(1);
                    exitPoint = intersectionPoints.get(0);
                }
                establishAsEntryPoint(road, lane, entryPoint);
                establishAsExitPoint(road, lane, exitPoint);
            }
        }

        // Fill in any of the holes
        this.areaOfConnection = GeomMath.filledArea(areaOfJunction);
    }

    /**
     * Get the turn direction of the vehicle at the next Junction.
     *
     * @param currentLane    the current lane.
     * @param departureLane  the departure lane.
     * @return the turn direction of the vehicle at the next Junction
     */
    /*public Constants.TurnDirection calcTurnDirection(Lane currentLane, Lane departureLane) {


        Road currentRoad = Debug.currentAimMap.getRoad(currentLane);
        Road departureRoad = Debug.currentAimMap.getRoad(departureLane);
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
                return Constants.TurnDirection.LEFT;
            } else if(theta > Math.PI) {
                return Constants.TurnDirection.RIGHT;
            } else {  // theta = Math.PI
                return Constants.TurnDirection.U_TURN;  // pretty unlikely.
            }
        }
    }*/
}
