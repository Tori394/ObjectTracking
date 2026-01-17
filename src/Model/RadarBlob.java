package Model;

public class RadarBlob {
    private double x;
    private double y;
    private final double xv;
    private final double yv;
    private double brightness;
    private double size;

    public RadarBlob(double x, double y, double xv, double yv, double size) {
        this.x = x;
        this.y = y;
        this.xv = xv;
        this.yv = yv;
        this.brightness = 200.0;
        this.size = size;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getBrightness() {
        return brightness;
    }

    public double getSize() {
        return size;
    }

    public void move() {
        this.x += this.xv;
        this.y += this.yv;
    }
}