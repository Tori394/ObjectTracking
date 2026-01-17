package Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TrackingSystem {
    private List<Track> tracks = new ArrayList<>();
    private int nextId = 1;

    // Struktury pomocnicze do drzewa
    private static class Hypothesis {
        List<Pair> assignments = new ArrayList<>();
        double totalScore = 0;
    }
    private static class Pair {
        Track track;
        DetectedObject blob;
        public Pair(Track t, DetectedObject b) { this.track = t; this.blob = b; }
    }

    // GŁÓWNA METODA AKTUALIZACJI
    public void update(List<DetectedObject> measurements) {
        // 1. Przewiduj (Wszystkie obiekty przesuwają się zgodnie z prędkością)
        for (Track t : tracks) t.predict();

        // 2. Drzewo asocjacji
        Hypothesis bestHypothesis = solveAssociationTree(0, tracks, new ArrayList<>(measurements));

        List<DetectedObject> assignedBlobs = new ArrayList<>();

        // 3. Zastosuj hipotezę
        if (bestHypothesis != null) {

            // Obsłuż te, które znalazły pary
            for (Pair p : bestHypothesis.assignments) {
                if (p.blob != null) {
                    p.track.update(p.blob);
                    measurements.remove(p.blob);
                    assignedBlobs.add(p.blob); // Zapamiętujemy, że blob zajęty
                }
            }

            // Obsłuż te, które NIE znalazły pary
            for (Pair p : bestHypothesis.assignments) {
                if (p.blob == null) {

                    // SPRAWDZAMY CZY ZŁĄCZENIE
                    boolean isOccluded = false;

                    // Sprawdzamy dystans do wszystkich blobów, które zostały już zajęte przez inne obiekty
                    for (DetectedObject busyBlob : assignedBlobs) {
                        double dist = Math.sqrt(Math.pow(p.track.getX() - busyBlob.getX(), 2) +
                                Math.pow(p.track.getY() - busyBlob.getY(), 2));

                        // Siedzi w środku innego bloba (złączyły się)
                        if (dist < 60.0) {
                            isOccluded = true;
                            break;
                        }
                    }

                    if (isOccluded) {
                        // JEST ZŁĄCZONY?
                        p.track.setMissingFrames(0);
                    } else {
                        // FAKTYCZNIE ZGUBIONY (Nie ma go ani samego, ani w grupie)
                        p.track.addMissingFrames();
                    }
                }
            }
        } else {
            // Awaria systemu - wszyscy zgubieni
            for(Track t : tracks) t.addMissingFrames();
        }

        // SPRZĄTANIE
        Iterator<Track> it = tracks.iterator();
        while (it.hasNext()) {
            Track t = it.next();

            // Jeśli obiekt zaginął
            if (t.getMissingFrames() > 30) {

                boolean isStable = t.getHistory().size() >= 50;

                if (isStable) {
                    saveTrackToFile(t);
                }

                it.remove();
            }
        }

        // Tworzenie nowych
        for (DetectedObject m : measurements) {
            tracks.add(new Track(m, nextId++));
        }
    }

    // ZAPIS RAPORTU
    private void saveTrackToFile(Track t) {
        List<DetectedObject> history = t.getHistory();

        // Punkt początkowy (kiedy obiekt pojawił się na radarze)
        DetectedObject start = history.get(0);

        // Punkt sprzed 5 klatek (gdy był jeszcze cały widoczny)
        int safeEndIndex = Math.max(0, history.size() - 5);
        DetectedObject end = history.get(safeEndIndex);

        // Obliczamy wektor przemieszczenia na podstawie historii
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();

        // Kąt z całego przelotu
        double angleRad = Math.atan2(dy, dx);
        double angleDeg = Math.toDegrees(angleRad);

        // Prędkość średnia (dystans / czas)
        double distance = Math.sqrt(dx*dx + dy*dy);
        double avgSpeed = distance / safeEndIndex; // piksele na klatkę
        String directionName = getDirectionName(angleDeg);

        String logEntry = String.format(
                "ZAREJESTROWANO OBIEKT: | Prędkość śr: %.2f px/klatka | Kierunek: %-12s (%.0f st.)",
                avgSpeed,
                directionName,
                angleDeg
        );

        System.out.println("Zapisano trajektorię ID: " + t.getId());
        System.out.println(logEntry);
    }

    // REKURENCJA
    private Hypothesis solveAssociationTree(int trackIndex, List<Track> currentTracks, List<DetectedObject> availableBlobs) {
        if (trackIndex >= currentTracks.size()) return new Hypothesis();

        Track currentTrack = currentTracks.get(trackIndex);
        Hypothesis bestLocalHypothesis = null;
        double maxProbability = -Double.MAX_VALUE;

        // Połącz z blobem
        for (int i = 0; i < availableBlobs.size(); i++) {
            DetectedObject blob = availableBlobs.get(i);

            double dist = Math.sqrt(Math.pow(currentTrack.getX() - blob.getX(), 2) + Math.pow(currentTrack.getY() - blob.getY(), 2));

            if (dist > 80) continue;

            double score = 1000.0 / (1.0 + dist);
            List<DetectedObject> remaining = new ArrayList<>(availableBlobs);
            remaining.remove(i);

            Hypothesis child = solveAssociationTree(trackIndex + 1, currentTracks, remaining);

            if (child != null) {
                double total = score + child.totalScore;
                if (total > maxProbability) {
                    maxProbability = total;
                    bestLocalHypothesis = child;
                    bestLocalHypothesis.assignments.add(0, new Pair(currentTrack, blob));
                    bestLocalHypothesis.totalScore = total;
                }
            }
        }

        // Miss
        Hypothesis missHypothesis = solveAssociationTree(trackIndex + 1, currentTracks, new ArrayList<>(availableBlobs));
        double totalMiss = 5.0 + (missHypothesis != null ? missHypothesis.totalScore : 0);

        if (bestLocalHypothesis == null || totalMiss > maxProbability) {
            bestLocalHypothesis = missHypothesis;
            if (bestLocalHypothesis == null) bestLocalHypothesis = new Hypothesis();
            bestLocalHypothesis.assignments.add(0, new Pair(currentTrack, null));
            bestLocalHypothesis.totalScore = totalMiss;
        }

        return bestLocalHypothesis;
    }

    public List<Track> getTracks() { return tracks; }

    private String getDirectionName(double angle) {
        // Zakres kątów: -180 do 180

        if (angle >= -22.5 && angle < 22.5)   return "PRAWO";
        if (angle >= 22.5 && angle < 67.5)    return "PRAWO-DÓŁ";
        if (angle >= 67.5 && angle < 112.5)   return "DÓŁ";
        if (angle >= 112.5 && angle < 157.5)  return "LEWO-DÓŁ";

        // Lewo jest na styku 180 i -180
        if (angle >= 157.5 || angle < -157.5) return "LEWO";

        if (angle >= -157.5 && angle < -112.5) return "LEWO-GÓRA";
        if (angle >= -112.5 && angle < -67.5)  return "GÓRA";
        if (angle >= -67.5 && angle < -22.5)   return "PRAWO-GÓRA";

        return "NIEZNANY";
    }
}