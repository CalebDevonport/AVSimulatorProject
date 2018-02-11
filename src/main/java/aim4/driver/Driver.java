package aim4.driver;

import aim4.map.lane.Lane;

public interface Driver extends DriverSimModel {
    void addCurrentlyOccupiedLane(Lane lane);
}
