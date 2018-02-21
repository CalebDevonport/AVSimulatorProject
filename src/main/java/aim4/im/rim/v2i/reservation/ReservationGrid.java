package aim4.im.rim.v2i.reservation;

import aim4.config.Constants;

import java.util.List;
import java.util.Set;

/**
 * The reservation grid.
 */
public class ReservationGrid extends ReservationArray{
    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The period of time between two clean up of the tile reservation
     * tables.
     */
    private static final int TILE_RESERVATION_TABLE_CLEAN_UP_PERIOD = 30;

    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The time tile.
     */
    public class TimeTile extends ReservationArray.TimeTile {

        /**
         * Create a time tile.
         *
         * @param dt   the tile ID
         * @param tid  the time ID
         */
        public TimeTile(int dt, int tid) {
            super(dt, tid);
        }

        /**
         * Get the time of this time tile.
         *
         * @return the time of this time tile
         */
        public double getTime() {
            return getDiscreteTime() * gridTimeStep;
        }

        /**
         * Convert this time-tile to a string representation
         */
        @Override
        public String toString() {
            return "TT(" + getTileId() + "," +
                    Constants.TWO_DEC.format(getTime()) + ")";
        }

    }
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The granularity of the map.
     */
    private final double granularity;

    /**
     * The time step
     */
    private final double gridTimeStep;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a reservation grid.
     * @param granularity  the granularity of the map
     * @param gridTimeStep  the time step.
     */
    public ReservationGrid(double granularity, double gridTimeStep) {
        super(2 * (int) granularity);
        this.granularity = granularity;
        this.gridTimeStep = gridTimeStep;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Clean up the reservation grid.
     *
     * @param currentTime the current time
     */
    public void cleanUp(double currentTime) {
        // clean up
        int currentDiscreteTime = calcDiscreteTime(currentTime);
        if (currentDiscreteTime % TILE_RESERVATION_TABLE_CLEAN_UP_PERIOD == 0) {
            cleanUp(currentDiscreteTime);
        }
    }

    /**
     * Get the discrete time of a given time.  If the given time is not
     * exactly equal to the discrete time, the largest discrete time that
     * is smaller or equal to the given time will be returned.
     *
     * @param time  the time
     * @return the discrete time
     */
    public int calcDiscreteTime(double time) {
        return (int)(time/gridTimeStep);
    }

    /**
     * Get the granularity of the map.
     *
     * @return the granularity of the map
     */
    public double getGranularity() { return granularity; }

    /**
     * Get the time step.
     *
     * @return the time step
     */
    public double getGridTimeStep() {
        return gridTimeStep;
    }

    /**
     * Get the remaining time in the grid time step of the given time
     *
     * @param time  the time
     * @return the remaining time in the grid time step
     */
    public double calcRemainingTime(double time) {
        return time - gridTimeStep * calcDiscreteTime(time);
    }

    /**
     * Get the time of a given discrete time.
     *
     * @param discreteTime  the time
     * @return the time
     */
    public double calcTime(int discreteTime) {
        return discreteTime * gridTimeStep;
    }

    /**
     * Get the last time at which any time-tile has been reserved.
     *
     * @return the last time at which any time-tile has been reserved;
     *         -1 if there is currently no reservation.
     */
    public double getLastReservedTime() {
        return super.getLastReservedDiscreteTime() * gridTimeStep;
    }

    /**
     * Get the set of all reserved tiles at a given discrete time.
     *
     * @param time  the time
     * @return the list of tile IDs that are reserved at the given discrete time.
     */
    public List<Integer> getReservedTilesAtTime(double time) {
        return super.getReservedTilesAtTime(calcDiscreteTime(time));
    }

    /**
     * Get the VINs of all reserved tiles at a given discrete time.
     *
     * @param time  the time
     * @return a set of reservation IDs.
     */
    public Set<Integer> getVinOfReservedTilesAtTime(double time) {
        return super.getVinOfReservedTilesAtTime(calcDiscreteTime(time));
    }
}
