package com.mygdx.game;

import java.util.*;

public class RoomMazeGenerator {

    private static final int ROOMS_W = 8;       // 8 rooms horizontally (8 x 4 = 32)
    private static final int ROOMS_H = 8;       // 8 rooms vertically (8 x 4 = 32)
    private static final int ROOM_SIZE = 4;

    private static final int[][] DIRS = { {0, -1}, {1, 0}, {0, 1}, {-1, 0} }; // N,E,S,W
    private static final int[] OPPOSITE = { 2, 3, 0, 1 };

    private final Random random = new Random();

    public int[][] generate() {
        int[][] roomConnections = generateRoomLevelMaze(ROOMS_W, ROOMS_H);
        return assembleMaze(roomConnections);
    }

    // Step 1: DFS to generate a connected maze at the "room" level
    private int[][] generateRoomLevelMaze(int w, int h) {
        int[][] maze = new int[h][w];
        boolean[][] visited = new boolean[h][w];
        dfs(0, 0, maze, visited, w, h);
        return maze;
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
    }

    // Step 2: build a 4x4 room pattern based on its connections
    private int[][] makeRoom(int connections) {
        int[][] room = new int[ROOM_SIZE][ROOM_SIZE];

        // fill with walls
        for (int i = 0; i < ROOM_SIZE; i++) {
            Arrays.fill(room[i], 1);
        }

        // carve a center area (open)
        for (int y = 1; y < 3; y++) {
            for (int x = 1; x < 3; x++) {
                room[y][x] = 0;
            }
        }

        // carve passages based on connections (N,E,S,W)
        if ((connections & (1 << 0)) != 0) { // North
            room[0][1] = room[0][2] = 0;
        }
        if ((connections & (1 << 1)) != 0) { // East
            room[1][3] = room[2][3] = 0;
        }
        if ((connections & (1 << 2)) != 0) { // South
            room[3][1] = room[3][2] = 0;
        }
        if ((connections & (1 << 3)) != 0) { // West
            room[1][0] = room[2][0] = 0;
        }

        // optional: random small obstacle
        if (random.nextDouble() < 0.3) {
            int ox = 1 + random.nextInt(2);
            int oy = 1 + random.nextInt(2);
            room[oy][ox] = 1;
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
                System.out.print(cell == 1 ? "█" : " ");
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

