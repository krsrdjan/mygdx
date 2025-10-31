package com.mygdx.game;

public class Potion extends Item {

    public Potion(GameBoard board) {
        super("potion-red.png", board);
    }

    @Override
    public void use(Hero hero) {
        // Heal hero by 1 if not at max health
        if (hero.getHealth() < hero.getMaxHealth()) {
            hero.heal(1);
            // Remove potion from board
            Square square = board.getSquare(position.x, position.y);
            if (square != null) {
                square.setCreature(null);
            }
            board.removeItem(this);
        }
    }
}

