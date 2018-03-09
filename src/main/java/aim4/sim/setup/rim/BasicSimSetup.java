package aim4.sim.setup.rim;

import aim4.sim.Simulator;

/**
 * The basic simulator setup
 */
public class BasicSimSetup implements RIMSimSetup{
    /** The number of columns */
    protected int numOfColumns;
    /** The number of rows */
    protected int numOfRows;
    /** The diameter of the roundabout */
    protected double roundaboutDiameter;
    /** The radius of the entrance and exit circles */
    protected double entranceExitRadius;
    /** The number of line lanes each arc lane has */
    protected int splitFactor;
    /** The width of lanes */
    protected double laneWidth;
    /** The speed limit of the line lanes */
    protected double laneSpeedLimit;
    /** The speed limit inside and on-approaching the roundabout */
    protected double roundaboutSpeedLimit;
    /** The number of lanes per road */
    protected int lanesPerRoad;
    /** The width of the area between the opposite directions of a road */
    protected double medianSize;
    /** The distance between intersection */
    protected double distanceBetween;
    /** The traffic level */
    protected double trafficLevel;
    /** The stopping distance before intersection */
    protected double stopDistBeforeIntersection;

    /**
     * Create a copy of a given basic simulator setup.
     *
     * @param basicSimSetup  a basic simulator setup
     */
    public BasicSimSetup(BasicSimSetup basicSimSetup) {
        this.numOfColumns = basicSimSetup.numOfColumns;
        this.numOfRows = basicSimSetup.numOfRows;
        this.roundaboutDiameter = basicSimSetup.roundaboutDiameter;
        this.entranceExitRadius = basicSimSetup.entranceExitRadius;
        this.splitFactor = basicSimSetup.splitFactor;
        this.laneWidth = basicSimSetup.laneWidth;
        this.laneSpeedLimit = basicSimSetup.laneSpeedLimit;
        this.roundaboutSpeedLimit = basicSimSetup.roundaboutSpeedLimit;
        this.lanesPerRoad = basicSimSetup.lanesPerRoad;
        this.medianSize = basicSimSetup.medianSize;
        this.distanceBetween = basicSimSetup.distanceBetween;
        this.trafficLevel = basicSimSetup.trafficLevel;
        this.stopDistBeforeIntersection = basicSimSetup.stopDistBeforeIntersection;
    }

