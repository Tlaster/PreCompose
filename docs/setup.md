# Setup

## Add Dependency
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose/badge.svg)](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose)

Add the dependency **in your common module's commonMain sourceSet**
```
// Please do remember to add compose.foundation and compose.animation
api(compose.foundation)
api(compose.animation)
//...
api("moe.tlaster:precompose:$precompose_version")
```
## Android
Change the Activity's parent class to `moe.tlaster.precompose.lifecycle.PreComposeActivity` and use `moe.tlaster.precompose.lifecycle.setContent` for setting compose content

## Desktop (JVM)
Change the `Window` to `moe.tlaster.precompose.PreComposeWindow`

## iOS
Set the `UIWindow.rootViewController` to `PreComposeApplication`

## Native macOS
Change the `Window` to `moe.tlaster.precompose.PreComposeWindow`

## Web (Canvas)
Change the `Window` to `moe.tlaster.precompose.preComposeWindow`

## Done!
That's it! Enjoying the PreCompose! Now you can write all your business logic and ui code in `commonMain`
