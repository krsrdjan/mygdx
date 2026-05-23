package com.mygdx.game;

public class HealPotion extends Item {

    public HealPotion(GameBoard board) {
        super("potion-red.png", board);
    }

    @Override
    public void use(Hero hero) {
        int before = hero.getHealth();
        if (before == hero.getMaxHealth()) {
            board.showToast("You found a health potion! (HP already full)");
            return;
        }

        hero.heal(1);
        int after = hero.getHealth();
        board.showToast("You found a health potion! +" + (after - before) + " HP");

        if (position != null) {
            Square square = board.getSquare(position.x, position.y);
            if (square != null && square.getCreature() == this) {
                square.setCreature(null);
            }
        }
        board.removeItem(this);
    }
}
