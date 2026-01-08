package Controller;

import Model.RadarNoise;
import Model.RadarBlob;

import java.util.List;
import java.util.Random;

public class DataGenerator {
    private final Random random = new Random();
    private final int width;
    private final int height;

    public DataGenerator(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public RadarNoise generateRadar(List<RadarBlob> blobs) {
        RadarNoise map = new RadarNoise(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int noise = random.nextInt(256);
                map.setPixel(x, y, noise);
            }
        }

        for (RadarBlob obj : blobs) {
            drawBlob(map, obj);
        }

        return map;
    }

    private void drawBlob(RadarNoise map, RadarBlob obj) {
        int range = (int) (obj.size * 3);

        int startX = Math.max(0, (int) (obj.x - range));
        int endX = Math.min(width - 1, (int) (obj.x + range));
        int startY = Math.max(0, (int) (obj.y - range));
        int endY = Math.min(height - 1, (int) (obj.y + range));

        for (int x = startX; x <= endX; x++) {
            for (int y = startY; y <= endY; y++) {

                // Liczymy odległość punktu od środka obiektu
                double distanceSq = Math.pow(x - obj.x, 2) + Math.pow(y - obj.y, 2);

                // Wzór na krzywą Gaussa
                double exponent = -distanceSq / (2 * Math.pow(obj.size, 2));
                double signal = obj.brightness * Math.exp(exponent);

                // Dodajemy bloba do szumu
                int oldPixel = map.getPixel(x, y);
                map.setPixel(x, y, oldPixel + (int) signal);
            }
        }
    }
}
