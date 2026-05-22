package pl.goated.client.gui;

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
	
	private static final int SEARCH_HEIGHT = 25;
	private static final int MODULE_BUTTON_HEIGHT = 60;
	private static final int MODULE_BUTTON_SPACING = 5;
	private static final int PADDING = 10;
	
	public ClickGui() {
		super(Text.literal("GoatedClient"));
	}
	
	@Override
	protected void init() {
		super.init();
		
		// Calculate GUI dimensions
		guiWidth = width / 2;
		guiHeight = (height * 3) / 4;
		guiX = (width - guiWidth) / 2;
		guiY = (height - guiHeight) / 2;
		
		// Initialize module buttons
		moduleButtons.clear();
		List<Module> modules = GoatedClient.getInstance().getModuleManager().getModules();
		
		int buttonY = SEARCH_HEIGHT + PADDING;
		int buttonsPerRow = 2;
		int buttonWidth = (guiWidth - PADDING * 3) / buttonsPerRow;
		
		for (int i = 0; i < modules.size(); i++) {
			Module module = modules.get(i);
			int col = i % buttonsPerRow;
			int row = i / buttonsPerRow;
			
			int x = PADDING + col * (buttonWidth + PADDING);
			int y = buttonY + row * (MODULE_BUTTON_HEIGHT + MODULE_BUTTON_SPACING);
			
			moduleButtons.add(new ModuleButton(module, x, y, buttonWidth, MODULE_BUTTON_HEIGHT));
		}
	}
	
	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		GuiModule guiModule = (GuiModule) GoatedClient.getInstance().getModuleManager().getModuleByName("GUI");
		
		// Render background blur
		if (guiModule.blur.getValue()) {
			renderBackground(context, mouseX, mouseY, delta);
		}
		
		// Adjust mouse coordinates to GUI space
		int relativeMouseX = mouseX - guiX;
		int relativeMouseY = mouseY - guiY;
		
		// Draw main GUI panel
		drawMainPanel(context, relativeMouseX, relativeMouseY, guiModule);
		
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
		
		// Draw background with rounded corners
		RenderUtil.drawRoundedRect(context, guiX, guiY, guiWidth, guiHeight, 8, bgColor);
		
		// Draw border
		RenderUtil.drawRoundedRectOutline(context, guiX, guiY, guiWidth, guiHeight, 8, 2, borderColor);
		
		// Draw search bar
		int searchY = guiY + PADDING;
		RenderUtil.drawRoundedRect(context, guiX + PADDING, searchY, guiWidth - PADDING * 2, SEARCH_HEIGHT, 5, 
			searchFocused ? adjustAlpha(bgColor, 200) : adjustAlpha(bgColor, 150));
		RenderUtil.drawRoundedRectOutline(context, guiX + PADDING, searchY, guiWidth - PADDING * 2, SEARCH_HEIGHT, 5, 1, borderColor);
		
		// Draw search icon and text
		String displayText = searchText.isEmpty() ? "Search modules..." : searchText;
		context.drawText(textRenderer, displayText, guiX + PADDING + 5, searchY + 8, 
			searchText.isEmpty() ? adjustAlpha(textColor, 128) : textColor, false);
		
		// Draw separator line
		int separatorY = guiY + PADDING + SEARCH_HEIGHT + 5;
		RenderUtil.drawHorizontalLine(context, guiX + PADDING, separatorY, guiWidth - PADDING * 2, 1, borderColor);
		
		// Draw module buttons
		for (ModuleButton button : moduleButtons) {
			if (button.matchesSearch(searchText)) {
				button.render(context, guiX, guiY + PADDING * 2 + SEARCH_HEIGHT + 5, mouseX, mouseY, guiModule);
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
		
		int relativeMouseX = (int) mouseX - guiX;
		int relativeMouseY = (int) mouseY - guiY - PADDING * 2 - SEARCH_HEIGHT - 5;
		
		// Check search bar click
		if (relativeMouseX >= PADDING && relativeMouseX <= guiWidth - PADDING &&
			relativeMouseY >= -SEARCH_HEIGHT - 5 && relativeMouseY <= -5) {
			searchFocused = true;
			return true;
		} else {
			searchFocused = false;
		}
		
		// Check module button clicks
		for (ModuleButton moduleButton : moduleButtons) {
			if (moduleButton.matchesSearch(searchText) && 
				moduleButton.isHovered(relativeMouseX, relativeMouseY)) {
				
				if (button == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
					moduleButton.module.toggle();
					return true;
				} else if (button == GLFW.GLFW_MOUSE_BUTTON_RIGHT) {
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
		
		if (searchFocused) {
			if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchText.isEmpty()) {
				searchText = searchText.substring(0, searchText.length() - 1);
				return true;
			} else if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
				searchFocused = false;
				return true;
			}
		}
		
		return super.keyPressed(keyCode, scanCode, modifiers);
	}
	
	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (searchFocused) {
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
	
	private int adjustAlpha(int color, int alpha) {
		return (color & 0x00FFFFFF) | (alpha << 24);
	}
	
	private static class ModuleButton {
		private final Module module;
		private final int x, y, width, height;
		
		public ModuleButton(Module module, int x, int y, int width, int height) {
			this.module = module;
			this.x = x;
			this.y = y;
			this.width = width;
			this.height = height;
		}
		
		public void render(DrawContext context, int panelX, int panelY, int mouseX, int mouseY, GuiModule guiModule) {
			int absoluteX = panelX + x;
			int absoluteY = panelY + y;
			
			boolean hovered = isHovered(mouseX, mouseY);
			
			int bgColor = module.isEnabled() ? 
				adjustColor(guiModule.backgroundColor.getValue(), 30) : 
				guiModule.backgroundColor.getValue();
			
			if (hovered) {
				bgColor = adjustColor(bgColor, 15);
			}
			
			// Draw button background
			RenderUtil.drawRoundedRect(context, absoluteX, absoluteY, width, height, 6, bgColor);
			RenderUtil.drawRoundedRectOutline(context, absoluteX, absoluteY, width, height, 6, 1, 
				guiModule.borderColor.getValue());
			
			// Draw module name
			context.drawText(context.getMatrices().peek().getPositionMatrix(), 
				module.getName(), absoluteX + 8, absoluteY + 8, 
				guiModule.textColor.getValue(), false);
			
			// Draw module description
			String desc = module.getDescription();
			if (desc.length() > 30) {
				desc = desc.substring(0, 27) + "...";
			}
			context.drawText(context.getMatrices().peek().getPositionMatrix(), 
				desc, absoluteX + 8, absoluteY + 22, 
				adjustAlpha(guiModule.textColor.getValue(), 150), false);
			
			// Draw enabled indicator
			if (module.isEnabled()) {
				RenderUtil.drawRoundedRect(context, absoluteX + width - 18, absoluteY + 8, 10, 10, 2, 0xFF00FF00);
			}
		}
		
		public boolean isHovered(int mouseX, int mouseY) {
			return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
		}
		
		public boolean matchesSearch(String search) {
			return search.isEmpty() || module.getName().toLowerCase().contains(search.toLowerCase());
		}
		
		private int adjustColor(int color, int amount) {
			int a = (color >> 24) & 0xFF;
			int r = Math.min(255, ((color >> 16) & 0xFF) + amount);
			int g = Math.min(255, ((color >> 8) & 0xFF) + amount);
			int b = Math.min(255, (color & 0xFF) + amount);
			return (a << 24) | (r << 16) | (g << 8) | b;
		}
		
		private int adjustAlpha(int color, int alpha) {
			return (color & 0x00FFFFFF) | (alpha << 24);
		}
	}
}
