![Night Config](logo.png)

[![Maven Central](https://img.shields.io/maven-central/v/com.electronwill.night-config/core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.electronwill.night-config%22)
[![](https://jitpack.io/v/TheElectronWill/Night-Config.svg)](https://jitpack.io/#TheElectronWill/Night-Config)
[![](https://img.shields.io/badge/javadoc-core-blue.svg)](https://jitpack.io/com/github/TheElectronWill/Night-Config/core/3.3.0/javadoc/index.html)
[![](https://img.shields.io/github/commits-since/TheElectronWill/Night-Config/v3.0.svg)](https://github.com/TheElectronWill/Night-Config/compare/v3.0...master)

# Introduction
NightConfig is a powerful yet easy-to-use java configuration library, written in Java 8.

It supports the following formats:
- [JSON](http://www.json.org/)
- [YAML v1.1](http://yaml.org/)
- [TOML v0.5](https://github.com/toml-lang/toml)
- [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)

# How to use
- Code examples are available [here.](https://github.com/TheElectronWill/Night-Config/tree/master/examples/src/main/java)
  *Note: these examples are for the last **stable** version, and may not work with the master branch.*
- You can also read the [wiki](https://github.com/TheElectronWill/Night-Config/wiki).

## Glimpse
```java
// Simple builder:
FileConfig conf = FileConfig.of("the/file/config.toml");

// Advanced builder, default resource, autosave and much more (-> cf the wiki)
CommentedFileConfig config = CommentedFileConfig.builder("myConfig.toml").defaultResource("defaultConfig.toml").autosave().build();
config.load(); // This actually reads the config

String name = config.get("username"); // Generic return type!
int id = config.get("account.id"); // Compound path: key "id" in subconfig "account"
int points = config.getOrElse("account.score", defaultScore); // Default value

config.set("account.score", points*2);

String comment = config.getComment("user");
// NightConfig saves the config's comments (for TOML and HOCON)

// config.save(); not needed here thanks to autosave()
config.close(); // Close the FileConfig once you're done with it :)
```


# Project management
NightConfig is managed with gradle. It is divided in several modules, the "core" module plus one module per supported format of configuration.

The project is available on [jitpack](https://jitpack.io/#TheElectronWill/Night-Config).

The repository has several branches:
- Branches of old unmaintened versions:
[stable-1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-1.x),
[stable-2.x](https://github.com/TheElectronWill/Night-Config/tree/stable-2.x)
[stable-2.1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-2.1.x)
- Current in-development branch: [master](https://github.com/TheElectronWill/Night-Config/tree/master)
