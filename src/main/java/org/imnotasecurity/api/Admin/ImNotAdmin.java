package org.imnotasecurity.api.Admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import net.minestom.server.timer.TaskSchedule;
import org.imnotasecurity.api.Database.ImNotDataProfile;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.api.ImNotServerState;
import org.imnotasecurity.internal.Database.SecDataProfile;

import java.time.Instant;

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
        SecDataProfile profile = ImNotDataProfile.getDataProfileFromPlayer(target);
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

    public static void stopServerSmart(CommandSender doer, boolean kickEveryone) {
        ImNotSecurity.setServerState(ImNotServerState.SHUTTING_DOWN);
        //kick everyone

        if (kickEveryone) {
            MinecraftServer.getConnectionManager().getOnlinePlayers().forEach(player -> {
                try {
                    player.kick("Server shutting down!");
                } catch (Exception e) {
                    System.out.println("---------------------------------");
                    System.out.println(e.getMessage());
                }
            });
        }


        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (kickEveryone) {
                ImNotSecurity.shutdownTasks.awaitAdvance(ImNotSecurity.shutdownTasks.arrive());
            }

            MinecraftServer.stopCleanly();
            System.exit(1);
        }).delay(TaskSchedule.tick(2)).schedule(); //wait before the disconnect event registers

    }
}
