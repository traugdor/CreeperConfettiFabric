package su226.creeperconfetti;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class ModClient implements ClientModInitializer {
  @Override
  public void onInitializeClient() {
    // The sounds are now registered in ModSounds
    ConfigSync.registerClient();
  }
}
