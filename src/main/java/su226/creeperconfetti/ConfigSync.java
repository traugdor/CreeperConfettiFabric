package su226.creeperconfetti;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

/**
 * Handles config synchronization between server and client
 */
public class ConfigSync {
    /**
     * Custom payload for config synchronization
     */
    public record ConfigSyncPayload(float chance, float damage, float soundChance) implements CustomPayload {
        public static final Identifier ID_IDENTIFIER = Identifier.of(CreeperConfetti.MODID, "config_sync");
        public static final CustomPayload.Id<ConfigSyncPayload> ID = new CustomPayload.Id<>(ID_IDENTIFIER);
        
        // Create a codec for our payload using ofStatic method
        public static final PacketCodec<PacketByteBuf, ConfigSyncPayload> CODEC = PacketCodec.ofStatic(
            // Encoder
            (PacketEncoder<PacketByteBuf, ConfigSyncPayload>) (buf, payload) -> {
                buf.writeFloat(payload.chance());
                buf.writeFloat(payload.damage());
                buf.writeFloat(payload.soundChance());
            },
            // Decoder
            (PacketDecoder<PacketByteBuf, ConfigSyncPayload>) buf -> {
                float chance = buf.readFloat();
                float damage = buf.readFloat();
                float soundChance = buf.readFloat();
                return new ConfigSyncPayload(chance, damage, soundChance);
            }
        );
        
        @Override
        public Id<? extends CustomPayload> getId() {
            return ID;
        }
    }
    
    /**
     * Register the server-side networking handlers
     */
    public static void registerServer() {
        // Register our payload type for server-to-client communication
        PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.ID, ConfigSyncPayload.CODEC);
        
        // Register event for when a player joins the server
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            // Send config to the player that just joined
            sendConfigToPlayer(handler.getPlayer());
        });
    }

    /**
     * Register the client-side networking handlers
     */
    @Environment(EnvType.CLIENT)
    public static void registerClient() {
        // The payload type is registered on both client and server during initialization
        // No need to register it again here as it would cause a duplicate registration error
        
        // Register a receiver for config sync packets from the server
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, (payload, context) -> {
            // We need to schedule this on the main client thread
            context.client().execute(() -> {
                // Update the client-side config
                Config.chance = payload.chance();
                Config.damage = payload.damage();
                Config.soundChance = payload.soundChance();
                
                ConsoleLogger.LOGGER.info("Received config from server: chance={}, damage={}, soundChance={}", 
                                        payload.chance(), payload.damage(), payload.soundChance());
            });
        });
    }

    /**
     * Send the current config to all connected players
     * @param server The Minecraft server instance
     */
    public static void syncConfigToAll(MinecraftServer server) {
        ConfigSyncPayload payload = new ConfigSyncPayload(Config.chance, Config.damage, Config.soundChance);
        for (ServerPlayerEntity player : PlayerLookup.all(server)) {
            ServerPlayNetworking.send(player, payload);
        }
        ConsoleLogger.LOGGER.info("Synced config to all players");
    }

    /**
     * Send the current config to a specific player
     * @param player The player to send the config to
     */
    public static void sendConfigToPlayer(ServerPlayerEntity player) {
        ConfigSyncPayload payload = new ConfigSyncPayload(Config.chance, Config.damage, Config.soundChance);
        ServerPlayNetworking.send(player, payload);
        ConsoleLogger.LOGGER.info("Synced config to player: {}", player.getName().getString());
    }
}
