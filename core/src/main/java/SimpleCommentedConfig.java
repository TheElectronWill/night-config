import com.electronwill.nightconfig.core.AbstractCommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.SimpleConfig;
import com.electronwill.nightconfig.core.UnmodifiableConfig;
import java.util.function.Predicate;

/**
 * A basic commented configuration.
 *
 * @author TheElectronWill
 */
public final class SimpleCommentedConfig extends AbstractCommentedConfig {

	private final Predicate<Class<?>> supportPredicate;

	/**
	 * Creates a SimpleCommentedConfig that supports the following types:
	 * <ul>
	 * <li>Integer, Long, Float and Double
	 * <li>Boolean
	 * <li>String
	 * <li>List and all its implementations
	 * <li>Config and all its implementations
	 * </ul>
	 */
	public SimpleCommentedConfig() {
		this.supportPredicate = SimpleConfig.BASIC_SUPPORT_PREDICATE;
	}

	/**
	 * Creates a SimpleCommentedConfig that uses the specified Predicate to determines what types
	 * it supports.
	 *
	 * @param supportPredicate the Predicate that returns true when the class it's given is
	 *                         supported by the config
	 */
	public SimpleCommentedConfig(Predicate<Class<?>> supportPredicate) {
		this.supportPredicate = supportPredicate;
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config. The supportPredicate will be
	 * {@link SimpleConfig#BASIC_SUPPORT_PREDICATE}.
	 *
	 * @param toCopy the config to copy
	 */
	public SimpleCommentedConfig(UnmodifiableConfig toCopy) {
		this(toCopy, SimpleConfig.BASIC_SUPPORT_PREDICATE);
	}

	/**
	 * Creates a SimpleConfig by copying a config.
	 *
	 * @param toCopy           the config to copy
	 * @param supportPredicate the Predicate that returns true when the class it's given is
	 *                         supported by the config
	 */
	public SimpleCommentedConfig(UnmodifiableConfig toCopy, Predicate<Class<?>> supportPredicate) {
		super(toCopy);
		this.supportPredicate = supportPredicate;
	}

	/**
	 * Creates a SimpleCommentedConfig by copying a config. The SimpleConfig will supports the same
	 * types as the specified config.
	 *
	 * @param toCopy the config to copy
	 */
	public SimpleCommentedConfig(Config toCopy) {
		this(toCopy, toCopy::supportsType);
	}

	@Override
	public boolean supportsType(Class<?> type) {
		return supportPredicate.test(type);
	}

	@Override
	public AbstractCommentedConfig clone() {
		return new SimpleCommentedConfig(this);
	}

	@Override
	protected AbstractCommentedConfig createSubConfig() {
		return new SimpleCommentedConfig();
	}
}