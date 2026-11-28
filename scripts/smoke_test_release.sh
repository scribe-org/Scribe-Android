#!/usr/bin/env bash
# SPDX-License-Identifier: GPL-3.0-or-later

set -euo pipefail

FLAVOR="${1:-Keyboards}"

echo "=================================================="
echo "Starting Release Smoke Test for flavor: $FLAVOR"
echo "=================================================="

# Wait for device/emulator to boot.
echo "Waiting for device to finish booting..."
adb wait-for-device
while [[ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" != "1" ]]; do
    echo "Device not ready yet, sleeping 2s..."
    sleep 2
done
echo "Device boot completed."

# Wake screen and dismiss lock screen.
adb shell input keyevent 82 || true
sleep 1

# Clear logcat buffer to only capture fresh logs.
adb logcat -c

if [[ "$FLAVOR" == "Keyboards" ]]; then
    PACKAGE="be.scri.keyboards.debug"
    ACTIVITY="be.scri.activities.MainActivity"
    IME_SERVICE="be.scri.keyboards.debug/be.scri.services.EnglishKeyboardIME"

    echo "Installing Scribe Keyboards Minified APK..."
    ./gradlew installKeyboardsDebug -Pminify

    echo "Enabling Scribe keyboard IME: $IME_SERVICE"
    adb shell ime enable "$IME_SERVICE"

    echo "Setting Scribe keyboard as default IME..."
    adb shell ime set "$IME_SERVICE"

    echo "Active IMEs on system:"
    adb shell ime list -s

    echo "Launching Scribe Keyboards MainActivity..."
    adb shell am start -W -n "$PACKAGE/$ACTIVITY"
    sleep 3

    echo "Opening text input to display active keyboard IME..."
    adb shell am start -a android.intent.action.SENDTO -d "sms:5551234" || true
    sleep 3

    SCREEN_SIZE=$(adb shell wm size | awk '{print $NF}' | tr -d '\r')
    WIDTH=$(echo "$SCREEN_SIZE" | cut -d'x' -f1)
    HEIGHT=$(echo "$SCREEN_SIZE" | cut -d'x' -f2)

    # Tap input field.
    INPUT_X=$(( WIDTH / 2 ))
    INPUT_Y=$(( HEIGHT * 90 / 100 ))
    echo "Focusing input field at ($INPUT_X, $INPUT_Y)..."
    adb shell input tap "$INPUT_X" "$INPUT_Y" || true
    sleep 2

    # Check whether the keyboard IME is shown.
    if adb shell dumpsys input_method | grep -q "mInputShown=true"; then
        echo "Keyboard IME is visible on screen (mInputShown=true)."
    else
        echo "Retrying focus tap on input field..."
        adb shell input tap "$INPUT_X" "$INPUT_Y" || true
        sleep 2
    fi

    # Tap keyboard keys in the center of the soft keyboard to trigger the JNI suggestion engine.
    KEY_X=$(( WIDTH / 2 ))
    KEY_Y=$(( HEIGHT * 70 / 100 ))
    echo "Tapping keyboard keys at ($KEY_X, $KEY_Y) to trigger native suggestion engine..."
    adb shell input tap "$KEY_X" "$KEY_Y" || true
    sleep 1
    adb shell input tap "$KEY_X" "$KEY_Y" || true
    sleep 2

elif [[ "$FLAVOR" == "Conjugate" ]]; then
    PACKAGE="be.scri.conjugate.debug"
    ACTIVITY="be.scri.activities.MainActivity"

    echo "Installing Scribe Conjugate Minified APK..."
    ./gradlew installConjugateDebug -Pminify

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

echo "Checking logcat for crashes or unsatisfied link errors in $PACKAGE..."

if adb logcat -d --pid="$PID" | grep -E "(FATAL EXCEPTION|UnsatisfiedLinkError|NoSuchMethodError|AndroidRuntime)"; then
    echo "ERROR: Critical crash or Linkage error detected in $PACKAGE!"
    exit 1
fi

if adb logcat -d | grep -E "(Process: $PACKAGE.*FATAL EXCEPTION|ANR in $PACKAGE)"; then
    echo "ERROR: Process crash or ANR detected in $PACKAGE!"
    exit 1
fi

echo "=================================================="
echo "Release Smoke Test for $FLAVOR PASSED!"
echo "=================================================="
