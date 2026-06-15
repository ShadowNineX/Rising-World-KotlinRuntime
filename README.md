# Rising World Kotlin Runtime

## About

This is just a dependency for Kotlin based mods. It doesn't do anything by it's own other than make Kotlin available for Kotlin compiled mods.

## Why?

It's better to just use 1 file instead of bundling in the Kotlin runtime multiple times which both saves filesize of mods and issues with conflicts.

## How to install?

Just grab the current release from the [Releases](https://github.com/ShadowNineX/Rising-World-KotlinRuntime/releases) page.

Just unzip the Kotlin-Runtime into the Plugins folder of game or server.

## Devs

### How to compile?

You need to have gradle and java installed and then create `gradle.properties` file with contents: 
`PLUGINAPIJAR=C:/Path/To/PluginAPI.jar` replace it with your actual location of the jar file on your system. Usually in either: `C:\Program Files (x86)\Steam\steamapps\common\RisingWorld\Data\Java\PluginAPI.jar` or `C:\Program Files (x86)\Steam\steamapps\common\RisingWorldDedicatedServer\Data\Java\PluginAPI.jar`
