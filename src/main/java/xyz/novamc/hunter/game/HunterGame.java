package xyz.novamc.hunter.game;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.novamc.hunter.HunterConfig;
import xyz.novamc.hunter.HunterPlugin;
import xyz.novamc.hunter.player.HunterList;
import xyz.novamc.hunter.player.HunterPlayer;
import xyz.novamc.hunter.player.PlayerState;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class HunterGame {

    private final JavaPlugin plugin;
    private final HunterListener listener;

    private final HunterList hunters = new HunterList();
    private final List<UUID> speedRunners = new ArrayList<>();
    private boolean isRunning;

    public HunterGame(HunterPlugin plugin) {
        this.plugin = plugin;
        this.listener = new HunterListener(this);
        this.isRunning = false;

        setLobbyBorder(Bukkit.getWorlds().get(0));
    }

    public JavaPlugin getPlugin() {
        return plugin;
    }

    public HunterListener getListener() {
        return listener;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean running) {
        isRunning = running;
    }

    public void removeSpeedRunner(Player player) {
        if (isRunning && isInMatch(player) && !isHunter(player)) {
            speedRunners.remove(player.getUniqueId());
        }
    }

    public void addHunter(Player player) {
        // Make sure hunter is not running and that player is not already a hunter
        if (!isRunning && !isHunter(player)) {
            hunters.add(new HunterPlayer(player.getUniqueId()));
        }
    }

    public void removeHunter(Player player) {
        // Make sure hunter is not running and that the player is a hunter
        if (!isRunning && isHunter(player)) {
            hunters.remove(hunters.get(player));
        }
    }

    public boolean isHunter(Player player) {
        return hunters.containsPlayer(player);
    }

    public HunterList getHunters() {
        return hunters;
    }

    public boolean isInMatch(Player player) {
        return hunters.containsPlayer(player) || speedRunners.contains(player.getUniqueId());
    }

    public void setPlayerState(Player player, PlayerState state) {
        switch (state) {
            case BEFORE_MATCH:
                // Set to adventure and give saturation
                player.setGameMode(GameMode.ADVENTURE);
                player.setInvulnerable(true);
                player.addPotionEffect(new PotionEffect(PotionEffectType.SATURATION, 60000000, 2, true, false));
                // Remove effects player might have had in-match
                player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
                player.removePotionEffect(PotionEffectType.REGENERATION);
                break;
            case IN_MATCH:
                // Set to survival and remove pre-match saturation
                player.setGameMode(GameMode.SURVIVAL);
                player.setInvulnerable(false);
                player.removePotionEffect(PotionEffectType.SATURATION);
                break;
            case IN_MATCH_SPEEDRUNNER:
                // Health boost effect for the speedrunners
                player.addPotionEffect(new PotionEffect(PotionEffectType.HEALTH_BOOST, 60000000, 0, true, false));
                player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20, 4, true, false));
                break;
            case SPECTATOR:
                // Set to spectator and remove saturation
                player.setGameMode(GameMode.SPECTATOR);
                player.setInvulnerable(false);
                player.removePotionEffect(PotionEffectType.SATURATION);
                // Remove effects player might have had in-match
                player.removePotionEffect(PotionEffectType.HEALTH_BOOST);
                player.removePotionEffect(PotionEffectType.REGENERATION);
        }
    }

    public Player trackPlayer(Player hunter) {
        // If auto tracking is enabled track nearest player
        if (HunterConfig.autoTracking) {
            Player nearest = null;
            double distance = Double.MAX_VALUE;

            List<Player> onlinePlayers = new ArrayList<>(Bukkit.getOnlinePlayers());
            // Remove hunters from online player list for tracking
            onlinePlayers.removeAll(hunters.getPlayerList());

            // Checks every player and keeps the closest one
            for (Player player : onlinePlayers) {
                if (player.getWorld().equals(hunter.getWorld())) {
                    double distanceSquared = player.getLocation().distanceSquared(player.getLocation());
                    if (distanceSquared < distance) {
                        distance = distanceSquared;
                        nearest = player;
                    }
                }
            }

            // Set hunter's compass to location of the nearest player
            if (nearest != null) {
                hunter.setCompassTarget(nearest.getLocation());
            }
            return nearest;
        }
        // If selective tracking is enabled
        else {
            if (speedRunners.size() == 0) {
                return null;
            }

            int currentIteration = hunters.get(hunter).getPlayerIteration();
            Player trackedPlayer = Bukkit.getPlayer(speedRunners.get(currentIteration));

            // If selected player is not online, fail
            if (!Bukkit.getOnlinePlayers().contains(trackedPlayer)) {
                return trackedPlayer;
            }

            // If selected player and hunter are in the same world, set compass to location of selected player
            if (hunter.getWorld().equals(trackedPlayer.getWorld())) {
                hunter.setCompassTarget(trackedPlayer.getLocation());
            }

            return trackedPlayer;
        }
    }

    public Player cycleTrackedPlayer(Player hunter) {
        if (speedRunners.size() == 0) {
            return null;
        }

        int maxIterations = speedRunners.size() - 1;
        int currentIteration = hunters.get(hunter).getPlayerIteration();

        // Cycle iteration of selected player with wrap around
        if (currentIteration < maxIterations) {
            hunters.get(hunter).setPlayerIteration(currentIteration + 1);
        }
        else {
            hunters.get(hunter).setPlayerIteration(0);
        }

        return Bukkit.getPlayer(speedRunners.get(hunters.get(hunter).getPlayerIteration()));
    }

    public void startGame() {
        if (!isRunning) {
            setRunning(true);

            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player != null) {
                    setPlayerState(player, PlayerState.IN_MATCH);
                    player.getInventory().clear();

                    // If player is hunter freeze them
                    if (this.hunters.containsPlayer(player)) {
                        hunters.get(player).setFrozen(true);
                    }
                    // Give speedrunner effects
                    else {
                        speedRunners.add(player.getUniqueId());
                        setPlayerState(player, PlayerState.IN_MATCH_SPEEDRUNNER);
                    }
                }
            }

            new BukkitRunnable() {
                @Override
                public void run() {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        if (player != null) {
                            // Give compass and unfreeze hunters after wait time
                            if (hunters.containsPlayer(player)) {
                                hunters.get(player).setPlayerIteration(0);
                                player.getInventory().addItem(new ItemStack(Material.COMPASS, 1));
                                trackPlayer(player);
                                hunters.get(player).setFrozen(false);
                            }
                        }
                    }
                    Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + String.valueOf(HunterConfig.waitTime) + " seconds are up, hunters have been released!");
                }
            }.runTaskLater(plugin, 20 * HunterConfig.waitTime);

            setMatchBorder(Bukkit.getWorlds().get(0));

            Bukkit.getServer().broadcastMessage(ChatColor.YELLOW + "Hunters will be released in " + HunterConfig.waitTime + " seconds!");
        }
    }

    public void stopGame() {
        if (isRunning) {
            setRunning(false);

            for (Player player : Bukkit.getOnlinePlayers()) {
                setPlayerState(player, PlayerState.BEFORE_MATCH);

                if (this.hunters.containsPlayer(player)) {
                    player.getInventory().removeItem(new ItemStack(Material.COMPASS));
                    player.setCompassTarget(player.getWorld().getSpawnLocation());
                }

                player.teleport(Bukkit.getWorlds().get(0).getSpawnLocation());
                player.getInventory().clear();
            }

            hunters.clear();
            speedRunners.clear();

            setLobbyBorder(Bukkit.getWorlds().get(0));
        }
    }

    public void setLobbyBorder(World world) {
        world.getWorldBorder().setSize(HunterConfig.lobbyBorderSize);
        world.getWorldBorder().setCenter(world.getSpawnLocation());
    }

    public void setMatchBorder(World world) {
        world.getWorldBorder().setSize(HunterConfig.matchBorderSize);
        world.getWorldBorder().setCenter(world.getSpawnLocation());
    }
}
