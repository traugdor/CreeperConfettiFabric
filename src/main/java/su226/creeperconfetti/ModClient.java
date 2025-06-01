package su226.creeperconfetti;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class ModClient implements ClientModInitializer {
  public static final Identifier CONFETTI_ID = Identifier.of("creeperconfetti", "confetti");
  public static final SoundEvent CONFETTI = SoundEvent.of(CONFETTI_ID);

  @Override
  public void onInitializeClient() {
    Registry.register(Registries.SOUND_EVENT, CONFETTI_ID, CONFETTI);
  }
}
