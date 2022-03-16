# TemplatePlugin

[![Build](https://github.com/Xpdustry/Router/actions/workflows/build.yml/badge.svg?branch=master)](https://github.com/Xpdustry/Router/actions/workflows/build.yml)
[![Mindustry 6.0 | 7.0 ](https://img.shields.io/badge/Mindustry-6.0%20%7C%207.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)
[![Xpdustry latest](https://repo.xpdustry.fr/api/badge/latest/releases/fr/xpdustry/router?color=00FFFF&name=Router&prefix=v)](https://github.com/Xpdustry/Router/releases)

## Description

A
## Building

- `./gradlew jar` for a simple jar that contains only the plugin code.

- `./gradlew shadowJar` for a fatJar that contains the plugin and its dependencies (use this for your server).

## Testing

- `./gradlew runMindustryClient`: Run Mindustry in desktop with the plugin.

- `./gradlew runMindustryServer`: Run Mindustry in a server with the plugin.

## Running

This plugin is compatible with V6 and V7.

Need [Distributor](https://github.com/Xpdustry/Distributor), both `distributor-core` and `distributor-js`.

**/!\ Up to v135, you will need [mod-loader](https://github.com/Xpdustry/ModLoaderPlugin) for dependency resolution.**
