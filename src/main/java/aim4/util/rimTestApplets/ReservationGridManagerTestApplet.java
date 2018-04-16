package aim4.util.rimTestApplets;

import aim4.config.Constants;
import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.driver.Driver;
import aim4.driver.rim.CrashTestDummy;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.v2i.reservation.ReservationGrid;
import aim4.im.rim.v2i.reservation.ReservationGridManager;
import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;
import aim4.msg.rim.v2i.Request;
import aim4.util.TiledRimArea;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.VehicleUtil;
import aim4.vehicle.aim.AIMBasicAutoVehicle;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ReservationGridManagerTestApplet extends Applet implements Runnable{
    // Roundabout properties
    private static final double MAP_WIDTH =  250;
    private static final double MAP_HEIGHT =  250;
    private static final double ARRIVAL_TIME = 4.0;
    private static final double STATIC_BUFFER_SIZE = 0.25;
    private static final double INTERNAL_TILE_TIME_BUFFER_SIZE = 0.1;
    private static final double GRANULARITY = 8.0;
    private static final List<Double> ROUNDABOUT_DIAMETER = Arrays.asList(30.0, 35.0, 40.0, 45.0);
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  9.7222;
    private static final int VIN =  1;

    // Colors taken from AIM Canvas
    private static final Color STRICT_AREA_COLOR = Color.BLUE;
    private static final Color ROAD_AREA_COLOR = Color.PINK;
    private static final Stroke ROAD_BOUNDARY_STROKE = new BasicStroke(0.3f);
    private static final Stroke ROAD_BOUNDARY_STROKE_2 = new BasicStroke(0.7f);
    private static final AffineTransform IDENTITY_TRANSFORM =
            new AffineTransform();
    private static final Color GRASS_COLOR = Color.GREEN.darker().darker();
    private static final String GRASS_TILE_FILE = "/images/grass128.png";

    public static int[] xLeadPoints = new int[1000];
    public static int[] yLeadPoints = new int[1000];

    public void paint(Graphics g) {
        // Create the RIM Map
        RimIntersectionMap map = new RimIntersectionMap(
                0,
                1,
                1,
                ROUNDABOUT_DIAMETER.get(0),
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);

        double scaleFactor = 18;
        Rectangle2D mapRect = new Rectangle2D.Double(0,0,MAP_WIDTH, MAP_HEIGHT);
        //Create Graphics2D object, cast g as a Graphics2D
        Graphics2D bgBuffer = (Graphics2D) g;

        // Apply AIM transformation
        AffineTransform tf = new AffineTransform();
        tf.translate(-1200, -1200);
        tf.scale(scaleFactor, scaleFactor);
        bgBuffer.setTransform(tf);

        // Set map colors
        BufferedImage grassImage = loadImage(GRASS_TILE_FILE);
        TexturePaint grassTexture = makeScaledTexture(grassImage, scaleFactor);
        paintEntireBuffer(bgBuffer, Color.RED);
        drawGrass(bgBuffer, mapRect, grassTexture);

        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE_2);
        bgBuffer.setPaint(STRICT_AREA_COLOR);

        // Create config
        ReservationGridManager.Config config = new ReservationGridManager.Config(SimConfig.TIME_STEP,
                SimConfig.GRID_TIME_STEP,
                STATIC_BUFFER_SIZE,
                INTERNAL_TILE_TIME_BUFFER_SIZE,
                GRANULARITY);

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create tiles for intersection
        TiledRimArea tiledRimArea = new TiledRimArea(
                roadBasedIntersection.getMinimalCircle(),
                roadBasedIntersection.getMaximalCircle(),
                GRANULARITY);

        // Assign the tiles times
        ReservationGrid reservationGrid = new ReservationGrid(GRANULARITY, SimConfig.GRID_TIME_STEP);

        // Set arrival and departure lanes
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane exitLane = getWestRoad().getExitApproachLane();

        // Set vehicle spec
        Request.VehicleSpecForRequestMsg vehicleSpecForRequestMsg = new Request.VehicleSpecForRequestMsg(VehicleSpecDatabase.getVehicleSpecByName("VAN"));

        // Set vehicle's driver query
        ReservationGridManager.Query query = new ReservationGridManager.Query(
                VIN,
                ARRIVAL_TIME,
                ROUNDABOUT_SPEED_LIMIT, // arrival velocity
                arrivalLane.getId(),
                exitLane.getId(),
                vehicleSpecForRequestMsg,
                ROUNDABOUT_SPEED_LIMIT, // max turn velocity
                false); // acceleration allowed


        // Position the Vehicle to be ready to start the simulation
        ArcSegmentLane arrivalLaneArc = (ArcSegmentLane) arrivalLane;
        ArcSegmentLane departureLaneArc = (ArcSegmentLane) exitLane;

        // Create a test vehicle to use in the internal simulation
        AIMBasicAutoVehicle testVehicle =
                createTestVehicle(query.getSpec(),
                        query.getArrivalVelocity(),
                        query.getMaxTurnVelocity(),
                        arrivalLaneArc);

        // Create a dummy driver to steer it
        Driver dummy = new CrashTestDummy(testVehicle, arrivalLaneArc, departureLaneArc);

        // Draw roads
        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);
        bgBuffer.setPaint(ROAD_AREA_COLOR);
        for(Road road : map.getRoads()) {
            Area roadArea = new Area();
            // Find the union of the shapes of the lanes for each road
            for(Lane lane : road.getContinuousLanes()) {
                // Add the area from each constituent lane
                roadArea.add(new Area(lane.getShape()));
            }
            bgBuffer.draw(roadArea);
        }

        // Draw the tiles
        tiledRimArea.getAllTilesById().forEach( tile -> {
            bgBuffer.draw(tile.getArea());
            float hue = tile.getId()/15f;
            bgBuffer.setPaint(Color.getHSBColor(hue, 1.0f,0.8f));
        });

        // Keep track of the TileTimes that will make up this reservation
        findTileTimesBySimulation(bgBuffer,
                (ArcSegmentLane)arrivalLane,
                testVehicle,
                dummy,
                query.getArrivalTime(),
                query.isAccelerating(),
                roadBasedIntersection,
                reservationGrid,
                tiledRimArea,
                config);

    }

    public void run() {
        //repaint();
    }

    private TexturePaint makeScaledTexture(BufferedImage image, double scale) {
        if (image != null) {
            // Make sure to scale it properly so it doesn't get all distorted
            Rectangle2D textureRect =
                    new Rectangle2D.Double(0, 0,
                            image.getWidth() / scale,
                            image.getHeight() / scale);
            // Now set up an easy-to-refer-to texture.
            return new TexturePaint(image, textureRect);
        } else {
            return null;
        }
    }

    private BufferedImage loadImage(String imageFileName) {
        InputStream is = this.getClass().getResourceAsStream(imageFileName);
        BufferedImage image = null;
        if (is != null) {
            try {
                image = ImageIO.read(is);
            } catch (IOException e) {
                image = null;
            }
        }
        return image;
    }

    private void paintEntireBuffer(Graphics2D buffer, Color color) {
        AffineTransform tf = buffer.getTransform();
        // set the transform
        buffer.setTransform(IDENTITY_TRANSFORM);
        // paint
        buffer.setPaint(color); // no need to set the stroke
        buffer.fillRect(0, 0, getSize().width, getSize().height);
        // Restore the original transform.
        buffer.setTransform(tf);
    }

    private void drawGrass(Graphics2D buffer,
                             Rectangle2D rect,
                             TexturePaint grassTexture) {
        // draw the grass everywhere
        if (grassTexture == null) {
            buffer.setPaint(GRASS_COLOR); // no need to set the stroke
        } else {
            buffer.setPaint(grassTexture); // no need to set the stroke
        }
        buffer.fill(rect);
    }

    private Road getNorthRoad(){
        return Debug.currentRimMap.getRoads().get(2);
    }

    private Road getEastRoad(){
        return Debug.currentRimMap.getRoads().get(0);
    }

    private Road getSouthRoad(){
        return Debug.currentRimMap.getRoads().get(3);
    }

    private Road getWestRoad(){
        return Debug.currentRimMap.getRoads().get(1);
    }

    private AIMBasicAutoVehicle createTestVehicle(
            Request.VehicleSpecForRequestMsg spec,
            double arrivalVelocity,
            double maxVelocity,
            ArcSegmentLane arrivalLane) {

        VehicleSpec newSpec = new VehicleSpec(
                "TestVehicle",
                spec.getMaxAcceleration(),
                spec.getMaxDeceleration(),
                maxVelocity,
                spec.getMinVelocity(),
                spec.getLength(),
                spec.getWidth(),
                spec.getFrontAxleDisplacement(),
                spec.getRearAxleDisplacement(),
                0.0, // wheelSpan
                0.0, // wheelRadius
                0.0, // wheelWidth
                spec.getMaxSteeringAngle(),
                spec.getMaxTurnPerSecond());
        AIMBasicAutoVehicle testVehicle = new AIMBasicAutoVehicle(
                newSpec,
                arrivalLane.getEndPoint(), // Position
                ((ArcSegmentLane) arrivalLane.getNextLane()).getArcLaneDecomposition().get(0).getInitialHeading(), // Heading
                0.0, // Steering angle
                arrivalVelocity, // velocity
                0.0, // target velocity
                0.0, // Acceleration
                0.0); // the current time   // TODO: need to think about the appropriate
        // current time

        return testVehicle;
    }

    private void findTileTimesBySimulation(Graphics2D bgBuffer,
                                           ArcSegmentLane arrivalLane,
                                           AIMBasicAutoVehicle testVehicle,
                                           Driver dummy,
                                           double arrivalTime,
                                           boolean accelerating, RoadBasedIntersection intersection, ReservationGrid reservationGrid, TiledRimArea tiledRimArea, ReservationGridManager.Config config) {
        // The area of the intersection
        Area areaPlus = intersection.getAreaPlus();
        // The following must be true because the test vehicle
        // starts at the entry point of the intersection.
        assertEquals(intersection.getEntryPoint(arrivalLane).getX(), testVehicle.getPointAtMiddleFront(
                Constants.DOUBLE_EQUAL_PRECISION).getX(), Constants.DOUBLE_EQUAL_PRECISION);
        assertEquals(intersection.getEntryPoint(arrivalLane).getY(), testVehicle.getPointAtMiddleFront(
                Constants.DOUBLE_EQUAL_PRECISION).getY(), Constants.DOUBLE_EQUAL_PRECISION);

        // The list of tile-times that will make up this reservation
        List<ReservationGrid.TimeTile> workingList = new ArrayList<>();

        // A discrete representation of the time throughout the internal simulation
        // Notice that currentIntTime != arrivalTime
        int currentIntTime = reservationGrid.calcDiscreteTime(arrivalTime);
        // The duration in the current time interval
        double currentDuration = reservationGrid.calcRemainingTime(arrivalTime);

        // While the vehicle has not entered the intersection, move
        while (!(VehicleUtil.intersects(testVehicle, areaPlus))) {
            moveTestVehicle(testVehicle, dummy, currentDuration, accelerating);
            bgBuffer.setPaint(Color.BLACK);
            bgBuffer.fill(testVehicle.getShape());
            currentIntTime++;  // Record that we've moved forward one time step
            currentDuration = reservationGrid.getGridTimeStep();
        }

        // Now drive the test vehicle until it leaves the intersection
        while(VehicleUtil.intersects(testVehicle, areaPlus)) {
            moveTestVehicle(testVehicle, dummy, currentDuration, accelerating);
            bgBuffer.setPaint(Color.BLACK);
            bgBuffer.fill(testVehicle.getShape());
            // Find out which tiles are occupied by the vehicle
            currentIntTime++;  // Record that we've moved forward one time step
            List<TiledRimArea.Tile> occupied =
                    tiledRimArea.findOccupiedTiles(testVehicle.getShape(STATIC_BUFFER_SIZE));

            // Make sure none of these tiles are reserved by someone else already
            for(TiledRimArea.Tile tile : occupied) {
                int buffer = (int) (config.getInternalTileTimeBufferSize() / config.getGridTimeStep());
                int tileId = tile.getId();
                for(int t = currentIntTime - buffer; t <= currentIntTime + buffer; t++){
                    // If the tile is already reserved and it isn't by us, we've failed
                    if (!reservationGrid.isReserved(t, tileId)) {
                        workingList.add(reservationGrid.new TimeTile(t, tile.getId()));
                    }
                }
            }
            currentDuration = reservationGrid.getGridTimeStep();
        }

        bgBuffer.setPaint(Color.RED);

        for(int i = 0; i < 32; i+=2)
        {
            bgBuffer.drawLine(xLeadPoints[i] + i,yLeadPoints[i] + i, xLeadPoints[i+1] + i, yLeadPoints[i+1] + i);
        }

    }

    private void moveTestVehicle(AIMBasicAutoVehicle testVehicle,
                                 Driver dummy,
                                 double duration,
                                 boolean accelerating) {
        // Give the CrashTestDummy a chance to steer
        dummy.act();
        // Now control the vehicle's acceleration
        if(accelerating) {
            // Accelerate at maximum rate, topping out at maximum velocity
            testVehicle.setMaxAccelWithMaxTargetVelocity();   // TODO: use other function instead of
            // setMaxAccelWithMaxTargetVelocity()
        } else {
            // Maintain a constant speed
            testVehicle.coast();
        }
        // Now move the vehicle
        testVehicle.move(duration);
        // TODO: testVehicle.setClock();
    }
}
