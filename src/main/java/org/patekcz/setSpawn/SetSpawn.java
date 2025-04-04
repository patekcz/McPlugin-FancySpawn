package org.patekcz.setSpawn;

import java.io.File;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent; // Import pro BungeeCord API (pro actionbar)
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable; // Pro odložené spuštění kódu

import net.md_5.bungee.api.chat.TextComponent;

public final class SetSpawn extends JavaPlugin implements Listener {

    private File spawnConfigFile;
    private FileConfiguration spawnConfig;
    private FileConfiguration config;
    private static SetSpawn instance;
    private FancySpawnAPI api;

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("Plugin SetSpawn se spouští...");
        saveDefaultConfig();
        config = getConfig();

        spawnConfigFile = new File(getDataFolder(), "spawnconfig.yml");
        if (!spawnConfigFile.exists()) {
            try {
                spawnConfigFile.createNewFile();
            } catch (IOException e) {
                getLogger().severe("Nastala chyba při vytváření souboru spawnconfig.yml.");
                e.printStackTrace();
            }
        }
        spawnConfig = YamlConfiguration.loadConfiguration(spawnConfigFile);
        
        // Zkontrolujeme, zda existuje spawn
        if (spawnConfig.getString("spawn.world") == null) {
            // Nastavíme výchozí spawn, pokud žádný není definován
            setDefaultSpawn();
        } else {
            // Ověříme, zda existuje svět s daným jménem
            String worldName = spawnConfig.getString("spawn.world");
            if (Bukkit.getWorld(worldName) == null) {
                getLogger().warning("Svět '" + worldName + "' nelze najít! Nastavuji výchozí spawn.");
                setDefaultSpawn();
            }
        }
        
        // Inicializace API
        api = new FancySpawnAPI(this);
        
