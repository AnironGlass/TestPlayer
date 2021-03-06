package com.anironglass.testplayer;

import android.content.Intent;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import static com.anironglass.testplayer.PickerActivity.TAG;

public class MediaPlayerActivity extends BasePlayerActivity implements
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnVideoSizeChangedListener,
        MediaPlayer.OnCompletionListener {

    static final String EXTRA_DRAW_VIEW_TYPE = "type";
    static final int TYPE_SURFACE_VIEW = -1;
    static final int TYPE_GL_SURFACE_VIEW = -2;
    static final int TYPE_TEXTURE_VIEW = -3;

    @IntDef({
            TYPE_SURFACE_VIEW,
            TYPE_GL_SURFACE_VIEW,
            TYPE_TEXTURE_VIEW,
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface DrawViewType {
    }

    private final MediaPlayer mediaPlayer = new MediaPlayer();
    private TrackSpinnerAdapter audioSpinnerAdapter;
    private FrameLayout rootView;
    private TextView playPauseButton;
    private View drawView;
    private Surface surface;
    @DrawViewType
    private int drawViewType = TYPE_SURFACE_VIEW;
    private boolean isPrepared = false;
    private boolean isPlaying = false;
    private int viewPortWidth;
    private int viewPortHeight;
    private float viewPortProportion;
    private float videoProportion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        if (null != intent) {
            drawViewType = intent.getIntExtra(EXTRA_DRAW_VIEW_TYPE, TYPE_SURFACE_VIEW);
        }

        initializeView();
        handleIntent(getIntent());
    }

    @Override
    protected void onDestroy() {
        if (null != surface) {
            surface.release();
        }
        release();
        super.onDestroy();
    }

    @Override
    void createPlayer(@NonNull Uri uri) {
        Log.d(TAG, "createPlayer(" + uri + ")");
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(getApplicationContext(), uri);
            mediaPlayer.setScreenOnWhilePlaying(true);
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.setOnErrorListener(this);
            mediaPlayer.setOnVideoSizeChangedListener(this);
            mediaPlayer.setOnCompletionListener(this);
            mediaPlayer.prepareAsync();
        } catch (IOException e) {
            e.printStackTrace();
            mediaPlayer.reset();
        }
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.d(TAG, "onPrepared()");
        isPrepared = true;
        setVideoSize(mediaPlayer.getVideoWidth(), mediaPlayer.getVideoHeight());
        populateAudioTracks();
        play();
        logTracks(mediaPlayer.getTrackInfo());
    }

    private void logTracks(@Nullable MediaPlayer.TrackInfo[] tracks) {
        if (null == tracks) {
            Log.d(TAG, "logTracks() -- no tracks");
        } else {
            for (int index = 0; index < tracks.length; index++) {
                MediaPlayer.TrackInfo track = tracks[index];
                Log.d(TAG, "track [" + index + "] " + track);
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, "onError() what=" + what + ", extra=" + extra);
        isPrepared = false;
        isPlaying = false;
        Toast.makeText(this, "ERROR", Toast.LENGTH_SHORT).show();
        return false;
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
        setVideoSize(width, height);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        clearAudioTracks();
    }

    private void initializeView() {
        rootView = new FrameLayout(this);
        rootView.setBackgroundColor(getColor(R.color.tvWhite));
        int padding = getResources().getDimensionPixelOffset(R.dimen.player_padding);
        rootView.setPadding(padding, padding, padding, padding);

        setContentView(rootView);

        initializeDrawView(rootView, drawViewType);
        initializeControlsView(rootView);
        initializeAudioTracksView(rootView);

        rootView.post(new Runnable() {
            @Override
            public void run() {
                viewPortWidth = rootView.getWidth() - rootView.getPaddingLeft() - rootView.getPaddingRight();
                viewPortHeight = rootView.getHeight() - rootView.getPaddingTop() - rootView.getPaddingBottom();
                viewPortProportion = (float) viewPortWidth / viewPortHeight;
                videoProportion = viewPortProportion;
            }
        });
    }

    private void initializeDrawView(@NonNull FrameLayout rootView, @DrawViewType int type) {
        switch (type) {
            case TYPE_TEXTURE_VIEW:
                setTitle(R.string.media_player_texture_view);
                drawView = createTextureView();
                break;
            case TYPE_GL_SURFACE_VIEW:
                setTitle(R.string.media_player_gl_surface_view);
                drawView = createGlSurfaceView();
                break;
            case TYPE_SURFACE_VIEW:
            default:
                setTitle(R.string.media_player_surface_view);
                drawView = createSurfaceView();
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        params.gravity = Gravity.CENTER;
        rootView.addView(drawView, params);
    }

    @NonNull
    private View createSurfaceView() {
        SurfaceView surfaceView = new SurfaceView(this);
        surfaceView.getHolder().addCallback(new SurfaceHolderCallback());
        return surfaceView;
    }

    @NonNull
    private View createGlSurfaceView() {
        GLSurfaceView glSurfaceView = new GLSurfaceView(this);
        glSurfaceView.setRenderer(new GLSurfaceViewRenderer());
        glSurfaceView.getHolder().addCallback(new SurfaceHolderCallback());
        return glSurfaceView;
    }

    @NonNull
    private View createTextureView() {
        TextureView textureView = new TextureView(this);
        textureView.setSurfaceTextureListener(new SurfaceTextureListener());
        return textureView;
    }

    private void initializeControlsView(@NonNull FrameLayout rootView) {
        LinearLayout controlsContainer = new LinearLayout(this);
        FrameLayout.LayoutParams controlsParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        controlsParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        controlsContainer.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams buttonsParams = new LinearLayout.LayoutParams(
                getResources().getDimensionPixelOffset(R.dimen.control_button_width),
                LinearLayout.LayoutParams.WRAP_CONTENT);
        int buttonsMargin = getResources().getDimensionPixelOffset(R.dimen.control_button_margin);
        buttonsParams.setMargins(buttonsMargin, buttonsMargin, buttonsMargin, buttonsMargin);

        playPauseButton = new TextView(new ContextThemeWrapper(this, R.style.ControlsButton));
        playPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPause();
            }
        });
        playPauseButton.setText(R.string.play);
        controlsContainer.addView(playPauseButton, buttonsParams);

        TextView stopButton = new TextView(new ContextThemeWrapper(this, R.style.ControlsButton));
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });
        stopButton.setText(R.string.stop);
        controlsContainer.addView(stopButton, buttonsParams);

        rootView.addView(controlsContainer, controlsParams);
    }

    private void initializeAudioTracksView(@NonNull FrameLayout rootView) {
        FrameLayout.LayoutParams contentParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );
        contentParams.gravity = Gravity.TOP | Gravity.END;
        int margin = getResources().getDimensionPixelOffset(R.dimen.control_button_margin);
        contentParams.setMargins(margin, margin, margin, margin);

        LinearLayout.LayoutParams itemParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );

        LinearLayout contentLayout = new LinearLayout(
                new ContextThemeWrapper(this, R.style.ContentBlockStyle)
        );
        contentLayout.setOrientation(LinearLayout.VERTICAL);

        TextView textView = new TextView(new ContextThemeWrapper(this, R.style.SpinnerTitle));
        textView.setText(R.string.audio_tracks);

        Spinner audioSpinner = new Spinner(this);
        audioSpinnerAdapter = new TrackSpinnerAdapter(this);
        audioSpinner.setAdapter(audioSpinnerAdapter);
        audioSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(
                    AdapterView<?> parent,
                    View view,
                    int position,
                    long id
            ) {
                handleTrackSelection(audioSpinnerAdapter.getItem(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // empty
            }
        });
        // Do this programmatically because form styles it works incorrectly
        audioSpinner.setBackgroundResource(R.drawable.spinner_background);
        audioSpinner.setMinimumWidth(
                getResources().getDimensionPixelOffset(R.dimen.min_track_spinner_width)
        );

        contentLayout.addView(textView, itemParams);
        contentLayout.addView(audioSpinner, itemParams);
        rootView.addView(contentLayout, contentParams);
    }

    private void clearAudioTracks() {
        audioSpinnerAdapter.clear();
        audioSpinnerAdapter.notifyDataSetChanged();
    }

    private void populateAudioTracks() {
        clearAudioTracks();

        List<PlayerTrack> audioTracks = getAudioTracks();
        audioSpinnerAdapter.addAll(audioTracks);
        audioSpinnerAdapter.notifyDataSetChanged();
    }

    private void handleTrackSelection(@Nullable PlayerTrack playerTrack) {
        if (playerTrack == null) return;
        mediaPlayer.selectTrack(playerTrack.getIndex());
    }

    @NonNull
    private List<PlayerTrack> getAudioTracks() {
        List<PlayerTrack> result = new ArrayList<>();
        MediaPlayer.TrackInfo[] trackInfos = mediaPlayer.getTrackInfo();
        if (trackInfos == null) return result;
        for (int i = 0; i < trackInfos.length; i++) {
            MediaPlayer.TrackInfo trackInfo = trackInfos[i];
            if (trackInfo.getTrackType() != MediaPlayer.TrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                continue;
            }
            result.add(toPlayerTrack(i, trackInfo));
        }
        return result;
    }

    private PlayerTrack toPlayerTrack(
            int index,
            @NonNull MediaPlayer.TrackInfo trackInfo
    ) {
        return new PlayerTrack(
                index,
                trackInfo.getLanguage()
        );
    }

    private void playPause() {
        if (isPlaying) {
            pause();
        } else {
            play();
        }
    }

    private void play() {
        Log.d(TAG, "play()");
        if (isPrepared) {
            playPauseButton.setText(R.string.pause);
            mediaPlayer.start();
            isPlaying = true;
        } else {
            createPlayer(data);
        }
    }

    private void pause() {
        Log.d(TAG, "pause()");
        playPauseButton.setText(R.string.play);
        mediaPlayer.pause();
        isPlaying = false;
    }

    private void stop() {
        Log.d(TAG, "stop()");
        playPauseButton.setText(R.string.play);
        clearAudioTracks();
        mediaPlayer.stop();
        isPrepared = false;
        isPlaying = false;
    }

    private void release() {
        Log.d(TAG, "release()");
        playPauseButton.setText(R.string.play);
        clearAudioTracks();
        mediaPlayer.release();
        isPrepared = false;
        isPlaying = false;
    }

    private void setVideoSize(int width, int height) {
        float newVideoProportion;
        if (width <= 0 || height <= 0) {
            newVideoProportion = viewPortProportion;
        } else {
            newVideoProportion = (float) width / height;
        }
        if (newVideoProportion != videoProportion) {
            videoProportion = newVideoProportion;
            fitDrawView();
        }
    }

    private void fitDrawView() {
        if (null != drawView) {
            FrameLayout.LayoutParams layoutParams =
                    (FrameLayout.LayoutParams) drawView.getLayoutParams();
            if (videoProportion > viewPortProportion) {
                layoutParams.width = viewPortWidth;
                layoutParams.height = (int) ((float) viewPortWidth / videoProportion);
            } else {
                layoutParams.width = (int) (videoProportion * (float) viewPortHeight);
                layoutParams.height = viewPortHeight;
            }
            drawView.setLayoutParams(layoutParams);
        }
    }

    private class SurfaceHolderCallback implements SurfaceHolder.Callback {

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            Log.d(TAG, "surfaceCreated()");
            mediaPlayer.setDisplay(holder);
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            Log.d(TAG, "surfaceDestroyed()");
            release();
        }
    }

    private class GLSurfaceViewRenderer implements GLSurfaceView.Renderer {
        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        }

        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
        }

        @Override
        public void onDrawFrame(GL10 gl) {
        }
    }

    private class SurfaceTextureListener implements TextureView.SurfaceTextureListener {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            Log.d(TAG, "onSurfaceTextureAvailable()");
            surface = new Surface(texture);
            mediaPlayer.setSurface(surface);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.d(TAG, "onSurfaceTextureDestroyed()");
            return false;
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    }

}
