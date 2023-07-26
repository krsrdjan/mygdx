package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.Random;


public class GameBoard {

    private Texture[][] board;
    private Hero hero;
    private Monster monster;
    public static final int SQUARE_SIZE = 128;
    public static final int BOARD_SQUARE_LENGHT = 4;

    public GameBoard() {
        board = new Texture[BOARD_SQUARE_LENGHT][BOARD_SQUARE_LENGHT];

        hero = new Hero("hero.png", 10, this);
        hero.setPosition(new Position(0,0));

        Random random = new Random();
        monster = new Monster("monster.png", 8, this);
        monster.setPosition(new Position(3,3));
//        monster.setPosition(new Position(
//                random.nextInt(1,4),
//                random.nextInt(1,4))
//        );
    }

    public Texture[][] getBoard() {
        return board;
    }

    public int getTexturePositionX(Texture texture) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Texture tex = board[i][j];
                if (tex == texture) {
                    return i * SQUARE_SIZE;
                }
            }
        }

        return 0;
    }

    public int getTexturePositionY(Texture texture) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Texture tex = board[i][j];
                if (tex == texture) {
                    return j * SQUARE_SIZE;
                }
            }
        }

        return 0;
    }

    public void moveHeroUp(){
        Position pos = hero.getPosition();
        if(pos.y < board.length - 1 && board[pos.x][pos.y+1] == null) {
            hero.setPosition(new Position(pos.x, pos.y+1));
        }
    }

    public void moveHeroRight(){
        Position pos = hero.getPosition();
        if(pos.x < board.length - 1 && board[pos.x+1][pos.y] == null) {
            hero.setPosition(new Position(pos.x+1, pos.y));
        }
    }

    public void moveHeroDown(){
        Position pos = hero.getPosition();
        if(pos.y > 0 && board[pos.x][pos.y-1] == null) {
            hero.setPosition(new Position(pos.x, pos.y-1));
        }
    }

    public void moveHeroLeft(){
        Position pos = hero.getPosition();
        if(pos.x > 0 && board[pos.x-1][pos.y] == null) {
            hero.setPosition(new Position(pos.x-1, pos.y));
        }
    }

    public void heroAttack() {
        System.out.println("Hero Attack");
        Position heroPos = hero.getPosition();

        Position monsterPos = monster.getPosition();
        if(Math.abs(heroPos.x - monsterPos.x) <= 1 && Math.abs(heroPos.y - monsterPos.y) <= 1) {
            monster.takeDamage(hero.attack());
        }
    }

    public void endHeroTurn() {
        hero.resetSpeed();
        monster.moveToHeroAndAttack(hero);
        //monster.attackHero(hero);
    }
}
