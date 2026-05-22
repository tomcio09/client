package pl.goated.client.module;

import pl.goated.client.GoatedClient;

public abstract class Module {
	private final String name;
	private final String description;
	private final Category category;
	protected boolean enabled;
	
	public Module(String name, String description, Category category) {
		this.name = name;
		this.description = description;
		this.category = category;
		this.enabled = false;
	}
	
	public void toggle() {
		setEnabled(!enabled);
	}
	
	public void setEnabled(boolean enabled) {
		if (this.enabled == enabled) return;
		
		this.enabled = enabled;
		
		if (enabled) {
			onEnable();
		} else {
			onDisable();
		}
		
		// Only save if GoatedClient is fully initialized
		try {
			if (GoatedClient.getInstance() != null && 
			    GoatedClient.getInstance().getConfigManager() != null &&
			    GoatedClient.getInstance().getModuleManager() != null) {
				GoatedClient.getInstance().getConfigManager().save();
			}
		} catch (Exception e) {
			// Ignore errors during initialization
		}
	}
	
	public void onEnable() {}
	public void onDisable() {}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Category getCategory() {
		return category;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public enum Category {
		COMBAT("Combat"),
		MOVEMENT("Movement"),
		RENDER("Render"),
		PLAYER("Player"),
		MISC("Misc");
		
		private final String name;
		
		Category(String name) {
			this.name = name;
		}
		
		public String getName() {
			return name;
		}
	}
}
