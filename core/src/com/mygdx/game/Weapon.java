package com.mygdx.game;

import java.util.Random;

public abstract class Weapon {
    protected final float chanceToHit;
    protected final int damage;
    protected final Random random = new Random();

    public Weapon(float chanceToHit, int damage) {
        this.chanceToHit = chanceToHit;
        this.damage = damage;
    }

    public float getChanceToHit() {
        return chanceToHit;
    }

    public int getDamage() {
        return damage;
    }

    public boolean attemptHit() {
        return random.nextFloat() < chanceToHit;
    }

    public int attack() {
        if (attemptHit()) {
            return damage;
        }
        return 0; // Miss
    }
}

