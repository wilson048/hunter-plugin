package xyz.novamc.hunter;

import xyz.novamc.hunter.old.CommandHunter;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Iterator;

public class Hunter extends JavaPlugin {
    public static Hunter instance;

    @Override
    public void onEnable() {
        instance = this;

        CommandHunter hunterCommand = new CommandHunter();
        this.getServer().getPluginManager().registerEvents(hunterCommand, this);
        this.getCommand("hunter").setExecutor(hunterCommand);
    }
}
