package com.electronwill.nightconfig.core.spec;

import com.electronwill.nightconfig.core.*;
import com.electronwill.nightconfig.core.Config.Attribute;
import com.electronwill.nightconfig.core.Config.Entry;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.electronwill.nightconfig.core.utils.StringUtils.single;

/**
 * Represents a specification for a configuration. With a ConfigSpec you can define mandatory
 * "properties" that the config's values must have and then check that the config is correct, and
 * even correct it automatically!
 * <h1>Defining entries</h1>
 * <p>
 * Use the "define" methods to define that some entry must be present in the config, and how its
 * value must be. You have to specify - at least - the path of the value and a default value that
 * will be used to replace the config's value in case it's incorrect.<br>
 * For instance, the following code defines a value with path "a.b" and which must be a String:
 * <pre>configSpec.define("a.b", "defaultString");</pre>
 *
 * <h2>Validators</h2>
 * <p>
 * Some methods (like the one used in the previous paragraph) automatically generate the rules that
 * make a config value correct or incorrect. But you can provide your own rule by specifying a
 * "validator": a {@link Predicate} that returns {@code true} if and only if the given value is
 * correct.<br>
 * For instance, this defines a value "arraylist" that must be an {@code ArrayList}:
 * {@code configSpec.define("arraylist", new ArrayList(), o -> o instanceof ArrayList);}
 *
 * <h2>Suppliers of default value</h2>
 * <p>
 * If the default value is heavy to create you should use a {@link Supplier} instead of creating a
 * default value, which is useless if the config's value happens to be correct.<br>
 * For instance, the code in the previous paragraph could be rewritten like this:
 * {@code configSpec.define("heavy", () -> new ArrayList(), o -> o instanceof ArrayList);}
 *
 * <h1>Checking configurations</h1>
 * <p>
 * Use the "isCorrect" methods to check whether a configuration is correct or not. A configuration
 * is correct if and only if:
 * <ol>
 * <li>Each entry defined in the spec is present in the config.
 * <li>Each entry in the config is defined in the spec.
 * <li>Each entry in the config has a correct value according to the spec.
 * </ol>
 *
 * <h1>Correcting configurations</h1>
 * <p>
 * Use the "correct" methods to correct a configuration. The correction behaves like this:
 * <ol>
 * <li>Each entry that is defined in the spec but absent from the config is added to the config,
 * with the default value defined in the spec.
 * <li>Each entry that isn't defined in the spec is removed from the config.
 * <li>Each incorrect config value is replaced by the default value specified in the spec.
 * </ol>
 *
 * @author TheElectronWill
 */
@SuppressWarnings("unchecked")
public class ConfigSpec {
	private final Config storage = new MemoryConfig();
	private boolean removeUnspecEntries = true;
	private boolean removeUnspecAttributes = true;

	public boolean removesUnspecEntries() {
		return removeUnspecEntries;
	}

	public void removeUnspecEntries(boolean removeUnspecEntries) {
		this.removeUnspecEntries = removeUnspecEntries;
	}

	public boolean removesUnspecAttributes() {
		return removeUnspecAttributes;
	}

	public void removeUnspecAttributes(boolean removeUnspecAttributes) {
		this.removeUnspecAttributes = removeUnspecAttributes;
	}

	public void define(String path, ValueCorrecter correcter) {
		Objects.requireNonNull(correcter);
		storage.set(path, correcter);
	}

	public void define(String[] path, ValueCorrecter<?> correcter) {
		Objects.requireNonNull(correcter);
		storage.set(path, correcter);
	}

	public <T> void define(AttributeType<T> attribute, String path, ValueCorrecter<T> correcter) {
		Objects.requireNonNull(correcter);
		storage.set(correcter(attribute), path, correcter); // yes it's wrongly typed, but practical
	}

	public <T> void define(AttributeType<T> attribute, String[] path, ValueCorrecter<T> correcter) {
		Objects.requireNonNull(correcter);
		storage.set(correcter(attribute), path, correcter);
	}

	public void undefine(String path) {
		storage.remove(path);
	}

	public void undefine(String[] path) {
		storage.remove(path);
	}

	public void undefine(AttributeType<?> attribute, String path) {
		storage.remove(attribute, path);
	}

	public void undefine(AttributeType<?> attribute, String[] path) {
		storage.remove(attribute, path);
	}

	public boolean isDefined(String path) {
		return storage.contains(path);
	}

	public boolean isDefined(String[] path) {
		return storage.contains(path);
	}

	public boolean isDefined(AttributeType<?> attribute, String path) {
		return storage.has(correcter(attribute), path);
	}

	public boolean isDefined(AttributeType<?> attribute, String[] path) {
		return storage.has(correcter(attribute), path);
	}

	public CorrectionResult<?> correct(String path, Object value) {
		return storage.<ValueCorrecter<?>>get(path).correct(value);
	}

	public CorrectionResult<?> correct(String[] path, Object value) {
		return storage.<ValueCorrecter<?>>get(path).correct(value);
	}

	public <T> CorrectionResult<T> correct(AttributeType<T> attribute, String path, Object value) {
		return storage.get(correcter(attribute), path).correct(value);
	}

