package com.electronwill.nightconfig.core.serde;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigFormat;
import com.electronwill.nightconfig.core.serde.annotations.SerdeComment;
import com.electronwill.nightconfig.core.serde.annotations.SerdeKey;
import com.electronwill.nightconfig.core.serde.annotations.SerdeSkipSerializingIf;

public final class SerializerContext {
	final ObjectSerializer settings;
	final Supplier<? extends ConfigFormat<?>> formatSupplier;
	final Supplier<? extends Config> configSupplier;

	SerializerContext(ObjectSerializer settings,
			Supplier<? extends ConfigFormat<?>> formatSupplier,
			Supplier<? extends Config> configSupplier) {
		this.settings = settings;
		this.formatSupplier = formatSupplier;
		this.configSupplier = configSupplier;
	}

	/**
	 * @return the current {@code ConfigFormat}
	 */
	public ConfigFormat<?> configFormat() {
		return formatSupplier.get();
	}

	/**
	 * @return a new {@code Config}
	 */
	public Config createConfig() {
		return configSupplier.get();
	}

	/**
	 * @return a new {@code CommentedConfig}
	 */
	public CommentedConfig createCommentedConfig() {
		return CommentedConfig.fake(createConfig());
	}

	/**
	 * Serializes a single value.
	 *
	 * @param value value coming from a field that we are serializing
	 * @throws SerdeException if no suitable serializer is found
	 * @return a value that can be added to a config
	 */
	public Object serializeValue(Object value) {
		ValueSerializer<Object, ?> serializer = settings.findValueSerializer(value, this);
		return serializer.serialize(value, this);
	}

	/**
	 * Serializes an object as a {@code Config} by transforming its fields into
	 * configuration entries in {@code destination}.
	 *
	 * @param source the object that we are serializing
	 * @param destination the config that we are modifying (result of the serialization)
	 */
	public void serializeFields(Object source, Config destination) {
		// loop through the class hierarchy of the source type
		Class<?> cls = source.getClass();
		while (cls != Object.class) {
			for (Field field : cls.getDeclaredFields()) {
				if (preCheck(field)) {
					// read the fields's value
					Object value;
					try {
						value = field.get(source);
					} catch (Exception e) {
						throw new SerdeException("Failed to read field `" + field + "`", e);
					}

					// skip the field if the annotation say so
					if (skipField(field, source, value)) {
						continue; // don't serialize, go to the next field
					}

					// get the config key and config comment
					List<String> path = Collections.singletonList(configKey(field));
					String comment = configComment(field);

					// Try to apply the default value.
					// (Note that this is not symmetrical with the deserialization process: the
					// default value is always a Java value, and we will serialize this default
					// value instead of the field's value.)
					Supplier<?> defaultValueSupplier = settings.findDefaultValueSupplier(value, field, source);
					if (defaultValueSupplier != null) {
						try {
							value = defaultValueSupplier.get();
						} catch (Exception e) {
							throw new SerdeException("Error in default value provider for field " + field);
						}
					}

					// find the right serializer
					ValueSerializer<Object, ?> serializer = settings.findValueSerializer(value, this);

					// serialize the value and modify the destination
					try {
						Object serialized = serializer.serialize(value, this);
						destination.set(path, serialized);
						if (comment != null && (destination instanceof CommentedConfig)) {
							((CommentedConfig) destination).setComment(path, comment);
						}
					} catch (Exception ex) {
						throw new SerdeException(
								"Error during serialization of field `" + field
										+ "` with serializer " + serializer,
								ex);
					}
				}
			}
			cls = cls.getSuperclass();
		}
	}

	private String configKey(Field field) {
		SerdeKey keyAnnot = field.getAnnotation(SerdeKey.class);
		return keyAnnot == null ? field.getName() : keyAnnot.value();
	}

	private String configComment(Field field) {
		SerdeComment[] commentAnnots = field.getDeclaredAnnotationsByType(SerdeComment.class);
		if (commentAnnots.length == 0) {
			return null;
		}
		String comment = commentAnnots[0].value();
		for (int i = 1; i < commentAnnots.length; i++) {
			comment += "\n";
			comment += commentAnnots[i].value();
		}
		return comment;
	}

	/**
	 * @return true if the field should be skipped
	 */
	@SuppressWarnings("unchecked")
	private boolean skipField(Field field, Object fieldContainer, Object fieldValue) {
		SerdeSkipSerializingIf annot = field.getAnnotation(SerdeSkipSerializingIf.class);
		if (annot == null) {
			return false;
		}
		try {
			Predicate<?> skipPredicate = AnnotationProcessor.resolveSkipSerializingIfPredicate(annot, fieldContainer,
					field);
			return ((Predicate<Object>) skipPredicate).test(fieldValue);
		} catch (Exception e) {
			String msg = "Failed to resolve or apply skip predicate for serialization of field " + field;
			throw new SerdeException(msg, e);
		}
	}

	private boolean preCheck(Field field) {
		int mods = field.getModifiers();
		if (Modifier.isStatic(mods) || field.isSynthetic()) {
			return false;
		}
		if (Modifier.isTransient(mods) && settings.applyTransientModifier) {
			return false;
		}
		if (Modifier.isFinal(mods) || !Modifier.isPublic(mods)) {
			field.setAccessible(true);
		}
		return true;
	}
}
