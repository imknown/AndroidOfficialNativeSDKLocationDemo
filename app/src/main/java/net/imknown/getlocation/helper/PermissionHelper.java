package net.imknown.getlocation.helper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

public class PermissionHelper {

    private Activity mActivity;

    private static final String PACKAGE = "package:";

    public PermissionHelper(Activity a) {
        this.mActivity = a;
    }

    public boolean hasPermissions(String... permissions) {
        for (String permission : permissions) {
            if (!hasPermission(permission)) {
                return false;
            }
        }

        return true;
    }

    public boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(mActivity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissions(String permission, int resultCode, @NonNull final DialogInterface.OnClickListener listener) {
//        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
//            // 至少拒绝过一次, 但是没有选择 "不再提醒"
//        } else {
//            ActivityCompat.requestPermissions(mActivity, new String[]{permission}, resultCode);
//        }

        showWhyNeedPermissionDialog(permission, resultCode, listener);
    }

    private void showWhyNeedPermissionDialog(final String permission, final int resultCode, @NonNull final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final AlertDialog alertDialog = builder.create();

        builder.setMessage("不给权限的话, 就没法定位~");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();

                listener.onClick(dialog, which);
            }
        });

        builder.setPositiveButton("朕知道了", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();

                ActivityCompat.requestPermissions(mActivity, new String[]{permission}, resultCode);
            }
        });

        builder.show();
    }

    public void showMissingPermissionDialog(@NonNull final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        final AlertDialog alertDialog = builder.create();

        builder.setMessage("当前应用缺少必要权限\n请打开所需权限");

        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();

                listener.onClick(dialog, which);
            }
        });

        builder.setPositiveButton("设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                alertDialog.dismiss();
                startAppSettings();

                listener.onClick(dialog, which);
            }
        });

        builder.show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE + mActivity.getPackageName()));
        mActivity.startActivity(intent);
    }
}
