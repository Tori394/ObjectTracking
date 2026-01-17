package Model;

import java.awt.*;
import java.util.List;

public class DetectedObject {
        private double x;
        private double y;
        public double stdDevX, stdDevY;
        private List<Point> pixels;


        public DetectedObject(double x, double y, double stdDevX, double stdDevY, List<Point> pixels) {
            this.x = x;
            this.y = y;
            this.stdDevX = stdDevX;
            this.stdDevY = stdDevY;
            this.pixels = pixels;
        }

        public List<Point> getPixels() { return pixels; }
        public double getX() { return x; }
        public double getY() { return y; }

        public void setX (double x) { this.x = x; }
        public void setY (double y) { this.y = y; }
}
