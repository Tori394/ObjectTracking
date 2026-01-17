package View;

import Model.RadarImage;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class RadarCanvas extends JPanel {
    private RadarImage currentMap;

    private final List<RadarOverlay> overlays = new ArrayList<>();

    public void updateMap(RadarImage newMap) {
        this.currentMap = newMap;
        this.revalidate();
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (currentMap != null) {
            return new Dimension(currentMap.getWidth(), currentMap.getHeight());
        }
        return new Dimension(TrackingView.W, TrackingView.H);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentMap == null) return;

        int w = currentMap.getWidth();
        int h = currentMap.getHeight();

        BufferedImage image = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);

        for (int x = 0; x < w; x++) {
            for (int y = 0; y < h; y++) {
                int brightness = currentMap.getPixel(x, y);
                // Zabezpieczenie zakresu 0-255
                brightness = Math.max(0, Math.min(255, brightness));

                Color c = new Color(brightness, brightness, brightness);
                image.setRGB(x, y, c.getRGB());
            }
        }

        g.drawImage(image, 0, 0, this);

        Graphics2D g2d = (Graphics2D) g;

        List<RadarOverlay> toDraw = new ArrayList<>(overlays);

        for (RadarOverlay overlay : toDraw) {
            overlay.paintOverlay(g2d);
        }
    }

    public void addOverlay(RadarOverlay o) {
        overlays.add(o);
        repaint();
    }

    public void clearOverlays() {
        overlays.clear();
        repaint();
    }

}