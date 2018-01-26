package aim4.merge.map;

import aim4.map.lane.Lane;
import aim4.map.lane.LineSegmentLane;
import aim4.map.merge.MergeSpawnPoint;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

/**
 * Created by Callum on 15/03/2017.
 */
public class MergeSpawnPointTest {
    private static final double LANE_WIDTH = 4.0;
    private static final double SPEED_LIMIT = 60.0;

    //MergeSpawnPoint parameters
    private double currentTime;
    private double heading;
    private double steeringAngle;
    private double acceleration;
    private Point2D pos;
    private Point2D laneStart;
    private Point2D laneEnd;
    private Lane lane;
    private GeneralPath noVehicleZone;
    private MergeSpawnPoint spawnPoint;

    //Vehicle Spec Properties
    private MergeSpawnPoint.MergeSpawnSpecGenerator mockVehicleSpecChooser;
    private MergeSpawnPoint.MergeSpawnSpec mockMergeSpawnSpec1;
    private MergeSpawnPoint.MergeSpawnSpec mockMergeSpawnSpec2;
    private MergeSpawnPoint.MergeSpawnSpec mockMergeSpawnSpec3;


    @Before
    public void setUp() throws Exception {
        //Create lane
        laneStart = new Point2D.Double(10.0, 20.0);
        laneEnd = new Point2D.Double(300.0, 600.0);
        lane = new LineSegmentLane(laneStart, laneEnd, LANE_WIDTH, SPEED_LIMIT);

        //Setup calculation tools for other parameters
        double startDistance = 0.0;
        double noVehicleZoneLength = 10.0;
        double normalisedStartDistance = lane.normalizedDistance(startDistance);

        currentTime = 10.0;
        pos = lane.getPointAtNormalizedDistance(normalisedStartDistance);
        heading = lane.getInitialHeading();
        steeringAngle = 12.0;
        acceleration = 5.0;
        noVehicleZone = new GeneralPath(
                lane.getShape(
                        normalisedStartDistance,
                        lane.normalizedDistance(startDistance + noVehicleZoneLength)
                ).getBounds2D()
        );

        //Create Spawn Point
        spawnPoint = new MergeSpawnPoint(
                currentTime,
                pos,
                heading,
                steeringAngle,
                acceleration,
                lane,
                noVehicleZone
        );

        //Create test merge specs
        mockMergeSpawnSpec1 = mock(MergeSpawnPoint.MergeSpawnSpec.class);
        mockMergeSpawnSpec2 = mock(MergeSpawnPoint.MergeSpawnSpec.class);
        mockMergeSpawnSpec3 = mock(MergeSpawnPoint.MergeSpawnSpec.class);
        when(mockMergeSpawnSpec1.toString()).thenReturn("Spec1");
        when(mockMergeSpawnSpec2.toString()).thenReturn("Spec2");
        when(mockMergeSpawnSpec3.toString()).thenReturn("Spec3");

        mockVehicleSpecChooser = mock(MergeSpawnPoint.MergeSpawnSpecGenerator.class);
        when(mockVehicleSpecChooser.act(any(MergeSpawnPoint.class), anyDouble())).thenReturn(
                new ArrayList<MergeSpawnPoint.MergeSpawnSpec>(){{
                    add(mockMergeSpawnSpec1);
                    add(mockMergeSpawnSpec2);
                    add(mockMergeSpawnSpec3);
                }}
        );

        spawnPoint.setVehicleSpecChooser(mockVehicleSpecChooser);
    }

    @Test
    public void testConstructor() throws Exception {
        assertEquals(spawnPoint.getCurrentTime(), currentTime, 0);
        assertEquals(spawnPoint.getPosition(), pos);
        assertEquals(spawnPoint.getHeading(), heading, 0);
        assertEquals(spawnPoint.getSteeringAngle(), steeringAngle, 0);
        assertEquals(spawnPoint.getAcceleration(), acceleration, 0);
        assertEquals(spawnPoint.getLane(), lane);
        assertEquals(spawnPoint.getNoVehicleZone(), noVehicleZone);
    }

    @Test
    public void testAct() throws Exception {
        double timestep = 5.0;
        List<MergeSpawnPoint.MergeSpawnSpec> results = spawnPoint.act(timestep);

        assertEquals("mergeSpawnSpec1 did not match the results of the act", results.get(0), mockMergeSpawnSpec1);
        assertEquals("mergeSpawnSpec2 did not match the results of the act", results.get(1), mockMergeSpawnSpec2);
        assertEquals("mergeSpawnSpec3 did not match the results of the act", results.get(2), mockMergeSpawnSpec3);
        assertEquals("mergeSpawnSpec1 toString did not match the results of the act list value toString",
                results.get(0).toString(), mockMergeSpawnSpec1.toString());
        assertEquals("mergeSpawnSpec2 toString did not match the results of the act list value toString",
                results.get(1).toString(), mockMergeSpawnSpec2.toString());
        assertEquals("mergeSpawnSpec3 toString did not match the results of the act list value toString",
                results.get(2).toString(), mockMergeSpawnSpec3.toString());
        assertEquals(currentTime + timestep, spawnPoint.getCurrentTime(), 0);
    }

    @Test
    public void testSetVehicleSpecChooser() throws Exception {
        final Field field = MergeSpawnPoint.class.getDeclaredField("vehicleSpecChooser");
        field.setAccessible(true);
        assertEquals("Fields didn't match", field.get(spawnPoint), mockVehicleSpecChooser);
    }

    @Test
    public void testGetCurrentTime() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("currentTime");
        field.setAccessible(true);

        double testValue = currentTime + 1;
        field.set(spawnPoint, testValue);

        final double result = spawnPoint.getCurrentTime();
        assertEquals(result, testValue, 0);
    }

    @Test
    public void testGetPosition() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("pos");
        field.setAccessible(true);

        Point2D testValue = mock(Point2D.class);
        field.set(spawnPoint, testValue);

        final Point2D result = spawnPoint.getPosition();
        assertEquals("pos not returned correctly", result, testValue);
    }

    @Test
    public void testGetHeading() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("heading");
        field.setAccessible(true);

        double testValue = heading + 1;
        field.set(spawnPoint, testValue);

        final double result = spawnPoint.getHeading();
        assertEquals(result, testValue, 0);
    }

    @Test
    public void testGetSteeringAngle() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("steeringAngle");
        field.setAccessible(true);

        double testValue = steeringAngle + 1;
        field.set(spawnPoint, testValue);

        final double result = spawnPoint.getSteeringAngle();
        assertEquals(result, testValue, 0);
    }

    @Test
    public void testGetAcceleration() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("acceleration");
        field.setAccessible(true);

        double testValue = acceleration + 1;
        field.set(spawnPoint, testValue);

        final double result = spawnPoint.getAcceleration();
        assertEquals(result, testValue, 0);
    }

    @Test
    public void testGetLane() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("lane");
        field.setAccessible(true);

        Lane testValue = mock(Lane.class);
        field.set(spawnPoint, testValue);

        final Lane result = spawnPoint.getLane();
        assertEquals("lane not returned correctly", result, testValue);
    }

    @Test
    public void testGetNoVehicleZone() throws Exception {
        final Field field = MergeSpawnPoint.class.getSuperclass().getDeclaredField("noVehicleZone");
        field.setAccessible(true);

        Rectangle2D testValue = mock(Rectangle2D.class);
        field.set(spawnPoint, testValue);

        final Shape result = spawnPoint.getNoVehicleZone();
        assertEquals("noVehicleZone not returned correctly", result, testValue);
    }
}