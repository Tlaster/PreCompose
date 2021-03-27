# Setup

## Add Dependency

Add Jitpack
```
maven { url = uri("https://jitpack.io") }
```
Add the dependency **in your common module's commonMain sourceSet**
```
api("com.github.Tlaster.PreCompose:precompose:$precompose_version")
```
## Android
Update the `AndroidManifest.xml` file from your Android project (not the common project's Android library) and add these two lines to the Activity.
```
android:windowSoftInputMode="adjustResize"
android:configChanges="orientation|keyboardHidden|keyboard|screenSize|smallestScreenSize|locale|layoutDirection|fontScale|screenLayout|density|uiMode"
```
Since compose render itself so it can handle very much everything(like what Flutter does), it's ok to let compose handling everything.

And for the Activity, change the parent class to `moe.tlaster.precompose.lifecycle.PreComposeActivity` and use `moe.tlaster.precompose.lifecycle.setContent` for setting compose content

## Desktop
At the `main.kt` file in your desktop project's `jvmMain` change the `Window` to `moe.tlaster.precompose.PreComposeWindow`

## Done!
That's it! Enjoying the PreCompose! Now you can write all your business logic and ui code in `commonMain`
