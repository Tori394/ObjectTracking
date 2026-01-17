package View;

import Model.DetectedObject;
import java.awt.*;
import java.util.List;

public class BlobOverlay implements RadarOverlay {
    private final Color c;
    private final List<DetectedObject> blobs;

    public BlobOverlay(List<DetectedObject> blobs) {
        this.blobs = blobs;
        this.c = Color.green;
    }

    @Override
    public void paintOverlay(Graphics2D g) {
        if (blobs == null || blobs.isEmpty()) return;

        for (DetectedObject blob : blobs) {

            // Środek ciężkości
            int cx = (int) blob.getX();
            int cy = (int) blob.getY();

            int radiusX = (int) (blob.stdDevX * 3.0);
            int radiusY = (int) (blob.stdDevY * 3.0);

            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 170));
            g.fillOval(cx - radiusX, cy - radiusY, radiusX * 2, radiusY * 2);

            g.setColor(Color.red);

            g.drawOval(cx, cy, 3, 3);

            }
        }
}
