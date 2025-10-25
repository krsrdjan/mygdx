package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;


// Tile represents a 4 x 4 squares in the game board
public class Tile {
    private final int x;
    private final int y;
    private final Texture[][] textures;

    public Tile(int x, int y, Texture[][] textures) {
        this.x = x;
        this.y = y;
        this.textures = textures;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public Texture getTexture(int x, int y) {
        return textures[x][y];
    }

}
