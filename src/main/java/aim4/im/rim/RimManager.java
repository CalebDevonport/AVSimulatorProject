package aim4.im.rim;

public interface RimManager {
    /**
     * Takes any actions required for a certain period of time.
     * @param timeStep the size of the time step to simulate in seconds
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
}
