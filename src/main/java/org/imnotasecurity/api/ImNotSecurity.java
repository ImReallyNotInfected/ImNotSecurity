package org.imnotasecurity.api;

import net.minestom.server.MinecraftServer;
import org.imnotasecurity.api.Properties.AbstractProperty;
import org.imnotasecurity.internal.Admin.AdminMaster;
import org.imnotasecurity.internal.Auth.AuthMaster;
import org.imnotasecurity.internal.Database.DatabaseMaster;
import org.imnotasecurity.internal.Permission.PermissionMaster;

import java.util.Scanner;
import java.util.concurrent.Phaser;

public class ImNotSecurity {
    private static AbstractProperty property;
    public static AbstractProperty getProperty() {
        return property;
    }

    private static ImNotServerState serverState = ImNotServerState.RUNNING;
    public static ImNotServerState getServerState() {return serverState;}
    public static void setServerState(ImNotServerState serverState) {
        ImNotSecurity.serverState = serverState;
    }
    public static final Phaser shutdownTasks = new Phaser(1);

    public static void init(AbstractProperty newProperty) {
        property = newProperty;

        // Start the console listener in a daemon thread
        Thread consoleThread = new Thread(() -> {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String command = scanner.nextLine();
                MinecraftServer.getCommandManager().executeServerCommand(command);
            }
        });
        consoleThread.setDaemon(true);
        consoleThread.start();

        //init
        DatabaseMaster.init();
        AuthMaster.init();
        PermissionMaster.init();
        AdminMaster.init();
    }
}
