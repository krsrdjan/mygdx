package com.mygdx.game;

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

    public void moveUp() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y < board.getBoard().length - 1 && board.getBoard()[pos.x][pos.y+1] == null) {
                setPosition(new Position(pos.x, pos.y+1));
                speed--;
            }
        }
    }

    public void moveRight() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x < board.getBoard().length - 1 && board.getBoard()[pos.x+1][pos.y] == null) {
                setPosition(new Position(pos.x+1, pos.y));
                speed--;
            }
        }
    }

    public void moveDown() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y > 0 && board.getBoard()[pos.x][pos.y-1] == null) {
                setPosition(new Position(pos.x, pos.y-1));
                speed--;
            }
        }
    }

    public void moveLeft() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x > 0 && board.getBoard()[pos.x-1][pos.y] == null) {
                setPosition(new Position(pos.x-1, pos.y));
                speed--;
            }
        }
    }
}
