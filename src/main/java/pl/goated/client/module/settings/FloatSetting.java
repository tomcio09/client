package pl.goated.client.module.settings;

public class FloatSetting extends Setting<Float> {
	private final float min;
	private final float max;
	private final String suffix;
	
	public FloatSetting(String name, float defaultValue, float min, float max, String suffix) {
		super(name, defaultValue);
		this.min = min;
		this.max = max;
		this.suffix = suffix;
	}
	
	public FloatSetting(String name, float defaultValue, float min, float max) {
		this(name, defaultValue, min, max, "");
	}
	
	@Override
	public void setValue(Float value) {
		super.setValue(Math.max(min, Math.min(max, value)));
	}
	
	public float getMin() { return min; }
	public float getMax() { return max; }
	public String getSuffix() { return suffix; }
	
	public float getPercent() {
		return (getValue() - min) / (max - min);
	}
	
	public void setFromPercent(float percent) {
		setValue(min + (max - min) * Math.max(0, Math.min(1, percent)));
	}
	
	public String getDisplayValue() {
		if (suffix.equals("%")) {
			return String.format("%.0f%s", getValue(), suffix);
		}
		return String.format("%.2f%s", getValue(), suffix);
	}
}
