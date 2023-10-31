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
    private int damage = 2;
    private Sound weaponHit;
    private boolean active = false;

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
            board.getTextures()[position.x][position.y] = null;
            board.removeMonster(this);
        }
    }

    public void setPosition(Position newPosition) {
        if(position != null) {
            board.getTextures()[position.x][position.y] = null;
        }

        this.board.getTextures()[newPosition.x][newPosition.y] = getTexture();
        this.position = newPosition;
    }

    public Position getPosition() {
        return position;
    }

    public void startTurn() {
        if(health <= 0) {
            board.endMonsterTurn();
            return;
        }

        //waitABitLess();

        speed = MAX_SPEED;
        moveToHeroAndAttack(board.getHero());

    }

    private boolean isHeroNear() {
        return true;
    }

    public void endTurn() {
        speed = 0;
    }

    private void moveToHeroAndAttack(final Hero hero) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                synchronized (GameBoard.getLockObject()) {
                    while (speed > 0 && active) {
                        Position heroPos = hero.getPosition();

                        // chase the hero until very near, same x or y position, no diagonal
                        if (Position.isVeryNear(heroPos, position)) {
                            System.out.println("Already very near hero");
                            speed--;
                            continue;
                        }

                        boolean tryOut = new Random().nextBoolean();

                        // chase the hero in zigzag fashion
                        if (Math.abs(heroPos.x - position.x) > Math.abs(heroPos.y - position.y)) {
                            if (heroPos.x > position.x) {//move right
                                if (board.isSquareEmpty(position.x + 1, position.y)) {
                                    setPosition(new Position(position.x + 1, position.y));
                                    waitABit();
                                    speed--;
                                    continue;
                                } else {
                                    if (tryOut && board.isSquareEmpty(position.x, position.y + 1)) { //try up
                                        setPosition(new Position(position.x, position.y + 1));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                    if (board.isSquareEmpty(position.x, position.y - 1)) { //try down
                                        setPosition(new Position(position.x, position.y - 1));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                }
                            } else { //move left
                                if (board.isSquareEmpty(position.x - 1, position.y)) {
                                    setPosition(new Position(position.x - 1, position.y));
                                    waitABit();
                                    speed--;
                                    continue;
                                } else {
                                    if (tryOut && board.isSquareEmpty(position.x, position.y + 1)) { //try up
                                        setPosition(new Position(position.x, position.y + 1));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                    if (board.isSquareEmpty(position.x, position.y - 1)) { //try down
                                        setPosition(new Position(position.x, position.y - 1));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                }
                            }
                        } else { //move up
                            if (heroPos.y > position.y) {
                                if (board.isSquareEmpty(position.x, position.y + 1)) {
                                    setPosition(new Position(position.x, position.y + 1));
                                    waitABit();
                                    speed--;
                                    continue;
                                } else {
                                    if (tryOut && board.isSquareEmpty(position.x + 1, position.y)) { //try right
                                        setPosition(new Position(position.x + 1, position.y));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                    if (board.isSquareEmpty(position.x - 1, position.y)) { //try left
                                        setPosition(new Position(position.x - 1, position.y));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                }
                            } else { //move down
                                if (board.isSquareEmpty(position.x, position.y - 1)) {
                                    setPosition(new Position(position.x, position.y - 1));
                                    waitABit();
                                    speed--;
                                    continue;
                                } else {
                                    if (tryOut && board.isSquareEmpty(position.x + 1, position.y)) { //try right
                                        setPosition(new Position(position.x + 1, position.y));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                    if (board.isSquareEmpty(position.x - 1, position.y)) { //try left
                                        setPosition(new Position(position.x - 1, position.y));
                                        waitABit();
                                        speed--;
                                        continue;
                                    }
                                }
                            }
                        }

//                    waitABit();
//                    speed--;
                    }

                    attackHero(hero);
                    endTurn();
                    board.endMonsterTurn();
                }
            }
        }).start();

    }

    private void waitABit() {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void waitABitLess() {
        try {
            Thread.sleep(300);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public void attackHero(Hero hero) {
        Position heroPos = hero.getPosition();
        if(Position.isNear(heroPos, position)) {
            weaponHit.play();
            hero.takeDamage(attack());
        }
    }

    public int attack() {
        return new Random().nextInt(damage);
    }

    public void activate() {
        active = true;
    }
}
