#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later

set -euo pipefail

FLAVOR="${1:-Keyboards}"

echo "=================================================="
echo "Starting Release Smoke Test for flavor: $FLAVOR"
echo "=================================================="

# Wait for device/emulator to boot
echo "Waiting for device to finish booting..."
adb wait-for-device
while [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; do
    echo "Device not ready yet, sleeping 2s..."
    sleep 2
done
echo "Device boot completed."

# Wake screen and dismiss lockscreen
adb shell input keyevent 82 || true
sleep 1

# Clear logcat buffer to only capture fresh logs
adb logcat -c

if [[ "$FLAVOR" == "Keyboards" ]]; then
    PACKAGE="be.scri.keyboards.debug"
    ACTIVITY="be.scri.activities.MainActivity"
    IME_SERVICE="be.scri.keyboards.debug/be.scri.services.EnglishKeyboardIME"

    echo "Installing Scribe Keyboards Minified APK..."
    ./gradlew installKeyboardsMinifiedDebug

    echo "Enabling Scribe keyboard IME: $IME_SERVICE"
    adb shell ime enable "$IME_SERVICE"

    echo "Setting Scribe keyboard as default IME..."
    adb shell ime set "$IME_SERVICE"

    echo "Active IMEs on system:"
    adb shell ime list -s

    echo "Launching Scribe Keyboards MainActivity..."
    adb shell am start -W -n "$PACKAGE/$ACTIVITY"
    sleep 4

    echo "Focusing practice text field to activate keyboard..."
    adb shell input tap 500 800 || true
    sleep 2

    echo "Simulating text input to trigger keyboard suggestion engine..."
    adb shell input text "hello" || true
    sleep 3

elif [[ "$FLAVOR" == "Conjugate" ]]; then
    PACKAGE="be.scri.conjugate.debug"
    ACTIVITY="be.scri.activities.MainActivity"

    echo "Installing Scribe Conjugate Minified APK..."
    ./gradlew installConjugateMinifiedDebug

    echo "Launching Scribe Conjugate MainActivity..."
    adb shell am start -W -n "$PACKAGE/$ACTIVITY"
    sleep 4

else
    echo "Unknown flavor '$FLAVOR'. Must be 'Keyboards' or 'Conjugate'."
    exit 1
fi

echo "Verifying process '$PACKAGE' is alive..."
PID=$(adb shell "pidof $PACKAGE 2>/dev/null || ps -A | grep $PACKAGE | awk '{print \$2}'" | tr -d '\r')
if [[ -z "$PID" ]]; then
    echo "ERROR: Process '$PACKAGE' is not running (crashed on startup)!"
    echo "Recent logcat dump:"
    adb logcat -d | tail -n 120
    exit 1
fi
echo "Process '$PACKAGE' is running (PID: $PID)"

echo "Checking logcat for crashes, unsatisfied link errors, or ANRs..."
if adb logcat -d | grep -E "(FATAL EXCEPTION|UnsatisfiedLinkError|ANR in $PACKAGE)"; then
    echo "ERROR: Critical crash, Linkage error, or ANR detected in release build!"
    exit 1
fi

echo "=================================================="
echo "Release Smoke Test for $FLAVOR PASSED!"
echo "=================================================="
