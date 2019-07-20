# TriggerReactor [![Build Status](https://travis-ci.org/wysohn/TriggerReactor.svg?branch=master)](https://travis-ci.org/wysohn/TriggerReactor)

For easy to use script solution and infinite possibility to expand

## Project Hierarchy
TriggerReactor currently consist of five major projects: core, bukkit, bukkit-legacy, bukkit-latest, and sponge.

After each projects' build is complete, the gradle script will automatically include necessary java binaries from the different projects, and then create a complete .jar files which are ready to be used.

### Core
The core layer is where the actual script engine and the various interfaces resides. For example, the `io.github.wysohn.triggerreactor.core.script` package consist of number of classes necessary for interpreting the .trg codes, hence it's not related to the Bukkit or Sponge APIs. 

In order to utilize the script engine, the `io.github.wysohn.triggerreactor.core` package contains `bridge`, `main`, and `manager` sub-packages, and these are mostly interfaces that most of their actual functionaly is not yet implemented.

### Bukkit
This is the project where connects the core layer and the Bukkit API. It's this project's responsibility to provide the actual functionality using both the interfaces of Core project and Bukkit API.

Add to that, Because there was a big leap in Bukkit API itself, it's very difficult to expect the TriggerReactor built on Bukkit API version less than or equal to 1.12.2 to work with the latest versions like 1.13 or after, Bukkit project has two sub-projects. These sub-projects compile the necessary codes with different Bukkit API versions.This project works as library for the subprojects, so some missing implementations are in the subprojects.

#### Bukkit-legacy (1.5.2~1.12.2)
This is the traditional TriggerReactor project before the project is separated into multiple projects. Other than adding a new features, this project will remain untouched.

