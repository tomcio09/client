package pl.goated.client.module.settings;

public class ColorSetting extends Setting<Integer> {
	public ColorSetting(String name, Integer defaultValue) {
		super(name, defaultValue);
	}
	
	public int getAlpha() {
		return (getValue() >> 24) & 0xFF;
	}
	
	public int getRed() {
		return (getValue() >> 16) & 0xFF;
	}
	
	public int getGreen() {
		return (getValue() >> 8) & 0xFF;
	}
	
	public int getBlue() {
		return getValue() & 0xFF;
	}
	
	public float getAlphaFloat() {
		return getAlpha() / 255.0f;
	}
	
	public float getRedFloat() {
		return getRed() / 255.0f;
	}
	
	public float getGreenFloat() {
		return getGreen() / 255.0f;
	}
	
	public float getBlueFloat() {
		return getBlue() / 255.0f;
	}
}
