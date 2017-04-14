![Night Config](logo.png)  
[![](https://jitpack.io/v/TheElectronWill/Night-Config.svg)](https://jitpack.io/#TheElectronWill/Night-Config)
![](https://img.shields.io/github/commits-since/TheElectronWill/Night-Config/v1.0.3.svg)

# Introduction
NightConfig is a simple yet powerful java configuration library, written in Java 8.

It supports the following formats:
- [JSON](http://www.json.org/)
- [YAML v1.1](http://yaml.org/)
- [TOML](https://github.com/toml-lang/toml)
- [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)

# Project management
NightConfig is managed with gradle. It is divided in several modules, the "core" module plus one module per supported format of configuration.

The project isn't available in a big repository (like Maven central) yet. You can use [jitpack](https://jitpack.io/#TheElectronWill/Night-Config) to build it from github more easily.

The repository has two branches:
- [stable-1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-1.x) contains the stable version v1. Only bugfixes will occur on this branch, the API won't change.
- [master](https://github.com/TheElectronWill/Night-Config/tree/master) contains the latest in-development branch. Anything can change without notice!

# Code examples
Read the [wiki](https://github.com/TheElectronWill/Night-Config/wiki) to learn how to use NightConfig.

**Note:** these are examples for the last **stable** version, and may not work with the master branch.

## Loading a TOML configuration from a file
```java
TomlConfig config = new TomlConfig();
config.readFrom(file);
```

## Getting/Setting some values
```java
String str = config.getValue("a.b.c");// Dot-separated string as path
String str2 = config.getValue(Arrays.asList("127.0.0.1"));// List of strings as path, in case you need dots in the path 
List<Integer> list = config.getValue("list");// No need to cast the returned value!
Optional<Boolean> bool = config.getOptionalValue("enabled");// Supports Optional gets

config.setValue("a.b.c", str);
config.setValue(Array.asList("127.0.0.1"), str2);
config.setValue("list", list);

config.removeValue("a.b.c");
config.removeValue(Array.asList("127.0.0.1"));
```

## Saving a TOML configuration to a file
```java
config.writeTo(file);// Yes, that's it!
// And it works the same with JSON, HOCON and YAML configs.
```

## Automatically correcting a configuration, based on a specification
[Learn more about this innovative feature on the wiki!](https://github.com/TheElectronWill/Night-Config/wiki/1.x-Config-specification)
```java
ConfigSpec spec = new ConfigSpec();
spec.define("a.b.c", defaultValue);

// Defines that a value must be in a list:
List<Character> acceptableValues = Arrays.asList('a', 'b', 'c');
spec.defineInList("myChar", defaultChar, acceptableValues);

// Defines that a value must be between two values:
spec.defineInRange("myInt", defaultInt, 0, 50);
// Works with floating-point numbers too!
spec.defineInRange("myDouble", defaultDouble 0.0, 50.0);
// Actually, it works with any comparable values
spec.defineInRange("myString", defaultString, "aaa", "bbb");

// There are many more methods to define values! Read the javadoc.

/* Once your specification is well defined, you can automatically correct your configuration!
This will ensure that all the values respect the specification,
by replacing invalid values with the corresponding default value */
spec.correct(config);
```
