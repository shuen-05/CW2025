package com.comp2042;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class HighScoreManager {
    
    private static final String HIGH_SCORE_FILE = "highscore.dat";
    private int highScore;
    
    public HighScoreManager() {
        loadHighScore();
    }
    
    public int getHighScore() {
        return highScore;
    }
    
    public void updateHighScore(int score) {
        if (score > highScore) {
            highScore = score;
            saveHighScore();
        }
    }
    
    private void loadHighScore() {
        try {
            Path path = Paths.get(HIGH_SCORE_FILE);
            if (Files.exists(path)) {
                try (DataInputStream dis = new DataInputStream(new FileInputStream(HIGH_SCORE_FILE))) {
                    highScore = dis.readInt();
                }
            } else {
                highScore = 0;
            }
        } catch (IOException e) {
            highScore = 0;
        }
    }
    
    private void saveHighScore() {
        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(HIGH_SCORE_FILE))) {
            dos.writeInt(highScore);
        } catch (IOException e) {
            // Handle silently - high score just won't persist
        }
    }
}
