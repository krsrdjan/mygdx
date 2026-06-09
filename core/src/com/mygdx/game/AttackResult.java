package com.mygdx.game;

public class AttackResult {
    private final int roll;
    private final int attackBonus;
    private final int total;
    private final int targetAc;
    private final boolean hit;
    private final int damage;

    public AttackResult(int roll, int attackBonus, int total, int targetAc, boolean hit, int damage) {
        this.roll = roll;
        this.attackBonus = attackBonus;
        this.total = total;
        this.targetAc = targetAc;
        this.hit = hit;
        this.damage = damage;
    }

    public int getRoll() {
        return roll;
    }

    public int getAttackBonus() {
        return attackBonus;
    }

    public int getTotal() {
        return total;
    }

    public int getTargetAc() {
        return targetAc;
    }

    public boolean isHit() {
        return hit;
    }

    public int getDamage() {
        return damage;
    }

    public String formatRollVsAc() {
        String bonusText = attackBonus >= 0 ? "+" + attackBonus : String.valueOf(attackBonus);
        return roll + bonusText + "=" + total + " vs AC " + targetAc;
    }
}
