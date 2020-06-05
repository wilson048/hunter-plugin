package xyz.novamc.hunter.command;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import xyz.novamc.hunter.game.HunterGame;

public class HunterListener implements Listener {

    private final HunterGame game;

    public HunterListener(HunterGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {

    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {

    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {

    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {

    }
}
