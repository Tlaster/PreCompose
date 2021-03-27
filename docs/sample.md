# Sample

## Note App
[Source Code](https://github.com/Tlaster/PreCompose/tree/master/sample)

run `./gradlew :sample:desktop:run` to run the desktop app or `./gradlew :sample:android:installDebug` to install the Android app.

This sample demonstrates the following features:
 - Navigation
 - Custom Navigation transition
 - ViewModel
 - LiveData

![](https://raw.githubusercontent.com/Tlaster/PreCompose/master/media/note_app.webp)

## Greetings App with ViewModel in 100 lines!

```kotlin
@Composable
fun App() {
    val navigator = rememberNavigator()
    MaterialTheme {
        NavHost(
            navigator = navigator,
            initialRoute = "/home"
        ) {
            scene(route = "/home") {
                val homeViewModel = viewModel {
                    HomeViewModel()
                }
                val name by homeViewModel.name.observeAsState()
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "Greet Me!",
                        style = MaterialTheme.typography.h6
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    TextField(
                        value = name,
                        maxLines = 1,
                        label = { Text(text = "Enter your name") },
                        onValueChange = {
                            homeViewModel.setName(it)
                        }
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(
                        onClick = {
                            navigator.navigate(route = "/greeting/$name")
                        }
                    ) {
                        Text(text = "GO!")
                    }
                }
            }
            scene(route = "/greeting/{name}") { backStackEntry ->
                backStackEntry.path<String>("name")?.let { name ->
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.h6
                        )
                        Spacer(modifier = Modifier.height(30.dp))
                        Button(onClick = { navigator.goBack() }) {
                            Text(text = "GO BACK!")
                        }
                    }
                }
            }
        }
    }
}

class HomeViewModel : ViewModel() {
    val name = LiveData("")
    fun setName(value: String) {
        name.value = value
    }
}
```

![](https://raw.githubusercontent.com/Tlaster/PreCompose/master/media/greeting_app.gif)