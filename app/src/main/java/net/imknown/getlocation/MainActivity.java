package net.imknown.getlocation;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import net.imknown.getlocation.helper.PermissionHelper;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends Activity implements LocationListener {
    private CheckBox showMore;

    private TextView text;

    private static final long MIN_TIME = 1000l;
    private static final float MIN_DISTANCE = 10f;

    private LocationManager locationManager;

    private int selectedPosition;

    private DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS", Locale.CHINA);

    private PermissionHelper mPermissionHelper;

    private static final int PERMISSION_ACCESS_LOCATION = 0;

    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        showMore = (CheckBox) findViewById(R.id.showMore);

        text = (TextView) findViewById(R.id.text);

        ((Spinner) findViewById(R.id.options)).setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                MainActivity.this.selectedPosition = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (selectedPosition) {
                    case 0:
                        provider = LocationManager.GPS_PROVIDER;
                        tryToLocation();
                        break;
                    case 1:
                        provider = LocationManager.NETWORK_PROVIDER;
                        tryToLocation();
                        break;
                }
            }
        });

        findViewById(R.id.stop).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stop();
            }
        });

        findViewById(R.id.clear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                text.setText(null);
            }
        });

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        mPermissionHelper = new PermissionHelper(this);
    }

    private void tryToLocation() {
        print("正在尝试 " + provider + " provider 定位");

        if (!mPermissionHelper.hasPermissions(Manifest.permission.ACCESS_FINE_LOCATION)) {
            mPermissionHelper.requestPermissions(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    PERMISSION_ACCESS_LOCATION,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            showInterrupted();
                        }
                    });
        } else {
            doLocation();
        }
    }

    private void showInterrupted() {
        print("已取消");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    doLocation();
                } else {
                    goError("请务必授予 " + provider + " provider 权限");

                    if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                        mPermissionHelper.showMissingPermissionDialog();
                    }

                    break;
                }
            }
        }
    }

    private void doLocation() {
        LocationProvider locationProvider = locationManager.getProvider(provider);

        if (!locationManager.isProviderEnabled(provider)) {
            startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            goError("请打开 " + provider + " provider, 无法定位");
            return;
        }

        if (locationProvider != null) {
            // noinspection MissingPermission
            locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, MainActivity.this);
        } else {
            goError("初始化 " + provider + " provider 失败");
        }
    }

    private void goError(String errorMsg) {
        print(errorMsg);
        Toast.makeText(MainActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
        showInterrupted();
    }

    private void print(String s) {
        String now = df.format(new Date());

        text.setText(text.getText() + "\n" + now + " ==== " + s);
    }

    private List<Address> getLocationList(double latitude, double longitude) {
        Geocoder gc = new Geocoder(this, Locale.getDefault());

        List<Address> locationList = null;
        try {
            locationList = gc.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return locationList;
    }

    @Override
    public void onLocationChanged(Location location) {
        print("onLocationChanged, location = " + location);

        double latitude = location.getLatitude();
        print("纬度 = " + latitude);

        double longitude = location.getLongitude();
        print("经度 = " + longitude);

        if (!showMore.isChecked()) {
            return;
        }

        List<Address> locationList = getLocationList(latitude, longitude);
        if (locationList != null && !locationList.isEmpty()) {
            print("locationList = " + locationList);

            Address address = locationList.get(0);
            print("address = " + address);

            String countryName = address.getCountryName();
            print("countryName = " + countryName);

            String countryCode = address.getCountryCode();
            print("countryCode = " + countryCode);

            String locality = address.getLocality();
            print("locality = " + locality);

            for (int i = 0; address.getAddressLine(i) != null; i++) {
                String addressLine = address.getAddressLine(i);
                print("addressLine = " + addressLine);
            }
        } else {
            print("未找到地理信息, 可能没有网络");
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        print("onStatusChanged = " + provider + ", status = " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        print("onProviderEnabled = " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        print("onProviderDisabled = " + provider);
    }

    private void stop() {
        print("已停止定位");

        // noinspection MissingPermission
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stop();
    }
}
