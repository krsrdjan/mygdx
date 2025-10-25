package com.mygdx.game;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameBoard {

    private Texture[][] textures;
    private boolean[][] explored = new boolean[BOARD_SQUARE_WIDTH][BOARD_SQUARE_HEIGHT];
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
        textures = new Texture[BOARD_SQUARE_WIDTH][BOARD_SQUARE_HEIGHT];

        // Random random = new Random();
        // textures[random.nextInt(BOARD_SQUARE_HEIGHT - 1)][random.nextInt(BOARD_SQUARE_HEIGHT - 1)] = new Texture("rock.png");
        // textures[random.nextInt(BOARD_SQUARE_HEIGHT - 1)][random.nextInt(BOARD_SQUARE_HEIGHT - 1)] = new Texture("rock.png");
        // textures[random.nextInt(BOARD_SQUARE_HEIGHT - 1)][random.nextInt(BOARD_SQUARE_HEIGHT - 1)] = new Texture("rock.png");
        // textures[random.nextInt(BOARD_SQUARE_HEIGHT - 1)][random.nextInt(BOARD_SQUARE_HEIGHT - 1)] = new Texture("rock.png");
        // textures[random.nextInt(BOARD_SQUARE_HEIGHT - 1)][random.nextInt(BOARD_SQUARE_HEIGHT - 1)] = new Texture("rock.png");

        hero = new Hero("hero.png", 20, this);
        hero.setPosition(new Position(0,0));

        // monsters.add(new Monster("orc.png", 8, this));
        // monsters.add(new Monster("werewolf.png", 6, this));
        // monsters.add(new Monster("monster.png", 6, this));

        // monsters.get(0).setPosition(new Position(BOARD_SQUARE_WIDTH / 2, BOARD_SQUARE_HEIGHT / 3));
        // monsters.get(1).setPosition(new Position(0, BOARD_SQUARE_HEIGHT / 3));
        // monsters.get(2).setPosition(new Position(BOARD_SQUARE_WIDTH / 2, 1));

    }

    public Texture[][] getTextures() {
        return textures;
    }

    public Texture getTexture(int x, int y) {
        if(x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            if(explored[x][y] || exploredAll) {
                return textures[x][y];
            } else {
                return createUnexploredTexture();
            }
        } else {
            return createUnexploredTexture();
        }
    }

    public void explore(int x, int y) {
        if(x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            explored[x][y] = true;
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
        if(x >= BOARD_SQUARE_WIDTH) {
            return false;
        }

        if(y >= BOARD_SQUARE_HEIGHT) {
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
