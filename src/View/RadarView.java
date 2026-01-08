package View;

import Model.RadarNoise;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class RadarView extends JPanel {
    private RadarNoise currentMap;

    public void updateMap(RadarNoise newMap) {
        this.currentMap = newMap;
        this.repaint();
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

        g.drawImage(image, 0, 0, getWidth(), getHeight(), null);
    }
}