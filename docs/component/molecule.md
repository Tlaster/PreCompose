# Molecule

Molecule is a library from cashapp, which can write business logic in Compose, and it's also Kotlin Multiplatform project. For more information: https://github.com/cashapp/molecule

# Why Molecule with PreCompose
Since Molecule does not include any Lifecycle and Navigation state management, PreCompose can help you to integrate Molecule with Lifecycle and Navigation.

# Setup
## Add Dependency
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose-molecule/badge.svg)](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose-molecule)

Add the dependency **in your common module's commonMain sourceSet**
```
api("moe.tlaster:precompose-molecule:$precompose_version")
```
# Usage

You can write a Presenter like this:
```kotlin
@Composable
fun CounterPresenter(
    action: Flow<CounterAction>,
): CounterState {
    var count by remember { mutableStateOf(0) }

    action.collectAction {
        when (this) {
            CounterAction.Increment -> count++
            CounterAction.Decrement -> count--
        }
    }

    return CounterState("Clicked $count times")
}
```
in your Compose UI, you can use this `CounterPresenter` with `rememberPresenter`
```kotlin
val (state, channel) = rememberPresenter { CounterPresenter(it) }
```
`state` is the instance of `CounterState`, which return by `CounterPresenter`, and `channel` is the instance of `Channel<CounterEvent>`, you can send event to `CounterPresenter` by `channel.trySend(CounterEvent.Increment)`

The molecule scope and the Event Channel will be managed by the ViewModel, so it has the same lifecycle as the ViewModel.

You can nest your Presenter by using `rememberNestedPresenter`