package com.github.lui798.hunter.command;

import com.github.lui798.hunter.Hunter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import java.util.*;

/*   Variable Names
     CIT -> clientIterationTracking
 */

public class CommandHunter implements TabExecutor, Listener {
    // Creates a list of the hunted players
    private List<UUID> huntedPlayers;

    // Create a list for each client with an iteration HashMap<Client, 1>
    private HashMap<UUID, Integer> CIT;

    // Keep track if match is running
    private boolean running = false;

    public CommandHunter() {
        this.huntedPlayers = new ArrayList<>();
        this.CIT = new HashMap<>();
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!running) return;
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!this.huntedPlayers.contains(player.getUniqueId()) && event.hasItem() && event.getMaterial() == Material.COMPASS) {
                updateCompassTracking(player, false);
            }
        }
        else if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            if (!this.huntedPlayers.contains(player.getUniqueId()) && event.hasItem() && event.getMaterial() == Material.COMPASS) {
                changeSelectedPlayer(player);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!running) return;
        if (!this.huntedPlayers.contains(event.getEntity().getUniqueId())) {
            if (event.getEntity().getInventory().contains(new ItemStack(Material.COMPASS))) {
                event.getDrops().removeIf(item -> item.getType() == Material.COMPASS);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        if (!running) return;

        Player player = event.getPlayer();
        new BukkitRunnable() {
            public void run() {
                if (huntedPlayers.contains(event.getPlayer().getUniqueId())) {
                    player.setGameMode(GameMode.SPECTATOR);
                    huntedPlayers.remove(player.getUniqueId());
                }
                else {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                    updateCompassTracking(player, false);
                }

            }
        }.runTaskLater(Hunter.instance, 10);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (!running) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60000000, 2, true, false));
        }
        else {
            // Checks to see if the player joining is a already in the match, if they're NOT set gamemode to spectator
            if (!this.huntedPlayers.contains(player.getUniqueId()) && !this.CIT.containsKey(player.getUniqueId())) {
                player.setGameMode(GameMode.SPECTATOR);
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!running) return;

        if (!this.huntedPlayers.contains(event.getPlayer().getUniqueId()) && event.getItemDrop().getItemStack().getType() == Material.COMPASS)
            event.setCancelled(true);
    }


    public void changeSelectedPlayer(Player player) {
        int maxIterations = huntedPlayers.size() - 1;
        int currentIteration = CIT.get(player.getUniqueId());

        if (currentIteration < maxIterations) {
            CIT.put(player.getUniqueId(), currentIteration + 1);
        }
        else {
            CIT.put(player.getUniqueId(), 0);
        }

        updateCompassTracking(player, true);
    }

    public void updateCompassTracking(Player player, boolean fromChangeSelected) {
        int currentIteration = CIT.get(player.getUniqueId());
        Player huntedPlayer = Bukkit.getServer().getPlayer(huntedPlayers.get(currentIteration));

        if (!Bukkit.getOnlinePlayers().contains(huntedPlayer)) {
            player.sendMessage(ChatColor.RED + "The player you're trying to track is not currently in-game!");
            return;
        }

        if (player.getWorld().equals(huntedPlayer.getWorld())) {
            player.setCompassTarget(huntedPlayer.getLocation());
        }
        else {
            player.sendMessage(ChatColor.RED + "The player you're currently tracking is in another dimension.");
            return;
        }

        if (fromChangeSelected) {
            player.sendMessage(ChatColor.YELLOW + "Compass is now tracking " + huntedPlayer.getName() + "." + currentIteration);
        }
        else {
            player.sendMessage(ChatColor.GREEN + "Updated compass for " + huntedPlayer.getName() + "." + currentIteration);
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length < 1 || args.length > 2) {
            commandInvalid(sender);
            return false;
        }

        if (args[0].equalsIgnoreCase("start")) {
            if (running) {
                sender.sendMessage(ChatColor.RED + "The hunting has already running.");
                return true;
            }

            if (huntedPlayers.size() == 0) {
                sender.sendMessage(ChatColor.RED + "You must add hunted players before starting!");
                return true;
            }

            running = true;

            for (Player p : sender.getServer().getOnlinePlayers()) {
                if (p != null) {
                    p.setGameMode(GameMode.SURVIVAL);
                    p.removePotionEffect(PotionEffectType.SATURATION);

                    if (this.huntedPlayers.contains(p.getUniqueId())) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 60000000, 0, true, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 4, true, false));
                    }
                    else {
                        CIT.put(p.getUniqueId(), 0);
                        p.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                        updateCompassTracking(p, false);
                    }
                }
            }

            sender.sendMessage(ChatColor.GREEN + "The hunting has started.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("stop")) {
            if (!running) {
                sender.sendMessage(ChatColor.RED + "The hunting is already stopped.");
                return true;
            }

            running = false;
            huntedPlayers.clear();
            CIT.clear();

            for (Player p : sender.getServer().getOnlinePlayers()) {
                if (p != null) {
                    p.setGameMode(GameMode.ADVENTURE);
                    p.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60000000, 2, true, false));

                    if (this.huntedPlayers.contains(p.getUniqueId())) {
                        p.removePotionEffect(PotionEffectType.HEALTH_BOOST);
                        p.removePotionEffect(PotionEffectType.REGENERATION);
                    }
                    else {
                        p.getInventory().removeItem(new ItemStack(Material.COMPASS));
                        p.setCompassTarget(p.getWorld().getSpawnLocation());
                    }
                }
            }

            sender.sendMessage(ChatColor.RED + "The hunting has stopped.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("add")) {
            if (running) {
                sender.sendMessage(ChatColor.RED + "Hunter is already running, stop it to add players.");
                return true;
            }

            Player player = sender.getServer().getPlayer(args[1]);
            if (!this.huntedPlayers.contains(player.getUniqueId())) {
                this.huntedPlayers.add(player.getUniqueId());
                sender.sendMessage(ChatColor.GREEN + player.getDisplayName() + " is now being hunted.");
            }
            else {
                sender.sendMessage(ChatColor.RED + player.getDisplayName() + " is already being hunted!");
            }

            return true;
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            if (running) {
                sender.sendMessage(ChatColor.RED + "Hunter is already running, stop it to remove players.");
                return true;
            }

            Player player = sender.getServer().getPlayer(args[1]);
            this.huntedPlayers.remove(player.getUniqueId());
            player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
            player.removePotionEffect(PotionEffectType.REGENERATION);
            sender.sendMessage(ChatColor.GREEN + player.getDisplayName() + " is no longer being hunted.");
            return true;
        }
        else {
            commandInvalid(sender);
            return false;
        }
    }

    private void commandInvalid(CommandSender sender) {
        sender.sendMessage(ChatColor.RED + "Invalid usage.");
        sender.sendMessage(ChatColor.RED + "/hunter add <name>");
        sender.sendMessage(ChatColor.RED + "/hunter remove <name>");
        sender.sendMessage(ChatColor.RED + "/hunter start");
        sender.sendMessage(ChatColor.RED + "/hunter stop");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length != 1) return null;

        final List<String> firstSet = Arrays.asList("add", "remove", "start", "stop");

        final List<String> completions = new ArrayList<>();

        StringUtil.copyPartialMatches(args[0], firstSet, completions);

        Collections.sort(completions);
        return completions;
    }
}
