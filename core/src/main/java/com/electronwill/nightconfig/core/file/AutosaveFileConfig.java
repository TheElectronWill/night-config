package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.utils.ObservedEntry;
import com.electronwill.nightconfig.core.utils.ObservedMap;

import java.util.Map;
import java.util.Optional;

/**
 * @author TheElectronWill
 */
final class AutosaveFileConfig extends FileConfigWrapper {
	AutosaveFileConfig(FileConfig config) {
		super(config);
	}

	@Override
	public Config.Entry getEntry(String[] path) {
		return new AutosaveEntry(config.getEntry(path));
	}

	@Override
	public <T> T set(AttributeType<T> attribute, String path, T value) {
		return saved(config.set(path, value));
	}

	@Override
	public <T> T add(AttributeType<T> attribute, String path, T value) {
		return saved(config.add(attribute, path, value));
	}

	@Override
	public <T> T remove(AttributeType<T> attribute, String path) {
		return saved(config.remove(attribute, path));
	}

	@Override
	public <T> T set(String[] path, Object value) {
		return saved(config.set(path, value));
	}

	@Override
	public Object add(String[] path, Object value) {
		return saved(config.add(path, value));
	}

	@Override
	public <T> T remove(String[] path) {
		return saved(config.remove(path));
	}

	@Override
	public Map<String, Object> valueMap() {
		return new ObservedMap<>(config.valueMap(), this::save);
	}

	private <T> T saved(T value) {
		save();
		return value;
	}

	private class AutosaveEntry implements Config.Entry {
		private final Config.Entry entry;

		private AutosaveEntry(Config.Entry entry) {
			this.entry = entry;
		}

		@Override
		public String getKey() {
			return entry.getKey();
		}

		@Override
		public <T> T addValue(Object value) {
			return saved(entry.addValue(value));
		}

		@Override
		public <T> T setValue(Object value) {
			return saved(entry.setValue(value));
		}

		@Override
		public <T> T removeValue() {
			return setValue(null);
		}

		@Override
		public <T> T set(AttributeType<T> attribute, T value) {
			return saved(entry.set(attribute, value));
		}

		@Override
		public <T> T add(AttributeType<T> attribute, T value) {
			return saved(entry.add(attribute, value));
		}

		@Override
		public <T> T remove(AttributeType<T> attribute) {
			return saved(entry.remove(attribute));
		}

		@Override
		public <T> Optional<T> getOptional(AttributeType<T> attribute) {
			return entry.getOptional(attribute);
		}

		@Override
		public void clearAttributes() {
			entry.clearAttributes();
			save();
		}

		@Override
		public Iterable<? extends Config.Attribute<?>> attributes() {
			return entry.attributes();
		}

		@Override
		public <T> Map.Entry<String, T> toMapEntry() {
			return new ObservedEntry<>(entry.toMapEntry(), AutosaveFileConfig.this::save);
		}

		@Override
		public <T> T getValue() {
			return entry.getValue();
		}

		@Override
		public boolean has(AttributeType<?> attribute) {
			return entry.has(attribute);
		}

		@Override
		public <T> T get(AttributeType<T> attribute) {
			return entry.get(attribute);
		}

		@Override
		public int hashCode() {
			return entry.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return entry.equals(obj);
		}
	}
}