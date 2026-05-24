package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.BooleanSetting;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class HudModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();
	
	public final BooleanSetting showWatermark;
	public final BooleanSetting showArrayList;
	public final BooleanSetting showCoordinates;
	
	public HudModule() {
		super("HUD", "Shows enabled modules on screen", Category.RENDER);
		
		showWatermark = new BooleanSetting("Show Watermark", true);
		showArrayList = new BooleanSetting("Show ArrayList", true);
		showCoordinates = new BooleanSetting("Show Coordinates", false);
		
		settings.add(showWatermark);
		settings.add(showArrayList);
		settings.add(showCoordinates);
	}
	
	public List<Setting<?>> getSettings() {
		return settings;
	}
}
