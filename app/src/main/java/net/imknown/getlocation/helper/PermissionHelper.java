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

/**
 * 参考自
 * http://blog.csdn.net/u011068996/article/details/50602100
 */
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
        if (ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permission)) {
            // 至少拒绝过一次, 但是没有选择 "不再提醒"
            showWhyNeedPermissionDialog(permission, resultCode, listener);
        } else {
            ActivityCompat.requestPermissions(mActivity, new String[]{permission}, resultCode);
        }
    }

    private void showWhyNeedPermissionDialog(final String permission, final int resultCode, @NonNull final DialogInterface.OnClickListener listener) {
        showDialog("您之前至少拒绝过一次, 但是不给权限的话, 就没法定位了~", "现在定位", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        listener.onClick(dialogInterface, which);
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        ActivityCompat.requestPermissions(mActivity, new String[]{permission}, resultCode);
                        break;
                }
            }
        });
    }

    public void showMissingPermissionDialog() {
        showDialog("您已经彻底拒绝, 请手动授予权限~", "现在设置", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                switch (which) {
                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    case DialogInterface.BUTTON_POSITIVE:
                        startAppSettings();
                        break;
                }
            }
        });
    }

    private void showDialog(String msg, String positiveMsg, DialogInterface.OnClickListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        builder.setMessage(msg);
        builder.setNegativeButton(android.R.string.cancel, listener);
        builder.setPositiveButton(positiveMsg, listener);
        builder.show();
    }

    // 启动应用的设置
    private void startAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse(PACKAGE + mActivity.getPackageName()));
        mActivity.startActivity(intent);
    }
}
