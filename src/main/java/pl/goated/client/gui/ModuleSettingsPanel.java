package pl.goated.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.Module;
import pl.goated.client.module.impl.GuiModule;
import pl.goated.client.module.settings.BooleanSetting;
import pl.goated.client.module.settings.ColorSetting;
import pl.goated.client.module.settings.Setting;
import pl.goated.client.util.RenderUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class ModuleSettingsPanel {
	private final Module module;
	private final ClickGui parent;
	private final List<SettingComponent> components = new ArrayList<>();
	
	private int x, y, width, height;
	private static final int CLOSE_BUTTON_SIZE = 20;
	private static final int PADDING = 15;
	private static final int SETTING_HEIGHT = 35;
	
	private ColorPicker activeColorPicker = null;
	
	public ModuleSettingsPanel(Module module, ClickGui parent) {
		this.module = module;
		this.parent = parent;
		
		MinecraftClient mc = MinecraftClient.getInstance();
		this.width = (int) (mc.getWindow().getScaledWidth() * 0.6);
		this.height = (int) (mc.getWindow().getScaledHeight() * 0.8);
		this.x = (mc.getWindow().getScaledWidth() - width) / 2;
		this.y = (mc.getWindow().getScaledHeight() - height) / 2;
		
		initSettings();
	}
	
	private void initSettings() {
		try {
			List<Setting<?>> settings = getModuleSettings();
			
			int yOffset = 50;
			for (Setting<?> setting : settings) {
				if (setting instanceof ColorSetting colorSetting) {
					components.add(new ColorSettingComponent(colorSetting, PADDING, yOffset, width - PADDING * 2));
					yOffset += SETTING_HEIGHT + 10;
				} else if (setting instanceof BooleanSetting booleanSetting) {
					components.add(new BooleanSettingComponent(booleanSetting, PADDING, yOffset, width - PADDING * 2));
					yOffset += SETTING_HEIGHT;
				}
			}
		} catch (Exception e) {
			GoatedClient.LOGGER.error("Failed to initialize settings", e);
		}
	}
	
	private List<Setting<?>> getModuleSettings() throws Exception {
		List<Setting<?>> settings = new ArrayList<>();
		for (Field field : module.getClass().getDeclaredFields()) {
			if (Setting.class.isAssignableFrom(field.getType())) {
				field.setAccessible(true);
				Setting<?> setting = (Setting<?>) field.get(module);
				if (setting != null) settings.add(setting);
			}
		}
		return settings;
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		
		int bgColor = guiModule.backgroundColor.getValue();
		int borderColor = guiModule.borderColor.getValue();
		int textColor = guiModule.textColor.getValue();
		
		RenderUtil.drawRoundedRect(context, x, y, width, height, 10, bgColor);
		RenderUtil.drawRoundedRectOutline(context, x, y, width, height, 10, 2, borderColor);
		
		RenderUtil.drawRect(context, x, y, width, 40, RenderUtil.adjustAlpha(borderColor, 50));
		context.drawText(MinecraftClient.getInstance().textRenderer, 
			module.getName() + " Settings", x + PADDING, y + 15, textColor, false);
		
		int closeX = x + width - CLOSE_BUTTON_SIZE - 10;
		int closeY = y + 10;
		boolean closeHovered = mouseX >= closeX && mouseX <= closeX + CLOSE_BUTTON_SIZE &&
			mouseY >= closeY && mouseY <= closeY + CLOSE_BUTTON_SIZE;
		
		RenderUtil.drawRoundedRect(context, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE, 4, 
			closeHovered ? 0xFFFF0000 : RenderUtil.adjustAlpha(borderColor, 100));
		context.drawText(MinecraftClient.getInstance().textRenderer, "X", closeX + 6, closeY + 6, 0xFFFFFFFF, false);
		
		for (SettingComponent component : components) {
			component.render(context, x, y, mouseX, mouseY, guiModule);
		}
		
		if (activeColorPicker != null) {
			activeColorPicker.render(context, mouseX, mouseY);
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (activeColorPicker != null) {
			if (activeColorPicker.mouseClicked(mouseX, mouseY, button)) {
				return true;
			} else {
				activeColorPicker = null;
				return false;
			}
		}
		
		int closeX = x + width - CLOSE_BUTTON_SIZE - 10;
		int closeY = y + 10;
		if (mouseX >= closeX && mouseX <= closeX + CLOSE_BUTTON_SIZE &&
			mouseY >= closeY && mouseY <= closeY + CLOSE_BUTTON_SIZE) {
			parent.closeSettingsPanel();
			GoatedClient.getInstance().getConfigManager().save();
			return true;
		}
		
		for (SettingComponent component : components) {
			if (component.mouseClicked(x, y, mouseX, mouseY, button)) {
				if (component instanceof ColorSettingComponent colorComp) {
					activeColorPicker = new ColorPicker(colorComp.setting, (int) mouseX, (int) mouseY);
				}
				return true;
			}
		}
		
		return false;
	}

	// DODANE: Rozwiązuje problem z suwakiem!
	public void mouseReleased(double mouseX, double mouseY, int button) {
		if (activeColorPicker != null) {
			activeColorPicker.mouseReleased();
		}
	}
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			parent.closeSettingsPanel();
			GoatedClient.getInstance().getConfigManager().save();
			return true;
		}
		return false;
	}
	
	private interface SettingComponent {
		void render(DrawContext context, int panelX, int panelY, int mouseX, int mouseY, GuiModule guiModule);
		boolean mouseClicked(int panelX, int panelY, double mouseX, double mouseY, int button);
	}
	
	private static class BooleanSettingComponent implements SettingComponent {
		private final BooleanSetting setting;
		private final int x, y, width;
		
		public BooleanSettingComponent(BooleanSetting setting, int x, int y, int width) {
			this.setting = setting;
			this.x = x;
			this.y = y;
			this.width = width;
		}
		
		@Override
		public void render(DrawContext context, int panelX, int panelY, int mouseX, int mouseY, GuiModule guiModule) {
			int absoluteX = panelX + x;
			int absoluteY = panelY + y;
			
			context.drawText(MinecraftClient.getInstance().textRenderer, 
				setting.getName(), absoluteX, absoluteY + 5, guiModule.textColor.getValue(), false);
			
			int toggleX = absoluteX + width - 45;
			int toggleWidth = 40;
			int toggleHeight = 20;
			
			boolean hovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
				mouseY >= absoluteY && mouseY <= absoluteY + toggleHeight;
			
			int bgColor = setting.getValue() ? 0xFF00AA00 : 0xFF555555;
			if (hovered) bgColor = RenderUtil.adjustBrightness(bgColor, 20);
			
			RenderUtil.drawRoundedRect(context, toggleX, absoluteY, toggleWidth, toggleHeight, 10, bgColor);
			
			int circleX = setting.getValue() ? toggleX + toggleWidth - 18 : toggleX + 2;
			RenderUtil.drawRoundedRect(context, circleX, absoluteY + 2, 16, 16, 8, 0xFFFFFFFF);
		}
		
		@Override
		public boolean mouseClicked(int panelX, int panelY, double mouseX, double mouseY, int button) {
			int absoluteX = panelX + x;
			int absoluteY = panelY + y;
			int toggleX = absoluteX + width - 45;
			
			if (mouseX >= toggleX && mouseX <= toggleX + 40 &&
				mouseY >= absoluteY && mouseY <= absoluteY + 20) {
				setting.toggle();
				GoatedClient.getInstance().getConfigManager().save();
				return true;
			}
			return false;
		}
	}
	
	private static class ColorSettingComponent implements SettingComponent {
		private final ColorSetting setting;
		private final int x, y, width;
		
		public ColorSettingComponent(ColorSetting setting, int x, int y, int width) {
			this.setting = setting;
			this.x = x;
			this.y = y;
			this.width = width;
		}
		
		@Override
		public void render(DrawContext context, int panelX, int panelY, int mouseX, int mouseY, GuiModule guiModule) {
			int absoluteX = panelX + x;
			int absoluteY = panelY + y;
			
			context.drawText(MinecraftClient.getInstance().textRenderer, 
				setting.getName(), absoluteX, absoluteY + 5, guiModule.textColor.getValue(), false);
			
			int previewX = absoluteX + width - 80;
			
			RenderUtil.drawRoundedRect(context, previewX, absoluteY, 70, 25, 5, setting.getValue());
			RenderUtil.drawRoundedRectOutline(context, previewX, absoluteY, 70, 25, 5, 1, guiModule.borderColor.getValue());
			
			String hex = String.format("#%08X", setting.getValue());
			context.drawText(MinecraftClient.getInstance().textRenderer, 
				hex, previewX + 5, absoluteY + 8, RenderUtil.getContrastColor(setting.getValue()), false);
		}
		
		@Override
		public boolean mouseClicked(int panelX, int panelY, double mouseX, double mouseY, int button) {
			int absoluteX = panelX + x;
			int absoluteY = panelY + y;
			int previewX = absoluteX + width - 80;
			
			if (mouseX >= previewX && mouseX <= previewX + 70 &&
				mouseY >= absoluteY && mouseY <= absoluteY + 25) {
				return true;
			}
			return false;
		}
	}
}
