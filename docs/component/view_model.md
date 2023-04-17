# ViewModel

Basically the same as Jetpack ViewModel
# Setup
## Add Dependency
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose-viewmodel/badge.svg)](https://maven-badges.herokuapp.com/maven-central/moe.tlaster/precompose-viewmodel)

Add the dependency **in your common module's commonMain sourceSet**
```
api("moe.tlaster:precompose-viewmodel:$precompose_version")
```

# Usage

You can define you ViewModel like this:
```kotlin
class HomeViewModel : ViewModel() {

}
```
With `viewModelScope` you can run suspend function like what you've done in Jetpack ViewModel.

To use ViewModel in compose, you can use the `viewModel()`  
```kotlin
val viewModel = viewModel(keys = listOf(someKey)) {
    SomeViewModel(someKey)
}
```
When the data that passing to the `keys` parameter changed, viewModel will be recreated, otherwise you will have the same viewModel instance. It's useful when your viewModel depends on some parameter that will receive from outside.

NOTE: If you're using Kotlin/Native target, please use viewModel with modelClass parameter instead.
```kotlin
val viewModel = viewModel(modelClass = SomeViewModel::class, keys = listOf(someKey)) {
    SomeViewModel(someKey)
}
```