package View;

import Controller.DataGenerator;
import Model.CCL;
import Model.RadarBlob;
import Model.RadarNoise;
import Model.Otsu;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrackingView {
    private JPanel mainPanel;
    private JButton generujButton;
    private JPanel radarPanel;
    private JPanel buttonsPanel;
    private JRadioButton otsuView;
    private JRadioButton radarView;
    private JPanel radioPanel;
    private JCheckBox showBlobsCheckBox;

    private RadarCanvas radarCanvas;
    private DataGenerator generator;

    private RadarNoise LastMap;
    private RadarNoise OtsuMap;

    public TrackingView() {
        generator = new DataGenerator(800, 560);

        radarPanel.setLayout(new BorderLayout());

        ButtonGroup group = new ButtonGroup();
        group.add(otsuView);
        group.add(radarView);
        otsuView.setEnabled(false);
        radarView.setEnabled(false);
        showBlobsCheckBox.setEnabled(false);

        radarCanvas = new RadarCanvas();
        radarPanel.add(radarCanvas, BorderLayout.CENTER);

        generujButton.addActionListener(e -> {
                List<RadarBlob> objects = new ArrayList<>();
                Random rand = new Random();

                for (int i = 0; i < 4; i++) {

                    double x = rand.nextInt(800);
                    double y = rand.nextInt(560);
                    double z = rand.nextInt(6)+4;

                    objects.add(new RadarBlob(x, y,z));
                }

                RadarNoise map = generator.generateRadar(objects);
                LastMap = map;

                OtsuMap = Otsu.applyThreshold(map, Otsu.OtsuTreshold(map));

                radarCanvas.addOverlay(new BlobOverlay(CCL.extract(OtsuMap)));

                radarCanvas.updateMap(map, false);

                showBlobsCheckBox.setSelected(false);
                showBlobsCheckBox.setEnabled(true);
                otsuView.setEnabled(true);
                otsuView.setSelected(false);
                radarView.setEnabled(true);
                radarView.setSelected(true);
        });

        otsuView.addActionListener( e-> {
            radarCanvas.updateMap(OtsuMap, showBlobsCheckBox.isSelected());
        });

        radarView.addActionListener( e-> {
            radarCanvas.updateMap(LastMap, showBlobsCheckBox.isSelected());
        });

    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Object Tracking");
        frame.setContentPane(new TrackingView().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