#### Bukkit-latest (1.13 ~ latest)
This is the new project which implements the latest Bukkit API. There were several changes that is very difficult to make the plugin both compatible in legacy version and the latest version, so this project will handle the part where those new changes should be adapted. (And it's also a new trend in Spigot community to separating legacy version support and latest version support)

### Sponge
This is a dedicated project only for Sponge. Implements functionality to connect Core project and Sponge API (ver7).

## Hierarchy Diagram

*The diagram may be a little bit different than the actual directory.*

```bash
.
├── build
│   ├── libs # This is where the actual plugin jar files will reside after build is complete
│   └── tmp
│       └── jar
├── bukkit
│   ├── build
│   │   ├── classes
│   │   │   └── main
│   │   │       └── io
│   │   │           └── github
│   │   │               └── wysohn
│   │   │                   └── triggerreactor
│   │   │                       └── bukkit
│   │   │                           ├── bridge
│   │   │                           │   └── entity
│   │   │                           ├── manager
│   │   │                           │   ├── event
│   │   │                           │   └── trigger
│   │   │                           │       └── share
│   │   │                           │           └── api
│   │   │                           └── tools
│   │   │                               └── prompts
│   │   ├── libs
│   │   ├── resources
│   │   │   └── main
│   │   │       ├── Executor
│   │   │       │   ├── ACTIONBAR.js
│   │   │       │   ├── BROADCAST.js
│   │   │       │   ├── BURN.js
│   │   │       │   ├── CLEARCHAT.js
│   │   │       │   ├── CLEARENTITY.js
│   │   │       │   ├── CLEARPOTION.js
│   │   │       │   ├── CLOSEGUI.js
│   │   │       │   ├── CMDCON.js
│   │   │       │   ├── CMD.js
│   │   │       │   ├── DOORCLOSE.js
│   │   │       │   ├── DOOROPEN.js
│   │   │       │   ├── DOORTOGGLE.js
│   │   │       │   ├── DROPITEM.js
│   │   │       │   ├── EXPLOSION.js
│   │   │       │   ├── FALLINGBLOCK.js
│   │   │       │   ├── GIVE.js
│   │   │       │   ├── GUI.js
│   │   │       │   ├── ITEMFRAMEROTATE.js
│   │   │       │   ├── ITEMFRAMESET.js
│   │   │       │   ├── KILL.js
│   │   │       │   ├── LEVEROFF.js
│   │   │       │   ├── LEVERON.js
│   │   │       │   ├── LEVERTOGGLE.js
│   │   │       │   ├── LIGHTNING.js
│   │   │       │   ├── LOG.js
│   │   │       │   ├── MESSAGE.js
│   │   │       │   ├── MODIFYHELDITEM.js
│   │   │       │   ├── MODIFYPLAYER.js
│   │   │       │   ├── MONEY.js
│   │   │       │   ├── MYSQL.js
│   │   │       │   ├── PERMISSION.js
│   │   │       │   ├── PLAYER
│   │   │       │   │   ├── SETFLYMODE.js
│   │   │       │   │   ├── SETFLYSPEED.js
│   │   │       │   │   ├── SETFOOD.js
│   │   │       │   │   ├── SETGAMEMODE.js
│   │   │       │   │   ├── SETHEALTH.js
│   │   │       │   │   ├── SETMAXHEALTH.js
│   │   │       │   │   ├── SETSATURATION.js
│   │   │       │   │   ├── SETWALKSPEED.js
│   │   │       │   │   └── SETXP.js
│   │   │       │   ├── POTION.js
│   │   │       │   ├── PUSH.js
│   │   │       │   ├── ROTATEBLOCK.js
│   │   │       │   ├── SCOREBOARD.js
│   │   │       │   ├── SERVER.js
│   │   │       │   ├── SETBLOCK.js
│   │   │       │   ├── SIGNEDIT.js
│   │   │       │   ├── SOUNDALL.js
│   │   │       │   ├── SOUND.js
│   │   │       │   ├── SPAWN.js
│   │   │       │   ├── TIME.js
│   │   │       │   ├── TP.js
│   │   │       │   ├── TPPOS.js
│   │   │       │   ├── VELOCITY.js
│   │   │       │   └── WEATHER.js
│   │   │       └── Placeholder
│   │   │           ├── air.js
│   │   │           ├── biome.js
│   │   │           ├── blockname.js
│   │   │           ├── currenttimeseconds.js
│   │   │           ├── emptyslot.js
│   │   │           ├── emptyslots.js
│   │   │           ├── exp.js
│   │   │           ├── explevel.js
│   │   │           ├── firstgroup.js
│   │   │           ├── food.js
│   │   │           ├── gamemode.js
│   │   │           ├── group.js
│   │   │           ├── haseffect.js
│   │   │           ├── haspermission.js
│   │   │           ├── health.js
│   │   │           ├── helditemdisplayname.js
│   │   │           ├── helditemhasenchant.js
│   │   │           ├── helditemid.js
│   │   │           ├── helditemlore.js
│   │   │           ├── helditemname.js
│   │   │           ├── ip.js
│   │   │           ├── isburning.js
│   │   │           ├── isflying.js
│   │   │           ├── isop.js
│   │   │           ├── issneaking.js
│   │   │           ├── issprinting.js
│   │   │           ├── maxhealth.js
│   │   │           ├── money.js
│   │   │           ├── mysql.js
│   │   │           ├── onlineplayers.js
│   │   │           ├── packlist.js
│   │   │           ├── pitch.js
│   │   │           ├── playerlocexact.js
│   │   │           ├── playerloc.js
│   │   │           ├── playername.js
│   │   │           ├── playeruuid.js
│   │   │           ├── prefix.js
│   │   │           ├── random.js
│   │   │           ├── round.js
│   │   │           ├── suffix.js
│   │   │           ├── time.js
│   │   │           ├── tps.js
│   │   │           ├── world.js
│   │   │           ├── worldname.js
│   │   │           ├── x.js
│   │   │           ├── yaw.js
│   │   │           ├── y.js
│   │   │           └── z.js
│   │   └── tmp
│   │       ├── compileJava
│   │       └── jar
│   ├── latest
│   │   ├── build
│   │   │   ├── classes
│   │   │   │   ├── main
│   │   │   │   │   └── io
│   │   │   │   │       └── github
│   │   │   │   │           └── wysohn
│   │   │   │   │               └── triggerreactor
│   │   │   │   │                   └── bukkit
│   │   │   │   │                       ├── bridge
│   │   │   │   │                       │   ├── entity
│   │   │   │   │                       │   └── event
│   │   │   │   │                       ├── main
│   │   │   │   │                       ├── manager
│   │   │   │   │                       │   ├── event
│   │   │   │   │                       │   └── trigger
│   │   │   │   │                       │       └── share
│   │   │   │   │                       │           └── api
│   │   │   │   │                       │               ├── coreprotect
│   │   │   │   │                       │               ├── mcmmo
│   │   │   │   │                       │               ├── placeholder
│   │   │   │   │                       │               ├── protocollib
│   │   │   │   │                       │               ├── vault
│   │   │   │   │                       │               └── worldguard
│   │   │   │   │                       └── tools
│   │   │   │   │                           └── prompts
│   │   │   │   └── test
│   │   │   │       └── io
│   │   │   │           └── github
│   │   │   │               └── wysohn
│   │   │   │                   └── triggerreactor
│   │   │   │                       └── bukkit
│   │   │   │                           └── script
│   │   │   │                               └── interpreter
│   │   │   ├── libs
│   │   │   ├── reports
│   │   │   │   └── tests
│   │   │   │       └── test
│   │   │   │           ├── classes
│   │   │   │           ├── css
│   │   │   │           ├── js
│   │   │   │           │   └── report.js
│   │   │   │           └── packages
│   │   │   ├── resources
│   │   │   │   └── main
│   │   │   │       └── plugin.yml
│   │   │   ├── test-results
│   │   │   │   └── test
│   │   │   │       └── binary
│   │   │   └── tmp
│   │   │       ├── compileJava
│   │   │       ├── compileTestJava
│   │   │       ├── jar
│   │   │       └── test
│   │   ├── libs
│   │   └── src
│   │       ├── main
│   │       │   ├── java
│   │       │   │   └── io
│   │       │   │       └── github
│   │       │   │           └── wysohn
│   │       │   │               └── triggerreactor
│   │       │   │                   └── bukkit
│   │       │   │                       ├── bridge
│   │       │   │                       │   ├── entity
│   │       │   │                       │   │   └── BukkitPlayer.java
│   │       │   │                       │   └── event
│   │       │   │                       │       └── BukkitPlayerBlockLocationEvent.java
│   │       │   │                       ├── main
│   │       │   │                       │   ├── JavaPluginBridge.java
│   │       │   │                       │   └── TriggerReactor.java
│   │       │   │                       ├── manager
│   │       │   │                       │   ├── AreaSelectionManager.java
│   │       │   │                       │   ├── BukkitScriptEngineInitializer.java
│   │       │   │                       │   ├── ExecutorManager.java
│   │       │   │                       │   ├── PermissionManager.java
│   │       │   │                       │   ├── PlaceholderManager.java
│   │       │   │                       │   ├── PlayerLocationManager.java
│   │       │   │                       │   └── trigger
│   │       │   │                       │       ├── AreaTriggerManager.java
│   │       │   │                       │       ├── BukkitConfigurationFileIO.java
│   │       │   │                       │       ├── BukkitTriggerManager.java
│   │       │   │                       │       ├── ClickTriggerManager.java
│   │       │   │                       │       ├── CommandTriggerManager.java
│   │       │   │                       │       ├── CustomTriggerManager.java
│   │       │   │                       │       ├── InventoryTriggerManager.java
│   │       │   │                       │       ├── LocationBasedTriggerManager.java
│   │       │   │                       │       ├── NamedTriggerManager.java
│   │       │   │                       │       ├── RepeatingTriggerManager.java
│   │       │   │                       │       ├── share
│   │       │   │                       │       │   ├── api
│   │       │   │                       │       │   │   ├── APISupport.java
│   │       │   │                       │       │   │   ├── coreprotect
│   │       │   │                       │       │   │   │   └── CoreprotectSupport.java
│   │       │   │                       │       │   │   ├── mcmmo
│   │       │   │                       │       │   │   │   └── McMmoSupport.java
│   │       │   │                       │       │   │   ├── placeholder
│   │       │   │                       │       │   │   │   └── PlaceHolderSupport.java
│   │       │   │                       │       │   │   ├── protocollib
│   │       │   │                       │       │   │   │   └── ProtocolLibSupport.java
│   │       │   │                       │       │   │   ├── vault
│   │       │   │                       │       │   │   │   └── VaultSupport.java
│   │       │   │                       │       │   │   └── worldguard
│   │       │   │                       │       │   │       └── WorldguardSupport.java
│   │       │   │                       │       │   └── CommonFunctions.java
│   │       │   │                       │       └── WalkTriggerManager.java
│   │       │   │                       └── tools
│   │       │   │                           └── BukkitUtil.java
│   │       │   └── resources
│   │       │       └── plugin.yml
│   │       └── test
│   │           └── java
│   │               └── io
│   │                   └── github
│   │                       └── wysohn
│   │                           └── triggerreactor
│   │                               └── bukkit
│   │                                   └── script
│   │                                       └── interpreter
│   │                                           └── TestInterpreter.java
│   ├── legacy
│   │   ├── build
│   │   │   ├── classes
│   │   │   │   ├── main
│   │   │   │   │   └── io
│   │   │   │   │       └── github
│   │   │   │   │           └── wysohn
│   │   │   │   │               └── triggerreactor
│   │   │   │   │                   └── bukkit
│   │   │   │   │                       ├── bridge
│   │   │   │   │                       │   ├── entity
│   │   │   │   │                       │   └── event
│   │   │   │   │                       ├── main
│   │   │   │   │                       ├── manager
│   │   │   │   │                       │   ├── event
│   │   │   │   │                       │   └── trigger
│   │   │   │   │                       │       └── share
│   │   │   │   │                       │           └── api
│   │   │   │   │                       │               ├── coreprotect
│   │   │   │   │                       │               ├── mcmmo
│   │   │   │   │                       │               ├── placeholder
│   │   │   │   │                       │               ├── protocollib
│   │   │   │   │                       │               ├── vault
│   │   │   │   │                       │               └── worldguard
│   │   │   │   │                       └── tools
│   │   │   │   │                           └── prompts
│   │   │   │   └── test
│   │   │   │       └── io
│   │   │   │           └── github
│   │   │   │               └── wysohn
│   │   │   │                   └── triggerreactor
│   │   │   │                       └── bukkit
│   │   │   │                           └── script
│   │   │   │                               └── interpreter
│   │   │   ├── libs
│   │   │   ├── reports
│   │   │   │   └── tests
│   │   │   │       └── test
│   │   │   │           ├── classes
│   │   │   │           ├── css
│   │   │   │           ├── js
│   │   │   │           │   └── report.js
│   │   │   │           └── packages
│   │   │   ├── resources
│   │   │   │   └── main
│   │   │   │       └── plugin.yml
│   │   │   ├── test-results
│   │   │   │   └── test
│   │   │   │       └── binary
│   │   │   └── tmp
│   │   │       ├── compileJava
│   │   │       ├── compileTestJava
│   │   │       ├── jar
│   │   │       └── test
│   │   ├── libs
│   │   └── src
│   │       ├── main
│   │       │   ├── java
│   │       │   │   └── io
│   │       │   │       └── github
│   │       │   │           └── wysohn
│   │       │   │               └── triggerreactor
│   │       │   │                   └── bukkit
│   │       │   │                       ├── bridge
│   │       │   │                       │   ├── entity
│   │       │   │                       │   │   └── BukkitPlayer.java
│   │       │   │                       │   └── event
│   │       │   │                       │       └── BukkitPlayerBlockLocationEvent.java
│   │       │   │                       ├── main
│   │       │   │                       │   ├── JavaPluginBridge.java
│   │       │   │                       │   └── TriggerReactor.java
│   │       │   │                       ├── manager
│   │       │   │                       │   ├── AreaSelectionManager.java
│   │       │   │                       │   ├── BukkitScriptEngineInitializer.java
│   │       │   │                       │   ├── ExecutorManager.java
│   │       │   │                       │   ├── PermissionManager.java
│   │       │   │                       │   ├── PlaceholderManager.java
│   │       │   │                       │   ├── PlayerLocationManager.java
│   │       │   │                       │   └── trigger
│   │       │   │                       │       ├── AreaTriggerManager.java
│   │       │   │                       │       ├── BukkitConfigurationFileIO.java
│   │       │   │                       │       ├── BukkitTriggerManager.java
│   │       │   │                       │       ├── ClickTriggerManager.java
│   │       │   │                       │       ├── CommandTriggerManager.java
│   │       │   │                       │       ├── CustomTriggerManager.java
│   │       │   │                       │       ├── InventoryTriggerManager.java
│   │       │   │                       │       ├── LocationBasedTriggerManager.java
│   │       │   │                       │       ├── NamedTriggerManager.java
│   │       │   │                       │       ├── RepeatingTriggerManager.java
│   │       │   │                       │       ├── share
│   │       │   │                       │       │   ├── api
│   │       │   │                       │       │   │   ├── APISupport.java
│   │       │   │                       │       │   │   ├── coreprotect
│   │       │   │                       │       │   │   │   └── CoreprotectSupport.java
│   │       │   │                       │       │   │   ├── mcmmo
│   │       │   │                       │       │   │   │   └── McMmoSupport.java
│   │       │   │                       │       │   │   ├── placeholder
│   │       │   │                       │       │   │   │   └── PlaceHolderSupport.java
│   │       │   │                       │       │   │   ├── protocollib
│   │       │   │                       │       │   │   │   └── ProtocolLibSupport.java
│   │       │   │                       │       │   │   ├── vault
│   │       │   │                       │       │   │   │   └── VaultSupport.java
│   │       │   │                       │       │   │   └── worldguard
│   │       │   │                       │       │   │       └── WorldguardSupport.java
│   │       │   │                       │       │   └── CommonFunctions.java
│   │       │   │                       │       └── WalkTriggerManager.java
│   │       │   │                       └── tools
│   │       │   │                           └── BukkitUtil.java
│   │       │   └── resources
│   │       │       └── plugin.yml
│   │       └── test
│   │           └── java
│   │               └── io
│   │                   └── github
│   │                       └── wysohn
│   │                           └── triggerreactor
│   │                               └── bukkit
│   │                                   └── script
│   │                                       └── interpreter
│   │                                           └── TestInterpreter.java
│   └── src
│       └── main
│           ├── java
│           │   └── io
│           │       └── github
│           │           └── wysohn
│           │               └── triggerreactor
│           │                   └── bukkit
│           │                       ├── bridge
│           │                       │   ├── BukkitCommandSender.java
│           │                       │   ├── BukkitInventory.java
│           │                       │   ├── BukkitItemStack.java
│           │                       │   ├── BukkitLocation.java
│           │                       │   └── entity
│           │                       │       └── BukkitEntity.java
│           │                       ├── manager
│           │                       │   ├── event
│           │                       │   │   ├── EventBase.java
│           │                       │   │   ├── PlayerBlockLocationEvent.java
│           │                       │   │   ├── PlayerPermissionCheckEventAsync.java
│           │                       │   │   ├── PlayerPermissionCheckEvent.java
│           │                       │   │   ├── TriggerReactorStartEvent.java
│           │                       │   │   └── TriggerReactorStopEvent.java
│           │                       │   ├── ScriptEditManager.java
│           │                       │   ├── trigger
│           │                       │   │   └── share
│           │                       │   │       └── api
│           │                       │   │           └── APISupport.java
│           │                       │   └── VariableManager.java
│           │                       └── tools
│           │                           ├── CopyYamlConfiguration.java
│           │                           ├── LocationUtil.java
│           │                           ├── prompts
│           │                           │   ├── EditingPrompt.java
│           │                           │   ├── ErrorPrompt.java
│           │                           │   ├── ExitConfirmPrompt.java
│           │                           │   └── UsagePrompt.java
│           │                           ├── SkullUtil.java
│           │                           └── Utf8YamlConfiguration.java
│           └── resources
│               ├── Executor
│               │   ├── ACTIONBAR.js
│               │   ├── BROADCAST.js
│               │   ├── BURN.js
│               │   ├── CLEARCHAT.js
│               │   ├── CLEARENTITY.js
│               │   ├── CLEARPOTION.js
│               │   ├── CLOSEGUI.js
│               │   ├── CMDCON.js
│               │   ├── CMD.js
│               │   ├── DOORCLOSE.js
│               │   ├── DOOROPEN.js
│               │   ├── DOORTOGGLE.js
│               │   ├── DROPITEM.js
│               │   ├── EXPLOSION.js
│               │   ├── FALLINGBLOCK.js
│               │   ├── GIVE.js
│               │   ├── GUI.js
│               │   ├── ITEMFRAMEROTATE.js
│               │   ├── ITEMFRAMESET.js
│               │   ├── KILL.js
│               │   ├── LEVEROFF.js
│               │   ├── LEVERON.js
│               │   ├── LEVERTOGGLE.js
│               │   ├── LIGHTNING.js
│               │   ├── LOG.js
│               │   ├── MESSAGE.js
│               │   ├── MODIFYHELDITEM.js
│               │   ├── MODIFYPLAYER.js
│               │   ├── MONEY.js
│               │   ├── MYSQL.js
│               │   ├── PERMISSION.js
│               │   ├── PLAYER
│               │   │   ├── SETFLYMODE.js
│               │   │   ├── SETFLYSPEED.js
│               │   │   ├── SETFOOD.js
│               │   │   ├── SETGAMEMODE.js
│               │   │   ├── SETHEALTH.js
│               │   │   ├── SETMAXHEALTH.js
│               │   │   ├── SETSATURATION.js
│               │   │   ├── SETWALKSPEED.js
│               │   │   └── SETXP.js
│               │   ├── POTION.js
│               │   ├── PUSH.js
│               │   ├── ROTATEBLOCK.js
│               │   ├── SCOREBOARD.js
│               │   ├── SERVER.js
│               │   ├── SETBLOCK.js
│               │   ├── SIGNEDIT.js
│               │   ├── SOUNDALL.js
│               │   ├── SOUND.js
│               │   ├── SPAWN.js
│               │   ├── TIME.js
│               │   ├── TP.js
│               │   ├── TPPOS.js
│               │   ├── VELOCITY.js
│               │   └── WEATHER.js
│               └── Placeholder
│                   ├── air.js
│                   ├── biome.js
│                   ├── blockname.js
│                   ├── currenttimeseconds.js
│                   ├── emptyslot.js
│                   ├── emptyslots.js
│                   ├── exp.js
│                   ├── explevel.js
│                   ├── firstgroup.js
│                   ├── food.js
│                   ├── gamemode.js
│                   ├── group.js
│                   ├── haseffect.js
│                   ├── haspermission.js
│                   ├── health.js
│                   ├── helditemdisplayname.js
│                   ├── helditemhasenchant.js
│                   ├── helditemid.js
│                   ├── helditemlore.js
│                   ├── helditemname.js
│                   ├── ip.js
│                   ├── isburning.js
│                   ├── isflying.js
│                   ├── isop.js
│                   ├── issneaking.js
│                   ├── issprinting.js
│                   ├── maxhealth.js
│                   ├── money.js
│                   ├── mysql.js
│                   ├── onlineplayers.js
│                   ├── packlist.js
│                   ├── pitch.js
│                   ├── playerlocexact.js
│                   ├── playerloc.js
│                   ├── playername.js
│                   ├── playeruuid.js
│                   ├── prefix.js
│                   ├── random.js
│                   ├── round.js
│                   ├── suffix.js
│                   ├── time.js
│                   ├── tps.js
│                   ├── world.js
│                   ├── worldname.js
│                   ├── x.js
│                   ├── yaw.js
│                   ├── y.js
│                   └── z.js
├── core
│   ├── build
│   │   ├── classes
│   │   │   ├── main
│   │   │   │   ├── io
│   │   │   │   │   └── github
│   │   │   │   │       └── wysohn
│   │   │   │   │           └── triggerreactor
│   │   │   │   │               ├── core
│   │   │   │   │               │   ├── bridge
│   │   │   │   │               │   │   ├── entity
│   │   │   │   │               │   │   └── event
│   │   │   │   │               │   ├── main
│   │   │   │   │               │   ├── manager
│   │   │   │   │               │   │   ├── location
│   │   │   │   │               │   │   └── trigger
│   │   │   │   │               │   │       └── share
│   │   │   │   │               │   │           └── api
│   │   │   │   │               │   └── script
│   │   │   │   │               │       ├── interpreter
│   │   │   │   │               │       ├── lexer
│   │   │   │   │               │       ├── parser
│   │   │   │   │               │       └── wrapper
│   │   │   │   │               └── tools
│   │   │   │   │                   └── mysql
│   │   │   │   └── org
│   │   │   │       └── apache
│   │   │   │           └── commons
│   │   │   │               └── lang3
│   │   │   │                   └── mutable
│   │   │   └── test
│   │   │       └── io
│   │   │           └── github
│   │   │               └── wysohn
│   │   │                   └── triggerreactor
│   │   │                       └── core
│   │   │                           ├── manager
│   │   │                           │   └── trigger
│   │   │                           │       └── share
│   │   │                           └── script
│   │   │                               ├── interpreter
│   │   │                               ├── lexer
│   │   │                               └── parser
│   │   ├── libs
│   │   ├── reports
│   │   │   └── tests
│   │   │       └── test
│   │   │           ├── classes
│   │   │           ├── css
│   │   │           ├── js
│   │   │           │   └── report.js
│   │   │           └── packages
│   │   ├── test-results
│   │   │   └── test
│   │   │       └── binary
│   │   └── tmp
│   │       ├── compileJava
│   │       ├── compileTestJava
│   │       ├── jar
│   │       └── test
│   └── src
│       ├── main
│       │   └── java
│       │       ├── io
│       │       │   └── github
│       │       │       └── wysohn
│       │       │           └── triggerreactor
│       │       │               ├── core
│       │       │               │   ├── bridge
│       │       │               │   │   ├── entity
│       │       │               │   │   │   ├── IEntity.java
│       │       │               │   │   │   └── IPlayer.java
│       │       │               │   │   ├── event
│       │       │               │   │   │   ├── IEvent.java
│       │       │               │   │   │   ├── IPlayerBlockLocationEvent.java
│       │       │               │   │   │   ├── IPlayerEvent.java
│       │       │               │   │   │   └── IPlayerPermissionCheckEvent.java
│       │       │               │   │   ├── ICommandSender.java
│       │       │               │   │   ├── IInventory.java
│       │       │               │   │   ├── IItemStack.java
│       │       │               │   │   ├── ILocation.java
│       │       │               │   │   └── IMinecraftObject.java
│       │       │               │   ├── main
│       │       │               │   │   └── TriggerReactor.java
│       │       │               │   ├── manager
│       │       │               │   │   ├── AbstractAreaSelectionManager.java
│       │       │               │   │   ├── AbstractExecutorManager.java
│       │       │               │   │   ├── AbstractJavascriptBasedManager.java
│       │       │               │   │   ├── AbstractPermissionManager.java
│       │       │               │   │   ├── AbstractPlaceholderManager.java
│       │       │               │   │   ├── AbstractPlayerLocationManager.java
│       │       │               │   │   ├── AbstractScriptEditManager.java
│       │       │               │   │   ├── AbstractVariableManager.java
│       │       │               │   │   ├── IScriptEngineInitializer.java
│       │       │               │   │   ├── KeyValueManager.java
│       │       │               │   │   ├── location
│       │       │               │   │   │   ├── Area.java
│       │       │               │   │   │   ├── SimpleChunkLocation.java
│       │       │               │   │   │   └── SimpleLocation.java
│       │       │               │   │   ├── Manager.java
│       │       │               │   │   └── trigger
│       │       │               │   │       ├── AbstractAreaTriggerManager.java
│       │       │               │   │       ├── AbstractCommandTriggerManager.java
│       │       │               │   │       ├── AbstractCustomTriggerManager.java
│       │       │               │   │       ├── AbstractInventoryTriggerManager.java
│       │       │               │   │       ├── AbstractLocationBasedTriggerManager.java
│       │       │               │   │       ├── AbstractNamedTriggerManager.java
│       │       │               │   │       ├── AbstractRepeatingTriggerManager.java
│       │       │               │   │       ├── AbstractTriggerManager.java
│       │       │               │   │       ├── ConfigurationFileIO.java
│       │       │               │   │       └── share
│       │       │               │   │           ├── api
│       │       │               │   │           │   ├── AbstractAPISupport.java
│       │       │               │   │           │   └── APISupportException.java
│       │       │               │   │           └── CommonFunctions.java
│       │       │               │   └── script
│       │       │               │       ├── interpreter
│       │       │               │       │   ├── Executor.java
│       │       │               │       │   ├── InterpretCondition.java
│       │       │               │       │   ├── InterpreterException.java
│       │       │               │       │   ├── Interpreter.java
│       │       │               │       │   ├── Placeholder.java
│       │       │               │       │   ├── SynchronizableTask.java
│       │       │               │       │   ├── TaskSupervisor.java
│       │       │               │       │   └── TemporaryGlobalVariableKey.java
│       │       │               │       ├── lexer
│       │       │               │       │   ├── LexerException.java
│       │       │               │       │   └── Lexer.java
│       │       │               │       ├── parser
│       │       │               │       │   ├── Node.java
│       │       │               │       │   ├── ParserException.java
│       │       │               │       │   └── Parser.java
│       │       │               │       ├── Token.java
│       │       │               │       └── wrapper
│       │       │               │           ├── Accessor.java
│       │       │               │           ├── IScriptObject.java
│       │       │               │           └── SelfReference.java
│       │       │               └── tools
│       │       │                   ├── FileUtil.java
│       │       │                   ├── JarUtil.java
│       │       │                   ├── Lag.java
│       │       │                   ├── mysql
│       │       │                   │   └── MiniConnectionPoolManager.java
│       │       │                   ├── ReflectionUtil.java
│       │       │                   ├── ScriptEditor.java
│       │       │                   ├── TimeUtil.java
│       │       │                   └── ValidationUtil.java
│       │       └── org
│       │           └── apache
│       │               └── commons
│       │                   └── lang3
│       │                       ├── CharUtils.java
│       │                       ├── ClassUtils.java
│       │                       ├── mutable
│       │                       │   └── MutableObject.java
│       │                       └── StringUtils.java
│       └── test
│           └── java
│               └── io
│                   └── github
│                       └── wysohn
│                           └── triggerreactor
│                               └── core
│                                   ├── manager
│                                   │   └── trigger
│                                   │       └── share
│                                   │           └── TestCommonFunctions.java
│                                   └── script
│                                       ├── interpreter
│                                       │   └── TestInterpreter.java
│                                       ├── lexer
│                                       │   └── TestLexer.java
│                                       └── parser
│                                           └── TestParser.java
├── gradle
│   └── wrapper
├── libs
└── sponge
    ├── build
    │   ├── classes
    │   │   └── main
    │   │       └── io
    │   │           └── github
    │   │               └── wysohn
    │   │                   └── triggerreactor
    │   │                       └── sponge
    │   │                           ├── bridge
    │   │                           │   ├── entity
    │   │                           │   └── event
    │   │                           ├── main
    │   │                           ├── manager
    │   │                           │   ├── event
    │   │                           │   └── trigger
    │   │                           │       └── share
    │   │                           │           └── api
    │   │                           │               └── nucleus
    │   │                           └── tools
    │   ├── libs
    │   ├── resources
    │   │   └── main
    │   │       ├── assets
    │   │       │   └── triggerreactor
    │   │       │       └── config.yml
    │   │       ├── Executor
    │   │       │   ├── ACTIONBAR.js
    │   │       │   ├── BROADCAST.js
    │   │       │   ├── BURN.js
    │   │       │   ├── CLEARCHAT.js
    │   │       │   ├── CLEARENTITY.js
    │   │       │   ├── CLEARPOTION.js
    │   │       │   ├── CLOSEGUI.js
    │   │       │   ├── CMDCON.js
    │   │       │   ├── CMD.js
    │   │       │   ├── DOORCLOSE.js
    │   │       │   ├── DOOROPEN.js
    │   │       │   ├── DOORTOGGLE.js
    │   │       │   ├── DROPITEM.js
    │   │       │   ├── EXPLOSION.js
    │   │       │   ├── FALLINGBLOCK.js
    │   │       │   ├── GIVE.js
    │   │       │   ├── GUI.js
    │   │       │   ├── ITEMFRAMEROTATE.js
    │   │       │   ├── ITEMFRAMESET.js
    │   │       │   ├── KILL.js
    │   │       │   ├── LEVEROFF.js
    │   │       │   ├── LEVERON.js
    │   │       │   ├── LEVERTOGGLE.js
    │   │       │   ├── LIGHTNING.js
    │   │       │   ├── LOG.js
    │   │       │   ├── MESSAGE.js
    │   │       │   ├── MODIFYHELDITEM.js
    │   │       │   ├── MODIFYPLAYER.js
    │   │       │   ├── MONEY.js
    │   │       │   ├── POTION.js
    │   │       │   ├── PUSH.js
    │   │       │   ├── ROTATEBLOCK.js
    │   │       │   ├── SCOREBOARD.js
    │   │       │   ├── SETBLOCK.js
    │   │       │   ├── SIGNEDIT.js
    │   │       │   ├── SOUNDALL.js
    │   │       │   ├── SOUND.js
    │   │       │   ├── SPAWN.js
    │   │       │   ├── TIME.js
    │   │       │   ├── TP.js
    │   │       │   ├── TPPOS.js
    │   │       │   ├── VELOCITY.js
    │   │       │   └── WEATHER.js
    │   │       └── Placeholder
    │   │           ├── air.js
    │   │           ├── biome.js
    │   │           ├── currenttimeseconds.js
    │   │           ├── emptyslot.js
    │   │           ├── emptyslots.js
    │   │           ├── exp.js
    │   │           ├── explevel.js
    │   │           ├── firstgroup.js
    │   │           ├── food.js
    │   │           ├── gamemode.js
    │   │           ├── group.js
    │   │           ├── haseffect.js
    │   │           ├── haspermission.js
    │   │           ├── health.js
    │   │           ├── helditemdisplayname.js
    │   │           ├── helditemhasenchant.js
    │   │           ├── helditemid.js
    │   │           ├── helditemlore.js
    │   │           ├── helditemname.js
    │   │           ├── ip.js
    │   │           ├── isburning.js
    │   │           ├── isflying.js
    │   │           ├── isop.js
    │   │           ├── issneaking.js
    │   │           ├── issprinting.js
    │   │           ├── maxhealth.js
    │   │           ├── money.js
    │   │           ├── onlineplayers.js
    │   │           ├── packlist.js
    │   │           ├── pitch.js
    │   │           ├── playerlocexact.js
    │   │           ├── playerloc.js
    │   │           ├── playername.js
    │   │           ├── playeruuid.js
    │   │           ├── random.js
    │   │           ├── time.js
    │   │           ├── tps.js
    │   │           ├── world.js
    │   │           ├── worldname.js
    │   │           ├── x.js
    │   │           ├── yaw.js
    │   │           ├── y.js
    │   │           └── z.js
    │   └── tmp
    │       ├── compileJava
    │       ├── generateMetadata
    │       └── jar
    ├── libs
    └── src
        └── main
            ├── java
            │   └── io
            │       └── github
            │           └── wysohn
            │               └── triggerreactor
            │                   └── sponge
            │                       ├── bridge
            │                       │   ├── entity
            │                       │   │   ├── SpongeEntity.java
            │                       │   │   └── SpongePlayer.java
            │                       │   ├── event
            │                       │   │   └── SpongePlayerBlockLocationEvent.java
            │                       │   ├── SpongeCommandSender.java
            │                       │   ├── SpongeInventory.java
            │                       │   ├── SpongeItemStack.java
            │                       │   └── SpongeLocation.java
            │                       ├── main
            │                       │   └── TriggerReactor.java
            │                       ├── manager
            │                       │   ├── AreaSelectionManager.java
            │                       │   ├── event
            │                       │   │   ├── PlayerBlockLocationEvent.java
            │                       │   │   ├── TriggerReactorStartEvent.java
            │                       │   │   └── TriggerReactorStopEvent.java
            │                       │   ├── ExecutorManager.java
            │                       │   ├── PlaceholderManager.java
            │                       │   ├── PlayerLocationManager.java
            │                       │   ├── ScriptEditManager.java
            │                       │   ├── SpongeScriptEngineInitializer.java
            │                       │   ├── trigger
            │                       │   │   ├── AreaTriggerManager.java
            │                       │   │   ├── ClickTriggerManager.java
            │                       │   │   ├── CommandTriggerManager.java
            │                       │   │   ├── CustomTriggerManager.java
            │                       │   │   ├── InventoryTriggerManager.java
            │                       │   │   ├── LocationBasedTriggerManager.java
            │                       │   │   ├── NamedTriggerManager.java
            │                       │   │   ├── RepeatingTriggerManager.java
            │                       │   │   ├── share
            │                       │   │   │   ├── api
            │                       │   │   │   │   ├── APISupport.java
            │                       │   │   │   │   └── nucleus
            │                       │   │   │   │       └── NucleusSupport.java
            │                       │   │   │   └── CommonFunctions.java
            │                       │   │   ├── SpongeConfigurationFileIO.java
            │                       │   │   └── WalkTriggerManager.java
            │                       │   └── VariableManager.java
            │                       └── tools
            │                           ├── ConfigurationUtil.java
            │                           ├── DelegatedPlayer.java
            │                           ├── LocationUtil.java
            │                           ├── TemporarilyPrivilegedPlayer.java
            │                           └── TextUtil.java
            └── resources
                ├── assets
                │   └── triggerreactor
                │       └── config.yml
                ├── Executor
                │   ├── ACTIONBAR.js
                │   ├── BROADCAST.js
                │   ├── BURN.js
                │   ├── CLEARCHAT.js
                │   ├── CLEARENTITY.js
                │   ├── CLEARPOTION.js
                │   ├── CLOSEGUI.js
                │   ├── CMDCON.js
                │   ├── CMD.js
                │   ├── DOORCLOSE.js
                │   ├── DOOROPEN.js
                │   ├── DOORTOGGLE.js
                │   ├── DROPITEM.js
                │   ├── EXPLOSION.js
                │   ├── FALLINGBLOCK.js
                │   ├── GIVE.js
                │   ├── GUI.js
                │   ├── ITEMFRAMEROTATE.js
                │   ├── ITEMFRAMESET.js
                │   ├── KILL.js
                │   ├── LEVEROFF.js
                │   ├── LEVERON.js
                │   ├── LEVERTOGGLE.js
                │   ├── LIGHTNING.js
                │   ├── LOG.js
                │   ├── MESSAGE.js
                │   ├── MODIFYHELDITEM.js
                │   ├── MODIFYPLAYER.js
                │   ├── MONEY.js
                │   ├── POTION.js
                │   ├── PUSH.js
                │   ├── ROTATEBLOCK.js
                │   ├── SCOREBOARD.js
                │   ├── SETBLOCK.js
                │   ├── SIGNEDIT.js
                │   ├── SOUNDALL.js
                │   ├── SOUND.js
                │   ├── SPAWN.js
                │   ├── TIME.js
                │   ├── TP.js
                │   ├── TPPOS.js
                │   ├── VELOCITY.js
                │   └── WEATHER.js
                └── Placeholder
                    ├── air.js
                    ├── biome.js
                    ├── currenttimeseconds.js
                    ├── emptyslot.js
                    ├── emptyslots.js
                    ├── exp.js
                    ├── explevel.js
                    ├── firstgroup.js
                    ├── food.js
                    ├── gamemode.js
                    ├── group.js
                    ├── haseffect.js
                    ├── haspermission.js
                    ├── health.js
                    ├── helditemdisplayname.js
                    ├── helditemhasenchant.js
                    ├── helditemid.js
                    ├── helditemlore.js
                    ├── helditemname.js
                    ├── ip.js
                    ├── isburning.js
                    ├── isflying.js
                    ├── isop.js
                    ├── issneaking.js
                    ├── issprinting.js
                    ├── maxhealth.js
                    ├── money.js
                    ├── onlineplayers.js
                    ├── packlist.js
                    ├── pitch.js
                    ├── playerlocexact.js
                    ├── playerloc.js
                    ├── playername.js
                    ├── playeruuid.js
                    ├── random.js
                    ├── time.js
                    ├── tps.js
                    ├── world.js
                    ├── worldname.js
                    ├── x.js
                    ├── yaw.js
                    ├── y.js
                    └── z.js
```
