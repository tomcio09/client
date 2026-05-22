package pl.goated.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.impl.GuiModule;
import pl.goated.client.module.settings.ColorSetting;
import pl.goated.client.util.RenderUtil;

public class ColorPicker {
	private final ColorSetting setting;
	private final int x, y;
	private final int width = 220;
	private final int height = 280;
	
	private boolean draggingHue = false;
	private boolean draggingSaturation = false;
	private boolean draggingAlpha = false;
	
	private float hue, saturation, brightness, alpha;
	
	public ColorPicker(ColorSetting setting, int x, int y) {
		this.setting = setting;
		this.x = Math.max(0, Math.min(x, MinecraftClient.getInstance().getWindow().getScaledWidth() - width));
		this.y = Math.max(0, Math.min(y, MinecraftClient.getInstance().getWindow().getScaledHeight() - height));
		
		// Convert current color to HSB
		int color = setting.getValue();
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		alpha = ((color >> 24) & 0xFF) / 255f;
		
		float[] hsb = java.awt.Color.RGBtoHSB(r, g, b, null);
		hue = hsb[0];
		saturation = hsb[1];
		brightness = hsb[2];
	}
	
	public void render(DrawContext context, int mouseX, int mouseY) {
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		
		// Draw background
		RenderUtil.drawRoundedRect(context, x, y, width, height, 8, guiModule.backgroundColor.getValue());
		RenderUtil.drawRoundedRectOutline(context, x, y, width, height, 8, 2, guiModule.borderColor.getValue());
		
		// Draw saturation/brightness picker (200x200)
		int pickerSize = 200;
		drawSaturationBrightnessPicker(context, x + 10, y + 10, pickerSize);
		
		// Draw hue slider
		drawHueSlider(context, x + 10, y + pickerSize + 20, pickerSize, 15);
		
		// Draw alpha slider
		drawAlphaSlider(context, x + 10, y + pickerSize + 45, pickerSize, 15);
		
		// Draw preview
		int previewY = y + pickerSize + 70;
		RenderUtil.drawRoundedRect(context, x + 10, previewY, pickerSize, 30, 5, getCurrentColor());
		
		// Draw hex value
		String hex = String.format("#%08X", getCurrentColor());
		context.drawText(MinecraftClient.getInstance().textRenderer, 
			hex, x + 15, previewY + 10, RenderUtil.getContrastColor(getCurrentColor()), false);
		
		// Update if dragging
		if (draggingSaturation) {
			updateSaturationBrightness(mouseX, mouseY, x + 10, y + 10, pickerSize);
		}
		if (draggingHue) {
			updateHue(mouseX, x + 10, pickerSize);
		}
		if (draggingAlpha) {
			updateAlpha(mouseX, x + 10, pickerSize);
		}
	}
	
