package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Hero extends Creature {

    public static final int MAX_WEAPON_INVENTORY = 2;

    private Sound weaponHit;
    private int MAX_SPEED = 8;
    private int speed = MAX_SPEED;
    private int attack = 1;
    private Position position;
    private GameBoard board;
    private Sound deathSound;
    private List<Weapon> inventory = new ArrayList<>();
    private Weapon currentWeapon;

    public static final int DEFAULT_ARMOR_CLASS = 14;
    public static final int EXP_PER_LEVEL = 5;

    private int level = 1;
    private int experience = 0;

    public Hero(String image, int health, GameBoard board) {
        super(image, health, DEFAULT_ARMOR_CLASS);
        this.board = board;

        weaponHit = SoundCache.get("sword.wav");
        deathSound = SoundCache.get("death.mp3");
        
        Sword sword = new Sword();
        Axe axe = new Axe();
        inventory.add(sword);
        inventory.add(axe);
        currentWeapon = sword;
    }

    public void heal(int amount) {
        health = Math.min(maxHealth, health + amount);
    }

    public void setPosition(Position newPosition) {
        if (position != null) {
            Square oldSquare = board.getSquare(position.x, position.y);
            if (oldSquare != null) {
                oldSquare.setCreature(null);
                board.restoreFloorItemAt(position);
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
        this.board.onHeroLandedOn(newPosition);
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

    public AttackResult attackAgainst(int targetArmorClass) {
        weaponHit.play(AudioConfig.VOLUME);
        if (currentWeapon != null) {
            return currentWeapon.attackAgainst(targetArmorClass);
        }
        return new AttackResult(0, 0, 0, targetArmorClass, false, 0);
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
            if(pos.y < GameBoard.BOARD_SQUARE_HEIGHT - 1 && board.isHeroTraversable(pos.x, pos.y+1)) {
                setPosition(new Position(pos.x, pos.y+1));
                speed--;
            }
        }
    }

    public void moveRight() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x < GameBoard.BOARD_SQUARE_WIDTH - 1 && board.isHeroTraversable(pos.x+1, pos.y)) {
                setPosition(new Position(pos.x+1, pos.y));
                speed--;
            }
        }
    }

    public void moveDown() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.y > 0 && board.isHeroTraversable(pos.x, pos.y-1)) {
                setPosition(new Position(pos.x, pos.y-1));
                speed--;
            }
        }
    }

    public void moveLeft() {
        if(speed > 0) {
            Position pos = getPosition();
            if(pos.x > 0 && board.isHeroTraversable(pos.x-1, pos.y)) {
                setPosition(new Position(pos.x-1, pos.y));
                speed--;
            }
        }
    }

    public void attackMonster(Monster monster) {
        if(attack > 0) {
            Position monsterPos = monster.getPosition();
            if(Position.isNear(getPosition(), monsterPos)) {
                AttackResult result = attackAgainst(monster.getArmorClass());
                if (result.isHit()) {
                    boolean killingBlow = monster.getHealth() <= result.getDamage();
                    int expValue = monster.getLevel();
                    monster.takeDamage(result.getDamage());
                    if (killingBlow) {
                        addExperience(expValue);
                    }
                    board.logCombat("Hit! " + result.formatRollVsAc() + " (" + result.getDamage() + " dmg)");
                } else {
                    board.logCombat("Miss! " + result.formatRollVsAc());
                }
                attack--;
            }
        }
    }

    public int getLevel() {
        return level;
    }

    public int getExperience() {
        return experience;
    }

    public int getExpPerLevel() {
        return EXP_PER_LEVEL;
    }

    public void addExperience(int amount) {
        if (amount <= 0) {
            return;
        }
        experience += amount;
        String expMessage = "Gained " + amount + " EXP";
        board.logCombat(expMessage);
        board.showToast(expMessage);
        while (experience >= EXP_PER_LEVEL) {
            levelUp();
        }
    }

    private void levelUp() {
        experience -= EXP_PER_LEVEL;
        level++;
        maxHealth++;
        health = Math.min(health + 1, maxHealth);
        String levelMessage = "Level up! Now Lv " + level + " (+1 max HP)";
        board.logCombat(levelMessage);
        board.showToast(levelMessage);
    }

    public int getSpeed() {
        return speed;
    }

    public int getMaxSpeed() {
        return MAX_SPEED;
    }

    public void setMaxSpeed(int maxSpeed) {
        MAX_SPEED = maxSpeed;
        speed = maxSpeed;
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

    public void addWeapon(Weapon weapon) {
        if (inventory.size() < MAX_WEAPON_INVENTORY) {
            inventory.add(weapon);
        }
    }

    /** Replaces the equipped weapon with {@code weapon} and returns the displaced weapon. */
    public Weapon swapEquippedWeapon(Weapon weapon) {
        int idx = inventory.indexOf(currentWeapon);
        if (idx < 0) {
            idx = 0;
        }
        Weapon previous = inventory.get(idx);
        inventory.set(idx, weapon);
        currentWeapon = weapon;
        return previous;
    }

    public void switchWeapon() {
        int currentIndex = inventory.indexOf(currentWeapon);
        int nextIndex = (currentIndex + 1) % inventory.size();
        currentWeapon = inventory.get(nextIndex);
    }

}
