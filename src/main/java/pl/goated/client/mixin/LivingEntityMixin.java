package pl.goated.client.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.impl.NoPushModule;

@Mixin(value = LivingEntity.class, priority = 900)
public class LivingEntityMixin {

	// Zapamiętujemy velocity PRZED wywołaniem pushOutOfBlocks
	private Vec3d velocityBeforePush = null;

	@Inject(
		method = "pushOutOfBlocks",
		at = @At("HEAD"),
		cancellable = true
	)
	private void onPushOutOfBlocksHead(double x, double y, double z, CallbackInfo ci) {
		if (!isLocalPlayer()) return;

		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) return;

		LivingEntity self = (LivingEntity) (Object) this;

		// Sprawdź czy to "solidny" blok (nie kwiatek, trawa itp.)
		if (!isSolidBlockPushing(self, x, y, z)) return;

		float multiplier = module.getPushMultiplier();

		// 0% = całkowite zablokowanie - anuluj od razu
		if (multiplier <= 0.0f) {
			ci.cancel();
			return;
		}

		// Dla częściowego tłumienia zapamiętaj velocity przed
		velocityBeforePush = self.getVelocity();
	}

	@Inject(
		method = "pushOutOfBlocks",
		at = @At("RETURN")
	)
	private void onPushOutOfBlocksReturn(double x, double y, double z, CallbackInfo ci) {
		if (!isLocalPlayer()) return;
		if (velocityBeforePush == null) return;

		NoPushModule module = getNoPushModule();
		if (module == null || !module.isEnabled()) {
			velocityBeforePush = null;
			return;
		}

		LivingEntity self = (LivingEntity) (Object) this;
		Vec3d afterVel = self.getVelocity();
		float multiplier = module.getPushMultiplier();

		// Oblicz TYLKO zmianę spowodowaną przez push
		double pushX = afterVel.x - velocityBeforePush.x;
		double pushZ = afterVel.z - velocityBeforePush.z;

		// Zastosuj mnożnik TYLKO na delta od pusha, nie na całe velocity
		self.setVelocity(
			velocityBeforePush.x + pushX * multiplier,
			afterVel.y,
			velocityBeforePush.z + pushZ * multiplier
		);

		velocityBeforePush = null;
	}

	// Sprawdza czy blok który wypycha to solidny blok (nie kwiatek, trawa, etc.)
	private boolean isSolidBlockPushing(LivingEntity entity, double x, double y, double z) {
		World world = entity.getWorld();
		if (world == null) return false;

		Box box = entity.getBoundingBox();
		BlockPos pos = BlockPos.ofFloored(x, y, z);

		try {
			BlockState state = world.getBlockState(pos);
			Block block = state.getBlock();

			// Sprawdź czy blok ma kolizję (solidny)
			VoxelShape collisionShape = state.getCollisionShape(world, pos, ShapeContext.of(entity));
			if (collisionShape.isEmpty()) return false;

			// Sprawdź czy blok faktycznie przecina się z graczem
			Box blockBox = collisionShape.getBoundingBox().offset(pos);
			if (!box.intersects(blockBox)) return false;

			// Pomiń bloki bez kolizji (kwiaty, trawa, etc.)
			// Solidne bloki mają pełny kształt kolizji
			double volume = collisionShape.getBoundingBox().getLengthX()
				* collisionShape.getBoundingBox().getLengthY()
				* collisionShape.getBoundingBox().getLengthZ();

			// Jeśli objętość kształtu kolizji jest bardzo mała - to nie solidny blok
			return volume > 0.1;

		} catch (Exception e) {
			return false;
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
		return (NoPushModule) GoatedClient.getInstance()
			.getModuleManager().getModuleByName("NoPush");
	}
}
