package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.Random;

public class Creature {
    protected final Texture texture;
    protected int health;

    public Creature(String image, int health) {
        this.texture = new Texture(image);
        this.health = health;
    }

    public Texture getTexture() {
        return texture;
    }

    public boolean isAlive() {
        return health > 0;
    }

}
