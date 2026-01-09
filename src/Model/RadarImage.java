package Model;

public class RadarImage {
    private final int width;
    private final int height;
    private final int[][] pixels;

    public RadarImage(int width, int height) {
        this.width = width;
        this.height = height;
        this.pixels = new int[width][height];
    }

    public void setPixel(int x, int y, int value) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            pixels[x][y] = Math.min(255, Math.max(0, value));
        }
    }

    public int getPixel(int x, int y) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            return pixels[x][y];
        }
        return 0;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
