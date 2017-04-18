package com.electronwill.nightconfig.core.conversion;

/**
 * @author TheElectronWill
 */
public interface Converter<FieldType, ConfigValueType> {
	FieldType convertRead(ConfigValueType value);

	ConfigValueType convertWrite(FieldType value);
}
