package org.imnotasecurity.internal.Admin.AdminCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import org.imnotasecurity.api.Admin.ImNotAdmin;
import org.imnotasecurity.api.ImNotSecurity;
import org.imnotasecurity.api.Permission.Permission;
import org.imnotasecurity.internal.Permission.PermissionCommands.PermissionCommand;

import java.util.Arrays;

public class KickCommand extends Command {
    public KickCommand() {
        super("kick");

        var targetArg = ArgumentType.Entity("target").onlyPlayers(true).singleEntity(true);
        var additionReasonArg = ArgumentType.StringArray("reason");
        var property = ImNotSecurity.getProperty();

        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("/kick <player> <addition_reason>");
            }
        });

        addSyntax((sender, context) -> {
            var target = context.get(targetArg).findFirstPlayer(sender);
            if (target == null) {return;}

            if (sender instanceof Player plr) {
                if (!PermissionCommand.checkOperationDoable(plr,target, Permission.TRIAL_MOD)) {
                    return;
                }

                plr.sendMessage(Component.text(
                        switch (property.getLanguage()) {
                            case VIETNAMESE -> "Đã thành công đuổi "+target.getUsername()+"!";
                            case null, default -> "Successfully kicked "+target.getUsername()+"!";
                        }
                ).color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC));
            }

            ImNotAdmin.kickPlayer(target, sender);
        },targetArg);

        addSyntax((sender, context) -> {
            var target = context.get(targetArg).findFirstPlayer(sender);
            if (target == null) {return;}
            String[] reason = context.get(additionReasonArg);

            if (sender instanceof Player plr) {
                if (!PermissionCommand.checkOperationDoable(plr,target, Permission.TRIAL_MOD)) {
                    return;
                }

                plr.sendMessage(Component.text(
                        switch (property.getLanguage()) {
                            case VIETNAMESE -> "Đã thành công đuổi "+target.getUsername()+" vì lí do : "+ Arrays.toString(reason) +"!";
                            case null, default -> "Successfully kicked "+target.getUsername()+" for the reason : "+ Arrays.toString(reason) +"!";
                        }
                ).color(NamedTextColor.GREEN).decorate(TextDecoration.ITALIC));
            }

            ImNotAdmin.kickPlayer(target, Arrays.toString(reason), sender);
        },targetArg,additionReasonArg);
    }
}
