package by.dragonsurvivalteam.dragonsurvival.client.render.entity.projectiles;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.common.entity.projectiles.BallLightningEntity;
import by.dragonsurvivalteam.dragonsurvival.registry.DSItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.ItemTransforms.TransformType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.model.AnimatedGeoModel;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

@OnlyIn( Dist.CLIENT )
public class BallLightningRenderer extends GeoProjectilesRenderer<BallLightningEntity>{
	private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(DragonSurvivalMod.MODID, "textures/entity/lightning_texture.png");

	public BallLightningRenderer(EntityRendererProvider.Context renderManager, AnimatedGeoModel<BallLightningEntity> modelProvider){
		super(renderManager, modelProvider);
	}

	@Override
	protected int getBlockLightLevel(BallLightningEntity p_225624_1_, BlockPos p_225624_2_){
		return 15;
	}

	@Override
	public void render(BallLightningEntity p_225623_1_, float p_225623_2_, float p_225623_3_, PoseStack stack, MultiBufferSource p_225623_5_, int p_225623_6_){
		if(p_225623_1_.tickCount >= 2 || !(entityRenderDispatcher.camera.getEntity().distanceToSqr(p_225623_1_) < 12.25D)){
			stack.pushPose();
			stack.translate(0F, -0.2F, 0F);
			stack.scale(2.0F, 2.0F, 2.0F);
			stack.mulPose(entityRenderDispatcher.cameraOrientation());
			stack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
			stack.popPose();
			super.render(p_225623_1_, p_225623_2_, p_225623_3_, stack, p_225623_5_, p_225623_6_);
		}


		super.render(p_225623_1_, p_225623_2_, p_225623_3_, stack, p_225623_5_, p_225623_6_);
	}

	@Override
	public ResourceLocation getTextureLocation(BallLightningEntity p_110775_1_){
		return TEXTURE_LOCATION;
	}
}