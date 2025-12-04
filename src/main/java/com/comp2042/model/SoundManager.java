package com.comp2042.model;

import javafx.scene.media.AudioClip;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects using cached AudioClip objects for optimal performance.
 * All sounds are pre-loaded into memory to avoid disk I/O during gameplay.
 */
public class SoundManager {
    
    private final Map<String, AudioClip> soundCache = new HashMap<>();
    
    /**
     * Pre-loads all sound effects into memory.
     * Call this once during initialization.
     */
    public void initialize() {
        // Load all sound effects into cache
        loadSound("tetris-line-clear-sound.mp3", 0.7);
        loadSound("tetris-gb-19-rotate-piece.mp3", 0.6);
        loadSound("game-over-arcade-6435.mp3", 0.7);
        loadSound("fx-hrddp.mp3", 0.6);
        loadSound("tetris.mp3", 0.5);
        loadSound("tetris-success.mp3", 0.7);
    }
    
    /**
     * Loads a sound file into the cache.
     * 
     * @param filename The name of the sound file in resources
     * @param volume The volume level (0.0 to 1.0)
     */
    private void loadSound(String filename, double volume) {
        try {
            URL soundUrl = getClass().getClassLoader().getResource(filename);
            if (soundUrl != null) {
                String soundPath = soundUrl.toExternalForm();
                AudioClip audioClip = new AudioClip(soundPath);
                audioClip.setVolume(volume);
                soundCache.put(filename, audioClip);
            } else {
                System.err.println("Could not find " + filename + " in resources");
            }
        } catch (Exception e) {
            System.err.println("Error loading sound " + filename + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Plays a sound effect from the cache.
     * 
     * @param filename The name of the sound file to play
     */
    public void play(String filename) {
        AudioClip clip = soundCache.get(filename);
        if (clip != null) {
            clip.play();
        } else {
            System.err.println("Sound not found in cache: " + filename);
        }
    }
    
    /**
     * Stops all currently playing sounds.
     */
    public void stopAll() {
        for (AudioClip clip : soundCache.values()) {
            clip.stop();
        }
    }
    
    /**
     * Disposes all cached sounds and clears the cache.
     */
    public void dispose() {
        stopAll();
        soundCache.clear();
    }
}

