package org.imnotasecurity.internal.Permission;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import org.imnotasecurity.internal.Permission.PermissionCommands.PermissionCommand;

public class PermissionMaster {
    public static void init() {
        CommandManager manager = MinecraftServer.getCommandManager();
        manager.register(new PermissionCommand());
    }
}
