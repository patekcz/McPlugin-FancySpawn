package org.patekcz.setSpawn;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.ChatColor;

import net.md_5.bungee.api.chat.TextComponent; // Import pro BungeeCord API (pro actionbar)

import java.io.File;
import java.io.IOException;

public final class SetSpawn extends JavaPlugin implements Listener {

    private File spawnConfigFile;
    private FileConfiguration spawnConfig;
    private FileConfiguration config;

    @Override
    public void onEnable() {
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
        getLogger().info("Plugin SetSpawn byl úspěšně načten.");

        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
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
        Player player = event.getPlayer();
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
        }

        String playerJoinSound = config.getString("sounds.playerJoin");
        player.playSound(player.getLocation(), playerJoinSound, 1.0f, 1.0f);
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
}
