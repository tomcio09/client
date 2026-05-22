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
				if (setting != null) {
					settings.add(setting);
				}
			}
		}
		
		return settings;
	}
	
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		
		int bgColor = guiModule.backgroundColor.getValue();
		int borderColor = guiModule.borderColor.getValue();
		int textColor = guiModule.textColor.getValue();
		
		// Draw background
		RenderUtil.drawRoundedRect(context, x, y, width, height, 10, bgColor);
		RenderUtil.drawRoundedRectOutline(context, x, y, width, height, 10, 2, borderColor);
		
		// Draw title bar
		RenderUtil.drawRoundedRect(context, x, y, width, 40, 10, adjustAlpha(borderColor, 50));
		context.drawText(MinecraftClient.getInstance().textRenderer, 
			module.getName() + " Settings", x + PADDING, y + 15, textColor, false);
		
		// Draw close button
		int closeX = x + width - CLOSE_BUTTON_SIZE - 10;
		int closeY = y + 10;
		boolean closeHovered = mouseX >= closeX && mouseX <= closeX + CLOSE_BUTTON_SIZE &&
			mouseY >= closeY && mouseY <= closeY + CLOSE_BUTTON_SIZE;
		
		RenderUtil.drawRoundedRect(context, closeX, closeY, CLOSE_BUTTON_SIZE, CLOSE_BUTTON_SIZE, 4, 
			closeHovered ? 0xFFFF0000 : adjustAlpha(borderColor, 100));
		
		// Draw X
		context.drawText(MinecraftClient.getInstance().textRenderer, 
			"X", closeX + 6, closeY + 6, 0xFFFFFFFF, false);
		
		// Draw settings
		for (SettingComponent component : components) {
			component.render(context, x, y, mouseX, mouseY, guiModule);
		}
		
		// Draw color picker if active
		if (activeColorPicker != null) {
			activeColorPicker.render(context, mouseX, mouseY);
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		// Check color picker click
		if (activeColorPicker != null) {
			if (activeColorPicker.mouseClicked(mouseX, mouseY, button)) {
				return true;
			} else {
				activeColorPicker = null;
				return false;
			}
		}
		
		// Check close button
		int closeX = x + width - CLOSE_BUTTON_SIZE - 10;
		int closeY = y + 10;
		if (mouseX >= closeX && mouseX <= closeX + CLOSE_BUTTON_SIZE &&
			mouseY >= closeY && mouseY <= closeY + CLOSE_BUTTON_SIZE) {
			parent.closeSettingsPanel();
			GoatedClient.getInstance().getConfigManager().save();
			return true;
		}
		
		// Check setting components
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
	
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			parent.closeSettingsPanel();
			GoatedClient.getInstance().getConfigManager().save();
			return true;
		}
		return false;
	}
	
	private int adjustAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (alpha << 24);
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
			
			// Draw setting name
			context.drawText(MinecraftClient.getInstance().textRenderer, 
				setting.getName(), absoluteX, absoluteY + 5, guiModule.textColor.getValue(), false);
			
			// Draw toggle
			int toggleX = absoluteX + width - 45;
			int toggleY = absoluteY;
			int toggleWidth = 40;
			int toggleHeight = 20;
			
			boolean hovered = mouseX >= toggleX && mouseX <= toggleX + toggleWidth &&
				mouseY >= toggleY && mouseY <= toggleY + toggleHeight;
			
			int bgColor = setting.getValue() ? 0xFF00AA00 : 0xFF555555;
			if (hovered) bgColor = adjustBrightness(bgColor, 20);
			
			RenderUtil.drawRoundedRect(context, toggleX, toggleY, toggleWidth, toggleHeight, 10, bgColor);
			
			// Draw toggle circle
			int circleX = setting.getValue() ? toggleX + toggleWidth - 18 : toggleX + 2;
			RenderUtil.drawRoundedRect(context, circleX, toggleY + 2, 16, 16, 8, 0xFFFFFFFF);
		}
		
		@Override
		public boolean mouseClicked(int panelX, int panelY, double mouseX, double mouseY, int button) {
			int absoluteX = panelX + x;
			int absoluteY = panelY + y;
			int toggleX = absoluteX + width - 45;
			
			if (mouseX >= toggleX && mouseX <= toggleX + 40 &&
				mouseY >= absoluteY && mouseY <= absoluteY + 20) {
				setting.toggle();
				return true;
			}
			return false;
		}
		
		private int adjustBrightness(int color, int amount) {
			int a = (color >> 24) & 0xFF;
			int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
			int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
			int b = Math.min(255, (color & 0xFF) + amount);
			return (a << 24) | (r << 16) | (g << 8) | b;
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
			
			// Draw setting name
			context.drawText(MinecraftClient.getInstance().textRenderer, 
				setting.getName(), absoluteX, absoluteY + 5, guiModule.textColor.getValue(), false);
			
			// Draw color preview
			int previewX = absoluteX + width - 80;
			int previewY = absoluteY;
			
			RenderUtil.drawRoundedRect(context, previewX, previewY, 70, 25, 5, setting.getValue());
			RenderUtil.drawRoundedRectOutline(context, previewX, previewY, 70, 25, 5, 1, guiModule.borderColor.getValue());
			
			// Draw hex value
			String hex = String.format("#%08X", setting.getValue());
			context.drawText(MinecraftClient.getInstance().textRenderer, 
				hex, previewX + 5, previewY + 8, getContrastColor(setting.getValue()), false);
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
		
		private int getContrastColor(int color) {
			int r = (color >> 16) & 0xFF;
			int g = (color >> 8) & 0xFF;
			int b = color & 0xFF;
			int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
			return luminance > 128 ? 0xFF000000 : 0xFFFFFFFF;
		}
	}
}
