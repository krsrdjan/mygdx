package com.mygdx.game;

public enum HeroKit {
    SQUIRE("Sword", 6, 1, 8, 14, 8),
    BRUTE("Axe", -1, 2, 8, 14, 6);

    private final String weaponName;
    private final int attackBonus;
    private final int damage;
    private final int maxHp;
    private final int armorClass;
    private final int mov;

    HeroKit(String weaponName, int attackBonus, int damage, int maxHp, int armorClass, int mov) {
        this.weaponName = weaponName;
        this.attackBonus = attackBonus;
        this.damage = damage;
        this.maxHp = maxHp;
        this.armorClass = armorClass;
        this.mov = mov;
    }

    public String weaponName() {
        return weaponName;
    }

    public int attackBonus() {
        return attackBonus;
    }

    public int damage() {
        return damage;
    }

    public int maxHp() {
        return maxHp;
    }

    public int armorClass() {
        return armorClass;
    }

    public int mov() {
        return mov;
    }

    public String portraitFile() {
        return "hero.png";
    }

    public String displayName() {
        if (this == BRUTE) {
            return "Brute";
        }
        return "Squire";
    }

    public Weapon startingWeapon() {
        if (this == BRUTE) {
            return new Axe();
        }
        return new Sword();
    }
}
