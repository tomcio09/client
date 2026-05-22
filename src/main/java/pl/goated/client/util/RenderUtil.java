package pl.goated.client.util;

import net.minecraft.client.gui.DrawContext;

public class RenderUtil {
	
	public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		// Use simple filled rectangles instead of complex circle math
		int a = (color >> 24) & 0xFF;
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		
		// Draw main rectangle
		context.fill(x + radius, y, x + width - radius, y + height, color);
		context.fill(x, y + radius, x + width, y + height - radius, color);
		
		// Draw corners (simple approach)
		drawCorner(context, x + radius, y + radius, radius, color); // Top-left
		drawCorner(context, x + width - radius, y + radius, radius, color); // Top-right
		drawCorner(context, x + radius, y + height - radius, radius, color); // Bottom-left
		drawCorner(context, x + width - radius, y + height - radius, radius, color); // Bottom-right
	}
	
	private static void drawCorner(DrawContext context, int cx, int cy, int radius, int color) {
		for (int i = 0; i < radius; i++) {
			int height = (int) Math.sqrt(radius * radius - i * i);
			context.fill(cx - i, cy - height, cx - i + 1, cy + height, color);
		}
	}
	
	public static void drawRoundedRectOutline(DrawContext context, int x, int y, int width, int height, int radius, int thickness, int color) {
		// Draw top line
		context.fill(x + radius, y, x + width - radius, y + thickness, color);
		// Draw bottom line
		context.fill(x + radius, y + height - thickness, x + width - radius, y + height, color);
		// Draw left line
		context.fill(x, y + radius, x + thickness, y + height - radius, color);
		// Draw right line
		context.fill(x + width - thickness, y + radius, x + width, y + height - radius, color);
	}
	
	public static void drawHorizontalLine(DrawContext context, int x, int y, int width, int thickness, int color) {
		context.fill(x, y, x + width, y + thickness, color);
	}
	
	public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}
	
	public static int adjustAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (alpha << 24);
	}
	
	public static int adjustBrightness(int color, int amount) {
		int a = (color >> 24) & 0xFF;
		int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
		int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
		int b = Math.min(255, (color & 0xFF) + amount);
		return (a << 24) | (r << 16) | (g << 8) | b;
	}
	
	public static int getContrastColor(int color) {
		int r = (color >> 16) & 0xFF;
		int g = (color >> 8) & 0xFF;
		int b = color & 0xFF;
		int luminance = (int) (0.299 * r + 0.587 * g + 0.114 * b);
		return luminance > 128 ? 0xFF000000 : 0xFFFFFFFF;
	}
}
