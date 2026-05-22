package pl.goated.client.module;

import pl.goated.client.module.impl.*;

import java.util.ArrayList;
import java.util.List;

public class ModuleManager {
	private final List<Module> modules = new ArrayList<>();
	
	public ModuleManager() {
		// Register modules
		register(new GuiModule());
		register(new HudModule());
		register(new AutoCripModule());
		register(new NoPushModule());
		register(new InteractionsModule());
	}
	
	private void register(Module module) {
		modules.add(module);
	}
	
	public List<Module> getModules() {
		return modules;
	}
	
	public Module getModuleByName(String name) {
		return modules.stream()
			.filter(m -> m.getName().equalsIgnoreCase(name))
			.findFirst()
			.orElse(null);
	}
	
	public List<Module> getModulesByCategory(Module.Category category) {
		return modules.stream()
			.filter(m -> m.getCategory() == category)
			.toList();
	}
}
