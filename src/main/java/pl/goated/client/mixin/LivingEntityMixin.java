package pl.goated.client.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.impl.NoPushModule;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

	@Inject(method = "tickMovement", at = @At("HEAD"))
	private void onTickMovement(CallbackInfo ci) {
		if (!isLocalPlayer()) return;
		
		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) return;
		
		// Jeśli Push Strength = 0%, zerujemy velocity (brak wypychania)
		// Jeśli Push Strength = 100%, normalne wypychanie
		float multiplier = module.getPushMultiplier();
		
		if (multiplier < 1.0f) {
			LivingEntity self = (LivingEntity) (Object) this;
			Vec3d vel = self.getVelocity();
			
			// Zachowaj velocity Y (grawitacja), modyfikuj tylko X i Z
			self.setVelocity(
				vel.x * multiplier,
				vel.y,
				vel.z * multiplier
			);
		}
	}

	private boolean isLocalPlayer() {
		LivingEntity self = (LivingEntity) (Object) this;
		MinecraftClient mc = MinecraftClient.getInstance();
		return mc != null && mc.player != null && self.equals(mc.player);
	}

	private NoPushModule getNoPushModule() {
		if (GoatedClient.getInstance() == null) return null;
		if (GoatedClient.getInstance().getModuleManager() == null) return null;
		return (NoPushModule) GoatedClient.getInstance().getModuleManager().getModuleByName("NoPush");
	}
}
