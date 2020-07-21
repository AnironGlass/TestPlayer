package com.anironglass.testplayer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class TrackSpinnerAdapter extends ArrayAdapter<PlayerTrack> {

    private static final int LAYOUT_RES = android.R.layout.simple_spinner_item;
    private static final int LAYOUT_DROPDOWN_RES = android.R.layout.simple_spinner_dropdown_item;

    private final LayoutInflater mLayoutInflater;

    public TrackSpinnerAdapter(@NonNull Context context) {
        super(context, LAYOUT_RES);
        mLayoutInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        TextView textView = (TextView) createViewFromResource(
                mLayoutInflater,
                convertView,
                LAYOUT_RES,
                parent
        );
        bindView(textView, position);
        return textView;
    }

    @Override
    public View getDropDownView(
            int position,
            @Nullable View convertView,
            @NonNull ViewGroup parent
    ) {
        TextView textView = (TextView) createViewFromResource(
                mLayoutInflater,
                convertView,
                LAYOUT_DROPDOWN_RES,
                parent
        );
        bindView(textView, position);
        return textView;
    }

    private View createViewFromResource(
            LayoutInflater inflater,
            @Nullable View convertView,
            @LayoutRes int layoutRes,
            @NonNull ViewGroup parent
    ) {
        TextView view;
        if (convertView == null) {
            view = (TextView) inflater.inflate(layoutRes, parent, false);
        } else {
            view = (TextView) convertView;
        }

        return view;
    }

    private void bindView(
            TextView contentView,
            int position
    ) {
        PlayerTrack track = getItem(position);
        if (track != null) {
            contentView.setText(track.getName());
        }
    }
}
