package com.electronwill.nightconfig.yaml;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.constructor.SafeConstructor;
import org.yaml.snakeyaml.nodes.Tag;

public class SaferConstructor extends SafeConstructor {

	public SaferConstructor() {
		super(new LoaderOptions());
		this.yamlConstructors.put(new Tag(java.util.Map.class), new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(java.util.HashMap.class), new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(java.util.LinkedHashMap.class), new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(java.util.SortedMap.class), new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(java.util.List.class), new ConstructYamlSeq());
		this.yamlConstructors.put(new Tag(java.util.ArrayList.class), new ConstructYamlSeq());
		this.yamlConstructors.put(new Tag(java.util.LinkedList.class), new ConstructYamlSeq());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.Config.class), new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.CommentedConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.AbstractConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.AbstractCommentedConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.file.FileConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.file.CommentedFileConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.concurrent.ConcurrentConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.concurrent.ConcurrentCommentedConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.concurrent.StampedConfig.class),
				new ConstructYamlMap());
		this.yamlConstructors.put(new Tag(com.electronwill.nightconfig.core.concurrent.SynchronizedConfig.class),
				new ConstructYamlMap());
	}

}
