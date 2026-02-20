package com.mygdx.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.HashMap;
import java.util.Map;

public final class TextureCache {
    private TextureCache() {}

    private static final Map<String, Texture> cache = new HashMap<>();

    public static Texture get(String filename) {
        Texture tex = cache.get(filename);
        if (tex == null) {
            tex = new Texture(filename);
            cache.put(filename, tex);
        }
        return tex;
    }

    public static Texture getOrCreateSolid(String key, float r, float g, float b, float a, int size) {
        Texture tex = cache.get(key);
        if (tex == null) {
            Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
            pixmap.setColor(r, g, b, a);
            pixmap.fill();
            tex = new Texture(pixmap);
            pixmap.dispose();
            cache.put(key, tex);
        }
        return tex;
    }

    public static void dispose() {
        for (Texture tex : cache.values()) {
            tex.dispose();
        }
        cache.clear();
    }
}
