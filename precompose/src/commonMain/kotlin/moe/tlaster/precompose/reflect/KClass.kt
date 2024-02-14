package moe.tlaster.precompose.reflect

import kotlin.reflect.KClass

expect val <T : Any> KClass<T>.canonicalName: String?
