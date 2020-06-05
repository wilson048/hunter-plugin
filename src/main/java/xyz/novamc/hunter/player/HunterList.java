package xyz.novamc.hunter.player;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class HunterList extends ArrayList<HunterPlayer> {

    public boolean containsPlayer(Player player) {
        for (HunterPlayer p : this) {
            if (player.getUniqueId() == p.getUuid()) {
                return true;
            }
        }
        return false;
    }

    public HunterPlayer get(Player player) {
        for (HunterPlayer p : this) {
            if (player.getUniqueId() == p.getUuid()) {
                return p;
            }
        }
        return null;
    }

    public List<Player> getPlayerList() {
        List<Player> players = new ArrayList<>();
        for (HunterPlayer p : this) {
            players.add(Bukkit.getPlayer(p.getUuid()));
        }
        return players;
    }
}
