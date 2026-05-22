package pl.goated.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.Module;
import pl.goated.client.module.impl.GuiModule;
import pl.goated.client.util.RenderUtil;

import java.util.ArrayList;
import java.util.List;

public class ClickGui extends Screen {
	private final List<ModuleButton> moduleButtons = new ArrayList<>();
	private String searchText = "";
	private boolean searchFocused = false;
	
	private ModuleSettingsPanel settingsPanel = null;
	
	private int guiX, guiY;
	private int guiWidth, guiHeight;
	
	private static final int SEARCH_HEIGHT = 30;
	private static final int MODULE_BUTTON_HEIGHT = 65;
	private static final int MODULE_BUTTON_SPACING = 8;
	private static final int PADDING = 12;
	private static final int CORNER_RADIUS = 20; // Zmienione z 8 na 20
	
	public ClickGui() {
		super(Text.literal("GoatedClient"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		if (guiModule == null) {
			GoatedClient.LOGGER.warn("GUI Module not found!");
			return;
		}
		
		// Calculate GUI dimensions - 50% width, 75% height
		guiWidth = (int) (width * 0.5);
		guiHeight = (int) (height * 0.75);
		guiX = (width - guiWidth) / 2;
		guiY = (height - guiHeight) / 2;
		
		// Initialize module buttons
		moduleButtons.clear();
		List<Module> modules = GoatedClient.getInstance().getModuleManager().getModules();
		
		if (modules.isEmpty()) {
			GoatedClient.LOGGER.warn("No modules found!");
			return;
		}
		
		int buttonY = guiY + SEARCH_HEIGHT + PADDING * 2;
		int buttonsPerRow = 2;
		int buttonWidth = (guiWidth - PADDING * 3) / buttonsPerRow;
		
		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			int col = i % buttonsPerRow;
			int row = i / buttonsPerRow;
			
			int x = guiX + PADDING + col * (buttonWidth + PADDING);
			int y = buttonY + row * (MODULE_BUTTON_HEIGHT + MODULE_BUTTON_SPACING);
			
			moduleButtons.add(new ModuleButton(module, x, y, buttonWidth, MODULE_BUTTON_HEIGHT));
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		// Render semi-transparent background
		this.renderBackground(context, mouseX, mouseY, delta);
		
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		if (guiModule == null) {
			super.render(context, mouseX, mouseY, delta);
			return;
		}
		
		// Draw main GUI panel
		drawMainPanel(context, mouseX, mouseY, guiModule);
		
		// Draw settings panel if open
		if (settingsPanel != null) {
			settingsPanel.render(context, mouseX, mouseY, delta);
		}
		
		super.render(context, mouseX, mouseY, delta);
	}
	
	private void drawMainPanel(DrawContext context, int mouseX, int mouseY, GuiModule guiModule) {
		int bgColor = guiModule.backgroundColor.getValue();
		int borderColor = guiModule.borderColor.getValue();
		int textColor = guiModule.textColor.getValue();
		
		GoatedClient.LOGGER.info("Drawing GUI: x=" + guiX + ", y=" + guiY + ", w=" + guiWidth + ", h=" + guiHeight);
		GoatedClient.LOGGER.info("BG Color: " + Integer.toHexString(bgColor));
		
		// Draw main background z zaokrągleniem 20
		RenderUtil.drawRoundedRect(context, guiX, guiY, guiWidth, guiHeight, CORNER_RADIUS, bgColor);
		
		// Draw border
		RenderUtil.drawRoundedRectOutline(context, guiX, guiY, guiWidth, guiHeight, CORNER_RADIUS, 2, borderColor);
		
		// Draw title
		context.drawText(textRenderer, "GoatedClient", guiX + PADDING, guiY + PADDING, textColor, false);
		
		// Draw search bar background
		int searchY = guiY + PADDING + 20;
		int searchBarColor = searchFocused ? RenderUtil.adjustAlpha(borderColor, 100) : RenderUtil.adjustAlpha(borderColor, 50);
		RenderUtil.drawRoundedRect(context, guiX + PADDING, searchY, guiWidth - PADDING * 2, SEARCH_HEIGHT, 10, searchBarColor);
		
		// Draw search text
		String displayText = searchText.isEmpty() ? "Search modules..." : searchText;
		context.drawText(textRenderer, displayText, guiX + PADDING + 10, searchY + 10, 
			searchText.isEmpty() ? RenderUtil.adjustAlpha(textColor, 128) : textColor, false);
		
		// Draw separator line below search
		int separatorY = searchY + SEARCH_HEIGHT + PADDING;
		RenderUtil.drawHorizontalLine(context, guiX + PADDING, separatorY, guiWidth - PADDING * 2, 1, borderColor);
		
		// Draw module buttons
		int moduleAreaY = separatorY + PADDING;
		int maxModuleY = guiY + guiHeight - PADDING;
		
		int buttonIndex = 0;
		for (ModuleButton button : moduleButtons) {
			if (button.matchesSearch(searchText)) {
				int row = buttonIndex / 2;
				int buttonYPos = moduleAreaY + row * (MODULE_BUTTON_HEIGHT + MODULE_BUTTON_SPACING);
				
				// Only render if within GUI bounds
				if (buttonYPos + MODULE_BUTTON_HEIGHT <= maxModuleY && buttonYPos >= moduleAreaY) {
					button.setPosition(
						guiX + PADDING + (buttonIndex % 2) * (button.width + PADDING),
						buttonYPos
					);
					button.render(context, mouseX, mouseY, guiModule);
				}
				buttonIndex++;
			}
		}
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (settingsPanel != null) {
			if (settingsPanel.mouseClicked(mouseX, mouseY, button)) {
				return true;
			}
		}
		
		// Check search bar click
		int searchY = guiY + PADDING + 20;
		if (mouseX >= guiX + PADDING && mouseX <= guiX + guiWidth - PADDING &&
			mouseY >= searchY && mouseY <= searchY + SEARCH_HEIGHT) {
			searchFocused = true;
			return true;
		} else {
			searchFocused = false;
		}
		
		// Check module button clicks
		for (ModuleButton moduleButton : moduleButtons) {
			if (moduleButton.matchesSearch(searchText) && 
				moduleButton.isHovered((int) mouseX, (int) mouseY)) {
				
				if (button == 0) { // Left click
					moduleButton.module.toggle();
					GoatedClient.getInstance().getConfigManager().save();
					GoatedClient.LOGGER.info("Toggled module: " + moduleButton.module.getName());
					return true;
				} else if (button == 1) { // Right click
					openSettingsPanel(moduleButton.module);
					return true;
				}
			}
		}
		
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (settingsPanel != null) {
			if (settingsPanel.keyPressed(keyCode, scanCode, modifiers)) {
				return true;
			}
		}
		
		if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
			if (settingsPanel != null) {
				settingsPanel = null;
			} else {
				close();
			}
			return true;
		}
		
		if (searchFocused) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
				searchText = searchText.substring(0, searchText.length() - 1);
				return true;
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (searchFocused && searchText.length() < 50) {
			searchText += chr;
			return true;
		}
		
		return super.charTyped(chr, modifiers);
	}
	
	private void openSettingsPanel(Module module) {
		settingsPanel = new ModuleSettingsPanel(module, this);
	}
	
	public void closeSettingsPanel() {
		settingsPanel = null;
	}
	
	@Override
	public boolean shouldPause() {
		return false;
	}
	
	private static class ModuleButton {
		private final Module module;
		private int x, y;
		private final int width, height;
		
		public ModuleButton(Module module, int x, int y, int width, int height) {
			this.module = module;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		public void setPosition(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public void render(DrawContext context, int mouseX, int mouseY, GuiModule guiModule) {
			boolean hovered = isHovered(mouseX, mouseY);
			
			// Darker color if enabled
			int bgColor = module.isEnabled() ? 
				RenderUtil.adjustBrightness(guiModule.backgroundColor.getValue(), 20) : 
				guiModule.backgroundColor.getValue();
			
			if (hovered) {
				bgColor = RenderUtil.adjustBrightness(bgColor, 15);
			}
			
			// Draw button background z zaokrągleniem 12
			RenderUtil.drawRoundedRect(context, x, y, width, height, 12, bgColor);
			RenderUtil.drawRoundedRectOutline(context, x, y, width, height, 12, 1, guiModule.borderColor.getValue());
			
			MinecraftClient mc = MinecraftClient.getInstance();
			
			// Draw module name
			context.drawText(mc.textRenderer, 
				module.getName(), x + 10, y + 10, 
				guiModule.textColor.getValue(), false);
			
			// Draw module description (truncated)
			String desc = module.getDescription();
			if (desc.length() > 35) {
				desc = desc.substring(0, 32) + "...";
			}
			context.drawText(mc.textRenderer, 
				desc, x + 10, y + 26, 
				RenderUtil.adjustAlpha(guiModule.textColor.getValue(), 180), false);
			
			// Draw enabled indicator
			if (module.isEnabled()) {
				RenderUtil.drawRoundedRect(context, x + width - 22, y + 10, 12, 12, 3, 0xFF00FF00);
			}
		}
		
		public boolean isHovered(int mouseX, int mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
		
		public boolean matchesSearch(String search) {
			return search.isEmpty() || module.getName().toLowerCase().contains(search.toLowerCase()) ||
				   module.getDescription().toLowerCase().contains(search.toLowerCase());
		}
	}
}
