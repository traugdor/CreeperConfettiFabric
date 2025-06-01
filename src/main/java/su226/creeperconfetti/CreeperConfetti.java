package su226.creeperconfetti;

import net.fabricmc.api.ModInitializer;

public class CreeperConfetti implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register sounds in the common initialization
        ModSounds.register();
    }
}
