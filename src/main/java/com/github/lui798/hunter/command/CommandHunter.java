package com.github.lui798.hunter.command;

import com.github.lui798.hunter.Hunter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.StringUtil;

import java.util.*;

public class CommandHunter implements TabExecutor, Listener {
    private Set<UUID> hunted;
    private HashMap<UUID, UUID> hunterTracking;
    private boolean running = false;

    public CommandHunter() {
        this.hunted = new HashSet<>();
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (!running) return;
        Player player = event.getPlayer();

        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            if (!this.hunted.contains(player.getUniqueId()) && event.hasItem() && event.getMaterial() == Material.COMPASS) {
                setCompassTracking(player);
            }

        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        if (!running) return;
        if (!this.hunted.contains(event.getEntity().getUniqueId())) {
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
                if (hunted.contains(event.getPlayer().getUniqueId())) {
                    player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 60000000, 0, true, false));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 4, true, false));
                }
                else {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                    setCompassTracking(player);
                }

            }
        }.runTaskLater(Hunter.instance, 10);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!running) return;

        Player player = event.getPlayer();
        if (!this.hunted.contains(event.getPlayer().getUniqueId()) || !event.getPlayer().getInventory().contains(new ItemStack(Material.COMPASS))) {
            new BukkitRunnable() {
                public void run() {
                    player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                }
            }.runTaskLater(Hunter.instance, 10);

            setCompassTracking(player);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (!this.hunted.contains(event.getPlayer().getUniqueId()) && event.getItemDrop().getItemStack().getType() == Material.COMPASS)
            event.setCancelled(true);
    }

    public void setCompassTracking(Player player) {
        Player nearest = null;
        double distance = Double.MAX_VALUE;
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.equals(player) || !onlinePlayer.getWorld().equals(player.getWorld()) || !this.hunted.contains(onlinePlayer.getUniqueId()))
                continue;
            double distanceSquared = onlinePlayer.getLocation().distanceSquared(player.getLocation());
            if (distanceSquared < distance) {
                distance = distanceSquared;
                nearest = onlinePlayer;
            }
        }
        if (nearest == null) {
            player.sendMessage(ChatColor.RED + "No players to track!");
            return;
        }
        player.setCompassTarget(nearest.getLocation());
        player.sendMessage(ChatColor.GREEN + "Compass is now tracking " + nearest.getName() + ".");
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
            running = true;

            for (Player p : sender.getServer().getOnlinePlayers()) {
                if (p != null) {
                    if (this.hunted.contains(p.getUniqueId())) {
                        p.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 60000000, 0, true, false));
                        p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 4, true, false));
                    }
                    else {
                        p.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                        setCompassTracking(p);
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

            for (Player p : sender.getServer().getOnlinePlayers()) {
                if (p != null) {
                    if (this.hunted.contains(p.getUniqueId())) {
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
            Player player = sender.getServer().getPlayer(args[1]);
            this.hunted.add(player.getUniqueId());
            sender.sendMessage(ChatColor.GREEN + player.getDisplayName() + " is now being hunted.");
            return true;
        }
        else if (args[0].equalsIgnoreCase("remove")) {
            Player player = sender.getServer().getPlayer(args[1]);
            this.hunted.remove(player.getUniqueId());
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
