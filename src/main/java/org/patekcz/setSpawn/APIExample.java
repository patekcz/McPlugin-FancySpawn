package org.patekcz.setSpawn;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Ukázková třída demonstrující použití FancySpawnAPI v jiném pluginu
 * Toto je pouze příklad a není používán v hlavním pluginu
 */
public class APIExample {

    /**
     * Ukázka, jak použít FancySpawnAPI v jiném pluginu
     * @param yourPlugin instance vašeho pluginu
     * @param player hráč, který má být teleportován na spawn
     */
    public static void exampleUsage(JavaPlugin yourPlugin, Player player) {
        // Získání instance pluginu FancySpawn
        SetSpawn fancySpawnPlugin = SetSpawn.getInstance();
        
        // Pokud je plugin aktivní, můžeme použít jeho API
        if (fancySpawnPlugin != null) {
            // Získání instance API
            FancySpawnAPI api = fancySpawnPlugin.getAPI();
            
            // Kontrola, zda je spawn nastaven
            if (api.isSpawnSet()) {
                // Teleportace hráče na spawn
                boolean success = api.teleportPlayerToSpawn(player);
                
                if (success) {
                    yourPlugin.getLogger().info("Hráč " + player.getName() + " byl teleportován na spawn.");
                } else {
                    yourPlugin.getLogger().warning("Nepodařilo se teleportovat hráče " + player.getName() + " na spawn.");
                }
            } else {
                yourPlugin.getLogger().warning("Spawn není nastaven!");
            }
        } else {
            yourPlugin.getLogger().warning("Plugin FancySpawn není nainstalován nebo není aktivní!");
        }
    }
    
    /**
     * Alternativní metoda, která využívá služby Bukkitu pro získání pluginu
     * @param yourPlugin instance vašeho pluginu
     * @param player hráč, který má být teleportován na spawn
     */
    public static void alternativeUsage(JavaPlugin yourPlugin, Player player) {
        // Alternativní způsob získání instance pluginu
        SetSpawn fancySpawnPlugin = (SetSpawn) yourPlugin.getServer().getPluginManager().getPlugin("FancySetSpawn");
        
        if (fancySpawnPlugin != null && fancySpawnPlugin.isEnabled()) {
            FancySpawnAPI api = fancySpawnPlugin.getAPI();
            
            // Získání lokace spawnu
            if (api.isSpawnSet()) {
                // Teleportace hráče
                api.teleportPlayerToSpawn(player);
                yourPlugin.getLogger().info("Hráč " + player.getName() + " byl teleportován na spawn.");
            }
        }
    }
} 