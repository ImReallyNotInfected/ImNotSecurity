package org.imnotasecurity.internal.Admin.AdminCommands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.Player;
import org.imnotasecurity.api.Admin.ImNotAdmin;
import org.imnotasecurity.api.Database.ImNotDataProfile;
import org.imnotasecurity.api.Permission.Permission;
import org.imnotasecurity.internal.Database.SecDataProfile;

public class StopRawCommand extends Command {
    public StopRawCommand() {
        super("stopserver","terminateserver");


        setDefaultExecutor((sender, context) -> {
            if (sender instanceof Player player) {
                player.sendMessage("/stop");
            }
        });

        addSyntax((sender, context) -> {
            if (sender instanceof Player plr) {
                SecDataProfile secDataProfile = ImNotDataProfile.getDataProfileFromPlayer(plr);

                if (secDataProfile==null || secDataProfile.getPermission().getLevel() < Permission.CO_OWNER.getLevel()) {
                    plr.sendMessage(Component.text("No Permission! You must be a Co-Owner, or an Owner").color(NamedTextColor.RED));
                    return;
                }
            }

            ImNotAdmin.stopServerSmart(sender, false);
        });
    }
}
