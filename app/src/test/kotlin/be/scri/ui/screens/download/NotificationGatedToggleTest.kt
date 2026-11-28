// SPDX-License-Identifier: GPL-3.0-or-later

package be.scri.ui.screens.download

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class NotificationGatedToggleTest {
    private class Harness(
        var permissionMissing: Boolean,
    ) {
        var permissionRequests = 0
        val applied = mutableListOf<Boolean>()
        val toggle =
            NotificationGatedToggle(
                needsPermission = { permissionMissing },
                requestPermission = { permissionRequests++ },
                apply = { applied.add(it) },
            )
    }

    @Test
    fun turningOnWithPermissionAppliesAtOnce() {
        val h = Harness(permissionMissing = false)

        h.toggle.onUserToggle(true)

        assertEquals(listOf(true), h.applied)
        assertEquals(0, h.permissionRequests)
    }

    @Test
    fun turningOnWithoutPermissionAsksAndAppliesNothingYet() {
        val h = Harness(permissionMissing = true)

        h.toggle.onUserToggle(true)

        assertEquals(1, h.permissionRequests)
        assertEquals(emptyList<Boolean>(), h.applied)
    }

    @Test
    fun grantedPermissionTurnsTheSwitchOn() {
        val h = Harness(permissionMissing = true)

        h.toggle.onUserToggle(true)
        h.toggle.onPermissionResult(granted = true)

        assertEquals(listOf(true), h.applied)
    }

    @Test
    fun deniedPermissionLeavesTheSwitchOffAndSchedulesNothing() {
        val h = Harness(permissionMissing = true)

        h.toggle.onUserToggle(true)
        h.toggle.onPermissionResult(granted = false)

        assertEquals(emptyList<Boolean>(), h.applied)
    }

    @Test
    fun turningOffNeverAsksForPermission() {
        val h = Harness(permissionMissing = true)

        h.toggle.onUserToggle(false)

        assertEquals(listOf(false), h.applied)
        assertEquals(0, h.permissionRequests)
    }
}
