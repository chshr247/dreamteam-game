package com.spacefarm.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.math.MathUtils;

public class AudioManager {
    private Sound plantSound;
    private Sound harvestSound;
    private Sound waterSound;
    private Sound wheelSound;
    private Sound boughtSound;
    private Music droneSound;
    private Music soundtrack;
    private Music menuSoundtrack;

    // ── Volume settings (0.0 .. 1.0) ──────────────────────────────────────────
    private float musicVolume = 0.7f;   // фонова музика (гра + меню)
    private float sfxVolume   = 1.0f;   // фонові звуки / ефекти (включно з дроном)
    private boolean droneActive = false; // приглушуємо музику, поки шумить дрон

    public AudioManager() {
        Gdx.app.log("AudioManager", "Initializing sounds...");
        try {
            plantSound = Gdx.audio.newSound(Gdx.files.internal("sound/garden.mp3"));
            harvestSound = Gdx.audio.newSound(Gdx.files.internal("sound/pickup-plants.mp3"));
            waterSound = Gdx.audio.newSound(Gdx.files.internal("sound/watering-garden.mp3"));
            wheelSound = Gdx.audio.newSound(Gdx.files.internal("sound/wheel.mp3"));
            boughtSound = Gdx.audio.newSound(Gdx.files.internal("sound/bought.mp3"));

            droneSound = Gdx.audio.newMusic(Gdx.files.internal("sound/drone.wav"));
            droneSound.setLooping(true);
            droneSound.setVolume(sfxVolume);

            soundtrack = Gdx.audio.newMusic(Gdx.files.internal("sound/soundtrack.mp3"));
            soundtrack.setLooping(true);
            soundtrack.setVolume(musicVolume);

            menuSoundtrack = Gdx.audio.newMusic(Gdx.files.internal("sound/soundtrack-menu.mp3"));
            menuSoundtrack.setLooping(true);
            menuSoundtrack.setVolume(musicVolume);

            Gdx.app.log("AudioManager", "Sounds initialized successfully.");
        } catch (Exception e) {
            Gdx.app.error("AudioManager", "Error loading sounds: " + e.getMessage());
        }
    }

    // ── Volume API ────────────────────────────────────────────────────────────

    /** Гучність фонової музики (0..1). */
    public float getMusicVolume() { return musicVolume; }

    public void setMusicVolume(float volume) {
        musicVolume = MathUtils.clamp(volume, 0f, 1f);
        if (soundtrack != null) {
            soundtrack.setVolume(droneActive ? musicVolume * 0.4f : musicVolume);
        }
        if (menuSoundtrack != null) {
            menuSoundtrack.setVolume(musicVolume);
        }
    }

    /** Гучність фонових звуків / ефектів (0..1). */
    public float getSfxVolume() { return sfxVolume; }

    public void setSfxVolume(float volume) {
        sfxVolume = MathUtils.clamp(volume, 0f, 1f);
        if (droneSound != null && droneSound.isPlaying()) {
            droneSound.setVolume(sfxVolume);
        }
    }

    // ── Sound effects ─────────────────────────────────────────────────────────

    public void playPlantSound() {
        if (plantSound != null) {
            plantSound.play(sfxVolume);
        } else {
            Gdx.app.error("AudioManager", "plantSound is null!");
        }
    }

    public void playHarvestSound() {
        if (harvestSound != null) {
            harvestSound.play(sfxVolume);
        } else {
            Gdx.app.error("AudioManager", "harvestSound is null!");
        }
    }

    public void playWaterSound() {
        if (waterSound != null) {
            waterSound.play(sfxVolume);
        } else {
            Gdx.app.error("AudioManager", "waterSound is null!");
        }
    }

    public void playWheelSound() {
        if (wheelSound != null) {
            Gdx.app.log("AudioManager", "Playing wheel sound");
            wheelSound.play(sfxVolume);
        } else {
            Gdx.app.error("AudioManager", "wheelSound is null!");
        }
    }

    public void playDroneSound() {
        if (droneSound != null) {
            if (!droneSound.isPlaying()) {
                Gdx.app.log("AudioManager", "Starting drone sound loop...");
                droneActive = true;
                droneSound.play();
                droneSound.setVolume(sfxVolume);
                // Slightly lower soundtrack volume while drone is noisy
                if (soundtrack != null) soundtrack.setVolume(musicVolume * 0.4f);
            }
        } else {
            Gdx.app.error("AudioManager", "droneSound is null!");
        }
    }

    public void stopDroneSound() {
        if (droneSound != null && droneSound.isPlaying()) {
            Gdx.app.log("AudioManager", "Stopping drone sound.");
            droneSound.stop();
            droneActive = false;
            // Restore soundtrack volume
            if (soundtrack != null) soundtrack.setVolume(musicVolume);
        }
    }

    // ── Background music (gameplay) ───────────────────────────────────────────

    public void playMusic() {
        if (soundtrack != null) {
            soundtrack.setVolume(droneActive ? musicVolume * 0.4f : musicVolume);
            if (!soundtrack.isPlaying()) {
                Gdx.app.log("AudioManager", "Starting music playback");
                soundtrack.play();
            }
        } else {
            Gdx.app.error("AudioManager", "soundtrack is null!");
        }
    }

    public void pauseMusic() {
        if (soundtrack != null && soundtrack.isPlaying()) {
            soundtrack.pause();
        }
    }

    public void resumeMusic() {
        if (soundtrack != null && !soundtrack.isPlaying()) {
            soundtrack.setVolume(droneActive ? musicVolume * 0.4f : musicVolume);
            soundtrack.play();
        }
    }

    public void stopMusic() {
        if (soundtrack != null && soundtrack.isPlaying()) {
            soundtrack.stop();
        }
    }

    // ── Background music (menu) ───────────────────────────────────────────────

    public void playMenuMusic() {
        if (menuSoundtrack != null) {
            menuSoundtrack.setVolume(musicVolume);
            if (!menuSoundtrack.isPlaying()) {
                Gdx.app.log("AudioManager", "Starting menu music playback");
                menuSoundtrack.play();
            }
        } else {
            Gdx.app.error("AudioManager", "menuSoundtrack is null!");
        }
    }

    public void stopMenuMusic() {
        if (menuSoundtrack != null && menuSoundtrack.isPlaying()) {
            menuSoundtrack.stop();
        }
    }

    // ── Other SFX ─────────────────────────────────────────────────────────────

    public void playBoughtSound() {
        if (boughtSound != null) {
            boughtSound.play(sfxVolume);
        } else {
            Gdx.app.error("AudioManager", "boughtSound is null!");
        }
    }

    public void dispose() {
        if (plantSound != null) plantSound.dispose();
        if (harvestSound != null) harvestSound.dispose();
        if (waterSound != null) waterSound.dispose();
        if (wheelSound != null) wheelSound.dispose();
        if (boughtSound != null) boughtSound.dispose();
        if (droneSound != null) droneSound.dispose();
        if (soundtrack != null) soundtrack.dispose();
        if (menuSoundtrack != null) menuSoundtrack.dispose();
    }
}