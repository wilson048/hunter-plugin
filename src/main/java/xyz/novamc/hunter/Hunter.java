package com.github.lui798.hunter;

import com.github.lui798.hunter.command.CommandHunter;
import org.bukkit.plugin.java.JavaPlugin;

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
