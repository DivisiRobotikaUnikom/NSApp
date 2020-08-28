package id.ac.divisirobotikaunikom.nakulasadewa.Not_Yet_Used;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import id.ac.divisirobotikaunikom.nakulasadewa.Wifi.Komunikasi;
import id.ac.divisirobotikaunikom.nakulasadewa.R;

public class Splash extends Activity {

    //BluetoothAdapter
    private BluetoothAdapter mBluetoothAdapter;

    private static final int REQUEST_ENABLE_BT = 1;
    public static int OVERLAY_PERMISSION_REQ_CODE = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN //fulscreen
                        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON //tetap nyala
                        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                        | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        setContentView(R.layout.splash);

        //Check Fitur BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Fitur Bluetooth Low Energy tidak ada", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth tidak support", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                        Uri.parse("package:" + getPackageName()));
//                startActivityForResult(intent, OVERLAY_PERMISSION_REQ_CODE);
//            }
//        }

        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            startIntent();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                startIntent();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(this, "Bluetooth Harus Aktif", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
//        if (requestCode == OVERLAY_PERMISSION_REQ_CODE) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (!Settings.canDrawOverlays(this)) {
//                    // SYSTEM_ALERT_WINDOW permission not granted...
//                }
//            }
//        }
    }

    public void startIntent() {
        SharedPreferences settings = getSharedPreferences("prefs", 0); // 0 mode private
        boolean firstRun = settings.getBoolean("run", true);
        Komunikasi.ipServer = settings.getString("ip", "192.168.0.99"); // jika tidak ada ip server 192.168.0.99
        Log.d("Check Boolean", "onCreate: " + firstRun);
        if (firstRun) {
            // here run your first-time instructions, for example :
            Intent in = new Intent("android.intent.action.SETTING");
            startActivity(in);
        } else {
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //Do some stuff
//            final MediaPlayer sound = MediaPlayer.create(Splash.this, R.raw.blip);
                Thread timer = new Thread() {
                    public void run() {
                        try {
                            sleep(2000);
//                        sound.start();
                            sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        } finally {
//                        sound.release();
                            Intent openMain = new Intent("android.intent.action.MAINACTIVITY");
                            startActivity(openMain);
                            finish();
                        }
                    }
                };
                timer.start();
            }
            finish();
        }
    }
}
