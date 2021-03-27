# LiveData

Basically the same as Jetpack LiveData, 

You can define LiveData in you ViewModel like this:
```kotlin
class HomeViewModel : ViewModel() {
    val text = LiveData("")
}
```
and using it in compose:
```kotlin
val text by viewModel.text.observeAsState()
Text(text = text)
```