	public <T> CorrectionResult<T> correct(AttributeType<T> attribute, String[] path, Object value) {
		return storage.get(correcter(attribute), path).correct(value);
	}

	public Config correctCopy(UnmodifiableConfig config) {
		return correctCopy(config, CorrectionListener.noop());
	}

	public Config correctCopy(UnmodifiableConfig config, CorrectionListener listener) {
		Config copy = new MemoryConfig(config);
		correct(copy, listener);
		return copy;
	}

	public int correct(Config config) {
		CorrectionCounter counter = new CorrectionCounter();
		correct(config, counter);
		return counter.correctionCount();
	}

	public void correct(Config config, CorrectionListener listener) {
		Deque<String> stack = new ArrayDeque<>(8); // path depth is usually small
		correct(storage, config, stack, listener, removeUnspecEntries, removeUnspecAttributes);
	}

	private static void correct(Config spec,
								Config target,
								Deque<String> pathStack,
								CorrectionListener listener,
								boolean rmUnspecEntries,
								boolean rmUnspecAttr) {
		// First step: remove the unspecified entries, if keepUnspecifiedEntries is false
		if (rmUnspecEntries) {
			removeUnspecified(spec, target);
		}
		// Second step: fix the incorrect entries and add the missing ones
		for (Entry specEntry : spec.entries()) {
			final String specKey = specEntry.getKey();
			final Object specValue = specEntry.getValue();
			final String[] localPath = single(specKey);
			final String[] fullPath = pathStack.toArray(single(null)); // provision 1 slot
			final Config.Entry actual = target.getEntry(localPath);

			pathStack.addLast(specKey);
			Config subTarget = null;
			if (actual == null) {
				// Missing entry
				subTarget = correctMissing(target, specValue, listener, localPath, fullPath);
			} else if (specValue instanceof Config && !(actual.getValue() instanceof Config)) {
				// Incompatible entry: expected a configuration
				subTarget = correctIncompatible(target, actual, listener, fullPath);
			} else {
				// Expected entry, let's check all its attributes (including its value)
				Iterator<? extends Attribute<?>> it = actual.attributes().iterator();
				while (it.hasNext()) {
					Attribute<?> entry = it.next();
					correctAttribute(it, specEntry, entry, listener, fullPath, rmUnspecAttr);
				}
			}
			// Correct recursively
			if (subTarget != null) {
				Config subSpec = (Config)specValue;
				correct(subSpec, subTarget, pathStack, listener, rmUnspecEntries, rmUnspecAttr);
			}
			pathStack.removeLast();
		}
	}

	private static void removeUnspecified(Config spec, Config target) {
		Iterator<? extends Entry> it = target.entries().iterator();
		while (it.hasNext()) {
			Entry entry = it.next();
			if (!spec.contains(single(entry.getKey()))) {
				it.remove();
			}
		}
	}

	private static Config correctMissing(Config target,
										 Object specValue,
										 CorrectionListener l,
										 String[] localPath,
										 String[] fullPath) {
		CorrectionResult<Object> correction;
		Config sub = null;
		if (specValue instanceof Config) {
			// Missing subconfig
			sub = target.createSubConfig();
			target.set(localPath, sub);
			correction = CorrectionResult.replacedBy(sub);
		} else {
			// Missing other value
			ValueCorrecter<Object> correcter = (ValueCorrecter<Object>)specValue;
			correction = correcter.correct(null);
			target.set(localPath, correction.replacementValue());
		}
		l.onCorrect(fullPath, StandardAttributes.VALUE, correction);
		return sub;
	}

	private static Config correctIncompatible(Config target,
											  Config.Entry actual,
											  CorrectionListener l,
											  String[] fullPath) {
		Config subTarget = target.createSubConfig();
		actual.setValue(subTarget);
		actual.clearExtraAttributes();
		CorrectionResult<Object> correction = CorrectionResult.replacedBy(subTarget);
		l.onCorrect(fullPath, StandardAttributes.VALUE, correction);
		return subTarget;
	}

	private static <T> void correctAttribute(Iterator<?> it,
											 Entry specEntry,
											 Attribute<T> entry,
											 CorrectionListener l,
											 String[] fullPath,
											 boolean rmUnspecAttr) {
		AttributeType<T> attribute = entry.getType();
		ValueCorrecter<T> correcter = specEntry.get(correcter(attribute));
		if (correcter != null) {
			CorrectionResult<T> correction = correcter.correct(entry.getValue());
			if (correction.isReplaced()) {
				entry.setValue(correction.replacementValue());
			} else if (correction.isRemoved()) {
				it.remove();
			}
			l.onCorrect(fullPath, attribute, correction);
		} else if (rmUnspecAttr) {
			it.remove();
			l.onCorrect(fullPath, attribute, CorrectionResult.removed());
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> AttributeType<ValueCorrecter<T>> correcter(AttributeType<T> attr) {
		if (attr == StandardAttributes.VALUE) {
			// Store the correcter of VALUE directly in the entry value
			return (AttributeType<ValueCorrecter<T>>)attr;
		}
		return new AttributeCorrecter<>(attr);
	}
}
