package aim4.gui.viewer;

import aim4.config.Constants;
import aim4.config.Debug;
import aim4.config.SimConfig;
import aim4.gui.StatusPanelContainer;
import aim4.gui.Viewer;
import aim4.gui.ViewerCardType;
import aim4.gui.ViewerDebugView;
import aim4.gui.frame.VehicleInfoFrame;
import aim4.gui.screen.Canvas;
import aim4.gui.screen.SimScreen;
import aim4.gui.screen.StatScreen;
import aim4.gui.setuppanel.SimSetupPanel;
import aim4.sim.Simulator;
import aim4.sim.setup.SimFactory;
import aim4.sim.setup.SimSetup;
import aim4.util.Util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;

/**
 * Created by Callum on 09/11/2016.
 */
public abstract class SimViewer extends JPanel implements
        MouseListener,
        ViewerDebugView {
    // ///////////////////////////////
    // CONSTANTS
    // ///////////////////////////////
    /**
     * The number of simulation seconds per GUI second. If it is larger than or
     * equal to <code>TURBO_SIM_SPEED</code>, the simulation will run as fast as
     * possible.
     */
    public static final double DEFAULT_SIM_SPEED = 15.0;
    /**
     * The number of screen updates per GUI second. If it is larger than or
     * equal to SimConfig.CYCLES_PER_SECOND, the screen will be updated at
     * every time step of the simulation.
     */
    public static final double DEFAULT_TARGET_FRAME_RATE = 20.0;
    /**
     * The inset size of the setup panels
     */
    private static final int SIM_SETUP_PANE_GAP = 50;
    /**
     * The simulation speed (simulation seconds per GUI second) at or beyond which
     * the turbo mode is on (i.e., the simulation will run as fast as possible)
     */
    public static final double TURBO_SIM_SPEED = 15.0;

    // ///////////////////////////////
    // GUI ELEMENTS
    // ///////////////////////////////
    /**
     * Sim Setup Panel
     */
    private SimSetupPanel simSetupPanel;
    /**
     * The card layout for the canvas
     */
    private CardLayout screenCardLayout;
    /**
     * Stores the screen which will be shown
     */
    protected SimScreen simScreen;
    /**
     * The canvas on which to draw the state of the simulator.
     */
    protected Canvas canvas;
    /**
     * The stat screen to display during the running of the simulation.
     */
    protected StatScreen statScreen;
    /**
     * Indicates whether the canvas should be enabled rather than the StatScreen
     */
    protected boolean liveViewSupported;
    /**
     * Reference to StatusPanelContainer
     */
    private StatusPanelContainer statusPanel;
    /** The frame for showing a vehicle information */
    protected VehicleInfoFrame vehicleInfoFrame;


    // ///////////////////////////////
    // SIM COMPONENTS
    // ///////////////////////////////
    /**
     * The Simulator running in this Viewer.
     */
    protected Simulator sim;
    /**
     * The simulation's thread
     */
    private SimThread simThread;
    /**
     * The target simulation speed
     */
    private double targetSimSpeed;
    /**
     * The target frame rate
     */
    private double targetFrameRate;
    /**
     * The time of the next screen update in millisecond
     */
    private long nextFrameTime;
    // ///////////////////////////////
    // RECORDING COMPONENTS
    // ///////////////////////////////
    // TODO: reset imageCounter after reset the simulator
    /**
     * Whether or not to save the screen during simulation
     */
    private boolean recording;
    /**
     * Image's directino
     */
    private String imageDir;
    /**
     * The number of generated images
     */
    private int imageCounter;

    // ///////////////////////////////
    // CLASS CONSTRUCTORS
    // ///////////////////////////////
    /**
     * Creates the SimViewer
     * @param statusPanel A reference to the StatusPanelContainer in Viewer
     * @param simSetupPanel A JPanel with the setup controls for the SimViewer
     */
    public SimViewer(StatusPanelContainer statusPanel, Viewer viewer, SimSetupPanel simSetupPanel, Boolean liveViewSupported) {
        this.statusPanel = statusPanel;
        this.simSetupPanel = simSetupPanel;
        this.sim = null;
        this.simThread = null;
        targetSimSpeed = DEFAULT_SIM_SPEED;
        // the frame rate cannot be not larger than the simulation cycle
        targetFrameRate =
                Math.min(DEFAULT_TARGET_FRAME_RATE, SimConfig.CYCLES_PER_SECOND);
        this.nextFrameTime = 0; // undefined yet.

        this.recording = false;
        this.imageDir = null;
        this.imageCounter = 0;

        this.liveViewSupported = liveViewSupported;
        if(liveViewSupported) {
            createCanvas(viewer);
            simScreen = this.canvas;
        } else {
            createStatScreen(viewer);
            simScreen = this.statScreen;
        }
        setComponentsLayout();
        setVisible(true);
    }

    /////////////////////////////////
    // NESTED CLASSES
    /////////////////////////////////
    //
    // TODO: SimThread should be a SwingWorker; but it works fine now.
    // http://java.sun.com/docs/books/tutorial/uiswing/concurrency/worker.html
    //
    /**
     * The simulation thread that holds the simulation process.
     */
    public class SimThread implements Runnable {

        // ///////////////////////////////
        // PRIVATE FIELDS
        // ///////////////////////////////
        /**
         * The simulation thread
         */
        private volatile Thread blinker;
        /**
         * Whether the turbo mode is on
         */
        private boolean isTurboMode;
        /**
         * In the turbo mode, it is the duration of each execution period In
         * the non turbo mode, it is the time period between simulation steps
         */
        private long timeDelay;
        /**
         * Whether the stepping mode is on
         */
        private boolean isSteppingMode;
        /**
         * Whether the simulation is stopped
         */
        private boolean isStopped;

        // ///////////////////////////////
        // CONSTRUCTORS
        // ///////////////////////////////
        /**
         * Create a simulation thread.
         *
         * @param isTurboMode Whether the turbo mode is on
         * @param timeDelay   The time delay
         */
        public SimThread(boolean isTurboMode, long timeDelay) {
            this.blinker = null;
            this.isTurboMode = isTurboMode;
            this.timeDelay = timeDelay;
            this.isSteppingMode = false;
            this.isStopped = false;
        }

        // ///////////////////////////////
        // PUBLIC METHODS
        // ///////////////////////////////

        // information retrieval
        /**
         * Whether the thread is stopped.
         *
         * @return Whether the thread is stopped.
         */
        public boolean isPaused() {
            return isStopped;
        }

        /**
         * Whether the thread is in the turbo mode.
         *
         * @return Whether the thread is in the turbo mode.
         */
        public boolean isTurboMode() {
            return isTurboMode;
        }

        // Settings
        /**
         * Set whether the turbo mode is on
         *
         * @param isTurboMode Whether the turbo mode is on
         */
        public synchronized void setTurboMode(boolean isTurboMode) {
            this.isTurboMode = isTurboMode;
        }

        /**
         * Set whether the stepping mode is on
         *
         * @param isSteppingMode Whether the stepping mode is on
         */
        public synchronized void setSteppingMode(boolean isSteppingMode) {
            this.isSteppingMode = isSteppingMode;
        }

        /**
         * Set the time delay.
         *
         * @param timeDelay the time delay.
         */
        public synchronized void setTimeDelay(long timeDelay) {
            this.timeDelay = timeDelay;
        }

        // ///////////////////////////////
        // PUBLIC METHODS
        // ///////////////////////////////

        // thread control
        /**
         * Start the simulation thread.
         */
        public synchronized void start() {
            assert blinker == null;
            this.blinker = new Thread(this, "AIM4 Simulator Thread");
            blinker.start();
        }

        /**
         * Terminate/Kill this simulation thread.
         */
        public synchronized void terminate() {
            assert blinker != null;
            blinker = null;
        }

        /**
         * Pause this simulation thread
         */
        public void pause() {
            // must have no synchronized keyword in order to avoid
            // funny behavior when the user clicks the "Pause" button.
            assert !isStopped;
            isStopped = true;
        }

        /**
         * Resume this simulation thread
         */
        public synchronized void resume() {
            assert isStopped;
            isStopped = false;
        }

        // ///////////////////////////////
        // PUBLIC METHODS
        // ///////////////////////////////

        // the run() function of the thread
        /**
         * {@inheritDoc}
         */
        @Override
        public void run() {
            Thread thisThread = Thread.currentThread();
            try {
                while (blinker == thisThread) {
                    if (isStopped) {
                        try {
                            Thread.sleep(10L); // just sleep for a very short moment
                        } catch (InterruptedException e) {
                            // do nothing
                        }
                    } else if (isTurboMode) {
                        runTurboMode();
                    } else {
                        runNormalMode();
                    }
                    // in any case, give other threads a chance to execute
                    Thread.yield();
                }
            } catch (Exception e) {
                showErrorMessage(e);
            }
            System.err.printf("The simulation has terminated.\n");
        }

        /**
         * Run the thread in the turbo mode.
         */
        private synchronized void runTurboMode() {
            double nextFastRunningStepTime = System.currentTimeMillis() + timeDelay;
            while (!isStopped) {
                runSimulationStep();
                // give GUI a chance to update the screen
                if (!updateScreenForOneStepInFastRunningMode()) {
                    break;
                }
                // only one simulation step in stepping mode
                if (isSteppingMode) {
                    break;
                }
                // check to see whether the time is up
                if (System.currentTimeMillis() >= nextFastRunningStepTime) {
                    break;
                }
            }
            // give GUI a chance to update the screen
            updateScreenInTurboMode();
            // if in stepping mode, just stop until resume() is called
            if (isSteppingMode) {
                isStopped = true;
            }
        }

        /**
         * Run the thread in the normal mode.
         */
        private synchronized void runNormalMode() {
            long nextInvokeTime = System.currentTimeMillis() + timeDelay;
            // Advance the simulation for one step
            runSimulationStep();
            // give GUI a chance to update the screen
            updateScreenInNormalMode();
            // if in stepping mode, just stop until resume() is called
            if (isSteppingMode) {
                isStopped = true;
            } else {
                // else may sleep for a while
                long t = nextInvokeTime - System.currentTimeMillis();
                if (t > 0) {
                    try {
                        Thread.sleep(t);
                    } catch (InterruptedException e) {
                        // do nothing
                    }
                } else {
                    // System.err.printf("Warning: Simulation is slower than GUI\n");
                }
            }
        }

    }

    // //////////////////////////////////////////////////
    // Methods invoked by SimThread
    // //////////////////////////////////////////////////
    /**
     * Run the simulation
     */
    protected Simulator.SimStepResult runSimulationStep() {
        Debug.clearShortTermDebugPoints();
        Simulator.SimStepResult simStepResult = sim.step(SimConfig.TIME_STEP);

        return simStepResult;
    }

    /**
     * Update screen after a simulation step for the fast running mode
     *
     * @return true if the simulation's thread should continue to execute; false
     * if the simulation's thread should take a break and call
     * updateScreenInFastRunningMode();
     */
    private boolean updateScreenForOneStepInFastRunningMode() {
        return (0.0 < targetFrameRate)
                && (targetFrameRate < SimConfig.CYCLES_PER_SECOND);
    }

    /**
     * Update screen after an execution period in the fast running mode
     */
    private void updateScreenInTurboMode() {
        if (0.0 < targetFrameRate) {
            updateScreen();
            saveScreenShot();
        }
    }

    /**
     * Update screen for the non fast running mode
     */
    private void updateScreenInNormalMode() {

        if (targetFrameRate >= SimConfig.CYCLES_PER_SECOND) {
            // update as fast as possible
            updateScreen();
            saveScreenShot();
        } else if (0.0 < targetFrameRate) {
            if (System.currentTimeMillis() > nextFrameTime) {
                updateScreen();
                saveScreenShot();
                nextFrameTime =
                        System.currentTimeMillis() + (long) (1000.0 / targetFrameRate);
            }
        } // else targetFrameRate == 0.0 then do nothing
    }

    // //////////////////////////////////////////////////
    // Basic fucntions for GUI updates
    // //////////////////////////////////////////////////
    /**
     * Update the screen
     */
    private void updateScreen() {
        simScreen.update();
        statusPanel.update();
    }

    /**
     * Save a screenshot
     */
    private void saveScreenShot() {
        if (recording && imageDir != null) {
            String outFileName =
                    imageDir + "/" + Constants.LEADING_ZEROES.format(imageCounter++)
                            + ".png";
            canvas.saveScreenShot(outFileName);
        }
    }

    private void showErrorMessage(Exception e) {
        String stackTraceMessage = "";
        for(StackTraceElement line : e.getStackTrace())
            stackTraceMessage += line.toString() + "\n";
        JOptionPane.showMessageDialog(
                this,
                String.format(
                        "An error was thrown!\nMessage: %s\nStack Trace:\n%s",
                        e.getLocalizedMessage(),
                        stackTraceMessage
                ),
                "Ooops...",
                JOptionPane.ERROR_MESSAGE
        );
        throw new RuntimeException(e);
    }

    // //////////////////////////////////////////////////
    // GUI Methods
    // //////////////////////////////////////////////////
    /**
     * Create all components in the viewer
     */
    protected void createCanvas(Viewer viewer) {
        throw new NoCanvasException();
    }

    protected void createStatScreen(Viewer viewer) {
        throw new NoStatScreenException();
    }

    /**
     * Set the layout of the viewer
     */
    private void setComponentsLayout() {
        // set the card layout for the layered pane
        screenCardLayout = new CardLayout();
        this.setLayout(screenCardLayout);

        // create the pane for containing the sim setup pane
        JPanel panel1 = new JPanel();
        panel1.setBackground(Canvas.GRASS_COLOR);
        panel1.setLayout(new GridBagLayout());
        GridBagConstraints c1 = new GridBagConstraints();
        c1.gridx = 0;
        c1.gridy = 0;
        c1.fill = GridBagConstraints.BOTH;
        c1.weightx = 1.0;
        c1.weighty = 1.0;
        c1.insets = new Insets(SIM_SETUP_PANE_GAP,
                SIM_SETUP_PANE_GAP,
                SIM_SETUP_PANE_GAP,
                SIM_SETUP_PANE_GAP);
        panel1.add(simSetupPanel, c1);
        // add the panel to the top layer
        this.add(panel1, "SIM_SETUP_PANEL");
        // add the canvas or the stat screen to the second layer
        if(liveViewSupported)
            this.add(canvas, "SCREEN");
        else
            this.add(statScreen, "SCREEN");
    }

    // ///////////////////////////////
    // ACCESSORS
    // ///////////////////////////////
    /**
     * Get the simulator object.
     *
     * @return the simulator object; null if the simulator object has not been
     * created.
     */
    public Simulator getSimulator() {
        return sim;
    }

    /**
     * Is the SimViewer currently recording
     * @return A boolean indicating whether the SimViewer is recording
     */
    public boolean isRecording() {
        return recording;
    }

    /**
     * Returns true if simThread is null
     * @return A boolean indicating if simThread is null.
     */
    public boolean isSimThreadNull() {
        return simThread == null;
    }

    /**
     * Returns true if simThread is paused
     * @return A boolean indicating if simThread is paused.
     */
    public boolean isSimThreadPaused() {
        return simThread.isPaused();
    }

    /**
     * Sets the boolean indicating whether the SimViewer is recording
     * @param recording A boolean indicating whether the SimViewer is recording
     */
    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    /**
     * Sets the image directory name where the recorded frames are stored
     * @param imageDir The directory where recorded frames are stored
     */
    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    /**
     * Sets the number of generated images
     * @param imageCounter The number of generated images
     */
    public void setImageCounter(int imageCounter) {
        this.imageCounter = imageCounter;
    }

    // ///////////////////////////////
    // PROCTECTED ACCESSORS
    // ///////////////////////////////
    protected SimSetupPanel getSimSetupPanel() {
        return this.simSetupPanel;
    }

    // ///////////////////////////////
    // CANVAS MANIPULATORS
    // ///////////////////////////////

    /**
     * Changes the card displayed in the SimViewer
     * @param cardType The card type to change to
     */
    public void showCard(ViewerCardType cardType) {
        screenCardLayout.show(this, cardType.toString());
    }

    /**
     * Calls initWithGivenMap() on the canvas using the map provided by sim.getMap().
     */
    public void startViewer() {
        if(liveViewSupported)
            canvas.initWithGivenMap(sim.getMap());
        else
            statScreen.start();
    }

    /**
     * Calls cleanUp() on the canvas
     */
    public void cleanUp() {
        simScreen.cleanUp();
    }

    /**
     * Calls requestFocusInWindow() on the canvas
     */
    public void requestScreenFocusInWindow() {
        if(liveViewSupported)
            canvas.requestFocusInWindow();
        else
            statScreen.requestFocusInWindow();
    }

    /**
     * Set whether to show the simulation time on the canvas.
     * @param showSimulationTime whether to show the simulation time
     */
    public void setIsShowSimulationTime(boolean showSimulationTime) {
        this.canvas.setIsShowSimulationTime(showSimulationTime);
    }

    /**
     * Set whether to show the VIN numbers.
     *
     * @param showVin whether to show the VIN numbers
     */
    public void setIsShowVin(boolean showVin) {
        this.canvas.setIsShowVin(showVin);
    }

    /**
     * Calls update() on the canvas
     */
    public void updateCanvas() {
        this.canvas.update();
    }

    // ///////////////////////////////
    // SIMULATION CONTROLS
    // ///////////////////////////////

    /**
     * Calls printDataCollectionLinesData() on the Map provided by sim.getMap().
     * @param outFileName The file name to print the data collection lines to.
     */
    public void printDataCollectionLinesData(String outFileName) {
        sim.getMap().printDataCollectionLinesData(outFileName);
    }

    /**
     *
     */
    public void printStatScreenData(String outFileName) {
        statScreen.printData(outFileName);
    }

    /**
     * Start the simulation process.
     */
    public void startSimProcess() {
        nextFrameTime = System.currentTimeMillis();
        simThread.start();
    }

    /**
     * Creates the simulation instance
     */
    public void createSimulator() {
        runBeforeCreatingSimulator();
        SimSetup simSetup = simSetupPanel.getSimSetup();
        assert sim == null && simSetup != null;
        // create the simulator
        sim = SimFactory.makeSimulator(simSetup);
        // create the simulation thread
        createSimThread();
    }

    /**
     * Pause the simulation process.
     */
    public void pauseSimProcess() {
        assert simThread != null && !simThread.isPaused();
        simThread.pause();
    }

    /**
     * Resume the simulation process.
     */
    public void resumeSimProcess() {
        assert simThread != null && simThread.isPaused();

        simThread.setSteppingMode(false);
        simThread.resume();
        nextFrameTime = System.currentTimeMillis();
    }

    /**
     * Step the simulation process.
     */
    public void stepSimProcess() {
        assert simThread != null && simThread.isPaused();

        simThread.setSteppingMode(true);
        simThread.resume();
    }

    /**
     * Reset the simulation process.
     */
    public void resetSimProcess() {
        runBeforeResettingSimulator();
        assert simThread != null;

        simThread.terminate();
        if (simThread.isPaused()) {
            simThread.setSteppingMode(false);
            simThread.resume();
        }
        simThread = null;
        sim = null;
    }

    protected abstract void runBeforeCreatingSimulator();

    protected abstract void runBeforeResettingSimulator();

    /**
     * Initialize the default Simulator to use.
     */
    private void createSimThread() {
        if (0 < targetSimSpeed
                && targetSimSpeed < TURBO_SIM_SPEED) {
            long timerDelay =
                    (long) (1000.0 * SimConfig.TIME_STEP / targetSimSpeed);
            simThread = new SimThread(false, timerDelay);
        } else {
            long timerDelay;
            if (targetFrameRate < SimConfig.CYCLES_PER_SECOND) {
                timerDelay = (long) (1000.0 / targetFrameRate);
            } else {
                timerDelay = (long) (1000.0 / SimConfig.CYCLES_PER_SECOND);
            }
            simThread = new SimThread(true, timerDelay);
        }
    }

    /**
     * Set the target simulation speed.
     *
     * @param simSpeed  set the target simulation speed
     */
    public void setTargetSimSpeed(double simSpeed) {
        this.targetSimSpeed = simSpeed;
        if (simThread != null) {
            if (Util.isDoubleZero(simSpeed)) {
                long timerDelay = (long) (1000.0 * SimConfig.TIME_STEP / 0.1);
                simThread.setTimeDelay(timerDelay);
                simThread.setTurboMode(false);
                if (!simThread.isPaused()) {
                    pauseSimProcess();
                }
            } else if (Util.isDoubleEqualOrGreater(simSpeed, TURBO_SIM_SPEED)) {
                long timerDelay;
                if (targetFrameRate < SimConfig.CYCLES_PER_SECOND) {
                    timerDelay = (long) (1000.0 / targetFrameRate);
                } else {
                    timerDelay = (long) (1000.0 / SimConfig.CYCLES_PER_SECOND);
                }
                simThread.setTimeDelay(timerDelay);
                simThread.setTurboMode(true);
                if (simThread.isPaused()) {
                    resumeSimProcess();
                }
            } else {
                long timerDelay = (long) (1000.0 * SimConfig.TIME_STEP / simSpeed);
                simThread.setTimeDelay(timerDelay);
                simThread.setTurboMode(false);
                if (simThread.isPaused()) {
                    resumeSimProcess();
                }
            }
        }
        requestScreenFocusInWindow();
    }

    /**
     * Set the target frame rate.
     *
     * @param targetFrameRate the target frame rate
     */
    public void setTargetFrameRate(double targetFrameRate) {
        this.targetFrameRate =
                Math.min(targetFrameRate, SimConfig.CYCLES_PER_SECOND);

        if (simThread != null) {
            if (simThread.isTurboMode()) {
                long timerDelay;
                if (0.0 < targetFrameRate) {
                    timerDelay = (long) (1000.0 / targetFrameRate);
                } else {
                    timerDelay = (long) (1000.0 / 10.0);
                }
                simThread.setTimeDelay(timerDelay);
            }
        }
    }

    public boolean hasStatScreen() {
        if (statScreen != null) {
            return true;
        }
        return false;
    }


    /////////////////////////////////
    // DEBUG
    /////////////////////////////////
    /**
     * {@inheritDoc}
     */
    @Override
    public void highlightVehicle(int vin) {
        canvas.highlightVehicle(vin);

    }

    protected class NoCanvasException extends RuntimeException {
        public NoCanvasException() {
            super("Expected SimViewer to use a Canvas, but no Canvas was created." +
                    " Does this SimViewer implement LiveView or use StatPanel?");
        }
    }

    protected class NoStatScreenException extends RuntimeException {
        public NoStatScreenException() {
            super("Expected SimViewer to use a StatScreen, but no StatScreen was created." +
                    " Does this SimViewer implement LiveView or use StatPanel?");
        }
    }

}
