package su226.creeperconfetti;

import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

/**
 * Common class for sound registry that works on both client and server
 */
public class ModSounds {
  // Sound identifiers
  public static final Identifier CONFETTI_ID = Identifier.of("creeperconfetti", "confetti");
  
  // Sound events - these are registered in the Mod initializer
  public static final SoundEvent CONFETTI = SoundEvent.of(CONFETTI_ID);
  
  // Register all sounds
  public static void register() {
    Registry.register(Registries.SOUND_EVENT, CONFETTI_ID, CONFETTI);
  }
}
