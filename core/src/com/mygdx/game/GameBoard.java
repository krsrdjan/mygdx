package com.mygdx.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameBoard {

    private Texture[][] textures;
    private boolean[][] explored = new boolean[BOARD_SQUARE_LENGTH][BOARD_SQUARE_LENGTH];
    private Hero hero;
    private List<Monster> monsters = new ArrayList<>();
    public static final int SQUARE_SIZE = 64;
    public static final int BOARD_SQUARE_LENGTH = 12;

    private static final Object lock = new Object();

    public static synchronized Object getLockObject(){
        return lock;
    }

    public GameBoard() {
        textures = new Texture[BOARD_SQUARE_LENGTH][BOARD_SQUARE_LENGTH];

        hero = new Hero("hero.png", 20, this);
        hero.setPosition(new Position(0,0));

        Random random = new Random();
        monsters.add(new Monster("orc.png", 8, this));
        //monsters.add(new Monster("werewolf.png", 6, this));
        //monsters.add(new Monster("monster.png", 6, this));

        monsters.get(0).setPosition(new Position(5,5));
        //monsters.get(1).setPosition(new Position(5,3));
        //monsters.get(2).setPosition(new Position(5,0));

    }

    public Texture[][] getTextures() {
        return textures;
    }

    public Texture getTexture(int x, int y) {
        if(explored[x][y]) {
            return textures[x][y];
        } else {
            return createUnexploredTexture();
        }
    }

    public void explore(int x, int y) {
        if(x >= 0 && x < explored.length && y >= 0 && y < explored.length) {
            explored[x][y] = true;
        }
    }

    private Texture createUnexploredTexture() {
        Pixmap pixmap = new Pixmap(SQUARE_SIZE, SQUARE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.2f, 0.2f, 0.2f, 1.0f); // Dark grey color
        pixmap.fill();

        // Create a Texture from the Pixmap
        return new Texture(pixmap);
    }

    public int getTexturePositionX(Texture texture) {
        for (int i = 0; i < textures.length; i++) {
            for (int j = 0; j < textures[i].length; j++) {
                Texture tex = textures[i][j];
                if (tex == texture) {
                    return i * SQUARE_SIZE;
                }
            }
        }

        return 0;
    }

    public int getTexturePositionY(Texture texture) {
        for (int i = 0; i < textures.length; i++) {
            for (int j = 0; j < textures[i].length; j++) {
                Texture tex = textures[i][j];
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
        if(x >= BOARD_SQUARE_LENGTH) {
            return false;
        }

        if(y >= BOARD_SQUARE_LENGTH) {
            return false;
        }

        if(x < 0 || y < 0) {
            return false;
        }

        return textures[x][y] == null;
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
    }
}
