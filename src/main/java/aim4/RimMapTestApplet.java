package aim4;

import aim4.map.Road;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.aim.AIMBasicAutoVehicle;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class RimMapTestApplet extends Applet implements Runnable{
    // Colors taken from AIM Canvas
    private static final Color LEFT_BORDER_COLOR = Color.YELLOW;
    private static final Color RIGHT_BORDER_COLOR = Color.RED;
    private static final Stroke ROAD_BOUNDARY_STROKE = new BasicStroke(0.3f);
    private static final Color ASPHALT_COLOR = Color.BLACK.brighter();
    private static final AffineTransform IDENTITY_TRANSFORM =
            new AffineTransform();
    private static final Color GRASS_COLOR = Color.GREEN.darker().darker();
    private static final String GRASS_TILE_FILE = "/images/grass128.png";
    private static final String ASPHALT_TILE_FILE = "/images/asphalt32.png";

    // Roundabout properties
    private static final double[] ROUNDABOUT_DIAMETER = {30.0, 35.0, 40.0, 45.0};
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  31.0686;
    private static final double ROUNDABOUT_SPEED_LIMIT =  21.748;
    private static final double MAP_WIDTH =  250;
    private static final double MAP_HEIGHT =  250;

    public void paint(Graphics g) {
        // Create the RIM Map
        RimIntersectionMap map = new RimIntersectionMap(
                0,
                1,
                1,
                ROUNDABOUT_DIAMETER[3],
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);

        double scaleFactor = 3;
        Rectangle2D mapRect = new Rectangle2D.Double(0,0,MAP_WIDTH, MAP_HEIGHT);
        //Create Graphics2D object, cast g as a Graphics2D
        Graphics2D bgBuffer = (Graphics2D) g;

        // Apply AIM transformation
        AffineTransform tf = new AffineTransform();
        tf.translate(0, 0);
        tf.scale(scaleFactor, scaleFactor);
        bgBuffer.setTransform(tf);

        // Set map colors
        BufferedImage grassImage = loadImage(GRASS_TILE_FILE);
        BufferedImage asphaltImage = loadImage(ASPHALT_TILE_FILE);
        TexturePaint grassTexture = makeScaledTexture(grassImage, scaleFactor);
        TexturePaint asphaltTexture = makeScaledTexture(asphaltImage, scaleFactor);
        paintEntireBuffer(bgBuffer, Color.LIGHT_GRAY);
        drawGrass(bgBuffer, mapRect, grassTexture);

        // Draw the roads
        for (Road road : map.getRoads()) {
            drawRoad(bgBuffer, road, asphaltTexture);
        }

        // Draw vehicles
        int i = 1;
        for (Road road : map.getRoads()) {
            float hue = i/15f;
            bgBuffer.setPaint(Color.getHSBColor(hue, 1.0f,0.8f));
            for (int index = 1; index <= 7; index++) {
                ArcSegmentLane vehicleLane = (ArcSegmentLane)road.getContinuousLanes().get(index);
                Point2D positionOfVehicleFront = new Point2D.Double(
                        vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                        vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
                AIMBasicAutoVehicle vehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), positionOfVehicleFront,
                        vehicleLane.getArcLaneDecomposition().get(0).getInitialHeading(),0,0,0,0, 0);
                bgBuffer.fill(vehicle.getShape());
            }
            i++;
        }

        // Draw spawn points
        bgBuffer.setPaint(Color.white);
        map.getVerticalSpawnPoints().forEach(point -> {
            bgBuffer.fill(point.getNoVehicleZone());
            bgBuffer.setPaint(Color.red);
            bgBuffer.drawOval((int)point.getPosition().getX(),
                    (int)point.getPosition().getY(), 1, 1);
            bgBuffer.setPaint(Color.white);
        });
        map.getHorizontalSpawnPoints().forEach(point -> {
            bgBuffer.fill(point.getNoVehicleZone());
            bgBuffer.setPaint(Color.red);
            bgBuffer.drawOval((int)point.getPosition().getX(),
                    (int)point.getPosition().getY(), 1, 1);
            bgBuffer.setPaint(Color.white);
        });
    }

    public void run() {
        repaint();
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

    private void drawRoad(Graphics2D bgBuffer, Road road,
                            TexturePaint asphaltTexture) {
        // Draw Entry Line Lane
        drawLineLane(bgBuffer, road.getContinuousLanes().get(0), asphaltTexture);

        // Draw the Arc Lanes
        for (int index = 1 ; index <= road.getContinuousLanes().size()-2; index++)
        drawArcLane(bgBuffer, road.getContinuousLanes().get(index), asphaltTexture);

        // Draw Exit Line Lane
        drawLineLane(bgBuffer, road.getContinuousLanes().get(8), asphaltTexture);
    }

    private void drawRoadAsLineLanes(Graphics2D bgBuffer, Road road,
                          TexturePaint asphaltTexture) {
        // Draw Entry Line Lane
        drawLineLane(bgBuffer, road.getContinuousLanes().get(0), asphaltTexture);

        // Draw the Arc Lanes
        for (int index = 1 ; index <= road.getContinuousLanes().size()-2; index++)
            drawArcLaneAsLineLanes(bgBuffer, road.getContinuousLanes().get(index), asphaltTexture);

        // Draw Exit Line Lane
        drawLineLane(bgBuffer, road.getContinuousLanes().get(8), asphaltTexture);
    }

    private void drawLineLane(Graphics2D bgBuffer,
                                Lane lane,
                                TexturePaint asphaltTexture) {
        // Draw the lane itself
        if (asphaltTexture == null) {
            bgBuffer.setPaint(ASPHALT_COLOR);
        } else {
            bgBuffer.setPaint(asphaltTexture);
        }
        LineSegmentLane lineLane = (LineSegmentLane) (lane);
        // Fill the lane shape
        bgBuffer.fill(lane.getShape());
        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);

        // Draw left border
        bgBuffer.setPaint(LEFT_BORDER_COLOR);
        bgBuffer.draw(lineLane.getLeftBorder());

        // Draw right border
        bgBuffer.setPaint(RIGHT_BORDER_COLOR);
        bgBuffer.draw(lineLane.getRightBorder());
    }

    private void drawArcLane(Graphics2D bgBuffer,
                            Lane lane,
                            TexturePaint asphaltTexture) {
        // Draw the lane itself
        if (asphaltTexture == null) {
            bgBuffer.setPaint(ASPHALT_COLOR);
        } else {
            bgBuffer.setPaint(asphaltTexture);
        }
        ArcSegmentLane arcLane = (ArcSegmentLane) (lane);
        // Fill the lane shape
        bgBuffer.fill(lane.getShape());
        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);

        // Draw left border
        bgBuffer.setPaint(LEFT_BORDER_COLOR);
        bgBuffer.draw(arcLane.leftBorder());

        // Draw right border
        bgBuffer.setPaint(RIGHT_BORDER_COLOR);
        bgBuffer.draw(arcLane.rightBorder());
    }

    private void drawArcLaneAsLineLanes(Graphics2D bgBuffer,
                             Lane lane,
                             TexturePaint asphaltTexture) {
        // Draw the lane itself
        if (asphaltTexture == null) {
            bgBuffer.setPaint(ASPHALT_COLOR);
        } else {
            bgBuffer.setPaint(asphaltTexture);
        }

        // Fill the lane shape
        bgBuffer.fill(lane.getShape());
        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);

        ArcSegmentLane arcLane = (ArcSegmentLane) (lane);
        for (LineSegmentLane lineLane : arcLane.getArcLaneDecomposition()) {
            // Draw left border
            bgBuffer.setPaint(LEFT_BORDER_COLOR);
            bgBuffer.draw(lineLane.leftBorder());

            // Draw right border
            bgBuffer.setPaint(RIGHT_BORDER_COLOR);
            bgBuffer.draw(lineLane.rightBorder());
        }
    }


}