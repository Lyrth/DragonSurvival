package by.dragonsurvivalteam.dragonsurvival.client.gui;

import by.dragonsurvivalteam.dragonsurvival.DragonSurvivalMod;
import by.dragonsurvivalteam.dragonsurvival.client.gui.dragon_editor.DragonEditorScreen;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.AltarTypeButton;
import by.dragonsurvivalteam.dragonsurvival.client.gui.widgets.buttons.generic.HelpButton;
import by.dragonsurvivalteam.dragonsurvival.client.render.ClientDragonRender;
import by.dragonsurvivalteam.dragonsurvival.client.util.FakeClientPlayerUtils;
import by.dragonsurvivalteam.dragonsurvival.client.util.TextRenderUtil;
import by.dragonsurvivalteam.dragonsurvival.common.capability.DragonStateHandler;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonBodies;
import by.dragonsurvivalteam.dragonsurvival.common.dragon_types.DragonTypes;
import by.dragonsurvivalteam.dragonsurvival.common.entity.DragonEntity;
import by.dragonsurvivalteam.dragonsurvival.util.DragonLevel;
import by.dragonsurvivalteam.dragonsurvival.util.DragonUtils;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraftforge.client.gui.widget.ExtendedButton;

public class DragonAltarGUI extends Screen{
	public static final ResourceLocation CONFIRM_BUTTON = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/confirm_button.png");
	public static final ResourceLocation CANCEL_BUTTON = new ResourceLocation(DragonSurvivalMod.MODID, "textures/gui/cancel_button.png");
	private static final ResourceLocation backgroundTexture = new ResourceLocation("textures/block/black_concrete.png");
	private final String[] animations = {"sit_head_locked", "idle_head_locked", "fly_head_locked", "swim_fast_head_locked", "run_head_locked", "dig_head_locked", "resting_left_head_locked", "vibing_sitting", "shy_sitting", "vibing_sitting", "rocking_on_back" };
	public DragonStateHandler handler1 = new DragonStateHandler();
	public DragonStateHandler handler2 = new DragonStateHandler();
	private int guiLeft;
	private int guiTop;
	private boolean hasInit = false;
	private int animation1 = 1;
	private int animation2 = 0;
	private int tick;

	public DragonAltarGUI(){
		super(Component.translatable("ds.gui.dragon_altar"));
	}

	@Override
	public void render(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks){;
		if(minecraft == null){
			return;
		}

		matrixStack.pushPose();
		matrixStack.translate(0, 0, -600);
		renderBackground(matrixStack);
		matrixStack.popPose();

		tick++;

		if(tick % 200 * 20 == 0){
			animation1++;
			animation2++;
;
			int randBody = (int) (Math.random() * (DragonBodies.bodyMappings.size()));
			//System.out.println("body num: " + randBody + " and: " + DragonBodies.bodyMappings.keySet().toArray()[randBody]);

			if (handler1.getBody() == null) {
				handler1.setBody(DragonBodies.CENTER);
			}
			handler2.setBody(DragonBodies.staticBodies.get(handler1.getBody().getBodyName()));
			handler1.setBody(DragonBodies.staticBodies.get(DragonBodies.bodyMappings.keySet().toArray()[randBody]));

			if(animation1 >= animations.length){
				animation1 = 0;
			}

			if(animation2 >= animations.length){
				animation2 = 0;
			}
		}

		for(Widget btn : renderables){
			if(btn instanceof AltarTypeButton button){
				if(button.isHoveredOrFocused()){
					handler1.setType(button.type);
					handler1.setHasFlight(true);
					handler1.setSize(DragonLevel.NEWBORN.size);
					handler1.getSkinData().skinPreset.skinAges.get(DragonLevel.NEWBORN).get().defaultSkin = true;

					handler2.setType(button.type);
					handler2.setHasFlight(true);
					handler2.setSize(button.type == null ? DragonLevel.NEWBORN.size : DragonLevel.ADULT.size);
					handler2.getSkinData().skinPreset.skinAges.get(button.type == null ? DragonLevel.NEWBORN : DragonLevel.ADULT).get().defaultSkin = true;

					FakeClientPlayerUtils.getFakePlayer(0, handler1).animationSupplier = () -> animations[animation1];
					FakeClientPlayerUtils.getFakePlayer(1, handler2).animationSupplier = () -> animations[animation2];

					renderDragon(width / 2 + 170, button.y + button.getHeight() / 2 + 20, 5, matrixStack, 20, FakeClientPlayerUtils.getFakePlayer(0, handler1), FakeClientPlayerUtils.getFakeDragon(0, handler1));
					renderDragon(width / 2 - 205, button.y + button.getHeight() / 2 + 1, -4, matrixStack, 40, FakeClientPlayerUtils.getFakePlayer(1, handler2), FakeClientPlayerUtils.getFakeDragon(1, handler2));
				}
			}
		}

		TextRenderUtil.drawCenteredScaledText(matrixStack, width / 2, 10, 2f, title.getString(), DyeColor.WHITE.getTextColor());

		super.render(matrixStack, mouseX, mouseY, partialTicks);
	}

