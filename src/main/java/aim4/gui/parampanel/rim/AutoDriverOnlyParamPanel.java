package aim4.gui.parampanel.rim;

import aim4.gui.component.LabeledSlider;
import aim4.sim.setup.rim.BasicSimSetup;

import javax.swing.*;

/**
 * The autonomous driver only simulation parameter panel.
 */
public class AutoDriverOnlyParamPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    LabeledSlider trafficRateSlider;
    LabeledSlider roundaboutDiameterSlider;
    LabeledSlider laneSpeedLimitSlider;
    LabeledSlider roundaboutSpeedLimitSlider;
    LabeledSlider stopDistToIntersectionSlider;
    LabeledSlider numOfColumnSlider;
    LabeledSlider numOfRowSlider;
    LabeledSlider lanesPerRoadSlider;

    /**
     * Create the autonomous driver only simulation parameter panel.
     *
     * @param simSetup  the simulation setup
     */
    public AutoDriverOnlyParamPanel(BasicSimSetup simSetup) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // create the components

        trafficRateSlider =
                new LabeledSlider(0.0, 2500.0,
                        0.0005 * 3600.0,
                        500, 1,
                        "Traffic Level: %.0f vehicles/hour/lane",
                        "%.0f");
        add(trafficRateSlider);

        roundaboutDiameterSlider =
                new LabeledSlider(30.0, 45.0,
                        simSetup.getRoundaboutDiameter(),
                        35.0, 1.0,
                        "Roundabout diameter: %.0f meters",
                        "%.0f");
        add(roundaboutDiameterSlider);

        laneSpeedLimitSlider =
                new LabeledSlider(0.0, 13.88,
                        simSetup.getLaneSpeedLimit(),
                        1, 1,
                        "Lane Speed Limit: %.0f meters/second",
                        "%.0f");
        add(laneSpeedLimitSlider);

        roundaboutSpeedLimitSlider =
                new LabeledSlider(0.0, 9.72,
                        simSetup.getRoundaboutSpeedLimit(),
                        1.0, 1.0,
                        "Roundabout Speed Limit: %.0f meters/second",
                        "%.0f");
        add(roundaboutSpeedLimitSlider);

        stopDistToIntersectionSlider =
                new LabeledSlider(0.0, 50.0,
                        simSetup.getStopDistBeforeIntersection(),
                        10.0, 1.0,
                        "Stopping Distance Before Intersection: %.0f meters",
                        "%.0f");
        add(stopDistToIntersectionSlider);

        numOfColumnSlider =
                new LabeledSlider(1.0, 1,
                        simSetup.getColumns(),
                        1.0, 1.0,
                        "Number of North-bound/South-bound Roads: %.0f",
                        "%.0f");
        add(numOfColumnSlider);

        numOfRowSlider =
                new LabeledSlider(1.0, 1,
                        simSetup.getColumns(),
                        1.0, 1.0,
                        "Number of East-bound/West-bound Roads: %.0f",
                        "%.0f");
        add(numOfRowSlider);

        lanesPerRoadSlider =
                new LabeledSlider(1.0, 1,
                        simSetup.getLanesPerRoad(),
                        1.0, 1.0,
                        "Number of Lanes per Road: %.0f",
                        "%.0f");
        add(lanesPerRoadSlider);


    }

    /**
     * Get the traffic rate.
     *
     * @return the traffic rate
     */
    public double getTrafficRate() {
        return trafficRateSlider.getValue() / 3600.0;
    }

    /**
     * Get the roundabout diameter.
     *
     * @return the speed limit
     */
    public double getRoundaboutDiameter() {
        return roundaboutDiameterSlider.getValue();
    }

    /**
     * Get the lane speed limit.
     *
     * @return the speed limit
     */
    public double getLaneSpeedLimit() {
        return laneSpeedLimitSlider.getValue();
    }

    /**
     * Get the lane speed limit.
     *
     * @return the speed limit
     */
    public double getRoundaboutSpeedLimit() {
        return roundaboutSpeedLimitSlider.getValue();
    }

    /**
     * Get the stop distance to intersection.
     *
     * @return the stop distance to intersection
     */
    public double getStopDistToIntersection() {
        double d = stopDistToIntersectionSlider.getValue();
        return (d < 1.0)?1.0:d;
    }

    /**
     * Get the number of columns.
     *
     * @return the number of columns
     */
    public int getNumOfColumns() {
        return (int)numOfColumnSlider.getValue();
    }

    /**
     * Get the number of rows.
     *
     * @return the number of rows
     */
    public int getNumOfRows() {
        return (int)numOfRowSlider.getValue();
    }

    /**
     * Get the number of lanes per road.
     *
     * @return the number of lanes per road
     */
    public int getLanesPerRoad() {
        return (int)lanesPerRoadSlider.getValue();
    }
}
