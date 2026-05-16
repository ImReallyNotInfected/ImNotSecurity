package org.imnotasecurity.api.Permission;

public enum Permission {
    MEMBER(0),
    TRIAL_MOD(5),
    MOD(10),
    ADMIN(15),
    DEV(20),
    CO_OWNER(25),
    OWNER(30);

    private final int level;
    Permission(int newLevel) {
        this.level = newLevel;
    }

    public int getLevel() {return level;}
}
