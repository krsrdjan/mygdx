package com.mygdx.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import java.util.ArrayList;
import java.util.Collection;

public class Hero extends Creature {

    private Sound weaponHit;
    private int MAX_SPEED = 8;
    private int speed = MAX_SPEED;
    private int attack = 1;
    private Position position;
    private GameBoard board;
    private Sound deathSound;
    private Collection<Weapon> inventory = new ArrayList<>();
    private Weapon currentWeapon;
    private int MAX_HEALTH;

    public Hero(String image, int health, GameBoard board) {
        super(image, health);
        this.MAX_HEALTH = health;
        weaponHit = Gdx.audio.newSound(Gdx.files.internal("sword.wav"));
        deathSound = Gdx.audio.newSound(Gdx.files.internal("death.mp3"));
        this.board = board;
        // Add Sword to inventory
        Sword sword = new Sword();
        inventory.add(sword);
        currentWeapon = sword;
    }
    
    public int getMaxHealth() {
        return MAX_HEALTH;
    }
    
    public void heal(int amount) {
        health = Math.min(MAX_HEALTH, health + amount);
    }

    public void setPosition(Position newPosition) {
        if(position != null) {
            Square oldSquare = board.getSquare(position.x, position.y);
            if(oldSquare != null) {
                oldSquare.setCreature(null);
            }
        }

        Square newSquare = board.getSquare(newPosition.x, newPosition.y);
        if(newSquare != null) {
            newSquare.setCreature(this);
        }
        this.position = newPosition;

        exploreAroundHero(newPosition);
        this.board.activateNearMonsters(newPosition);
        this.board.collectNearbyItems(this);
    }

    private void exploreAroundHero(Position newPosition) {
        Position tilePosition = board.getHeroTilePosition();

        //for tile 0,0 explore each square in the tile 0,0 to 3,3 etc
        for(int i = 0; i < 4; i++) {
            for(int j = 0; j < 4; j++) {
                board.explore(tilePosition.x * 4 + i, tilePosition.y * 4 + j);
            }
        }
    }

    public Position getPosition() {
        return position;
    }

    public int attack() {
        weaponHit.play(AudioConfig.VOLUME);
        if (currentWeapon != null) {
            return currentWeapon.attack();
        }
        return 0; // No weapon equipped
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
        health = health - damage;
        if(health <= 0) {
            deathSound.play(AudioConfig.VOLUME);
            Square square = board.getSquare(position.x, position.y);
            if(square != null) {
                square.setCreature(null);
            }
        }
    }

    public void moveUp() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y < GameBoard.BOARD_SQUARE_HEIGHT - 1 && board.isSquareEmpty(pos.x, pos.y+1)) {
                setPosition(new Position(pos.x, pos.y+1));
                speed--;
            }
        }
    }

    public void moveRight() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x < GameBoard.BOARD_SQUARE_WIDTH - 1 && board.isSquareEmpty(pos.x+1, pos.y)) {
                setPosition(new Position(pos.x+1, pos.y));
                speed--;
            }
        }
    }

    public void moveDown() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y > 0 && board.isSquareEmpty(pos.x, pos.y-1)) {
                setPosition(new Position(pos.x, pos.y-1));
                speed--;
            }
        }
    }

    public void moveLeft() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x > 0 && board.isSquareEmpty(pos.x-1, pos.y)) {
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

    public int getSpeed() {
        return speed;
    }

    public Collection<Weapon> getInventory() {
        return inventory;
    }

    public Weapon getCurrentWeapon() {
        return currentWeapon;
    }

    public void setCurrentWeapon(Weapon weapon) {
        if (inventory.contains(weapon)) {
            this.currentWeapon = weapon;
        }
    }

}
