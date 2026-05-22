package pl.goated.client.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;

public class RenderUtil {
	
	public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		// Simplify by using DrawContext's built-in fill method
		// Draw main rectangle
		context.fill(x + radius, y, x + width - radius, y + height, color);
		context.fill(x, y + radius, x + width, y + height - radius, color);
		
		// Draw corners (simplified circles)
		drawCorners(context, x, y, width, height, radius, color);
	}
	
	private static void drawCorners(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		// Top-left corner
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (isInCircle(i, j, radius)) {
					context.fill(x + i, y + j, x + i + 1, y + j + 1, color);
				}
			}
		}
		
		// Top-right corner
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (isInCircle(radius - i, j, radius)) {
					context.fill(x + width - radius + i, y + j, x + width - radius + i + 1, y + j + 1, color);
				}
			}
		}
		
		// Bottom-left corner
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (isInCircle(i, radius - j, radius)) {
					context.fill(x + i, y + height - radius + j, x + i + 1, y + height - radius + j + 1, color);
				}
			}
		}
		
		// Bottom-right corner
		for (int i = 0; i < radius; i++) {
			for (int j = 0; j < radius; j++) {
				if (isInCircle(radius - i, radius - j, radius)) {
					context.fill(x + width - radius + i, y + height - radius + j, x + width - radius + i + 1, y + height - radius + j + 1, color);
				}
			}
		}
	}
	
	private static boolean isInCircle(int x, int y, int radius) {
		return x * x + y * y <= radius * radius;
	}
	
	public static void drawRoundedRectOutline(DrawContext context, int x, int y, int width, int height, int radius, int thickness, int color) {
		// Draw outline by drawing multiple rectangles
		for (int i = 0; i < thickness; i++) {
			drawRoundedRect(context, x - i, y - i, width + i * 2, height + i * 2, radius, color);
		}
	}
	
	public static void drawHorizontalLine(DrawContext context, int x, int y, int width, int thickness, int color) {
		context.fill(x, y, x + width, y + thickness, color);
	}
	
	public static void drawVerticalLine(DrawContext context, int x, int y, int height, int thickness, int color) {
		context.fill(x, y, x + thickness, y + height, color);
	}
	
	public static void drawRect(DrawContext context, int x, int y, int width, int height, int color) {
		context.fill(x, y, x + width, y + height, color);
	}
	
	public static void drawCircleOutline(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
		// Draw circle using Bresenham algorithm
		int x = 0;
		int y = radius;
		int d = 3 - 2 * radius;
		
		while (x <= y) {
			drawLine(context, centerX + x, centerY - y, centerX + x, centerY - y + thickness, color);
			drawLine(context, centerX - x, centerY - y, centerX - x, centerY - y + thickness, color);
			drawLine(context, centerX + x, centerY + y, centerX + x, centerY + y + thickness, color);
			drawLine(context, centerX - x, centerY + y, centerX - x, centerY + y + thickness, color);
			drawLine(context, centerX + y, centerY - x, centerX + y, centerY - x + thickness, color);
			drawLine(context, centerX - y, centerY - x, centerX - y, centerY - x + thickness, color);
			drawLine(context, centerX + y, centerY + x, centerX + y, centerY + x + thickness, color);
			drawLine(context, centerX - y, centerY + x, centerX - y, centerY + x + thickness, color);
			
			if (d < 0) {
				d = d + 4 * x + 6;
			} else {
				d = d + 4 * (x - y) + 10;
				y--;
			}
			x++;
		}
	}
	
	public static void drawLine(DrawContext context, int x1, int y1, int x2, int y2, int color) {
		int dx = Math.abs(x2 - x1);
		int dy = Math.abs(y2 - y1);
		int sx = x1 < x2 ? 1 : -1;
		int sy = y1 < y2 ? 1 : -1;
		int err = dx - dy;
		
		int x = x1;
		int y = y1;
		
		while (true) {
			context.fill(x, y, x + 1, y + 1, color);
			
			if (x == x2 && y == y2) break;
			
			int e2 = 2 * err;
			if (e2 > -dy) {
				err -= dy;
				x += sx;
			}
			if (e2 < dx) {
				err += dx;
				y += sy;
			}
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
