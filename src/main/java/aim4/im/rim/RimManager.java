package aim4.im.rim;

import aim4.map.Road;
import aim4.map.connections.RimConnection;
import aim4.map.lane.Lane;
import aim4.vehicle.VehicleSimModel;

import java.awt.geom.Rectangle2D;

public interface RimManager {
    /**
     * Takes any actions required for a certain period of time.
     * @param timeStep the size of the timestep to simulate in seconds
     */
    public void act(double timeStep);

    //ACCESSORS
    /**
     * Get the unique ID number of this Rim Manager.
     *
     * @return the ID number of this Rim Manager
     */
    public int getId();

    /**
     * Returns the current time
     * @return The current simulation time.
     */
    public double getCurrentTime();

    /**
     * Returns the RimConnection managed
     * @return The RimConnection managed by this RimManager.
     */
    public RimConnection getRimConnection();

    //MANAGEMENT
    /**
     * Returns true if this RimManager manages the road provided.
     * @param r A Road to test
     * @return A boolean indicating whether this RimManager manages the road provided.
     */
    public boolean manages(Road r);

    /**
     * Returns true if this RimManager manages the lane provided.
     * @param l A lane to test
     * @return A boolean indicating whether this RimManager manages the lane provided.
     */
    public boolean manages(Lane l);

    //CHECKS
    /**
     * Determine whether the given Vehicle is currently entirely contained
     * within the Area governed by this IntersectionManager.
     *
     * @param vehicle the Vehicle
     * @return        whether the Vehicle is currently entirely contained within
     *                the Area governed by this IntersectionManager
     */
    public boolean contains(VehicleSimModel vehicle);

    /**
     * Returns true if the Rectangle2D provided intersects the RimConnection the RimManager is managing.
     * @param rectangle The rectangle to check
     * @return A boolean indicating whether the rectangle provided intersects the RimConnection managed.
     */
    public boolean intersects(Rectangle2D rectangle);
}
