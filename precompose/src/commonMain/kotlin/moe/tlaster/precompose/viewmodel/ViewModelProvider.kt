package moe.tlaster.precompose.viewmodel

import kotlin.reflect.KClass

inline fun <reified T : ViewModel> ViewModelStore.getViewModel(
    noinline creator: () -> T,
): T {
    val key = T::class.qualifiedName.toString()
    return getViewModel(key, T::class, creator)
}

fun <T : ViewModel> ViewModelStore.getViewModel(
    key: String,
    clazz: KClass<T>,
    creator: () -> T,
): T {
    val existing = get(key)
    if (existing != null && clazz.isInstance(existing)) {
        @Suppress("UNCHECKED_CAST")
        return existing as T
    } else {
        @Suppress("ControlFlowWithEmptyBody")
        if (existing != null) {
            // TODO: log a warning.
        }
    }
    val viewModel = creator.invoke()
    put(key, viewModel)
    return viewModel
}
