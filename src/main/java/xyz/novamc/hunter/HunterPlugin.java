package xyz.novamc.hunter;

import xyz.novamc.hunter.command.HunterCommand;
import org.bukkit.plugin.java.JavaPlugin;
import xyz.novamc.hunter.game.HunterGame;

public class HunterPlugin extends JavaPlugin {

    private HunterGame hunterGame;

    @Override
    public void onEnable() {
        HunterConfig.init(this);
        hunterGame = new HunterGame(this);

        this.getServer().getPluginManager().registerEvents(hunterGame.getListener(), this);
        this.getCommand("hunter").setExecutor(new HunterCommand(this));
    }

    public HunterGame getGame() {
        return hunterGame;
    }
}
