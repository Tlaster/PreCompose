package moe.tlaster.precompose.koin

import androidx.compose.runtime.Composable
import moe.tlaster.precompose.reflect.canonicalName
import moe.tlaster.precompose.stateholder.LocalStateHolder
import moe.tlaster.precompose.stateholder.StateHolder
import moe.tlaster.precompose.viewmodel.ViewModel
import org.koin.compose.LocalKoinScope
import org.koin.core.parameter.ParametersDefinition
import org.koin.core.qualifier.Qualifier
import org.koin.core.scope.Scope
import kotlin.reflect.KClass

@Composable
inline fun <reified T : ViewModel> koinViewModel(
    qualifier: Qualifier? = null,
    stateHolder: StateHolder = checkNotNull(LocalStateHolder.current) {
        "No StateHolder was provided via LocalStateHolder"
    },
    key: String? = null,
    scope: Scope = LocalKoinScope.current,
    noinline parameters: ParametersDefinition? = null,
): T {
    return koinViewModel(
        T::class,
        qualifier,
        stateHolder,
        key,
        scope,
        parameters,
    )
}

@Composable
fun <T : ViewModel> koinViewModel(
    vmClass: KClass<T>,
    qualifier: Qualifier? = null,
    stateHolder: StateHolder = checkNotNull(LocalStateHolder.current) {
        "No StateHolder was provided via LocalStateHolder"
    },
    key: String? = null,
    scope: Scope = LocalKoinScope.current,
    parameters: ParametersDefinition? = null,
): T {
    return resolveViewModel(
        vmClass,
        stateHolder,
        key,
        qualifier,
        scope,
        parameters,
    )
}

private fun <T : ViewModel> resolveViewModel(
    vmClass: KClass<T>,
    stateHolder: StateHolder,
    key: String? = null,
    qualifier: Qualifier? = null,
    scope: Scope,
    parameters: ParametersDefinition? = null,
): T {
    return stateHolder.getOrPut(qualifier?.value ?: key ?: vmClass.canonicalName ?: "") {
        scope.get(vmClass, qualifier, parameters)
    }
}
