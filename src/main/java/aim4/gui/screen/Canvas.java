/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.gui.screen;

import aim4.config.Debug;
import aim4.config.DebugPoint;
import aim4.gui.Viewer;
import aim4.gui.viewer.SimViewer;
import aim4.map.BasicMap;
import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.sim.Simulator;
import aim4.util.Util;
import aim4.vehicle.VehicleSimModel;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * The Canvas is the visual area on which the Layout, IntersectionManagers,
 * Vehicles, and so forth are drawn for the user to see.
 */
public abstract class Canvas extends JPanel implements ComponentListener,
        MouseListener,
        MouseWheelListener,
        MouseMotionListener,
        SimScreen {

    // ///////////////////////////////
    // CONSTANTS
    // ///////////////////////////////
    /** The serial version ID for serialization */
    private static final long serialVersionUID = 1L;
    /**
     * An AffineTransform that does nothing. This is used mainly for when text
     * must be drawn to the Canvas, but we do not want it distorted by the usual
     * AffineTransform, which transforms between Simulator space and Graphics
     * coordinates.
     */
    private static final AffineTransform IDENTITY_TRANSFORM =
            new AffineTransform();
    /**
     * The factor on the zooming scale for each notch
     */
    private static final double SCALE_FACTOR = 1.10;
    /**
     * The maximum number of zooming in steps
     */
    private static final int ZOOM_IN_SCALE_NUM = 10;
    /**
     * The maximum number of zooming out steps
     */
    private static final int ZOOM_OUT_SCALE_NUM = 25;
    /**
     * The maximum number of zooming steps
     */
    private static final int SCALE_NUM =
            ZOOM_IN_SCALE_NUM + ZOOM_OUT_SCALE_NUM + 1;
    /**
     * The margin of the view that must stay on screen.
     */
    private static final int VIEW_MARGIN = 50;
    // Drawing elements for background
    /** The file name of the file containing the image to use for grass. */
    private static final String GRASS_TILE_FILE = "/images/grass128.png";
    /** The file name of the file containing the image to use for asphalt. */
    private static final String ASPHALT_TILE_FILE = "/images/asphalt32.png";
    /** The color of the grass, if the image does not load properly. */
    public static final Color GRASS_COLOR = Color.GREEN.darker().darker();
    /** The color of the asphalt, if the image does not load properly. */
    public static final Color ASPHALT_COLOR = Color.BLACK.brighter();
    /** The color of the background */
    public static final Color BACKGROUND_COLOR = Color.gray;
    // Drawing elements for vehicle
    /** The stroke used of vehicles. */
    protected static final Stroke VEHICLE_STROKE = new BasicStroke(0.1f);
    /** The color of ordinary Vehicles. */
    protected static final Color VEHICLE_COLOR = Color.YELLOW;

    /** The colors that emergency Vehicles cycle through. */
    // private static final Color[] EMERGENCY_VEHICLE_COLORS =
    //  { Color.RED, Color.BLUE };
    /**
     * The period, in seconds, that emergency Vehicles take to cycle through
     * each of their Colors. {@value} seconds.
     */
    // private static final double EMERGENCY_VEHICLE_COLOR_PERIOD = 0.5; // sec
    /** The color of vehicles that have been clicked on by the user */
    protected static final Color VEHICLE_SELECTED_COLOR = Color.ORANGE;
    /** The color of vehicle's tires. */
    protected static final Color TIRE_COLOR = Color.BLACK;
    /** The tire color */
    protected static final Stroke TIRE_STROKE = new BasicStroke(0.1f);
    /** The vehicle information string color */
    protected static final Color VEHICLE_INFO_STRING_COLOR = Color.RED;
    /** The vehicle information string font */
    protected static final Font VEHICLE_INFO_STRING_FONT =
            new Font("Monospaced", Font.PLAIN, 5);
    // Drawing elements for lanes
    /** The color of the road boundary */
    private static final Color ROAD_BOUNDARY_COLOR = Color.YELLOW;
    /** The stroke of the road boundary */
    private static final Stroke ROAD_BOUNDARY_STROKE = new BasicStroke(0.3f);
    // Drawing elements for lane separators
    /**
     * The color with which to draw lines separating traffic traveling in the
     * same direction on the road.
     */
    private static final Color LANE_SEPARATOR_COLOR = Color.WHITE;
    /**
     * The stroke used to draw the broken white lines separating the lanes in
     * the same direction on the road.
     */
    private static final Stroke LANE_SEPARATOR_STROKE =
            new BasicStroke(.3f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0,
                    new float[]{1, 3}, 0);
    // Drawing elements for data collection lines
    /** The color of the data collection lines */
    private static final Color DCL_COLOR = Color.WHITE;
    /** The stroke of the data collection lines */
    private static final Stroke DCL_STROKE = new BasicStroke(0.3f);
    // simulation time
    /** Simulation time's string color */
    private static final Color SIMULATION_TIME_STRING_COLOR = Color.YELLOW;
    /** Simulation time's font */
    private static final Font SIMULATION_TIME_STRING_FONT =
            new Font("Monospaced", Font.PLAIN, 18);
    /** Simulation time location X */
    private static final int SIMULATION_TIME_LOCATION_X = 12;
    /** Simulation time location Y */
    private static final int SIMULATION_TIME_LOCATION_Y = 24;
    // the highlighted vehicle
    /** The color of the highlighted vehicle */
    private static final Color HIGHLIGHTED_VEHICLE_COLOR = Color.GREEN;
    /** The stroke of the highlighted vehicle */
    private static final Stroke HIGHLIGHTED_VEHICLE_STROKE =
            new BasicStroke(0.3f);
    // Debug points
    /** Debut point stroke */
    private static final Stroke DEBUG_POINT_STROKE = new BasicStroke(0.3f);
    /** Debut point font */
    private static final Font DEBUG_POINT_FONT =
            new Font("Monospaced", Font.PLAIN, 5);
    /**
     * The radius, in meters, of the circles used to display DriverAgents'
     * DebugPoints. {@value} meters.
     */
    private static final double DEBUG_POINT_RADIUS = 0.5;
    // Tracks
    /** The color of the track */
    protected static final Color TRACK_COLOR = Color.RED;
    /** The stroke of the track */
    protected static final Stroke TRACK_STROKE = new BasicStroke(0.3f);
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////
    /** The map */
    private BasicMap basicMap;
    /** The position of the x-coordinate of the origin in the sim space */
    private int posOfOriginX;
    /** The position of the y-coordinate of the origin in the sim space */
    private int posOfOriginY;
    /** The scale of the map on screen */
    private double[] scaleTable;
    /** The last position of the x-coordinate of the cursor */
    private int lastCursorX;
    /** The last position of the y-coordinate of the cursor */
    private int lastCursorY;
    /** The current scale index */
    protected int scaleIndex;
    /** The image for grass texture. */
    protected BufferedImage grassImage;
    /** The image for asphalt texture. */
    protected BufferedImage asphaltImage;
    /**
     * A cache of the background so that we do not need to redraw it every time
     * a vehicle moves.
     */
    private Image[] mapImageTable;
    /**
     * The buffer for the image that will be drawn to the canvas whenever it is
     * repainted.
     */
    private Image displayImage;
    /**
     * The graphic context in which we will use to draw to the displayImage.
     */
    protected Graphics2D displayBuffer;
    /**
     * the simViewer
     */
    protected SimViewer simViewer;
    /**
     * The viewer
     */
    protected Viewer viewer;
    /**
     * Whether other threads can update the canvas via update()
     */
    private boolean canUpdateCanvas;
    /**
     * Whether to show the simulation time on canvas
     */
    protected boolean isShowSimulationTime;
    /**
     * Whether to show the VIN numbers
     */
    protected boolean isShowVin;


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////
    /**
     * Create a new canvas.
     *
     * @param simViewer the simViewer object
     */
    public Canvas(SimViewer simViewer, Viewer viewer) {
        this.simViewer = simViewer;
        this.viewer = viewer;

        basicMap = null;

        posOfOriginX = 0;
        posOfOriginY = 0;
        scaleIndex = 0;
        scaleTable = null;

        lastCursorX = -1;
        lastCursorY = -1;

        grassImage = loadImage(GRASS_TILE_FILE);
        if (grassImage == null) {
            System.err.println("Could not load image from file: " + GRASS_TILE_FILE);
        }
        asphaltImage = loadImage(ASPHALT_TILE_FILE);
        if (asphaltImage == null) {
            System.err.println("Could not load image from file: " + ASPHALT_TILE_FILE);
        }

        mapImageTable = null;
        displayImage = null;
        displayBuffer = null;

        canUpdateCanvas = false;

        isShowSimulationTime = Viewer.IS_SHOW_SIMULATION_TIME;
        isShowVin = Viewer.IS_SHOW_VIN_BY_DEFAULT;

        addMouseListener(simViewer);
        addKeyListener(viewer);
        addComponentListener(this);
        addMouseListener(this);
        addMouseWheelListener(this);
        addMouseMotionListener(this);
    }

    /**
     * Load an image from an image file.
     *
     * @param imageFileName  the path to the image file
     * @return the image object
     */
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

    /**
     * Initialize the canvas with a given map.
     *
     * @param basicMap  the layout the canvas will be visualizing
     */
    public void initWithGivenMap(BasicMap basicMap) {
        this.basicMap = basicMap;

        posOfOriginX = 0;
        posOfOriginY = 0;
        setupScale();

        lastCursorX = -1;
        lastCursorY = -1;

        // create the displayBuffer
        makeDisplayBuffer();

        // Set up the affine transform so we draw in the right coordinate space
        // resetAffineTransform();

        // create the background
        mapImageTable = new Image[SCALE_NUM];
        for (int i = 0; i < SCALE_NUM; i++) {
            mapImageTable[i] = null;
        }
        // create the map image for the initial scale
        mapImageTable[scaleIndex] =
                createMapImage(basicMap, scaleTable[scaleIndex]);

        canUpdateCanvas = true;
    }

    /**
     * Create the display buffer
     */
    private void makeDisplayBuffer() {
        displayImage = createImage(getWidth(), getHeight());
        displayBuffer = (Graphics2D) displayImage.getGraphics();
        displayBuffer.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
    }

    /**
     * Paint the entire buffer with the given color
     *
     * @param buffer the display buffer
     * @param color  the color
     */
    protected void paintEntireBuffer(Graphics2D buffer, Color color) {
        AffineTransform tf = buffer.getTransform();
        // set the transform
        buffer.setTransform(IDENTITY_TRANSFORM);
        // paint
        buffer.setPaint(color); // no need to set the stroke
        buffer.fillRect(0, 0, getSize().width, getSize().height);
        // Restore the original transform.
        buffer.setTransform(tf);
    }

    /**
     * Reset the affine transform.
     */
    protected void resetAffineTransform() {
        AffineTransform tf = new AffineTransform();
        tf.translate(posOfOriginX, posOfOriginY);
        tf.scale(scaleTable[scaleIndex], scaleTable[scaleIndex]);
        displayBuffer.setTransform(tf);
    }

    /**
     * Move the origin to stay within view.
     */
    private void moveOriginToStayWithinView() {
        Rectangle2D r = basicMap.getDimensions();
        if (getWidth() >= VIEW_MARGIN) {
            if (posOfOriginX > getWidth() - VIEW_MARGIN) {
                posOfOriginX = getWidth() - VIEW_MARGIN;
            } else {
                int w = (int) (r.getWidth() * scaleTable[scaleIndex]);
                if (posOfOriginX + w < VIEW_MARGIN) {
                    posOfOriginX = VIEW_MARGIN - w;
                }
            }
        }  // else just do nothing
        if (getHeight() >= VIEW_MARGIN) {
            if (posOfOriginY > getHeight() - VIEW_MARGIN) {
                posOfOriginY = getHeight() - VIEW_MARGIN;
            } else {
                int w = (int) (r.getHeight() * scaleTable[scaleIndex]);
                if (posOfOriginY + w < VIEW_MARGIN) {
                    posOfOriginY = VIEW_MARGIN - w;
                }
            }
        }  // else just do nothing
    }

    /**
     * Setup the scale.
     */
    private void setupScale() {
        Rectangle2D mapRect = basicMap.getDimensions();
        // the initial scale
        scaleTable = new double[SCALE_NUM];
        scaleIndex = ZOOM_IN_SCALE_NUM;
        // calculate the scale at the middle (current scale)
        scaleTable[scaleIndex] = Math.min(getWidth() / mapRect.getWidth(),
                getHeight() / mapRect.getHeight());
        for (int i = scaleIndex - 1; i >= 0; i--) {
            scaleTable[i] = scaleTable[i + 1] * SCALE_FACTOR;
        }
        for (int i = scaleIndex + 1; i < SCALE_NUM; i++) {
            scaleTable[i] = scaleTable[i - 1] / SCALE_FACTOR;
        }
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////
    /**
     * Create a background image for the given Layout.
     *
     * @param map  the Layout for which to create a background image
     *        s    the scale of the map
     */
    protected abstract Image createMapImage(BasicMap map, double scale);

    /**
     * Get the map image at a given scale index.  If the map image does not
     * exist in the cache, create it.
     *
     * @param scaleIndex  the scale index
     * @return the map image at the given scale index
     */
    protected Image getMapImageTable(int scaleIndex) {
        if (mapImageTable[scaleIndex] == null) {
            mapImageTable[scaleIndex] =
                    createMapImage(basicMap, scaleTable[scaleIndex]);
        }
        return mapImageTable[scaleIndex];
    }

    /**
     * Create a scaled image.
     *
     * @param image  the image object
     * @param scale  the scaling factor
     * @return the new image
     */
    protected TexturePaint makeScaledTexture(BufferedImage image, double scale) {
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

    /**
     * Paint the rectangle with the grass picture.
     *
     * @param buffer        the image buffer
     * @param rect          the rectangle
     * @param grassTexture  the grass texture
     */
    protected void drawGrass(Graphics2D buffer,
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

    /**
     * Draw a road on the display buffer.
     *
     * @param bgBuffer        the display buffer
     * @param road            the road
     * @param asphaltTexture  the grass texture
     */
    protected void drawRoad(Graphics2D bgBuffer, Road road,
                          TexturePaint asphaltTexture) {
        for (Lane lane : road.getLanes()) {
            drawLane(bgBuffer, lane, asphaltTexture);
        }
    }

    /**
     * Draw a roundabout road on the display buffer.
     *
     * @param bgBuffer        the display buffer
     * @param road            the road
     * @param asphaltTexture  the grass texture
     */
    protected void drawRoundaboutRoad(Graphics2D bgBuffer, Road road,
                            TexturePaint asphaltTexture) {
    	for (Lane lane : road.getAllContinuousLanes()) {
            drawLane(bgBuffer, lane, asphaltTexture);
        }
    }

    /**
     * Draw a lane on the display buffer.
     *
     * @param bgBuffer        the display buffer
     * @param lane            the lane
     * @param asphaltTexture  the asphalt texture
     */
    protected void drawLane(Graphics2D bgBuffer,
                          Lane lane,
                          TexturePaint asphaltTexture) {
        // Draw the lane itself
        if (asphaltTexture == null) {
            bgBuffer.setPaint(ASPHALT_COLOR);
        } else {
            bgBuffer.setPaint(asphaltTexture);
        }
        bgBuffer.fill(lane.getShape());
        // Draw the left boundary
        if (lane.hasLeftNeighbor()) {
            bgBuffer.setPaint(LANE_SEPARATOR_COLOR);
            bgBuffer.setStroke(LANE_SEPARATOR_STROKE);
        } else {
            bgBuffer.setPaint(ROAD_BOUNDARY_COLOR);
            bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);
        }
        bgBuffer.draw(lane.leftBorder());
        // Draw the right boundary
        if (lane.hasRightNeighbor()) {
            bgBuffer.setPaint(LANE_SEPARATOR_COLOR);
            bgBuffer.setStroke(LANE_SEPARATOR_STROKE);
        } else {
            bgBuffer.setPaint(ROAD_BOUNDARY_COLOR);
            bgBuffer.setStroke(ROAD_BOUNDARY_STROKE);
        }
        bgBuffer.draw(lane.rightBorder());
    }

    /**
     * Draw the data collection lines.
     *
     * @param bgBuffer             the display buffer
     * @param dataCollectionLines  the list of data collection lines
     */
    protected void drawDataCollectionLines(Graphics2D bgBuffer,
                                         List<DataCollectionLine> dataCollectionLines) {
        for (DataCollectionLine line : dataCollectionLines) {
            bgBuffer.setPaint(DCL_COLOR);
            bgBuffer.setStroke(DCL_STROKE);
            bgBuffer.draw(line.getShape());
        }
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////
    /**
     * Clean up the canvas
     */
    public synchronized void cleanUp() {
        paintEntireBuffer(displayBuffer, BACKGROUND_COLOR);
        for (int i = 0; i < SCALE_NUM; i++) {
            mapImageTable[i] = null;
        }
        repaint();
    }

    /**
     * Update the canvas to visualize the current state of simulation.
     */
    public void update() {
        if (canUpdateCanvas) {
            // TODO: think how to avoid multiple calls of update() are queued here
            // by synchronized when the the canvas is resized, rescaled, or
            // repositioned.
            updateCanvas();
        }
    }

    /**
     * Update the canvas to visualize the current state of simulation.
     */
    private synchronized void updateCanvas() {
        doUpdateCanvas();
    }

    /**
     * Update the canvas to visualize the current state of simulation.
     */
    protected void doUpdateCanvas() {
        // reset the affine transform
        resetAffineTransform();
        // Clear the screen
        paintEntireBuffer(displayBuffer, BACKGROUND_COLOR);
        // draw the map
        drawImageOnBuffer(displayBuffer, getMapImageTable(scaleIndex));
        // Get the simulator
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void paint(Graphics g) {
        if (displayImage != null) {
            g.drawImage(displayImage, 0, 0, this);
        }
    }

    /////////////////////////////////
    // PRIVATE METHODS
    /////////////////////////////////
    /**
     * Draw an image on screen.
     *
     * @param buffer  the display buffer
     * @param image   the image
     */
    protected void drawImageOnBuffer(Graphics2D buffer, Image image) {
        // Save the current transform so we can restore it.
        AffineTransform tf = buffer.getTransform();
        // set the identity transform
        buffer.setTransform(IDENTITY_TRANSFORM);
        // Copy the background image over to the display image
        buffer.drawImage(image, posOfOriginX, posOfOriginY, null);
        // Restore the original transform.
        buffer.setTransform(tf);
    }

    /**
     * Draw an individual Vehicle, and any associated debug information, if this
     * Vehicle is a debug Vehicle.
     *
     * @param buffer       the display buffer
     * @param vehicle      the Vehicle to draw now
     * @param currentTime  the current simulated time
     */
    protected void drawVehicle(Graphics2D buffer,
                             VehicleSimModel vehicle,
                             double currentTime) {
        boolean selectedVehicle = (Debug.getTargetVIN() == vehicle.getVIN());

        buffer.setStroke(VEHICLE_STROKE);
        buffer.fill(vehicle.getShape());

        // Draw wheels and stuff if needed
        if (selectedVehicle) {
            buffer.setPaint(TIRE_COLOR);
            buffer.setStroke(TIRE_STROKE);
            for (Shape wheel : vehicle.getWheelShapes()) {
                buffer.fill(wheel);
            }
        }
    }

    /**
     * Draw the information string of the vehicle on screen
     *
     * @param buffer       the display buffer
     * @param vehicle      the vehicle
     * @param currentTime  the current simulated time
     */
    protected void drawVehicleInfoString(Graphics2D buffer,
                                       VehicleSimModel vehicle,
                                       double currentTime) {
        java.util.List<String> infos = new LinkedList<String>();

        if (isShowVin) {
            infos.add(Integer.toString(vehicle.getVIN()));
        }

        if (infos.size() > 0) {
            Point2D centerPoint = vehicle.getCenterPoint();
            buffer.setColor(VEHICLE_INFO_STRING_COLOR);
            buffer.setFont(VEHICLE_INFO_STRING_FONT);
            buffer.drawString(Util.concatenate(infos, ","),
                    (float) centerPoint.getX(),
                    (float) centerPoint.getY());
        }
    }

    /**
     * Draw the simulation time.
     *
     * @param buffer       the display buffer
     * @param currentTime  the time
     */
    protected void drawSimulationTime(Graphics2D buffer, double currentTime) {
        // Save the current transform so we can restore it.
        AffineTransform tf = buffer.getTransform();
        // Set the identity transform
        buffer.setTransform(IDENTITY_TRANSFORM);
        // Draw the time
        buffer.setColor(SIMULATION_TIME_STRING_COLOR);
        buffer.setFont(SIMULATION_TIME_STRING_FONT);
        buffer.drawString(String.format("%.2fs", currentTime),
                SIMULATION_TIME_LOCATION_X,
                SIMULATION_TIME_LOCATION_Y);
        // Restore the original transform.
        buffer.setTransform(tf);
    }

    /**
     * Draw a series of debug points.
     *
     * @param buffer       the display buffer
     * @param debugPoints  a set of debug points
     */
    protected void drawDebugPoints(Graphics2D buffer,
                                 List<DebugPoint> debugPoints) {
        for (DebugPoint p : debugPoints) {
            drawDebugPoint(buffer, p);
        }
    }

    /**
     * Draw a debug point.
     *
     * @param buffer  the display buffer
     * @param p       a debug point
     */
    private void drawDebugPoint(Graphics2D buffer, DebugPoint p) {
        if (p.getPoint() != null) {
            buffer.setPaint(p.getColor());
            buffer.setStroke(DEBUG_POINT_STROKE);
            // If there's supposed to be a start point, draw a line from it
            // to the point
            if (p.hasStartPoint()) {
                buffer.draw(new Line2D.Double(p.getStartPoint(), p.getPoint()));
            }
            // Always draw the point
            buffer.fill(new Ellipse2D.Double(
                    p.getPoint().getX() - DEBUG_POINT_RADIUS,
                    p.getPoint().getY() - DEBUG_POINT_RADIUS,
                    DEBUG_POINT_RADIUS * 2,
                    DEBUG_POINT_RADIUS * 2));
            // We need to change the transform here so our text winds up facing
            // the right way and at the right size
            if (p.hasText()) {
                buffer.setFont(DEBUG_POINT_FONT);
                buffer.drawString(
                        p.getText(),
                        (float) (p.getPoint().getX() - DEBUG_POINT_RADIUS),
                        (float) (p.getPoint().getY() - DEBUG_POINT_RADIUS));
            }
        } // else skip the debug point
    }

    /**
     * Draw the tracks.
     *
     * @param buffer  the display buffer
     */
    protected abstract void drawTracks(Graphics2D buffer);

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // component listener

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentHidden(ComponentEvent e) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentMoved(ComponentEvent e) {
        // do nothing
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void componentResized(ComponentEvent e) {
        canUpdateCanvas = false;
        makeDisplayBuffer();
        if (basicMap != null) {
            moveOriginToStayWithinView();
            updateCanvas();
            canUpdateCanvas = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void componentShown(ComponentEvent e) {
        // do nothing
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // mouse listener

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseClicked(MouseEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseEntered(MouseEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseExited(MouseEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mousePressed(MouseEvent e) {
        lastCursorX = e.getX();
        lastCursorY = e.getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseReleased(MouseEvent e) {
        lastCursorX = -1;
        lastCursorY = -1;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // mouse wheel listener

    /**
     * {@inheritDoc}
     */
    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        if (canUpdateCanvas) {
            synchronized (this) {
                canUpdateCanvas = false;
                // First, save the old position at the user space
                Point2D p = getMapPosition(e.getX(), e.getY());
                // Second, update the scale
                int notches = e.getWheelRotation();
                scaleIndex += notches;
                if (scaleIndex < 0) {
                    scaleIndex = 0;
                }
                if (scaleIndex >= SCALE_NUM) {
                    scaleIndex = SCALE_NUM - 1;
                }
                // Third, recalculate the origin's screen location
                posOfOriginX = (int) (e.getX() - scaleTable[scaleIndex] * p.getX());
                posOfOriginY = (int) (e.getY() - scaleTable[scaleIndex] * p.getY());
                moveOriginToStayWithinView();
                updateCanvas();
                canUpdateCanvas = true;
            }
        }
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    // mouse drag listener

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)
                && lastCursorX >= 0 && lastCursorY >= 0) {
            canUpdateCanvas = false;
            posOfOriginX += e.getX() - lastCursorX;
            posOfOriginY += e.getY() - lastCursorY;
            moveOriginToStayWithinView();
            updateCanvas();
            canUpdateCanvas = true;
        }
        lastCursorX = e.getX();
        lastCursorY = e.getY();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public synchronized void mouseMoved(MouseEvent e) {
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Set whether to show the simulation time.
     *
     * @param b whether to show the simulation time
     */
    public void setIsShowSimulationTime(boolean b) {
        isShowSimulationTime = b;
    }

    /**
     * Set whether to show the VIN numbers.
     *
     * @param b whether to show the VIN numbers
     */
    public void setIsShowVin(boolean b) {
        isShowVin = b;
    }

    /**
     * Save the screen to a file in PNG format.
     *
     * @param outFileName  the output file name
     */
    public void saveScreenShot(String outFileName) {
        File outfile = new File(outFileName);
        try {
            if (!ImageIO.write((BufferedImage) displayImage, "png", outfile)) {
                System.err.printf("Error in Canvas::saveScreenShot(): "
                        + "no appropriate writer is found\n");
            }
        } catch (IOException ioe) {
            System.err.println("Error: " + ioe);
        }
    }

    /**
     * Convert the position on screen to the position on the map.
     *
     * @param screenPosX  The x-coordinate of the screen position
     * @param screenPosY  The y-coordinate of the screen position
     *
     * @return The corresponding position on the map; null if there is no map
     */
    public Point2D getMapPosition(int screenPosX, int screenPosY) {
        return new Point2D.Double(
                (screenPosX - posOfOriginX) / scaleTable[scaleIndex],
                (screenPosY - posOfOriginY) / scaleTable[scaleIndex]);
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * Highlight a particular vehicle.
     *
     * @param vin  the VIN number of the vehicle
     */
    public void highlightVehicle(int vin) {
        Simulator sim = simViewer.getSimulator();
        if (sim != null) {
            VehicleSimModel vehicle = sim.getActiveVehicle(vin);
            if (vehicle != null) {
                displayBuffer.setPaint(HIGHLIGHTED_VEHICLE_COLOR);
                displayBuffer.setStroke(HIGHLIGHTED_VEHICLE_STROKE);
                displayBuffer.fill(vehicle.getShape());
                repaint();
            }
        }
    }
}
