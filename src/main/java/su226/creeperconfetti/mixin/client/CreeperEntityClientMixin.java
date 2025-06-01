package su226.creeperconfetti.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import su226.creeperconfetti.Config;
import su226.creeperconfetti.ModClient;

import java.util.Random;

@Mixin(CreeperEntity.class)
public class CreeperEntityClientMixin {
    @Shadow
    private int currentFuseTime;
    
    @Shadow
    private int fuseTime;
    
    @Inject(at = @At("INVOKE"), method = "tick()V")
    void tick(CallbackInfo info) {
        CreeperEntity that = (CreeperEntity)(Object)this;
        int fuseTime = this.fuseTime - 2; // Client-side decrement is 2
        
        // Only proceed if we're on the client and the creeper is about to explode
        if (!that.getWorld().isClient() || !that.isAlive() || this.currentFuseTime < fuseTime) {
            return;
        }
        
        Random rand = new Random(that.getUuid().getMostSignificantBits());
        if (rand.nextDouble() < Config.chance) {
            Vec3d pos = that.getPos();
            boolean charged = that.isCharged();
            
            // Play sounds using MinecraftClient's sound manager
            if (rand.nextDouble() < Config.soundChance) {
                MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(ModClient.CONFETTI, 1.0F)
                );
            }
            
            // Play the firework twinkle sound
            MinecraftClient.getInstance().getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0F)
            );
            
            // Spawn firework particles
            for (int i = 0; i < 50; i++) {
                double offsetX = rand.nextGaussian();
                double offsetY = rand.nextGaussian();
                double offsetZ = rand.nextGaussian();
                that.getWorld().addParticleClient(
                    ParticleTypes.FIREWORK,
                    pos.x, pos.y + 0.5, pos.z,
                    offsetX * 0.15, offsetY * 0.15, offsetZ * 0.15
                );
            }
            
            // Add more particles and sounds for charged creepers
            if (charged) {
                // Play explosion sound for charged creepers
                MinecraftClient.getInstance().getSoundManager().play(
                    PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXPLODE, 2.0F)
                );
                
                // Add flash particles for charged creepers
                for (int i = 0; i < 30; i++) {
                    double offsetX = rand.nextGaussian();
                    double offsetY = rand.nextGaussian();
                    double offsetZ = rand.nextGaussian();
                    that.getWorld().addParticleClient(
                        ParticleTypes.FLASH,
                        pos.x, pos.y + 2.5, pos.z,
                        offsetX * 0.1, offsetY * 0.1, offsetZ * 0.1
                    );
                }
            }
        }
    }
}
