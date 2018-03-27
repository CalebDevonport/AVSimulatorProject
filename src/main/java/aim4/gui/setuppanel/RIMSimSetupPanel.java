package aim4.gui.setuppanel;

import aim4.gui.parampanel.rim.AutoDriverOnlyParamPanel;
import aim4.gui.parampanel.rim.NoProtocolParamPanel;
import aim4.sim.setup.rim.AutoDriverOnlySimSetup;
import aim4.sim.setup.rim.BasicSimSetup;
import aim4.sim.setup.rim.NoProtocolSimSetup;
import aim4.sim.setup.rim.RIMSimSetup;

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
    /** The simulation setup panel */
    private BasicSimSetup simSetup;

    /**
     * Create a simulation setup panel
     *
     * @param initSimSetup  the initial simulation setup
     */
    public RIMSimSetupPanel(BasicSimSetup initSimSetup) {
        this.simSetup = initSimSetup;

        // create the combo box pane
        JPanel comboBoxPane = new JPanel(); //use FlowLayout
        comboBoxPane.setBackground(Color.WHITE);

        String comboBoxItems[] =
                { AUTO_DRIVER_ONLY_SETUP_PANEL,
                        STOP_SIGNS_SETUP_PANEL,
                        NO_PROTOCOL_SETUP_PANEL};
        comboBox = new JComboBox(comboBoxItems);
        comboBox.setEditable(false);
        comboBox.addItemListener(this);
        comboBoxPane.add(comboBox);

        // create the cards pane
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        // add the parameter panels
        autoDriverOnlySetupPanel = new AutoDriverOnlyParamPanel(simSetup);
        addParamPanel(autoDriverOnlySetupPanel, AUTO_DRIVER_ONLY_SETUP_PANEL);
        stopSignSetupPanel = new AutoDriverOnlyParamPanel(simSetup);
        addParamPanel(stopSignSetupPanel, STOP_SIGNS_SETUP_PANEL);
        noProtocolSetupPanel = new NoProtocolParamPanel(simSetup);
        addParamPanel(noProtocolSetupPanel, NO_PROTOCOL_SETUP_PANEL);

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
            AutoDriverOnlySimSetup simSetup2 = new AutoDriverOnlySimSetup(simSetup);
            simSetup2.setTrafficLevel(autoDriverOnlySetupPanel.getTrafficRate());
            simSetup2.setRoundaboutDiameter(autoDriverOnlySetupPanel.getRoundaboutDiameter());
            simSetup2.setLaneSpeedLimit(autoDriverOnlySetupPanel.getLaneSpeedLimit());
            simSetup2.setRoundaboutSpeedLimit(autoDriverOnlySetupPanel.getRoundaboutSpeedLimit());
//            simSetup2.setStopDistBeforeIntersection(
//                    autoDriverOnlySetupPanel.getStopDistToIntersection());
            simSetup2.setNumOfColumns(autoDriverOnlySetupPanel.getNumOfColumns());
            simSetup2.setNumOfRows(autoDriverOnlySetupPanel.getNumOfRows());
            simSetup2.setLanesPerRoad(autoDriverOnlySetupPanel.getLanesPerRoad());
            if(autoDriverOnlySetupPanel.uploadTrafficSchedule != null) {

                autoDriverOnlySetupPanel.uploadTrafficSchedule = new File(autoDriverOnlySetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup2.setUploadTrafficSchedule(autoDriverOnlySetupPanel.uploadTrafficSchedule);


            } else {
                autoDriverOnlySetupPanel.uploadTrafficSchedule = null;
                simSetup2.setUploadTrafficSchedule(autoDriverOnlySetupPanel.uploadTrafficSchedule);
            }
            return simSetup2;
        } else if (comboBox.getSelectedIndex() == 1) {
            AutoDriverOnlySimSetup simSetup2 = new AutoDriverOnlySimSetup(simSetup);
            simSetup2.setTrafficLevel(autoDriverOnlySetupPanel.getTrafficRate());
            simSetup2.setRoundaboutDiameter(autoDriverOnlySetupPanel.getRoundaboutDiameter());
            simSetup2.setLaneSpeedLimit(autoDriverOnlySetupPanel.getLaneSpeedLimit());
            simSetup2.setRoundaboutSpeedLimit(autoDriverOnlySetupPanel.getRoundaboutSpeedLimit());
            simSetup2.setStopDistBeforeIntersection(
                    autoDriverOnlySetupPanel.getStopDistToIntersection());
            simSetup2.setNumOfColumns(autoDriverOnlySetupPanel.getNumOfColumns());
            simSetup2.setNumOfRows(autoDriverOnlySetupPanel.getNumOfRows());
            simSetup2.setLanesPerRoad(autoDriverOnlySetupPanel.getLanesPerRoad());
            simSetup2.setIsStopSignMode(true);
            if(autoDriverOnlySetupPanel.uploadTrafficSchedule != null) {

                autoDriverOnlySetupPanel.uploadTrafficSchedule = new File(autoDriverOnlySetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup2.setUploadTrafficSchedule(autoDriverOnlySetupPanel.uploadTrafficSchedule);


            } else {
                autoDriverOnlySetupPanel.uploadTrafficSchedule = null;
                simSetup2.setUploadTrafficSchedule(autoDriverOnlySetupPanel.uploadTrafficSchedule);
            }
            return simSetup2;
        } else if (comboBox.getSelectedIndex() == 2) {
            NoProtocolSimSetup simSetup2 = new NoProtocolSimSetup(simSetup);
            simSetup2.setTrafficLevel(noProtocolSetupPanel.getTrafficRate());
            simSetup2.setRoundaboutDiameter(noProtocolSetupPanel.getRoundaboutDiameter());
            simSetup2.setLaneSpeedLimit(noProtocolSetupPanel.getLaneSpeedLimit());
            simSetup2.setRoundaboutSpeedLimit(noProtocolSetupPanel.getRoundaboutSpeedLimit());
            simSetup2.setStopDistBeforeIntersection(
                    noProtocolSetupPanel.getStopDistToIntersection());
            simSetup2.setNumOfColumns(noProtocolSetupPanel.getNumOfColumns());
            simSetup2.setNumOfRows(noProtocolSetupPanel.getNumOfRows());
            simSetup2.setLanesPerRoad(noProtocolSetupPanel.getLanesPerRoad());
            if(noProtocolSetupPanel.uploadTrafficSchedule != null) {

                noProtocolSetupPanel.uploadTrafficSchedule = new File(noProtocolSetupPanel.uploadTrafficScheduleTextbox.getText());
                simSetup2.setUploadTrafficSchedule(noProtocolSetupPanel.uploadTrafficSchedule);


            } else {
                noProtocolSetupPanel.uploadTrafficSchedule = null;
                simSetup2.setUploadTrafficSchedule(noProtocolSetupPanel.uploadTrafficSchedule);
            }
            return simSetup2;
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
