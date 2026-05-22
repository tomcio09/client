package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.BooleanSetting;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class InteractionsModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();
	
	public final BooleanSetting noMissDelay;
	public final BooleanSetting fastBreak;
	
	public InteractionsModule() {
		super("Interactions", "Modify interaction behavior", Category.PLAYER);
		
		noMissDelay = new BooleanSetting("No Miss Delay", true);
		fastBreak = new BooleanSetting("Fast Break", false);
		
		settings.add(noMissDelay);
		settings.add(fastBreak);
	}
	
	public List<Setting<?>> getSettings() {
		return settings;
	}
}
