package Model;

import java.awt.Point;
import java.util.List;

public class DetectedObject {
        private final double x, y;
        public double stdDevX, stdDevY;
        private List<Point> pixels;


        public DetectedObject(double x, double y, double stdDevX, double stdDevY, int size, List<Point> pixels) {
            this.x = x;
            this.y = y;
            this.stdDevX = stdDevX;
            this.stdDevY = stdDevY;
            this.pixels = pixels;
        }

        public List<Point> getPixels() { return pixels; }
        public double getX() { return x; }
        public double getY() { return y; }
}
