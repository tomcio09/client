package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class NoPushModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();
	
	public NoPushModule() {
		super("NoPush", "Prevents being pushed by entities", Category.MOVEMENT);
	}
	
	@Override
	public void onEnable() {
		// Implementation will be added with mixins
	}
	
	@Override
	public void onDisable() {
		// Implementation will be added with mixins
	}
	
	public List<Setting<?>> getSettings() {
		return settings;
	}
}
