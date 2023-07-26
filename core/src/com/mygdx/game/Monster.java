package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;

import java.util.Random;

public class Monster extends Creature {

    private Sound deathSound;
    private Position position;
    private GameBoard board;
    private int MAX_SPEED = 3;
    private int speed = MAX_SPEED;
    private Sound weaponHit;

    public Monster(String image, int health, GameBoard board) {
        super(image, health);
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        weaponHit = Gdx.audio.newSound(Gdx.files.internal("sword.wav"));

        this.board = board;
    }

    public void takeDamage(int damage) {
        System.out.println("Damage taken: " + damage);
        health = health - damage;
        if(health <= 0) {
            deathSound.play();
            board.getBoard()[position.x][position.y] = null;
        }
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

    public void moveToHeroAndAttack(final Hero hero) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                while(speed > 0) {
                    Position heroPos = hero.getPosition();
                    if (Position.isNear(heroPos, position)) {
                        System.out.println("Already near hero");
                        speed--;
                        continue;
                    }

                    //move to hero position
                    if (heroPos.x - position.x < 0) {
                        setPosition(new Position(position.x - 1, position.y));
                    } else if (heroPos.x - position.x > 0) {
                        setPosition(new Position(position.x + 1, position.y));
                    } else if (heroPos.y - position.y < 0) {
                        setPosition(new Position(position.x, position.y - 1));
                    } else if (heroPos.y - position.y > 0) {
                        setPosition(new Position(position.x, position.y + 1));
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    speed--;
                }

                attackHero(hero);

                speed = MAX_SPEED;
            }
        }).start();

    }

    public void attackHero(Hero hero) {
        Position heroPos = hero.getPosition();
        if(Position.isNear(heroPos, position)) {
            weaponHit.play();
            hero.takeDamage(attack());
        }
    }

    public int attack() {
        return new Random().nextInt(3);
    }
}
