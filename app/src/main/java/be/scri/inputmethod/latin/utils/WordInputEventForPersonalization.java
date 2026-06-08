/*
 * Copyright (C) 2014 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package be.scri.inputmethod.latin.utils;

import be.scri.latin.NgramContext;
import be.scri.latin.common.StringUtils;
import be.scri.latin.define.DecoderSpecificConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

// Note: this class is used as a parameter type of a native method. You should be careful when you
// rename this class or field name. See BinaryDictionary#addMultipleDictionaryEntriesNative().
public final class WordInputEventForPersonalization {
    public final int[] mTargetWord;
    public final int mPrevWordsCount;
    public final int[][] mPrevWordArray =
            new int[DecoderSpecificConstants.MAX_PREV_WORD_COUNT_FOR_N_GRAM][];
    public final boolean[] mIsPrevWordBeginningOfSentenceArray =
            new boolean[DecoderSpecificConstants.MAX_PREV_WORD_COUNT_FOR_N_GRAM];
    // Time stamp in seconds.
    public final int mTimestamp;

    public WordInputEventForPersonalization(final CharSequence targetWord,
            final NgramContext ngramContext, final int timestamp) {
        mTargetWord = StringUtils.toCodePointArray(targetWord);
        mPrevWordsCount = ngramContext.getPrevWordCount();
        ngramContext.outputToArray(mPrevWordArray, mIsPrevWordBeginningOfSentenceArray);
        mTimestamp = timestamp;
    }

    public static ArrayList<WordInputEventForPersonalization> createInputEventFrom(
            final List<String> tokens, final int timestamp,
            final Object spacingAndPunctuations, final Locale locale) {
        return new ArrayList<WordInputEventForPersonalization>();
    }
}
