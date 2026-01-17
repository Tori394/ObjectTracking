package View;

import Model.Track;
import Model.DetectedObject;
import java.awt.*;
import java.util.List;

public class TracksOverlay implements RadarOverlay {
    private List<Track> tracks;

    public TracksOverlay(List<Track> tracks) {
        this.tracks = tracks;
    }

    @Override
    public void paintOverlay(Graphics2D g) {
        if (tracks == null) return;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (Track t : tracks) {
            g.setColor(t.getColor());

            // Rysuj ogon
            List<DetectedObject> history = t.getHistory();
            for (int i = 0; i < history.size() - 1; i++) {
                DetectedObject p1 = history.get(i);
                DetectedObject p2 = history.get(i+1);
                g.drawLine((int)p1.getX(), (int)p1.getY(), (int)p2.getX(), (int)p2.getY());
            }

            // Rysuj kwadrat z ID
            /*int size = 20;
            g.setStroke(new BasicStroke(2));
            g.drawRect((int)t.getX() - 10, (int)t.getY() - 10, size, size);
            g.drawString("ID:" + t.getId(), (int)t.getX(), (int)t.getY() - 15);*/
        }
    }
}