	private void drawSaturationBrightnessPicker(DrawContext context, int x, int y, int size) {
		// Draw gradient for saturation and brightness
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				float s = (float) i / size;
				float b = 1f - (float) j / size;
				int color = java.awt.Color.HSBtoRGB(hue, s, b) | 0xFF000000;
				context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
			}
		}
		
		// Draw border
		RenderUtil.drawRoundedRectOutline(context, x, y, size, size, 4, 1, 0xFFFFFFFF);
		
		// Draw selector circle
		int selectorX = (int) (x + saturation * size);
		int selectorY = (int) (y + (1 - brightness) * size);
		RenderUtil.drawCircleOutline(context, selectorX, selectorY, 5, 1, 0xFFFFFFFF);
	}
	
	private void drawHueSlider(DrawContext context, int x, int y, int width, int height) {
		// Draw hue gradient
		for (int i = 0; i < width; i++) {
			float h = (float) i / width;
			int color = java.awt.Color.HSBtoRGB(h, 1f, 1f) | 0xFF000000;
			context.fill(x + i, y, x + i + 1, y + height, color);
		}
		
		// Draw border
		RenderUtil.drawRoundedRectOutline(context, x, y, width, height, 3, 1, 0xFF000000);
		
		// Draw selector
		int selectorX = (int) (x + hue * width);
		context.fill(selectorX - 2, y - 3, selectorX + 2, y + height + 3, 0xFFFFFFFF);
	}
	
	private void drawAlphaSlider(DrawContext context, int x, int y, int width, int height) {
		// Draw checkerboard background
		drawCheckerboard(context, x, y, width, height);
		
		// Draw alpha gradient
		int baseColor = java.awt.Color.HSBtoRGB(hue, saturation, brightness) & 0x00FFFFFF;
		for (int i = 0; i < width; i++) {
			float a = (float) i / width;
			int color = baseColor | ((int) (a * 255) << 24);
			context.fill(x + i, y, x + i + 1, y + height, color);
		}
		
		// Draw border
		RenderUtil.drawRoundedRectOutline(context, x, y, width, height, 3, 1, 0xFF000000);
		
		// Draw selector
		int selectorX = (int) (x + alpha * width);
		context.fill(selectorX - 2, y - 3, selectorX + 2, y + height + 3, 0xFFFFFFFF);
	}
	
	private void drawCheckerboard(DrawContext context, int x, int y, int width, int height) {
		int squareSize = 5;
		for (int i = 0; i < width; i += squareSize) {
			for (int j = 0; j < height; j += squareSize) {
				boolean isWhite = ((i / squareSize) + (j / squareSize)) % 2 == 0;
				context.fill(x + i, y + j, 
					x + Math.min(i + squareSize, width), 
					y + Math.min(j + squareSize, height),
					isWhite ? 0xFFCCCCCC : 0xFF888888);
			}
		}
	}
	
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int pickerSize = 200;
		
		// Check if clicking outside panel
		if (mouseX < x || mouseX > x + width || mouseY < y || mouseY > y + height) {
			setting.setValue(getCurrentColor());
			return false;
		}
		
		// Check saturation/brightness picker
		if (mouseX >= x + 10 && mouseX <= x + 10 + pickerSize &&
			mouseY >= y + 10 && mouseY <= y + 10 + pickerSize) {
			draggingSaturation = true;
			updateSaturationBrightness((int) mouseX, (int) mouseY, x + 10, y + 10, pickerSize);
			return true;
		}
		
		// Check hue slider
		if (mouseX >= x + 10 && mouseX <= x + 10 + pickerSize &&
			mouseY >= y + pickerSize + 20 && mouseY <= y + pickerSize + 35) {
			draggingHue = true;
			updateHue((int) mouseX, x + 10, pickerSize);
			return true;
		}
		
		// Check alpha slider
		if (mouseX >= x + 10 && mouseX <= x + 10 + pickerSize &&
			mouseY >= y + pickerSize + 45 && mouseY <= y + pickerSize + 60) {
			draggingAlpha = true;
			updateAlpha((int) mouseX, x + 10, pickerSize);
			return true;
		}
		
		return true;
	}
	
	public void mouseReleased() {
		if (draggingSaturation || draggingHue || draggingAlpha) {
			setting.setValue(getCurrentColor());
		}
		draggingSaturation = false;
		draggingHue = false;
		draggingAlpha = false;
	}
	
	private void updateSaturationBrightness(int mouseX, int mouseY, int pickerX, int pickerY, int size) {
		saturation = Math.max(0, Math.min(1, (float) (mouseX - pickerX) / size));
		brightness = Math.max(0, Math.min(1, 1f - (float) (mouseY - pickerY) / size));
	}
	
	private void updateHue(int mouseX, int sliderX, int width) {
		hue = Math.max(0, Math.min(1, (float) (mouseX - sliderX) / width));
	}
	
	private void updateAlpha(int mouseX, int sliderX, int width) {
		alpha = Math.max(0, Math.min(1, (float) (mouseX - sliderX) / width));
	}
	
	private int getCurrentColor() {
		int rgb = java.awt.Color.HSBtoRGB(hue, saturation, brightness);
		int a = (int) (alpha * 255);
		return (a << 24) | (rgb & 0x00FFFFFF);
	}
}
