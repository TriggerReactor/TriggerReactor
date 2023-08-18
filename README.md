# TriggerReactor ![Java CI](https://github.com/TriggerReactor/TriggerReactor/workflows/Java%20CI/badge.svg) [![CodeFactor](https://www.codefactor.io/repository/github/triggerreactor/triggerreactor/badge)](https://www.codefactor.io/repository/github/triggerreactor/triggerreactor)

For easy to use script solution and infinite possibility to expand

## Project Hierarchy

TriggerReactor currently consist of five major projects: core, bukkit, bukkit-legacy, bukkit-latest, and sponge.

After each projects' build is complete, the gradle script will automatically include necessary java binaries from the
different projects, and then create a complete .jar files which are ready to be used.

### Core

The core layer is where the actual script engine and the various interfaces resides. For example,
the `io.github.wysohn.triggerreactor.core.script` package consist of number of classes necessary for interpreting the
.trg codes, hence it's not related to the Bukkit or Sponge APIs.

In order to utilize the script engine, the `io.github.wysohn.triggerreactor.core` package contains `bridge`, `main`,
and `manager` sub-packages, and these are mostly interfaces that most of their actual functionaly is not yet
implemented.

### Bukkit

This is the project where connects the core layer and the Bukkit API. It's this project's responsibility to provide the
actual functionality using both the interfaces of Core project and Bukkit API.

Add to that, Because there was a big leap in Bukkit API itself, it's very difficult to expect the TriggerReactor built
on Bukkit API version less than or equal to 1.12.2 to work with the latest versions like 1.13 or after, Bukkit project
has two sub-projects. These sub-projects compile the necessary codes with different Bukkit API versions.This project
works as library for the subprojects, so some missing implementations are in the subprojects.

#### Bukkit-legacy (1.5.2~1.12.2)

This is the traditional TriggerReactor project before the project is separated into multiple projects. Other than adding
a new features, this project will remain untouched.

#### Bukkit-latest (1.13 ~ latest)

This is the new project which implements the latest Bukkit API. There were several changes that is very difficult to
make the plugin both compatible in legacy version and the latest version, so this project will handle the part where
those new changes should be adapted. (And it's also a new trend in Spigot community to separating legacy version support
and latest version support)

### Sponge

This is a dedicated project only for Sponge. Implements functionality to connect Core project and Sponge API (ver7).
