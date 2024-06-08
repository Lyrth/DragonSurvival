package by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.buttons;

import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

public class AdultEditorButton extends Button{
	private final DragonEditorScreen dragonEditorScreen;

	public AdultEditorButton(DragonEditorScreen dragonEditorScreen){
		super(dragonEditorScreen.width / 2 + 60, dragonEditorScreen.guiTop - 30, 120, 20, Component.translatable("ds.level.adult"), btn -> {
			dragonEditorScreen.level = DragonLevel.ADULT;
			dragonEditorScreen.dragonRender.zoom = dragonEditorScreen.level.size * 2;
			dragonEditorScreen.handler.getSkinData().compileSkin();
			dragonEditorScreen.update();
		});
		this.dragonEditorScreen = dragonEditorScreen;
	}

	@Override
	public void render(PoseStack pMatrixStack, int pMouseX, int pMouseY, float pPartialTicks){
		active = visible = dragonEditorScreen.showUi;
		super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks);
	}

	@Override
	public void renderButton(PoseStack stack, int p_230431_2_, int p_230431_3_, float p_230431_4_){
		int j = isHovered || dragonEditorScreen.level == DragonLevel.ADULT ? 16777215 : 10526880;
		TextRenderUtil.drawCenteredScaledText(stack, x + width / 2, y + 4, 1.5f, getMessage().getString(), j | Mth.ceil(alpha * 255.0F) << 24);
	}
}