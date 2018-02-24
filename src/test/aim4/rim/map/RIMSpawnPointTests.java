package aim4.rim.map;

import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.im.rim.v2i.reservation.ReservationGridManager;
import aim4.map.Road;
import aim4.map.rim.RimIntersectionMap;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class RIMSpawnPointTests {
    private static final double ARRIVAL_TIME = 6.0;
    private static final double CURRENT_TIME = 1.0;
    private static final double STATIC_BUFFER_SIZE = 0.25;
    private static final double INTERNAL_TILE_TIME_BUFFER_SIZE = 0.1;
    private static final double GRANULARITY = 6.0;
    private static final List<Double> ROUNDABOUT_DIAMETER = Arrays.asList(30.0, 35.0, 40.0, 45.0);
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  6.04;

    @Test
    public void initialiseSpawnPoints_withMap_returnsSpawnPoints() {
        //act
        RimIntersectionMap map = getRimIntersectionMap();

        //assert
       assert map.getHorizontalSpawnPoints().size() == 2;
       assert map.getVerticalSpawnPoints().size() == 2;


    }

    private RimIntersectionMap getRimIntersectionMap() {
        return new RimIntersectionMap(
                0,
                1,
                1,
                ROUNDABOUT_DIAMETER.get(3),
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);
    }

    private ReservationGridManager.Config getConfig() {
        return new ReservationGridManager.Config(SimConfig.TIME_STEP,
                SimConfig.GRID_TIME_STEP,
                STATIC_BUFFER_SIZE,
                INTERNAL_TILE_TIME_BUFFER_SIZE,
                GRANULARITY);
    }

    private Road getNorthRoad(){ return Debug.currentRimMap.getRoads().get(2); }

    private Road getEastRoad(){
        return Debug.currentRimMap.getRoads().get(0);
    }

    private Road getSouthRoad(){
        return Debug.currentRimMap.getRoads().get(3);
    }

    private Road getWestRoad(){
        return Debug.currentRimMap.getRoads().get(1);
    }
}
