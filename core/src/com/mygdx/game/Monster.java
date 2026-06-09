package com.mygdx.game;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Timer;

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
    private final String name;
    private Weapon weapon;
    private final int level;

    public Monster(String image, int health, GameBoard board) {
        super(image, health);
        deathSound = SoundCache.get("death.mp3");
        weaponHit = SoundCache.get("sword.wav");

        this.board = board;
        this.name = inferNameFromImage(image);
        this.level = health;
    }

    public Monster(String image, int health, int damage, int maxSpeed, int armorClass, int level, GameBoard board) {
        super(image, health, armorClass);
        deathSound = SoundCache.get("death.mp3");
        weaponHit = SoundCache.get("sword.wav");

        this.board = board;
        this.damage = damage;
        this.MAX_SPEED = maxSpeed;
        this.speed = MAX_SPEED;
        this.name = inferNameFromImage(image);
        this.level = level;
    }
    
    public void setWeapon(Weapon weapon) {
        this.weapon = weapon;
    }

    public int getSpeed() {
        return speed;
    }

    public int getMaxSpeed() {
        return MAX_SPEED;
    }

    public int getLevel() {
        return level;
    }

    public void takeDamage(int damage) {
        health = health - damage;
        if(health <= 0) {
            deathSound.play(AudioConfig.VOLUME);
            Square square = board.getSquare(position.x, position.y);
            if(square != null) {
                square.setCreature(null);
            }
            board.recordMonsterKill();
            board.removeMonster(this);
            if (new Random().nextFloat() < 0.33f) {
                HealPotion potion = new HealPotion(board);
                potion.setPosition(position);
                board.addItem(potion);
            }
        }
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
    }

    public Position getPosition() {
        return position;
    }

    public void startTurn() {
        if(health <= 0) {
            return;
        }

        if (!active) {
            return;
        }

        speed = MAX_SPEED;
        moveToHeroAndAttack(board.getHero());
    }

    public void endTurn() {
        speed = 0;
    }

    private void moveToHeroAndAttack(final Hero hero) {
        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                executeNextStep(hero);
            }
        }, 0.3f);
    }

    private void executeNextStep(final Hero hero) {
        if (health <= 0) {
            board.notifyMonsterTurnComplete();
            return;
        }

        if (speed <= 0 || !active || Position.isNear(getPosition(), hero.getPosition())) {
            attackHero(hero);
            endTurn();
            board.notifyMonsterTurnComplete();
            return;
        }

        Position next = findNextStepBfs(getPosition(), hero.getPosition());
        if (next != null && board.isPassable(next.x, next.y)) {
            setPosition(next);
            speed--;
        } else {
            speed--;
        }

        Timer.schedule(new Timer.Task() {
            @Override
            public void run() {
                executeNextStep(hero);
            }
        }, 0.5f);
    }

    private Position findNextStepBfs(Position start, Position goal) {
        int width = GameBoard.BOARD_SQUARE_WIDTH;
        int height = GameBoard.BOARD_SQUARE_HEIGHT;
        boolean[][] visited = new boolean[width][height];
        Position[][] parent = new Position[width][height];
        java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();

        // If the goal is not walkable (e.g., hero occupies it), we still path to any adjacent
        // We will stop BFS when we reach a tile very-near the hero

        queue.add(start);
        visited[start.x][start.y] = true;
        int[][] dirs = new int[][] { {1,0}, {-1,0}, {0,1}, {0,-1} };

        Position found = null;
        while (!queue.isEmpty()) {
            Position p = queue.poll();
            if (Position.isVeryNear(p, goal)) {
                found = p;
                break;
            }
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];
                if (nx < 0 || ny < 0 || nx >= width || ny >= height) continue;
                if (visited[nx][ny]) continue;
                if (!board.isPassable(nx, ny)) continue;
                visited[nx][ny] = true;
                parent[nx][ny] = p;
                queue.add(new Position(nx, ny));
            }
        }

        if (found == null) {
            return null;
        }

        // Reconstruct path back to start and return the first step after start
        Position cur = found;
        Position prev = parent[cur.x][cur.y];
        while (prev != null && !(prev.x == start.x && prev.y == start.y)) {
            cur = prev;
            prev = parent[cur.x][cur.y];
        }
        return cur;
    }

    public void attackHero(Hero hero) {
        Position heroPos = hero.getPosition();
        if(Position.isNear(heroPos, position)) {
            AttackResult result = attackAgainst(hero.getArmorClass());
            if (result.isHit()) {
                hero.takeDamage(result.getDamage());
                board.logCombat(name + " hits! " + result.formatRollVsAc() + " (" + result.getDamage() + " dmg)");
            } else {
                board.logCombat(name + " misses! " + result.formatRollVsAc());
            }
        }
    }

    public AttackResult attackAgainst(int targetArmorClass) {
        weaponHit.play(AudioConfig.VOLUME);
        if (weapon != null) {
            return weapon.attackAgainst(targetArmorClass);
        }
        int roll = new Random().nextInt(20) + 1;
        int total = roll;
        boolean hit = total >= targetArmorClass;
        int damageDealt = hit ? damage : 0;
        return new AttackResult(roll, 0, total, targetArmorClass, hit, damageDealt);
    }
    
    public Weapon getWeapon() {
        return weapon;
    }

    public void activate(Hero hero) {
        if (active) {
            return;
        }
        active = true;
        if (maxHealth == 1 && Position.isNear(position, hero.getPosition())) {
            board.logCombat(name + " strikes first!");
            attackHero(hero);
        }
    }

    public boolean isActive() {
        return active;
    }

    public String getName() {
        return name;
    }

    private String inferNameFromImage(String image) {
        if (image == null) return "Monster";
        String lower = image.toLowerCase();
        if (lower.contains("troll")) return "Troll";
        if (lower.contains("orc")) return "Orc";
        if (lower.contains("werewolf")) return "Werewolf";
        if (lower.contains("ogre")) return "Ogre";
        if (lower.contains("skeleton")) return "Skeleton";
        return "Monster";
    }
}