	@Override
	public void renderBackground(PoseStack pMatrixStack){
		super.renderBackground(pMatrixStack);
		renderBorders(pMatrixStack, backgroundTexture, 0, width, 32, height - 32, width, height);
	}

	public static void renderBorders(PoseStack stack, ResourceLocation texture, int x0, int x1, int y0, int y1, int width, int height){
		Tesselator tesselator = Tesselator.getInstance();
		BufferBuilder bufferbuilder = tesselator.getBuilder();
		RenderSystem.setShaderTexture(0, texture);

		stack.pushPose();
		double zLevel = 0;

		RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
//		RenderSystem.setShaderTexture(0, GuiComponent.BACKGROUND_LOCATION);
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(519);
		bufferbuilder.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
		bufferbuilder.vertex(x0, y0, zLevel).uv(0.0F, (float)y0 / 32.0F).color(64, 64, 64, 55).endVertex();
		bufferbuilder.vertex(x0 + width, y0, zLevel).uv((float)width / 32.0F, (float)y0 / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferbuilder.vertex(x0 + width, 0.0D, zLevel).uv((float)width / 32.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		bufferbuilder.vertex(x0, 0.0D, zLevel).uv(0.0F, 0.0F).color(64, 64, 64, 255).endVertex();
		bufferbuilder.vertex(x0, height, zLevel).uv(0.0F, (float)height / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferbuilder.vertex(x0 + width, height, zLevel).uv((float)width / 32.0F, (float)height / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferbuilder.vertex(x0 + width, y1, zLevel).uv((float)width / 32.0F, (float)y1 / 32.0F).color(64, 64, 64, 255).endVertex();
		bufferbuilder.vertex(x0, y1, zLevel).uv(0.0F, (float)y1 / 32.0F).color(64, 64, 64, 255).endVertex();
		tesselator.end();

		RenderSystem.depthFunc(515);
		RenderSystem.disableDepthTest();
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ZERO, GlStateManager.DestFactor.ONE);
		RenderSystem.disableTexture();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);

		bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		bufferbuilder.vertex(x0, y0 + 4, zLevel).uv(0.0F, 1.0F).color(0, 0, 0, 0).endVertex();
		bufferbuilder.vertex(x1, y0 + 4, zLevel).uv(1.0F, 1.0F).color(0, 0, 0, 0).endVertex();
		bufferbuilder.vertex(x1, y0, zLevel).uv(1.0F, 0.0F).color(0, 0, 0, 255).endVertex();
		bufferbuilder.vertex(x0, y0, zLevel).uv(0.0F, 0.0F).color(0, 0, 0, 255).endVertex();
		bufferbuilder.vertex(x0, y1, zLevel).uv(0.0F, 1.0F).color(0, 0, 0, 255).endVertex();
		bufferbuilder.vertex(x1, y1, zLevel).uv(1.0F, 1.0F).color(0, 0, 0, 255).endVertex();
		bufferbuilder.vertex(x1, y1 - 4, zLevel).uv(1.0F, 0.0F).color(0, 0, 0, 0).endVertex();
		bufferbuilder.vertex(x0, y1 - 4, zLevel).uv(0.0F, 0.0F).color(0, 0, 0, 0).endVertex();
		tesselator.end();
		stack.popPose();
	}

	private void renderDragon(int x, int y, int xrot, PoseStack matrixStack, float size, Player player, DragonEntity dragon){
		matrixStack.pushPose();
		float scale = size * 1.5f;
		matrixStack.scale(scale, scale, scale);
		matrixStack.translate(0, 0, 400);
		ClientDragonRender.dragonModel.setCurrentTexture(null);
		ClientDragonRender.renderEntityInInventory(DragonUtils.isDragon(player) ? dragon : player, x, y, scale, xrot, -3);
		matrixStack.popPose();
	}


	@Override
	protected void init(){
		super.init();

		if(!hasInit){
			hasInit = true;
		}

		guiLeft = (width - 304) / 2;
		guiTop = (height - 190) / 2;

		addRenderableWidget(new HelpButton(width / 2, 32 + 5, 16, 16, "ds.help.altar", 1));

		addRenderableWidget(new AltarTypeButton(this, DragonTypes.CAVE, width / 2 - 104, guiTop + 30));
		addRenderableWidget(new AltarTypeButton(this, DragonTypes.FOREST, width / 2 - 51, guiTop + 30));
		addRenderableWidget(new AltarTypeButton(this, DragonTypes.SEA, width / 2 + 2, guiTop + 30));
		addRenderableWidget(new AltarTypeButton(this, null, width / 2 + 55, guiTop + 30));

		addRenderableWidget(new ExtendedButton(width / 2 - 75, height - 25, 150, 20, Component.translatable("ds.gui.dragon_editor"), btn -> {
			Minecraft.getInstance().setScreen(new DragonEditorScreen(Minecraft.getInstance().screen));
		}){
			@Override
			public void render(PoseStack p_230430_1_, int p_230430_2_, int p_230430_3_, float p_230430_4_){
				visible = DragonUtils.isDragon(minecraft.player);
				super.render(p_230430_1_, p_230430_2_, p_230430_3_, p_230430_4_);
			}
		});
	}
}