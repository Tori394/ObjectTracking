package View;
import Controller.DataGenerator;
import Model.RadarNoise;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

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
                RadarNoise mapa = generator.generateNoise();
                radarCanvas.updateMap(mapa);
            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tabela – przykład JTable");
        frame.setContentPane(new TrackingView().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 600);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}
