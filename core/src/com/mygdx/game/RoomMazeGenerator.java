package com.mygdx.game;

import java.util.*;

public class RoomMazeGenerator {

    private static final int ROOMS_W = 8;       // 8 rooms horizontally (8 x 4 = 32)
    private static final int ROOMS_H = 8;       // 8 rooms vertically (8 x 4 = 32)
    private static final int ROOM_SIZE = 4;

    /** Maze cell values */
    public static final int FLOOR = 0;
    public static final int WALL = 1;
    public static final int ROCK = 2;

    private static final int HERO_SPAWN_ROOM_X = 4;
    private static final int HERO_SPAWN_ROOM_Y = 4;

    /** Local (x,y) within a 4×4 room — only placed on walkable cells. */
    private static final int[][][] AMBUSH_PATTERNS = {
            { {1, 1} },
            { {2, 2} },
            { {1, 1}, {2, 2} },
            { {2, 1}, {1, 2} },
            { {1, 2}, {2, 2} },
    };

    private static final int[][][] MEDIUM_PATTERNS = {
            { {1, 2} },
            { {2, 1} },
            { {1, 1} },
            { {2, 2} },
    };

    private static final int[][] DIRS = { {0, -1}, {1, 0}, {0, 1}, {-1, 0} }; // N,E,S,W
    private static final int[] OPPOSITE = { 2, 3, 0, 1 };

    private final Random random = new Random();
    private int[][] lastRoomConnections;

    public int[][] generate() {
        lastRoomConnections = generateRoomLevelMaze(ROOMS_W, ROOMS_H);
        return assembleMaze(lastRoomConnections);
    }

    public int[][] getRoomConnections() {
        return lastRoomConnections;
    }

    /**
     * Places {@link #ROCK} cells by {@link #ROOM_SIZE room} exit count. Call after perimeter walls are
     * finalized (e.g. after {@code ensureRoomInteriorsEmpty} in {@link GameBoard}).
     */
    public void applyRoomLayouts(int[][] maze, int[][] roomConnections) {
        if (roomConnections == null) {
            return;
        }
        for (int ry = 0; ry < ROOMS_H; ry++) {
            for (int rx = 0; rx < ROOMS_W; rx++) {
                if (rx == HERO_SPAWN_ROOM_X && ry == HERO_SPAWN_ROOM_Y) {
                    continue;
                }
                applyLayoutForRoom(maze, rx, ry, roomConnections[ry][rx]);
            }
        }
    }

    private void applyLayoutForRoom(int[][] maze, int roomX, int roomY, int connections) {
        int exits = Integer.bitCount(connections);
        if (exits >= 3) {
            return;
        }
        int[][][] pool = exits == 1 ? AMBUSH_PATTERNS : MEDIUM_PATTERNS;
        List<int[][]> candidates = new ArrayList<>(Arrays.asList(pool));
        Collections.shuffle(candidates, random);
        for (int[][] pattern : candidates) {
            int[][] saved = copyRoom(maze, roomX, roomY);
            stampPattern(maze, roomX, roomY, pattern);
            if (isRoomFloorConnectedFromDoors(maze, roomX, roomY, connections)) {
                return;
            }
            restoreRoom(maze, roomX, roomY, saved);
        }
    }

    private int[][] copyRoom(int[][] maze, int roomX, int roomY) {
        int[][] copy = new int[ROOM_SIZE][ROOM_SIZE];
        for (int ly = 0; ly < ROOM_SIZE; ly++) {
            for (int lx = 0; lx < ROOM_SIZE; lx++) {
                int wx = roomX * ROOM_SIZE + lx;
                int wy = roomY * ROOM_SIZE + ly;
                copy[ly][lx] = maze[wy][wx];
            }
        }
        return copy;
    }

    private void restoreRoom(int[][] maze, int roomX, int roomY, int[][] saved) {
        for (int ly = 0; ly < ROOM_SIZE; ly++) {
            for (int lx = 0; lx < ROOM_SIZE; lx++) {
                int wx = roomX * ROOM_SIZE + lx;
                int wy = roomY * ROOM_SIZE + ly;
                maze[wy][wx] = saved[ly][lx];
            }
        }
    }

