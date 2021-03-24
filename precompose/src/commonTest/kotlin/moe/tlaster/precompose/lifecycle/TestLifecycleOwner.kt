package moe.tlaster.precompose.lifecycle

class TestLifecycleOwner : LifecycleOwner {
    override val lifecycle by lazy {
        LifecycleRegistry()
    }
}
