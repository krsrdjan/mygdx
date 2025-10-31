package com.mygdx.game;

public abstract class Item extends Creature {
    protected Position position;
    protected GameBoard board;

    public Item(String image, GameBoard board) {
        super(image, 1); // Items have 1 health (they can be "consumed")
        this.board = board;
    }

    public void setPosition(Position newPosition) {
        if (position != null) {
            Square oldSquare = board.getSquare(position.x, position.y);
            if (oldSquare != null) {
                oldSquare.setCreature(null);
            }
        }

        Square newSquare = board.getSquare(newPosition.x, newPosition.y);
        if (newSquare != null) {
            newSquare.setCreature(this);
        }
        this.position = newPosition;
    }

    public Position getPosition() {
        return position;
    }

    public abstract void use(Hero hero);
}

