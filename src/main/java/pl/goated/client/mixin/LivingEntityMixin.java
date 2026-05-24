package pl.goated.client.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.impl.NoPushModule;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
	
	@ModifyVariable(method = "pushOutOfBlocks", at = @At("HEAD"), argsOnly = true)
	private double modifyPushX(double x) {
		if (!isLocalPlayer()) return x;
		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) return x;
		return x * module.getPushMultiplier();
	}
	
	@ModifyVariable(method = "pushOutOfBlocks", at = @At("HEAD"), argsOnly = true, ordinal = 1)
	private double modifyPushZ(double z) {
		if (!isLocalPlayer()) return z;
		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) return z;
		return z * module.getPushMultiplier();
	}
	
	private boolean isLocalPlayer() {
		LivingEntity self = (LivingEntity) (Object) this;
		MinecraftClient mc = net.minecraft.client.MinecraftClient.getInstance();
		return mc != null && mc.player != null && self.equals(mc.player);
	}
	
	private NoPushModule getNoPushModule() {
		if (GoatedClient.getInstance() == null) return null;
		if (GoatedClient.getInstance().getModuleManager() == null) return null;
		return (NoPushModule) GoatedClient.getInstance().getModuleManager().getModuleByName("NoPush");
	}
}
