package xyz.novamc.hunter.player;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public class HunterPlayerList extends ArrayList<HunterPlayer> {

    public boolean containsPlayer(Player player) {
        for (HunterPlayer p : this) {
            if (player.getUniqueId() == p.getUuid()) {
                return true;
            }
        }
        return false;
    }
}
