// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.helpers

import be.scri.BuildConfig

enum class AppFlavor {
    CONJUGATE,
    KEYBOARDS,
}

object FlavorProvider {
    fun get(): AppFlavor =
        when (BuildConfig.FLAVOR) {
            "conjugate" -> AppFlavor.CONJUGATE
            "keyboards" -> AppFlavor.KEYBOARDS
            else -> error("Unknown flavor")
        }
}
