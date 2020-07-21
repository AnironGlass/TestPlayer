package com.anironglass.testplayer;

import android.text.TextUtils;

import androidx.annotation.Nullable;

public final class PlayerTrack {
    private final int index;
    private final String lang;

    public PlayerTrack(int index, @Nullable String lang) {
        this.index = index;
        this.lang = lang;
    }

    public int getIndex() {
        return index;
    }

    public String getName() {
        StringBuilder name = new StringBuilder("Track ");
        name.append(index);
        if (!TextUtils.isEmpty(lang)) {
            name.append(" (");
            name.append(lang);
            name.append(')');
        }
        return name.toString();
    }
}
