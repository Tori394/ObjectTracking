package Model;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Collections;

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
        // 1. Przewiduj
        for (Track t : tracks) t.predict();

        // 2. Drzewo asocjacji (Znajdź najlepszy plan)
        Hypothesis bestHypothesis = solveAssociationTree(0, tracks, new ArrayList<>(measurements));

        List<DetectedObject> assignedBlobs = new ArrayList<>();

        // 3. Zastosuj hipotezę
        if (bestHypothesis != null) {

            for (Pair p : bestHypothesis.assignments) {
                if (p.blob != null) {

                    // Pobieramy ostatnią widoczną pozycję z historii
                    double visualDist = getVisualDist(p);

                    // LIMIT
                    if (visualDist > 150) {
                        p.track.addMissingFrames(); // Odrzuć (Miss)
                    }
                    else {
                        // Jest OK - aktualizujemy
                        p.track.update(p.blob);
                        measurements.remove(p.blob);
                        assignedBlobs.add(p.blob);
                    }

                } else {
                    // Blob jest null, więc od razu sprawdzamy okluzję
                    checkOcclusionOrMiss(p.track, assignedBlobs);
                }
            }
        } else {
            for(Track t : tracks) t.addMissingFrames();
        }

        // SPRZĄTANIE
        Iterator<Track> it = tracks.iterator();
        while (it.hasNext()) {
            Track t = it.next();

            if (t.getMissingFrames() > 30) {

                boolean isStable = t.getHistory().size() >= 20;

                // Bierzemy ostatni punkt, w którym radar faktycznie widział obiekt.
                double realX = 0;
                double realY = 0;

                if (!t.getHistory().isEmpty()) {
                    DetectedObject lastSeen = t.getHistory().get(t.getHistory().size() - 1);
                    realX = lastSeen.getX();
                    realY = lastSeen.getY();
                } else {
                    realX = t.getX();
                    realY = t.getY();
                }

                int width = 800;
                int height = 600;
                int margin = 80;

                boolean leftEdge   = realX < margin;
                boolean rightEdge  = realX > (width - margin);
                boolean topEdge    = realY < margin;
                boolean bottomEdge = realY > (height - margin);

                boolean atBorder = leftEdge || rightEdge || topEdge || bottomEdge;

                if (isStable && atBorder) {
                    saveTrackToFile(t);
                } else {
                    // if(isStable) System.out.println("Odrzucono środek: " + (int)realX + ", " + (int)realY);
                }

                it.remove();
            }
        }

        // Tworzenie nowych
        for (DetectedObject m : measurements) {
            tracks.add(new Track(m, nextId++));
        }
    }

    private static double getVisualDist(Pair p) {
        DetectedObject lastVisible = null;
        if (!p.track.getHistory().isEmpty()) {
            lastVisible = p.track.getHistory().get(p.track.getHistory().size() - 1);
        }

        // 2. Liczymy "skok wizualny" (od ostatniej kropki na ekranie do nowej)
        double visualDist = 0;
        if (lastVisible != null) {
            visualDist = Math.sqrt(Math.pow(lastVisible.getX() - p.blob.getX(), 2) +
                    Math.pow(lastVisible.getY() - p.blob.getY(), 2));
        } else {
            // Jeśli brak historii (nowy obiekt), bierzemy dystans matematyczny
            visualDist = Math.sqrt(Math.pow(p.track.getX() - p.blob.getX(), 2) +
                    Math.pow(p.track.getY() - p.blob.getY(), 2));
        }
        return visualDist;
    }

    private void checkOcclusionOrMiss(Track t, List<DetectedObject> assignedBlobs) {
        boolean isOccluded = false;
        for (DetectedObject busyBlob : assignedBlobs) {
            double dist = Math.sqrt(Math.pow(t.getX() - busyBlob.getX(), 2) +
                    Math.pow(t.getY() - busyBlob.getY(), 2));
            if (dist < 80.0) {
                isOccluded = true;
                break;
            }
        }
        if (isOccluded) {
            t.setMissingFrames(0);
        } else {
            t.addMissingFrames();
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

        //System.out.println("Zapisano trajektorię ID: " + t.getId());
        System.out.println(logEntry);
    }

    // REKURENCJA
    // ZOPTYMALIZOWANA REKURENCJA (DRZEWO ASOCJACJI Z AGRESYWNYM PRZYCINANIEM)
    private Hypothesis solveAssociationTree(int trackIndex, List<Track> currentTracks, List<DetectedObject> availableBlobs) {
        // Warunek stopu
        if (trackIndex >= currentTracks.size()) return new Hypothesis();

        Track currentTrack = currentTracks.get(trackIndex);
        Hypothesis bestLocalHypothesis = null;
        double maxProbability = -Double.MAX_VALUE;

        // ZNAJDŹ KANDYDATÓW
        List<Candidate> candidates = new ArrayList<>();

        for (int i = 0; i < availableBlobs.size(); i++) {
            DetectedObject blob = availableBlobs.get(i);
            double dist = Math.sqrt(Math.pow(currentTrack.getX() - blob.getX(), 2) + Math.pow(currentTrack.getY() - blob.getY(), 2));

            if (dist > 60) continue;

            double score = 1000.0 / (1.0 + dist);
            candidates.add(new Candidate(blob, i, score));
        }

        Collections.sort(candidates);

        // OPTYMALIZACJA
        int branchLimit = 3; // Domyślnie sprawdzamy 3 opcje
        boolean forceMatch = false; //Czy wymuszamy dopasowanie

        if (!candidates.isEmpty()) {
            double bestScore = candidates.get(0).score;
            // Jeśli mamy kandydata bliżej niż 30px
            if (bestScore > 30.0) {
                branchLimit = 1;
                forceMatch = true;
            }
        }

        int count = 0;

        // REKURENCJA PO KANDYDATACH
        for (Candidate cand : candidates) {
            if (count >= branchLimit) break;
            count++;

            List<DetectedObject> remaining = new ArrayList<>(availableBlobs);
            remaining.remove(cand.blob);

            Hypothesis child = solveAssociationTree(trackIndex + 1, currentTracks, remaining);

            if (child != null) {
                double total = cand.score + child.totalScore;
                if (total > maxProbability) {
                    maxProbability = total;
                    bestLocalHypothesis = child;
                    bestLocalHypothesis.assignments.add(0, new Pair(currentTrack, cand.blob));
                    bestLocalHypothesis.totalScore = total;
                }
            }
        }

        // REKURENCJA (OPCJA MISS - ZGUBIENIE)
        if (!forceMatch) {
            Hypothesis missHypothesis = solveAssociationTree(trackIndex + 1, currentTracks, new ArrayList<>(availableBlobs));
            double totalMiss = 5.0 + (missHypothesis != null ? missHypothesis.totalScore : 0);

            if (bestLocalHypothesis == null || totalMiss > maxProbability) {
                bestLocalHypothesis = missHypothesis;
                if (bestLocalHypothesis == null) bestLocalHypothesis = new Hypothesis();
                bestLocalHypothesis.assignments.add(0, new Pair(currentTrack, null));
                bestLocalHypothesis.totalScore = totalMiss;
            }
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

    private static class Candidate implements Comparable<Candidate> {
        DetectedObject blob;
        int originalIndex;
        double score;

        public Candidate(DetectedObject blob, int idx, double score) {
            this.blob = blob;
            this.originalIndex = idx;
            this.score = score;
        }

        @Override
        public int compareTo(Candidate o) {
            // Sortujemy malejąco po score
            return Double.compare(o.score, this.score);
        }
    }
}
