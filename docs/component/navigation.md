# Navigation

Similar to Jetpack Navigation, but a little different.

You still have the `NavHost` composable function to define the navigation graph like what you've done in Jetpack Navigation, and it behaves like what Jetpack Navigation provides. `NavHost` provides back stack management and stack lifecycle and viewModel management.

## Quick example
```kotlin
// Define a navigator, which is a replacement for Jetpack Navigation's NavController
val navigator = rememberNavigator()
NavHost(
    // Assign the navigator to the NavHost
    navigator = navigator,
    // Navigation transition for the scenes in this NavHost, this is optional
    navTransition = NavTransition(),
    // The start destination
    initialRoute = "/home",
) {
    // Define a scene to the navigation graph
    scene(
        // Scene's route path
        route = "/home",
        // Navigation transition for this scene, this is optional
        navTransition = NavTransition(),
    ) {
        Text(text = "Hello!")
    }
}
```

## Navigator

Replacement for Jetpack Navigation's NavController

 - `Navigator.navigate(route: String, options: NavOptions? = null)`  
Navigate to a route in the current RouteGraph with optional NavOptions

 - `Navigator.goBack()`  
Attempts to navigate up in the navigation hierarchy

 - `Navigator.canGoBack: Boolean`  
Check if navigator can navigate up

### NavOptions

Similar to Jetpack Navigation's NavOptions, you can have NavOptions like this:
```kotlin
navigator.navigate(
    "/home",
    NavOptions(
        // Launch the scene as single top
        launchSingleTop = true,
    ),
)
```
or you can have `popUpTo` functionality
```kotlin
navigator.navigate(
    "/detail",
    NavOptions(
        popUpTo = PopUpTo(
            // The destination of popUpTo
            route = "/home",
            // Whether the popUpTo destination should be popped from the back stack.
            inclusive = true,
        )
    ),
)
```

## Scene route pattern

### Static
```kotlin
scene(route = "/home") {

}
```

### Variable
```kotlin
scene(route = "/detail/{id}") { backStackEntry ->
    val id: Int? = backStackEntry.path<Int>("id")
}
```
this is most common usage of navigation route

Optional path variable
```kotlin
scene(route = "/detail/{id}?") { backStackEntry ->
    val id: Int? = backStackEntry.path<Int>("id")
}
```
The trailing ? makes the path variable optional. The route matches:
 - `/detail`
 - `/detail/123`
 - `/detail/asd`

### Regex
```kotlin
scene(route = "/detail/{id:[0-9]+}") { backStackEntry ->
    val id: Int? = backStackEntry.path<Int>("id")
}
```
You can define a path variable: `id`. Regex expression is everything after the first `:`, like: `[0-9]+`

Optional syntax is also supported for regex path variable: `/user/{id:[0-9]+}?`:
 - matches `/user`
 - matches `/user/123`

### Group
```kotlin
group(route = "/group", initialRoute = "/nestedScreen1") {
    scene(route = "/nestedScreen1") {
        
    }
    scene(route = "/nestedScreen2") {
        
    }
}
```

 ## QueryString
 
 **DO NOT** define your query string in scene route, this will have no effect on both navigation route and query string.

 You can pass your query string as to `Navigator.navigate(route: String)`, like: `Navigator.navigate("/detail/123?my=query")`

And you can retrieve query string from `BackStackEntry.query(name: String)`

```kotlin
scene(route = "/detail/{id}") { backStackEntry ->
    val my: String? = backStackEntry.query<String>("my")
}
```

If your query string is a list, you can retrieve by `BackStackEntry.queryList(name: String)`

## Navigation transition
You can define a `NavTransition` for both `NavHost` and `scene`, PreCompose will use the `scene`'s `NavTransition` if the `scene` define a `NavTransition`, otherwise will fall back to `NavHost`'s `NavTransition`.

There are 4 transition type for `NavTransition`
 - `createTransition`  
 Transition the scene that about to appear for the first time, similar to activity onCreate

  - `destroyTransition`  
Transition the scene that about to disappear forever, similar to activity onDestroy

 - `pauseTransition`  
 Transition the scene that will be pushed into back stack, similar to activity onPause

  - `resumeTransition`  
  Transition the scene that about to show from the back stack, similar to activity onResume
