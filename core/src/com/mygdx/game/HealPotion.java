package com.mygdx.game;

public class HealPotion extends Item {

    public HealPotion(GameBoard board) {
        super("potion-red.png", board);
    }

    @Override
    public void use(Hero hero) {
        int before = hero.getHealth();
        hero.heal(1);
        int after = hero.getHealth();

        if (after > before) {
            board.showToast("You found a health potion! +" + (after - before) + " HP");
        } else {
            board.showToast("You found a health potion! (HP already full)");
        }

        if (position != null) {
            Square square = board.getSquare(position.x, position.y);
            if (square != null) {
                square.setCreature(null);
            }
        }
        board.removeItem(this);
    }
}
