package aim4.sim.setup.rim;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.rim.pilot.V2IPilot;
import aim4.im.rim.v2i.reservation.ReservationGridManager;
import aim4.map.rim.RimIntersectionMap;
import aim4.map.rim.RimMapUtil;
import aim4.sim.Simulator;
import aim4.sim.simulator.rim.AutoDriverOnlySimulator;

/**
 * The setup for the simulator in which all vehicles are autonomous.
 */
public class AutoDriverOnlySimSetup extends BasicSimSetup implements RIMSimSetup{
    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////

    /**
     * The time period between the processing times.
     */
    public static final double DEFAULT_PROCESSING_INTERVAL = 2.0;  // seconds

    /**
     * The traffic type.
     */
    public enum TrafficType {
        UNIFORM_RANDOM,
        UNIFORM_TURNBASED,
        FILE,
    }

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /** Whether the base line mode is on */
    private boolean isBaseLineMode = false;
    /** Whether the batch mode is on */
    private boolean isBatchMode = false;
    /** The traffic type */
    private TrafficType trafficType = TrafficType.UNIFORM_RANDOM;
    /** The static buffer size */
    private double staticBufferSize = 0.25;
    /** The time buffer for internal tiles */
    private double internalTileTimeBufferSize = 0.1;
    /** The granularity of the reservation grid */
    private double granularity = 6.0;
    /** The name of the file about the traffic volume */
    private String trafficVolumeFileName = null;

    /////////////////////////////////
    // CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a setup for the simulator in which all vehicles are autonomous.
     *
     * @param basicSimSetup  the basic simulator setup
     */
    public AutoDriverOnlySimSetup(BasicSimSetup basicSimSetup) {
        super(basicSimSetup);
    }

    /**
     * Create a setup for the simulator in which all vehicles are autonomous.
     *
     * @param columns                     the number of columns
     * @param rows                        the number of rows
     * @param roundaboutDiameter          the diameter of the roundabout
     * @param entranceExitRadius          the radius of the entrance & exit circles
     * @param splitFactor                 the number of line lanes in each arc lane
     * @param laneWidth                   the width of lanes
     * @param laneSpeedLimit              the speed limit of the line lanes
     * @param roundaboutSpeedLimit        the speed limit of the roundabout
     * @param lanesPerRoad                the number of lanes per road
     * @param medianSize                  the median size
     * @param distanceBetween             the distance between intersections
     * @param trafficLevel                the traffic level
     * @param stopDistBeforeIntersection  the stopping distance before
     *                                    intersections
     */
    public AutoDriverOnlySimSetup(int columns, int rows,
                                  double roundaboutDiameter,
                                  double entranceExitRadius,
                                  int splitFactor,
                                  double laneWidth,
                                  double laneSpeedLimit,
                                  double roundaboutSpeedLimit,
                                  int lanesPerRoad,
                                  double medianSize,
                                  double distanceBetween,
                                  double trafficLevel,
                                  double stopDistBeforeIntersection) {
        super(columns, rows, roundaboutDiameter, entranceExitRadius, splitFactor, laneWidth, laneSpeedLimit, roundaboutSpeedLimit, lanesPerRoad,
                medianSize, distanceBetween, trafficLevel,
                stopDistBeforeIntersection);
    }
    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Turn on or off the base line mode.
     *
     * @param b  Whether the base line mode is on
     */
    public void setIsBaseLineMode(boolean b) {
        isBaseLineMode = b;
    }


    /**
     * Set the uniform random traffic.
     *
     * @param trafficLevel  the traffic level
     */
    public void setUniformRandomTraffic(double trafficLevel) {
        this.trafficType = TrafficType.UNIFORM_RANDOM;
        this.trafficLevel = trafficLevel;
    }

    /**
     * Set the uniform turn-based traffic.
     *
     * @param trafficLevel the traffic level
     */
    public void setUniformTurnBasedTraffic(double trafficLevel) {
        this.trafficType = TrafficType.UNIFORM_TURNBASED;
        this.trafficLevel = trafficLevel;
    }


