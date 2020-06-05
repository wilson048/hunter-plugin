package xyz.novamc.hunter.player;

import java.util.UUID;

public class HunterPlayer {

    private final UUID uuid;
    private int playerIteration;
    private boolean isFrozen;

    public HunterPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    public void setPlayerIteration(int iteration) {
        playerIteration = iteration;
    }

    public int getPlayerIteration() {
        return playerIteration;
    }

    public void setFrozen(boolean value) {
        isFrozen = value;
    }

    public boolean isFrozen() {
        return isFrozen;
    }

    public UUID getUuid() {
        return uuid;
    }
}
