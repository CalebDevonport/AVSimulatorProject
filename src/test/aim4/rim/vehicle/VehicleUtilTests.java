package aim4.rim.vehicle;

import aim4.config.Debug;
import aim4.im.rim.IntersectionManager;
import aim4.im.rim.RoadBasedIntersection;
import aim4.im.rim.RoadBasedTrackModel;
import aim4.im.rim.TrackModel;
import aim4.map.Road;
import aim4.map.lane.Lane;
import aim4.map.rim.RimIntersectionMap;
import aim4.util.ArrayListRegistry;
import aim4.util.Registry;
import aim4.vehicle.VehicleSpec;
import aim4.vehicle.VehicleSpecDatabase;
import aim4.vehicle.VehicleUtil;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

public class VehicleUtilTests {
    private static final double CURRENT_TIME = 1.0;
    private static final List<Double> ROUNDABOUT_DIAMETER = Arrays.asList(30.0, 35.0, 40.0, 45.0);
    private static final double ENTRANCE_EXIT_RADIUS =  20.0;
    private static final double LANE_WIDTH =  3.014;
    private static final double LANE_SPEED_LIMIT =  19.44;
    private static final double ROUNDABOUT_SPEED_LIMIT =  9.7222;

    @Test
    public void safeToCross_withValidTraversalVelocityAndMinRoundaboutDiameterAndNorthEast_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec
        VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        // Set traversal velocity
        double traversalVelocity = ROUNDABOUT_SPEED_LIMIT;

        //act
       boolean safeToCross = VehicleUtil.safeToCross(spec, arrivalLane, departureLane, intersectionManager, traversalVelocity);

