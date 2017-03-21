package aim4.gui.parampanel.cpm;

import aim4.gui.component.LabeledSlider;
import aim4.sim.setup.cpm.BasicCPMSimSetup;

import javax.swing.*;

/**
 * The autonomous driver only simulation parameter panel for CPM.
 */
public class CPMAutoDriverParamPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    LabeledSlider speedLimitSlider;
    LabeledSlider laneWidthSlider;
    LabeledSlider numberOfParkingLanesSlider;
    LabeledSlider parkingLengthSlider;
    LabeledSlider accessLengthSlider;
    LabeledSlider trafficLevelSlider;

    /**
     * Create the autonomous driver only simulation parameter panel.
     *
     * @param simSetup  the simulation setup
     */

    public CPMAutoDriverParamPanel(BasicCPMSimSetup simSetup) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));

        // create the components

        speedLimitSlider =
                new LabeledSlider(5.0, 25.0,
                        simSetup.getSpeedLimit(),
                        10.0, 5.0,
                        "Speed Limit: %.0f meters/second",
                        "%.0f");
        add(speedLimitSlider);

        laneWidthSlider =
                new LabeledSlider(1.0, 10.0,
                        simSetup.getLaneWidth(),
                        1.0, 1.0,
                        "Width of Lanes: %.0f",
                        "%.0f");
        add(laneWidthSlider);

        numberOfParkingLanesSlider =
                new LabeledSlider(1, 10,
                        simSetup.getNumberOfParkingLanes(),
                        1.0, 1.0,
                        "Number of Parking Lanes: %.0f",
                        "%.0f");
        add(numberOfParkingLanesSlider);

        parkingLengthSlider =
                new LabeledSlider(0.0, 100.0,
                        simSetup.getParkingLength(),
                        10.0, 5.0,
                        "Length of Parking: %.0f meters",
                        "%.0f");
        add(parkingLengthSlider);

        // TODO CPM What is minimum length for this?
        accessLengthSlider =
                new LabeledSlider(0.0, 10.0,
                        simSetup.getAccessLength(),
                        10.0, 1.0,
                        "Length of Parking Lane Access: %.0f meters",
                        "%.0f");
        add(accessLengthSlider);

        trafficLevelSlider =
                new LabeledSlider(0.0, 2500.0,
                        simSetup.getTrafficLevel() * 3600.0, // CPM TODO Why?
                        500.0, 100.0,
                        "Traffic Level: %.0f vehicles/hour/lane",
                        "%.0f");
        add(trafficLevelSlider);


    }


    public double getSpeedLimit() {
        return speedLimitSlider.getValue();
    }

    public double getLaneWidth() {
        return laneWidthSlider.getValue();
    }

    public double getParkingLength() {
        return parkingLengthSlider.getValue();
    }

    public double getAccessLength() {
        return accessLengthSlider.getValue();
    }

    public int getNumberOfParkingLanes() {
        return (int)numberOfParkingLanesSlider.getValue();
    }

    public double getTrafficLevel() {
        return trafficLevelSlider.getValue();
    }
}
