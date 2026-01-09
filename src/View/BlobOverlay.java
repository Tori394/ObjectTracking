package View;

import Model.DetectedObject;
import java.awt.*;
import java.util.List;

public class BlobOverlay implements RadarOverlay {
    private static final Color[] PALETA = {
            new Color(37, 184, 136),
            new Color(200, 90, 200),
            new Color(108, 153, 236),
            new Color(213, 91, 113),
            new Color(239, 204, 46),
            new Color(41, 213, 29),
            new Color(118, 80, 213),
            new Color(189, 34, 189),
            new Color(24, 5, 189),
            new Color(99, 225, 116),
            new Color(255, 106, 0),
            new Color(38, 244, 189),
            new Color(246, 147, 255)
    };

    private final List<DetectedObject> blobs;

    public BlobOverlay(List<DetectedObject> blobs) {
        this.blobs = blobs;
    }

    @Override
    public void paintOverlay(Graphics2D g) {
        if (blobs == null || blobs.isEmpty()) return;

        int labelId = 0;

        for (DetectedObject blob : blobs) {

            Color c = (PALETA[labelId]);

            // Środek ciężkości
            int cx = (int) blob.getX();
            int cy = (int) blob.getY();

            int radiusX = (int) (blob.stdDevX * 3.0);
            int radiusY = (int) (blob.stdDevY * 3.0);

            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 170));
            g.fillOval(cx - radiusX, cy - radiusY, radiusX * 2, radiusY * 2);

            g.setColor(Color.red);

            g.drawOval(cx, cy, 2, 2);

            labelId ++;
            }
        }
}
