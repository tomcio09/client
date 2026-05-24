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

@Mixin(value = LivingEntity.class, priority = 900)
public class LivingEntityMixin {

	// Hookujemy dokładnie w metodę odpowiedzialną za wypychanie z bloków
	// zamiast globalnego tickMovement - mniej podejrzane
	@Inject(
		method = "pushOutOfBlocks",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onPushOutOfBlocks(double x, double y, double z, CallbackInfo ci) {
		if (!isLocalPlayer()) return;

		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) return;

		float multiplier = module.getPushMultiplier();

		// Jeśli 0% - całkowicie anuluj wypychanie
		if (multiplier <= 0.0f) {
			ci.cancel();
			return;
		}

		// Jeśli między 0% a 100% - nie anulujemy całkowicie
		// tylko zmniejszamy w osobnym kroku przez velocity
		if (multiplier < 1.0f) {
			LivingEntity self = (LivingEntity) (Object) this;
			Vec3d before = self.getVelocity();
			
			// Pozwól metodzie normalnie się wykonać
			// ale po niej zmniejszymy efekt
			// (to robi drugi inject poniżej)
		}
	}

	@Inject(
		method = "pushOutOfBlocks",
		at = @At("RETURN"),
		cancellable = false
	)
	private void afterPushOutOfBlocks(double x, double y, double z, CallbackInfo ci) {
		if (!isLocalPlayer()) return;

		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) return;

		float multiplier = module.getPushMultiplier();

		// Tylko jeśli częściowe tłumienie (między 0% a 100%)
		if (multiplier > 0.0f && multiplier < 1.0f) {
			LivingEntity self = (LivingEntity) (Object) this;
			Vec3d vel = self.getVelocity();
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
		return (NoPushModule) GoatedClient.getInstance().getModuleManager()
			.getModuleByName("NoPush");
	}
}
