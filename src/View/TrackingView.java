package View;

import Controller.DataGenerator;
import Model.*;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TrackingView {
    public static final int W = 800;
    public static final int H = 600;
    private static final int MARGIN = 80;

    private JPanel mainPanel;
    private JButton generujButton;
    private JPanel radarPanel;
    private JPanel buttonsPanel;
    private JPanel radioPanel;
    private JPanel generatingPanel;
    private JButton nextFrameButton;
    private JRadioButton otsuView;
    private JRadioButton radarView;
    private JCheckBox showBlobsCheckBox;
    private JCheckBox showTracksChceckBox;
    private ButtonGroup bg;

    private RadarCanvas radarCanvas;
    private DataGenerator generator;

    private RadarImage LastMap;
    private RadarImage OtsuMap;
    private List<RadarBlob> objects;
    private final Random rand;

    private TrackingSystem trackingSystem;
    private Timer simulationTimer;

    public TrackingView() {
        generator = new DataGenerator(W, H);
        rand = new Random();
        trackingSystem = new TrackingSystem();

        radarPanel.setLayout(new BorderLayout());

        radarCanvas = new RadarCanvas();
        radarCanvas.setPreferredSize(new Dimension(W, H));

        radarPanel.add(radarCanvas, BorderLayout.CENTER);

        bg = new ButtonGroup();
        bg.add(otsuView);
        bg.add(radarView);

        // Zablokowanie przycisków
        showBlobsCheckBox.setEnabled(false);
        showTracksChceckBox.setEnabled(false);
        otsuView.setEnabled(false);
        radarView.setEnabled(false);

        // PRZYCISK GENERUJ
        generujButton.addActionListener(e -> {
            if (simulationTimer != null && simulationTimer.isRunning()) simulationTimer.stop();

            objects = new ArrayList<>();
            trackingSystem = new TrackingSystem();
            radarCanvas.clearOverlays();

            radarView.setSelected(true);

            for (int i = 0; i < 50; i++) {

                double x = rand.nextInt(W - 100) + 70;
                double y = rand.nextInt(H - 100) + 70;

                double xv = (rand.nextDouble() * 6) - 3;
                double yv = (rand.nextDouble() * 6) - 3;

                RadarBlob blob = new RadarBlob(x, y, xv, yv, rand.nextInt(8)+4);
                objects.add(blob);
            }

            processFrame();

            // Odblokowanie przycisków
            showBlobsCheckBox.setEnabled(true);
            showTracksChceckBox.setEnabled(true);
            otsuView.setEnabled(true);
            radarView.setEnabled(true);
        });

        // PRZYCISK RUN
        nextFrameButton.addActionListener(e -> {
            if (simulationTimer != null && simulationTimer.isRunning()) {
                return;
            }
            if (objects == null || objects.isEmpty()) return;

            System.out.print("\n\n\nStart symulacji\n\n");

            simulationTimer = new Timer(50, event -> {

                if (objects.isEmpty()) {
                    simulationTimer.stop();
                    System.out.println("Koniec symulacji - brak obiektów.");
                    return;
                }

                java.util.Iterator<RadarBlob> it = objects.iterator();

                while (it.hasNext()) {
                    RadarBlob blob = it.next();

                    blob.move();

                    if (blob.getX() < -MARGIN || blob.getX() > W + MARGIN ||
                            blob.getY() < -MARGIN || blob.getY() > H + MARGIN) {
                        it.remove();
                    }
                }

                processFrame();
            });

            simulationTimer.start();
        });

        // Obsługa widoków
        otsuView.addActionListener(e -> refreshView());
        radarView.addActionListener(e -> refreshView());
        showBlobsCheckBox.addActionListener(e -> refreshView());
        showTracksChceckBox.addActionListener(e -> refreshView());
    }

    private void processFrame() {
        LastMap = generator.generateRadar(objects);
        OtsuMap = Otsu.applyThreshold(LastMap, Otsu.OtsuTreshold(LastMap));

        List<DetectedObject> rawBlobs = CCL.extract(OtsuMap, LastMap);
        trackingSystem.update(rawBlobs);
        refreshView();
    }

    private void refreshView() {
        if (LastMap == null) return;

        radarCanvas.updateMap(otsuView.isSelected() ? OtsuMap : LastMap);
        radarCanvas.clearOverlays();

        if (showBlobsCheckBox.isSelected()) {
            radarCanvas.addOverlay(new BlobOverlay(CCL.extract(OtsuMap, LastMap)));
        }

        if (showTracksChceckBox.isSelected()) {
            radarCanvas.addOverlay(new TracksOverlay(trackingSystem.getTracks()));
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tracking System");
        frame.setContentPane(new TrackingView().mainPanel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
    }
}