package com.mygdx.game;

import java.util.Random;

public class RandomMonsterFactory {

    private static final String[] MONSTER_IMAGES = new String[] {
            "troll.png", "orc.png", "werewolf.png", "ogre.png", "skeleton.png"
    };

    private final Random random = new Random();

    public Monster createRandomMonster(GameBoard board) {
        String image = MONSTER_IMAGES[random.nextInt(MONSTER_IMAGES.length)];
        return createForImage(image, board);
    }

    public Monster createTroll(GameBoard board) {
        Monster monster = new Monster("troll.png", 2, 2, 4, 14, 2, board);
        monster.setWeapon(new BigClub());
        return monster;
    }

    public Monster createOrc(GameBoard board) {
        Monster monster = new Monster("orc.png", 2, 1, 5, 13, 2, board);
        monster.setWeapon(new Mace());
        return monster;
    }

    public Monster createWerewolf(GameBoard board) {
        Monster monster = new Monster("werewolf.png", 1, 1, 6, 12, 1, board);
        monster.setWeapon(new Bite());
        return monster;
    }

    public Monster createOgre(GameBoard board) {
        Monster monster = new Monster("ogre.png", 3, 3, 3, 15, 3, board);
        monster.setWeapon(new Hammer());
        return monster;
    }

    public Monster createSkeleton(GameBoard board) {
        Monster monster = new Monster("skeleton.png", 1, 1, 7, 11, 1, board);
        monster.setWeapon(new RustyBlade());
        return monster;
    }

    private Monster createForImage(String image, GameBoard board) {
        if ("troll.png".equals(image)) {
            return createTroll(board);
        } else if ("orc.png".equals(image)) {
            return createOrc(board);
        } else if ("ogre.png".equals(image)) {
            return createOgre(board);
        } else if ("skeleton.png".equals(image)) {
            return createSkeleton(board);
        } else {
            return createWerewolf(board);
        }
    }
}
