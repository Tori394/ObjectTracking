package Model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Track {
    private int id;
    private double x, y;
    private double vx, vy;
    private int missingFrames = 0;
    private Color color;

    // Historia
    private List<DetectedObject> history = new ArrayList<>();

    public Track(DetectedObject initialBlob, int id) {
        this.id = id;
        this.x = initialBlob.getX();
        this.y = initialBlob.getY();

        int r = (int) Math.round(Math.random() * 255);
        int g = (int) Math.round(Math.random() * 255);
        int b = (int) Math.round(Math.random() * 255);

        this.color = new Color(r, g, b);

        this.vx = 0;
        this.vy = 0;
        this.history.add(initialBlob);
    }

    // Przewidywanie
    public void predict() {
        x += vx;
        y += vy;
    }

    // Aktualizacja
    public void update(DetectedObject blob) {
        this.vx = blob.getX() - this.x;
        this.vy = blob.getY() - this.y;
        this.x = blob.getX();
        this.y = blob.getY();
        this.missingFrames = 0;

        this.history.add(blob);
        if (history.size() > 80) history.remove(0);
    }

    public List<DetectedObject> getHistory() { return history; }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public int getId() {
        return id;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void addMissingFrames() {
        this.missingFrames++;
    }

    public int getMissingFrames() {
        return missingFrames;
    }

    public Color getColor() {
        return color;
    }

    public void setMissingFrames(int i) {
        this.missingFrames = i;
    }
}