package aim4.merge.map;

import aim4.map.DataCollectionLine;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.merge.MergeMap;
import aim4.map.merge.MergeSpawnPoint;
import aim4.util.HashMapRegistry;
import aim4.util.Registry;
import com.sun.scenario.effect.Merge;
import org.junit.Before;
import org.junit.Test;

import javax.xml.crypto.Data;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Created by Callum on 15/03/2017.
 */
public class MergeMapTest {
    private MergeMap mergeMap;

    @Before
    public void setUp() throws Exception {
        mergeMap = new MergeMap();
    }

    @Test
    public void testGetRoads() throws Exception {
        final Field field = MergeMap.class.getDeclaredField("roads");
        field.setAccessible(true);

        List<Road> testValue = new ArrayList<Road>(){{
            add(mock(Road.class));
            add(mock(Road.class));
            add(mock(Road.class));
        }};
        field.set(mergeMap, testValue);

        final List<Road> result = mergeMap.getRoads();
        assertEquals(result, testValue);
    }

    @Test
    public void testGetDimensions() throws Exception {
        final Field field = MergeMap.class.getDeclaredField("dimensions");
        field.setAccessible(true);

        Rectangle2D testValue = mock(Rectangle2D.class);
        field.set(mergeMap, testValue);

        final Rectangle2D result = mergeMap.getDimensions();
        assertEquals(result, testValue);
    }

    @Test
    public void testGetMaximumSpeedLimit() throws Exception {
        //Setup
        double maxSpeedLimit = 65.0;
        Road road1 = mock(Road.class);
        Road road2 = mock(Road.class);
        Lane lane11 = mock(Lane.class);
        Lane lane12 = mock(Lane.class);
        Lane lane13 = mock(Lane.class);
        Lane lane21 = mock(Lane.class);
        Lane lane22 = mock(Lane.class);
        Lane lane23 = mock(Lane.class);

        //Create Lane behaviour
        when(lane11.getSpeedLimit()).thenReturn(-maxSpeedLimit);
        when(lane12.getSpeedLimit()).thenReturn(0.0);
        when(lane13.getSpeedLimit()).thenReturn(maxSpeedLimit - (maxSpeedLimit/2) - (maxSpeedLimit/4));
        when(lane21.getSpeedLimit()).thenReturn(maxSpeedLimit);
        when(lane22.getSpeedLimit()).thenReturn(maxSpeedLimit - (maxSpeedLimit/2));
        when(lane23.getSpeedLimit()).thenReturn(maxSpeedLimit - (maxSpeedLimit/4));

        //Create Lane Lists
        List<Lane> road1Lanes = new ArrayList<Lane>();
        road1Lanes.add(lane11);
        road1Lanes.add(lane12);
        road1Lanes.add(lane13);

        List<Lane> road2Lanes = new ArrayList<Lane>();
        road1Lanes.add(lane21);
        road1Lanes.add(lane22);
        road1Lanes.add(lane23);

        //Create Road behaviour
        when(road1.getLanes()).thenReturn(road1Lanes);
        when(road2.getLanes()).thenReturn(road2Lanes);

        //Create Road list
        List<Road> roads = new ArrayList<Road>();
        roads.add(road1);
        roads.add(road2);

        //Set roads
        final Field roadsField = MergeMap.class.getDeclaredField("roads");
        roadsField.setAccessible(true);

        roadsField.set(mergeMap, roads);

        //Test
        final Field memoMaximumSpeedLimitField = MergeMap.class.getDeclaredField("memoMaximumSpeedLimit");
        memoMaximumSpeedLimitField.setAccessible(true);

        assertTrue((Double) memoMaximumSpeedLimitField.get(mergeMap) < 0);
        assertEquals(mergeMap.getMaximumSpeedLimit(), maxSpeedLimit, 0);
        assertEquals(memoMaximumSpeedLimitField.get(mergeMap), maxSpeedLimit);
    }

    @Test
    public void testGetLaneRegistry() throws Exception {
        final Field field = MergeMap.class.getDeclaredField("laneRegistry");
        field.setAccessible(true);

        Registry<Lane> testValue = new HashMapRegistry<Lane>(){{
            register(mock(Lane.class));
            register(mock(Lane.class));
            register(mock(Lane.class));
        }};
        field.set(mergeMap, testValue);

        final Registry<Lane> result = mergeMap.getLaneRegistry();
        assertEquals(result, testValue);
    }

    @Test
    public void testGetRoad() throws Exception {
        final Field laneToRoadField = MergeMap.class.getDeclaredField("laneToRoad");
        laneToRoadField.setAccessible(true);
        final Field laneRegistryField = MergeMap.class.getDeclaredField("laneRegistry");
        laneRegistryField.setAccessible(true);

        Lane lane1 = mock(Lane.class);
        Lane lane2 = mock(Lane.class);
        Lane lane3 = mock(Lane.class);
        Road road1 = mock(Road.class);
        Road road2 = mock(Road.class);
        Road road3 = mock(Road.class);

        Registry<Lane> mockLaneRegistry = new HashMapRegistry<Lane>();
        mockLaneRegistry.register(lane1);
        mockLaneRegistry.register(lane2);
        mockLaneRegistry.register(lane3);

        Map<Lane, Road> mockLaneToRoad = new HashMap<Lane, Road>();
        mockLaneToRoad.put(lane1, road1);
        mockLaneToRoad.put(lane2, road2);
        mockLaneToRoad.put(lane3, road3);

        laneToRoadField.set(mergeMap, mockLaneToRoad);
        laneRegistryField.set(mergeMap, mockLaneRegistry);

        //Lane ID
        assertEquals(mergeMap.getRoad(0), road1);
        assertEquals(mergeMap.getRoad(1), road2);
        assertEquals(mergeMap.getRoad(2), road3);

        //Lane
        assertEquals(mergeMap.getRoad(lane1), road1);
        assertEquals(mergeMap.getRoad(lane2), road2);
        assertEquals(mergeMap.getRoad(lane3), road3);
    }

    @Test
    public void testGetDataCollectionLines() throws Exception {
        final Field field = MergeMap.class.getDeclaredField("dataCollectionLines");
        field.setAccessible(true);

        List<DataCollectionLine> testValue = new ArrayList<DataCollectionLine>(){{
            add(mock(DataCollectionLine.class));
            add(mock(DataCollectionLine.class));
            add(mock(DataCollectionLine.class));
        }};
        field.set(mergeMap, testValue);

        final List<DataCollectionLine> result = mergeMap.getDataCollectionLines();
        assertEquals(result, testValue);
    }

    @Test
    public void testGetSpawnPoints() throws Exception {
        final Field field = MergeMap.class.getDeclaredField("spawnPoints");
        field.setAccessible(true);

        List<MergeSpawnPoint> testValue = new ArrayList<MergeSpawnPoint>(){{
            add(mock(MergeSpawnPoint.class));
            add(mock(MergeSpawnPoint.class));
            add(mock(MergeSpawnPoint.class));
        }};
        field.set(mergeMap, testValue);

        final List<MergeSpawnPoint> result = mergeMap.getSpawnPoints();
        assertEquals(result, testValue);
    }
}