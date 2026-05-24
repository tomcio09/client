package pl.goated.client.util;

import net.minecraft.client.gui.DrawContext;

public class RenderUtil {
	
	// Nowy, zoptymalizowany pod Sodium/Nvidium algorytm paskowy (0 nakładających się pikseli)
	public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		if (radius <= 0) {
			context.fill(x, y, x + width, y + height, color);
			return;
		}
		
		// Zabezpieczenie przed zbyt dużym promieniem
		radius = Math.min(radius, Math.min(width / 2, height / 2));
		
		// Środkowy, duży prostokąt
		context.fill(x, y + radius, x + width, y + height - radius, color);
		
		// Górne i dolne zaokrąglenie rysowane paskami
		for (int i = 0; i < radius; i++) {
			int yTop = y + i;
			int yBottom = y + height - 1 - i;
			
			double dy = radius - i - 0.5;
			int xOffset = radius - (int) Math.round(Math.sqrt(radius * radius - dy * dy));
			
			// Górny pasek
			context.fill(x + xOffset, yTop, x + width - xOffset, yTop + 1, color);
			// Dolny pasek
			context.fill(x + xOffset, yBottom, x + width - xOffset, yBottom + 1, color);
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
		
		radius = Math.min(radius, Math.min(width / 2, height / 2));
		
		// Lewa i Prawa krawędź (proste części)
		context.fill(x, y + radius, x + thickness, y + height - radius, color);
		context.fill(x + width - thickness, y + radius, x + width, y + height - radius, color);
		
		// Górna i Dolna zaokrąglona część
		for (int i = 0; i < radius; i++) {
			int yTop = y + i;
			int yBottom = y + height - 1 - i;
			
			double dy = radius - i - 0.5;
			int outerXOffset = radius - (int) Math.round(Math.sqrt(radius * radius - dy * dy));
			
			if (i < thickness) {
				// Pełna linia zamykająca ramkę z góry i dołu
				context.fill(x + outerXOffset, yTop, x + width - outerXOffset, yTop + 1, color);
				context.fill(x + outerXOffset, yBottom, x + width - outerXOffset, yBottom + 1, color);
			} else {
				// Boczne ścianki na wysokości łuku
				int innerRadius = radius - thickness;
				double innerDy = innerRadius - (i - thickness) - 0.5;
				int innerXOffset = thickness + innerRadius - (int) Math.round(Math.sqrt(innerRadius * innerRadius - innerDy * innerDy));
				
				// Góra - lewo i prawo
				context.fill(x + outerXOffset, yTop, x + innerXOffset, yTop + 1, color);
				context.fill(x + width - innerXOffset, yTop, x + width - outerXOffset, yTop + 1, color);
				
				// Dół - lewo i prawo
				context.fill(x + outerXOffset, yBottom, x + innerXOffset, yBottom + 1, color);
				context.fill(x + width - innerXOffset, yBottom, x + width - outerXOffset, yBottom + 1, color);
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
		drawRoundedRectOutline(context, centerX - radius, centerY - radius, radius * 2, radius * 2, radius, thickness, color);
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
