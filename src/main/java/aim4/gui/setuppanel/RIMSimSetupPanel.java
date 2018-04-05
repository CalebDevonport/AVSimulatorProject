package aim4.gui.setuppanel;

import aim4.gui.parampanel.rim.AIMCrossOptimalParamPanel;
import aim4.gui.parampanel.rim.AIMCrossParamPanel;
import aim4.gui.parampanel.rim.AutoDriverOnlyParamPanel;
import aim4.gui.parampanel.rim.RIMOptimalProtocolParamPanel;
import aim4.sim.setup.rim.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

/**
 * The simulation setup panel.
 */
public class RIMSimSetupPanel extends SimSetupPanel implements ItemListener {
    private static final long serialVersionUID = 1L;

    final static String RIM_SETUP_PANEL = "RIM Protocol";
    final static String RIM_OPTIMAL_SETUP_PANEL = "RIM Optimal Protocol";
    final static String RIM_STOP_SIGNS_SETUP_PANEL = "RIM Stop Signs Protocol";
    final static String AIM_CROSS_INTERSECTION_SETUP_PANEL = "AIM Cross Protocol";
    final static String AIM_CROSS_INTERSECTION_OPTIMAL_SETUP_PANEL = "AIM Optimal Cross Protocol";
    final static String AIM_CROSS_INTERSECTION_STOP_SIGNS_SETUP_PANEL = "AIM Stop Signs Protocol";

    /** The combo box */
    private JComboBox comboBox;
    /** The card panel */
    private JPanel cards; //a panel that uses CardLayout
    /** The card layout */
    private CardLayout cardLayout;
    /** the rim simulation setup panel */
    private AutoDriverOnlyParamPanel rimSetupPanel;
    /** The rim optimal simulation setup panel */
    private RIMOptimalProtocolParamPanel rimOptimalProtocolSetupPanel;
    /** the rim stop sign simulation setup panel */
    private AutoDriverOnlyParamPanel rimStopSignSetupPanel;
    /** the aim cross intersection simulation setup panel */
    private AIMCrossParamPanel aimCrossSetupPanel;
    /** the aim cross intersection optimal simulation setup panel */
    private AIMCrossOptimalParamPanel aimCrossOptimalSetupPanel;
    /** the rim stop sign simulation setup panel */
    private AIMCrossParamPanel aimCrossStopSignSetupPanel;
    /** The rim simulation setup panel */
    private BasicSimSetup rimSimSetup;
    /** The aim simulation setup panel */
    private aim4.sim.setup.aim.BasicSimSetup aimSimSetup;

    /**
     * Create a simulation setup panel
     *
     * @param rimSimSetup  the rim initial simulation setup
     * @param aimSimSetup  the aim initial simulation setup
     */
    public RIMSimSetupPanel(BasicSimSetup rimSimSetup, aim4.sim.setup.aim.BasicSimSetup aimSimSetup) {
        this.rimSimSetup = rimSimSetup;
        this.aimSimSetup = aimSimSetup;

        // create the combo box pane
        JPanel comboBoxPane = new JPanel(); //use FlowLayout
        comboBoxPane.setBackground(Color.WHITE);

        String comboBoxItems[] =
                { RIM_SETUP_PANEL,
                        RIM_STOP_SIGNS_SETUP_PANEL,
                        RIM_OPTIMAL_SETUP_PANEL,
                        AIM_CROSS_INTERSECTION_SETUP_PANEL,
                        AIM_CROSS_INTERSECTION_OPTIMAL_SETUP_PANEL,
                        AIM_CROSS_INTERSECTION_STOP_SIGNS_SETUP_PANEL};
        comboBox = new JComboBox(comboBoxItems);
        comboBox.setEditable(false);
        comboBox.addItemListener(this);
        comboBoxPane.add(comboBox);

        // create the cards pane
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // add the parameter panels
        rimSetupPanel = new AutoDriverOnlyParamPanel(rimSimSetup);
        addParamPanel(rimSetupPanel, RIM_SETUP_PANEL);
        rimStopSignSetupPanel = new AutoDriverOnlyParamPanel(rimSimSetup);
        addParamPanel(rimStopSignSetupPanel, RIM_STOP_SIGNS_SETUP_PANEL);
        rimOptimalProtocolSetupPanel = new RIMOptimalProtocolParamPanel(rimSimSetup);
        addParamPanel(rimOptimalProtocolSetupPanel, RIM_OPTIMAL_SETUP_PANEL);
        aimCrossSetupPanel = new AIMCrossParamPanel(aimSimSetup);
        addParamPanel(aimCrossSetupPanel, AIM_CROSS_INTERSECTION_SETUP_PANEL);
        aimCrossOptimalSetupPanel = new AIMCrossOptimalParamPanel(aimSimSetup);
        addParamPanel(aimCrossOptimalSetupPanel, AIM_CROSS_INTERSECTION_OPTIMAL_SETUP_PANEL);
        aimCrossStopSignSetupPanel = new AIMCrossParamPanel(aimSimSetup);
        addParamPanel(aimCrossStopSignSetupPanel, AIM_CROSS_INTERSECTION_STOP_SIGNS_SETUP_PANEL);

        // add the combo box pane and cards pane
        setLayout(new BorderLayout());
        add(comboBoxPane, BorderLayout.PAGE_START);
        add(cards, BorderLayout.CENTER);
    }

