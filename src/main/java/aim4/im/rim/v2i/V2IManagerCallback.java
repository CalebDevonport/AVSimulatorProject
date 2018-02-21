package aim4.im.rim.v2i;


import aim4.im.AczManager;
import aim4.im.AdmissionControlZone;
import aim4.im.rim.Intersection;
import aim4.im.rim.TrackModel;
import aim4.im.rim.v2i.reservation.ReservationGrid;
import aim4.im.rim.v2i.reservation.ReservationGridManager;
import aim4.msg.rim.i2v.I2VMessage;

/**
 * An interface of the methods of V2IManager that are available for
 * the policies.
 */
public interface V2IManagerCallback {
    /**
     * A callback method for sending a I2V message.
     *
     * @param msg a I2V message
     */
    void sendI2VMessage(I2VMessage msg);

    /**
     * Get the id of the intersection manager.
     *
     * @return the id of the intersection manager.
     */
    int getId();

    /**
     * Get the current time
     *
     * @return the current time
     */
    double getCurrentTime();

    /**
     * Get the intersection managed by this intersection manager.
     *
     * @return the intersection managed by this intersection manager
     */
    Intersection getIntersection();


    // TODO: remove this function
    TrackModel getTrackModel();

    /**
     * Get the reservation grid.
     *
     * @return the reservation grid
     */
    ReservationGrid getReservationGrid();

    /**
     * Get the manager of the reservation grid
     *
     * @return the manager of the reservation grid
     */
    ReservationGridManager getReservationGridManager();

    /**
     * Get the Admission Control Zone of a given lane.
     *
     * @param laneId  the id of the lane
     * @return the admission control zone of the lane.
     */
    AdmissionControlZone getACZ(int laneId);

    /**
     * Get the manager of an ACZ
     */
    AczManager getAczManager(int laneId);


}
