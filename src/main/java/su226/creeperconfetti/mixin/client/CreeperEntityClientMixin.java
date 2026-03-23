package su226.creeperconfetti.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
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
import su226.creeperconfetti.CreeperExplosionTracker;
import su226.creeperconfetti.ModSounds;

import java.util.Random;

/**
 * Client-side mixin for CreeperEntity to handle particle effects
 */
@Environment(EnvType.CLIENT)
@Mixin(CreeperEntity.class)
public abstract class CreeperEntityClientMixin {
  @Shadow int currentFuseTime;
  @Shadow int fuseTime;

  @Inject(at = @At("INVOKE"), method = "tick()V")
  void tick(CallbackInfo info) {
    CreeperEntity that = (CreeperEntity)(Object)this;
    int fuseTime = this.fuseTime - 2; // Client-side timing
    
    // Only trigger when the creeper is about to explode
    if (!that.isAlive() || this.currentFuseTime < fuseTime) {
      return;
    }
    
    // Use a consistent seed for randomization based on the creeper's UUID
    Random rand = new Random(that.getUuid().getMostSignificantBits());
    
    // Only proceed with the chance configured
    if (rand.nextDouble() < Config.chance) {
      // Set the flag FIRST to catch the explosion sound
      CreeperExplosionTracker.setCreeperExplosion(that.getUuid());
      
      Vec3d pos = that.getPos();
      boolean charged = that.isCharged();
      
      ClientWorld world = MinecraftClient.getInstance().world;
      if (world != null) {
        // Only play sounds when the creeper is at the exact moment of explosion
        // This is when currentFuseTime equals fuseTime - 2 on the client
        if (this.currentFuseTime == fuseTime) {
          
          // Use MinecraftClient to play sounds directly
          MinecraftClient client = MinecraftClient.getInstance();
          
          // Always play the confetti sound
          client.getSoundManager().play(
              PositionedSoundInstance.master(ModSounds.CONFETTI, 1.0F)
          );
          
          // Always play the firework twinkle sound
          client.getSoundManager().play(
              PositionedSoundInstance.master(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0F)
          );
          
          // Only play explosion sound for charged creepers
          if (charged) {
            client.getSoundManager().play(
                PositionedSoundInstance.master(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, 0.5F)
            );
          }
        }
        
        // Spawn firework particles
        for (int i = 0; i < 50; i++) {
          double offsetX = rand.nextGaussian() * 0.15;
          double offsetY = rand.nextGaussian() * 0.15;
          double offsetZ = rand.nextGaussian() * 0.15;
          
          world.addParticleClient(
              ParticleTypes.FIREWORK,
              false, // force
              true, // alwaysSpawn
              pos.x, pos.y + 0.5, pos.z, // position
              offsetX, offsetY, offsetZ // velocity
          );
        }
        
        // Add more particles for charged creepers
        if (charged) {
          // Add flash particles for charged creepers
          for (int i = 0; i < 30; i++) {
            double offsetX = rand.nextGaussian() * 0.1;
            double offsetY = rand.nextGaussian() * 0.1;
            double offsetZ = rand.nextGaussian() * 0.1;
            
            world.addParticleClient(
                ParticleTypes.FLASH,
                false, // force
                true, // alwaysSpawn
                pos.x, pos.y + 0.5, pos.z, // position
                offsetX, offsetY, offsetZ // velocity
            );
          }
        }
      }
    }
  }
}
