package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameBoard {

    private Texture[][] board;
    private Hero hero;
    private List<Monster> monster = new ArrayList<Monster>();
    public static final int SQUARE_SIZE = 128;
    public static final int BOARD_SQUARE_LENGTH = 6;

    public GameBoard() {
        board = new Texture[BOARD_SQUARE_LENGTH][BOARD_SQUARE_LENGTH];

        hero = new Hero("hero.png", 10, this);
        hero.setPosition(new Position(0,0));

        Random random = new Random();
        monster.add(new Monster("monster.png", 8, this));
        monster.add(new Monster("monster.png", 8, this));

        monster.get(0).setPosition(new Position(5,5));
        monster.get(1).setPosition(new Position(5,4));
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
        hero.moveUp();
    }

    public void moveHeroRight(){
        hero.moveRight();
    }

    public void moveHeroDown(){
        hero.moveDown();
    }

    public void moveHeroLeft(){
        hero.moveLeft();
    }

    public void heroAttack() {
        Monster nearest = getNearestMonster(hero);
        hero.attackMonster(nearest);
    }

    private Monster getNearestMonster(Hero hero) {
        int distance = BOARD_SQUARE_LENGTH + BOARD_SQUARE_LENGTH;
        Monster nearest = null;

       for(Monster m : monster) {
           int current = Position.calculateDistance(hero.getPosition(), m.getPosition());
           if(current <= distance) {
               nearest = m;
               distance = current;
           }
       }

       return nearest;
    }

    public void endHeroTurn() {
        hero.endTurn();
        for(Monster m : monster) {
            m.startTurn();
        }
    }

    public void endMonsterTurn() {
        hero.startTurn();
    }

    public Hero getHero() {
        return hero;
    }

}
