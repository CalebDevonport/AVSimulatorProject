package aim4.map.rim;

import aim4.map.Road;
import aim4.map.SpawnPoint;
import aim4.map.lane.Lane;
import aim4.vehicle.VehicleSpec;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * A spawn point for rim simulations.
 */
public class RIMSpawnPoint extends SpawnPoint{

    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /** The specification of a spawn */
    public static class RIMSpawnSpec extends SpawnSpec {
        /** The destination road */
        Road destinationRoad;

        /**
         * Create a spawn specification.
         *
         * @param spawnTime       the spawn time
         * @param vehicleSpec     the vehicle specification
         * @param destinationRoad the destination road
         */
        public RIMSpawnSpec(double spawnTime, VehicleSpec vehicleSpec, Road destinationRoad) {
            super(spawnTime, vehicleSpec);
            this.destinationRoad = destinationRoad;
        }

        /**
         * Get the destination road.
         *
         * @return the destination road
         */
        public Road getDestinationRoad() {
            return destinationRoad;
        }
    }

    /**
     * The interface of the spawn specification generator.
     */
    public static interface RIMSpawnSpecGenerator {
        /**
         * Advance the time step.
         *
         * @param spawnPoint  the spawn point
         * @param timeStep    the time step
         * @return the list of spawn spec generated in this time step.
         */
        List<RIMSpawnSpec> act(RIMSpawnPoint spawnPoint, double timeStep);
    }

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** The vehicle spec chooser */
    private RIMSpawnSpecGenerator vehicleSpecChooser;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a spawn point.
     *
     * @param currentTime         the current time
     * @param pos                 the initial position
     * @param heading             the initial heading
     * @param steeringAngle       the initial steering angle
     * @param acceleration        the initial acceleration
     * @param lane                the lane
     * @param noVehicleZone       the no vehicle zone
     * @param vehicleSpecChooser  the vehicle spec chooser
     */
    public RIMSpawnPoint(double currentTime,
                         Point2D pos,
                         double heading,
                         double steeringAngle,
                         double acceleration,
                         Lane lane,
                         Rectangle2D noVehicleZone,
                         RIMSpawnSpecGenerator vehicleSpecChooser) {
        super(currentTime, pos, heading, steeringAngle,acceleration, lane, noVehicleZone);
        this.vehicleSpecChooser = vehicleSpecChooser;
    }

    /**
     * Create a spawn point.
     *
     * @param currentTime         the current time
     * @param pos                 the initial position
     * @param heading             the initial heading
     * @param steeringAngle       the initial steering angle
     * @param acceleration        the initial acceleration
     * @param lane                the lane
     * @param noVehicleZone       the no vehicle zone
     */
    public RIMSpawnPoint(double currentTime,
                         Point2D pos,
                         double heading,
                         double steeringAngle,
                         double acceleration,
                         Lane lane,
                         Rectangle2D noVehicleZone) {
        super(currentTime, pos, heading, steeringAngle, acceleration, lane, noVehicleZone);
        this.vehicleSpecChooser = null;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Advance the time step.
     *
     * @param timeStep  the time step
     * @return The list of spawn spec generated in this time step
     */
    public List<RIMSpawnSpec> act(double timeStep) {
        assert vehicleSpecChooser != null;
        List<RIMSpawnSpec> spawnSpecs = vehicleSpecChooser.act(this, timeStep);
        currentTime += timeStep;
        return spawnSpecs;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Set the vehicle spec chooser.
     *
     * @param vehicleSpecChooser the vehicle spec chooser
     */
    public void setVehicleSpecChooser(RIMSpawnSpecGenerator vehicleSpecChooser) {
        // assert this.vehicleSpecChooser == null;  // TODO think whether it is okay
        this.vehicleSpecChooser = vehicleSpecChooser;
    }
}
