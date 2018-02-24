package aim4.im.rim.v2i.policy;

import aim4.im.rim.v2i.reservation.ReservationGrid;

/**
 * An extension to the base policy's callback interface.
 */
public interface ExtendedBasePolicyCallback extends BasePolicyCallback{
    /**
     * Get the reservation grid.
     *
     * @return the reservation grid.
     */
    ReservationGrid getReservationGrid();
}
