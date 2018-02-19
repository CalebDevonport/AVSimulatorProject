package aim4.msg.rim.v2i;

import aim4.config.Constants;

/**
 * Message sent from a Vehicle to a RIM Intersection Manager to inform it that it
 * has completed its reservation.
 */
public class Done extends V2IMessage{
    /////////////////////////////////
    // PRIVATE FIELDS
    /////////////////////////////////

    /**
     * The ID number of the reservation.
     */
    private int reservationID;


    /////////////////////////////////
    // CLASS CONSTRUCTORS
    /////////////////////////////////

    /**
     * Basic class constructor with all required fields.
     *
     * @param sourceID              the ID number of the Vehicle sending this
     *                              message
     * @param destinationID         the ID number of the IntersectionManager to
     * @param reservationID         the ID number of the reservation
     */
    public Done(int sourceID, int destinationID, int reservationID) {
        // Set source and destination
        super(sourceID, destinationID);
        this.reservationID = reservationID;
        messageType = Type.DONE;
        size += Constants.INTEGER_SIZE;
    }

    /////////////////////////////////
    // PUBLIC METHODS
    /////////////////////////////////

    /**
     * Get the ID number of the reservation.
     */
    public int getReservationID() {
        return reservationID;
    }

    /////////////////////////////////
    // DEBUG
    /////////////////////////////////

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Done(vin" + getVin() + " -> im" + getImId() +
                ", id" + reservationID + ")";
    }
}
