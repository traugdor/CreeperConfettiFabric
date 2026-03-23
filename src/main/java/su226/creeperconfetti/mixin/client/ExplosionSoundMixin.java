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
import su226.creeperconfetti.CreeperExplosionTracker;

/**
 * Client-side mixin to cancel the original creeper explosion sound
 */
@Environment(EnvType.CLIENT)
@Mixin(SoundManager.class)
public class ExplosionSoundMixin {
    @Inject(method = "play(Lnet/minecraft/client/sound/SoundInstance;)V", at = @At("HEAD"), cancellable = true)
    private void onPlay(SoundInstance sound, CallbackInfo ci) {
        String soundId = sound.getId().toString();
        if (soundId.equals("minecraft:entity.generic.explode")) {
            // Only cancel the explosion sound if it's from a creeper that's being handled by our mod
            if (CreeperExplosionTracker.isCreeperExploding() && Config.chance > 0) {
                ci.cancel();
            }
        }
    }
}
