package com.electronwill.nightconfig.core.file;

import com.electronwill.nightconfig.core.AttributeType;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.EntryData;
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
	public EntryData getData(String[] path) {
		return new AutosaveEntryData(config.getData(path));
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

	private class AutosaveEntryData implements EntryData {
		private final EntryData data;

		private AutosaveEntryData(EntryData data) {
			this.data = data;
		}

		@Override
		public <T> T addValue(Object value) {
			return saved(data.addValue(value));
		}

		@Override
		public <T> T setValue(Object value) {
			return saved(data.setValue(value));
		}

		@Override
		public <T> T set(AttributeType<T> attribute, T value) {
			return saved(data.set(attribute, value));
		}

		@Override
		public <T> T add(AttributeType<T> attribute, T value) {
			return saved(data.add(attribute, value));
		}

		@Override
		public <T> T remove(AttributeType<T> attribute) {
			return saved(data.remove(attribute));
		}

		@Override
		public <T> Optional<T> getOptional(AttributeType<T> attribute) {
			return data.getOptional(attribute);
		}

		@Override
		public void clearExtraAttributes() {
			data.clearExtraAttributes();
			save();
		}

		@Override
		public Iterable<? extends Config.AttributeEntry<?>> attributes() {
			return data.attributes();
		}

		@Override
		public Config.Entry toConfigEntry(String key) {
			return new AutosaveConfigEntry(data.toConfigEntry(key));
		}

		@Override
		public <K, V> Map.Entry<K, V> toMapEntry(K key) {
			return new ObservedEntry<>(data.toMapEntry(key), AutosaveFileConfig.this::save);
		}

		@Override
		public <T> T getValue() {
			return data.getValue();
		}

		@Override
		public boolean has(AttributeType<?> attribute) {
			return data.has(attribute);
		}

		@Override
		public <T> T get(AttributeType<T> attribute) {
			return data.get(attribute);
		}

		@Override
		public int hashCode() {
			return data.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return data.equals(obj);
		}
	}

	private class AutosaveConfigEntry implements Config.Entry {
		private final Config.Entry entry;

		private AutosaveConfigEntry(Config.Entry entry) {
			this.entry = entry;
		}

		@Override
		public <T> T set(AttributeType<T> attribute, T value) {
			return saved(entry.set(attribute, value));
		}

		@Override
		public <T> T setValue(Object value) {
			return saved(entry.setValue(value));
		}

		@Override
		public Iterable<? extends Config.AttributeEntry<?>> attributes() {
			return entry.attributes();
		}

		@Override
		public String getKey() {
			return entry.getKey();
		}

		@Override
		public <T> T get(AttributeType<T> attribute) {
			return entry.get(attribute);
		}

		@Override
		public <T> Optional<T> getOptional(AttributeType<T> attribute) {
			return entry.getOptional(attribute);
		}
	}
}