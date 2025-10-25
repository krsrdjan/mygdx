package com.mygdx.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;


public class GameBoard {

    private Square[][] board;
    private Hero hero;
    private List<Monster> monsters = new ArrayList<>();
    public static final int SQUARE_SIZE = 64;
    public static final int BOARD_SQUARE_HEIGHT = 12;
    public static final int BOARD_SQUARE_WIDTH = 16;
    public boolean exploredAll = false;
    private static final Object lock = new Object();

    public static synchronized Object getLockObject(){
        return lock;
    }

    public GameBoard() {
        board = new Square[BOARD_SQUARE_WIDTH][BOARD_SQUARE_HEIGHT];
        
        // Initialize all squares as empty floors
        for (int x = 0; x < BOARD_SQUARE_WIDTH; x++) {
            for (int y = 0; y < BOARD_SQUARE_HEIGHT; y++) {
                board[x][y] = new Square(null);
            }
        }
       
        hero = new Hero("hero.png", 8, this);
        hero.setPosition(new Position(0,0));
       
    }

    public Square getSquare(int x, int y) {
        if(x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            return board[x][y];
        }
        return null;
    }

    public Texture getTexture(int x, int y) {
        if(x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            Square square = board[x][y];
            if(square.isExplored() || exploredAll) {
                return square.getTexture();
            } else {
                return createUnexploredTexture();
            }
        } else {
            return createUnexploredTexture();
        }
    }

    public void explore(int x, int y) {
        if(x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            board[x][y].setExplored(true);
        }
    }

    public void activateNearMonsters(Position position) {
        for(Monster m : monsters) {
            if(Position.isNear(m.getPosition(), position)) {
                m.activate();
            }
        }
    }

    private Texture createUnexploredTexture() {
        Pixmap pixmap = new Pixmap(SQUARE_SIZE, SQUARE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.2f, 0.2f, 1.0f); // Dark grey color
        pixmap.fill();

        // Create a Texture from the Pixmap
        return new Texture(pixmap);
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
        int distance = BOARD_SQUARE_WIDTH + BOARD_SQUARE_HEIGHT;
        Monster nearest = null;

       for(Monster m : monsters) {
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

        for(Monster m : monsters) {
            m.startTurn();
        }
    }

    public void endMonsterTurn() {
        hero.startTurn();
    }

    public Hero getHero() {
        return hero;
    }

    public boolean isSquareEmpty(int x, int y) {
        if(x >= BOARD_SQUARE_WIDTH || y >= BOARD_SQUARE_HEIGHT || x < 0 || y < 0) {
            return false;
        }
        Square square = board[x][y];
        return square.getTexture() == null && square.getCreature() == null;
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
    }
}
