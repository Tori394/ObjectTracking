package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Point;

public class CCL {

    private static List<Integer> neighbourEquivalenceTable = new ArrayList<>();

    public static List<DetectedObject> extract(RadarImage binaryMap, RadarImage rawMap) {
        int width = binaryMap.getWidth();
        int height = binaryMap.getHeight();
        int[][] labels = new int[width][height];

        neighbourEquivalenceTable.clear();
        neighbourEquivalenceTable.add(0);

        // ETAP ETYKIETOWANIA
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (binaryMap.getPixel(x, y) == 0) continue;

                List<Integer> neighbors = new ArrayList<>();
                if (x > 0 && labels[x - 1][y] > 0) neighbors.add(labels[x - 1][y]);
                if (y > 0) {
                    if (labels[x][y - 1] > 0) neighbors.add(labels[x][y - 1]);
                    if (x > 0 && labels[x - 1][y - 1] > 0) neighbors.add(labels[x - 1][y - 1]);
                    if (x < width - 1 && labels[x + 1][y - 1] > 0) neighbors.add(labels[x + 1][y - 1]);
                }

                if (neighbors.isEmpty()) {
                    int newLabel = neighbourEquivalenceTable.size();
                    neighbourEquivalenceTable.add(newLabel);
                    labels[x][y] = newLabel;
                } else {
                    int minLabel = Integer.MAX_VALUE;
                    for (int l : neighbors) if (l < minLabel) minLabel = l;
                    labels[x][y] = minLabel;
                    for (int l : neighbors) if (l != minLabel) union(minLabel, l);
                }
            }
        }

        // ROZWIĄZYWANIE KONFLIKTÓW
        int maxLabel = neighbourEquivalenceTable.size();
        int[] finalLabelMapping = new int[maxLabel];
        for (int i = 1; i < maxLabel; i++) finalLabelMapping[i] = find(i);

        Map<Integer, List<Point>> groupedPixels = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int lbl = labels[x][y];
                if (lbl > 0) {
                    int resolved = finalLabelMapping[lbl];
                    groupedPixels.computeIfAbsent(resolved, k -> new ArrayList<>()).add(new Point(x, y));
                }
            }
        }

        List<DetectedObject> rawResults = new ArrayList<>();

        // OBLICZENIA SRODKA
        for (List<Point> pixels : groupedPixels.values()) {
            if (pixels.size() < 20) continue; // Filtr szumu

            double totalMass = 0;       // Suma jasności
            double weightedSumX = 0;    // Suma X * jasność
            double weightedSumY = 0;    // Suma Y * jasność
            double sumSqX = 0, sumSqY = 0;

            //ŚRODEK CIĘŻKOŚCI
            for (Point p : pixels) {
                // Pobieramy jasność z surowego obrazu (0-255)
                int brightness = rawMap.getPixel(p.x, p.y);

                if (brightness < 1) brightness = 1;

                totalMass += brightness;
                weightedSumX += p.x * brightness;
                weightedSumY += p.y * brightness;
            }

            double meanX = weightedSumX / totalMass;
            double meanY = weightedSumY / totalMass;

            // Odchylenie standardowe
            for (Point p : pixels) {
                sumSqX += Math.pow(p.x - meanX, 2);
                sumSqY += Math.pow(p.y - meanY, 2);
            }
            double stdDevX = Math.sqrt(sumSqX / pixels.size());
            double stdDevY = Math.sqrt(sumSqY / pixels.size());

            rawResults.add(new DetectedObject(meanX, meanY, stdDevX, stdDevY, pixels));
        }

        // Jeśli jakieś bloby "wyleciały" z siebie (są bardzo blisko), sklejamy je z powrotem.
        return mergeCloseBlobs(rawResults);
    }

    // Sklejanie rozerwanych obiektów
    private static List<DetectedObject> mergeCloseBlobs(List<DetectedObject> blobs) {
        if (blobs.isEmpty()) return blobs;
        List<DetectedObject> merged = new ArrayList<>(blobs);
        boolean change = true;

        while (change) {
            change = false;
            for (int i = 0; i < merged.size(); i++) {
                for (int j = i + 1; j < merged.size(); j++) {
                    DetectedObject b1 = merged.get(i);
                    DetectedObject b2 = merged.get(j);

                    // Liczymy dystans między środkami
                    double dist = Math.sqrt(Math.pow(b1.getX() - b2.getX(), 2) + Math.pow(b1.getY() - b2.getY(), 2));

                    double threshold = Math.max(b1.stdDevX, b1.stdDevY) + Math.max(b2.stdDevX, b2.stdDevY) + 10;

                    if (dist < threshold) {
                        double totalPix = b1.getPixels().size() + b2.getPixels().size();
                        double newX = (b1.getX() * b1.getPixels().size() + b2.getX() * b2.getPixels().size()) / totalPix;
                        double newY = (b1.getY() * b1.getPixels().size() + b2.getY() * b2.getPixels().size()) / totalPix;

                        b1.setX(newX);
                        b1.setY(newY);


                        b1.stdDevX = Math.max(b1.stdDevX, b2.stdDevX);
                        b1.stdDevY = Math.max(b1.stdDevY, b2.stdDevY);

                        merged.remove(j);
                        change = true;
                        break;
                    }
                }
                if (change) break;
            }
        }
        return merged;
    }

    private static int find(int i) {
        if (neighbourEquivalenceTable.get(i) != i) {
            neighbourEquivalenceTable.set(i, find(neighbourEquivalenceTable.get(i)));
        }
        return neighbourEquivalenceTable.get(i);
    }

    private static void union(int i, int j) {
        int rootI = find(i);
        int rootJ = find(j);
        if (rootI != rootJ) {
            if (rootI < rootJ) neighbourEquivalenceTable.set(rootJ, rootI);
            else neighbourEquivalenceTable.set(rootI, rootJ);
        }
    }
}