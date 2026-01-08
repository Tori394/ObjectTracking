package View;
import Controller.DataGenerator;
import Model.RadarBlob;
import Model.RadarNoise;

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

    public TrackingView() {
        generator = new DataGenerator(800, 560);

        radarPanel.setLayout(new BorderLayout());

        radarCanvas = new RadarView();
        radarPanel.add(radarCanvas, BorderLayout.CENTER);

        generujButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                List<RadarBlob> objects = new ArrayList<>();
                Random rand = new Random();

                for (int i = 0; i < 4; i++) {

                    double x = rand.nextInt(800);
                    double y = rand.nextInt(560);
                    double z = rand.nextInt(3)+2;

                    objects.add(new RadarBlob(x, y,z));
                }

                RadarNoise mapa = generator.generateRadar(objects);
                radarCanvas.updateMap(mapa);
            }
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
