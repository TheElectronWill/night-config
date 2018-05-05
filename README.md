![Night Config](logo.png)

[![](https://jitpack.io/v/TheElectronWill/Night-Config.svg)](https://jitpack.io/#TheElectronWill/Night-Config)
[![](https://img.shields.io/badge/javadoc-core-blue.svg)](https://jitpack.io/com/github/TheElectronWill/Night-Config/core/3.1.0/javadoc/index.html)
![](https://img.shields.io/github/commits-since/TheElectronWill/Night-Config/v3.0.svg)

# Introduction
NightConfig is a powerful yet easy-to-use java configuration library, written in Java 8.

It supports the following formats:
- [JSON](http://www.json.org/)
- [YAML v1.1](http://yaml.org/)
- [TOML](https://github.com/toml-lang/toml)
- [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)

# Code examples
- Code examples are available [here.](https://github.com/TheElectronWill/Night-Config/tree/master/examples/src/main/java)
*Note: these examples are for the last **stable** version, and may not work with the master branch.*
- You can also read the [wiki](https://github.com/TheElectronWill/Night-Config/wiki).

## Sample
```java
FileConfig config = FileConfig.builder("myConfig.toml").defaultResource("defaultConfig.toml").autosave().build();
config.load();

String name = config.get("username");
int id = config.get("account.id");
int points = config.getOrElse("account.score", defaultScore);

config.set("account.score", points*2);

String comment = config.getComment("user");
config.close();
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
