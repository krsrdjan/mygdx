package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class Creature {
    protected final Texture texture;
    protected int health;
    protected int maxHealth;
    protected int armorClass;

    public Creature(String image, int health) {
        this(image, health, 12);
    }

    public Creature(String image, int health, int armorClass) {
        this.texture = TextureCache.get(image);
        this.health = health;
        this.maxHealth = health;
        this.armorClass = armorClass;
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

    public int getArmorClass() {
        return armorClass;
    }

    public void setArmorClass(int armorClass) {
        this.armorClass = armorClass;
    }
}
