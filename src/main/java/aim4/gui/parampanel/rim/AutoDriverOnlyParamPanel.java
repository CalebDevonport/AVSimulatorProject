package aim4.gui.parampanel.rim;

import aim4.gui.component.LabeledSlider;
import aim4.map.rim.RimMapUtil;
import aim4.sim.setup.rim.BasicSimSetup;
import org.json.simple.JSONArray;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * The autonomous driver only simulation parameter panel.
 */
public class AutoDriverOnlyParamPanel extends JPanel implements ActionListener {
    private static final long serialVersionUID = 1L;

    LabeledSlider trafficRateSlider;
    LabeledSlider scheduleTimeLimitSlider;
    LabeledSlider roundaboutDiameterSlider;
    LabeledSlider laneSpeedLimitSlider;
    LabeledSlider roundaboutSpeedLimitSlider;
    LabeledSlider stopDistToIntersectionSlider;
    LabeledSlider numOfColumnSlider;
    LabeledSlider numOfRowSlider;
    LabeledSlider lanesPerRoadSlider;
    /**Dictates the number of schedules to create*/
    private LabeledSlider numberOfSchedulesSlider;

    /**The JPanel containing all of the option sliders**/
    private JPanel optionPane;
    public JTextArea uploadTrafficScheduleTextbox;

    //JSON FILES
    //todo: set these to private
    public File uploadTrafficSchedule;

    //ENUM FOR BUTTON ACTIONS//
    private enum ButtonActionCommands {
        UPLOAD_CLEAR,
        UPLOAD_SELECT,
        CREATE_UNIFORM_RANDOM_SCHEDULE,
        CREATE_UNIFORM_RATIO_SCHEDULE
    }


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
                new LabeledSlider(1.0, 1,
                        simSetup.getLanesPerRoad(),
                        1.0, 1.0,
                        "Number of Lanes per Road: %.0f",
                        "%.0f");
        add(lanesPerRoadSlider);

        scheduleTimeLimitSlider =
                new LabeledSlider(0.0, 5000.0,
                        1800.0,
                        500.0, 100.0,
                        "Schedule time limit: %.0fs",
                        "%.0fs");
        add(scheduleTimeLimitSlider);

        numberOfSchedulesSlider =
                new LabeledSlider(0, 100,
                        1,
                        10, 1,
                        "Number of schedules to save: %.0f schedules",
                        "%.0f");
        add(numberOfSchedulesSlider);

        //Create button for uniform schedule generation
        JButton createUniformScheduleButton = new JButton("Create uniform random schedule");
        createUniformScheduleButton.addActionListener(this);
        createUniformScheduleButton.setActionCommand(ButtonActionCommands.CREATE_UNIFORM_RANDOM_SCHEDULE.toString());
        JLabel uniformScheduleLabel = new JLabel("To create an uniform spawn schedule please use the slider to select the traffic level");

        //Create button for ratio schedule generation
        JButton createUniformRatioScheduleButton = new JButton("Create uniform ratio schedule");
        createUniformRatioScheduleButton.addActionListener(this);
        createUniformRatioScheduleButton.setActionCommand(ButtonActionCommands.CREATE_UNIFORM_RATIO_SCHEDULE.toString());
        JLabel ratioScheduleLabel = new JLabel("To create a ratio spawn schedule please modify the traffic volumes csv in the rosources package");


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
        optionPane.add(createUniformScheduleButton);
        optionPane.add(uniformScheduleLabel);
        optionPane.add(createUniformRatioScheduleButton);
        optionPane.add(ratioScheduleLabel);
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
            case CREATE_UNIFORM_RANDOM_SCHEDULE:
                double trafficLevel = trafficRateSlider.getValue() / 3600;
                double timeLimit = scheduleTimeLimitSlider.getValue();
                double laneSpeedLimit = laneSpeedLimitSlider.getValue();
                double roundaboutDiameter = roundaboutDiameterSlider.getValue();
                double roundaboutSpeedLimit = roundaboutSpeedLimitSlider.getValue();
                List<JSONArray> schedules = new ArrayList<JSONArray>();
                JSONArray schedule = RimMapUtil.createUniformSpawnSchedule(
                        trafficLevel,
                        timeLimit,
                        1,
                        1,
                        roundaboutDiameter,
                        20,
                        4,
                        3.014,
                        laneSpeedLimit,
                        roundaboutSpeedLimit,
                        1,
                        0,
                        0);
                schedules.add(schedule);
                try {
                    saveJSON(schedules);
                } catch (IOException ex) {
                    String stackTraceMessage = "";
                    for(StackTraceElement line : ex.getStackTrace())
                        stackTraceMessage += line.toString() + "\n";
                    String errorMessage = String.format(
                            "Error Occured whilst saving: %s\nStack Trace:\n%s",
                            ex.getLocalizedMessage(),
                            stackTraceMessage);
                    JOptionPane.showMessageDialog(this,errorMessage,"Saving error",JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);
                }
                break;

