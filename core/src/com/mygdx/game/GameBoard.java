package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;

import java.util.ArrayList;
import java.util.List;

public class GameBoard {

    private Square[][] board;
    private Hero hero;
    private List<Monster> monsters = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private boolean spawnOnExplore = false;
    private boolean heroTurn = true;
    private final boolean[][] roomSpawnAttempted = new boolean[BOARD_SQUARE_WIDTH / 4][BOARD_SQUARE_HEIGHT / 4];
    private static final boolean TEST_MODE = false;

    private final RandomMonsterFactory monsterFactory = new RandomMonsterFactory();
    private final java.util.Random random = new java.util.Random();
    private int activeMonstersTakingTurn = 0;
    public static final int SQUARE_SIZE = 64;
    public static final int BOARD_SQUARE_HEIGHT = 32;
    public static final int BOARD_SQUARE_WIDTH = 32;
    public static final int ROOMS_WIDE = BOARD_SQUARE_WIDTH / 4;
    public static final int ROOMS_TALL = BOARD_SQUARE_HEIGHT / 4;
    public static final int TOTAL_ROOMS = ROOMS_WIDE * ROOMS_TALL;
    public boolean exploredAll = false;
    private final boolean[][] roomExplored = new boolean[ROOMS_WIDE][ROOMS_TALL];
    private int roomsExploredCount = 0;
    private int monstersKilled = 0;
    private boolean victory = false;
    private boolean fullExplorationAnnounced = false;
    private StringCallback toastNotifier;
    private Texture wallTexture;
    private Texture rockTexture;
    private Texture unexploredTexture;
    private final int[][] roomConnections;
    private final CombatLog combatLog = new CombatLog();
    private int round = 1;

    public void setToastNotifier(StringCallback toastNotifier) {
        this.toastNotifier = toastNotifier;
    }

    public void showToast(String message) {
        combatLog.add(message);
        if (toastNotifier != null) {
            toastNotifier.call(message);
        }
    }

    public void logCombat(String message) {
        combatLog.add(message);
    }

    public CombatLog getCombatLog() {
        return combatLog;
    }

    public int getRound() {
        return round;
    }

    public int getRoomsExploredCount() {
        return roomsExploredCount;
    }

    public int getTotalRooms() {
        return TOTAL_ROOMS;
    }

    public int getMonstersKilled() {
        return monstersKilled;
    }

    public boolean isVictory() {
        return victory;
    }

    public void recordMonsterKill() {
        monstersKilled++;
    }

    public GameBoard() {
        board = new Square[BOARD_SQUARE_WIDTH][BOARD_SQUARE_HEIGHT];

        wallTexture = TextureCache.getOrCreateSolid("_wall", 0.0f, 0.0f, 0.0f, 1.0f, SQUARE_SIZE);
        rockTexture = TextureCache.get("rock.png");
        unexploredTexture = TextureCache.getOrCreateSolid("_unexplored", 0.2f, 0.2f, 0.2f, 1.0f, SQUARE_SIZE);

        // Initialize all squares as empty floors
        for (int x = 0; x < BOARD_SQUARE_WIDTH; x++) {
            for (int y = 0; y < BOARD_SQUARE_HEIGHT; y++) {
                board[x][y] = new Square(null);
            }
        }

        // Generate walls using RoomMazeGenerator
        RoomMazeGenerator generator = new RoomMazeGenerator();
        int[][] maze = generator.generate();
        roomConnections = generator.getRoomConnections();
        
        // Remove isolated single walls, keep walls that are part of larger structures
        int[][] cleanedMaze = removeIsolatedWalls(maze);
        
        // Ensure inner 2×2 of every room stays floor (perimeter walls unchanged)
        int[][] roomCleanedMaze = ensureRoomInteriorsEmpty(cleanedMaze);

        generator.applyRoomLayouts(roomCleanedMaze, generator.getRoomConnections());

        for (int x = 0; x < BOARD_SQUARE_WIDTH; x++) {
            for (int y = 0; y < BOARD_SQUARE_HEIGHT; y++) {
                int cell = roomCleanedMaze[y][x];
                if (cell == RoomMazeGenerator.WALL) {
                    board[x][y] = new Square(wallTexture);
                } else if (cell == RoomMazeGenerator.ROCK) {
                    board[x][y] = new Square(rockTexture);
                }
            }
        }

        hero = new Hero("hero.png", TEST_MODE ? 200 : 20, this);
        if (TEST_MODE) {
            hero.setMaxSpeed(200);
        }
        Position spawn = findNearestEmpty(new Position(16, 16));
        spawn = fallbackFindAnyEmpty(spawn);
        if (spawn == null) {
            spawn = new Position(0, 0);
        }
        hero.setPosition(spawn);
        // Enable spawning only after initial placement to avoid flooding the start room
        spawnOnExplore = true;

    }

