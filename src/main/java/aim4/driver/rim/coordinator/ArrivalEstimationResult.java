package aim4.driver.rim.coordinator;

import aim4.vehicle.AccelSchedule;

/**
 * The result of arrival estimations.
 */
public class ArrivalEstimationResult {

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The arrival time.
     */
    double arrivalTime;

    /**
     * The arrival velocity.
     */
    double arrivalVelocity;

    /**
     * The acceleration schedule.
     */
    AccelSchedule accelSchedule;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Construct an arrival estimation result object.
     *
     * @param arrivalTime      the arrival time
     * @param arrivalVelocity  the arrival velocity
     * @param accelSchedule    the acceleration schedule
     */
    public ArrivalEstimationResult(double arrivalTime,
                                   double arrivalVelocity,
                                   AccelSchedule accelSchedule) {
        this.arrivalTime = arrivalTime;
        this.arrivalVelocity = arrivalVelocity;
        this.accelSchedule = accelSchedule;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the arrival time.
     *
     * @return the arrival time.
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Get the arrival velocity.
     *
     * @return the arrival velocity.
     */
    public double getArrivalVelocity() {
        return arrivalVelocity;
    }

    /**
     * Get the acceleration schedule.
     *
     * @return the acceleration schedule.
     */
    public AccelSchedule getAccelSchedule() {
        return accelSchedule;
    }
}
