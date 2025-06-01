package su226.creeperconfetti.mixin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import su226.creeperconfetti.Config;
import su226.creeperconfetti.ModClient;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin {
  @Shadow int currentFuseTime;
  @Shadow int fuseTime;
  @Shadow int explosionRadius;

  @Inject(at = @At("INVOKE"), method = "tick()V")
  void tick(CallbackInfo info) {
    CreeperEntity that = (CreeperEntity)(Object)this;
    int fuseTime = this.fuseTime - (that.getWorld().isClient() ? 2 : 1);
    if (!that.isAlive() || this.currentFuseTime < fuseTime) {
      return;
    }
    Random rand = new Random(that.getUuid().getMostSignificantBits());
    if (rand.nextDouble() < Config.chance) {
      Vec3d pos = that.getPos();
      boolean charged = that.isCharged();
      if (that.getWorld().isClient()) {
        if (rand.nextDouble() < Config.soundChance) {
          // Use CONFETTI sound
          that.getWorld().playSound(
              null, // Entity source
              pos.x, pos.y, pos.z, // Position
              ModClient.CONFETTI, // Sound event
              SoundCategory.HOSTILE, // Category
              2.0F, 1.0F // Volume and pitch
          );
        }
        // Play the firework twinkle sound
        that.getWorld().playSound(
            null, // Entity source
            pos.x, pos.y, pos.z, // Position
            SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, // Sound event
            SoundCategory.HOSTILE, // Category
            1.0F, 1.0F // Volume and pitch
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
          that.getWorld().playSound(
              null, // Entity source
              pos.x, pos.y, pos.z, // Position
              SoundEvents.ENTITY_GENERIC_EXPLODE, // Sound event
              SoundCategory.HOSTILE, // Category
              2.0F, 0.5F // Volume and pitch
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
      } else {
        // Server-side logic
        // Create a fake explosion with no block destruction
        that.getWorld().createExplosion(
            that, // Entity causing explosion
            pos.x, pos.y, pos.z, // Position
            0, // Power (0 for no destruction)
            false, // Create fire?
            World.ExplosionSourceType.MOB // Explosion source type
        );
        that.discard();
      }
    }
  }

  NbtCompound generateTag(byte type) {
    Random rand = new Random();
    int[] list = new int[rand.nextInt(3) + 6];
    list[0] = 0xE67E22;
    list[1] = 0x00E0FF;
    list[2] = 0x0FFF00;
    for (int i = 3; i < list.length; i++) {
      list[i] = rand.nextInt(0x1000000);
    }
    NbtCompound fireworkTag = new NbtCompound();
    fireworkTag.putIntArray("Colors", list);
    fireworkTag.putBoolean("Flicker", true);
    fireworkTag.putByte("Type", type);
    NbtList nbttaglist = new NbtList();
    nbttaglist.add(fireworkTag);
    NbtCompound fireworkItemTag = new NbtCompound();
    fireworkItemTag.put("Explosions", nbttaglist);
    return fireworkItemTag;
  }
}
