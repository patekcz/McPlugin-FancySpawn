package org.patekcz.setSpawn;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * API třída pro plugin FancySpawn
 * Umožňuje komunikaci s pluginem z ostatních pluginů
 */
public class FancySpawnAPI {
    
    private final SetSpawn plugin;
    
    /**
     * Vytváří novou instanci API
     * @param plugin instance pluginu
     */
    public FancySpawnAPI(SetSpawn plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Získá aktuální lokaci spawnu
     * @return lokace spawnu nebo null, pokud spawn nebyl nastaven
     */
    public Location getSpawnLocation() {
        return plugin.getSpawnLocation();
    }
    
    /**
     * Teleportuje hráče na spawn
     * @param player hráč, který bude teleportován
     * @return true pokud teleportace proběhla úspěšně, jinak false
     */
    public boolean teleportPlayerToSpawn(Player player) {
        return plugin.teleportPlayerToSpawn(player);
    }
    
    /**
     * Ověří, zda je spawn nastaven
     * @return true pokud je spawn nastaven, jinak false
     */
    public boolean isSpawnSet() {
        return getSpawnLocation() != null;
    }
} 