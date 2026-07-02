package org.imnotasecurity.internal.Database;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.lang.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.*;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.pojo.PojoCodecProvider;
import org.imnotasecurity.api.Database.ImNotDataProfile;
import org.imnotasecurity.api.Database.ImNotPlayerType;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.api.ImNotServerState;
import org.imnotasecurity.api.Permission.Permission;
import org.imnotasecurity.api.Properties.AbstractProperty;
import org.imnotasecurity.api.Properties.MongoProperty;
import org.imnotasecurity.internal.Auth.AuthMaster;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.*;

import static com.mongodb.client.model.Filters.eq;

public class DatabaseMaster {
    //for unique property
    public static MongoCollection<SecDataProfile> mongoCollection;
    //end
    private static SecDataProfile createNewDataBase(String playerKey, ImNotPlayerType playerType) {
        var prof = new SecDataProfile();
        prof.setPlayerKey(playerKey);
        prof.setPlayerType(playerType);
        prof.setEmail("");
        prof.setPassword("");
        prof.setPermission(Permission.MEMBER);
        Instant now = Instant.now();
        prof.setBanDate(now);
        prof.setBanDurationSeconds(0);
        prof.setMuteDate(now);
        prof.setMuteDurationSeconds(0);
        prof.setBanReason("");

        return prof;
    }

    private static Map<String, Long> hangList = new ConcurrentHashMap<>();
    private final static float saveTimeLimit = 10;

    @NonNull private static SecDataProfile getRawDataBase(Player player) {
        var property = ImNotSecurity.getProperty();
        //check if cracked or not
        var a = ImNotDataProfile.getOnlineStatus(player);
        ImNotPlayerType playerType = a.type();
        String key = a.key();

        if (property instanceof MongoProperty) {
            SecDataProfile profile = mongoCollection.find(eq("playerKey",key)).first();
            if (profile != null) {
                if (player.getUuid().version() == 4) {
                    profile.setPlayerName(player.getUsername().toLowerCase());
                }

                return profile;
            } else {
                SecDataProfile newProfile = createNewDataBase(key,playerType);
                mongoCollection.insertOne(newProfile);
                return newProfile;
            }
        } else {
            return createNewDataBase(key, playerType);
        }
    }

    public static void loadProfile(Player player, SecDataProfile profile) {
//        var dataProfile = getRawDataBase(player);
        ImNotDataProfile.addPlayer(player, profile);
    }

    public static void saveProfile(Player player) {
        SecDataProfile profile = ImNotDataProfile.getDataProfileFromPlayer(player);
        if (profile!=null) {
            String key = profile.getPlayerKey();
            hangList.put(key, System.currentTimeMillis());
//            hangList.putIfAbsent(key, System.currentTimeMillis());
            ImNotDataProfile.removeDataProfileFromPlayer(player);
            mongoCollection.findOneAndReplace(eq("playerKey",profile.getPlayerKey()), profile);
            //done
            hangList.remove(key);
        }
    }

    public static void init() {
        AbstractProperty property = ImNotSecurity.getProperty();
        if (property instanceof MongoProperty mongoProperty) {
            MongoClientSettings settings = MongoClientSettings.builder().applyConnectionString(new ConnectionString(mongoProperty.getKey()))
                    .codecRegistry(
                            CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(),CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))
                    ).build();
            try {
                var client = MongoClients.create(settings);
                MongoDatabase player = client.getDatabase("security");
                mongoCollection = player.getCollection("data", SecDataProfile.class);
            } catch (Exception e) {
                System.err.println("FATAL ERROR "+ e.getMessage());
                System.exit(1);
            }
        }

        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        // Player joining
        eventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            Player player = event.getPlayer();
            var status = ImNotDataProfile.getOnlineStatus(player);
            String key = status.key();

            if (hangList.containsKey(key) && System.currentTimeMillis() - hangList.get(key) < ((long) saveTimeLimit * 1000)) {
                player.kick(Component.text(
                        switch (property.getLanguage()) {
                            case VIETNAMESE -> "Bạn vào quá nhanh! Hãy chờ một tí nữa nhé!";
                            default -> "You joined too fast! Please wait.";
                        }
                ).color(NamedTextColor.RED));
                return;
            }

            hangList.remove(key);
            SecDataProfile profile = getRawDataBase(player);
            loadProfile(player, profile);

            boolean successful = AuthMaster.authPlayer(player, profile);
            if (!successful) { return; }
            player.removeTag(AuthMaster.STILL_IN_LOGIN);

            property.getLoadCallback().accept(event);
        });

        eventHandler.addListener(PlayerDisconnectEvent.class, event -> {
            Player player = event.getPlayer();

            SecDataProfile profile = ImNotDataProfile.getDataProfileFromPlayer(player);
            if (profile == null) return;

            String key = profile.getPlayerKey();
            hangList.put(key, System.currentTimeMillis());
            ImNotDataProfile.removeDataProfileFromPlayer(player);

            boolean isShuttingDown = ImNotSecurity.getServerState() == ImNotServerState.SHUTTING_DOWN;

            if (isShuttingDown) {
                try {
                    mongoCollection.findOneAndReplace(eq("playerKey", key), profile);
                } catch (Exception e) {
                    hangList.remove(key);
                    e.printStackTrace();
                } finally {
                    hangList.remove(key);
                }
            } else {
                CompletableFuture.runAsync(() -> {
                    try {
                        mongoCollection.findOneAndReplace(eq("playerKey", key), profile);
                    } catch (Exception e) {
                        hangList.remove(key);
                        e.printStackTrace();
                    } finally {
                        hangList.remove(key);
                    }
                });
            }
        });

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            ImNotSecurity.setServerState(ImNotServerState.SHUTTING_DOWN);

            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
                player.kick("Server shutting down!");
            });

            MinecraftServer.stopCleanly();
        }));
    }
}
