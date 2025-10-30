package com.mygdx.game;

import java.util.Random;

public class RandomMonsterFactory {

    private static final String[] MONSTER_IMAGES = new String[] {
            "troll.png", "orc.png", "werewolf.png"
    };

    private final Random random = new Random();

    public Monster createRandomMonster(GameBoard board) {
        String image = MONSTER_IMAGES[random.nextInt(MONSTER_IMAGES.length)];
        return createForImage(image, board);
    }

    public Monster createTroll(GameBoard board) {
        return new Monster("troll.png", 6, 2, 4, board); // tough, slow
    }

    public Monster createOrc(GameBoard board) {
        return new Monster("orc.png", 5, 2, 5, board);   // balanced
    }

    public Monster createWerewolf(GameBoard board) {
        return new Monster("werewolf.png", 4, 1, 6, board); // fast, fragile
    }

    private Monster createForImage(String image, GameBoard board) {
        if ("troll.png".equals(image)) {
            return createTroll(board);
        } else if ("orc.png".equals(image)) {
            return createOrc(board);
        } else {
            return createWerewolf(board);
        }
    }
}