    /**
     * Set the traffic volume according to the specification in a file.
     *
     * @param trafficVolumeFileName  the file name of the traffic volume
     */
    public void setTrafficVolume(String trafficVolumeFileName) {
        this.trafficType = TrafficType.FILE;
        this.trafficVolumeFileName = trafficVolumeFileName;
    }

    /**
     * Set the buffer sizes.
     *
     * @param staticBufferSize             the static buffer size
     * @param internalTileTimeBufferSize   the time buffer size of internal tiles
     * @param edgeTileTimeBufferSize       the time buffer size of edge tiles
     * @param isEdgeTileTimeBufferEnabled  whether the edge time buffer is
     *                                     enabled
     * @param granularity                  the granularity of the simulation grid
     */
    public void setBuffers(double staticBufferSize,
                           double internalTileTimeBufferSize,
                           double edgeTileTimeBufferSize,
                           boolean isEdgeTileTimeBufferEnabled,
                           double granularity) {
        this.staticBufferSize = staticBufferSize;
        this.internalTileTimeBufferSize = internalTileTimeBufferSize;
        this.granularity = granularity;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Simulator getSimulator() {
        double currentTime = 0.0;
        RimIntersectionMap layout = new RimIntersectionMap(currentTime,
                numOfColumns,
                numOfRows,
                roundaboutDiameter,
                entranceExitRadius,
                splitFactor,
                laneWidth,
                laneSpeedLimit,
                roundaboutSpeedLimit,
                lanesPerRoad,
                medianSize,
                distanceBetween);
        /* standard */
        ReservationGridManager.Config gridConfig =
                new ReservationGridManager.Config(SimConfig.TIME_STEP,
                        SimConfig.GRID_TIME_STEP,
                        staticBufferSize,
                        internalTileTimeBufferSize,
                        granularity);  // granularity

        /* for demo */
/*
    ReservationGridManager.Config gridConfig =
      new ReservationGridManager.Config(SimConfig.TIME_STEP,
                                        SimConfig.GRID_TIME_STEP,
                                        0.25,  // staticBufferSize
                                        0.0,   // internalTileTimeBufferSize
                                        0.25,   // edgeTileTimeBufferSize
                                        true,  // edgeTileTimeBufferSize
                                        1.0);  // granularity
*/

        /*  for Marvin */
/*
    ReservationGridManager.Config gridConfig =
      new ReservationGridManager.Config(SimConfig.TIME_STEP,
                                        SimConfig.GRID_TIME_STEP,
                                        0.9,  // staticBufferSize
                                        1.2,   // internalTileTimeBufferSize
                                        1.2,   // edgeTileTimeBufferSize
                                        true,  // edgeTileTimeBufferSize
                                        1.0);  // granularity
*/

        Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE = true;

        if (!isBaseLineMode) {
            if (isBatchMode) {
                //TODO: Implement batch mode for rim
//                RimMapUtil.setBatchManagers(layout, currentTime, gridConfig,
//                        processingInterval);
            } else {
                RimMapUtil.setFCFSManagers(layout, currentTime, gridConfig);
            }

            switch(trafficType) {
                case UNIFORM_RANDOM:
                    RimMapUtil.setUniformRandomSpawnPoints(layout, trafficLevel);
                    break;
//                case UNIFORM_TURNBASED:
//                    RimMapUtil.setUniformTurnBasedSpawnPoints(layout, trafficLevel);
//                    break;
//
//                case FILE:
//                    RimMapUtil.setUniformRatioSpawnPoints(layout, trafficVolumeFileName);
//                    break;
            }
        } else {
            RimMapUtil.setFCFSManagers(layout, currentTime, gridConfig);
//            RimMapUtil.setBaselineSpawnPoints(layout, 12.0);
        }


        V2IPilot.DEFAULT_STOP_DISTANCE_BEFORE_INTERSECTION =
                stopDistBeforeIntersection;
        return new AutoDriverOnlySimulator(layout);
    }
}
