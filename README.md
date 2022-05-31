# TemplatePlugin

[![Xpdustry latest](https://repo.xpdustry.fr/api/badge/latest/releases/fr/xpdustry/router?color=00FFFF&name=Router&prefix=v)](https://github.com/Xpdustry/Router/releases)
[![Build status](https://github.com/Xpdustry/Router/actions/workflows/build.yml/badge.svg?branch=master&event=push)](https://github.com/Xpdustry/Router/actions/workflows/build.yml)
[![Mindustry 6.0 | 7.0 ](https://img.shields.io/badge/Mindustry-6.0%20%7C%207.0-ffd37f)](https://github.com/Anuken/Mindustry/releases)

## Description

A plugin for building and sharing schematics in a dedicated server.

## Building

- `./gradlew jar` for a simple jar that contains only the plugin code.

- `./gradlew shadowJar` for a fatJar that contains the plugin and its dependencies (use this for
  your server).

## Testing

- `./gradlew runMindustryClient`: Run Mindustry in desktop with the plugin.

- `./gradlew runMindustryServer`: Run Mindustry in a server with the plugin.

## Running

This plugin runs on Java 17 and is compatible with Mindustry V6 and V7.

## TODO

- [X] Base plot management

- [ ] Schematics management
