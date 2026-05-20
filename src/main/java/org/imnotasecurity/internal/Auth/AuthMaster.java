package org.imnotasecurity.internal.Auth;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerConfigCustomClickEvent;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.tag.Tag;
import org.imnotasecurity.api.Database.ImNotDataProfile;
import org.imnotasecurity.api.Database.ImNotPlayerType;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.api.Properties.AbstractProperty;
import org.imnotasecurity.internal.Database.SecDataProfile;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;

import static net.kyori.adventure.text.format.TextColor.color;

public class AuthMaster {
    public static final Tag<Boolean> STILL_IN_LOGIN = Tag.Boolean("STILL_IN_LOGIN");
    private static final Map<UUID, Phaser> playerLoginPhasers = new ConcurrentHashMap<>();
    private static final Map<UUID, Phaser> playerEmailPhasers = new ConcurrentHashMap<>();

    public static void attemptLoginPlayer(Player player, SecDataProfile secDataProfile, String password) {
        Phaser phaser = playerLoginPhasers.getOrDefault(player.getUuid(),null);
        AbstractProperty property = ImNotSecurity.getProperty();
        if (!player.hasTag(STILL_IN_LOGIN)) {
            //something fundamentally wrong
            return;
        }

        if (phaser == null || phaser.isTerminated()) {
            player.kick(Component.text("There is something wrong! Please try again.").color(NamedTextColor.RED));
            return;
        }

        if (secDataProfile.getPassword().isEmpty()) {
            //REGISTER TIME
            //check if password less than 20, longer than 3
            if (password.length() > 20) {
                player.kick(Component.text(
                        switch (property.getLanguage()) {
                            case VIETNAMESE -> "Mật khẩu quá dài vượt 20 chữ!";
                            case null, default -> "Password must not be more than 20 characters!";
                        },NamedTextColor.RED));
                return;
            }

            if (password.length() < 3) {
                player.kick(Component.text(
                        switch (property.getLanguage()) {
                            case VIETNAMESE -> "Mật khẩu quá ngắn, dưới 3 chữ!";
                            case null, default -> "Password must not be less than 3 characters!";
                        },NamedTextColor.RED));
                return;
            }

            //DONE
            secDataProfile.setPassword(password);
            phaser.arriveAndDeregister();
        } else {
            //LOGIN TIME
            if (!password.equals(secDataProfile.getPassword())) {
                player.kick(Component.text(
                        switch (property.getLanguage()) {
                            case VIETNAMESE -> "Sai Mật Khẩu!";
                            case null, default -> "Wrong Password! If you believe you forgot your Password, please later check the Recovery button.";
                        },NamedTextColor.RED));
                return;
            }

            //go
            phaser.arriveAndDeregister();
        }
    }

