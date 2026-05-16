package org.imnotasecurity.api.Admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.imnotasecurity.api.Database.ImNotDataProfile;
import org.imnotasecurity.internal.Database.DataProfile;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

public class ImNotAdmin {
    public static void kickPlayer(Player target, String reason, CommandSender doer) {
        target.kick(Component.text("You got kicked for the following reason : ").append(
                Component.text(reason, NamedTextColor.RED)
        ));
    }

    public static void kickPlayer(Player target, CommandSender doer) {
        target.kick(Component.text("You got kicked!").color(NamedTextColor.RED));
    }

    public static void banPlayer(Player target, String reason, long seconds, CommandSender doer) {
        DataProfile profile = ImNotDataProfile.getDataProfileFromPlayer(target);
        if (profile!=null) {
            var nowData = Instant.now();
            profile.setBanDate(nowData);
            profile.setBanDurationSeconds(seconds);
            profile.setBanReason(reason);

            target.kick(Component.text("Something wrong has happened! Please try rejoining"));
        }
    }

    public static void banPlayer(Player target, long seconds, CommandSender doer) {
        banPlayer(target,"",seconds,doer);
    }
}