	private Position findNearestEmpty(Position start) {
		boolean[][] visited = new boolean[BOARD_SQUARE_WIDTH][BOARD_SQUARE_HEIGHT];
		int[][] dirs = new int[][] { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };
		java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();
		// Clamp start inside bounds
		int sx = Math.max(0, Math.min(BOARD_SQUARE_WIDTH - 1, start.x));
		int sy = Math.max(0, Math.min(BOARD_SQUARE_HEIGHT - 1, start.y));
		queue.add(new Position(sx, sy));
		visited[sx][sy] = true;

		while (!queue.isEmpty()) {
			Position p = queue.poll();
			if (isSquareEmpty(p.x, p.y)) {
				return p;
			}
			for (int[] d : dirs) {
				int nx = p.x + d[0];
				int ny = p.y + d[1];
				if (nx >= 0 && nx < BOARD_SQUARE_WIDTH && ny >= 0 && ny < BOARD_SQUARE_HEIGHT && !visited[nx][ny]) {
					visited[nx][ny] = true;
					queue.add(new Position(nx, ny));
				}
			}
		}
		return null;
	}

	private Position fallbackFindAnyEmpty(Position current) {
		if (current != null) {
			return current;
		}
		for (int sx = 0; sx < BOARD_SQUARE_WIDTH; sx++) {
			for (int sy = 0; sy < BOARD_SQUARE_HEIGHT; sy++) {
				if (isSquareEmpty(sx, sy)) {
					return new Position(sx, sy);
				}
			}
		}
		return null;
	}

    
    private int[][] removeIsolatedWalls(int[][] maze) {
        int[][] cleaned = new int[maze.length][maze[0].length];
        
        // Copy original maze
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                cleaned[y][x] = maze[y][x];
            }
        }
        
        // Remove isolated walls (walls with no adjacent walls)
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                if (maze[y][x] == RoomMazeGenerator.WALL) { // perimeter wall
                    if (countAdjacentWalls(maze, x, y) == 0) {
                        // Remove isolated wall
                        cleaned[y][x] = 0;
                    }
                }
            }
        }
        
        return cleaned;
    }
    
    private int countAdjacentWalls(int[][] maze, int x, int y) {
        int count = 0;
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}}; // N, S, W, E
        
        for (int[] dir : directions) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            
            if (nx >= 0 && nx < maze[0].length && ny >= 0 && ny < maze.length) {
                if (maze[ny][nx] == 1) {
                    count++;
                }
            }
        }
        
        return count;
    }
    
    private int[][] ensureRoomInteriorsEmpty(int[][] maze) {
        int[][] cleaned = new int[maze.length][maze[0].length];
        
        // Copy original maze
        for (int y = 0; y < maze.length; y++) {
            for (int x = 0; x < maze[0].length; x++) {
                cleaned[y][x] = maze[y][x];
            }
        }
        
        // Process each 4x4 room
        for (int roomY = 0; roomY < 8; roomY++) { // 8 rooms vertically
            for (int roomX = 0; roomX < 8; roomX++) { // 8 rooms horizontally
                // Clear inner 2x2 area of each 4x4 room (positions 1,1 to 2,2 within the room)
                for (int innerY = 1; innerY <= 2; innerY++) {
                    for (int innerX = 1; innerX <= 2; innerX++) {
                        int worldX = roomX * 4 + innerX;
                        int worldY = roomY * 4 + innerY;
                        
                        if (worldX < maze[0].length && worldY < maze.length) {
                            cleaned[worldY][worldX] = 0; // Make it empty (floor)
                        }
                    }
                }
            }
        }
        
        return cleaned;
    }

    public Position getHeroTilePosition() {
        return new Position(hero.getPosition().x / 4, hero.getPosition().y / 4);
    }

    public Square getSquare(int x, int y) {
        if (x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            return board[x][y];
        }
        return null;
    }

    /** Passable floor for movement/pathfinding (no fog check). */
    public boolean isPassable(int x, int y) {
        return isSquareTraversable(x, y);
    }

    public boolean isWalkable(int x, int y) {
        if (x < 0 || y < 0 || x >= BOARD_SQUARE_WIDTH || y >= BOARD_SQUARE_HEIGHT) {
            return false;
        }
        Square s = board[x][y];
        // Hero click-to-move preview; monsters use isPassable instead
        return s.isExplored() && isSquareTraversable(x, y);
    }

    public Texture getTexture(int x, int y) {
        if (x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            Square square = board[x][y];
            if (square.isExplored() || exploredAll) {
                return square.getTexture();
            } else {
                return unexploredTexture;
            }
        } else {
            return unexploredTexture;
        }
    }

    public void explore(int x, int y) {
        if (x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            Square square = board[x][y];
            boolean newlyExplored = !square.isExplored();
            square.setExplored(true);

            if (newlyExplored) {
                markRoomExploredIfNew(x, y);
            }

            if (spawnOnExplore && newlyExplored) {
                int roomX = x / 4;
                int roomY = y / 4;
                if (roomX >= 0 && roomX < roomSpawnAttempted.length && roomY >= 0 && roomY < roomSpawnAttempted[0].length) {
                    if (!roomSpawnAttempted[roomX][roomY]) {
                        roomSpawnAttempted[roomX][roomY] = true;
                        trySpawnMonsterInRoom(roomX, roomY);
                    }
                }
            }
        }
    }

    private void markRoomExploredIfNew(int x, int y) {
        int roomX = x / 4;
        int roomY = y / 4;
        if (roomX < 0 || roomX >= ROOMS_WIDE || roomY < 0 || roomY >= ROOMS_TALL) {
            return;
        }
        if (roomExplored[roomX][roomY]) {
            return;
        }
        roomExplored[roomX][roomY] = true;
        roomsExploredCount++;
        if (roomsExploredCount >= TOTAL_ROOMS) {
            exploredAll = true;
            if (!fullExplorationAnnounced) {
                fullExplorationAnnounced = true;
                showToast("All rooms explored! Survive this turn to win.");
            }
        }
    }

    private void trySpawnMonsterInRoom(int roomX, int roomY) {
        int startX = roomX * 4;
        int startY = roomY * 4;
        java.util.List<Position> candidates = new java.util.ArrayList<>();
        for (int dx = 0; dx < 4; dx++) {
            for (int dy = 0; dy < 4; dy++) {
                int sx = startX + dx;
                int sy = startY + dy;
                if (sx >= 0 && sx < BOARD_SQUARE_WIDTH && sy >= 0 && sy < BOARD_SQUARE_HEIGHT) {
                    if (isSquareEmpty(sx, sy) && isReachableFromRoomDoors(sx, sy, roomX, roomY)) {
                        candidates.add(new Position(sx, sy));
                    }
                }
            }
        }
        if (!candidates.isEmpty()) {
            Position p = candidates.get(random.nextInt(candidates.size()));

            if (TEST_MODE) {
                WeaponPickup item = createRandomWeaponPickup();
                item.setPosition(p);
                items.add(item);
            } else if (random.nextFloat() < 0.20f) {  // ~20% chance of item instead of monster
                Item item = random.nextFloat() < 0.5f
                        ? new GreaterHealPotion(this)
                        : createRandomWeaponPickup();
                item.setPosition(p);
                items.add(item);
            } else {
                Monster m = monsterFactory.createRandomMonster(this);
                m.setPosition(p);
                monsters.add(m);
            }
        }
    }

    public void activateNearMonsters(Position position) {
        Hero hero = this.hero;
        for (Monster m : monsters) {
            if (Position.isNear(m.getPosition(), position)) {
                m.activate(hero);
            }
        }
    }


    public void moveHeroUp() {
        hero.moveUp();
    }

    public void moveHeroRight() {
        hero.moveRight();
    }

    public void moveHeroDown() {
        hero.moveDown();
    }

    public void moveHeroLeft() {
        hero.moveLeft();
    }

    public void heroAttack() {
        Monster nearest = getNearestMonster(hero);
        hero.attackMonster(nearest);
    }

    public void heroAttackMonster(Monster monster) {
        if (monster != null && Position.isNear(hero.getPosition(), monster.getPosition())) {
            hero.attackMonster(monster);
        }
    }

    public Monster getMonsterAt(int x, int y) {
        for (Monster m : monsters) {
            Position pos = m.getPosition();
            if (pos.x == x && pos.y == y) {
                return m;
            }
        }
        return null;
    }

    public Item getItemAt(int x, int y) {
        for (Item item : items) {
            Position pos = item.getPosition();
            if (pos != null && pos.x == x && pos.y == y) {
                return item;
            }
        }
        return null;
    }

    /** Hero or monster on a square; floor items under an actor are excluded. */
    public Creature getActorAt(int x, int y) {
        Hero h = hero;
        if (h != null && h.isAlive()) {
            Position heroPos = h.getPosition();
            if (heroPos != null && heroPos.x == x && heroPos.y == y) {
                return h;
            }
        }
        return getMonsterAt(x, y);
    }

    public WeaponPickup getWeaponPickupAt(int x, int y) {
        Square square = getSquare(x, y);
        if (square == null || (!square.isExplored() && !exploredAll)) {
            return null;
        }
        for (Item item : items) {
            if (!(item instanceof WeaponPickup)) {
                continue;
            }
            Position pos = item.getPosition();
            if (pos != null && pos.x == x && pos.y == y) {
                return (WeaponPickup) item;
            }
        }
        return null;
    }

    private WeaponPickup createRandomWeaponPickup() {
        float roll = random.nextFloat();
        Weapon weapon;
        if (roll < 1f / 3f) {
            weapon = new Mace();
        } else if (roll < 2f / 3f) {
            weapon = new BigClub();
        } else {
            weapon = new Hammer();
        }
        return new WeaponPickup(weapon, this);
    }

    public void heroClickOnTile(int tileX, int tileY) {
        if (!heroTurn || !hero.isAlive()) {
            return;
        }
        if (tileX < 0 || tileX >= BOARD_SQUARE_WIDTH || tileY < 0 || tileY >= BOARD_SQUARE_HEIGHT) {
            return;
        }

        Position heroPos = hero.getPosition();
        if (tileX == heroPos.x && tileY == heroPos.y) {
            return;
        }

        Monster monsterAtTile = getMonsterAt(tileX, tileY);
        if (monsterAtTile != null && Position.isNear(heroPos, monsterAtTile.getPosition())) {
            heroAttackMonster(monsterAtTile);
            return;
        }

        if (hero.getSpeed() <= 0) {
            showToast("No moves left. End your turn.");
            return;
        }

        int dx = tileX - heroPos.x;
        int dy = tileY - heroPos.y;

        if (Math.abs(dx) >= Math.abs(dy)) {
            if (dx > 0) {
                if (isSquareTraversable(heroPos.x + 1, heroPos.y)) {
                    hero.moveRight();
                    return;
                }
            } else if (dx < 0) {
                if (isSquareTraversable(heroPos.x - 1, heroPos.y)) {
                    hero.moveLeft();
                    return;
                }
            }
            if (dy > 0) {
                if (isSquareTraversable(heroPos.x, heroPos.y + 1)) {
                    hero.moveUp();
                    return;
                }
            } else if (dy < 0) {
                if (isSquareTraversable(heroPos.x, heroPos.y - 1)) {
                    hero.moveDown();
                    return;
                }
            }
        } else {
            if (dy > 0) {
                if (isSquareTraversable(heroPos.x, heroPos.y + 1)) {
                    hero.moveUp();
                    return;
                }
            } else if (dy < 0) {
                if (isSquareTraversable(heroPos.x, heroPos.y - 1)) {
                    hero.moveDown();
                    return;
                }
            }
            if (dx > 0) {
                if (isSquareTraversable(heroPos.x + 1, heroPos.y)) {
                    hero.moveRight();
                    return;
                }
            } else if (dx < 0) {
                if (isSquareTraversable(heroPos.x - 1, heroPos.y)) {
                    hero.moveLeft();
                    return;
                }
            }
        }
    }

    private Monster getNearestMonster(Hero hero) {
        int distance = BOARD_SQUARE_WIDTH + BOARD_SQUARE_HEIGHT;
        Monster nearest = null;

        for (Monster m : monsters) {
            int current = Position.calculateDistance(hero.getPosition(), m.getPosition());
            if (current <= distance) {
                nearest = m;
                distance = current;
            }
        }

        return nearest;
    }

    public Monster getAdjacentMonsterToHero() {
        Monster nearest = getNearestMonster(hero);
        if (nearest != null && Position.isNear(hero.getPosition(), nearest.getPosition())) {
            return nearest;
        }
        return null;
    }

    public void endHeroTurn() {
        if (!heroTurn) {
            return;
        }
        heroTurn = false;
        hero.endTurn();
        if (monsters.isEmpty()) {
            // No monsters to act; immediately start hero's next turn
            endMonsterTurn();
            return;
        }
        // Count how many active monsters will take their turn
        activeMonstersTakingTurn = 0;
        for (Monster m : monsters) {
            if (m.isActive()) {
                activeMonstersTakingTurn++;
            }
        }
        // If no active monsters, immediately start hero's next turn
        if (activeMonstersTakingTurn == 0) {
            endMonsterTurn();
            return;
        }
        // Start turn for all monsters (only active ones will actually move)
        for (Monster m : monsters) {
            m.startTurn();
        }
    }

    public void endMonsterTurn() {
        if (checkAndTriggerVictory()) {
            return;
        }
        round++;
        logCombat("Round " + round + " begins.");
        heroTurn = true;
        hero.startTurn();
    }

    private boolean checkAndTriggerVictory() {
        if (victory) {
            return true;
        }
        if (!hero.isAlive()) {
            return false;
        }
        if (roomsExploredCount < TOTAL_ROOMS) {
            return false;
        }
        victory = true;
        heroTurn = false;
        logCombat("Victory! The dungeon is fully explored.");
        showToast("Victory! The dungeon is fully explored.");
        return true;
    }

    public void notifyMonsterTurnComplete() {
        activeMonstersTakingTurn--;
        if (activeMonstersTakingTurn <= 0) {
            activeMonstersTakingTurn = 0;
            endMonsterTurn();
        }
    }

    public Hero getHero() {
        return hero;
    }

    public boolean isHeroTurn() {
        return heroTurn;
    }

    public boolean isSquareEmpty(int x, int y) {
        if (x >= BOARD_SQUARE_WIDTH || y >= BOARD_SQUARE_HEIGHT || x < 0 || y < 0) {
            return false;
        }
        Square square = board[x][y];
        return square.getTexture() == null && square.getCreature() == null;
    }

    /** Floor items do not block movement or monster pathfinding. */
    public boolean isSquareTraversable(int x, int y) {
        if (x >= BOARD_SQUARE_WIDTH || y >= BOARD_SQUARE_HEIGHT || x < 0 || y < 0) {
            return false;
        }
        Square square = board[x][y];
        Creature occupant = square.getCreature();
        return square.getTexture() == null && (occupant == null || occupant instanceof Item);
    }

    /** Whether a floor square connects to this room's door passages (used for spawn placement). */
    private boolean isReachableFromRoomDoors(int sx, int sy, int roomX, int roomY) {
        if (roomConnections == null) {
            return true;
        }
        int connections = roomConnections[roomY][roomX];
        int x0 = roomX * 4;
        int y0 = roomY * 4;
        boolean[][] visited = new boolean[4][4];
        java.util.ArrayDeque<Position> queue = new java.util.ArrayDeque<>();
        seedDoorTraversable(x0, y0, roomX, roomY, connections, visited, queue);

        int[][] dirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };
        while (!queue.isEmpty()) {
            Position p = queue.poll();
            if (p.x == sx && p.y == sy) {
                return true;
            }
            for (int[] d : dirs) {
                int nx = p.x + d[0];
                int ny = p.y + d[1];
                if (nx < x0 || nx >= x0 + 4 || ny < y0 || ny >= y0 + 4) {
                    continue;
                }
                int lx = nx - x0;
                int ly = ny - y0;
                if (visited[lx][ly] || !isSquareTraversable(nx, ny)) {
                    continue;
                }
                visited[lx][ly] = true;
                queue.add(new Position(nx, ny));
            }
        }
        return false;
    }

    private void seedDoorTraversable(int x0, int y0, int roomX, int roomY, int connections,
            boolean[][] visited, java.util.ArrayDeque<Position> queue) {
        if ((connections & (1 << 0)) != 0) {
            trySeedTraversable(x0 + 1, y0 + 0, x0, y0, visited, queue);
            trySeedTraversable(x0 + 2, y0 + 0, x0, y0, visited, queue);
        }
        if ((connections & (1 << 1)) != 0) {
            trySeedTraversable(x0 + 3, y0 + 1, x0, y0, visited, queue);
            trySeedTraversable(x0 + 3, y0 + 2, x0, y0, visited, queue);
        }
        if ((connections & (1 << 2)) != 0) {
            trySeedTraversable(x0 + 1, y0 + 3, x0, y0, visited, queue);
            trySeedTraversable(x0 + 2, y0 + 3, x0, y0, visited, queue);
        }
        if ((connections & (1 << 3)) != 0) {
            trySeedTraversable(x0 + 0, y0 + 1, x0, y0, visited, queue);
            trySeedTraversable(x0 + 0, y0 + 2, x0, y0, visited, queue);
        }
    }

    private void trySeedTraversable(int wx, int wy, int x0, int y0,
            boolean[][] visited, java.util.ArrayDeque<Position> queue) {
        if (!isSquareTraversable(wx, wy)) {
            return;
        }
        int lx = wx - x0;
        int ly = wy - y0;
        if (!visited[lx][ly]) {
            visited[lx][ly] = true;
            queue.add(new Position(wx, wy));
        }
    }

    /** Puts a floor Item back on its Square after a Creature steps off. */
    public void restoreFloorItemAt(Position position) {
        for (Item item : items) {
            Position itemPos = item.getPosition();
            if (itemPos != null && itemPos.x == position.x && itemPos.y == position.y) {
                Square square = getSquare(position.x, position.y);
                if (square != null && square.getCreature() == null) {
                    square.setCreature(item);
                }
                return;
            }
        }
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
    }

    public void removeItem(Item item) {
        items.remove(item);
    }

    public void addItem(Item item) {
        items.add(item);
    }

    public void collectNearbyItems(Hero hero) {
        Position heroPos = hero.getPosition();
        java.util.List<Item> itemsToCollect = new java.util.ArrayList<>();
        for (Item item : items) {
            Position itemPos = item.getPosition();
            if (itemPos != null && itemPos.x == heroPos.x && itemPos.y == heroPos.y) {
                itemsToCollect.add(item);
            }
        }
        for (Item item : itemsToCollect) {
            item.use(hero);
        }
    }
}
