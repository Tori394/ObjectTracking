package View;

import Model.RadarImage;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RadarCanvas extends JPanel {
    private RadarImage currentMap;
    private boolean overlayState;
    private BlobOverlay overlay;

    public void updateMap(RadarImage newMap, boolean checkbox) {
        overlayState = checkbox;
        this.currentMap = newMap;
        this.revalidate();
        this.repaint();
    }

    @Override
    public Dimension getPreferredSize() {
        if (currentMap != null) {
            return new Dimension(currentMap.getWidth(), currentMap.getHeight());
        }
        return new Dimension(800, 560); // Domyślny rozmiar
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

                Color c = new Color(brightness, brightness, brightness);
                int color = c.getRGB();

                image.setRGB(x, y, color);
            }
        }

        g.drawImage(image, 0, 0, this);

        if (overlayState && overlay != null) {
            Graphics2D g2d = (Graphics2D) g;
            overlay.paintOverlay(g2d);
        }
    }

    public void addOverlay(BlobOverlay o) {
        overlay = o;
    }

}