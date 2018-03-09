package aim4.gui.screen.rim;

import aim4.config.Debug;
import aim4.driver.rim.RIMAutoDriver;
import aim4.driver.rim.coordinator.V2ICoordinator;
import aim4.gui.Viewer;
import aim4.gui.screen.Canvas;
import aim4.gui.viewer.RIMSimViewer;
import aim4.im.rim.IntersectionManager;
import aim4.im.rim.v2i.V2IManager;
import aim4.map.BasicMap;
import aim4.map.BasicRIMIntersectionMap;
import aim4.map.Road;
import aim4.map.track.*;
import aim4.msg.rim.v2i.Request;
import aim4.msg.rim.v2i.V2IMessage;
import aim4.sim.Simulator;
import aim4.sim.simulator.rim.RIMSimulator;
import aim4.util.Util;
import aim4.vehicle.VehicleSimModel;
import aim4.vehicle.rim.RIMAutoVehicleSimModel;
import aim4.vehicle.rim.RIMVehicleSimModel;

import java.awt.*;
import java.awt.geom.*;
import java.util.Collection;
import java.util.LinkedList;

public class RIMCanvas extends Canvas{
    // Drawing elements for intersections
    /**
     * The color with which to draw the outline of an intersection that uses a
     * V2I system to manage traffic.
     */
    private static final Color IM_OUTLINE_COLOR = Color.CYAN;
    /** Selected IM's outline color */
    private static final Color SELECTED_IM_OUTLINE_COLOR = Color.ORANGE;
    /** IM's stroke */
    private static final Stroke IM_OUTLINE_STROKE = new BasicStroke(0.3f);

    /** The color of vehicles that have reservations. */
    private static final Color VEHICLE_HAS_RESERVATION_COLOR = Color.WHITE;
    /** The color of vehicles that are waiting for a response */
    private static final Color VEHICLE_WAITING_FOR_RESPONSE_COLOR =
            Color.blue.brighter().brighter().brighter();
    /** MARVIN's coloring */
    private static final int MARVIN_VEHICLE_VIN = 42;
    /** MARVIN's color */
    private static final Color MARVIN_VEHICLE_COLOR = Color.RED;
    // IM debug shapes
    /** The color of IM debug shapes */
    private static final Color IM_DEBUG_SHAPE_COLOR = Color.CYAN;

    /**
     * Whether or not the Canvas will try to draw the IntersectionManagers'
     * debugging shapes.
     */
    private boolean isShowIMDebugShapes;

