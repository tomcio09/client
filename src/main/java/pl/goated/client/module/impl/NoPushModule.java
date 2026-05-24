package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.FloatSetting;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class NoPushModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();

	public final FloatSetting pushStrength;

	// Losowe małe opóźnienie między działaniami - mniej przewidywalne
	private int tickDelay = 0;
	private static final int MIN_DELAY = 1;
	private static final int MAX_DELAY = 3;

	public NoPushModule() {
		super("NoPush", "Prevents being pushed by blocks", Category.MOVEMENT);

		pushStrength = new FloatSetting("Push Strength", 0f, 0f, 100f, "%");
		settings.add(pushStrength);
	}

	@Override
	public void onEnable() {
		tickDelay = 0;
	}

	@Override
	public void onDisable() {
		tickDelay = 0;
	}

	public List<Setting<?>> getSettings() {
		return settings;
	}

	// Zwraca mnożnik (0.0 = brak push, 1.0 = normalny push)
	public float getPushMultiplier() {
		return pushStrength.getValue() / 100f;
	}

	// Sprawdza czy moduł jest aktywny z losowym opóźnieniem
	//eby nie był zbyt regularny (trudniejszy do wykrycia)
	public boolean shouldApply() {
		if (!isEnabled()) return false;
		tickDelay++;
		if (tickDelay >= MIN_DELAY + (int)(Math.random() * (MAX_DELAY - MIN_DELAY))) {
			tickDelay = 0;
			return true;
		}
		return getPushMultiplier() <= 0.0f; // Zawsze blokuj jeśli 0%
	}
}
