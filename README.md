![Night Config](logo.png)  
[![](https://jitpack.io/v/TheElectronWill/Night-Config.svg)](https://jitpack.io/#TheElectronWill/Night-Config)
[![](https://img.shields.io/badge/javadoc-core-blue.svg)](https://jitpack.io/com/github/TheElectronWill/Night-Config/core/v2.1.2/javadoc/index.html)
![](https://img.shields.io/github/commits-since/TheElectronWill/Night-Config/v2.0.svg)

# Introduction
NightConfig is a simple yet powerful java configuration library, written in Java 8.

It supports the following formats:
- [JSON](http://www.json.org/)
- [YAML v1.1](http://yaml.org/)
- [TOML](https://github.com/toml-lang/toml)
- [HOCON](https://github.com/typesafehub/config/blob/master/HOCON.md)

# Project management
NightConfig is managed with gradle. It is divided in several modules, the "core" module plus one module per supported format of configuration.

The project is available on [jitpack](https://jitpack.io/#TheElectronWill/Night-Config).

The repository has several branches:
- [stable-1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-1.x) contains the 
stable version 1. Only bugfixes will occur on this branch, the API won't break.
- [stable-2.x](https://github.com/TheElectronWill/Night-Config/tree/stable-2.x) contains the 
stable version 2.0. Only bugfixes will occur on this branch, the API won't break.
- [stable-2.1.x](https://github.com/TheElectronWill/Night-Config/tree/stable-2.1.x) contains the 
latest stable version 2.1. Only bugfixes will occur on this branch, the API won't break.
- [master](https://github.com/TheElectronWill/Night-Config/tree/master) contains the latest 
in-development branch. Anything can change without notice!

# Code examples
Read the [wiki](https://github.com/TheElectronWill/Night-Config/wiki) to learn how to use NightConfig.

**Note:** these are examples for the last **stable** version, and may not work with the master branch.

## Loading a TOML configuration from a file
```java
TomlConfig config = new TomlParser().parse(file);
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

config.setComment("a.b.c", "Night-Config v2 supports comments!");
```

## Saving a TOML configuration to a file
```java
config.write(file);// Yes, that's all!
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
spec.defineInRange("myDouble", defaultDouble, 0.0, 50.0);
// Actually, it works with any comparable values
spec.defineInRange("myString", defaultString, "aaa", "bbb");

// There are many more methods to define values! Read the javadoc.

/* Once your specification is well defined, you can automatically correct your configuration!
This will ensure that all the values respect the specification,
by replacing invalid values with the corresponding default value */
spec.correct(config);
```

## Converting configurations to plain objects
You can easily convert a config to a plain java object (and vice-versa).
For instance, if you have a class like this:
```java
class ConfigObject {
    String name = "The_Name";
    int id = 123_001;
    Coordinates coords = new Coordinates(1, 2, 3);
}
```
With a class Coordinates:
```java
class Coordinates {
    int x, y, z;
}
````
You can get an instance of ConfigObject from a config with minimum effort:
```java
ConfigObject object = new ObjectConverter().toObject(config, ConfigObject::new);
```
And also get a configuration from a ConfigObject:
```java
Config config = new ObjectConverter().toConfig(object, TomlConfig::new);
