package pl.goated.client.hud;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.Module;
import pl.goated.client.module.impl.GuiModule;
import pl.goated.client.module.impl.HudModule;
import pl.goated.client.util.RenderUtil;

import java.util.List;

public class HudRenderer {
	
	public static void register() {
		HudRenderCallback.EVENT.register(HudRenderer::render);
	}
	
	private static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
		MinecraftClient mc = MinecraftClient.getInstance();
		if (mc == null || mc.player == null) return;
		
		HudModule hudModule = (HudModule) GoatedClient.getInstance().getModuleManager().getModuleByName("HUD");
		if (hudModule == null || !hudModule.isEnabled()) return;
		
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		if (guiModule == null) return;
		
		// Pobierz wszystkie włączone moduły (poza GUI i HUD)
		List<Module> enabledModules = GoatedClient.getInstance().getModuleManager().getModules()
			.stream()
			.filter(m -> m.isEnabled() && !m.getName().equals("GUI") && !m.getName().equals("HUD"))
			.toList();
		
		if (!hudModule.showArrayList.getValue() || enabledModules.isEmpty()) return;
		
		int scaledWidth = mc.getWindow().getScaledWidth();
		int textColor = guiModule.textColor.getValue();
		int bgColor = RenderUtil.adjustAlpha(guiModule.backgroundColor.getValue(), 180);
		int borderColor = guiModule.borderColor.getValue();
		
		int padding = 4;
		int lineHeight = 12;
		int moduleHeight = lineHeight + padding;
		int maxTextWidth = 0;
		
		// Znajdź najszerszy tekst
		for (Module module : enabledModules) {
			int w = mc.textRenderer.getWidth(module.getName());
			if (w > maxTextWidth) maxTextWidth = w;
		}
		
		int boxWidth = maxTextWidth + padding * 2;
		int startX = scaledWidth - boxWidth - 5;
		int startY = 5;
		
		// Rysuj tło i każdy moduł
		for (int i = 0; i < enabledModules.size(); i++) {
			Module module = enabledModules.get(i);
			int y = startY + i * moduleHeight;
			
			// Tło modułu
			RenderUtil.drawRoundedRect(context, startX, y, boxWidth, moduleHeight, 3, bgColor);
			
			// Zielony border po lewej stronie
			context.fill(startX, y, startX + 2, y + moduleHeight, 0xFF00CC00);
			
			// Nazwa modułu
			context.drawText(mc.textRenderer, module.getName(), 
				startX + padding + 2, y + padding / 2 + 2, textColor, false);
		}
	}
}
