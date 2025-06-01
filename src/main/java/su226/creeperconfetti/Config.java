package su226.creeperconfetti;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class Config {
  
  public static float chance = .1f;
  public static float damage = 0f;
  public static float soundChance = .05f;

  static final Path configPath = FabricLoader.getInstance().getConfigDir().resolve("creeperconfetti.properties");

  static float parseRangedFloat(String s, float min, float max, float def) {
    if (s == null) {
      return def;
    }
    try {
      return Math.min(Math.max(Float.parseFloat(s), min), max);
    } catch (NumberFormatException e) {
      return def;
    }
  }

  static void serialize() {
    Properties prop = new Properties();
    prop.setProperty("chance", Float.toString(chance));
    prop.setProperty("damage", Float.toString(damage));
    prop.setProperty("soundChance", Float.toString(soundChance));
    
    try {
      // Create parent directories if they don't exist
      Files.createDirectories(configPath.getParent());
      
      // Write the config file
      OutputStream s = Files.newOutputStream(configPath);
      prop.store(s, "Creeper Confetti Config");
      s.close();
      ConsoleLogger.LOGGER.info("Saved CreeperConfetti config to {}", configPath);
      
      // Sync config to clients if we're on a server
      syncToClients();
    } catch (IOException e) {
      ConsoleLogger.LOGGER.warn("Failed to write config: {}", e.getMessage());
    }
  }
  
  /**
   * Synchronize config values to all connected clients if we're on a server
   */
  public static void syncToClients() {
    // Get the server instance (may be null if we're not on a server)
    net.minecraft.server.MinecraftServer server = CreeperConfetti.getServer();
    
    // Only sync if we're on a server with players
    if (server != null && !server.getPlayerManager().getPlayerList().isEmpty()) {
      ConsoleLogger.LOGGER.info("Synchronizing config to all connected clients");
      ConfigSync.syncConfigToAll(server);
    }
  }

  static void deserialize() {
    Properties prop = new Properties();
    boolean configExists = Files.exists(configPath);
    
    if (configExists) {
      try {
        InputStream s = Files.newInputStream(configPath);
        prop.load(s);
        s.close();
        chance = parseRangedFloat(prop.getProperty("chance"), 0f, 1f, .1f);
        damage = parseRangedFloat(prop.getProperty("damage"), 0f, 1f, 0f);
        soundChance = parseRangedFloat(prop.getProperty("soundChance"), 0f, 1f, .05f);
        ConsoleLogger.LOGGER.info("Loaded CreeperConfetti config from {}", configPath);
      } catch (IOException e) {
        ConsoleLogger.LOGGER.warn("Failed to read config: {}", e.getMessage());
      }
    } else {
      ConsoleLogger.LOGGER.info("Config file does not exist, creating with default values");
    }
    
    // Always serialize to ensure the config file exists
    Config.serialize();
  }
}
