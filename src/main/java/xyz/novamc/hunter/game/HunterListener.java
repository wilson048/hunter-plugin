package xyz.novamc.hunter.game;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.novamc.hunter.HunterConfig;
import xyz.novamc.hunter.player.PlayerState;

public class HunterListener implements Listener {

    private final HunterGame game;

    public HunterListener(HunterGame game) {
        this.game = game;
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!game.isRunning()) return;
        Player player = event.getPlayer();

        if (game.isHunter(player)) {
            if (event.hasItem() && event.getMaterial() == Material.COMPASS) {
                if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    Player trackedPlayer = game.trackPlayer(player);

                    if (HunterConfig.autoTracking) {
                        if (trackedPlayer == null) {
                            player.sendMessage(ChatColor.RED + "No players to track in your dimension!");
                        }
                        else {
                            player.sendMessage(ChatColor.GREEN + "Compass is now tracking " + trackedPlayer.getName() + ".");
                        }
                    }
                    else {
                        if (trackedPlayer == null) {
                            player.sendMessage(ChatColor.RED + "No players to track!");
                        }
                        // If selective tracking and selected player is not online alert player
                        else if (!Bukkit.getOnlinePlayers().contains(trackedPlayer)) {
                            player.sendMessage(ChatColor.RED + trackedPlayer.getName() + " is not currently in-game!");
                        }
                        else if (!player.getWorld().equals(trackedPlayer.getWorld())) {
                            player.sendMessage(ChatColor.RED + trackedPlayer.getName() + " is in another dimension!");
                        }
                        else {
                            player.sendMessage(ChatColor.GREEN + "Updated compass for " + trackedPlayer.getName() + ".");
                        }
                    }
                }
                else if (!HunterConfig.autoTracking && (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK)) {
                    Player trackedPlayer = game.cycleTrackedPlayer(player);
                    if (trackedPlayer != null) {
                        player.sendMessage(ChatColor.YELLOW + "Compass is now tracking " + trackedPlayer.getName() + ".");
                        game.trackPlayer(player);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (game.isRunning() && game.isHunter(player) && game.getHunters().get(player).isFrozen()) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!game.isRunning()) return;
        Player player = event.getEntity();

        if (game.isHunter(player)) {
            if (player.getInventory().contains(new ItemStack(Material.COMPASS))) {
                event.getDrops().removeIf(itemStack -> itemStack.getType() == Material.COMPASS);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        if (!game.isRunning()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    game.setPlayerState(player, PlayerState.BEFORE_MATCH);
                }
            }.runTaskLater(game.getPlugin(), 10);
        }
        else {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (!game.isHunter(player)) {
                        game.setPlayerState(player, PlayerState.SPECTATOR);
                        game.removeSpeedRunner(player);
                    }
                    else {
                        player.getInventory().addItem(new ItemStack(Material.COMPASS));
                        game.trackPlayer(player);
                    }
                }
            }.runTaskLater(game.getPlugin(), 10);
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!game.isRunning()) {
            game.setPlayerState(player, PlayerState.BEFORE_MATCH);

        }
        else {
            if (!game.isInMatch(player)) {
                game.setPlayerState(player, PlayerState.SPECTATOR);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!game.isRunning()) return;
        Player player = event.getPlayer();

        if (game.isHunter(player) && event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            event.setCancelled(true);
        }
    }
}
