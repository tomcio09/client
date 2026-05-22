package pl.goated.client.module.impl;

import pl.goated.client.module.Module;
import pl.goated.client.module.settings.BooleanSetting;
import pl.goated.client.module.settings.ColorSetting;
import pl.goated.client.module.settings.Setting;

import java.util.ArrayList;
import java.util.List;

public class GuiModule extends Module {
	private final List<Setting<?>> settings = new ArrayList<>();
	
	public final ColorSetting borderColor;
	public final ColorSetting backgroundColor;
	public final ColorSetting textColor;
	public final BooleanSetting blur;
	
	public GuiModule() {
		super("GUI", "Customize GUI appearance", Category.RENDER);
		
		borderColor = new ColorSetting("Border Color", 0xFF000000);
		backgroundColor = new ColorSetting("Background Color", 0xFFFFFFFF);
		textColor = new ColorSetting("Text Color", 0xFF000000);
		blur = new BooleanSetting("Background Blur", true);
		
		settings.add(borderColor);
		settings.add(backgroundColor);
		settings.add(textColor);
		settings.add(blur);
		
		// Don't save on init - just enable it
		this.enabled = true;
	}
	
	public List<Setting<?>> getSettings() {
		return settings;
	}
}