    /** All FLOOR cells must be reachable from door passages (prevents sealed-off pockets). */
    private boolean isRoomFloorConnectedFromDoors(int[][] maze, int roomX, int roomY, int connections) {
        int floorCount = 0;
        for (int ly = 0; ly < ROOM_SIZE; ly++) {
            for (int lx = 0; lx < ROOM_SIZE; lx++) {
                int wx = roomX * ROOM_SIZE + lx;
                int wy = roomY * ROOM_SIZE + ly;
                if (maze[wy][wx] == FLOOR) {
                    floorCount++;
                }
            }
        }
        if (floorCount <= 1) {
            return true;
        }

        boolean[][] visited = new boolean[ROOM_SIZE][ROOM_SIZE];
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        seedDoorFloors(maze, roomX, roomY, connections, visited, queue);

        int reached = 0;
        int[][] walkDirs = { {1, 0}, {-1, 0}, {0, 1}, {0, -1} };
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int wx = p[0];
            int wy = p[1];
            if (maze[wy][wx] == FLOOR) {
                reached++;
            }
            for (int[] d : walkDirs) {
                int nx = wx + d[0];
                int ny = wy + d[1];
                if (nx < roomX * ROOM_SIZE || nx >= (roomX + 1) * ROOM_SIZE
                        || ny < roomY * ROOM_SIZE || ny >= (roomY + 1) * ROOM_SIZE) {
                    continue;
                }
                int lx = nx - roomX * ROOM_SIZE;
                int ly = ny - roomY * ROOM_SIZE;
                if (!visited[ly][lx] && maze[ny][nx] == FLOOR) {
                    visited[ly][lx] = true;
                    queue.add(new int[] { nx, ny });
                }
            }
        }
        return reached == floorCount;
    }

    private void seedDoorFloors(int[][] maze, int roomX, int roomY, int connections,
            boolean[][] visited, ArrayDeque<int[]> queue) {
        int x0 = roomX * ROOM_SIZE;
        int y0 = roomY * ROOM_SIZE;
        if ((connections & (1 << 0)) != 0) {
            trySeedFloor(maze, x0 + 1, y0 + 0, roomX, roomY, visited, queue);
            trySeedFloor(maze, x0 + 2, y0 + 0, roomX, roomY, visited, queue);
        }
        if ((connections & (1 << 1)) != 0) {
            trySeedFloor(maze, x0 + 3, y0 + 1, roomX, roomY, visited, queue);
            trySeedFloor(maze, x0 + 3, y0 + 2, roomX, roomY, visited, queue);
        }
        if ((connections & (1 << 2)) != 0) {
            trySeedFloor(maze, x0 + 1, y0 + 3, roomX, roomY, visited, queue);
            trySeedFloor(maze, x0 + 2, y0 + 3, roomX, roomY, visited, queue);
        }
        if ((connections & (1 << 3)) != 0) {
            trySeedFloor(maze, x0 + 0, y0 + 1, roomX, roomY, visited, queue);
            trySeedFloor(maze, x0 + 0, y0 + 2, roomX, roomY, visited, queue);
        }
    }

    private void trySeedFloor(int[][] maze, int wx, int wy, int roomX, int roomY,
            boolean[][] visited, ArrayDeque<int[]> queue) {
        if (maze[wy][wx] != FLOOR) {
            return;
        }
        int lx = wx - roomX * ROOM_SIZE;
        int ly = wy - roomY * ROOM_SIZE;
        if (!visited[ly][lx]) {
            visited[ly][lx] = true;
            queue.add(new int[] { wx, wy });
        }
    }

    private void stampPattern(int[][] maze, int roomX, int roomY, int[][] localCells) {
        for (int[] cell : localCells) {
            int lx = cell[0];
            int ly = cell[1];
            int worldX = roomX * ROOM_SIZE + lx;
            int worldY = roomY * ROOM_SIZE + ly;
            if (worldX >= 0 && worldX < maze[0].length && worldY >= 0 && worldY < maze.length) {
                if (maze[worldY][worldX] == FLOOR) {
                    maze[worldY][worldX] = ROCK;
                }
            }
        }
    }

    // Step 1: DFS to generate a connected maze at the "room" level
    private int[][] generateRoomLevelMaze(int w, int h) {
        int[][] maze = new int[h][w];
        boolean[][] visited = new boolean[h][w];
        dfs(0, 0, maze, visited, w, h);
        
        // Add more connections to create junction-like rooms
        addJunctionConnections(maze, w, h);
        
        return maze;
    }
    
    // Add additional connections to create more 3-way and 4-way junctions
    private void addJunctionConnections(int[][] maze, int w, int h) {
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                // For each room, try to add more connections
                List<Integer> availableDirs = new ArrayList<>();
                for (int d = 0; d < 4; d++) {
                    int nx = x + DIRS[d][0];
                    int ny = y + DIRS[d][1];
                    if (nx >= 0 && nx < w && ny >= 0 && ny < h) {
                        // Add connection with 6% chance if not already connected
                        if ((maze[y][x] & (1 << d)) == 0 && random.nextDouble() < 0.06) {
                            maze[y][x] |= (1 << d);
                            maze[ny][nx] |= (1 << OPPOSITE[d]);
                        }
                    }
                }
            }
        }
    }

    private void dfs(int x, int y, int[][] maze, boolean[][] visited, int w, int h) {
        visited[y][x] = true;

        List<Integer> dirs = Arrays.asList(0, 1, 2, 3);
        Collections.shuffle(dirs, random);

        for (int d : dirs) {
            int nx = x + DIRS[d][0];
            int ny = y + DIRS[d][1];
            if (nx >= 0 && nx < w && ny >= 0 && ny < h && !visited[ny][nx]) {
                // mark bidirectional connection
                maze[y][x] |= (1 << d);
                maze[ny][nx] |= (1 << OPPOSITE[d]);
                dfs(nx, ny, maze, visited, w, h);
            }
        }
        
        // Add extra connections to create more junctions (9% chance)
        for (int d : dirs) {
            int nx = x + DIRS[d][0];
            int ny = y + DIRS[d][1];
            if (nx >= 0 && nx < w && ny >= 0 && ny < h && random.nextDouble() < 0.09) {
                // Add extra connection if not already connected
                if ((maze[y][x] & (1 << d)) == 0) {
                    maze[y][x] |= (1 << d);
                    maze[ny][nx] |= (1 << OPPOSITE[d]);
                }
            }
        }
    }

    // Step 2: build a 4x4 room pattern based on its connections
    private int[][] makeRoom(int connections) {
        int[][] room = new int[ROOM_SIZE][ROOM_SIZE];

        // fill with walls
        for (int i = 0; i < ROOM_SIZE; i++) {
            Arrays.fill(room[i], WALL);
        }

        // carve a center area (open)
        for (int y = 1; y < 3; y++) {
            for (int x = 1; x < 3; x++) {
                room[y][x] = FLOOR;
            }
        }

        // carve passages based on connections (N,E,S,W)
        if ((connections & (1 << 0)) != 0) { // North
            room[0][1] = room[0][2] = FLOOR;
        }
        if ((connections & (1 << 1)) != 0) { // East
            room[1][3] = room[2][3] = FLOOR;
        }
        if ((connections & (1 << 2)) != 0) { // South
            room[3][1] = room[3][2] = FLOOR;
        }
        if ((connections & (1 << 3)) != 0) { // West
            room[1][0] = room[2][0] = FLOOR;
        }


        return room;
    }

    // Step 3: assemble the 8x8 rooms into a 32x32 maze
    private int[][] assembleMaze(int[][] roomMaze) {
        int[][] maze = new int[ROOMS_H * ROOM_SIZE][ROOMS_W * ROOM_SIZE];

        for (int ry = 0; ry < ROOMS_H; ry++) {
            for (int rx = 0; rx < ROOMS_W; rx++) {
                int[][] room = makeRoom(roomMaze[ry][rx]);
                for (int y = 0; y < ROOM_SIZE; y++) {
                    for (int x = 0; x < ROOM_SIZE; x++) {
                        maze[ry * ROOM_SIZE + y][rx * ROOM_SIZE + x] = room[y][x];
                    }
                }
            }
        }

        return maze;
    }

    // Utility: print maze to console
    public static void printMaze(int[][] maze) {
        for (int[] row : maze) {
            for (int cell : row) {
                System.out.print(cell == WALL ? "█" : (cell == ROCK ? "▪" : " "));
            }
            System.out.println();
        }
    }

    // --- Test ---
    public static void main(String[] args) {
        RoomMazeGenerator generator = new RoomMazeGenerator();
        int[][] maze = generator.generate();
        printMaze(maze);
    }
}

