<h1 align="center">📡 Radar Multi-Target Tracking System</h1>

<p align="center"> A Java Swing application that simulates raw radar signal processing, object detection, and trajectory tracking using advanced computer vision algorithms. </p>

<p align="center">
  <img src="https://img.shields.io/badge/Language-Java-blue.svg">
  <img src="https://img.shields.io/badge/GUI-Swing-blue.svg">
  <img src="https://img.shields.io/badge/Status-Finished-brightgreen">
</p>

---

## 📋 Project Description
The goal of this project is to simulate a complete radar signal processing pipeline. It starts from generating synthetic raw signal data (noise + targets), processes it to extract detections, and finally applies a tracking algorithm to maintain object identity over time. The system handles challenges such as signal noise, object merging, occlusion, and track initialization/termination.

## ⚙️ Features
* **Synthetic Data Generation:** Simulates a radar PPI (Plan Position Indicator) scope with Gaussian noise and moving targets.

* **Adaptive Thresholding:** Automatically determines the optimal signal-to-noise threshold using Otsu's Method.

* **Object Extraction:** Implements Connected Component Labeling (CCL) to group pixels into distinct objects (blobs).

* **Multi-Target Tracking:** Associates detections with existing tracks using a recursive hypothesis tree.

* **Visualization:** Layered rendering system showing raw signal, detected centroids, and historical trails.

## 🛰️ Signal Processing Pipeline
The application follows a strict DSP (Digital Signal Processing) pipeline:

1.  * Signal GenerationThe background is filled with random noise.
    * Targets are drawn using a Gaussian distribution formula to simulate the "glow" of a radar echo
    * Targets move with a constant velocity vector $[v_x, v_y]$.
2. Detection & Segmentation
   * Binarization: The raw grayscale image is converted to a binary map. The threshold $T$ is calculated dynamically for every frame using statistical variance maximization (Otsu).
   * Labeling (CCL): A two-pass algorithm scans the binary map to assign unique labels to connected pixels. It uses an Equivalence Table (Union-Find) to resolve complex shapes.
   * Feature Extraction: Calculates the Center of Mass $(\bar{x}, \bar{y})$ and standard deviation $(\sigma_x, \sigma_y)$ for each blob to filter out small noise artifacts.
3. Tracking Logic - The system uses a Track-While-Scan (TWS) approach:
   * Prediction: Estimates the next position based on previous velocity: $x_{t+1} = x_t + v_x$.
   * Gating: Only considers measurements within a visual radius (e.g., 60px) of the predicted track.
   * Data Association: Solves the assignment problem using a Recursive Hypothesis Tree. It finds the combination of Track-Measurement pairs that maximizes the total score.
   * Life Cycle:
     
     New Track: Created from unassigned measurements.
     
     Coasting: Tracks survive for several frames without measurements (handling occlusion).
     
     Termination: Tracks are deleted after leaving the area or missing too many frames.

  ## 💻 UI Preview
  <img width="1005" height="861" alt="image" src="https://github.com/user-attachments/assets/0c3aea3d-1bee-46e1-b18c-09771d83f3d2" />
  <img width="1000" height="859" alt="image" src="https://github.com/user-attachments/assets/1e654c02-350f-4d98-9615-2785193f999c" />
  <img width="1007" height="862" alt="image" src="https://github.com/user-attachments/assets/88e16e62-4893-4376-901f-7120d69d4bb0" />
  <img width="1002" height="860" alt="image" src="https://github.com/user-attachments/assets/60f072cd-b1df-4aee-aa2e-9c0abb1bf1eb" />
  <img width="894" height="689" alt="image" src="https://github.com/user-attachments/assets/0637b71a-0777-405d-a838-3d55ab051586" />


