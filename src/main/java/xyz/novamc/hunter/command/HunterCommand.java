package xyz.novamc.hunter.command;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import xyz.novamc.hunter.HunterConfig;
import xyz.novamc.hunter.HunterPlugin;
import xyz.novamc.hunter.game.HunterGame;

import java.util.*;

public class HunterCommand implements TabExecutor {

    private final HashMap<String, Integer> argMap = new HashMap<>();
    private final HunterPlugin plugin;
    private final HunterGame game;

    public HunterCommand(HunterPlugin plugin) {
        this.plugin = plugin;
        this.game = plugin.getGame();

        argMap.put("add", 2);
        argMap.put("remove", 2);
        argMap.put("start", 1);
        argMap.put("stop", 1);
        argMap.put("reload", 1);
        argMap.put("help", 1);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + plugin.getDescription().getName()+ " v" + plugin.getDescription().getVersion() + " by "
                    + plugin.getDescription().getAuthors().toString().replace("[", "").replace("]", ""));
            return true;
        }

        Player player = Bukkit.getPlayer(sender.getName());

        if (args[0].equalsIgnoreCase("help") && args.length == argMap.get("help")) {
            sender.sendMessage(ChatColor.GREEN + "Commands for Hunter");
            helpMessage(sender, ChatColor.YELLOW);
        }
        else if (args[0].equalsIgnoreCase("reload") && args.length == argMap.get("reload")) {
            HunterConfig.reload();
            sender.sendMessage(ChatColor.YELLOW + "Successfully reloaded the config file for Hunter!");
        }
        else if (args[0].equalsIgnoreCase("add") && args.length == argMap.get("add")) {
            if (game.isRunning()) {
                sender.sendMessage(ChatColor.RED + "Hunter game is already running, stop it to add players.");
                return true;
            }

            Player specifiedPlayer = Bukkit.getPlayer(args[1]);
            if (game.isHunter(specifiedPlayer)) {
                sender.sendMessage(ChatColor.RED + specifiedPlayer.getName() + " is already a hunter!");
            }
            else {
                game.addHunter(specifiedPlayer);
                sender.sendMessage(ChatColor.GREEN + specifiedPlayer.getName() + " is now a hunter.");
            }
        }
        else if (args[0].equalsIgnoreCase("remove") && args.length == argMap.get("remove")) {
            if (game.isRunning()) {
                sender.sendMessage(ChatColor.RED + "Hunter is already running, stop it to remove players.");
                return true;
            }

            Player specifiedPlayer = Bukkit.getPlayer(args[1]);
            if (!game.isHunter(specifiedPlayer)) {
                sender.sendMessage(ChatColor.RED + specifiedPlayer.getName() + " is not a hunter.");
            }
            else {
                game.removeHunter(specifiedPlayer);
                sender.sendMessage(ChatColor.GREEN + specifiedPlayer.getName() + " is no longer a hunter.");
            }
        }
        else if (args[0].equalsIgnoreCase("start") && args.length == argMap.get("start")) {
            if (game.isRunning()) {
                sender.sendMessage(ChatColor.RED + "Hunter is already running!");
                return true;
            }

            game.startGame();
            sender.sendMessage(ChatColor.GREEN + "The hunter match has started!");
        }
        else if (args[0].equalsIgnoreCase("stop") && args.length == argMap.get("stop")) {
            if (!game.isRunning()) {
                sender.sendMessage(ChatColor.RED + "Hunter is already stopped!");
                return true;
            }

            game.stopGame();
            sender.sendMessage(ChatColor.RED + "The hunter match has stopped!");
        }
        else {
            helpMessage(sender, ChatColor.RED);
        }

        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        final List<String> completions = new ArrayList<>();

        if (args.length > 0) {
            if (args.length == 1 && !argMap.containsKey(args[0])) {
                StringUtil.copyPartialMatches(args[0], argMap.keySet(), completions);
            }
            else if (args.length != 1 && argMap.containsKey(args[0]) && args.length == argMap.get(args[0])) {
                completions.addAll(onlinePlayers());
            }
        }

        Collections.sort(completions);
        return completions;
    }

    private List<String> onlinePlayers() {
        List<String> playerNames = new ArrayList<>();

        for (Player player : plugin.getServer().getOnlinePlayers()) {
            playerNames.add(player.getName());
        }

        return playerNames;
    }

    private void helpMessage(CommandSender sender, ChatColor color) {
        sender.sendMessage(color + "/hunter help");
        sender.sendMessage(color + "/hunter add <name>");
        sender.sendMessage(color + "/hunter remove <name>");
        sender.sendMessage(color + "/hunter start");
        sender.sendMessage(color + "/hunter stop");
        sender.sendMessage(color + "/hunter reload");
    }
}
