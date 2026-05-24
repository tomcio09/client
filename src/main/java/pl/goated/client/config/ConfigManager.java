package pl.goated.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;
import pl.goated.client.GoatedClient;
import pl.goated.client.module.Module;
import pl.goated.client.module.settings.BooleanSetting;
import pl.goated.client.module.settings.ColorSetting;
import pl.goated.client.module.settings.FloatSetting;
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
			if (GoatedClient.getInstance() == null || 
			    GoatedClient.getInstance().getModuleManager() == null) return;
			
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
								} else if (setting instanceof FloatSetting) {
									settings.addProperty(setting.getName(), (Float) setting.getValue());
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
			
		} catch (Exception e) {
			GoatedClient.LOGGER.error("Failed to save config", e);
		}
	}
	
	public void load() {
		try {
			if (!Files.exists(configPath)) {
				save();
				return;
			}
			
			String json = Files.readString(configPath);
			JsonObject root = gson.fromJson(json, JsonObject.class);
			JsonObject modules = root.getAsJsonObject("modules");
			if (modules == null) return;
			
			for (Module module : GoatedClient.getInstance().getModuleManager().getModules()) {
				if (!modules.has(module.getName())) continue;
				
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
									if (setting instanceof ColorSetting cs) {
										cs.setValue(settings.get(setting.getName()).getAsInt());
									} else if (setting instanceof BooleanSetting bs) {
										bs.setValue(settings.get(setting.getName()).getAsBoolean());
									} else if (setting instanceof FloatSetting fs) {
										fs.setValue(settings.get(setting.getName()).getAsFloat());
									}
								}
							} catch (IllegalAccessException e) {
								GoatedClient.LOGGER.error("Failed to load setting", e);
							}
						}
					}
				}
			}
			
		} catch (Exception e) {
			GoatedClient.LOGGER.error("Failed to load config", e);
		}
	}
}
