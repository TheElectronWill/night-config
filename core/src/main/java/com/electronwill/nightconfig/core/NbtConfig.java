package com.electronwill.nightconfig.core;

/**
 * A NBT configuration, which supports the additional types byte array and int array.
 *
 * @author TheElectronWill
 */
public interface NbtConfig extends Config {

	byte[] getByteArray(String path);

	void setByteArray(String path, byte[] value);

	int[] getIntArray(String path);

	void setIntArray(String path, int[] value);
}
