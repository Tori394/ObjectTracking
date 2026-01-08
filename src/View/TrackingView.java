package View;
import Controller.DataGenerator;
import Model.RadarBlob;
import Model.RadarNoise;
import Model.CCL;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrackingView {
    private JPanel mainPanel;
    private JButton generujButton;
    private JButton otsuButton;
    private JPanel radarPanel;
    private JPanel buttonsPanel;

    private RadarView radarCanvas;
    private DataGenerator generator;

    private RadarNoise LastMap;

    public TrackingView() {
        generator = new DataGenerator(800, 560);

        radarPanel.setLayout(new BorderLayout());

        radarCanvas = new RadarView();
        radarPanel.add(radarCanvas, BorderLayout.CENTER);

        generujButton.addActionListener(e -> {
                List<RadarBlob> objects = new ArrayList<>();
                Random rand = new Random();

                for (int i = 0; i < 4; i++) {

                    double x = rand.nextInt(800);
                    double y = rand.nextInt(560);
                    double z = rand.nextInt(5)+2;

                    objects.add(new RadarBlob(x, y,z));
                }

                RadarNoise map = generator.generateRadar(objects);
                LastMap = map;
                radarCanvas.updateMap(map);
        });

        otsuButton.addActionListener( e-> {
            int tresh = CCL.OtsuTreshold(LastMap);
            RadarNoise map = CCL.applyThreshold(LastMap, tresh);
            radarCanvas.updateMap(map);
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
