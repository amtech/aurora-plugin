package aurora.plugin.source.gen;

import java.text.DateFormat;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import aurora.plugin.source.gen.builders.ISourceBuilder;
import uncertain.composite.CompositeMap;
import uncertain.composite.IterationHandle;

public class BuilderSession {
	private SourceGenManager sourceGenManager;

	private StringBuilder eventResult = new StringBuilder();
	

	public BuilderSession(SourceGenManager sourceGenManager) {
		super();
		this.sourceGenManager = sourceGenManager;
	}

	private CompositeMap model;

	private CompositeMap context;

	private CompositeMap current_context;

	private CompositeMap current_model;

	private Map<String, Object> configMap;

	public SourceGenManager getSourceGenManager() {
		return sourceGenManager;
	}

	public void setSourceGenManager(SourceGenManager sourceGenManager) {
		this.sourceGenManager = sourceGenManager;
	}

	public CompositeMap getContext() {
		return context;
	}

	public void setContext(CompositeMap context) {
		this.context = context;
	}

	public CompositeMap getModel() {
		return model;
	}

	public void setModel(CompositeMap model) {
		this.model = model;
	}

	public CompositeMap getCurrentModel() {
		return current_model;
	}

	public void setCurrentModel(CompositeMap current_model) {
		this.current_model = current_model;
	}

	public void appendContext(CompositeMap context) {
		CompositeMap parent = this.getCurrentModel().getParent();
		if (parent == null) {
			this.context = context;
		} else {
			String modelId = parent.getString("markid", "");
			if ("".equals(modelId) == false) {
				CompositeMap parentContext = lookUpParentContext(modelId);
				if (parentContext != null) {
					parentContext.addChild(context);
				}
			}
		}
		this.setCurrentContext(context);
	}

	private CompositeMap lookUpParentContext(final String modelId) {
		if (this.current_context != null) {
			String c_id = current_context.getString("markid", "");
			if (modelId.equals(c_id)) {
				return current_context;
			}
		}
		final CompositeMap[] maps = new CompositeMap[1];
		this.context.iterate(new IterationHandle() {
			public int process(CompositeMap map) {
				String c_id = map.getString("markid", "");
				if (modelId.equals(c_id)) {
					maps[0] = map;
					return IterationHandle.IT_BREAK;
				}
				return IterationHandle.IT_CONTINUE;
			}
		}, true);
		return maps[0];
	}

	public CompositeMap getCurrentContext() {
		return current_context;
	}

	public void setCurrentContext(CompositeMap current_context) {
		this.current_context = current_context;
	}

	public String buildComponent(CompositeMap model) {
		return this.sourceGenManager.buildComponent(this, model);
	}

	public void appendResultln(String result) {
		if ("".equals(result))
			return;
		getEventResult().append(result);
		getEventResult().append("\n");
	}

	public void appendResult(String result) {
		if ("".equals(result))
			return;
		getEventResult().append(result);
	}

	public void clearEventResult() {
		setEventResult(new StringBuilder());
	}

	public StringBuilder getEventResult() {
		return eventResult;
	}

	public void setEventResult(StringBuilder eventResult) {
		this.eventResult = eventResult;
	}

	public BuilderSession getCopy() {
		BuilderSession bs = new BuilderSession(this.sourceGenManager);
		bs.setContext(this.getContext());
		bs.setCurrentContext(this.getCurrentContext());
		bs.setCurrentModel(this.getCurrentModel());
		bs.setModel(this.getModel());
		bs.configMap = configMap;
		return bs;
	}

	public IDGenerator getIDGenerator() {
		return sourceGenManager.getIDGenerator();
	}

	public Object execActionEvent(String event, CompositeMap context) {
		StringBuilder sb = new StringBuilder();
		Collection<String> values = sourceGenManager.getBuilders().values();
		for (String clazz : values) {
			ISourceBuilder newInstance = sourceGenManager
					.createNewInstance(clazz);
			if (newInstance != null) {
				BuilderSession bs = this.getCopy();
				((ISourceBuilder) newInstance).actionEvent(event, bs);
				sb.append(bs.getEventResult());
			}
		}
		return sb.toString();
	}

	public ModelMapParser createModelMapParser(CompositeMap model) {
		return sourceGenManager.createModelMapParser(model);
	}

	public void addConfig(String key, Object value) {
		if (configMap == null)
			configMap = new HashMap<String, Object>();
		configMap.put(key, value);
	}

	public Map<String, String> defineConfig() {
		String property = System.getProperty("user.name");
		String format = DateFormat.getDateInstance().format(
				new java.util.Date());
		Map<String, String> config = new HashMap<String, String>();
		config.put("encoding", "UTF-8");
		config.put("date", format);
		config.put("author", property);
		config.put("revision", "1.0");
		config.put("copyright", "add by aurora_ide team");
		config.put("template_type", getModel().getString("template_type", ""));
		if (configMap != null) {
			Set<String> keySet = configMap.keySet();
			for (String k : keySet) {
				config.put(k, ""+configMap.get(k));
			}
		}
		return config;
	}

	public Object getConfig(String key) {
		if (configMap != null)
			return configMap.get(key);
		return null;
	}
}
