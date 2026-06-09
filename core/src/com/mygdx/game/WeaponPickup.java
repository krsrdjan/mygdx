package com.mygdx.game;

public class WeaponPickup extends Item {

    private Weapon weapon;

    public WeaponPickup(Weapon weapon, GameBoard board) {
        super("weapon.png", board);
        this.weapon = weapon;
    }

    public Weapon getWeapon() {
        return weapon;
    }

    @Override
    public void use(Hero hero) {
        Position pickupPos = position;
        if (pickupPos == null) {
            return;
        }

        if (hero.getInventory().size() < Hero.MAX_WEAPON_INVENTORY) {
            hero.addWeapon(weapon);
            board.showToast(formatFound(weapon));
            clearFromSquare();
            board.removeItem(this);
            return;
        }

        Weapon previous = hero.swapEquippedWeapon(weapon);
        board.showToast("Swapped " + previous.getName() + " for " + weapon.getName());

        clearFromSquare();
        board.removeItem(this);

        WeaponPickup dropped = new WeaponPickup(previous, board);
        dropped.moveTo(pickupPos, false);
        board.addItem(dropped);
    }

    static String formatFound(Weapon weapon) {
        return "Found " + weapon.getName() + "! (" + Weapon.formatAttackBonus(weapon.getAttackBonus())
                + " atk, " + weapon.getDamage() + " dmg)";
    }

    private void clearFromSquare() {
        if (position == null) {
            return;
        }
        Square square = board.getSquare(position.x, position.y);
        if (square != null && square.getCreature() == this) {
            square.setCreature(null);
        }
    }
}
