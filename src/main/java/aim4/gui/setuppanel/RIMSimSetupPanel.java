package aim4.gui.setuppanel;

import aim4.gui.parampanel.rim.AutoDriverOnlyParamPanel;
import aim4.sim.setup.rim.AutoDriverOnlySimSetup;
import aim4.sim.setup.rim.BasicSimSetup;
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

    /** The combo box */
    private JComboBox comboBox;
    /** The card panel */
    private JPanel cards; //a panel that uses CardLayout
    /** The card layout */
    private CardLayout cardLayout;
    /** the auto driver only simulation setup panel */
    private AutoDriverOnlyParamPanel autoDriverOnlySetupPanel;
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
                { AUTO_DRIVER_ONLY_SETUP_PANEL };
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
