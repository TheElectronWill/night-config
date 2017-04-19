package com.electronwill.nightconfig.core.conversion;

/**
 * @author TheElectronWill
 */
public interface Converter<FieldType, ConfigValueType> {
	FieldType convertToField(ConfigValueType value);

	ConfigValueType convertFromField(FieldType value);
}
