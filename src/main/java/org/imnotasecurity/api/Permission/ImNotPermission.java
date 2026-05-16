package org.imnotasecurity.api.Permission;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.CommandSender;
import net.minestom.server.entity.Player;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.internal.Database.DataProfile;

public class ImNotPermission {
    public static void promotePermission(Player player, DataProfile profile, Permission newPermission, CommandSender doer) {
        profile.setPermission(newPermission);
        player.sendMessage(Component.text(switch (ImNotSecurity.getProperty().getLanguage()) {
            case VIETNAMESE -> "Bạn đã đc thăng chức đến "+ newPermission.toString()+"!";
            case null, default -> "You got your permission promoted to "+newPermission.toString()+"!";
        }).color(NamedTextColor.GREEN).decorate(TextDecoration.BOLD,TextDecoration.ITALIC));
    }

    public static void removePermission(Player player, DataProfile profile, CommandSender doer) {
        Permission oldPer = profile.getPermission();
        profile.setPermission(Permission.MEMBER);
        player.sendMessage(Component.text(switch (ImNotSecurity.getProperty().getLanguage()) {
            case VIETNAMESE -> "Bạn đã bị tước quyền "+ oldPer.toString()+"!";
            case null, default -> "You got your permission removed!";
        }).color(NamedTextColor.RED).decorate(TextDecoration.BOLD,TextDecoration.ITALIC));
    }

    public static boolean isHigherPermission(Permission first, Permission second) {
        return first.getLevel() > second.getLevel();
    }
}
