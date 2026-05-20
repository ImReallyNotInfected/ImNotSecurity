package org.imnotasecurity.internal.Admin;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import org.imnotasecurity.internal.Admin.AdminCommands.BanCommand;
import org.imnotasecurity.internal.Admin.AdminCommands.KickCommand;
import org.imnotasecurity.internal.Admin.AdminCommands.StopCommand;

import java.time.Instant;

public class AdminMaster {
    public static void init() {
        CommandManager manager = MinecraftServer.getCommandManager();

        manager.register(new KickCommand());
        manager.register(new BanCommand());
        manager.register(new StopCommand());
    }
}