    /**
     * Add a parameter panel.
     *
     * @param paramPanel  the parameter panel
     * @param paramLabel  the label of the parameter panel
     */
    private void addParamPanel(JPanel paramPanel, String paramLabel) {
        JScrollPane scrollPane = new JScrollPane(paramPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cards.add(scrollPane, paramLabel);
    }

    /**
     * Create and return a simulation setup object.
     *
     * @return the simulation setup object
     */
    public RIMSimSetup getSimSetup() {
        if (comboBox.getSelectedIndex() == 0) {
            AutoDriverOnlySimSetup simSetup = new AutoDriverOnlySimSetup(this.rimSimSetup);
            simSetup.setTrafficLevel(rimSetupPanel.getTrafficRate());
            simSetup.setRoundaboutDiameter(rimSetupPanel.getRoundaboutDiameter());
            simSetup.setLaneSpeedLimit(rimSetupPanel.getLaneSpeedLimit());
            simSetup.setRoundaboutSpeedLimit(rimSetupPanel.getRoundaboutSpeedLimit());
//            simSetup.setStopDistBeforeIntersection(
//                    rimSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(rimSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(rimSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(rimSetupPanel.getLanesPerRoad());
            if(rimSetupPanel.uploadTrafficSchedule != null) {

                rimSetupPanel.uploadTrafficSchedule = new File(rimSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(rimSetupPanel.uploadTrafficSchedule);


            } else {
                rimSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(rimSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;
        } else if (comboBox.getSelectedIndex() == 1) {
            AutoDriverOnlySimSetup simSetup = new AutoDriverOnlySimSetup(this.rimSimSetup);
            simSetup.setTrafficLevel(rimStopSignSetupPanel.getTrafficRate());
            simSetup.setRoundaboutDiameter(rimStopSignSetupPanel.getRoundaboutDiameter());
            simSetup.setLaneSpeedLimit(rimStopSignSetupPanel.getLaneSpeedLimit());
            simSetup.setRoundaboutSpeedLimit(rimStopSignSetupPanel.getRoundaboutSpeedLimit());
//            simSetup.setStopDistBeforeIntersection(
//                    rimStopSignSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(rimStopSignSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(rimStopSignSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(rimStopSignSetupPanel.getLanesPerRoad());
            simSetup.setIsStopSignMode(true);
            if(rimStopSignSetupPanel.uploadTrafficSchedule != null) {

                rimStopSignSetupPanel.uploadTrafficSchedule = new File(rimStopSignSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(rimStopSignSetupPanel.uploadTrafficSchedule);


            } else {
                rimStopSignSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(rimStopSignSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;
        } else if (comboBox.getSelectedIndex() == 2) {
            RIMOptimalSimSetup simSetup = new RIMOptimalSimSetup(this.rimSimSetup);
            simSetup.setTrafficLevel(rimOptimalProtocolSetupPanel.getTrafficRate());
            simSetup.setRoundaboutDiameter(rimOptimalProtocolSetupPanel.getRoundaboutDiameter());
            simSetup.setLaneSpeedLimit(rimOptimalProtocolSetupPanel.getLaneSpeedLimit());
            simSetup.setRoundaboutSpeedLimit(rimOptimalProtocolSetupPanel.getRoundaboutSpeedLimit());
//            simSetup.setStopDistBeforeIntersection(
//                    rimOptimalProtocolSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(rimOptimalProtocolSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(rimOptimalProtocolSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(rimOptimalProtocolSetupPanel.getLanesPerRoad());
            if(rimOptimalProtocolSetupPanel.uploadTrafficSchedule != null) {

                rimOptimalProtocolSetupPanel.uploadTrafficSchedule = new File(rimOptimalProtocolSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(rimOptimalProtocolSetupPanel.uploadTrafficSchedule);


            } else {
                rimOptimalProtocolSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(rimOptimalProtocolSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;
        }else if(comboBox.getSelectedIndex() == 3){
            AIMCrossSimSetup simSetup = new AIMCrossSimSetup(this.aimSimSetup);
            simSetup.setTrafficLevel(aimCrossSetupPanel.getTrafficRate());
            simSetup.setSpeedLimit(aimCrossSetupPanel.getSpeedLimit());
            simSetup.setStopDistBeforeIntersection(
                    aimCrossSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(aimCrossSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(aimCrossSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(aimCrossSetupPanel.getLanesPerRoad());
            if(aimCrossSetupPanel.uploadTrafficSchedule != null) {

                aimCrossSetupPanel.uploadTrafficSchedule = new File(aimCrossSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(aimCrossSetupPanel.uploadTrafficSchedule);


            } else {
                aimCrossSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(aimCrossSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;

        } else if(comboBox.getSelectedIndex() == 4){
            AIMCrossOptimalSimSetup simSetup = new AIMCrossOptimalSimSetup(this.aimSimSetup);
            simSetup.setTrafficLevel(aimCrossOptimalSetupPanel.getTrafficRate());
            simSetup.setSpeedLimit(aimCrossOptimalSetupPanel.getSpeedLimit());
            simSetup.setStopDistBeforeIntersection(
                    aimCrossOptimalSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(aimCrossOptimalSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(aimCrossOptimalSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(aimCrossOptimalSetupPanel.getLanesPerRoad());
            if(aimCrossOptimalSetupPanel.uploadTrafficSchedule != null) {

                aimCrossOptimalSetupPanel.uploadTrafficSchedule = new File(aimCrossOptimalSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(aimCrossOptimalSetupPanel.uploadTrafficSchedule);


            } else {
                aimCrossOptimalSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(aimCrossOptimalSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;

        } else if(comboBox.getSelectedIndex() == 5){
            AIMCrossSimSetup simSetup = new AIMCrossSimSetup(this.aimSimSetup);
            simSetup.setTrafficLevel(aimCrossStopSignSetupPanel.getTrafficRate());
            simSetup.setSpeedLimit(aimCrossStopSignSetupPanel.getSpeedLimit());
            simSetup.setNumOfColumns(aimCrossStopSignSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(aimCrossStopSignSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(aimCrossStopSignSetupPanel.getLanesPerRoad());
            simSetup.setStopDistBeforeIntersection(
                    aimCrossStopSignSetupPanel.getStopDistToIntersection());
            simSetup.setIsStopSignMode(true);
            if(aimCrossStopSignSetupPanel.uploadTrafficSchedule != null) {

                aimCrossStopSignSetupPanel.uploadTrafficSchedule = new File(aimCrossStopSignSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(aimCrossStopSignSetupPanel.uploadTrafficSchedule);


            } else {
                aimCrossStopSignSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(aimCrossStopSignSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;

        } else {
            throw new RuntimeException(
                    "SimSetupPane::getSimSetup(): not implemented yet");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void itemStateChanged(ItemEvent evt) {
        cardLayout.show(cards, (String)evt.getItem());
    }

    public BasicSimSetup getRimSimSetup(){
        return this.rimSimSetup;
    }

    public aim4.sim.setup.aim.BasicSimSetup getAimSimSetup(){
        return this.aimSimSetup;
    }
}
