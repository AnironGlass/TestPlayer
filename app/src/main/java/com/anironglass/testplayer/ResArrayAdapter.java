package com.anironglass.testplayer;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import java.util.ArrayList;
import java.util.List;

class ResArrayAdapter extends BaseAdapter {

    private final LayoutInflater inflater;
    private final int layoutResId;
    private final int[] resIdArray;
    private final List<String> strings;

    ResArrayAdapter(@NonNull Context context, @LayoutRes int layoutResId, @StringRes int[] resIdArray) {
        this.resIdArray = resIdArray;
        this.layoutResId = layoutResId;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        strings = new ArrayList<>();
        Resources resources = context.getResources();
        for (int reaIs : resIdArray) {
            strings.add(resources.getString(reaIs));
        }
    }

    @Override
    public int getCount() {
        return strings.size();
    }

    @Override
    public Object getItem(int position) {
        return strings.get(position);
    }

    @Override
    public long getItemId(int position) {
        return resIdArray[position];
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null) {
            view = inflater.inflate(layoutResId, parent, false);
        }
        ((TextView) view).setText(strings.get(position));
        return view;
    }

}
