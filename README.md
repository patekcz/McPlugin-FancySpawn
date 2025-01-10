# FancySpawn Plugin for Minecraft

This plugin allows players to set a spawn point and teleport to it.

## Features

* **Set spawn:** Players can set the spawn to their current position using the `/setspawn` command.
* **Teleport to spawn:** Players can teleport to the spawn using the `/spawn` command.
* **Sound effects:** The plugin plays sound effects during teleportation and spawn setting.
* **Configuration management:** The plugin uses configuration files to manage messages and sounds.

## Installation

1. Download the plugin from GitHub.
2. Place the plugin files into the `plugins` folder of your Minecraft server.
3. Restart the server.

## Usage

* **Set spawn:** Use the `/setspawn` command.
* **Teleport to spawn:** Use the `/spawn` command.

## Configuration

The plugin configuration can be found in the `config.yml` file. Here you can modify messages and sound effects.

## Configuration Examples

```yaml
messages:
  spawnSet: "&aSpawn has been set to your current position!"
  teleportToSpawn: "&aYou have been teleported to the spawn!"
  spawnNotSet: "&cSpawn position is not set."
  commandOnlyPlayer: "&cThis command can only be used by players."
sounds:
  teleport: "minecraft:entity.experience_orb.pickup"
  setSpawn: "minecraft:entity.experience_orb.pickup"
  playerJoin: "minecraft:entity.player.levelup"
soundSettings:
  volume: 1.0
  pitch: 1.0
