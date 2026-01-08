package Controller;

import Model.RadarNoise;
import java.util.Random;

public class DataGenerator {
    private final Random random = new Random();
    private final int width;
    private final int height;

    public DataGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public RadarNoise generateNoise() {
        RadarNoise map = new RadarNoise(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int noise = random.nextInt(256);
                map.setPixel(x, y, noise);
            }
        }
        return map;
    }
}