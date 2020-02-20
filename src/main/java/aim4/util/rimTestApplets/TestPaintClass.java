package aim4.util.rimTestApplets;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.TexturePaint;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;

import aim4.im.rim.RoadBasedIntersection;
import aim4.map.lane.ArcSegmentLane;
import aim4.map.rim.RimIntersectionMap;
import aim4.util.TiledRimArea;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.aim.AIMBasicAutoVehicle;

public class TestPaintClass extends JPanel {

	private static final long serialVersionUID = 1L;
	
	// Colors taken from AIM Canvas
    private static final Stroke ROAD_BOUNDARY_STROKE = new BasicStroke(0.3f);
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
	
    public static void main(String[] args) {
		JFrame frame = new JFrame("Mini Tennis");
		frame.add(new TestPaintClass());
		frame.setSize(1000, 1000);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
    
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

        double scaleFactor = 8;
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

        bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);
        bgBuffer.setPaint(Color.white);

        // Create a connection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create a tiled rim area
        TiledRimArea tiledRimArea = new TiledRimArea(roadBasedIntersection.getMinimalCircle(), roadBasedIntersection.getMaximalCircle(), 6);

        tiledRimArea.getAllTilesById().forEach( tile -> {
            bgBuffer.draw(tile.getArea());
//            float hue = tile.getId()/15f;
//            bgBuffer.setPaint(Color.getHSBColor(hue, 1.0f,0.8f));
        });

        // Create a vehicle

        // North Road
        ArcSegmentLane vehicleLane = (ArcSegmentLane)map.getRoads().get(2).getContinuousLanes().get(4);
        Point2D positionOfVehicleFront = new Point2D.Double(
                vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
        AIMBasicAutoVehicle vehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), positionOfVehicleFront,
                vehicleLane.getArcLaneDecomposition().get(2).getInitialHeading(),0,0,0,0, 0);

        // South Road
        ArcSegmentLane southVehicleLane = (ArcSegmentLane)map.getRoads().get(3).getContinuousLanes().get(3);
        Point2D southPositionOfVehicleFront = new Point2D.Double(
                southVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                southVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
        AIMBasicAutoVehicle southVehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), southPositionOfVehicleFront,
                southVehicleLane.getArcLaneDecomposition().get(0).getInitialHeading(),0,0,0,0, 0);

//        // East Road
//        ArcSegmentLane vehicleLane = (ArcSegmentLane)map.getRoads().get(0).getContinuousLanes().get(3);
//        Point2D positionOfVehicleFront = new Point2D.Double(
//                vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
//                vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
//        AIMBasicAutoVehicle vehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), positionOfVehicleFront,
//                vehicleLane.getArcLaneDecomposition().get(0).getInitialHeading(),0,0,0,0, 0);


//        // West Road
//        ArcSegmentLane vehicleLane = (ArcSegmentLane)map.getRoads().get(1).getContinuousLanes().get(7);
//        Point2D positionOfVehicleFront = new Point2D.Double(
//                vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
//                vehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
//        AIMBasicAutoVehicle vehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), positionOfVehicleFront,
//                vehicleLane.getArcLaneDecomposition().get(0).getInitialHeading(),0,0,0,0, 0);


        // Draw occupied lanes
		
		  bgBuffer.setPaint(Color.RED);
		  tiledRimArea.findOccupiedTiles(vehicle.getShape()).forEach( tile -> {
		  bgBuffer.draw(tile.getArea()); });
		  tiledRimArea.findOccupiedTiles(southVehicle.getShape()).forEach( tile -> {
			  bgBuffer.draw(tile.getArea()); });
		  
		  // Draw vehicle 
		  bgBuffer.setPaint(Color.BLACK);
		  bgBuffer.fill(vehicle.getShape());
		  bgBuffer.fill(southVehicle.getShape());
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
    
    /*@Override
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D) g;
		g2d.setColor(Color.RED);
		g2d.fillOval(0, 0, 30, 30);
		g2d.drawOval(0, 50, 30, 30);		
		g2d.fillRect(50, 0, 30, 30);
		g2d.drawRect(50, 50, 30, 30);

		g2d.draw(new Ellipse2D.Double(0, 100, 30, 30));
	}*/
    
	
	/*
	 * public static void main(String[] args) { JFrame frame = new
	 * JFrame("Mini Tennis"); frame.add(new TestPaintClass()); frame.setSize(300,
	 * 300); frame.setVisible(true);
	 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); }
	 */
}
