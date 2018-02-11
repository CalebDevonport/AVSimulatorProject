package aim4;

import aim4.map.Road;
import aim4.map.connections.RimConnection;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;

import javax.imageio.ImageIO;
import java.applet.Applet;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

public class RimConnectionTestApplet extends Applet implements Runnable{
    // Colors taken from AIM Canvas
    private static final Color STRICT_AREA_COLOR = Color.BLUE;
    private static final Color ROAD_AREA_COLOR = Color.PINK;
    private static final Color POINTS_COLOR = Color.RED;
    private static final Color ENTRY_POINTS_COLOR = Color.magenta;
    private static final Color EXIT_POINTS_COLOR = Color.BLACK;
    private static final Color APPROACH_ENTRY_POINTS_COLOR = Color.PINK;
    private static final Color APPROACH_EXIT_POINTS_COLOR = Color.DARK_GRAY;
    private static final Stroke ROAD_BOUNDARY_STROKE = new BasicStroke(0.3f);
    private static final Stroke ROAD_BOUNDARY_STROKE_2 = new BasicStroke(0.7f);
    private static final AffineTransform IDENTITY_TRANSFORM =
            new AffineTransform();
    private static final Color GRASS_COLOR = Color.GREEN.darker().darker();
    private static final String GRASS_TILE_FILE = "/images/grass128.png";

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

        double scaleFactor = 4;
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
        TexturePaint grassTexture = makeScaledTexture(grassImage, scaleFactor);
        paintEntireBuffer(bgBuffer, Color.RED);
        drawGrass(bgBuffer, mapRect, grassTexture);

        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE_2);
        bgBuffer.setPaint(STRICT_AREA_COLOR);

        // Create a connection
        RimConnection connection = new RimConnection(map.getRoads());
        // Draw the connections between roads
        bgBuffer.draw(connection.getArea());

        // Draw the road areas
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
        bgBuffer.setPaint(POINTS_COLOR);
        bgBuffer.drawOval((int)connection.getCentroid().getX()-1,
                (int)connection.getCentroid().getY()-1, 1, 1);

        // Draw entry points
        bgBuffer.setPaint(ENTRY_POINTS_COLOR);
        List<Lane> entryLanes = connection.getEntryLanes();
        entryLanes.forEach( entryLane -> bgBuffer.drawOval((int)connection.getEntryPoint(entryLane).getX(),
                (int)connection.getEntryPoint(entryLane).getY(), 1, 1));

        // Draw entry approach points
        bgBuffer.setPaint(APPROACH_ENTRY_POINTS_COLOR);
        entryLanes.forEach( entryLane -> bgBuffer.drawOval((int)connection.getEntryApproachPoint(entryLane).getX(),
                (int)connection.getEntryApproachPoint(entryLane).getY(), 1, 1));

        // Draw exit points
        bgBuffer.setPaint(EXIT_POINTS_COLOR);
        List<Lane> exitLanes = connection.getExitLanes();
        exitLanes.forEach( exitLane -> bgBuffer.drawOval((int)connection.getExitPoint(exitLane).getX(),
                (int)connection.getExitPoint(exitLane).getY(), 1, 1));

        // Draw approach exit points
        bgBuffer.setPaint(APPROACH_EXIT_POINTS_COLOR);
        exitLanes.forEach( exitLane -> bgBuffer.drawOval((int)connection.getExitApproachPoint(exitLane).getX(),
                (int)connection.getExitApproachPoint(exitLane).getY(), 1, 1));


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
}
