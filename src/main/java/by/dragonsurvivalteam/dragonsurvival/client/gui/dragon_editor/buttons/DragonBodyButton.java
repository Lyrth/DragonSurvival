package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.util.TooltipRendering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.AbstractDragonBody;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class DragonBodyButton extends Button {
	private DragonEditorScreen dragonEditorScreen;
	private AbstractDragonBody dragonBody;
	private ResourceLocation texture_location;
	private int pos;
	private boolean locked = false;
	
	public DragonBodyButton(DragonEditorScreen dragonEditorScreen, int x, int y, int xSize, int ySize, AbstractDragonBody dragonBody, int pos, boolean locked) {
		super(x, y, xSize, ySize, Component.literal(dragonBody.toString()), btn -> {
			if (!locked) {
				dragonEditorScreen.dragonBody = dragonBody;
				dragonEditorScreen.update();
			}
		});
		texture_location = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/body_type_icon_" + dragonEditorScreen.dragonType.getTypeName().toLowerCase() + ".png");
		this.dragonEditorScreen = dragonEditorScreen;
		this.dragonBody = dragonBody;
		this.pos = pos;
		this.locked = locked;
	}
	
	public void press() {
		dragonEditorScreen.dragonBody = dragonBody;
		dragonEditorScreen.update();
	}

	@Override
	public void renderToolTip(PoseStack p_230443_1_, int p_230443_2_, int p_230443_3_){
		TooltipRendering.drawHoveringText(p_230443_1_, Component.translatable("ds.gui.body_types." + dragonBody.getBodyName().toLowerCase() + ".tooltip"), p_230443_2_, p_230443_3_);
		//TODO Add the same tooltip as for magic skills (similar to achievements) instead of the current one. Basic description on the main section, additional characteristics are revealed on ctrl (like Claws skill or any active skills). The main body type is centered. Changes in the positive side of other types should be highlighted in green color, and in the negative red. The localization is already in the file.
	}

	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		active = visible = dragonEditorScreen.showUi;
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
		RenderSystem.setShaderTexture(0, texture_location);
		RenderSystem.setShader(GameRenderer::getRendertypeTranslucentShader);

		int i = 0;
		if (this.dragonBody.equals(dragonEditorScreen.dragonBody)) {
			i = 2;
		} else if (this.locked) {
			i = 3;
		} else if (this.isHoveredOrFocused()) {
			i = 1;
		}
		this.blit(pPoseStack, this.x, this.y, pos * this.width, i * this.height, this.width, this.height);
		//RenderSystem.setShaderTexture(0, WIDGETS_LOCATION);
	}
}
