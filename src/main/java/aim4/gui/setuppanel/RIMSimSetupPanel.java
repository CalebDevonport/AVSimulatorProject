package aim4.gui.setuppanel;

import aim4.gui.parampanel.rim.AIMCrossIntersectionParamPanel;
import aim4.gui.parampanel.rim.AutoDriverOnlyParamPanel;
import aim4.gui.parampanel.rim.NoProtocolParamPanel;
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

    final static String AUTO_DRIVER_ONLY_SETUP_PANEL = "RIM Protocol";
    final static String NO_PROTOCOL_SETUP_PANEL = "No Protocol";
    final static String STOP_SIGNS_SETUP_PANEL = "Stop Signs";
    final static String AIM_CROSS_INTERSECTION_SETUP_PANEL = "AIM Protocol-Cross Intersection";

    /** The combo box */
    private JComboBox comboBox;
    /** The card panel */
    private JPanel cards; //a panel that uses CardLayout
    /** The card layout */
    private CardLayout cardLayout;
    /** the auto driver only simulation setup panel */
    private AutoDriverOnlyParamPanel autoDriverOnlySetupPanel;
    /** The traffic signal setup panel */
    private NoProtocolParamPanel noProtocolSetupPanel;
    /** the stop sign simulation setup panel */
    private AutoDriverOnlyParamPanel stopSignSetupPanel;
    /** the aim cross intersection simulation setup panel */
    private AIMCrossIntersectionParamPanel aimCrossIntersectionSetupPanel;
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
                { AUTO_DRIVER_ONLY_SETUP_PANEL,
                        STOP_SIGNS_SETUP_PANEL,
                        NO_PROTOCOL_SETUP_PANEL,
                        AIM_CROSS_INTERSECTION_SETUP_PANEL};
        comboBox = new JComboBox(comboBoxItems);
        comboBox.setEditable(false);
        comboBox.addItemListener(this);
        comboBoxPane.add(comboBox);

        // create the cards pane
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // add the parameter panels
        autoDriverOnlySetupPanel = new AutoDriverOnlyParamPanel(rimSimSetup);
        addParamPanel(autoDriverOnlySetupPanel, AUTO_DRIVER_ONLY_SETUP_PANEL);
        stopSignSetupPanel = new AutoDriverOnlyParamPanel(rimSimSetup);
        addParamPanel(stopSignSetupPanel, STOP_SIGNS_SETUP_PANEL);
        noProtocolSetupPanel = new NoProtocolParamPanel(rimSimSetup);
        addParamPanel(noProtocolSetupPanel, NO_PROTOCOL_SETUP_PANEL);
        aimCrossIntersectionSetupPanel = new AIMCrossIntersectionParamPanel(aimSimSetup);
        addParamPanel(aimCrossIntersectionSetupPanel, AIM_CROSS_INTERSECTION_SETUP_PANEL);

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
            simSetup.setTrafficLevel(autoDriverOnlySetupPanel.getTrafficRate());
            simSetup.setRoundaboutDiameter(autoDriverOnlySetupPanel.getRoundaboutDiameter());
            simSetup.setLaneSpeedLimit(autoDriverOnlySetupPanel.getLaneSpeedLimit());
            simSetup.setRoundaboutSpeedLimit(autoDriverOnlySetupPanel.getRoundaboutSpeedLimit());
            simSetup.setStopDistBeforeIntersection(
                    autoDriverOnlySetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(autoDriverOnlySetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(autoDriverOnlySetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(autoDriverOnlySetupPanel.getLanesPerRoad());
            if(autoDriverOnlySetupPanel.uploadTrafficSchedule != null) {

                    autoDriverOnlySetupPanel.uploadTrafficSchedule = new File(autoDriverOnlySetupPanel.uploadTrafficScheduleTextbox.getText());
                    simSetup.setUploadTrafficSchedule(autoDriverOnlySetupPanel.uploadTrafficSchedule);


            } else {
                autoDriverOnlySetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(autoDriverOnlySetupPanel.uploadTrafficSchedule);
            }
            return simSetup;
        } else if (comboBox.getSelectedIndex() == 1) {
            AutoDriverOnlySimSetup simSetup = new AutoDriverOnlySimSetup(this.rimSimSetup);
            simSetup.setTrafficLevel(stopSignSetupPanel.getTrafficRate());
            simSetup.setRoundaboutDiameter(stopSignSetupPanel.getRoundaboutDiameter());
            simSetup.setLaneSpeedLimit(stopSignSetupPanel.getLaneSpeedLimit());
            simSetup.setRoundaboutSpeedLimit(stopSignSetupPanel.getRoundaboutSpeedLimit());
            simSetup.setStopDistBeforeIntersection(
                    stopSignSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(stopSignSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(stopSignSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(stopSignSetupPanel.getLanesPerRoad());
            simSetup.setIsStopSignMode(true);
            if(stopSignSetupPanel.uploadTrafficSchedule != null) {

                stopSignSetupPanel.uploadTrafficSchedule = new File(stopSignSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(stopSignSetupPanel.uploadTrafficSchedule);


            } else {
                stopSignSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(stopSignSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;
        } else if (comboBox.getSelectedIndex() == 2) {
            NoProtocolSimSetup simSetup = new NoProtocolSimSetup(this.rimSimSetup);
            simSetup.setTrafficLevel(noProtocolSetupPanel.getTrafficRate());
            simSetup.setRoundaboutDiameter(noProtocolSetupPanel.getRoundaboutDiameter());
            simSetup.setLaneSpeedLimit(noProtocolSetupPanel.getLaneSpeedLimit());
            simSetup.setRoundaboutSpeedLimit(noProtocolSetupPanel.getRoundaboutSpeedLimit());
            simSetup.setStopDistBeforeIntersection(
                    noProtocolSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(noProtocolSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(noProtocolSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(noProtocolSetupPanel.getLanesPerRoad());
            if(noProtocolSetupPanel.uploadTrafficSchedule != null) {

                noProtocolSetupPanel.uploadTrafficSchedule = new File(noProtocolSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(noProtocolSetupPanel.uploadTrafficSchedule);


            } else {
                noProtocolSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(noProtocolSetupPanel.uploadTrafficSchedule);
            }
            return simSetup;
        }else if(comboBox.getSelectedIndex() == 3){
            AIMCrossIntersectionSimSetup simSetup = new AIMCrossIntersectionSimSetup(this.aimSimSetup);
            simSetup.setTrafficLevel(aimCrossIntersectionSetupPanel.getTrafficRate());
            simSetup.setSpeedLimit(aimCrossIntersectionSetupPanel.getSpeedLimit());
            simSetup.setStopDistBeforeIntersection(
                    aimCrossIntersectionSetupPanel.getStopDistToIntersection());
            simSetup.setNumOfColumns(aimCrossIntersectionSetupPanel.getNumOfColumns());
            simSetup.setNumOfRows(aimCrossIntersectionSetupPanel.getNumOfRows());
            simSetup.setLanesPerRoad(aimCrossIntersectionSetupPanel.getLanesPerRoad());
            if(aimCrossIntersectionSetupPanel.uploadTrafficSchedule != null) {

                aimCrossIntersectionSetupPanel.uploadTrafficSchedule = new File(aimCrossIntersectionSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup.setUploadTrafficSchedule(aimCrossIntersectionSetupPanel.uploadTrafficSchedule);


            } else {
                aimCrossIntersectionSetupPanel.uploadTrafficSchedule = null;
                simSetup.setUploadTrafficSchedule(aimCrossIntersectionSetupPanel.uploadTrafficSchedule);
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
}
