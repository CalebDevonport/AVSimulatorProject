package aim4.map.aim;

import aim4.config.Debug;
import aim4.im.aim.IntersectionManager;
import aim4.map.BasicAIMIntersectionMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.util.ArrayListRegistry;
import aim4.util.GeomMath;
import aim4.util.Registry;
import aim4.vehicle.VinRegistry;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

public class GridRIMIntersectionMap extends GridAIMIntersectionMap implements BasicAIMIntersectionMap {

    /////////////////////////////////
    // CONSTANTS
    /////////////////////////////////

    /**
     * The length of the no vehicle zone
     */
    private static final double NO_VEHICLE_ZONE_LENGTH = 28.0;
    // private static final double NO_VEHICLE_ZONE_LENGTH = 10.0;

    /**
     * The position of the data collection line on a lane
     */
    private static final double DATA_COLLECTION_LINE_POSITION =
            NO_VEHICLE_ZONE_LENGTH;

    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The number of rows
     */
    private int rows;
    /**
     * The number of columns
     */
    private int columns;
    /**
     * The dimensions of the map
     */
    private Rectangle2D dimensions;
    /**
     * The set of roads
     */
    private List<Road> roads;
    /**
     * The set of horizontal roads
     */
    private List<Road> horizontalRoads = new ArrayList<Road>();
    /**
     * The set of vertical roads
     */
    private List<Road> verticalRoads = new ArrayList<Road>();
    /**
     * The list of intersection managers
     */
    private List<IntersectionManager> intersectionManagers;
    /**
     * The array of intersection managers
     */
    private IntersectionManager[][] intersectionManagerGrid;
    /**
     * The maximum speed limit
     */
    private double memoMaximumSpeedLimit = -1;
    /**
     * The data collection lines
     */
    private List<DataCollectionLine> dataCollectionLines;
    /**
     * The spawn points
     */
    private List<AIMSpawnPoint> spawnPoints;
    /**
     * The horizontal spawn points
     */
    private List<AIMSpawnPoint> horizontalSpawnPoints;
    /**
     * The vertical spawn points
     */
    private List<AIMSpawnPoint> verticalSpawnPoints;
    /**
     * The lane registry
     */
    private Registry<Lane> laneRegistry =
            new ArrayListRegistry<Lane>();
    /**
     * The IM registry
     */
    private Registry<IntersectionManager> imRegistry =
            new ArrayListRegistry<IntersectionManager>();
    /**
     * A mapping form lanes to roads they belong
     */
    private Map<Lane, Road> laneToRoad = new HashMap<Lane, Road>();

    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Create a grid map.
     *
     * @param initTime                  the initial time
     * @param columns                   the number of columns (number of vertical sets of roads)
     * @param rows                      the number of rows (number of horizontal sets of roads)
     * @param laneWidth                 the lane width
     * @param speedLimit                the speed limit
     * @param lanesPerRoad              the number of lanes per road
     * @param widthBetweenOppositeRoads the width of the area between the roads in opposite
     *                                  direction
     * @param distanceBetween           the distance between the adjacent intersections
     */
    public GridRIMIntersectionMap(double initTime, int columns, int rows,
                                  double laneWidth, double speedLimit, int lanesPerRoad,
                                  double widthBetweenOppositeRoads, double distanceBetween) {
        // Can't make these unless there is at least one row and column
        if (rows < 1 || columns < 1) {
            throw new IllegalArgumentException("Must have at least one column " +
                    "and row!");
        }
        this.columns = columns;
        this.rows = rows;
        distanceBetween = 0; // the case for multiple intersections is not handled

        // Generate the size of the map.
        // The map is 250x250 similarly with RIM
        double height = 250;
        double width = 250;
        dimensions = new Rectangle2D.Double(0, 0, width, height);

        dataCollectionLines = new ArrayList<DataCollectionLine>();

        // Create the vertical Roads
        for (int column = 0; column < columns; column++) {
            double roadMiddleX = height/ 2;

            // First create the right road (northbound)
            Road right =
                    new Road(GeomMath.ordinalize(column + 1) + " Avenue N", this);
            // Add the lanes to the road
            for (int i = 0; i < lanesPerRoad; i++) {
                double x = roadMiddleX + // Start in the middle
                        (i * laneWidth) + // Move down for each lane we've done
                        (laneWidth + widthBetweenOppositeRoads) / 2; // Get to the lane center
                Lane l = new LineSegmentLane(x, // x1
                        height, // y1
                        x, // x2
                        0, // y2
                        laneWidth, // width
                        speedLimit);
                int laneId = laneRegistry.register(l);
                l.setId(laneId);
                right.addTheRightMostLane(l);
                laneToRoad.put(l, right);
            }
            verticalRoads.add(right);

            // generate the data collection lines
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "NorthBound" + column + "Entrance",
                            dataCollectionLines.size(),
                            new Point2D.Double(roadMiddleX,
                                    height - DATA_COLLECTION_LINE_POSITION),
                            new Point2D.Double(roadMiddleX + lanesPerRoad * laneWidth + widthBetweenOppositeRoads,
                                    height - DATA_COLLECTION_LINE_POSITION),
                            true));
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "NorthBound" + column + "Exit",
                            dataCollectionLines.size(),
                            new Point2D.Double(roadMiddleX,
                                    DATA_COLLECTION_LINE_POSITION),
                            new Point2D.Double(roadMiddleX + lanesPerRoad * laneWidth + widthBetweenOppositeRoads,
                                    DATA_COLLECTION_LINE_POSITION),
                            true));

            // Now create the left (southbound)
            Road left = new Road(GeomMath.ordinalize(column + 1) + " Avenue S", this);
            for (int i = 0; i < lanesPerRoad; i++) {
                double x = roadMiddleX - // Start in the middle
                        (i * laneWidth) - // Move up for each lane we've done
                        (laneWidth + widthBetweenOppositeRoads) / 2; // Get to the lane center
                Lane l = new LineSegmentLane(x, // x1
                        0, // y1
                        x, // x2
                        height, // y2
                        laneWidth, // width
                        speedLimit);
                int laneId = laneRegistry.register(l);
                l.setId(laneId);
                left.addTheRightMostLane(l);
                laneToRoad.put(l, left);
            }
            verticalRoads.add(left);

            // generate the data collection lines
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "SouthBound" + column + "Entrance",
                            dataCollectionLines.size(),
                            new Point2D.Double(roadMiddleX,
                                    DATA_COLLECTION_LINE_POSITION),
                            new Point2D.Double(roadMiddleX - lanesPerRoad * laneWidth - widthBetweenOppositeRoads,
                                    DATA_COLLECTION_LINE_POSITION),
                            true));
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "SouthBound" + column + "Exit",
                            dataCollectionLines.size(),
                            new Point2D.Double(roadMiddleX,
                                    height - DATA_COLLECTION_LINE_POSITION),
                            new Point2D.Double(roadMiddleX - lanesPerRoad * laneWidth - widthBetweenOppositeRoads,
                                    height - DATA_COLLECTION_LINE_POSITION),
                            true));

            // Set up the "dual" relationship
            right.setDual(left);
        }

        // Create the horizontal Roads
        for (int row = 0; row < rows; row++) {
            double roadMiddleY = width / 2;
            // First create the lower (eastbound)
            Road lower = new Road(GeomMath.ordinalize(row + 1) + " Street E", this);
            for (int i = 0; i < lanesPerRoad; i++) {
                double y = roadMiddleY + // Start in the middle
                        (i * laneWidth) + // Move up for each lane we've done
                        (laneWidth + widthBetweenOppositeRoads) / 2; // Get to the lane center
                Lane l = new LineSegmentLane(0, // x1
                        y, // y1
                        width, // x2
                        y, // y2
                        laneWidth, // width
                        speedLimit);
                int laneId = laneRegistry.register(l);
                l.setId(laneId);
                lower.addTheRightMostLane(l);
                laneToRoad.put(l, lower);
            }
            horizontalRoads.add(lower);

            // generate the data collection lines
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "EastBound" + row + "Entrance",
                            dataCollectionLines.size(),
                            new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY),
                            new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY + lanesPerRoad * laneWidth + widthBetweenOppositeRoads),
                            true));
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "EastBound" + row + "Exit",
                            dataCollectionLines.size(),
                            new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY),
                            new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY + lanesPerRoad * laneWidth + widthBetweenOppositeRoads),
                            true));


            // Now create the upper (westbound)
            Road upper = new Road(GeomMath.ordinalize(row + 1) + " Street W", this);
            for (int i = 0; i < lanesPerRoad; i++) {
                double y = roadMiddleY - // Start in the middle
                        (i * laneWidth) - // Move down for each lane we've done
                        (laneWidth + widthBetweenOppositeRoads) / 2; // Get to the lane center
                Lane l = new LineSegmentLane(width, // x1
                        y, // y1
                        0, // x2
                        y, // y2
                        laneWidth, // width
                        speedLimit);
                int laneId = laneRegistry.register(l);
                l.setId(laneId);
                upper.addTheRightMostLane(l);
                laneToRoad.put(l, upper);
            }
            horizontalRoads.add(upper);

            // generate the data collection lines
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "WestBound" + row + "Entrance",
                            dataCollectionLines.size(),
                            new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY),
                            new Point2D.Double(width - DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY - lanesPerRoad * laneWidth - widthBetweenOppositeRoads),
                            true));
            dataCollectionLines.add(
                    new DataCollectionLine(
                            "WestBound" + row + "Exit",
                            dataCollectionLines.size(),
                            new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY),
                            new Point2D.Double(DATA_COLLECTION_LINE_POSITION,
                                    roadMiddleY - lanesPerRoad * laneWidth - widthBetweenOppositeRoads),
                            true));

            // Set up the "dual" relationship
            lower.setDual(upper);
        }

        roads = new ArrayList<Road>(horizontalRoads);
        roads.addAll(verticalRoads);
        roads = Collections.unmodifiableList(roads);

        // We should have columns * rows intersections, so make space for 'em
        intersectionManagers = new ArrayList<IntersectionManager>(columns * rows);
        intersectionManagerGrid = new IntersectionManager[columns][rows];

        initializeSpawnPoints(initTime);
    }

    public GridRIMIntersectionMap(GridAIMIntersectionMap map, double laneWidth,
                                  double widthBetweenOppositeRoads, double distanceBetween) {
        this(0, map.getColumns(), map.getRows(),
                laneWidth, map.getMaximumSpeedLimit(),
                map.getRoads().get(0).getLanes().size(),
                widthBetweenOppositeRoads, distanceBetween);
    }

    /**
     * Initialize spawn points.
     *
     * @param initTime the initial time
     */
    private void initializeSpawnPoints(double initTime) {
        spawnPoints = new ArrayList<AIMSpawnPoint>(columns + rows);
        horizontalSpawnPoints = new ArrayList<AIMSpawnPoint>(rows);
        verticalSpawnPoints = new ArrayList<AIMSpawnPoint>(columns);

        for (Road road : horizontalRoads) {
            for (Lane lane : road.getLanes()) {
                horizontalSpawnPoints.add(makeSpawnPoint(initTime, lane));
            }
        }

        for (Road road : verticalRoads) {
            for (Lane lane : road.getLanes()) {
                verticalSpawnPoints.add(makeSpawnPoint(initTime, lane));
            }
        }

        spawnPoints.addAll(horizontalSpawnPoints);
        spawnPoints.addAll(verticalSpawnPoints);

        Debug.currentAimMap = this;
    }

    /**
     * Initialize one spawn point on the eastbound road.
     * Added this method so we can follow one driver agent when debugging.
     * Useful to understand more how their driver agent works.
     *
     * @param initTime the initial time
     */
    private void initializeOneSpawnPoint(double initTime) {
        if (rows > 1 || columns > 1) {
            throw new IllegalArgumentException("Undefined behaviour with one spawn point");
        }
        spawnPoints = new ArrayList<AIMSpawnPoint>(1);
        horizontalSpawnPoints = new ArrayList<AIMSpawnPoint>(1);

        Lane lane = horizontalRoads.get(0).getLanes().get(0);
        horizontalSpawnPoints.add(makeSpawnPoint(initTime, lane));

        spawnPoints.addAll(horizontalSpawnPoints);

        Debug.currentAimMap = this;
    }

    /**
     * Make spawn points.
     *
     * @param initTime the initial time
     * @param lane     the lane
     * @return the spawn point
     */
    private AIMSpawnPoint makeSpawnPoint(double initTime, Lane lane) {
        double startDistance = 0.0;
        double normalizedStartDistance = lane.normalizedDistance(startDistance);
        Point2D pos = lane.getPointAtNormalizedDistance(normalizedStartDistance);
        double heading = lane.getInitialHeading();
        double steeringAngle = 0.0;
        double acceleration = 0.0;
        double d = lane.normalizedDistance(startDistance + NO_VEHICLE_ZONE_LENGTH);
        Rectangle2D noVehicleZone =
                lane.getShape(normalizedStartDistance, d).getBounds2D();

        return new AIMSpawnPoint(initTime, pos, heading, steeringAngle, acceleration,
                lane, noVehicleZone);
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Road> getRoads() {
        return roads;
    }

    /**
     * Get road by name.
     */
    public Road getRoadByName(String nameOfRoad) {
        final Road[] roadByName = {null};
        roads.forEach(road -> {
            if (road.getName().compareTo(nameOfRoad) == 0) {
                roadByName[0] = road;
            }
        });
        return roadByName[0];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Road> getDestinationRoads() {
        return roads;
    }


    /**
     * {@inheritDoc}
     */
    @Override
    public Rectangle2D getDimensions() {
        return dimensions;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public double getMaximumSpeedLimit() {
        if (memoMaximumSpeedLimit < 0) {
            for (Road r : getRoads()) {
                for (Lane l : r.getLanes()) {
                    if (l.getSpeedLimit() > memoMaximumSpeedLimit) {
                        memoMaximumSpeedLimit = l.getSpeedLimit();
                    }
                }
            }
        }
        return memoMaximumSpeedLimit;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<IntersectionManager> getIntersectionManagers() {
        return Collections.unmodifiableList(intersectionManagers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DataCollectionLine> getDataCollectionLines() {
        return dataCollectionLines;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AIMSpawnPoint> getSpawnPoints() {
        return spawnPoints;
    }

    /**
     * Get the list of horizontal spawn points.
     *
     * @return the list of horizontal spawn points
     */
    public List<AIMSpawnPoint> getHorizontalSpawnPoints() {
        return horizontalSpawnPoints;
    }


    /**
     * Get the list of vertical spawn points.
     *
     * @return the list of vertical spawn points
     */
    public List<AIMSpawnPoint> getVerticalSpawnPoints() {
        return verticalSpawnPoints;
    }

    /////////////////////////////////////////////
    // PUBLIC METHODS  (specific to Grid Layout)
    /////////////////////////////////////////////

    /**
     * Get the number of rows in this grid layout.
     *
     * @return the number of rows
     */
    public int getRows() {
        return rows;
    }

    /**
     * Get the number of columns in this grid layout.
     *
     * @return the number of columns
     */
    public int getColumns() {
        return columns;
    }


    /**
     * Get the list of all roads that enter a particular intersection.
     *
     * @param column the column of the intersection
     * @param row    the row of the intersection
     * @return the list of roads that enter the intersection at (column, row)
     */
    public List<Road> getRoads(int column, int row) {
        // First some error checking
        if (row >= rows || column >= columns || row < 0 || column < 0) {
            throw new ArrayIndexOutOfBoundsException("(" + column + "," + row +
                    " are not valid indices. " +
                    "This GridLayout only has " +
                    column + " columns and " +
                    row + " rows.");
        }
        List<Road> answer = new ArrayList<Road>();
        answer.add(verticalRoads.get(2 * column));
        answer.add(verticalRoads.get(2 * column + 1));
        answer.add(horizontalRoads.get(2 * row));
        answer.add(horizontalRoads.get(2 * row + 1));
        return answer;
    }

    /**
     * Get the set of horizontal roads.
     *
     * @return the set of horizontal roads
     */
    public List<Road> getHorizontalRoads() {
        return horizontalRoads;
    }

    /**
     * Get the set of vertical roads.
     *
     * @return the set of vertical roads
     */
    public List<Road> getVerticalRoads() {
        return verticalRoads;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<IntersectionManager> getImRegistry() {
        return imRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Registry<Lane> getLaneRegistry() {
        return laneRegistry;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Road getRoad(Lane lane) {
        return laneToRoad.get(lane);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Road getRoad(int laneID) {
        return laneToRoad.get(laneRegistry.get(laneID));
    }

    /**
     * Get the intersection manager of a particular intersection.
     *
     * @param column the column of the intersection
     * @param row    the row of the intersection
     */
    public IntersectionManager getManager(int column, int row) {
        return intersectionManagerGrid[column][row];
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setManager(int column, int row, IntersectionManager im) {
        // Barf if this is already set
        if (intersectionManagerGrid[column][row] != null) {
            throw new RuntimeException("The intersection manager at (" + column +
                    ", " + row + ") has already been set!");
        }
        intersectionManagerGrid[column][row] = im;
        intersectionManagers.add(im);
    }


    /**
     * Remove managers in all intersections.
     */
    public void removeAllManagers() {
        for (int column = 0; column < columns; column++) {
            for (int row = 0; row < rows; row++) {
                intersectionManagerGrid[column][row] = null;
            }
        }
        intersectionManagers.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void printDataCollectionLinesData(String outFileName) {
        PrintStream outfile = null;
        try {
            outfile = new PrintStream(outFileName);
        } catch (FileNotFoundException e) {
            System.err.printf("Cannot open file %s\n", outFileName);
            return;
        }
        // TODO: sort by time and LineId and VIN
        outfile.printf("VIN,Time,DCLname,vType,startLaneId,destRoad\n");
        for (DataCollectionLine line : dataCollectionLines) {
            for (int vin : line.getAllVIN()) {
                for (double time : line.getTimes(vin)) {
                    outfile.printf("%d,%.4f,%s,%s,%d,%s\n",
                            vin, time, line.getName(),
                            VinRegistry.getVehicleSpecFromVIN(vin).getName(),
                            VinRegistry.getSpawnPointFromVIN(vin).getLane().getId(),
                            VinRegistry.getDestRoadFromVIN(vin).getName());
                }
            }
        }

        outfile.close();
    }
}
