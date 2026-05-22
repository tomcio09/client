package pl.goated.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.Module;
import pl.goated.client.module.settings.BooleanSetting;
import pl.goated.client.module.settings.ColorSetting;
import pl.goated.client.module.settings.Setting;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
	private final Path configPath;
	private final Gson gson;
	
	public ConfigManager() {
		this.configPath = FabricLoader.getInstance().getConfigDir().resolve("goatedclient.json");
		this.gson = new GsonBuilder().setPrettyPrinting().create();
	}
	
	public void save() {
		try {
			JsonObject root = new JsonObject();
			JsonObject modules = new JsonObject();
			
			for (Module module : GoatedClient.getInstance().getModuleManager().getModules()) {
				JsonObject moduleData = new JsonObject();
				moduleData.addProperty("enabled", module.isEnabled());
				
				JsonObject settings = new JsonObject();
				for (Field field : module.getClass().getDeclaredFields()) {
					if (Setting.class.isAssignableFrom(field.getType())) {
						field.setAccessible(true);
						try {
							Setting<?> setting = (Setting<?>) field.get(module);
							if (setting != null) {
								if (setting instanceof ColorSetting) {
									settings.addProperty(setting.getName(), (Integer) setting.getValue());
								} else if (setting instanceof BooleanSetting) {
									settings.addProperty(setting.getName(), (Boolean) setting.getValue());
								}
							}
						} catch (IllegalAccessException e) {
							GoatedClient.LOGGER.error("Failed to save setting", e);
						}
					}
				}
				
				moduleData.add("settings", settings);
				modules.add(module.getName(), moduleData);
			}
			
			root.add("modules", modules);
			Files.writeString(configPath, gson.toJson(root));
			
		} catch (IOException e) {
			GoatedClient.LOGGER.error("Failed to save config", e);
		}
	}
	
	public void load() {
		if (!Files.exists(configPath)) {
			save();
			return;
		}
		
		try {
			String json = Files.readString(configPath);
			JsonObject root = gson.fromJson(json, JsonObject.class);
			JsonObject modules = root.getAsJsonObject("modules");
			
			for (Module module : GoatedClient.getInstance().getModuleManager().getModules()) {
				if (modules.has(module.getName())) {
					JsonObject moduleData = modules.getAsJsonObject(module.getName());
					
					if (moduleData.has("enabled")) {
						module.setEnabled(moduleData.get("enabled").getAsBoolean());
					}
					
					if (moduleData.has("settings")) {
						JsonObject settings = moduleData.getAsJsonObject("settings");
						
						for (Field field : module.getClass().getDeclaredFields()) {
							if (Setting.class.isAssignableFrom(field.getType())) {
								field.setAccessible(true);
								try {
									Setting<?> setting = (Setting<?>) field.get(module);
									if (setting != null && settings.has(setting.getName())) {
										if (setting instanceof ColorSetting colorSetting) {
											colorSetting.setValue(settings.get(setting.getName()).getAsInt());
										} else if (setting instanceof BooleanSetting booleanSetting) {
											booleanSetting.setValue(settings.get(setting.getName()).getAsBoolean());
										}
									}
								} catch (IllegalAccessException e) {
									GoatedClient.LOGGER.error("Failed to load setting", e);
								}
							}
						}
					}
				}
			}
			
		} catch (IOException e) {
			GoatedClient.LOGGER.error("Failed to load config", e);
		}
	}
}
