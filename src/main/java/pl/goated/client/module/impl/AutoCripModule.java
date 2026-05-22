package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class AutoCripModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();
	
	public AutoCripModule() {
		super("AutoCrip", "Automatically performs crip actions", Category.PLAYER);
	}
	
	public List<Setting<?>> getSettings() {
		return settings;
	}
}
