package org.imnotasecurity.internal.Database;

import org.imnotasecurity.api.Database.ImNotPlayerType;
import org.imnotasecurity.api.Permission.Permission;

import java.time.Instant;

public class DataProfile {
    private ImNotPlayerType playerType;
    public void setPlayerType(ImNotPlayerType playerType) {
        this.playerType = playerType;
    }
    public ImNotPlayerType getPlayerType() {
        return playerType;
    }

    private String playerKey;
    public String getPlayerKey() {
        return playerKey;
    }
    public void setPlayerKey(String playerKey) {
        this.playerKey = playerKey;
    }

    private String password;
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }

    private String email;
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    private Permission permission;
    public Permission getPermission() {
        return permission;
    }
    public void setPermission(Permission permission) {
        this.permission = permission;
    }

    private Instant banDate;
    public Instant getBanDate() {
        return banDate;
    }
    public void setBanDate(Instant banDate) {
        this.banDate = banDate;
    }

    private long banDurationSeconds;
    public long getBanDurationSeconds() {
        return banDurationSeconds;
    }
    public void setBanDurationSeconds(long banDurationSeconds) {
        this.banDurationSeconds = banDurationSeconds;
    }

    private String banReason;
    public void setBanReason(String banReason) {
        this.banReason = banReason;
    }
    public String getBanReason() {
        return banReason;
    }

    private Instant muteDate;
    public Instant getMuteDate() {
        return muteDate;
    }
    public void setMuteDate(Instant muteDate) {
        this.muteDate = muteDate;
    }

    private long muteDurationSeconds;
    public long getMuteDurationSeconds() {
        return muteDurationSeconds;
    }
    public void setMuteDurationSeconds(long muteDurationSeconds) {
        this.muteDurationSeconds = muteDurationSeconds;
    }

    public DataProfile() {

    }
}
