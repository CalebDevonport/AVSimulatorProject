package aim4.gui.parampanel.rim;

import aim4.gui.component.LabeledSlider;
import aim4.sim.setup.rim.BasicSimSetup;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

/**
 * The no protocol simulation parameter panel for rim.
 */
public class RIMOptimalProtocolParamPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    LabeledSlider trafficRateSlider;
    LabeledSlider roundaboutDiameterSlider;
    LabeledSlider laneSpeedLimitSlider;
    LabeledSlider roundaboutSpeedLimitSlider;
    LabeledSlider stopDistToIntersectionSlider;
    LabeledSlider numOfColumnSlider;
    LabeledSlider numOfRowSlider;
    LabeledSlider lanesPerRoadSlider;

    /**The JPanel containing all of the option sliders**/
    private JPanel optionPane;
    public JTextArea uploadTrafficScheduleTextbox;

    //JSON FILES
    //todo: set these to private
    public File uploadTrafficSchedule;

    //ENUM FOR BUTTON ACTIONS//
    private enum ButtonActionCommands {
        UPLOAD_CLEAR,
        UPLOAD_SELECT
    }


    /**
     * Create the autonomous driver only simulation parameter panel.
     *
     * @param simSetup  the simulation setup
     */
    public RIMOptimalProtocolParamPanel(BasicSimSetup simSetup) {
        setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));


        // create the components

        trafficRateSlider =
                new LabeledSlider(0.0, 2500.0,
                        simSetup.getTrafficLevel() * 3600.0,
                        500, 100,
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
                new LabeledSlider(0.0, 30.0,
                        simSetup.getLaneSpeedLimit(),
                        1, 1,
                        "Lane Speed Limit: %.0f meters/second",
                        "%.0f");
        add(laneSpeedLimitSlider);

        roundaboutSpeedLimitSlider =
                new LabeledSlider(0.0, 10.0,
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
                new LabeledSlider(1.0, 2.0,
                        simSetup.getLanesPerRoad(),
                        1.0, 1.0,
                        "Number of Lanes per Road: %.0f",
                        "%.0f");
        add(lanesPerRoadSlider);

        //Schedule selector from file
        JLabel uploadTrafficScheduleLabel = new JLabel("Upload Schedule:");
        uploadTrafficScheduleTextbox = new JTextArea(1,20);
        JButton uploadTrafficScheduleSelectButton = new JButton("Browse...");
        JButton uploadTrafficScheduleClearButton = new JButton("Clear");

        //Set its buttons
        uploadTrafficScheduleSelectButton.addActionListener(this);
        uploadTrafficScheduleSelectButton.setActionCommand(ButtonActionCommands.UPLOAD_SELECT.toString());
        uploadTrafficScheduleClearButton.addActionListener(this);
        uploadTrafficScheduleClearButton.setActionCommand(ButtonActionCommands.UPLOAD_CLEAR.toString());

        //Create schedule pane
        JPanel uploadTrafficSchedulePane = new JPanel();
        uploadTrafficSchedulePane.setLayout(new FlowLayout());

        uploadTrafficSchedulePane.add(uploadTrafficScheduleLabel);
        uploadTrafficSchedulePane.add(uploadTrafficScheduleTextbox);
        uploadTrafficSchedulePane.add(uploadTrafficScheduleSelectButton);
        uploadTrafficSchedulePane.add(uploadTrafficScheduleClearButton);


        optionPane = new JPanel();
        optionPane.setLayout(new BoxLayout(optionPane, BoxLayout.PAGE_AXIS));
        optionPane.add(uploadTrafficSchedulePane);
        add(optionPane, BorderLayout.CENTER);


    }

    public void actionPerformed(ActionEvent e) {
        switch(ButtonActionCommands.valueOf(e.getActionCommand())) {
            case UPLOAD_CLEAR:
                uploadTrafficSchedule = null;
                uploadTrafficScheduleTextbox.setText("");
                break;
            case UPLOAD_SELECT:
                uploadTrafficSchedule = getFileFromUser();
                if(uploadTrafficSchedule != null)
                    uploadTrafficScheduleTextbox.setText(uploadTrafficSchedule.getAbsolutePath());
                break;
        }
    }

    private File getFileFromUser() {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files","json");
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filter);
        int returnVal = fc.showOpenDialog(this);

        if(returnVal == JFileChooser.APPROVE_OPTION)
            return fc.getSelectedFile();
        else
            return null;
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
