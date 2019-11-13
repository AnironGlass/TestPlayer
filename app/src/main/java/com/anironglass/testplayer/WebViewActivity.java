package com.anironglass.testplayer;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import static com.anironglass.testplayer.PickerActivity.TAG;

public class WebViewActivity extends BasePlayerActivity {

    private WebView webView;
    private ViewGroup fullScreenContainer;
    private View fullScreenView;
    private WebChromeClient.CustomViewCallback fullscreenViewCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FrameLayout contentView = new FrameLayout(this);
        contentView.setBackgroundColor(getColor(R.color.tvWhite));
        int padding = getResources().getDimensionPixelOffset(R.dimen.player_padding);
        contentView.setPadding(padding, padding, padding, padding);

        webView = new WebView(this);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new MyWebChromeClient());

        contentView.addView(webView, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        fullScreenContainer = new FrameLayout(this);
        fullScreenContainer.setVisibility(View.GONE);
        contentView.addView(fullScreenContainer, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT));

        setContentView(contentView);

        handleIntent(getIntent());
    }

    @Override
    void createPlayer(@NonNull Uri uri) {
        Log.d(TAG, "createPlayer(" + uri + ")");
        webView.loadUrl(uri.toString());
    }

    private class MyWebChromeClient extends  WebChromeClient {

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            Log.d(TAG, "onShowCustomView()");
            webView.setVisibility(View.GONE);
            fullScreenContainer.setVisibility(View.VISIBLE);
            fullScreenContainer.addView(view);
            fullScreenView = view;
            fullscreenViewCallback = callback;
            super.onShowCustomView(view, callback);
        }

        @Override
        public void onHideCustomView() {
            Log.d(TAG, "onHideCustomView()");
            fullScreenContainer.removeView(fullScreenView);
            if (null != fullscreenViewCallback) {
                fullscreenViewCallback.onCustomViewHidden();
            }
            fullScreenView = null;
            webView.setVisibility(View.VISIBLE);
            fullScreenContainer.setVisibility(View.GONE);
            super.onHideCustomView();
        }

    }

}
