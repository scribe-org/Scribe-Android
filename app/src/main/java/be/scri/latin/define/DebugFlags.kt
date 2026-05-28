/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package be.scri.latin.define

import android.content.Context

object DebugFlags {
    @JvmField
    var DEBUG_ENABLED = false

    fun init(context: Context) {
        // No-op for Scribe-Android
    }
}
