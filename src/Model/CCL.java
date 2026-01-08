package Model;

import static java.lang.Math.round;

public class CCL {

    public static int OtsuTreshold(RadarNoise map) {
        int[] histogram = new int[256];
        int width = map.getWidth();
        int height = map.getHeight();
        int pixels = width * height;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                int val = map.getPixel(i, j);
                histogram[val]++;
            }
        }

        double totalMean = 0;
        for (int i = 0; i < 256; i++) {
            totalMean += i * (double) histogram[i] / pixels;
        }

        int bestThreshold = 0;
        double maxVariance = 0;
        double w1 = 0;
        double sum1 = 0;
        double u1 = 0;
        double w2 = 0;
        double u2 = 0;

        for (int t = 0; t < 256; t++) {
            double prob = (double)histogram[t] / pixels;

            w1 += prob;
            w2 = 1.0 - w1;

            if (w1 == 0 || w2 == 0) continue;


            sum1 += t * prob;
            u1 = sum1 / w1;
            u2 = (totalMean - sum1) / w2;

            // Wariancja Międzyklasowa
            double variance = w1 * w2 * Math.pow(u1 - u2, 2);

            if (variance > maxVariance) {
                maxVariance = variance;
                bestThreshold = t;
            }

        }
        return (int) Math.round((bestThreshold) * 2.1);
    }

    public static RadarNoise applyThreshold(RadarNoise inputMap, int threshold) {
        int width = inputMap.getWidth();
        int height = inputMap.getHeight();
        RadarNoise binaryMap = new RadarNoise(width, height);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int oldVal = inputMap.getPixel(x, y);

                if (oldVal > threshold) {
                    binaryMap.setPixel(x, y, 255); // Bialy blob
                } else {
                    binaryMap.setPixel(x, y, 0);   // Czarne tlo
                }
            }
        }
        return binaryMap;
    }

}