    /**
     * Create a new canvas.
     *
     * @param simViewer the simViewer object
     * @param viewer
     */
    public RIMCanvas(RIMSimViewer simViewer, Viewer viewer) {
        super(simViewer, viewer);
        isShowIMDebugShapes = Viewer.IS_SHOW_IM_DEBUG_SHAPES_BY_DEFAULT;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected Image createMapImage(BasicMap map, double scale) {
        assert map instanceof BasicRIMIntersectionMap;
        BasicRIMIntersectionMap intersectionMap = (BasicRIMIntersectionMap) map;

        Rectangle2D mapRect = intersectionMap.getDimensions();
        // First, set up an image buffer
        Image bgImage = createImage((int) (mapRect.getWidth() * scale),
                (int) (mapRect.getHeight() * scale));
        Graphics2D bgBuffer = (Graphics2D) bgImage.getGraphics();
        // Set the transform
        AffineTransform tf = new AffineTransform();
        tf.translate(0, 0);
        tf.scale(scale, scale);
        bgBuffer.setTransform(tf);
        // create the textures that depends on the scale
        TexturePaint grassTexture = makeScaledTexture(grassImage, scale);
        TexturePaint asphaltTexture = makeScaledTexture(asphaltImage, scale);
        // paint the background with the red color in order to
        // show that no space in the buffer is not redrawn.
        paintEntireBuffer(bgBuffer, Color.RED);
        // draw the grass on the map only
        drawGrass(bgBuffer, mapRect, grassTexture);
        // Next, loop through the roads and draw them all
        for (Road road : intersectionMap.getRoads()) {
            drawRoundaboutRoad(bgBuffer, road, asphaltTexture);
        }
        // Draw all the intersections
        for (IntersectionManager im : intersectionMap.getIntersectionManagers()) {
            drawIntersectionManager(bgBuffer, im, asphaltTexture);
        }
        // Then draw the data collection lines
        drawDataCollectionLines(bgBuffer, intersectionMap.getDataCollectionLines());

        return bgImage;
    }

    /**
     * Draw an intersection on the display buffer.
     *
     * @param bgBuffer        the display buffer
     * @param im              the intersection manager
     * @param asphaltTexture  the asphaltTexture
     */
    private void drawIntersectionManager(Graphics2D bgBuffer,
                                         IntersectionManager im,
                                         TexturePaint asphaltTexture) {

        boolean selected = (Debug.getTargetIMid() == im.getId());

        // First, fill in the intersection with asphalt color/texture
        if (asphaltTexture == null) {
            bgBuffer.setPaint(ASPHALT_COLOR);
        } else {
            bgBuffer.setPaint(asphaltTexture);
        }
        bgBuffer.fill(im.getIntersection().getArea());
        // Then, outline it with the appropriate color
        if (im instanceof V2IManager) {
            if (selected) {
                bgBuffer.setPaint(SELECTED_IM_OUTLINE_COLOR);
            } else {
                bgBuffer.setPaint(IM_OUTLINE_COLOR);
            }
            bgBuffer.setStroke(IM_OUTLINE_STROKE);
            bgBuffer.draw(im.getIntersection().getArea());
        }

    }

    /**
     * Update the canvas to visualize the current state of simulation.
     */
    @Override
    protected void doUpdateCanvas() {
        super.doUpdateCanvas();
        Simulator sim = simViewer.getSimulator();
        // if the simulator exists, draw the current view
        if (sim != null) {
            Collection<IntersectionManager> ims =
                    ((BasicRIMIntersectionMap) sim.getMap()).getIntersectionManagers();
            // draw the intersection managers' debug shapes
            if (isShowIMDebugShapes) {
                for (IntersectionManager im : ims) {
                    drawIMDebugShapes(displayBuffer, im);
                }
            }
            // draw the vehicles
            for (RIMVehicleSimModel v : ((RIMSimulator) sim).getActiveVehicles()) {
                drawVehicle(displayBuffer, v, sim.getSimulationTime());
            }

            // draw simulation time.
            if (isShowSimulationTime) {
                drawSimulationTime(displayBuffer, sim.getSimulationTime());
            }
            // draw the debug points
            drawDebugPoints(displayBuffer, Debug.getLongTermDebugPoints());
            drawDebugPoints(displayBuffer, Debug.getShortTermDebugPoints());
            // draw tracks
            // drawTracks(displayBuffer);
            // lastly, draw the vehicles' information string
            for (RIMVehicleSimModel v : ((RIMSimulator) sim).getActiveVehicles()) {
                drawVehicleInfoString(displayBuffer, v, sim.getSimulationTime());
            }
            // Finally display the new image
            repaint();
        } // else no simulator no drawing
    }

    /**
     * Draw an individual Vehicle, and any associated debug information, if this
     * Vehicle is a debug Vehicle.
     *
     * @param buffer       the display buffer
     * @param vehicle      the Vehicle to draw now
     * @param currentTime  the current simulated time
     */
    @Override
    protected void drawVehicle(Graphics2D buffer,
                               VehicleSimModel vehicle,
                               double currentTime) {
        // whether the vehicle is selected
        boolean selectedVehicle = (Debug.getTargetVIN() == vehicle.getVIN());
        // check to see if we use another color
        if (selectedVehicle) {
            buffer.setPaint(VEHICLE_SELECTED_COLOR);
        } else if (vehicle.getVIN() == MARVIN_VEHICLE_VIN) {
            buffer.setPaint(MARVIN_VEHICLE_COLOR);
        } else if (Debug.getVehicleColor(vehicle.getVIN()) != null) {
            buffer.setPaint(Debug.getVehicleColor(vehicle.getVIN()));
        } else if (Debug.SHOW_VEHICLE_COLOR_BY_MSG_STATE) {
            if (vehicle.getDriver() instanceof RIMAutoDriver) {
                RIMAutoDriver autoDriver = (RIMAutoDriver) vehicle.getDriver();
                if (autoDriver.getCurrentCoordinator() instanceof V2ICoordinator) {
                    V2ICoordinator coordinator =
                            (V2ICoordinator) autoDriver.getCurrentCoordinator();
                    if (coordinator.isAwaitingResponse()) {
                        buffer.setPaint(VEHICLE_WAITING_FOR_RESPONSE_COLOR);
                    } else if (coordinator.getReservationParameter() != null) {
                        buffer.setPaint(VEHICLE_HAS_RESERVATION_COLOR);
                    } else {
                        buffer.setPaint(VEHICLE_COLOR);  // the default color
                    }
                } else {
                    buffer.setPaint(VEHICLE_COLOR);  // the default color
                }
            } else {
                buffer.setPaint(VEHICLE_COLOR);  // the default color
            }
        } else {
            buffer.setPaint(VEHICLE_COLOR);  // the default color
        }

        buffer.setStroke(VEHICLE_STROKE);

        // Now draw the vehicle's shape
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
    @Override
    protected void drawVehicleInfoString(Graphics2D buffer,
                                         VehicleSimModel vehicle,
                                         double currentTime) {
        java.util.List<String> infos = new LinkedList<String>();

        // display the vin
        if (isShowVin) {
            infos.add(Integer.toString(vehicle.getVIN()));
        }

        if (vehicle instanceof RIMAutoVehicleSimModel
                && vehicle.getDriver() instanceof RIMAutoDriver) {
            RIMAutoDriver da = (RIMAutoDriver) vehicle.getDriver();
            if (da.getCurrentCoordinator() instanceof V2ICoordinator) {
                V2ICoordinator coordinator =
                        (V2ICoordinator) da.getCurrentCoordinator();

                // display the arrival time of the request (if any)
                if (Debug.SHOW_ARRIVAL_TIME) {
                    if (coordinator.isAwaitingResponse()
                            || coordinator.getReservationParameter() != null) {
                        V2IMessage msg =
                                ((RIMAutoVehicleSimModel) vehicle).getLastV2IMessage();
                        if (msg instanceof Request) {
                            Request request = (Request) msg;
                            if (request.getProposals().size() > 0) {
                                // one arrival time is enough.
                                double arrival_time =
                                        request.getProposals().get(0).getArrivalTime();
                                infos.add(String.format("%.2f", arrival_time));
                            } else {
                                infos.add("No Proposals");
                            }
                        } // else ignore other types of messages
                    }
                }

                if (Debug.SHOW_REMAINING_ARRIVAL_TIME) {
                    if (coordinator.isAwaitingResponse()
                            || coordinator.getReservationParameter() != null) {
                        V2IMessage msg =
                                ((RIMAutoVehicleSimModel) vehicle).getLastV2IMessage();
                        if (msg instanceof Request) {
                            Request request = (Request) msg;
                            if (request.getProposals().size() > 0) {
                                // one arrival time is enough.
                                double arrival_time =
                                        request.getProposals().get(0).getArrivalTime();
                                if (coordinator.getReservationParameter() == null
                                        || arrival_time - currentTime >= 0) {
                                    infos.add(String.format("%.2f", arrival_time - currentTime));
                                }
                            } else {
                                infos.add("No Proposals");
                            }
                        } // else ignore other types of messages
                    }
                }
            }
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
     * Draw the debugging shapes that the IntersectionManagers provide. These
     * are usually things like used tiles for a tile-based reservation policy,
     * current heuristic values and so forth.
     *
     * @param buffer  the display buffer
     * @param im      the intersection manager whose debug shapes to draw
     */
    private void drawIMDebugShapes(Graphics2D buffer, IntersectionManager im) {
        for (Shape s : im.getDebugShapes()) {
            buffer.setPaint(IM_DEBUG_SHAPE_COLOR);
            buffer.fill(s);
        }
    }

    /**
     * Draw the tracks.
     *
     * @param buffer  the display buffer
     */
    protected void drawTracks(Graphics2D buffer) {
        PathTrack track = new PathTrack();

        track.add(new ArcTrack(new WayPoint(50, 100),
                new WayPoint(100, 50),
                new WayPoint(100, 100), true));
        track.add(new LineTrack(new WayPoint(100, 50),
                new WayPoint(200, 50)));
        track.add(new ArcTrack(new WayPoint(200, 50),
                new WayPoint(250, 100),
                new WayPoint(200, 100), false));


        buffer.setPaint(TRACK_COLOR);
        buffer.setStroke(TRACK_STROKE);
        buffer.draw(track.getShape());

        TrackPosition pos = track.new Position(0);
        buffer.setPaint(Color.MAGENTA);
        do {
            double x = pos.getX();
            double y = pos.getY();
            double slope = pos.getTangentSlope();
            double d = 30.0;
            double x2 = x + d * Math.cos(slope);
            double y2 = y + d * Math.sin(slope);
            buffer.draw(new Line2D.Double(x, y, x2, y2));

            double r = 2.0;
            buffer.draw(new Ellipse2D.Double(x - r, y - r, 2 * r, 2 * r));
        } while (pos.move(10.0) == 0);
    }

    /**
     * Set whether or not the canvas draws the IM shapes.
     *
     * @param useIMDebugShapes  whether or not the canvas should draw the shapes
     */
    public void setIsShowIMDebugShapes(boolean useIMDebugShapes) {
        this.isShowIMDebugShapes = useIMDebugShapes;
    }


}