    public static boolean authPlayer(Player player, SecDataProfile secDataProfile) {
        AbstractProperty property = ImNotSecurity.getProperty();
        //check if is banned
        Instant now = Instant.now();
        Duration durationCheck = Duration.between(secDataProfile.getBanDate(), now);
        if (durationCheck.toSeconds() < secDataProfile.getBanDurationSeconds()) {
            //banned
            Duration duration = Duration.between(secDataProfile.getBanDate().plusSeconds(secDataProfile.getBanDurationSeconds()),now);
            Component banMessage = Component.text("You have been banned for the server.").color(NamedTextColor.RED);
            banMessage = banMessage.appendNewline().append(Component.empty().style(Style.empty()));
            banMessage = banMessage.append(Component.text("Ban ends in : ",NamedTextColor.WHITE).append(Component.text(-duration.toDays()+" days "+
                    -duration.toHoursPart()+" hours "+-duration.toMinutesPart()+" minutes",NamedTextColor.RED)).append(Component.text(".",NamedTextColor.RED)));
            banMessage = banMessage.appendNewline().append(Component.empty().style(Style.empty()));
            banMessage = banMessage.append(Component.text("For the following reason : ",NamedTextColor.WHITE).append(Component.text(secDataProfile.getBanReason(),NamedTextColor.RED)));
            banMessage = banMessage.appendNewline().append(Component.empty().style(Style.empty()));
            banMessage = banMessage.append(Component.text("If you believe you are falsely banned, you can appeal at : ", NamedTextColor.WHITE));
            banMessage = banMessage.appendNewline().append(Component.empty().style(Style.empty()));
            banMessage = banMessage.append(Component.text(property.getWebsite(),NamedTextColor.YELLOW));

            player.kick(banMessage);
            return false;
        }
        //end



        if (property.getDoAuthLoginForCracked() && !secDataProfile.getPlayerType().equals(ImNotPlayerType.PREMIUM)) {
            //login
            player.setTag(STILL_IN_LOGIN,true);

            //now check if login or register
            if (secDataProfile.getPassword().equalsIgnoreCase("")) {
                //register! Has not login before
                player.showDialog(AuthGUIs.getRegisterGUI());

                //yield
                CompletableFuture<Boolean> succFuture = CompletableFuture.supplyAsync(() -> {
                   //now wait for login
                    Phaser phaser = new Phaser(1);
                    playerLoginPhasers.put(player.getUuid(),phaser);

                    phaser.register();

                    phaser.awaitAdvance(phaser.arrive());

                    return true;
                });

                boolean succ;

                try {succ = succFuture.get(property.getLoginLimitTicks()*50, TimeUnit.MILLISECONDS); }
                catch (TimeoutException e) {
                    player.kick(Component.text(
                            switch (property.getLanguage()) {
                                case VIETNAMESE -> "Bạn kẹt ở phần đăng ký/nhập quá lâu!";
                                case null, default -> "You were stuck for too long!";
                            },NamedTextColor.RED));

                    succ = false;}
                catch (Exception e) {
                    player.kick(Component.text("Error!").color(NamedTextColor.RED));
                    succ = false;
                }

                if (succ) {playerLoginPhasers.remove(player.getUuid());}
                else if (!player.isOnline()) {
                    //player is already kicked out by login event handlers
                    return false;
                }

                return succ;
            } else {
                //login!
                player.showDialog(AuthGUIs.getLoginGUI());

                //yield
                CompletableFuture<Boolean> succFuture = CompletableFuture.supplyAsync(() -> {
                    //now wait for login
                    Phaser phaser = new Phaser(1);
                    playerLoginPhasers.put(player.getUuid(),phaser);
                    phaser.register();
                    phaser.awaitAdvance(phaser.arrive());

                    return true;
                });

                boolean succ;

                try {succ = succFuture.get(property.getLoginLimitTicks()*50, TimeUnit.MILLISECONDS); }
                catch (TimeoutException e) {
                    player.kick(Component.text(
                            switch (property.getLanguage()) {
                                case VIETNAMESE -> "Bạn kẹt ở phần đăng ký quá lâu!";
                                case null, default -> "You were stuck at login for too long!";
                            },NamedTextColor.RED));

                    succ = false;}
                catch (Exception e) {
                    player.kick(Component.text("Error!").color(NamedTextColor.RED));
                    succ = false;
                }

                if (succ) {playerLoginPhasers.remove(player.getUuid());}
                else if (!player.isOnline()) {
                    //player is already kicked out by login event handlers
                    return false;
                }

                return succ;
            }
        } else {
//            //let them go
//            player.sendMessage(Component.text(
//                    switch (property.getLanguage()) {
//                        case VIETNAMESE -> "+ Bạn có acc Premium r, ko cần login nữa!";
//                        case null, default -> "+ Since you are on an official, you don't need to login.";
//                    }
//            ).color(NamedTextColor.YELLOW));

            return true;
        }
    }

    public static void init() {
        GlobalEventHandler eventHandler = MinecraftServer.getGlobalEventHandler();

        eventHandler.addListener(PlayerDisconnectEvent.class, playerDisconnectEvent -> {
            Player player = playerDisconnectEvent.getPlayer();
            playerLoginPhasers.remove(player.getUuid());
            playerEmailPhasers.remove(player.getUuid());
        });

        eventHandler.addListener(PlayerConfigCustomClickEvent.class, event -> {
            AbstractProperty property = ImNotSecurity.getProperty();
           Player player = event.getPlayer();
           SecDataProfile profile = ImNotDataProfile.getDataProfileFromPlayer(player);

           if (profile==null || event.getPayload()==null) {
               return;
           }

           if (event.getPayload() instanceof CompoundBinaryTag compoundBinaryTag) {
               Key key = event.getKey();
               if (key.equals(Key.key("register"))) {
                   //REGISTER
                   String password = compoundBinaryTag.getString("password_register");
                   String confirmPassword = compoundBinaryTag.getString("confirm_password_register");

                   if (!confirmPassword.equals(password)) {
                       //not same!
                       player.kick(Component.text(
                               switch (property.getLanguage()) {
                                   case VIETNAMESE -> "Vui lòng nhập lại mật khẩu đúng chính xác!";
                                   case null, default -> "Confirm password not equal to password!";
                               }
                       ).color(NamedTextColor.RED));
                       return;
                   }

                   AuthMaster.attemptLoginPlayer(player,profile,password);
               } else if (key.equals(Key.key("login"))) {
                   //LOGIN
                   String password = compoundBinaryTag.getString("password_login");
                   AuthMaster.attemptLoginPlayer(player,profile,password);
               }
           }
        });
    }
}
