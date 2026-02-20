package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.HashMap;
import java.util.Map;

public final class SoundCache {
    private SoundCache() {}

    private static final Map<String, Sound> cache = new HashMap<>();

    public static Sound get(String filename) {
        Sound sound = cache.get(filename);
        if (sound == null) {
            sound = Gdx.audio.newSound(Gdx.files.internal(filename));
            cache.put(filename, sound);
        }
        return sound;
    }

    public static void dispose() {
        for (Sound sound : cache.values()) {
            sound.dispose();
        }
        cache.clear();
    }
}
