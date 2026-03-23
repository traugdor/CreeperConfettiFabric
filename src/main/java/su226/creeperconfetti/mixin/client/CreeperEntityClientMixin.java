package su226.creeperconfetti.mixin.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.particle.ParticleEffect;
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
    
    // CRITICAL: Only run on client side to avoid thread safety issues
    if (!that.getEntityWorld().isClient()) {
      return;
    }
    
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
      
      Vec3d pos = new Vec3d(that.getX(), that.getY(), that.getZ());
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
              new PositionedSoundInstance(ModSounds.CONFETTI, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F, net.minecraft.util.math.random.Random.create(), pos.x, pos.y, pos.z)
          );
          
          // Always play the firework twinkle sound
          client.getSoundManager().play(
              new PositionedSoundInstance(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, net.minecraft.sound.SoundCategory.BLOCKS, 1.0F, 1.0F, net.minecraft.util.math.random.Random.create(), pos.x, pos.y, pos.z)
          );
          
          // Only play explosion sound for charged creepers
          if (charged) {
            client.getSoundManager().play(
                new PositionedSoundInstance(SoundEvents.ENTITY_FIREWORK_ROCKET_BLAST, net.minecraft.sound.SoundCategory.BLOCKS, 0.5F, 1.0F, net.minecraft.util.math.random.Random.create(), pos.x, pos.y, pos.z)
            );
          }
        }
        
        // Spawn firework particles
        for (int i = 0; i < 50; i++) {
          double offsetX = rand.nextGaussian() * 0.15;
          double offsetY = rand.nextGaussian() * 0.15;
          double offsetZ = rand.nextGaussian() * 0.15;
          
          ((ClientWorldAccessor)world).invokeAddParticle(
              pos.x, pos.y + 0.5, pos.z, // position
              offsetX, offsetY, // velocity X and Y only
              (ParticleEffect)ParticleTypes.FIREWORK
          );
        }
        
        // Add more particles for charged creepers
        if (charged) {
          // Add flash particles for charged creepers
          for (int i = 0; i < 30; i++) {
            double offsetX = rand.nextGaussian() * 0.1;
            double offsetY = rand.nextGaussian() * 0.1;
            double offsetZ = rand.nextGaussian() * 0.1;
            
            ((ClientWorldAccessor)world).invokeAddParticle(
                pos.x, pos.y + 0.5, pos.z, // position
                offsetX, offsetY, // velocity X and Y only
                (ParticleEffect)ParticleTypes.EXPLOSION_EMITTER
            );
          }
        }
      }
    }
  }
}
