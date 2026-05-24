package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.FloatSetting;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class NoPushModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();

	public final FloatSetting pushStrength;

	public NoPushModule() {
		super("NoPush", "Prevents being pushed by solid blocks", Category.MOVEMENT);

		// 0% = brak wypychania, 100% = normalne wypychanie
		pushStrength = new FloatSetting("Push Strength", 0f, 0f, 100f, "%");
		settings.add(pushStrength);
	}

	@Override
	public void onEnable() {}

	@Override
	public void onDisable() {}

	public List<Setting<?>> getSettings() {
		return settings;
	}

	public float getPushMultiplier() {
		return pushStrength.getValue() / 100f;
	}
}
