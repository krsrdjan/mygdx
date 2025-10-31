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
        Monster monster = new Monster("troll.png", 2, 2, 4, board); // tough, slow
        monster.setWeapon(new BigClub());
        return monster;
    }

    public Monster createOrc(GameBoard board) {
        Monster monster = new Monster("orc.png", 2, 1, 5, board);   // balanced
        monster.setWeapon(new Mace());
        return monster;
    }

    public Monster createWerewolf(GameBoard board) {
        Monster monster = new Monster("werewolf.png", 1, 1, 6, board); // fast, fragile
        monster.setWeapon(new Bite());
        return monster;
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


