package com.mygdx.game;

public abstract class Item extends Creature {
    protected Position position;
    protected GameBoard board;

    public Item(String image, GameBoard board) {
        super(image, 1); // Items have 1 health (they can be "consumed")
        this.board = board;
    }

    public void setPosition(Position newPosition) {
        moveTo(newPosition, true);
    }

    /** Updates floor position; optionally claims the square when nothing else occupies it. */
    public void moveTo(Position newPosition, boolean occupySquare) {
        if (position != null) {
            Square oldSquare = board.getSquare(position.x, position.y);
            if (oldSquare != null && oldSquare.getCreature() == this) {
                oldSquare.setCreature(null);
            }
        }

        this.position = newPosition;

        if (occupySquare) {
            Square newSquare = board.getSquare(newPosition.x, newPosition.y);
            if (newSquare != null && newSquare.getCreature() == null) {
                newSquare.setCreature(this);
            }
        }
    }

    public Position getPosition() {
        return position;
    }

    public abstract void use(Hero hero);
}

