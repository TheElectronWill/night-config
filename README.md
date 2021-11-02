![Night Config](logo.png)

[![Maven Central](https://img.shields.io/maven-central/v/com.electronwill.night-config/core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.electronwill.night-config%22)
[![Javadocs](http://javadoc.io/badge/com.electronwill.night-config/core.svg)](http://javadoc.io/doc/com.electronwill.night-config/core)
[![Build Status](https://travis-ci.com/TheElectronWill/Night-Config.svg?branch=master)](https://travis-ci.com/TheElectronWill/Night-Config)

# Introduction
NightConfig is a powerful yet easy-to-use java configuration library, written in Java 8.

It supports the following formats:
- [JSON](http://www.json.org/)
- [YAML v1.1](http://yaml.org/)
- [TOML v1.0](https://github.com/toml-lang/toml)
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
List<String> names = config.get("users_list"); // Generic return type!
long id = config.getLong("account.id"); // Compound path: key "id" in subconfig "account"
int points = config.getIntOrElse("account.score", defaultScore); // Default value

config.set("account.score", points*2);

String comment = config.getComment("user");
// NightConfig saves the config's comments (for TOML and HOCON)

// config.save(); not needed here thanks to autosave()
config.close(); // Close the FileConfig once you're done with it :)
```


# Project building
NightConfig is built with Gradle. The project is divided in several modules, the "core" module plus one module per supported configuration format.

The releases are available on [Maven Central](https://search.maven.org/search?q=com.electronwill.night-config) and [JitPack](https://jitpack.io/#TheElectronWill/Night-Config).

# Android
Older versions of Android don't provide the packages `java.util.function` and `java.nio.file`, which
NightConfig heavily uses. If you encounter issues on android you can use the special version that
I've made for you by adding `_android` to the modules' names.

Please [read the wiki](https://github.com/TheElectronWill/Night-Config/wiki/Modules-and-dependencies) for more information.

# Repo branches
The repository has several branches:
- Branches of old unmaintened versions:
[stable-1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-1.x),
[stable-2.x](https://github.com/TheElectronWill/Night-Config/tree/stable-2.x)
[stable-2.1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-2.1.x)
- Current in-development branch: [master](https://github.com/TheElectronWill/Night-Config/tree/master)
