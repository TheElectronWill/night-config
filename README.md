![Night Config](logo.png)

[![Maven Central](https://img.shields.io/maven-central/v/com.electronwill.night-config/core.svg)](https://search.maven.org/#search%7Cga%7C1%7Cg%3A%22com.electronwill.night-config%22)
[![Javadocs](https://javadoc.io/badge/com.electronwill.night-config/core.svg)](https://javadoc.io/doc/com.electronwill.night-config/core)
[![Build Status](https://travis-ci.com/TheElectronWill/Night-Config.svg?branch=master)](https://travis-ci.com/TheElectronWill/Night-Config)

# Introduction

NightConfig is a powerful yet easy-to-use java configuration library, written in Java 8.

It supports the following formats:
- [JSON](https://www.json.org/)
- [YAML v1.1](https://yaml.org/)
- [TOML v1.0](https://github.com/toml-lang/toml)
- [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)

# How to use

- Please read the extensive [wiki](https://github.com/TheElectronWill/Night-Config/wiki).
- You can also try the runnable [examples](examples/src/main/java) (see [below](#running-the-examples)).

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

## Running the examples

Each file in `examples/src/main/java` has a main function and shows how to use NightConfig for many different use cases.

To run an example:
1. Clone this repository.
2. `cd` to it
3. Run `./gradlew examples:run -PmainClass=${CLASS}` by replacing `${CLASS}` with the example of your choice.

For example, to run [FileConfigExample.java](examples/src/main/java/FileConfigExample.java):
```sh
./gradlew examples:run -PmainClass=FileConfigExample
```

The file be compiled automatically, and the given main class will be executed.

# Project building

NightConfig is built with Gradle. The project is divided in several modules, the "core" module plus one module per supported configuration format. Please [read the wiki for more information](https://github.com/TheElectronWill/Night-Config/wiki/Modules-and-dependencies).

The releases are available on [Maven Central](https://search.maven.org/search?q=com.electronwill.night-config) and [JitPack](https://jitpack.io/#TheElectronWill/Night-Config).

## Android

⚠️ The `_android` modules are deprecated and will be removed in a future version. ⚠️

*Why? Because the maintainance burden of these modules is not worth it, given that almost no smartphone uses these very old versions of Android.*

Older versions of Android don't provide the packages `java.util.function` and `java.nio.file`, which NightConfig heavily uses. If you encounter issues on android you can use the special version that I've made for you by adding `_android` to the modules' names.
