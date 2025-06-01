package su226.creeperconfetti.mixin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
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
          // Use CONFETTI (uppercase) and check if we're on client side
          that.getWorld().playSound(null, new BlockPos((int)pos.x, (int)pos.y, (int)pos.z), ModClient.CONFETTI, SoundCategory.HOSTILE, 2F, 1F);
        }
        that.getWorld().playSound(null, new BlockPos((int)pos.x, (int)pos.y, (int)pos.z), SoundEvents.ENTITY_FIREWORK_ROCKET_TWINKLE, SoundCategory.HOSTILE, 1F, 1F);
        // Note: addFireworkParticle is no longer available in this form in 1.21.5
        // Using client-side particle methods would require a different approach
        if (charged) {
          // Same issue with firework particles for charged creepers
        }
      } else {
        if (Config.damage != 0) {
          that.getWorld().createExplosion(that, pos.x, pos.y, pos.z, Config.damage * (charged ? 2f : 1f) * this.explosionRadius, World.ExplosionSourceType.MOB);
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
