package pl.goated.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pl.goated.client.config.ConfigManager;
import pl.goated.client.gui.ClickGui;
import pl.goated.client.module.ModuleManager;

public class GoatedClient implements ClientModInitializer {
	public static final String MOD_ID = "goatedclient";
	public static final String MOD_NAME = "GoatedClient";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_NAME);
	
	private static GoatedClient INSTANCE;
	private ModuleManager moduleManager;
	private ConfigManager configManager;
	private ClickGui clickGui;
	private KeyBinding openGuiKey;
	
	@Override
	public void onInitializeClient() {
		INSTANCE = this;
		LOGGER.info("Initializing " + MOD_NAME);
		
		try {
			// Initialize managers
			configManager = new ConfigManager();
			moduleManager = new ModuleManager();
			clickGui = new ClickGui();
			
			// Load config
			configManager.load();
			
			// Register keybinding (RIGHT_SHIFT)
			openGuiKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.goatedclient.open_gui",
				InputUtil.Type.KEYSYM,
				GLFW.GLFW_KEY_RIGHT_SHIFT,
				"category.goatedclient"
			));
			
			// Register tick event - works everywhere (menus, worlds, etc)
			ClientTickEvents.END_CLIENT_TICK.register(client -> {
				if (openGuiKey.wasPressed()) {
					MinecraftClient.getInstance().setScreen(clickGui);
				}
			});
			
			LOGGER.info(MOD_NAME + " initialized successfully!");
		} catch (Exception e) {
			LOGGER.error("Failed to initialize GoatedClient", e);
			e.printStackTrace();
		}
	}
	
	public static GoatedClient getInstance() {
		return INSTANCE;
	}
	
	public ModuleManager getModuleManager() {
		return moduleManager;
	}
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	public ClickGui getClickGui() {
		return clickGui;
	}
}
