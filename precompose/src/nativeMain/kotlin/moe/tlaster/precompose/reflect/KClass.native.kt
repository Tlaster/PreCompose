package moe.tlaster.precompose.reflect

import kotlin.reflect.KClass

actual val <T : Any> KClass<T>.canonicalName: String?
    get() = this.qualifiedName
