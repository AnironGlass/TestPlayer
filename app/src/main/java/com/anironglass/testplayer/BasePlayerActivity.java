package com.anironglass.testplayer;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

abstract class BasePlayerActivity extends Activity {

    private static final int REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE = 1;

    protected Uri data;

    abstract void createPlayer(@NonNull Uri uri);

    void handleIntent(@Nullable Intent intent) {
        if (null == intent) return;
        data = intent.getData();
        if (null != data) {
            String scheme = data.getScheme();
            boolean isExternalStorageData = null == scheme || "file".equals(scheme);
            if (isExternalStorageData
                    && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                    && !isPermissionGranted(Manifest.permission.READ_EXTERNAL_STORAGE)) {
                requestPermission(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE);
            } else {
                createPlayer(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE_PERMISSION_EXTERNAL_STORAGE) {
            boolean isGranted = PackageManager.PERMISSION_GRANTED == grantResults[0];
            if (isGranted) {
                if (null != data) {
                    createPlayer(data);
                }
            } else {
                Toast.makeText(this, "STORAGE PERMISSION DENIED", Toast.LENGTH_SHORT)
                        .show();
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressWarnings("SameParameterValue")
    boolean isPermissionGranted(@NonNull String permission) {
        int checkPermissionResult = checkSelfPermission(permission);
        return PackageManager.PERMISSION_GRANTED == checkPermissionResult;
    }

    @RequiresApi(Build.VERSION_CODES.M)
    @SuppressWarnings("SameParameterValue")
    void requestPermission(@NonNull String permission, int requestCode) {
        requestPermissions(
                new String[]{permission},
                requestCode);
    }

}
