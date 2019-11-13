package com.anironglass.testplayer;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PickerActivity extends Activity {

    static final String TAG = "TestPlayer";

    private static final int[] PLAYER_TYPES_ARRAY = new int[] {
            R.string.media_player_surface_view,
            R.string.media_player_gl_surface_view,
            R.string.media_player_texture_view,
            R.string.web_view,
            R.string.google_chrome,
    };

    private final List<String> uriList = new ArrayList<>();
    private TextView noUriView;
    private ListView uriListView;
    private ListView playerTypesListView;
    private ArrayAdapter<String> uriListAdapter;
    private UriRepository uriRepository;
    private AlertDialog.Builder dialogBuilder;
    private int positionToDelete = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picker);
        initializeWindow(getWindow());
        Log.i(TAG, " " + this + " onCreate()");

        if (null == uriRepository) {
            uriRepository = new UriRepository(this);
        }

        if (null == dialogBuilder) {
            dialogBuilder = new AlertDialog.Builder(this)
                    .setTitle(R.string.delete)
                    .setPositiveButton(
                            R.string.delete,
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteUri(positionToDelete);
                                    positionToDelete = -1;
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .setCancelable(true);
        }

        if (null == savedInstanceState) {
            initializeView();
            setUri(getIntent());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.i(TAG, " " + this + " onNewIntent(" + intent.getData() + ")");
        setUri(intent);
    }

    @Override
    protected void onDestroy() {
        uriRepository = null;
        super.onDestroy();
    }

    private void initializeWindow(@NonNull Window window) {
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.format = PixelFormat.TRANSLUCENT;
        window.setAttributes(layoutParams);
    }

    private void initializeView() {
        playerTypesListView = findViewById(R.id.player_type_list_view);
        ListAdapter adapter = new ResArrayAdapter(
                this,
                R.layout.item_player_type,
                PLAYER_TYPES_ARRAY
        );
        playerTypesListView.setAdapter(adapter);
        playerTypesListView.setOnItemClickListener(new PlayerTypeListClickListener());
        playerTypesListView.setSelection(0);

        noUriView = findViewById(R.id.no_uri_view);

        uriListView = findViewById(R.id.uri_list_view);
        uriListView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        uriListAdapter = new ArrayAdapter<>(
                this,
                R.layout.item_uri,
                uriList
        );
        uriListView.setAdapter(uriListAdapter);
        uriListView.setOnItemClickListener(new UriListClickListener());
        uriListView.setOnItemLongClickListener(new UriListLongClickListener());
    }

    private void setUri(@Nullable Intent intent) {
        boolean newUri = false;
        if (null != intent) {
            Uri uri = intent.getData();
            if (null != uri) {
                newUri = true;
                uriRepository.addOrUpdateUri(uri.toString());
                uriListView.smoothScrollToPosition(0);
            }
            updateUriList();
        }

        if (uriList.isEmpty()) {
            noUriView.setVisibility(View.VISIBLE);
            uriListView.setVisibility(View.GONE);

        } else {
            noUriView.setVisibility(View.GONE);
            uriListView.setVisibility(View.VISIBLE);
            if (newUri) {
                uriRepository.setCurrentListPosition(0);
                uriListView.setItemChecked(0, true);
            } else {
                int selectedPosition = uriRepository.getCurrentListPosition();
                if (selectedPosition > -1) {
                    uriListView.smoothScrollToPosition(selectedPosition);
                    uriListView.setSelection(selectedPosition);
                    uriListView.setItemChecked(selectedPosition, true);
                }
            }
        }

        playerTypesListView.requestFocus();
        Log.i(TAG, "setUri: " + uriList);
    }

    private void updateUriList() {
        uriList.clear();
        uriList.addAll(uriRepository.getStoredUris());
        uriListAdapter.notifyDataSetChanged();
    }

    @Nullable
    private Uri getSelectedUri() {
        Uri selectedUri = null;
        int checkedPosition = uriListView.getCheckedItemPosition();
        if (checkedPosition >= 0 && checkedPosition < uriList.size()) {
            selectedUri = Uri.parse(uriList.get(checkedPosition));
        }
        return selectedUri;
    }

    private void deleteUri(int position) {
        if (position >= 0 && position < uriList.size()) {
            String uri = uriList.get(position);
            if (null != uri) {
                uriRepository.delete(uri);
                int checkedPosition = uriRepository.getCurrentListPosition();
                if (position == checkedPosition) {
                    uriRepository.setCurrentListPosition(-1);
                    uriListView.setItemChecked(position, false);
                }
                updateUriList();
            }
        }
    }

    private class PlayerTypeListClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Intent intent;
            int playerTypeResId = PLAYER_TYPES_ARRAY[position];
            String playerType = getString(playerTypeResId);
            switch (playerTypeResId) {
                case R.string.google_chrome:
                    intent = new Intent();
                    intent.setClassName(
                            "com.android.chrome",
                            "org.chromium.chrome.browser.document.ChromeLauncherActivity");
                    ResolveInfo ri = getPackageManager().resolveActivity(intent, 0);
                    if (null == ri) {
                        intent = null;
                        Toast.makeText(PickerActivity.this, R.string.not_installed, Toast.LENGTH_SHORT)
                                .show();
                    } else {
                        intent.setAction(Intent.ACTION_VIEW);
                    }
                    break;
                case R.string.web_view:
                    intent = new Intent(PickerActivity.this, WebViewActivity.class);
                    break;
                case R.string.media_player_texture_view:
                    intent = new Intent(PickerActivity.this, MediaPlayerActivity.class);
                    intent.putExtra(MediaPlayerActivity.EXTRA_DRAW_VIEW_TYPE, MediaPlayerActivity.TYPE_TEXTURE_VIEW);
                    break;
                case R.string.media_player_gl_surface_view:
                    intent = new Intent(PickerActivity.this, MediaPlayerActivity.class);
                    intent.putExtra(MediaPlayerActivity.EXTRA_DRAW_VIEW_TYPE, MediaPlayerActivity.TYPE_GL_SURFACE_VIEW);
                    break;
                case R.string.media_player_surface_view:
                default:
                    intent = new Intent(PickerActivity.this, MediaPlayerActivity.class);
                    intent.putExtra(MediaPlayerActivity.EXTRA_DRAW_VIEW_TYPE, MediaPlayerActivity.TYPE_SURFACE_VIEW);
                    break;
            }
            if (null != intent) {
                Log.i(TAG, "start: " + playerType);
                Uri selectedUri = getSelectedUri();
                if (null != selectedUri) {
                    intent.setData(selectedUri);
                    startActivity(intent);
                } else {
                    Toast.makeText(PickerActivity.this, R.string.no_data_source, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        }
    }

    private class UriListClickListener implements AdapterView.OnItemClickListener {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            playerTypesListView.requestFocus();
            uriRepository.setCurrentListPosition(position);
        }
    }

    private class UriListLongClickListener implements AdapterView.OnItemLongClickListener {
        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            if (position >= 0 && position < uriList.size()) {
                String uri = uriList.get(position);
                if (null != uri) {
                    positionToDelete = position;
                    dialogBuilder.setMessage(uriList.get(position));
                    dialogBuilder.show();
                }
            }
            return true;
        }
    }

}
