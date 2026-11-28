# SPDX-License-Identifier: GPL-3.0-or-later

-keepclasseswithmembernames class * {
    native <methods>;
}

-keep class be.scri.inputmethod.keyboard.** { *; }
-keep class be.scri.inputmethod.latin.** { *; }
-keep class be.scri.latin.** { *; }

# WorkManager looks this class up by name. Keep the worker constructor.
-keep class be.scri.helpers.dataupdate.DataUpdateCheckWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
