package com.mygdx.game;

import java.util.Random;

public abstract class Weapon {
    protected final String name;
    protected final int attackBonus;
    protected final int damage;
    protected final Random random = new Random();

    public Weapon(int attackBonus, int damage, String name) {
        this.attackBonus = attackBonus;
        this.damage = damage;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getDamage() {
        return damage;
    }

    public static String formatAttackBonus(int attackBonus) {
        return attackBonus >= 0 ? "+" + attackBonus : String.valueOf(attackBonus);
    }

    public AttackResult attackAgainst(int targetArmorClass) {
        int roll = random.nextInt(20) + 1;
        int total = roll + attackBonus;
        boolean hit = total >= targetArmorClass;
        int damageDealt = hit ? damage : 0;
        return new AttackResult(roll, attackBonus, total, targetArmorClass, hit, damageDealt);
    }
}
