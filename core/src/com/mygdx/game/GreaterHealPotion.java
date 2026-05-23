package com.mygdx.game;

public class GreaterHealPotion extends Item {

    public GreaterHealPotion(GameBoard board) {
        super("potion-blue.png", board);
    }

    @Override
    public void use(Hero hero) {
        int before = hero.getHealth();
        if (before == hero.getMaxHealth()) {
            board.showToast("You found a greater health potion! (HP already full)");
            return;
        }

        hero.heal(2);
        int after = hero.getHealth();
        board.showToast("You found a greater health potion! +" + (after - before) + " HP");

        if (position != null) {
            Square square = board.getSquare(position.x, position.y);
            if (square != null && square.getCreature() == this) {
                square.setCreature(null);
            }
        }
        board.removeItem(this);
    }
}
