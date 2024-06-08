package by.dragonsurvivalteam.dragonsurvival.mixins;

import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateProvider;
import by.dragonsurvivalteam.dragonsurvival.config.ServerConfig;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin( InventoryScreen.class )
public abstract class MixinInventoryScreen extends EffectRenderingInventoryScreen<InventoryMenu> implements RecipeUpdateListener{
	public MixinInventoryScreen(InventoryMenu p_98701_, Inventory p_98702_, Component p_98703_){
		super(p_98701_, p_98702_, p_98703_);
	}

	@Redirect( method = "renderEntityInInventoryRaw(IIIFFLnet/minecraft/world/entity/LivingEntity;)V", at = @At( value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;runAsFancy(Ljava/lang/Runnable;)V" ) )
	private static void dragonScreenEntityRender(Runnable p_runAsFancy_0_){
		LocalPlayer player = Minecraft.getInstance().player;
		if(DragonStateProvider.getCap(player).isPresent() && DragonUtils.getHandler(player).isDragon()){
			DragonStateProvider.getCap(player).ifPresent(dragonStateHandler -> {
				double bodyYaw = dragonStateHandler.getMovementData().bodyYaw;
				double headYaw = dragonStateHandler.getMovementData().headYaw;
				double headPitch = dragonStateHandler.getMovementData().headPitch;

				double lastBodyYaw = dragonStateHandler.getMovementData().bodyYawLastTick;
				double lastHeadYaw = dragonStateHandler.getMovementData().headYawLastTick;
				double lastHeadPitch = dragonStateHandler.getMovementData().headPitchLastTick;

				dragonStateHandler.getMovementData().bodyYaw = player.yBodyRot;
				dragonStateHandler.getMovementData().headYaw = 0;
				dragonStateHandler.getMovementData().headPitch = 0;

				dragonStateHandler.getMovementData().bodyYawLastTick = player.yBodyRot;
				dragonStateHandler.getMovementData().headYawLastTick = player.yHeadRot;
				dragonStateHandler.getMovementData().headPitchLastTick = player.xRot;

				RenderSystem.runAsFancy(p_runAsFancy_0_);

				dragonStateHandler.getMovementData().bodyYaw = bodyYaw;
				dragonStateHandler.getMovementData().headYaw = headYaw;
				dragonStateHandler.getMovementData().headPitch = headPitch;

				dragonStateHandler.getMovementData().bodyYawLastTick = lastBodyYaw;
				dragonStateHandler.getMovementData().headYawLastTick = lastHeadYaw;
				dragonStateHandler.getMovementData().headPitchLastTick = lastHeadPitch;
			});
		}else{
			RenderSystem.runAsFancy(p_runAsFancy_0_);
		}
	}

	private static float dragonScreenEntityRescaler(float pX){
		LocalPlayer player = Minecraft.getInstance().player;
		DragonStateHandler handler = DragonUtils.getHandler(player);

		if(handler.isDragon()){
			double size = handler.getSize();
			if(size > ServerConfig.DEFAULT_MAX_GROWTH_SIZE){
				// Scale the matrix back to the DEFAULT_MAX_GROWTH_SIZE to prevent the entity from clipping in the inventory panel
				pX *= Mth.sqrt(((float)(ServerConfig.DEFAULT_MAX_GROWTH_SIZE / size)));
			}
		}

		return pX;
	}

	@ModifyArg(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scaling(FFF)Lorg/joml/Matrix4f;"), index = 0)
	private static float dragonScreenEntityRescalerX(float pX) {
		return dragonScreenEntityRescaler(pX);
	}

	@ModifyArg(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scaling(FFF)Lorg/joml/Matrix4f;"), index = 1)
	private static float dragonScreenEntityRescalerY(float pY) {
		return dragonScreenEntityRescaler(pY);
	}

	@ModifyArg(method = "renderEntityInInventory", at = @At(value = "INVOKE", target = "Lorg/joml/Matrix4f;scaling(FFF)Lorg/joml/Matrix4f;"), index = 2)
	private static float dragonScreenEntityRescalerZ(float pZ) {
		return dragonScreenEntityRescaler(pZ);
	}
}