package org.imnotasecurity.api.Database;

import net.minestom.server.entity.Player;
import org.imnotasecurity.internal.Database.SecDataProfile;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ImNotDataProfile {
    private static Map<UUID, SecDataProfile> dataProfiles = new ConcurrentHashMap<>();
    
    @Deprecated
    public static void addPlayer(Player player, SecDataProfile newSecDataProfile) {
        UUID uuid = player.getUuid();
        dataProfiles.putIfAbsent(uuid, newSecDataProfile);
    }

    @Nullable public static SecDataProfile getDataProfileFromPlayer(Player player) {
        return dataProfiles.getOrDefault(player.getUuid(),null);
    }

    @Nullable public static SecDataProfile getDataProfileFromPlayer(UUID uuid) {
        return dataProfiles.getOrDefault(uuid,null);
    }

    public static void removeDataProfileFromPlayer(Player player) {
        dataProfiles.remove(player.getUuid());
    }

    public record OnlineStatus(ImNotPlayerType type, String key) {}
    public static OnlineStatus getOnlineStatus(Player player) {
        //check if cracked or not
        ImNotPlayerType playerType;
        String key;
        if (player.getUuid().version() == 4) {
            playerType = ImNotPlayerType.PREMIUM;
            key = player.getUuid().toString();
        } else {
            playerType = ImNotPlayerType.CRACKED;
            key = player.getUsername().toLowerCase();
        }

        return new OnlineStatus(playerType,key);
    }
}
