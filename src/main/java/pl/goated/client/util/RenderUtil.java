package pl.goated.client.util;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.*;
import org.joml.Matrix4f;

public class RenderUtil {
	
	public static void drawRoundedRect(DrawContext context, int x, int y, int width, int height, int radius, int color) {
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		
		float a = (color >> 24 & 0xFF) / 255.0f;
		float r = (color >> 16 & 0xFF) / 255.0f;
		float g = (color >> 8 & 0xFF) / 255.0f;
		float b = (color & 0xFF) / 255.0f;
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION_COLOR);
		
		// Main rectangle
		bufferBuilder.vertex(matrix, x + radius, y + radius, 0).color(r, g, b, a);
		
		// Top-left corner
		for (int i = 0; i <= 90; i += 5) {
			double angle = Math.toRadians(180 + i);
			bufferBuilder.vertex(matrix, 
				(float) (x + radius + Math.cos(angle) * radius),
				(float) (y + radius + Math.sin(angle) * radius), 0)
				.color(r, g, b, a);
		}
		
		// Top-right corner
		for (int i = 0; i <= 90; i += 5) {
			double angle = Math.toRadians(270 + i);
			bufferBuilder.vertex(matrix,
				(float) (x + width - radius + Math.cos(angle) * radius),
				(float) (y + radius + Math.sin(angle) * radius), 0)
				.color(r, g, b, a);
		}
		
		// Bottom-right corner
		for (int i = 0; i <= 90; i += 5) {
			double angle = Math.toRadians(i);
			bufferBuilder.vertex(matrix,
				(float) (x + width - radius + Math.cos(angle) * radius),
				(float) (y + height - radius + Math.sin(angle) * radius), 0)
				.color(r, g, b, a);
		}
		
		// Bottom-left corner
		for (int i = 0; i <= 90; i += 5) {
			double angle = Math.toRadians(90 + i);
			bufferBuilder.vertex(matrix,
				(float) (x + radius + Math.cos(angle) * radius),
				(float) (y + height - radius + Math.sin(angle) * radius), 0)
				.color(r, g, b, a);
		}
		
		// Close the fan
		double angle = Math.toRadians(180);
		bufferBuilder.vertex(matrix,
			(float) (x + radius + Math.cos(angle) * radius),
			(float) (y + radius + Math.sin(angle) * radius), 0)
			.color(r, g, b, a);
		
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
	
	public static void drawRoundedRectOutline(DrawContext context, int x, int y, int width, int height, int radius, int thickness, int color) {
		drawRoundedRect(context, x - thickness, y - thickness, width + thickness * 2, height + thickness * 2, radius, color);
		drawRoundedRect(context, x, y, width, height, radius, 0xFF000000 | (color & 0x00FFFFFF));
	}
	
	public static void drawHorizontalLine(DrawContext context, int x, int y, int width, int thickness, int color) {
		context.fill(x, y, x + width, y + thickness, color);
	}
	
	public static void drawCircleOutline(DrawContext context, int centerX, int centerY, int radius, int thickness, int color) {
		Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
		
		float a = (color >> 24 & 0xFF) / 255.0f;
		float r = (color >> 16 & 0xFF) / 255.0f;
		float g = (color >> 8 & 0xFF) / 255.0f;
		float b = (color & 0xFF) / 255.0f;
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorProgram);
		
		BufferBuilder bufferBuilder = Tessellator.getInstance().begin(VertexFormat.DrawMode.DEBUG_LINE_STRIP, VertexFormats.POSITION_COLOR);
		
		for (int i = 0; i <= 360; i += 5) {
			double angle = Math.toRadians(i);
			bufferBuilder.vertex(matrix,
				(float) (centerX + Math.cos(angle) * radius),
				(float) (centerY + Math.sin(angle) * radius), 0)
				.color(r, g, b, a);
		}
		
		BufferRenderer.drawWithGlobalProgram(bufferBuilder.end());
		RenderSystem.disableBlend();
	}
}
