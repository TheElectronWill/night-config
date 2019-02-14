module com.electronwill.night.config.yaml {
	requires transitive com.electronwill.night.config.core;
	requires java.sql;
	requires snakeyaml;

	exports com.electronwill.nightconfig.yaml;
}