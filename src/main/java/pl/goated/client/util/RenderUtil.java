package pl.goated.client.util;

import net.minecraft.client.gui.DrawContext;

public class RenderUtil {
	
	public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		if (radius <= 0) {
			context.fill(x, y, x + width, y + height, color);
			return;
		}
		
		// Centralna część
		context.fill(x + radius, y, x + width - radius, y + height, color);
		// Lewa część
		context.fill(x, y + radius, x + radius, y + height - radius, color);
		// Prawa część
		context.fill(x + width - radius, y + radius, x + width, y + height - radius, color);
		
		// Rogi (0=Lewy-Górny, 1=Prawy-Górny, 2=Lewy-Dolny, 3=Prawy-Dolny)
		drawCorner(context, x + radius, y + radius, radius, 0, color);
		drawCorner(context, x + width - radius, y + radius, radius, 1, color);
		drawCorner(context, x + radius, y + height - radius, radius, 2, color);
		drawCorner(context, x + width - radius, y + height - radius, radius, 3, color);
	}
	
	private static void drawCorner(DrawContext context, int cx, int cy, int radius, int type, int color) {
		for (int i = 0; i < radius; i++) {
			int h = (int) Math.round(Math.sqrt(radius * radius - i * i));
			
			if (type == 0) { // Lewy Górny
				context.fill(cx - i - 1, cy - h, cx - i, cy, color);
			} else if (type == 1) { // Prawy Górny
				context.fill(cx + i, cy - h, cx + i + 1, cy, color);
			} else if (type == 2) { // Lewy Dolny
				context.fill(cx - i - 1, cy, cx - i, cy + h, color);
			} else if (type == 3) { // Prawy Dolny
				context.fill(cx + i, cy, cx + i + 1, cy + h, color);
			}
		}
	}
	
	public static void drawRoundedRectOutline(DrawContext context, int x, int y, int width, int height, int radius, int thickness, int color) {
		if (radius <= 0) {
			drawHorizontalLine(context, x, y, width, thickness, color);
			drawHorizontalLine(context, x, y + height - thickness, width, thickness, color);
			context.fill(x, y + thickness, x + thickness, y + height - thickness, color);
			context.fill(x + width - thickness, y + thickness, x + width, y + height - thickness, color);
			return;
		}
		
		// Góra
		context.fill(x + radius, y, x + width - radius, y + thickness, color);
		// Dół
		context.fill(x + radius, y + height - thickness, x + width - radius, y + height, color);
		// Lewo
		context.fill(x, y + radius, x + thickness, y + height - radius, color);
		// Prawo
		context.fill(x + width - thickness, y + radius, x + width, y + height - radius, color);
		
		// Obramowania Rogów
		drawCornerOutline(context, x + radius, y + radius, radius, thickness, 0, color);
		drawCornerOutline(context, x + width - radius, y + radius, radius, thickness, 1, color);
		drawCornerOutline(context, x + radius, y + height - radius, radius, thickness, 2, color);
		drawCornerOutline(context, x + width - radius, y + height - radius, radius, thickness, 3, color);
	}

	private static void drawCornerOutline(DrawContext context, int cx, int cy, int radius, int thickness, int type, int color) {
		for (int i = 0; i < radius; i++) {
			int outerH = (int) Math.round(Math.sqrt(radius * radius - i * i));
			int innerR = radius - thickness;
			int innerH = (i < innerR) ? (int) Math.round(Math.sqrt(innerR * innerR - i * i)) : 0;
			
			if (outerH > innerH) {
				if (type == 0) { // Lewy Górny
					context.fill(cx - i - 1, cy - outerH, cx - i, cy - innerH, color);
				} else if (type == 1) { // Prawy Górny
					context.fill(cx + i, cy - outerH, cx + i + 1, cy - innerH, color);
				} else if (type == 2) { // Lewy Dolny
					context.fill(cx - i - 1, cy + innerH, cx - i, cy + outerH, color);
				} else if (type == 3) { // Prawy Dolny
					context.fill(cx + i, cy + innerH, cx + i + 1, cy + outerH, color);
				}
			}
		}
	}
	
	public static void drawHorizontalLine(DrawContext context, int x, int y, int width, int thickness, int color) {
		context.fill(x, y, x + width, y + thickness, color);
	}
	
	public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}
	
	public static void drawCircleOutline(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
		// Sprytne wykorzystanie rogów do narysowania pełnego koła
		drawCornerOutline(context, centerX, centerY, radius, thickness, 0, color);
		drawCornerOutline(context, centerX, centerY, radius, thickness, 1, color);
		drawCornerOutline(context, centerX, centerY, radius, thickness, 2, color);
		drawCornerOutline(context, centerX, centerY, radius, thickness, 3, color);
	}
	
	public static void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
		// Algorytm DDA do rysowania linii
		double dx = x2 - x1;
		double dy = y2 - y1;
		double steps = Math.max(Math.abs(dx), Math.abs(dy));
		
		double xInc = dx / steps;
		double yInc = dy / steps;
		
		double x = x1;
		double y = y1;
		
		for (int i = 0; i <= steps; i++) {
			context.fill((int)x, (int)y, (int)x + 1, (int)y + 1, color);
			x += xInc;
			y += yInc;
		}
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
