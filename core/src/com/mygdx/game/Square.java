package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

public class Square {
    private final Texture texture;
    private Creature creature;
    private boolean explored;

    public Square(Texture texture) {
        this.texture = texture;
        this.creature = null;
        this.explored = false;
    }

    public Texture getTexture() {
        return texture;
    }

    public Creature getCreature() {
        return creature;
    }

    public void setCreature(Creature creature) {
        this.creature = creature;
    }

    public boolean isExplored() {
        return explored;
    }

    public void setExplored(boolean explored) {
        this.explored = explored;
    }
}