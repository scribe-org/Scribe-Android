/*
 * Copyright (C) 2011 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.android.inputmethod.keyboard;

import helium314.keyboard.latin.utils.JniUtils;

public class ProximityInfo {
    private static final String TAG = ProximityInfo.class.getSimpleName();
    public static final int MAX_PROXIMITY_CHARS_SIZE = 16;

    private long mNativeProximityInfo;
    static {
        JniUtils.loadNativeLibrary();
    }

    private static native long setProximityInfoNative(int displayWidth, int displayHeight,
            int gridWidth, int gridHeight, int mostCommonKeyWidth, int mostCommonKeyHeight,
            int[] proximityCharsArray, int keyCount, int[] keyXCoordinates, int[] keyYCoordinates,
            int[] keyWidths, int[] keyHeights, int[] keyCharCodes, float[] sweetSpotCenterXs,
            float[] sweetSpotCenterYs, float[] sweetSpotRadii);

    private static native void releaseProximityInfoNative(long nativeProximityInfo);

    public ProximityInfo() {
        final int gridWidth = 1;
        final int gridHeight = 1;
        final int[] proximityCharsArray = new int[gridWidth * gridHeight * MAX_PROXIMITY_CHARS_SIZE];
        final int[] keyXCoordinates = new int[0];
        final int[] keyYCoordinates = new int[0];
        final int[] keyWidths = new int[0];
        final int[] keyHeights = new int[0];
        final int[] keyCharCodes = new int[0];
        final float[] sweetSpotCenterXs = new float[0];
        final float[] sweetSpotCenterYs = new float[0];
        final float[] sweetSpotRadii = new float[0];

        mNativeProximityInfo = setProximityInfoNative(
                480, 800, // displayWidth, displayHeight
                gridWidth, gridHeight,
                48, 48, // mostCommonKeyWidth, mostCommonKeyHeight
                proximityCharsArray,
                0, // keyCount
                keyXCoordinates, keyYCoordinates,
                keyWidths, keyHeights,
                keyCharCodes,
                sweetSpotCenterXs, sweetSpotCenterYs,
                sweetSpotRadii
        );
    }

    public long getNativeProximityInfo() {
        return mNativeProximityInfo;
    }

    @Override
    protected void finalize() throws Throwable {
        try {
            if (mNativeProximityInfo != 0) {
                releaseProximityInfoNative(mNativeProximityInfo);
                mNativeProximityInfo = 0;
            }
        } finally {
            super.finalize();
        }
    }
}
