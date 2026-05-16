package org.imnotasecurity.internal.Permission.PermissionCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.imnotasecurity.api.Database.ImNotDataProfile;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.api.Permission.ImNotPermission;
import org.imnotasecurity.api.Permission.Permission;
import org.imnotasecurity.api.Properties.AbstractProperty;
import org.imnotasecurity.internal.Database.DataProfile;

public class PermissionCommand extends Command {
    public static boolean checkOperationDoable(Player doer, Player target, Permission minimum) {
        AbstractProperty property = ImNotSecurity.getProperty();
        DataProfile doerProfile = ImNotDataProfile.getDataProfileFromPlayer(doer);
        DataProfile targetProfile = ImNotDataProfile.getDataProfileFromPlayer(target);
        if (doerProfile==null || targetProfile==null) {
            return false;
        }

        if (doerProfile.getPermission().getLevel() < minimum.getLevel()) {
            doer.sendMessage(Component.text(
                    switch (property.getLanguage()) {
                        case VIETNAMESE -> "Bạn không có quyền này!";
                        case null, default -> "You don't have enough permission!";
                    }
            ).color(NamedTextColor.RED));
            return false;
        }

        if (doerProfile.getPermission().getLevel() <= targetProfile.getPermission().getLevel()) {
            doer.sendMessage(Component.text(
                    switch (property.getLanguage()) {
                        case VIETNAMESE -> "Bạn không thể thực hiện với người có quyền cao hơn!!";
                        case null, default -> "That person's permission is too high!";
                    }
            ).color(NamedTextColor.RED));
            return false;
        }

        return true;
    }

    public static boolean checkOperationDoable(Player doer, Player target) {
        return checkOperationDoable(doer,target,Permission.ADMIN);
    }

    public PermissionCommand() {
        super("permission", "Permission","imnothub:permission");

        var plrArg = ArgumentType.Entity("player").onlyPlayers(true).singleEntity(true);
        var operation = ArgumentType.Enum("operation",PermissionOperation.class);
        var permission = ArgumentType.Enum("permission", Permission.class);


        //Operations, check if player is admin/higher
        addSyntax((sender, context) -> {
            PermissionOperation operationAction = context.get(operation);
            if (!operationAction.equals(PermissionOperation.REMOVE)) {return;}

            var target = context.get(plrArg).findFirstPlayer(sender);
            if (target==null) {return;}
            var targetProfile = ImNotDataProfile.getDataProfileFromPlayer(target);
            if (targetProfile==null) {return;}

            if (sender instanceof Player player) if (!checkOperationDoable(player, target)) {
                return;
            }
            ImNotPermission.removePermission(target,targetProfile,sender);
        },operation,plrArg); //remove

        addSyntax((sender, context) -> {
            PermissionOperation operationAction = context.get(operation);
            Permission newPermission = context.get(permission);
            if (!operationAction.equals(PermissionOperation.PROMOTE)) {return;}

            var target = context.get(plrArg).findFirstPlayer(sender);
            if (target==null) {return;}
            var targetProfile = ImNotDataProfile.getDataProfileFromPlayer(target);
            if (targetProfile==null) {return;}

            if (sender instanceof Player player) if (!checkOperationDoable(player, target)) {
                return;
            }
            ImNotPermission.promotePermission(target,targetProfile,newPermission,sender);
        },operation,plrArg,permission); //promote
    }
}