    /**
     * Create a basic simulator setup.
     *
     * @param columns                     the number of columns
     * @param rows                        the number of rows
     * @param roundaboutDiameter          the diameter of the roundabout
     * @param entranceExitRadius          the radius of the entrance & exit circles
     * @param splitFactor                 the number of line lanes in each arc lane
     * @param lanesPerRoad                the number of lanes per road
     * @param laneSpeedLimit              the speed limit of the line lanes
     * @param roundaboutSpeedLimit        the speed limit of the roundabout
     * @param medianSize                  the width of the area between the
     *                                    opposite directions of a road
     * @param distanceBetween             the distance between intersections
     * @param trafficLevel                the traffic level
     * @param stopDistBeforeIntersection  the stopping distance before
     *                                    intersection
     */
    public BasicSimSetup(int columns, int rows, double roundaboutDiameter, double entranceExitRadius, int splitFactor,
                         double laneWidth, double laneSpeedLimit, double roundaboutSpeedLimit,
                         int lanesPerRoad,
                         double medianSize, double distanceBetween,
                         double trafficLevel,
                         double stopDistBeforeIntersection) {
        this.numOfColumns = columns;
        this.numOfRows = rows;
        this.roundaboutDiameter = roundaboutDiameter;
        this.entranceExitRadius = entranceExitRadius;
        this.splitFactor = splitFactor;
        this.laneWidth = laneWidth;
        this.laneSpeedLimit = laneSpeedLimit;
        this.roundaboutSpeedLimit = roundaboutSpeedLimit;
        this.lanesPerRoad = lanesPerRoad;
        this.medianSize = medianSize;
        this.distanceBetween = distanceBetween;
        this.trafficLevel = trafficLevel;
        this.stopDistBeforeIntersection = stopDistBeforeIntersection;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Simulator getSimulator() {
        // TODO: think how to avoid using the following assertation.
        assert false : ("Cannot instantiate BasicSimSetup");
        return null;
    }

    /**
     * Get the number of columns.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return numOfColumns;
    }

    /**
     * Get the number of rows.
     *
     * @return the number of rows
     */
    public int getRows() {
        return numOfRows;
    }

    /**
     * Get the diameter of the roundabout.
     *
     * @return the width of lanes
     */
    public double getRoundaboutDiameter() {
        return roundaboutDiameter;
    }

    /**
     * Get the radius of the entrance and exit circles.
     *
     * @return the width of lanes
     */
    public double getEntranceExitRadius() {
        return entranceExitRadius;
    }

    /**
     * Get the number of line lanes each arc lane has.
     *
     * @return the width of lanes
     */
    public int getSplitFactor() {
        return splitFactor;
    }


    /**
     * Get the width of lanes.
     *
     * @return the width of lanes
     */
    public double getLaneWidth() {
        return laneWidth;
    }

    /**
     * Get the speed limit of the line lanes.
     *
     * @return the speed limit of the roads.
     */
    public double getLaneSpeedLimit() {
        return laneSpeedLimit;
    }

    /**
     * Get the speed limit inside and on approaching the roundabout.
     *
     * @return the speed limit of the roads.
     */
    public double getRoundaboutSpeedLimit() {
        return roundaboutSpeedLimit;
    }

    /**
     * Get the number of lanes per road.
     *
     * @return the number of lanes per road
     */
    public int getLanesPerRoad() {
        return lanesPerRoad;
    }

    /**
     * Get the width of the area between the opposite directions of a road.
     *
     * @return the width of the area between the opposite directions of a road
     */
    public double getMedianSize() {
        return medianSize;
    }

    /**
     * Get the distance between intersections.
     *
     * @return the distance between intersections
     */
    public double getDistanceBetween() {
        return distanceBetween;
    }

    /**
     * Get the traffic level.
     *
     * @return the traffic level
     */
    public double getTrafficLevel() {
        return trafficLevel;
    }

    /**
     * Get the stopping distance before intersection.
     *
     * @return the stopping distance before intersection
     */
    public double getStopDistBeforeIntersection() {
        return stopDistBeforeIntersection;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public void setTrafficLevel(double trafficLevel) {
        this.trafficLevel = trafficLevel;
    }

    /**
     * Set the roundabout diameter
     *
     * @param roundaboutDiameter  the roundabout diameter
     */
    public void setRoundaboutDiameter(double roundaboutDiameter) {
        this.roundaboutDiameter = roundaboutDiameter;
    }


    /**
     * Set the speed limit.
     *
     * @param speedLimit  the speed limit
     */
    public void setLaneSpeedLimit(double speedLimit) {
        this.laneSpeedLimit = speedLimit;
    }

    // TODO: maybe move to AutoDriverOnlySimSetup

    /**
     * Set the speed limit of the roundabout.
     *
     * @param speedLimit  the speed limit
     */
    public void setRoundaboutSpeedLimit(double speedLimit) {
        this.roundaboutSpeedLimit = speedLimit;
    }

    // TODO: maybe move to AutoDriverOnlySimSetup

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStopDistBeforeIntersection(double stopDistBeforeIntersection) {
        this.stopDistBeforeIntersection = stopDistBeforeIntersection;
    }

    /**
     * Set the number of columns.
     *
     * @param numOfColumns the number of columns
     */
    public void setNumOfColumns(int numOfColumns) {
        this.numOfColumns = numOfColumns;
    }

    /**
     * Set the number of rows.
     *
     * @param numOfRows the number of rows
     */
    public void setNumOfRows(int numOfRows) {
        this.numOfRows = numOfRows;
    }

    /**
     * Set the number of lanes per road.
     *
     * @param lanesPerRoad  the number of lanes per road
     */
    public void setLanesPerRoad(int lanesPerRoad) {
        this.lanesPerRoad = lanesPerRoad;
    }
}
