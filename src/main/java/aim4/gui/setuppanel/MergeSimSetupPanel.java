package aim4.gui.setuppanel;

import aim4.gui.parampanel.merge.MergeParamPanel;
import aim4.gui.parampanel.merge.S2SMergeParamPanel;
import aim4.gui.parampanel.merge.SingleLaneParamPanel;
import aim4.sim.setup.merge.MergeSimSetup;
import aim4.sim.setup.merge.enums.MapType;
import aim4.sim.setup.merge.enums.ProtocolType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

/**
 * Created by Callum on 08/02/2017.
 */
public class MergeSimSetupPanel extends SimSetupPanel implements ItemListener {
    //COMBOBOX OPTIONS//
    /**
     * Merge types for Merge Combobox
     */
    final static String S2S_MERGE_TITLE = "Single-to-Single Merge";
    final static String SINGLE_LANE_TEST = "Single Lane RimMapTestApplet";

    //GUI ELEMENTS//
    private JComboBox mergeComboBox;
    private JPanel cards;
    private CardLayout cardLayout;

    private S2SMergeParamPanel s2sPanel;
    private SingleLaneParamPanel singleLaneParamPanel;

    public MergeSimSetupPanel(){
        //Setup the mergeComboBox
        JPanel comboBoxPane = new JPanel();
        comboBoxPane.setBackground(Color.WHITE);

        String comboBoxItems[] =
                {
                        S2S_MERGE_TITLE,
                        SINGLE_LANE_TEST
                };
        mergeComboBox = new JComboBox(comboBoxItems);
        mergeComboBox.setEditable(false);
        mergeComboBox.addItemListener(this);
        comboBoxPane.add(mergeComboBox);

        //Create the cards pane
        cardLayout = new CardLayout();
        cards = new JPanel(cardLayout);

        //Create the parameter panels
        s2sPanel = new S2SMergeParamPanel();
        singleLaneParamPanel = new SingleLaneParamPanel();
        addParamPanel(s2sPanel, S2S_MERGE_TITLE);
        addParamPanel(singleLaneParamPanel, SINGLE_LANE_TEST);

        //Put them together
        setLayout(new BorderLayout());
        add(comboBoxPane, BorderLayout.PAGE_START);
        add(cards, BorderLayout.CENTER);
    }

    private void addParamPanel(MergeParamPanel paramPanel, String paramLabel) {
        JScrollPane scrollPane = new JScrollPane(paramPanel);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        cards.add(scrollPane, paramLabel);
    }

    @Override
    public MergeSimSetup getSimSetup() {
        switch(mergeComboBox.getSelectedIndex()){
            case 0: return s2sPanel.getSimSetup();
            case 1: return singleLaneParamPanel.getSimSetup();
            default: throw new RuntimeException("Merge type combo box went out of range");
        }
    }

    public MapType getMapType() {
        switch(mergeComboBox.getSelectedIndex()){
            case 0: return MapType.S2S;
            case 1: return MapType.SINGLE;
            default: throw new RuntimeException("Merge type combo box went out of range");
        }
    }

    public ProtocolType getProtocolType() {
        switch(mergeComboBox.getSelectedIndex()){
            case 0: return s2sPanel.getProtocolType();
            case 1: return ProtocolType.NONE;
            default: throw new RuntimeException("Merge type combo box went out of range");
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        cardLayout.show(cards, (String)e.getItem());
    }
}
