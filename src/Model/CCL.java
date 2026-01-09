package Model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.awt.Point;

public class CCL{

    // Struktura przechowywania relacji
    private static List<Integer> neighbourEquivalenceTable = new ArrayList<>();

    public static List<DetectedObject> extract(RadarImage binaryMap) {
        int width = binaryMap.getWidth();
        int height = binaryMap.getHeight();
        int[][] labels = new int[width][height]; // Mapa etykiet

        // Resetujemy tabelę równoważności (0 to tło)
        neighbourEquivalenceTable.clear();
        neighbourEquivalenceTable.add(0);

        // =========================================================================
        // PIERWSZE PRZEJŚCIE
        // Nadawanie etykiet tymczasowych i rejestrowanie konfliktów
        // =========================================================================
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {

                if (binaryMap.getPixel(x, y) == 0) continue; // jak tło to pomiń

                List<Integer> neighbors = new ArrayList<>(); // jak kawałek bloba to sprawdź sąsiadów

                // Sprawdzamy sąsiadów już odwiedzonych więc z góry i z lewej
                // Dla 8-sąsiedztwa są to:
                if (x > 0 && labels[x - 1][y] > 0) neighbors.add(labels[x - 1][y]); // Lewy
                if (y > 0) {
                    if (labels[x][y - 1] > 0) neighbors.add(labels[x][y - 1]);     // Górny
                    if (x > 0 && labels[x - 1][y - 1] > 0) neighbors.add(labels[x - 1][y - 1]); // Górny-lewy
                    if (x < width - 1 && labels[x + 1][y - 1] > 0) neighbors.add(labels[x + 1][y - 1]); // Górny-prawy
                }

                if (neighbors.isEmpty()) {
                    // Brak sąsiadów - tworzymy nową etykietę
                    int newLabel = neighbourEquivalenceTable.size();
                    neighbourEquivalenceTable.add(newLabel);
                    labels[x][y] = newLabel;
                } else {
                    // Sąsiedzi istnieją - Bierzemy najmniejszą etykiete spośród nich (najstarszy)
                    int minLabel = Integer.MAX_VALUE;
                    for (int l : neighbors) {
                        if (l < minLabel) minLabel = l;
                    }
                    labels[x][y] = minLabel;

                    // ZAPISYWANIE KONFLIKTÓW
                    // Jeśli sąsiedzi mają różne etykiety, łączymy je w tabeli
                    for (int l : neighbors) {
                        if (l != minLabel) {
                            union(minLabel, l);
                        }
                    }
                }
            }
        }

        // =========================================================================
        // ROZWIĄZYWANIE KONFLIKTÓW (Label Equivalence Resolution)
        // StaraEtykieta -> OstatecznaEtykieta
        // =========================================================================
        int maxLabel = neighbourEquivalenceTable.size();
        int[] finalLabelMapping = new int[maxLabel];

        for (int i = 1; i < maxLabel; i++) {
            finalLabelMapping[i] = find(i);
        }

        // =========================================================================
        // DRUGIE PRZEJŚCIE
        // Aktualizacja etykiet na obrazie
        // =========================================================================

        // Grupujemy pixele w bloby/obiekty
        Map<Integer, List<Point>> groupedPixels = new HashMap<>();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int currentLabel = labels[x][y];
                if (currentLabel > 0) {

                    int resolvedLabel = finalLabelMapping[currentLabel]; // Podmieniamy etykietę
                    labels[x][y] = resolvedLabel;

                    groupedPixels
                            .computeIfAbsent(resolvedLabel, k -> new ArrayList<>())
                            .add(new Point(x, y));
                }
            }
        }

        // =========================================================================
        // OBLICZENIA STATYSTYCZNE
        // Średnia i Odchylenie Standardowe
        // =========================================================================
        List<DetectedObject> results = new ArrayList<>();

        for (List<Point> pixels : groupedPixels.values()) {
            if (pixels.size() < 20) continue;

            // Średnia arytmetyczna (mu)
            double sumX = 0, sumY = 0;
            int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
            int minY = Integer.MAX_VALUE, maxY = Integer.MIN_VALUE;

            for (Point p : pixels) {
                sumX += p.x;
                sumY += p.y;
            }

            double meanX = sumX / pixels.size();
            double meanY = sumY / pixels.size();

            // B. Odchylenie standardowe (sigma)
            double sumSqX = 0, sumSqY = 0;
            for (Point p : pixels) {
                sumSqX += Math.pow(p.x - meanX, 2);
                sumSqY += Math.pow(p.y - meanY, 2);
            }
            double stdDevX = Math.sqrt(sumSqX / pixels.size());
            double stdDevY = Math.sqrt(sumSqY / pixels.size());

            results.add(new DetectedObject(meanX, meanY, stdDevX, stdDevY, pixels.size(), pixels));
        }

        return results;
    }

    // Znajdź korzeń
    private static int find(int i) {
        if (neighbourEquivalenceTable.get(i) != i) { // Jeśli element nie wskazuje na samego siebie, to znaczy, że nie jest korzeniem
            neighbourEquivalenceTable.set(i, find(neighbourEquivalenceTable.get(i))); // Rekurencyjne szukanie korzenia + przypisanie go bezpośrednio jako nowego rodzica 'i'.
        }
        return neighbourEquivalenceTable.get(i);
    }

    // Połącz dwa zbiory (zapisz konflikt)
    private static void union(int i, int j) {
        int rootI = find(i);
        int rootJ = find(j);
        if (rootI != rootJ) {
            // Mniejszy staje się rodzicem większego
            if (rootI < rootJ) neighbourEquivalenceTable.set(rootJ, rootI);
            else neighbourEquivalenceTable.set(rootI, rootJ);
        }
    }

}