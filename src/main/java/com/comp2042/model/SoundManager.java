package com.comp2042.model;

import javafx.scene.media.AudioClip;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages sound effects using cached AudioClip objects for optimal performance.
 * All sounds are pre-loaded into memory to avoid disk I/O during gameplay.
 * Singleton pattern to allow access from multiple controllers.
 */
public class SoundManager {
    
    private static SoundManager instance;
    
    private final Map<String, AudioClip> soundCache = new HashMap<>();
    private final Map<String, Double> baseVolumes = new HashMap<>(); // Store base volumes for each sound
    
    private double musicVolume = 0.3; // Default to 30% (0.3) to match original behavior
    private double sfxVolume = 1.0;
    
    private MediaPlayer backgroundMusicPlayer;
    
    /**
     * Private constructor for singleton pattern.
     */
    private SoundManager() {
    }
    
    /**
     * Gets the singleton instance of SoundManager.
     * 
     * @return The SoundManager instance
     */
    public static SoundManager getInstance() {
        if (instance == null) {
            instance = new SoundManager();
        }
        return instance;
    }
    
    /**
     * Pre-loads all sound effects into memory.
     * Call this once during initialization.
     * Safe to call multiple times - will only load if cache is empty (idempotent).
     */
    public void initialize() {
        // Only initialize if cache is empty (idempotent)
        if (soundCache.isEmpty()) {
            // Load all sound effects into cache
            loadSound("tetris-line-clear-sound.mp3", 0.7);
            loadSound("tetris-gb-19-rotate-piece.mp3", 0.6);
            loadSound("game-over-arcade-6435.mp3", 0.7);
            loadSound("fx-hrddp.mp3", 0.6);
            loadSound("tetris.mp3", 0.5);
            loadSound("tetris-success.mp3", 0.7);
        }
    }
    
    /**
     * Sets the background music MediaPlayer reference.
     * 
     * @param mediaPlayer The MediaPlayer instance for background music
     */
    public void setBackgroundMusicPlayer(MediaPlayer mediaPlayer) {
        this.backgroundMusicPlayer = mediaPlayer;
        // Apply current music volume to the new player
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(musicVolume);
        }
    }
    
    /**
     * Sets the music volume and updates the background music player.
     * 
     * @param volume Volume level (0.0 to 1.0)
     */
    public void setMusicVolume(double volume) {
        this.musicVolume = Math.max(0.0, Math.min(1.0, volume)); // Clamp to 0.0-1.0
        if (backgroundMusicPlayer != null) {
            backgroundMusicPlayer.setVolume(musicVolume);
        }
    }
    
    /**
     * Gets the current music volume.
     * 
     * @return Current music volume (0.0 to 1.0)
     */
    public double getMusicVolume() {
        return musicVolume;
    }
    
    /**
     * Sets the SFX volume and updates all cached AudioClips.
     * 
     * @param volume Volume level (0.0 to 1.0)
     */
    public void setSfxVolume(double volume) {
        this.sfxVolume = Math.max(0.0, Math.min(1.0, volume)); // Clamp to 0.0-1.0
        // Update volume for all cached AudioClips: baseVolume * sfxVolume
        for (Map.Entry<String, AudioClip> entry : soundCache.entrySet()) {
            String filename = entry.getKey();
            AudioClip clip = entry.getValue();
            Double baseVolume = baseVolumes.get(filename);
            if (baseVolume != null) {
                clip.setVolume(baseVolume * sfxVolume);
            }
        }
    }
    
    /**
     * Gets the current SFX volume.
     * 
     * @return Current SFX volume (0.0 to 1.0)
     */
    public double getSfxVolume() {
        return sfxVolume;
    }
    
    /**
     * Loads a sound file into the cache.
     * 
     * @param filename The name of the sound file in resources
     * @param baseVolume The base volume level (0.0 to 1.0) - will be multiplied by sfxVolume
     */
    private void loadSound(String filename, double baseVolume) {
        try {
            URL soundUrl = getClass().getClassLoader().getResource(filename);
            if (soundUrl != null) {
                String soundPath = soundUrl.toExternalForm();
                AudioClip audioClip = new AudioClip(soundPath);
                // Store base volume
                baseVolumes.put(filename, baseVolume);
                // Set volume as base volume * current sfx volume
                audioClip.setVolume(baseVolume * sfxVolume);
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
     * Plays a sound effect from the cache at the current SFX volume.
     * The clip's volume is already set correctly (baseVolume * sfxVolume).
     * 
     * @param filename The name of the sound file to play
     */
    public void play(String filename) {
        AudioClip clip = soundCache.get(filename);
        if (clip != null) {
            // Clip volume is already set correctly, just play it
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

