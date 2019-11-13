package com.anironglass.testplayer;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.anironglass.testplayer.PickerActivity.TAG;

class StringListParser {

    @NonNull
    static List<String> toStringList(@Nullable String json) {
        int size = 0;
        JSONArray jsonArray = null;
        if (null != json) {
            try {
                jsonArray = new JSONArray(json);
            } catch (JSONException e) {
                Log.e(TAG, "ERROR", e);
            }
        }
        if (null != jsonArray) {
            size = jsonArray.length();
        }
        List<String> result = new ArrayList<>();
        if (null != jsonArray) {
            for (int index = 0; index < size; index++) {
                try {
                    String value = jsonArray.getString(index);
                    if (null != value) {
                        result.add(value);
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "ERROR", e);
                }
            }
        }
        return result;
    }

    @NonNull
    static String toJson(@Nullable List<String> values) {
        JSONArray jsonArray = new JSONArray();
        if (null != values) {
            for (String value : values) {
                jsonArray.put(value);
            }
        }
        return jsonArray.toString();
    }

}
