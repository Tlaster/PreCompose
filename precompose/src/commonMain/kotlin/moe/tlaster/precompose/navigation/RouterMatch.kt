package moe.tlaster.precompose.navigation

import kotlin.math.min

internal class RouterMatch {
    var matches = false
    var route: Route? = null
    var vars = arrayListOf<String>()
    var pathMap = linkedMapOf<String, String>()
    fun key(keys: List<String>) {
        for (i in 0 until min(keys.size, vars.size)) {
            pathMap[keys[i]] = vars.removeAt(i)
        }
    }

    fun truncate(size: Int) {
        var sizeInt = size
        while (sizeInt < vars.size) {
            vars.removeAt(sizeInt++)
        }
    }

    fun value(value: String) {
        vars.add(value)
    }

    fun pop() {
        if (vars.isNotEmpty()) {
            vars.removeLast()
        }
    }

    fun found(route: Route): RouterMatch {
        this.route = route
        matches = true
        return this
    }
}