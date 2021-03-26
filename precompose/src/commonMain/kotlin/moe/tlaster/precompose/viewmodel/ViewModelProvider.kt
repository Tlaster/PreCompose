package moe.tlaster.precompose.viewmodel

internal inline fun <reified T : ViewModel> ViewModelStore.getViewModel(
    creator: () -> T,
): T {
    val key = T::class.qualifiedName.toString()
    return getViewModel(key, creator)
}

internal inline fun <reified T : ViewModel> ViewModelStore.getViewModel(
    key: String,
    creator: () -> T,
): T {
    val existing = get(key)
    if (existing != null && existing is T) {
        @Suppress("UNCHECKED_CAST")
        return existing
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
