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

public class TiledRimAreaTestClass extends JPanel {

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
		JFrame frame = new JFrame("TiledRimAreaTestClass");
		frame.add(new TiledRimAreaTestClass());
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

        double scaleFactor = 7;
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
        TiledRimArea tiledRimArea = new TiledRimArea(roadBasedIntersection.getMinimalCircle(),
        		roadBasedIntersection.getCentralCircle(), roadBasedIntersection.getMaximalCircle(), 
        		8, roadBasedIntersection.getLaneNum());

        tiledRimArea.getAllTilesById().forEach( tile -> {
            bgBuffer.draw(tile.getArea());
//            float hue = tile.getId()/15f;
//            bgBuffer.setPaint(Color.getHSBColor(hue, 1.0f,0.8f));
        });

        // Create a vehicle

        // North Road
        ArcSegmentLane northVehicleLane = (ArcSegmentLane)map.getRoads().get(2).getContinuousLanesForLane(1).get(4);
        Point2D northPositionOfVehicleFront = new Point2D.Double(
                northVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                northVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
        AIMBasicAutoVehicle northVehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), northPositionOfVehicleFront,
                northVehicleLane.getArcLaneDecomposition().get(2).getInitialHeading(),0,0,0,0, 0);
        
        //// South Road
        ArcSegmentLane southVehicleLane = (ArcSegmentLane)map.getRoads().get(3).getContinuousLanesForLane(1).get(3);
        Point2D southPositionOfVehicleFront = new Point2D.Double(
                southVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                southVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
        AIMBasicAutoVehicle southVehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), southPositionOfVehicleFront,
                southVehicleLane.getArcLaneDecomposition().get(2).getInitialHeading(),0,0,0,0, 0);
        
        //// East Road
        ArcSegmentLane eastVehicleLane = (ArcSegmentLane)map.getRoads().get(0).getContinuousLanesForLane(1).get(3);
        Point2D eastPositionOfVehicleFront = new Point2D.Double(
                eastVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                eastVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
        AIMBasicAutoVehicle eastVehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), eastPositionOfVehicleFront,
                eastVehicleLane.getArcLaneDecomposition().get(2).getInitialHeading(),0,0,0,0, 0);
        
        
        //// West Road
        ArcSegmentLane westVehicleLane = (ArcSegmentLane)map.getRoads().get(1).getContinuousLanesForLane(1).get(7);
        Point2D westPositionOfVehicleFront = new Point2D.Double(
                westVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getX(),
                westVehicleLane.getArcLaneDecomposition().get(2).getStartPoint().getY());
        AIMBasicAutoVehicle westVehicle = new AIMBasicAutoVehicle(VehicleSpecDatabase.getVehicleSpecByName("COUPE"), westPositionOfVehicleFront,
                westVehicleLane.getArcLaneDecomposition().get(2).getInitialHeading(),0,0,0,0, 0);
        
        
        //// Draw occupied lanes
		//
		  bgBuffer.setPaint(Color.RED);
		  Point2D[] pointsNorth = northVehicle.getSpec().getCornerPoints(
				  0.25, northVehicle.getPosition(), northVehicle.gaugeHeading());
		  Point2D[] pointsSouth = southVehicle.getSpec().getCornerPoints(
				  0.25, southVehicle.getPosition(), southVehicle.gaugeHeading());
		  Point2D[] pointsEast = eastVehicle.getSpec().getCornerPoints(
				  0.25, eastVehicle.getPosition(), eastVehicle.gaugeHeading());
		  Point2D[] pointsWest = westVehicle.getSpec().getCornerPoints(
				  0.25, westVehicle.getPosition(), westVehicle.gaugeHeading());
		  tiledRimArea.findOccupiedTiles(pointsNorth).forEach( tile -> {
			  bgBuffer.draw(tile.getArea()); });
		  tiledRimArea.findOccupiedTiles(pointsSouth).forEach( tile -> {
			  bgBuffer.draw(tile.getArea()); });
		  tiledRimArea.findOccupiedTiles(pointsEast).forEach( tile -> {
			  bgBuffer.draw(tile.getArea()); });
		  tiledRimArea.findOccupiedTiles(pointsWest).forEach( tile -> {
			  bgBuffer.draw(tile.getArea()); });
		  
		//  // Draw vehicle 
		  bgBuffer.setPaint(Color.BLACK);
		  bgBuffer.fill(northVehicle.getShape());
		  bgBuffer.fill(southVehicle.getShape());
		  bgBuffer.fill(eastVehicle.getShape());
		  bgBuffer.fill(westVehicle.getShape());
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
