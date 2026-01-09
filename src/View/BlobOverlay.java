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

            g.setColor(PALETA[labelId]);

            for (Point p : blob.getPixels()) {
                g.fillRect(p.x, p.y, 1, 1);
            }

            g.setColor(Color.red);

            int cx = (int) blob.getX();
            int cy = (int) blob.getY();

            g.drawOval(cx, cy, 2, 2);

            labelId ++;
            }
        }
}
