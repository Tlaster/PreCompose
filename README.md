# PreCompose
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose/badge.svg)](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose)
[![compose-jb-version](https://img.shields.io/badge/compose--jb-1.5.10-blue)](https://github.com/JetBrains/compose-jb)
![license](https://img.shields.io/github/license/Tlaster/PreCompose)

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)
![badge-web](https://img.shields.io/badge/Platform-Web-blue)

Compose Multiplatform Navigation && ViewModel, inspired by Jetpack Navigation, ViewModel and Lifecycle, PreCompose provides similar (or even the same) components for you but in Kotlin, and it's Kotlin Multiplatform project.

# Setup
Add the dependency **in your common module's commonMain sourceSet**
```
api("moe.tlaster:precompose:$precompose_version")
// api("moe.tlaster:precompose-viewmodel:$precompose_version") // For ViewModel intergration
// api("moe.tlaster:precompose-molecule:$precompose_version") // For Molecule intergration 
// api("moe.tlaster:precompose-koin:$precompose_version") // For Koin intergration
```

## Wrap the `App()`

Wrap your App with `PreComposApp` like this:
```Kotlin
@Composable
fun App() {
    PreComposeApp {
        // your app's content goes here
    }
}
```
That's it! Enjoying the PreCompose! Now you can write all your business logic and ui code in `commonMain`

# Components
 - [Navigation](/docs/component/navigation.md)
 - [ViewModel](/docs/component/view_model.md)
 - [Molecule Integration](/docs/component/molecule.md)
- [Koin Integration](/docs/component/koin.md)

# Sample
 - [Note App](/docs/sample.md#note-app)
 - [Greetings App with ViewModel in 100 lines!](/docs/sample.md#greetings-app-with-viewmodel-in-100-lines)

# Credits

Thanks JetBrains for [supporting open source software](https://www.jetbrains.com/community/opensource/#support)

<a href="https://www.jetbrains.com/community/opensource/#support">
  <img src="https://resources.jetbrains.com/storage/products/company/brand/logos/jb_beam.png" alt="JetBrains Logo (Main) logo." width="200" />
</a>

# LICENSE
```
MIT License

Copyright (c) 2021 Tlaster

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
