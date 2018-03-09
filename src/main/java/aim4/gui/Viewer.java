/*
Copyright (c) 2011 Tsz-Chiu Au, Peter Stone
University of Texas at Austin
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

1. Redistributions of source code must retain the above copyright notice, this
list of conditions and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright notice,
this list of conditions and the following disclaimer in the documentation
and/or other materials provided with the distribution.

3. Neither the name of the University of Texas at Austin nor the names of its
contributors may be used to endorse or promote products derived from this
software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package aim4.gui;

import aim4.config.Debug;
import aim4.gui.viewer.*;
import aim4.sim.Simulator;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * The viewer is a Graphical User Interface (GUI) that allows a user to run the
 * AIM Simulator while watching the vehicles in real time.
 */
public class Viewer extends JFrame implements ActionListener, ItemListener, KeyListener {

    // ///////////////////////////////
    // CONSTANTS
    // ///////////////////////////////
    /** The serial version ID for serialization */
    private static final long serialVersionUID = 1L;
    /**
     * Whether or not the current simulation time is shown on screen.
     */
    public static final boolean IS_SHOW_SIMULATION_TIME = true;
    /**
     * Whether or not the simulator shows the vin of the vehicles on screen.
     */
    public static final boolean IS_SHOW_VIN_BY_DEFAULT = false;
    /**
     * Whether or not the IM Shapes are shown by default.
     */
    public static final boolean IS_SHOW_IM_DEBUG_SHAPES_BY_DEFAULT = false;
    /**
     * The String to display in the title bar of the main app.
     */
    private static final String TITLEBAR_STRING = "AV Simulator";
    /**
     * The width of the start/pause/resume button and the step buttons. {@value}
     * pixels.
     */
    private static final int DEFAULT_BUTTON_WIDTH = 100; // px
    /**
     * The height of the status pane. {@value} pixels.
     */
    private static final int DEFAULT_STATUS_PANE_HEIGHT = 200; // px
    /**
     * Preferred maximum width for the canvas, in pixels. {@value} pixels.
     */
    private static final int PREF_MAX_CANVAS_WIDTH = 650;
    /**
     * Preferred maximum height for the canvas, in pixels. {@value} pixels.
     */
    private static final int PREF_MAX_CANVAS_HEIGHT = 650;

    // ///////////////////////////////
    // GUI ELEMENTS
    // ///////////////////////////////
    /** Tabbed pane containing all of the simulation viewers */
    private JTabbedPane tabbedPane;
    /** The current viewer selected by the tabbedPane */
    private SimViewer selectedViewer;
    /** Panel containing the AIMSimViewer*/
    private AIMSimViewer aimViewer;
    /** Panel containing the RIMSimViewer*/
    private RIMSimViewer rimViewer;
    /** Panel containing the MergeSimViewer*/
    private MergeSimViewer mergeViewer;
    /** Panel containing the CPMSimViewer*/
    private CPMSimViewer cpmViewer;
    /** The status pane on which to display statistics. */
    private StatusPanelContainer statusPanel;
    /** The Start/Pause/Resume Button */
    private JButton startButton;
    /** The Step Button */
    private JButton stepButton;
    // Menu Items
    /** Menu item "Autonomous Vehicles Only" */
    // private JCheckBoxMenuItem autoOnlySimTypeMenuItem;
    /** Menu item "Human Drivers Only" */
    // private JCheckBoxMenuItem humanOnlySimTypeMenuItem;
    /** Menu item "Human Drivers Only" */
    // private JCheckBoxMenuItem mixedSimTypeMenuItem;
    /** Menu item "Start Simulation Process" */
    private JMenuItem startMenuItem;
    /** Menu item "Step" */
    private JMenuItem stepMenuItem;
    /** Menu item "Reset" */
    private JMenuItem resetMenuItem;
    /** Menu item "Dump Data Collection Lines' Data" */
    private JMenuItem dumpDataMenuItem;
    /** Menu item "Dump Statistics Screen Data" */
    private JMenuItem dumpStatScreenMenuItem;
    /** Saves the results data using the Sim's produceResultsCSV method */
    private JMenuItem saveResultsMenuItem;
    /** Menu item for activating recording. */
    private JMenuItem startRecordingMenuItem;
    /** Menu item for deactivating recording. */
    private JMenuItem stopRecordingMenuItem;
    /** Menu item for starting the UDP listener */
    private JMenuItem startUdpListenerMenuItem;
    /** Menu item for stopping the UDP listener */
    private JMenuItem stopUdpListenerMenuItem;
    /** Menu item for controlling whether to show the simulation time */
    private JCheckBoxMenuItem showSimulationTimeMenuItem;
    /** Menu item for controlling whether to show VIN numbers */
    private JCheckBoxMenuItem showVinMenuItem;
    /** Menu item for controlling whether to show debug shapes */
    private JCheckBoxMenuItem showIMShapesMenuItem;
    /** Menu item for clearing simulator's debug point */
    private JMenuItem clearDebugPointsMenuItem;

