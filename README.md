# PreCompose
[![](https://jitpack.io/v/Tlaster/PreCompose.svg)](https://jitpack.io/#Tlaster/PreCompose)
[![](https://img.shields.io/badge/compose--jb-0.4.0--build177-blue)](https://github.com/JetBrains/compose-jb)

Let you write your Kotlin application in pure compose, which is **Pre**tty **Compose**.

PreCompose inspired by Jetpack Lifecycle, ViewModel, LiveData and Navigation. If you're familiar with these components in Android, PreCompose provides similar (or even the same) components for you but it's written in pure Kotlin and it's Kotlin Multiplatform project. Let you enjoy the power of Kotlin and compose, `write once, run everywhere`(like what Flutter does)

# Why PreCompose?
Since compose actually renders itself using skia like what Flutter does, it can be platform-independent with the power of Kotlin Multiplatform. So why not do the things that Flutter does? And get better! Just write your business logic and ui code once in one `commonMain`, and your application can be anywhere, powered by Kotlin and compose!  

There's nothing complex to setup, just write your ViewModel and UI code and you're good to go! Complex things like lifecycle will be handled by PreCompose.

# Setup
[Setup guide for PreCompose](/docs/setup.md)

# Components
 - [Navigation](/docs/component/navigation.md)
 - [LiveData](/docs/component/live_data.md)
 - [ViewModel](/docs/component/view_model.md)

# [Sample](/docs/sample.md)
![](https://raw.githubusercontent.com/Tlaster/PreCompose/master/media/note_app.webp)
![](https://raw.githubusercontent.com/Tlaster/PreCompose/master/media/greeting_app.gif)

