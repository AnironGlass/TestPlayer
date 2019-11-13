package com.anironglass.testplayer;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.List;

class UriRepository {

    private static final int URI_REPOSITORY_SIZE = 20;

    private static final String NAME = "uri_repository";
    private static final String PREF_DATA = "data";
    private static final String PREF_SELECTED = "selected";

    private final SharedPreferences preferences;

    UriRepository(@NonNull Context context) {
        preferences = context.getSharedPreferences(NAME, Context.MODE_PRIVATE);
    }

    void addOrUpdateUri(@NonNull String uri) {
        List<String> storedUris = getStoredUris();
        if (storedUris.isEmpty()) {
            storedUris.add(uri);
        } else {
            int position = storedUris.indexOf(uri);
            if (position == -1) {
                storedUris.add(0, uri);
                int size = Math.min(URI_REPOSITORY_SIZE, storedUris.size() + 1);
                if (storedUris.size() > size) {
                    storedUris.remove(storedUris.size() - 1);
                }
            } else {
                storedUris.add(0, storedUris.remove(position));
            }
        }
        preferences.edit()
                .putString(PREF_DATA, StringListParser.toJson(storedUris))
                .apply();
    }

    void delete(@NonNull String uri) {
        List<String> storedUris = getStoredUris();
        if (!storedUris.isEmpty()) {
            storedUris.remove(uri);
        }
        preferences.edit()
                .putString(PREF_DATA, StringListParser.toJson(storedUris))
                .apply();
    }

    @NonNull
    List<String> getStoredUris() {
        return StringListParser.toStringList(preferences.getString(PREF_DATA, null));
    }

    void setCurrentListPosition(int selected) {
        preferences.edit()
                .putInt(PREF_SELECTED, selected)
                .apply();
    }

    int getCurrentListPosition() {
        return preferences.getInt(PREF_SELECTED, -1);
    }

}
