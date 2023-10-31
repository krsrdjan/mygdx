package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Random;

public class Hero extends Creature {

    private Sound weaponHit;
    private int MAX_SPEED = 100;
    private int speed = MAX_SPEED;
    private int attack = 1;
    private int damage = 10;
    private int sight = 1;
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
            board.getTextures()[position.x][position.y] = null;
        }

        this.board.getTextures()[newPosition.x][newPosition.y] = getTexture();
        this.position = newPosition;

        exploreAroundHero(newPosition);

        this.board.activateNearMonsters(newPosition);
    }

    private void exploreAroundHero(Position newPosition) {
        this.board.explore(newPosition.x, newPosition.y);
        for(int i = newPosition.x - sight; i <= newPosition.x + sight; i++) {
            for(int j = newPosition.y - sight; j <= newPosition.y + sight; j++) {
                this.board.explore(i, j);
            }
        }
    }

    public Position getPosition() {
        return position;
    }

    public int attack() {
        weaponHit.play();
        return new Random().nextInt(damage);
    }

    public void startTurn() {
        speed = MAX_SPEED;
        attack = 1;
    }

    public void endTurn() {
        speed = 0;
        attack = 0;
    }

    public void takeDamage(int damage) {
        System.out.println("Damage taken: " + damage);
        health = health - damage;
        if(health <= 0) {
            deathSound.play();
            board.getTextures()[position.x][position.y] = null;
        }
    }

    public void moveUp() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y < board.getTextures().length - 1 && board.getTextures()[pos.x][pos.y+1] == null) {
                setPosition(new Position(pos.x, pos.y+1));
                speed--;
            }
        }
    }

    public void moveRight() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x < board.getTextures().length - 1 && board.getTextures()[pos.x+1][pos.y] == null) {
                setPosition(new Position(pos.x+1, pos.y));
                speed--;
            }
        }
    }

    public void moveDown() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y > 0 && board.getTextures()[pos.x][pos.y-1] == null) {
                setPosition(new Position(pos.x, pos.y-1));
                speed--;
            }
        }
    }

    public void moveLeft() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x > 0 && board.getTextures()[pos.x-1][pos.y] == null) {
                setPosition(new Position(pos.x-1, pos.y));
                speed--;
            }
        }
    }

    public void attackMonster(Monster monster) {
        if(attack > 0) {
            Position monsterPos = monster.getPosition();
            if(Position.isNear(getPosition(), monsterPos)) {
                monster.takeDamage(attack());
                attack--;
            }
        }
    }

}
