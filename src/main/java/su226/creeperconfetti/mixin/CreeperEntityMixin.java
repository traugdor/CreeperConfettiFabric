package su226.creeperconfetti.mixin;

import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import su226.creeperconfetti.Config;

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
      // Client-side effects (particles and sounds) are now handled in the client-only mixin
      if (!that.getWorld().isClient()) {
        // Server-side logic
        // Create a fake explosion with no block destruction
        that.getWorld().createExplosion(
            that, // Entity causing explosion
            pos.x, pos.y, pos.z, // Position
            Config.damage, // Power
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
