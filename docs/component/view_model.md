# ViewModel

Basically the same as Jetpack ViewModel

You can define you ViewModel like this:
```kotlin
class HomeViewModel : ViewModel() {

}
```
With `viewModelScope` you can run suspend function like what you've done in Jetpack ViewModel.

To use ViewModel in compose, you can use the `viewModel()`
```kotlin
val viewModel = viewModel(keys: listOf(someKey)) {
    SomeViewModel(someKey)
}
```
When the data that passing to the `keys` parameter changed, viewModel will be recreate, otherwise you will have the same viewModel instance. It's usefull when your viewModel depends on some parameter that will receive from outside.