package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class Creature {
    protected final Texture texture;
    protected int health;
    protected int maxHealth;

    public Creature(String image, int health) {
        this.texture = TextureCache.get(image);
        this.health = health;
        this.maxHealth = health;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isAlive() {
        return health > 0;
    }

    public int getHealth() {
        return health;
    }

    public int getMaxHealth() {
        return maxHealth;
    }

}