            case CREATE_UNIFORM_RATIO_SCHEDULE:
                try {
                    String trafficLevelVolumeName = "traffic_volumes.csv";
                    timeLimit = scheduleTimeLimitSlider.getValue();
                    laneSpeedLimit = laneSpeedLimitSlider.getValue();
                    roundaboutDiameter = roundaboutDiameterSlider.getValue();
                    roundaboutSpeedLimit = roundaboutSpeedLimitSlider.getValue();
                    schedules = new ArrayList<JSONArray>();
                    schedule = RimMapUtil.createRatioSpawnSchedule(
                            trafficLevelVolumeName,
                            timeLimit,
                            1,
                            1,
                            roundaboutDiameter,
                            20,
                            4,
                            3.014,
                            laneSpeedLimit,
                            roundaboutSpeedLimit,
                            1,
                            0,
                            0);
                    schedules.add(schedule);
                try {
                    saveJSON(schedules);
                } catch (IOException ex) {
                    String stackTraceMessage = "";
                    for(StackTraceElement line : ex.getStackTrace())
                        stackTraceMessage += line.toString() + "\n";
                    String errorMessage = String.format(
                            "Error Occured whilst saving: %s\nStack Trace:\n%s",
                            ex.getLocalizedMessage(),
                            stackTraceMessage);
                    JOptionPane.showMessageDialog(this,errorMessage,"Saving error",JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(ex);
                }
                } catch (NullPointerException e1) {
                    String stackTraceMessage = "";
                    for(StackTraceElement line : e1.getStackTrace())
                        stackTraceMessage += line.toString() + "\n";
                    String errorMessage = String.format(
                            "Error Occured whilst opening csv file: %s\nStack Trace:\n%s",
                            e1.getLocalizedMessage(),
                            stackTraceMessage);
                    JOptionPane.showMessageDialog(this,errorMessage,"Opening csv file error",JOptionPane.ERROR_MESSAGE);
                    throw new RuntimeException(e1);

                }
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

    private void saveJSON(List<JSONArray> schedules) throws IOException {
        FileNameExtensionFilter filter = new FileNameExtensionFilter("JSON Files","json");
        final JFileChooser fc = new JFileChooser();
        fc.setFileFilter(filter);
        int returnVal = fc.showSaveDialog(this);
        if(returnVal == JFileChooser.APPROVE_OPTION) {
            String pathExtension = fc.getSelectedFile().getAbsolutePath();
            for(int i = 0; i < schedules.size(); i++) {
                String jsonString = schedules.get(i).toJSONString();
                List<String> writeList = new ArrayList<String>();
                writeList.add(jsonString);
                String path = pathExtension;
                if(pathExtension.endsWith(".json")) {
                    int lastIndex = path.lastIndexOf(".json");
                    if(lastIndex > -1)
                        path = path.substring(0, lastIndex) + "_" + Integer.toString(i+1) + ".json";
                } else {
                    path = path.substring(0, path.length()) + "_" + Integer.toString(i+1) + ".json";
                }
                Files.write(Paths.get(path), writeList, Charset.forName("UTF-8"));
            }
        }
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
