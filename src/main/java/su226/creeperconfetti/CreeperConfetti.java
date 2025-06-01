package su226.creeperconfetti;

import net.fabricmc.api.ModInitializer;
import net.minecraft.server.MinecraftServer;

public class CreeperConfetti implements ModInitializer {
    public static final String MODID = "creeperconfetti";
    private static MinecraftServer server;
    
    @Override
    public void onInitialize() {
        Config.deserialize();
        ModSounds.register();
        ConfigSync.registerServer();
    }
    
    /**
     * Sets the current server instance
     * @param server The MinecraftServer instance
     */
    public static void setServer(MinecraftServer server) {
        CreeperConfetti.server = server;
    }
    
    /**
     * Gets the current server instance
     * @return The MinecraftServer instance
     */
    public static MinecraftServer getServer() {
        return server;
    }
    
    /**
     * Syncs the current config to all connected players
     */
    public static void syncConfig() {
        if (server != null && server.isRunning()) {
            ConfigSync.syncConfigToAll(server);
        }
    }
}
