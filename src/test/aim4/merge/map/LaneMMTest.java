package aim4.merge.map;

import aim4.config.SimConfig;
import aim4.im.merge.reservation.nogrid.ReservationMergeManager;
import aim4.map.connections.MergeConnection;
import aim4.map.lane.Lane;
import aim4.map.merge.MergeMapUtil;
import aim4.map.merge.S2SMergeMap;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Created by Callum on 14/04/2017.
 */
public class LaneMMTest {
    private final static double SPEED_LIMIT = 60.0;
    //S2S PROPERTIES
    private final static double TARGET_LEAD_IN_DISTANCE = 150.0;
    private final static double TARGET_LEAD_OUT_DISTANCE = 150.0;
    private final static double MERGE_LEAD_IN_DISTANCE = 150.0;

    @Test
    public void testDistanceToNextMerge() throws Exception {
        List<Double> testAngles = new ArrayList<Double>();
        testAngles.add(10.0);
        testAngles.add(20.0);
        testAngles.add(30.0);
        testAngles.add(40.0);
        testAngles.add(45.0);
        testAngles.add(50.0);
        testAngles.add(60.0);
        testAngles.add(70.0);
        testAngles.add(80.0);
        testAngles.add(90.0);
        for(double mergeAngle : testAngles) {
            testDistanceToNextMerge(mergeAngle);
        }
    }

    private void testDistanceToNextMerge(double mergeAngle) {
        //Set up
        double currentTime = 0;
        S2SMergeMap map = new S2SMergeMap(currentTime,
                SPEED_LIMIT, SPEED_LIMIT, TARGET_LEAD_IN_DISTANCE,
                TARGET_LEAD_OUT_DISTANCE, MERGE_LEAD_IN_DISTANCE,
                mergeAngle);
        ReservationMergeManager.Config mergeReservationConfig =
                new ReservationMergeManager.Config(SimConfig.TIME_STEP, SimConfig.MERGE_TIME_STEP);
        MergeMapUtil.setFCFSMergeManagers(map, currentTime, mergeReservationConfig);
        MergeConnection merge = map.getMergeManagers().get(0).getMergeConnection();
        Lane targetLane = null;
        Lane mergeLane = null;
        for(Lane l : merge.getLanes()) {
            if(l.getInitialHeading() == 0)
                targetLane = l;
            else
                mergeLane = l;
        }
        assert mergeLane != null && targetLane != null;
        double mergeDistance = mergeLane.getStartPoint().distance(merge.getEntryPoint(mergeLane));
        double targetDistance = targetLane.getStartPoint().distance(merge.getEntryPoint(targetLane));

        //Test
        assertEquals(mergeDistance, mergeLane.getLaneMM().distanceToNextMerge(mergeLane.getStartPoint()), 0.0001);
        assertEquals(targetDistance, targetLane.getLaneMM().distanceToNextMerge(targetLane.getStartPoint()), 0.0001);
    }
}