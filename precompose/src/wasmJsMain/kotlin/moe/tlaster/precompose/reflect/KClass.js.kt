package moe.tlaster.precompose.reflect

import kotlin.reflect.KClass

actual val <T : Any> KClass<T>.canonicalName: String?
    // qualifiedName is unsupported [This reflection API is not supported yet in JavaScript]
    get() = this.simpleName
