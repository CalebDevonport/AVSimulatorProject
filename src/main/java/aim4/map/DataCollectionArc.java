package aim4.map;

import aim4.vehicle.VehicleSimModel;

import java.awt.*;
import java.awt.geom.Arc2D;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import static aim4.util.GeomMath.isPointIntersectingArc;

/**
 * The data collection arc.
 */
public class DataCollectionArc {
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The no repeat time period
     */
    protected static final double NO_REPEAT_TIME_PERIOD = 1.0; // seconds

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The name of this data collection arc
     */
    protected String name;
    /**
     * The ID of this data collection arc
     */
    protected int id;
    /**
     * The arc
     */
    protected Arc2D arc;
    /**
     * The record of the times of the vehicle passing through the arc
     */
    protected Map<Integer, List<Double>> vinToTime;
    /**
     * Whether vehicles should not be counted more than once when it passes
     * through the arc more than once within the NO_REPEAT_TIME_PERIOD.
     */
    protected boolean isNoRepeat;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a data collection arc.
     *
     * @param name       the name of the data collection arc
     * @param id         the ID of the arc
     * @param origin     the X and Y coordinates of the circle
     * @param radius     the radius of the circle
     * @param start      The starting angle of the arc in degrees.
     * @param extent     The angular extent of the arc in degrees.
     * @param type       The closure type for the arc: OPEN, CHORD, or PIE.
     * @param isNoRepeat Whether vehicles should not be counted more than once
     *                   when it passes through the arc more than once within
     *                   the NO_REPEAT_TIME_PERIOD.
     */
    public DataCollectionArc(String name, int id, Point2D origin, double radius, double start, double extent, int type,
                             boolean isNoRepeat) {
        this.name = name;
        this.id = id;
        this.vinToTime = new HashMap<Integer, List<Double>>();
        this.arc = new Arc2D.Double();
        arc.setArcByCenter(origin.getX(), origin.getY(), radius, start, extent, type);
        this.isNoRepeat = isNoRepeat;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the shape of the arc.
     *
     * @return the shape of the arc
     */
    public Shape getShape() {
        return arc;
    }

    /**
     * Whether the vehicle intersects the arc.
     *
     * @param v    the vehicle
     * @param time the current time
     * @param p1   the first point of the vehicle
     * @param p2   the second point of the vehicle
     * @return whether the vehicle intersects the arc
     */
    public boolean intersect(VehicleSimModel v, double time,
                             Point2D p1, Point2D p2) {
        int vin = v.getVIN();
        if (!isNoRepeat
                || !vinToTime.containsKey(vin)
                || vinToTime.get(vin).get(vinToTime.get(vin).size() - 1)
                + NO_REPEAT_TIME_PERIOD < time) {
            if (isPointIntersectingArc(p1, arc) && isPointIntersectingArc(p2, arc)){
                if (!vinToTime.containsKey(vin)) {
                    List<Double> times = new LinkedList<Double>();
                    times.add(time);
                    vinToTime.put(vin, times);
                } else {
                    vinToTime.get(vin).add(time);
                }
                return true;
            } else {
                return false;
            }
        } else {  // the vehicle passed through this data collection arc
            // twice or more within last NO_REPEAT_TIME_PERIOD seconds
            return false;
        }
    }

    /**
     * Get the name of the arc.
     *
     * @return the name of the arc
     */
    public String getName() {
        return name;
    }

    /**
     * Get the ID of the arc.
     *
     * @return the ID of the arc
     */
    public int getId() {
        return id;
    }

    /**
     * Get the VINs of all vehicles.
     *
     * @return the VINs of all vehicles
     */
    public Set<Integer> getAllVIN() {
        return vinToTime.keySet();
    }

    /**
     * Get the time a vehicle passing through the arc.
     *
     * @param vin the VIN of the vehicle
     * @return the time the vehicle passing through the arc
     */
    public List<Double> getTimes(int vin) {
        return vinToTime.get(vin);
    }
}
