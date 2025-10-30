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
    public static final int BOARD_SQUARE_HEIGHT = 32;
    public static final int BOARD_SQUARE_WIDTH = 32;
    public boolean exploredAll = false;
    private static final Object lock = new Object();

    public static synchronized Object getLockObject() {
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

        // Generate walls using RoomMazeGenerator
        RoomMazeGenerator generator = new RoomMazeGenerator();
        int[][] maze = generator.generate();
        
        // Remove isolated single walls, keep walls that are part of larger structures
        int[][] cleanedMaze = removeIsolatedWalls(maze);
        
        // Ensure inner 4x4 squares of every room are empty
        int[][] roomCleanedMaze = ensureRoomInteriorsEmpty(cleanedMaze);
        
        // Apply room-cleaned maze to board
        for (int x = 0; x < BOARD_SQUARE_WIDTH; x++) {
            for (int y = 0; y < BOARD_SQUARE_HEIGHT; y++) {
                if (roomCleanedMaze[y][x] == 1) { // 1 = wall
                    board[x][y] = new Square(createWallTexture());
                }
            }
        }

		hero = new Hero("hero.png", 8, this);
		Position spawn = findNearestEmpty(new Position(16, 16));
		spawn = fallbackFindAnyEmpty(spawn);
		if (spawn == null) {
			spawn = new Position(0, 0);
		}
		hero.setPosition(spawn);

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

    private Texture createWallTexture() {
        Pixmap pixmap = new Pixmap(SQUARE_SIZE, SQUARE_SIZE, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.0f, 0.0f, 0.0f, 1.0f); // Black color
        pixmap.fill();
        return new Texture(pixmap);
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
                if (maze[y][x] == 1) { // If it's a wall
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

    public Texture getTexture(int x, int y) {
        if (x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            Square square = board[x][y];
            if (square.isExplored() || exploredAll) {
                return square.getTexture();
            } else {
                return createUnexploredTexture();
            }
        } else {
            return createUnexploredTexture();
        }
    }

    public void explore(int x, int y) {
        if (x >= 0 && x < BOARD_SQUARE_WIDTH && y >= 0 && y < BOARD_SQUARE_HEIGHT) {
            board[x][y].setExplored(true);
        }
    }

    public void activateNearMonsters(Position position) {
        for (Monster m : monsters) {
            if (Position.isNear(m.getPosition(), position)) {
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

    public void moveHeroUp() {
        hero.moveUp();
        System.out.println("moveHeroUp called, tile position: " + getHeroTilePosition());
    }

    public void moveHeroRight() {
        hero.moveRight();
        System.out.println("moveHeroRight called, tile position: " + getHeroTilePosition());
    }

    public void moveHeroDown() {
        hero.moveDown();
        System.out.println("moveHeroDown called, tile position: " + getHeroTilePosition());
    }

    public void moveHeroLeft() {
        hero.moveLeft();
        System.out.println("moveHeroLeft called, tile position: " + getHeroTilePosition());
    }

    public void heroAttack() {
        Monster nearest = getNearestMonster(hero);
        hero.attackMonster(nearest);
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

    public void endHeroTurn() {
        hero.endTurn();

        for (Monster m : monsters) {
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
        if (x >= BOARD_SQUARE_WIDTH || y >= BOARD_SQUARE_HEIGHT || x < 0 || y < 0) {
            return false;
        }
        Square square = board[x][y];
        return square.getTexture() == null && square.getCreature() == null;
    }

    public void removeMonster(Monster monster) {
        monsters.remove(monster);
    }
}