        getLogger().info("Plugin SetSpawn byl úspěšně načten.");
        getLogger().info("FancySpawnAPI je dostupné pro použití v jiných pluginech.");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        instance = null;
    }

    /**
     * Nastaví výchozí spawn na spawn point prvního světa
     */
    private void setDefaultSpawn() {
        if (Bukkit.getWorlds().isEmpty()) {
            getLogger().warning("Nelze nastavit výchozí spawn, neexistují žádné světy!");
            return;
        }
        
        World defaultWorld = Bukkit.getWorlds().get(0);
        Location worldSpawn = defaultWorld.getSpawnLocation();
        
        // Uložíme výchozí spawn
        spawnConfig.set("spawn.world", defaultWorld.getName());
        spawnConfig.set("spawn.x", worldSpawn.getX());
        spawnConfig.set("spawn.y", worldSpawn.getY());
        spawnConfig.set("spawn.z", worldSpawn.getZ());
        spawnConfig.set("spawn.yaw", 0.0f);
        spawnConfig.set("spawn.pitch", 0.0f);
        
        try {
            spawnConfig.save(spawnConfigFile);
            getLogger().info("Výchozí spawn byl nastaven na spawn lokaci světa: " + defaultWorld.getName());
        } catch (IOException e) {
            getLogger().severe("Nepodařilo se uložit výchozí spawn!");
            e.printStackTrace();
        }
    }

    // Metoda pro zobrazení zpráv podle nastavení v konfiguraci
    private void sendMessageToPlayer(Player player, String messageKey) {
        String message = ChatColor.translateAlternateColorCodes('&', config.getString(messageKey, ""));
        String location = config.getString("messages.messageLocation", "chat").toLowerCase();

        switch (location) {
            case "actionbar":
                // Použijeme ActionBar zprávy
                player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, new TextComponent(message));
                break;
            case "chat":
            default:
                player.sendMessage(message);
                break;
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        
        // Použijeme BukkitRunnable pro odložené spuštění teleportace
        // To zajistí, že se hráč správně načte do světa před teleportací
        new BukkitRunnable() {
            @Override
            public void run() {
                // Přímé použití API metody pro teleportaci na spawn
                if (teleportPlayerToSpawn(player)) {
                    // Teleportace úspěšná, přehrajeme zvuk připojení
                    String playerJoinSound = config.getString("sounds.playerJoin");
                    player.playSound(player.getLocation(), playerJoinSound, 1.0f, 1.0f);
                } else {
                    // Spawn není nastaven, pokusíme se ho nastavit znovu
                    setDefaultSpawn();
                    
                    // Zkusíme teleportaci znovu s krátkým zpožděním
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            if (teleportPlayerToSpawn(player)) {
                                String playerJoinSound = config.getString("sounds.playerJoin");
                                player.playSound(player.getLocation(), playerJoinSound, 1.0f, 1.0f);
                            } else {
                                getLogger().warning("Nepodařilo se teleportovat hráče na spawn: " + player.getName());
                            }
                        }
                    }.runTaskLater(instance, 20L); // 1 sekunda zpoždění (20 tiků)
                }
            }
        }.runTaskLater(this, 40L); // 2 sekundové zpoždění (40 tiků) pro zajištění načtení hráče
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("setspawn")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                Location location = player.getLocation();
                String worldName = location.getWorld().getName();
                double x = location.getX();
                double y = location.getY();
                double z = location.getZ();
                float yaw = location.getYaw();
                float pitch = location.getPitch();

                spawnConfig.set("spawn.world", worldName);
                spawnConfig.set("spawn.x", x);
                spawnConfig.set("spawn.y", y);
                spawnConfig.set("spawn.z", z);
                spawnConfig.set("spawn.yaw", yaw);
                spawnConfig.set("spawn.pitch", pitch);
                try {
                    spawnConfig.save(spawnConfigFile);
                } catch (Exception e) {
                    sendMessageToPlayer(player, "messages.spawnNotSet");
                    e.printStackTrace();
                }

                sendMessageToPlayer(player, "messages.spawnSet");

                String setSpawnSound = config.getString("sounds.setSpawn");
                float volume = (float) config.getDouble("soundSettings.volume");
                float pitchSetting = (float) config.getDouble("soundSettings.pitch");
                player.playSound(player.getLocation(), setSpawnSound, volume, pitchSetting);
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.commandOnlyPlayer")));
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("spawn")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                String worldName = spawnConfig.getString("spawn.world");
                double x = spawnConfig.getDouble("spawn.x");
                double y = spawnConfig.getDouble("spawn.y");
                double z = spawnConfig.getDouble("spawn.z");
                float yaw = (float) spawnConfig.getDouble("spawn.yaw");
                float pitch = (float) spawnConfig.getDouble("spawn.pitch");

                if (worldName != null) {
                    Location spawnLocation = new Location(Bukkit.getWorld(worldName), x, y, z, yaw, pitch);
                    player.teleport(spawnLocation);
                    sendMessageToPlayer(player, "messages.teleportToSpawn");

                    String teleportSound = config.getString("sounds.teleport");
                    float volume = (float) config.getDouble("soundSettings.volume");
                    float pitchSetting = (float) config.getDouble("soundSettings.pitch");
                    player.playSound(player.getLocation(), teleportSound, volume, pitchSetting);
                } else {
                    sendMessageToPlayer(player, "messages.spawnNotSet");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', config.getString("messages.commandOnlyPlayer")));
                return true;
            }
        }
        return false;
    }
    
    /**
     * Získá instanci hlavní třídy pluginu
     * @return instance hlavní třídy pluginu
     */
    public static SetSpawn getInstance() {
        return instance;
    }
    
    /**
     * Získá instanci FancySpawnAPI pro použití v jiných pluginech
     * @return instance API
     */
    public FancySpawnAPI getAPI() {
        return api;
    }
    
    /**
     * Získá lokaci spawnu
     * @return lokace spawnu nebo null, pokud spawn nebyl nastaven
     */
    public Location getSpawnLocation() {
        String worldName = spawnConfig.getString("spawn.world");
        if (worldName == null) {
            return null;
        }
        
        // Ověříme, zda existuje svět s daným jménem
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            getLogger().warning("Svět '" + worldName + "' není načten nebo neexistuje!");
            return null;
        }
        
        double x = spawnConfig.getDouble("spawn.x");
        double y = spawnConfig.getDouble("spawn.y");
        double z = spawnConfig.getDouble("spawn.z");
        float yaw = (float) spawnConfig.getDouble("spawn.yaw");
        float pitch = (float) spawnConfig.getDouble("spawn.pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    /**
     * Teleportuje hráče na spawn a přehraje zvuk
     * @param player hráč, který bude teleportován
     * @return true pokud teleportace proběhla úspěšně, jinak false
     */
    public boolean teleportPlayerToSpawn(Player player) {
        Location spawnLocation = getSpawnLocation();
        if (spawnLocation == null) {
            return false;
        }
        
        // Zajistíme, že cílový svět je načten
        World targetWorld = spawnLocation.getWorld();
        if (targetWorld == null) {
            getLogger().warning("Nelze teleportovat hráče " + player.getName() + " - cílový svět neexistuje!");
            return false;
        }
        
        // Zkontrolujeme, zda je teleportace úspěšná
        boolean success = player.teleport(spawnLocation);
        if (success) {
            sendMessageToPlayer(player, "messages.teleportToSpawn");
            
            String teleportSound = config.getString("sounds.teleport");
            float volume = (float) config.getDouble("soundSettings.volume");
            float pitchSetting = (float) config.getDouble("soundSettings.pitch");
            player.playSound(player.getLocation(), teleportSound, volume, pitchSetting);
        } else {
            getLogger().warning("Teleportace hráče " + player.getName() + " selhala!");
        }
        
        return success;
    }
}
