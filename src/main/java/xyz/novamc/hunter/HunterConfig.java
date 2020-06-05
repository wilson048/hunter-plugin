package xyz.novamc.hunter;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public class HunterConfig {

    private static JavaPlugin PLUGIN;
    private static FileConfiguration CONFIG;

    public static boolean autoTracking;
    public static int waitTime;
    public static double lobbyBorderSize;
    public static double matchBorderSize;

    public static void init(JavaPlugin plugin) {
        PLUGIN = plugin;
        CONFIG = plugin.getConfig();

        PLUGIN.saveDefaultConfig();
        loadValues();
    }

    private static void loadValues() {
        autoTracking = CONFIG.getBoolean("autoTracking");
        waitTime = CONFIG.getInt("waitTime");
        lobbyBorderSize = CONFIG.getDouble("lobbyBorderSize");
        matchBorderSize = CONFIG.getDouble("matchBorderSize");
    }

    public static void reload() {
        PLUGIN.reloadConfig();
        CONFIG = PLUGIN.getConfig();
        loadValues();
    }
}