    // ///////////////////////////////
    // CLASS CONSTRUCTORS
    // ///////////////////////////////
    /**
     * Create a new viewer object.
     */
    public Viewer() {
        this(false);
    }

    /**
     * Create a new viewer object.
     *
     * @param isRunNow      whether or not the simulation is run immediately
     */
    public Viewer(final boolean isRunNow) {
        super(TITLEBAR_STRING);
        // for debugging
        Debug.viewer = this.selectedViewer;

        // Lastly, schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable() {

            /**
             * {@inheritDoc}
             */
            @Override
            public void run() {
                createAndShowGUI(isRunNow);
            }

        });
    }

    // ///////////////////////////////
    // GUI METHODS
    // ///////////////////////////////
    /**
     * Create a new GUI and show it.
     *
     * @param isRunNow      whether or not the simulation is run immediately
     */
    private void createAndShowGUI(boolean isRunNow) {
        // Apple specific property.
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        System.setProperty("com.apple.mrj.application.apple.menu.about.name",
                "AIM Viewer");
        // Make sure that the program quits when we close the window
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Build the GUI
        createMenuBar();
        createStatusPanel();
        createTabbedPane();
        addKeyListener(this);
        setComponentsLayout();
        pack(); // pick the layout and show it
        setVisible(true);
        initGUIsetting();

        if (isRunNow) {
            startSimProcess();
            selectedViewer.requestScreenFocusInWindow();
        }
    }

    /**
     * Create the menu system.
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        JMenu menu;
        JMenuItem menuItem;

        setJMenuBar(menuBar);

        // The File menu exists only on non-Mac OS X environment
        if (!System.getProperty("os.name").equals("Mac OS X")) {
            // File
            menu = new JMenu("File");
            menuBar.add(menu);
            // File->Quit
            menuItem = new JMenuItem("Quit AV Simulator");
            menuItem.addActionListener(this);
            menu.add(menuItem);
        }

        // Simulation
        menu = new JMenu("Simulator");
        menuBar.add(menu);

    /*
    // Simulator->Autonomous Vehicles Only
    autoOnlySimTypeMenuItem =
        new JCheckBoxMenuItem("Autonomous Vehicles Only", true);
    autoOnlySimTypeMenuItem.addActionListener(this);
    menu.add(autoOnlySimTypeMenuItem);
    // Simulator->Human Drivers Only
    humanOnlySimTypeMenuItem =
        new JCheckBoxMenuItem("Human Drivers Only", false);
    humanOnlySimTypeMenuItem.addActionListener(this);
    menu.add(humanOnlySimTypeMenuItem);
    // Simulator->Mixed Drivers
    mixedSimTypeMenuItem = new JCheckBoxMenuItem("Mixed Drivers", false);
    mixedSimTypeMenuItem.addActionListener(this);
    menu.add(mixedSimTypeMenuItem);
    // Simulator->separator
    // menu.addSeparator();
    */

        // Simulator->Start Simulation
        startMenuItem = new JMenuItem("Start");
        startMenuItem.addActionListener(this);
        menu.add(startMenuItem);
        // Simulator->Step
        stepMenuItem = new JMenuItem("Step");
        stepMenuItem.addActionListener(this);
        menu.add(stepMenuItem);
        // Simulator->Reset
        resetMenuItem = new JMenuItem("Reset");
        resetMenuItem.addActionListener(this);
        menu.add(resetMenuItem);

        // Data
        menu = new JMenu("Data");
        menuBar.add(menu);
        // Data->Dump Data Collection Lines' Data
        dumpDataMenuItem = new JMenuItem("Dump Data Collection Lines' Data");
        dumpDataMenuItem.addActionListener(this);
        menu.add(dumpDataMenuItem);
        // Data->Dump Statistics Screen Data
        dumpStatScreenMenuItem = new JMenuItem("Dump Statistics Screen Data");
        dumpStatScreenMenuItem.addActionListener(this);
        menu.add(dumpStatScreenMenuItem);
        // Create CSV Results
        saveResultsMenuItem = new JMenuItem("Save Simulator Results");
        saveResultsMenuItem.addActionListener(this);
        menu.add(saveResultsMenuItem);


        // Recording
        menu = new JMenu("Recording");
        menuBar.add(menu);
        // Recording->Start
        startRecordingMenuItem = new JMenuItem("Start");
        startRecordingMenuItem.addActionListener(this);
        menu.add(startRecordingMenuItem);
        // Recording->Stop
        stopRecordingMenuItem = new JMenuItem("Stop");
        stopRecordingMenuItem.addActionListener(this);
        menu.add(stopRecordingMenuItem);

        // UDP
        menu = new JMenu("UDP");
        menuBar.add(menu);
        // UDP->Start Listening
        startUdpListenerMenuItem = new JMenuItem("Start Listening");
        startUdpListenerMenuItem.addActionListener(this);
        startUdpListenerMenuItem.setEnabled(false);
        menu.add(startUdpListenerMenuItem);
        // UDP->Stop Listening
        stopUdpListenerMenuItem = new JMenuItem("Stop Listening");
        stopUdpListenerMenuItem.addActionListener(this);
        stopUdpListenerMenuItem.setEnabled(false);
        menu.add(stopUdpListenerMenuItem);

        // View
        menu = new JMenu("View");
        menuBar.add(menu);
        // View->Show simulation time
        showSimulationTimeMenuItem = new JCheckBoxMenuItem("Show Simulation Time",
                IS_SHOW_SIMULATION_TIME);
        showSimulationTimeMenuItem.addItemListener(this);
        menu.add(showSimulationTimeMenuItem);
        // View->Show VIN numbers
        showVinMenuItem = new JCheckBoxMenuItem("Show VINs",
                IS_SHOW_VIN_BY_DEFAULT);
        showVinMenuItem.addItemListener(this);
        menu.add(showVinMenuItem);
        // View->Show IM Shapes
        showIMShapesMenuItem = new JCheckBoxMenuItem("Show IM Shapes", false);
        showIMShapesMenuItem.addItemListener(this);
        menu.add(showIMShapesMenuItem);

        // Debug
        menu = new JMenu("Debug");
        menuBar.add(menu);
        // Debug->Clear Debug Points
        clearDebugPointsMenuItem = new JMenuItem("Clear Debug Points");
        clearDebugPointsMenuItem.addActionListener(this);
        menu.add(clearDebugPointsMenuItem);
    }

    /**
     * Create tab panel
     */
    private void createTabbedPane() {
        tabbedPane = new JTabbedPane();
        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                selectedViewer = (SimViewer) ((JTabbedPane) e.getSource()).getSelectedComponent();
                Debug.viewer = selectedViewer;
            }
        });

        aimViewer = new AIMSimViewer(statusPanel, this);
        rimViewer = new RIMSimViewer(statusPanel, this);
        mergeViewer = new MergeSimViewer(statusPanel, this);
        cpmViewer = new CPMSimViewer(statusPanel, this);

        tabbedPane.add("AIM", aimViewer);
        tabbedPane.add("RIM", rimViewer);
        tabbedPane.add("MERGE", mergeViewer);
        tabbedPane.add("CPM", cpmViewer);

        selectedViewer = (SimViewer) tabbedPane.getSelectedComponent();
    }

    /**
     * Create status panel components
     */
    private void createStatusPanel() {
        statusPanel = new StatusPanelContainer(this);
        startButton = new JButton("Start");
        startButton.addActionListener(this);
        stepButton = new JButton("Step");
        stepButton.setEnabled(false);
        stepButton.addActionListener(this);
    }

    /**
     * Set the layout of the viewer
     */
    private void setComponentsLayout() {
        tabbedPane.setPreferredSize(new Dimension(PREF_MAX_CANVAS_WIDTH, PREF_MAX_CANVAS_HEIGHT));

        // set the group layout
        Container pane = getContentPane();
        GroupLayout layout = new GroupLayout(pane);
        pane.setLayout(layout);
        // Turn on automatically adding gaps between components
        layout.setAutoCreateGaps(false);
        // Turn on automatically creating gaps between components that touch
        // the edge of the container and the container.
        layout.setAutoCreateContainerGaps(false);
        // layout for the horizontal axis
        layout.setHorizontalGroup(layout.createParallelGroup(
                GroupLayout.Alignment.LEADING).addComponent(tabbedPane).addGroup(layout.createSequentialGroup().addGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING).addComponent(
                        startButton,
                        DEFAULT_BUTTON_WIDTH,
                        GroupLayout.DEFAULT_SIZE,
                        DEFAULT_BUTTON_WIDTH).addComponent(
                        stepButton, DEFAULT_BUTTON_WIDTH,
                        GroupLayout.DEFAULT_SIZE,
                        DEFAULT_BUTTON_WIDTH)).addComponent(
                statusPanel)));
        // layout for the vertical axis
        layout.setVerticalGroup(
                layout.createSequentialGroup().addComponent(tabbedPane).addGroup(
                        layout.createParallelGroup(GroupLayout.Alignment.CENTER).addGroup(layout.createSequentialGroup().addComponent(
                                startButton).addComponent(stepButton)).addComponent(statusPanel,
                                DEFAULT_STATUS_PANE_HEIGHT,
                                GroupLayout.DEFAULT_SIZE,
                                DEFAULT_STATUS_PANE_HEIGHT)));
    }

    /**
     * Initialize the GUI setting.
     */
    private void initGUIsetting() {
        resetButtonMenuItem();
        startRecordingMenuItem.setEnabled(true);
        stopRecordingMenuItem.setEnabled(false);
        startUdpListenerMenuItem.setEnabled(false);
        stopUdpListenerMenuItem.setEnabled(false);
        showSimulationTimeMenuItem.setSelected(IS_SHOW_SIMULATION_TIME);
        showVinMenuItem.setSelected(IS_SHOW_VIN_BY_DEFAULT);
        showIMShapesMenuItem.setSelected(IS_SHOW_IM_DEBUG_SHAPES_BY_DEFAULT);
    }

    /**
     * Use the simulation start GUI setting.
     */
    private void setSimStartGUIsetting() {
        selectedViewer.showCard(ViewerCardType.SCREEN);
        selectedViewer.startViewer();
        statusPanel.init();

        // update the buttons
        startButton.setText("Pause");
        stepButton.setEnabled(false);
        // update the menu items
    /*
    autoOnlySimTypeMenuItem.setEnabled(false);
    humanOnlySimTypeMenuItem.setEnabled(false);
    mixedSimTypeMenuItem.setEnabled(false);
    */
        startMenuItem.setText("Pause");
        stepMenuItem.setEnabled(false);
        resetMenuItem.setEnabled(true);
        dumpDataMenuItem.setEnabled(true);
        saveResultsMenuItem.setEnabled(true);
        startUdpListenerMenuItem.setEnabled(true);
        clearDebugPointsMenuItem.setEnabled(true);

        if (selectedViewer.hasStatScreen()) {
            dumpStatScreenMenuItem.setEnabled(true);
        } else {
            dumpStatScreenMenuItem.setEnabled(false);
        }
    }

    /**
     * Use the simulation reset GUI setting.
     */
    private void setSimResetGUIsetting() {
        selectedViewer.cleanUp();
        statusPanel.clear();
        resetButtonMenuItem();
    }

    /**
     * Reset the button menu items.
     */
    private void resetButtonMenuItem() {
        selectedViewer.showCard(ViewerCardType.SIM_SETUP_PANEL);
        // update the buttons
        startButton.setText("Start");
        stepButton.setEnabled(false);
        // update the menu items
    /*
    autoOnlySimTypeMenuItem.setSelected(true);
    humanOnlySimTypeMenuItem.setSelected(false);
    mixedSimTypeMenuItem.setSelected(false);
    */
        startMenuItem.setText("Start");
        stepMenuItem.setEnabled(false);
        resetMenuItem.setEnabled(false);
        dumpDataMenuItem.setEnabled(false);
        dumpStatScreenMenuItem.setEnabled(false);
        saveResultsMenuItem.setEnabled(false);
        startUdpListenerMenuItem.setEnabled(false);
        clearDebugPointsMenuItem.setEnabled(false);
    }

    // ///////////////////////////////
    // ACCESSORS
    // ///////////////////////////////
    public Simulator getSelectedSimulator() {
        return selectedViewer.getSimulator();
    }

    public void setTargetSimSpeed(double target) {
        selectedViewer.setTargetSimSpeed(target);
    }

    public void setTargetFrameRate(double target) {
        selectedViewer.setTargetFrameRate(target);
    }

    // ///////////////////////////////
    // SIM METHODS
    // ///////////////////////////////
    /**
     * The handler when the user pressed the start button.
     */
    private void startButtonHandler() {
        if (selectedViewer.isSimThreadNull()) {
            startSimProcess();
            tabbedPane.setEnabled(false);
        } else if (!selectedViewer.isSimThreadPaused()) {
            pauseSimProcess();
        } else {
            resumeSimProcess();
            startMenuItem.setText("Start");
            stepMenuItem.setEnabled(false);
            resetMenuItem.setEnabled(false);
            dumpDataMenuItem.setEnabled(false);
            saveResultsMenuItem.setEnabled(false);
            startUdpListenerMenuItem.setEnabled(false);
            clearDebugPointsMenuItem.setEnabled(false);
        }
    }

    public void stepButtonHandler() {
        selectedViewer.stepSimProcess();
    }

    /**
     * Start the simulation process.
     *
     */
    public void startSimProcess() {
        selectedViewer.createSimulator();
        // initialize the GUI
        setSimStartGUIsetting();
        // start the thread
        selectedViewer.startSimProcess();
        resetMenuItem.setEnabled(false);
    }

    /**
     * Pause the simulation process.
     */
    public void pauseSimProcess() {
        selectedViewer.pauseSimProcess();

        // update the buttons
        startButton.setText("Resume");
        stepButton.setEnabled(true);
        // update the menu items
        startMenuItem.setText("Resume");
        stepMenuItem.setEnabled(true);
        resetMenuItem.setEnabled(true);
    }

    /**
     * Resume the simulation process.
     */
    public void resumeSimProcess() {
        selectedViewer.resumeSimProcess();

        // update the buttons
        startButton.setText("Pause");
        stepButton.setEnabled(false);
        // update the menu items
        startMenuItem.setText("Pause");
        stepMenuItem.setEnabled(false);
        resetMenuItem.setEnabled(false);
    }

    /**
     * Reset the simulation process.
     */
    public void resetSimProcess() {
        selectedViewer.resetSimProcess();
        setSimResetGUIsetting();
        tabbedPane.setEnabled(true);
    }

    public void startUdpListening() {
        assert startUdpListenerMenuItem.isEnabled();

        if (((AIMSimViewer) selectedViewer).udpListenerHasStarted()) {
            startUdpListenerMenuItem.setEnabled(false);
            stopUdpListenerMenuItem.setEnabled(true);
            ((AIMSimViewer) selectedViewer).startUdpListening();
        }
        else if (((RIMSimViewer) selectedViewer).udpListenerHasStarted()) {
            startUdpListenerMenuItem.setEnabled(false);
            stopUdpListenerMenuItem.setEnabled(true);
            ((RIMSimViewer) selectedViewer).startUdpListening();
        }else {
            System.err.printf("Failed to start UDP listener...\n");
        }

    }

    /**
     * Stop the UDP listening
     */
    private void stopUdpListening() {
        assert stopUdpListenerMenuItem.isEnabled();

        if (!((AIMSimViewer) selectedViewer).udpListenerHasStarted()) {
            startUdpListenerMenuItem.setEnabled(true);
            stopUdpListenerMenuItem.setEnabled(false);
            ((AIMSimViewer) selectedViewer).stopUdpListening();
            ((AIMSimViewer) selectedViewer).removeUdpListener();
        } else if (!((RIMSimViewer) selectedViewer).udpListenerHasStarted()) {
            startUdpListenerMenuItem.setEnabled(true);
            stopUdpListenerMenuItem.setEnabled(false);
            ((RIMSimViewer) selectedViewer).stopUdpListening();
            ((RIMSimViewer) selectedViewer).removeUdpListener();
        }else {
            System.err.printf("Failed to stop UDP listener...\n");
        }
    }

    // ///////////////////////////////////////////
    // ActionListener interface for menu items
    // ///////////////////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public void actionPerformed(ActionEvent e) {
    /*
    if (e.getSource() == autoOnlySimTypeMenuItem) {
      throw new RuntimeException("Cannot change simulation type yet.");
      // // very ugly code. need update
      // GridLayoutUtil.setFCFSManagers((GridLayout)sim.getLayout(),
      // sim.getSimulatedTime(),
      // 1.0);
      // GridLayoutUtil.setUniformRandomSpawnPoints((GridLayout)
      // sim.getLayout(),
      // 0.25);
      // sim = new AutoDriverOnlySimulator(sim.getLayout());
      // autoOnlySimTypeMenuItem.setSelected(true);
      // humanOnlySimTypeMenuItem.setSelected(false);
      // mixedSimTypeMenuItem.setSelected(false);
    } else if (e.getSource() == humanOnlySimTypeMenuItem) {
      throw new RuntimeException("Human drivers only simulation not "
          + "implemented yet");
    } else if (e.getSource() == mixedSimTypeMenuItem) {
      throw new RuntimeException("Mixed drivers simulation not "
          + "implemented yet");
    } else
    */
        if (e.getSource() == startMenuItem || e.getSource() == startButton) {
            startButtonHandler();
            selectedViewer.requestScreenFocusInWindow();
        } else if (e.getSource() == stepMenuItem || e.getSource() == stepButton) {
            stepButtonHandler();
            selectedViewer.requestScreenFocusInWindow();
        } else if (e.getSource() == resetMenuItem) {
            resetSimProcess();
        } else if (e.getSource() == dumpDataMenuItem) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
            int returnVal = chooser.showDialog(this, "Save");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                boolean isDumpData = false;
                String outFileName = null;
                try {
                    outFileName = chooser.getSelectedFile().getCanonicalPath();
                    isDumpData = true;
                } catch (IOException ioe) {
                    // nothing
                }
                if (isDumpData) {
                    selectedViewer.printDataCollectionLinesData(outFileName);
                }
            }
        } else if (e.getSource() == dumpStatScreenMenuItem) {
            JFileChooser chooser = new JFileChooser();
            chooser.setFileSelectionMode(JFileChooser.SAVE_DIALOG);
            int returnVal = chooser.showDialog(this, "Save");
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                boolean isDumpData = false;
                String outFileName = null;
                try {
                    outFileName = chooser.getSelectedFile().getCanonicalPath();
                    isDumpData = true;
                } catch (IOException ioe) {
                    // nothing
                }
                if (isDumpData) {
                    selectedViewer.printStatScreenData(outFileName);
                }
            }
        } else if (e.getSource() == saveResultsMenuItem) {
            String csv = getSelectedSimulator().produceResultsCSV();
            java.util.List<String> csvResultAsList = new ArrayList<String>();
            csvResultAsList.add(csv);
            //Save
            FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files","csv");
            final JFileChooser fc = new JFileChooser();
            fc.setFileFilter(filter);
            int returnVal = fc.showSaveDialog(this);
            if(returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    Files.write(Paths.get(file.getAbsolutePath()), csvResultAsList, Charset.forName("UTF-8"));
                } catch (IOException e1) {
                    //nothing
                }
            }
        }
        else if (e.getSource() == startRecordingMenuItem) {
            if (!selectedViewer.isRecording()) {
                JFileChooser chooser = new JFileChooser();
                chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                int returnVal = chooser.showDialog(this, "Choose Directory");
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    try {
                        selectedViewer.setImageDir(chooser.getSelectedFile().getCanonicalPath());
                        selectedViewer.setRecording(true);
                    } catch (IOException ioe) {
                        // nothing
                    }
                    if (selectedViewer.isRecording()) {
                        startRecordingMenuItem.setEnabled(false);
                        stopRecordingMenuItem.setEnabled(true);
                    }
                }
            }
        } else if (e.getSource() == stopRecordingMenuItem) {
            if (selectedViewer.isRecording()) {
                selectedViewer.setRecording(false);
                selectedViewer.setImageCounter(0);
                startRecordingMenuItem.setEnabled(true);
                stopRecordingMenuItem.setEnabled(false);
            }
        } else if (e.getSource() == startUdpListenerMenuItem) {
            startUdpListening();
        } else if (e.getSource() == stopUdpListenerMenuItem) {
            stopUdpListening();
        } else if (e.getSource() == clearDebugPointsMenuItem) {
            Debug.clearLongTermDebugPoints();
        } else if ("Quit".equals(e.getActionCommand())) {
            System.exit(0);
        } // else ignore other events
    }

    // ///////////////////////////////
    // ItemListener interface
    // ///////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public void itemStateChanged(ItemEvent e) {
        Object source = e.getItemSelectable();

        if (source == showSimulationTimeMenuItem) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                selectedViewer.setIsShowSimulationTime(true);
            } else {
                selectedViewer.setIsShowSimulationTime(false);
            }
            selectedViewer.updateCanvas();
        } else if (source == showVinMenuItem) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                selectedViewer.setIsShowVin(true);
            } else {
                selectedViewer.setIsShowVin(false);
            }
            selectedViewer.updateCanvas();
        } else if (source == showIMShapesMenuItem && selectedViewer.equals(aimViewer)) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ((AIMSimViewer) selectedViewer).setIsShowIMDebugShapes(true);
            } else {
                ((AIMSimViewer) selectedViewer).setIsShowIMDebugShapes(false);
            }
            selectedViewer.updateCanvas();
        } else if (source == showIMShapesMenuItem && selectedViewer.equals(rimViewer)) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                ((RIMSimViewer) selectedViewer).setIsShowIMDebugShapes(true);
            } else {
                ((RIMSimViewer) selectedViewer).setIsShowIMDebugShapes(false);
            }
            selectedViewer.updateCanvas();
        }
    }

    // ///////////////////////////////
    // KeyListener interface
    // ///////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public void keyPressed(KeyEvent e) {
        if (!selectedViewer.isSimThreadNull()) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_ENTER:
                    startButtonHandler();
                    break;
                case KeyEvent.VK_SPACE:
                    if (selectedViewer.isSimThreadPaused()) {
                        stepButtonHandler();
                    } else {
                        startButtonHandler();
                    }
                    break;
                case KeyEvent.VK_ESCAPE:
                    resetSimProcess();
                    break;
                default:
                    // do nothing
            }
        } // else ignore the event
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyReleased(KeyEvent e) {
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void keyTyped(KeyEvent e) {
    }

}
