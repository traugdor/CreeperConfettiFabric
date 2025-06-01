package su226.creeperconfetti.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.PositionedSoundInstance;
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
          // Use CONFETTI sound with MinecraftClient's sound manager
          MinecraftClient.getInstance().getSoundManager().play(
              PositionedSoundInstance.master(ModClient.CONFETTI, 1.0F)
          );
        }
        // Play the firework twinkle sound
        MinecraftClient.getInstance().getSoundManager().play(
            PositionedSoundInstance.master(SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, 1.0F)
        );
        // Spawn firework particles using the correct method for 1.21.5
        for (int i = 0; i < 50; i++) {
          double offsetX = (rand.nextDouble() - 0.5) * 2;
          double offsetY = (rand.nextDouble() - 0.5) * 2;
          double offsetZ = (rand.nextDouble() - 0.5) * 2;
          // Use addParticleClient which is the correct method for 1.21.5
          that.getWorld().addParticleClient(
              ParticleTypes.FIREWORK,
              pos.x, pos.y + 0.5, pos.z,
              offsetX * 0.15, offsetY * 0.15, offsetZ * 0.15);
        }
        
        // Add more particles and sounds for charged creepers
        if (charged) {
          // Play explosion sound for charged creepers
          MinecraftClient.getInstance().getSoundManager().play(
              PositionedSoundInstance.master(SoundEvents.ENTITY_GENERIC_EXPLODE, 2.0F)
          );
          
          // Add flash particles for charged creepers
          for (int i = 0; i < 30; i++) {
            double offsetX = (rand.nextDouble() - 0.5) * 3;
            double offsetY = (rand.nextDouble() - 0.5) * 3;
            double offsetZ = (rand.nextDouble() - 0.5) * 3;
            that.getWorld().addParticleClient(
                ParticleTypes.FLASH,
                pos.x, pos.y + 2.5, pos.z,
                offsetX * 0.1, offsetY * 0.1, offsetZ * 0.1);
          }
        }
      } else {
        if (Config.damage != 0) {
          // For server-side, create explosion with proper damage
          float power = Config.damage * (charged ? 2f : 1f) * this.explosionRadius;
          // Use the correct createExplosion method with the proper parameters
          that.getWorld().createExplosion(
              that,                        // Entity source
              null,                        // DamageSource (null uses default)
              null,                        // Explosion behavior callback
              pos.x, pos.y, pos.z,          // Position
              power,                        // Power/radius
              false,                        // Create fire?
              World.ExplosionSourceType.MOB  // Explosion source type
          );
        }
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
