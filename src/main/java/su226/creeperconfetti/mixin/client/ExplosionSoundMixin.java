package su226.creeperconfetti.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su226.creeperconfetti.Config;

/**
 * Client-side mixin to cancel the original creeper explosion sound
 */
@Environment(EnvType.CLIENT)
@Mixin(SoundManager.class)
public class ExplosionSoundMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance sound, CallbackInfo ci) {
        // Check if this is the generic explosion sound by its ID string
        // In Minecraft 1.21.5, the explosion sound has the ID 'minecraft:entity.generic.explode'
        String soundId = sound.getId().toString();
        if (soundId.equals("minecraft:entity.generic.explode")) {
            // If our mod is active (chance > 0), cancel the original explosion sound
            if (Config.chance > 0) {
                // Cancel the original explosion sound
                ci.cancel();
            }
        }
    }
}
