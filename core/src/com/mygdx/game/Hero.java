package com.mygdx.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Random;

public class Hero extends Creature {

    private Sound weaponHit;
    private int speed = 5;
    private Position position;
    private GameBoard board;
    private Sound deathSound;

    public Hero(String image, int health, GameBoard board) {
        super(image, health);
        weaponHit = Gdx.audio.newSound(Gdx.files.internal("sword.wav"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        this.board = board;
    }

    public void setPosition(Position newPosition) {
        if(position != null) {
            board.getBoard()[position.x][position.y] = null;
        }

        this.board.getBoard()[newPosition.x][newPosition.y] = getTexture();
        this.position = newPosition;
    }

    public Position getPosition() {
        return position;
    }

    public int attack() {
        weaponHit.play();
        return new Random().nextInt(5);
    }

    public void resetSpeed() {
        speed = 5;
    }

    public void takeDamage(int damage) {
        System.out.println("Damage taken: " + damage);
        health = health - damage;
        if(health <= 0) {
            deathSound.play();
            board.getBoard()[position.x][position.y] = null;
        }
    }
}