        //assert
        assert safeToCross == true;

    }

    @Test
    public void safeToCross_withValidTraversalVelocityAndMaxRoundaboutDiameterAndNorthEast_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec
        VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        // Set traversal velocity
        double traversalVelocity = ROUNDABOUT_SPEED_LIMIT;

        //act
        boolean safeToCross = VehicleUtil.safeToCross(spec, arrivalLane, departureLane, intersectionManager, traversalVelocity);

        //assert
        assert safeToCross == true;

    }

    @Test
    public void safeToCross_withValidTraversalVelocityAndMinRoundaboutDiameterAndNorthNorth_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec
        VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getNorthRoad().getExitApproachLane();

        // Set traversal velocity
        double traversalVelocity = ROUNDABOUT_SPEED_LIMIT;

        //act
        boolean safeToCross = VehicleUtil.safeToCross(spec, arrivalLane, departureLane, intersectionManager, traversalVelocity);

        //assert
        assert safeToCross == true;

    }

    @Test
    public void safeToCross_withValidTraversalVelocityAndMaxRoundaboutDiameterAndNorthNorth_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec
        VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getNorthRoad().getExitApproachLane();

        // Set traversal velocity
        double traversalVelocity = ROUNDABOUT_SPEED_LIMIT;

        //act
        boolean safeToCross = VehicleUtil.safeToCross(spec, arrivalLane, departureLane, intersectionManager, traversalVelocity);

        //assert
        assert safeToCross == true;

    }

    @Test
    public void safeToCross_withValidTraversalVelocityAndMinRoundaboutDiameterAndNorthWest_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec
        VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        // Set traversal velocity
        double traversalVelocity = ROUNDABOUT_SPEED_LIMIT;

        //act
        boolean safeToCross = VehicleUtil.safeToCross(spec, arrivalLane, departureLane, intersectionManager, traversalVelocity);

        //assert
        assert safeToCross == true;

    }

    @Test
    public void safeToCross_withValidTraversalVelocityAndMaxRoundaboutDiameterAndNorthWest_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec
        VehicleSpec spec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        // Set traversal velocity
        double traversalVelocity = ROUNDABOUT_SPEED_LIMIT;

        //act
        boolean safeToCross = VehicleUtil.safeToCross(spec, arrivalLane, departureLane, intersectionManager, traversalVelocity);

        //assert
        assert safeToCross == true;

    }

    @Test
    public void calculateMaxTurnVelocity_withMinRoundaboutDiameterAndNorthWest_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        //act
        double calculateMaxTurnVelocityForVAN = VehicleUtil.calculateMaxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double calculateMaxTurnVelocityForCOUPE = VehicleUtil.calculateMaxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert calculateMaxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForVAN != 0.0;
        assert calculateMaxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void calculateMaxTurnVelocity_withMinRoundaboutDiameterAndNorthEast_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getEastRoad().getExitApproachLane();

        //act
        double calculateMaxTurnVelocityForVAN = VehicleUtil.calculateMaxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double calculateMaxTurnVelocityForCOUPE = VehicleUtil.calculateMaxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert calculateMaxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForVAN != 0.0;
        assert calculateMaxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void calculateMaxTurnVelocity_withMaxRoundaboutDiameterAndNorthWest_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        //act
        double calculateMaxTurnVelocityForVAN = VehicleUtil.calculateMaxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double calculateMaxTurnVelocityForCOUPE = VehicleUtil.calculateMaxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert calculateMaxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForVAN != 0.0;
        assert calculateMaxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void calculateMaxTurnVelocity_withMaxRoundaboutDiameterAndNorthEast_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getEastRoad().getExitApproachLane();

        //act
        double calculateMaxTurnVelocityForVAN = VehicleUtil.calculateMaxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double calculateMaxTurnVelocityForCOUPE = VehicleUtil.calculateMaxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert calculateMaxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert calculateMaxTurnVelocityForVAN != 0.0;
        assert calculateMaxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void maxTurnVelocity_withMinRoundaboutDiameterAndNorthEast_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getEastRoad().getExitApproachLane();

        //act
        double maxTurnVelocityForVAN = VehicleUtil.maxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double maxTurnVelocityForCOUPE = VehicleUtil.maxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert maxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForVAN != 0.0;
        assert maxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void maxTurnVelocity_withMaxRoundaboutDiameterAndNorthEast_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getEastRoad().getExitApproachLane();

        //act
        double maxTurnVelocityForVAN = VehicleUtil.maxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double maxTurnVelocityForCOUPE = VehicleUtil.maxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert maxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForVAN != 0.0;
        assert maxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void maxTurnVelocity_withMinRoundaboutDiameterAndNorthWest_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(0));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        //act
        double maxTurnVelocityForVAN = VehicleUtil.maxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double maxTurnVelocityForCOUPE = VehicleUtil.maxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert maxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForVAN != 0.0;
        assert maxTurnVelocityForCOUPE != 0.0;

    }

    @Test
    public void maxTurnVelocity_withMaxRoundaboutDiameterAndNorthWest_returnsTrue() {
        //arrange

        // Create map
        RimIntersectionMap map = getRimIntersectionMap(ROUNDABOUT_DIAMETER.get(3));

        // Create intersection
        RoadBasedIntersection roadBasedIntersection = new RoadBasedIntersection(map.getRoads());

        // Create track model
        TrackModel trackModel = new RoadBasedTrackModel(roadBasedIntersection);

        // IM Registry
        Registry<IntersectionManager> registry = new ArrayListRegistry<>();

        // Create IM
        IntersectionManager intersectionManager = new IntersectionManager(roadBasedIntersection, trackModel, CURRENT_TIME, registry);


        // Set vehicle spec for a van
        VehicleSpec vanSpec = VehicleSpecDatabase.getVehicleSpecByName("VAN");

        // Set vehicle spec for a coupe
        VehicleSpec coupeSpec = VehicleSpecDatabase.getVehicleSpecByName("COUPE");

        // Set arrival lane
        Lane arrivalLane = getNorthRoad().getEntryApproachLane();
        Lane departureLane = getWestRoad().getExitApproachLane();

        //act
        double maxTurnVelocityForVAN = VehicleUtil.maxTurnVelocity(vanSpec, arrivalLane, departureLane, intersectionManager);
        double maxTurnVelocityForCOUPE = VehicleUtil.maxTurnVelocity(coupeSpec, arrivalLane, departureLane, intersectionManager);

        //assert
        assert maxTurnVelocityForVAN < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForCOUPE < ROUNDABOUT_SPEED_LIMIT;
        assert maxTurnVelocityForVAN != 0.0;
        assert maxTurnVelocityForCOUPE != 0.0;

    }

    private RimIntersectionMap getRimIntersectionMap(double roundaboutDiameter) {
        return new RimIntersectionMap(
                0,
                1,
                1,
                roundaboutDiameter,
                ENTRANCE_EXIT_RADIUS,
                4,
                LANE_WIDTH,
                LANE_SPEED_LIMIT,
                ROUNDABOUT_SPEED_LIMIT,
                1,
                0,
                0);
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
