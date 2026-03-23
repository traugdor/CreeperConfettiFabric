package su226.creeperconfetti.mixin.client;

import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.ParticleEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ClientWorld.class)
public interface ClientWorldAccessor {
    @Invoker("addParticle")
    void invokeAddParticle(double x, double y, double z, double velocityX, double velocityY, ParticleEffect particle);
}
