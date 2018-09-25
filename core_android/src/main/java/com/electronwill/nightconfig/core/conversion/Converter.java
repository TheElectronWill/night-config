package com.electronwill.nightconfig.core.conversion;

/**
 * Performs conversions between field values and config values.
 *
 * @author TheElectronWill
 */
public interface Converter<FieldType, ConfigValueType> {
	FieldType convertToField(ConfigValueType value);

	ConfigValueType convertFromField(FieldType value);
}
