package id.ac.divisirobotikaunikom.nakulasadewa;

import android.app.Activity;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.view.Display;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import id.ac.divisirobotikaunikom.nakulasadewa.BLE.BluetoothLeService;
import id.ac.divisirobotikaunikom.nakulasadewa.BLE.SampleGattAttributes;
import id.ac.divisirobotikaunikom.nakulasadewa.Bluetooth.Bluetooth;
import id.ac.divisirobotikaunikom.nakulasadewa.Display.Background;
import id.ac.divisirobotikaunikom.nakulasadewa.Display.MainThread;
import id.ac.divisirobotikaunikom.nakulasadewa.Display.Mata;
import id.ac.divisirobotikaunikom.nakulasadewa.Display.Mulut;
import id.ac.divisirobotikaunikom.nakulasadewa.TTS.TTS;
import id.ac.divisirobotikaunikom.nakulasadewa.Wifi.ServerUdp;

public class MainActivity extends Activity implements View.OnTouchListener, InterfaceToNinebot, View.OnClickListener {
    public static final String TAG = "MAINACTIVITY";
    public static final String STR_BLUETOOTH = "BLUETOOTH";

    public static final String ADDRESS_NAKULA = "CD:3B:7F:02:2D:33";
    public static final String ADDRESS_SADEWA = "D1:33:4F:99:F4:A4";

    public static final int NONE = 0;
    public static final int WIFI = 1;
    public static final int BLUETOOTH = 2;

    //This Class
    public static MainActivity mainActivity;

    //SurfaceView
    private SurfaceView svWajah;

    //Context
//    public static Context context;

    //Robot Nakula
//    private final String ninebot = "CD:3B:7F:02:2D:33"; //Ninebot 1
//    Robot Sadewa
    private String ninebot = "D1:33:4F:99:F4:A4"; // Ninebot 2

    //BLE
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public static BluetoothLeService mBluetoothLeService;
    private final String LIST_NAME = "NAME";
    private final String LIST_UUID = "UUID";
    private ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
            new ArrayList<ArrayList<BluetoothGattCharacteristic>>();
    public static boolean startRequest = false;
    public static boolean mConnected = false;
    public static boolean bReady = false;
    private boolean mScanning;
    private boolean isRegister = false;
    private String mDeviceAddress;

    //Komunikasi
    public int comunitation = 0;
    public boolean isCanChangeCom = false;

    //Wifi
    private ServerUdp serverUdp;

    //Bluetooth Hc-05
    private Bluetooth bluetooth;
    private static final UUID MY_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private String address = "00:21:13:02:1D:06";

    //Request data ninebot
//    public final static int BATTERY = 0x22;
//    public final static int CURRENT_SPEED = 0x26;
//    public final static int TEMPERATURE = 0x3E;
//    public final static int VOLTAGE = 0x47;
    //    public final static int CURRENT = 0x50;
    //    public final static int PITCH_ANGLE = 0x61;
    //    public final static int ROLL_ANGLE = 0x62;
    //    public final static int PITCH_ANGLE_VELOCITY = 0x63;
    //    public final static int ROLL_ANGLE_VELOCITY = 0x64;
//    private float[] value = new float[10];
//    private int[] allRequest = {BATTERY, CURRENT_SPEED, TEMPERATURE, VOLTAGE};
    //            , CURRENT, PITCH_ANGLE
    //            , ROLL_ANGLE, PITCH_ANGLE_VELOCITY, ROLL_ANGLE_VELOCITY};

    //Display Wajah
    public static final int WIDTH = 1920;
    public static final int HEIGHT = 1200;
    public static Mata mata;
    public Mulut mulut;
    private Bitmap R_mata;
    private Bitmap R_mulut;
    private Bitmap R_comunication;
    private Background bg;
    private float scaleFactorX;
    private float scaleFactorY;
    public static int maxHeight;
    public static int maxWidth;
    public static int divWidth;
    public static int divHeight;

    //Button
    private Button b1, b2, b3;

    //Main Thread
    private MainThread thread;

    private Handler mHandler;

    //dialog
    private EditText etAddress;
    private Button bWifi, bBluetooth, bNone, bResetConnection, bSetRobot, bAddress, bResetBluetooth;
    private Dialog dialog;
    private SeekBar sbMaju, sbBelok;
    private Button bPwm;
    private TextView tvMaju, tvBelok;
    private ToggleButton tgRobot;


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

        setContentView(R.layout.activity_main);

        Intent backIntent = new Intent(this, RSSPullService.class);
        startService(backIntent);

        mHandler = new Handler();
        // Initializes a Bluetooth adapter.  For API level 18 and above, get a reference to
        // BluetoothAdapter through BluetoothManager.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        //get from database
        comunitation = getComunication();

        SharedPreferences settings = getSharedPreferences("prefs", 0); // 0 mode private
        ninebot = settings.getString("robot", ADDRESS_SADEWA);

        address = settings.getString("address", "00:21:13:02:1D:06");

        if (comunitation == WIFI) {
            serverUdp = new ServerUdp();
            serverUdp.setInterface(this);
        } else if (comunitation == BLUETOOTH) {
            bluetooth = new Bluetooth(this, mBluetoothAdapter, address, MY_UUID, this);
            bluetooth.onResume();
        }

        R_mata = BitmapFactory.decodeResource(getResources(), R.drawable.mata);
        R_mulut = BitmapFactory.decodeResource(getResources(), R.drawable.mulut);
        if (comunitation == BLUETOOTH) {
            R_comunication = BitmapFactory.decodeResource(getResources(), R.drawable.bluetooth);
        } else if (comunitation == WIFI) {
            R_comunication = BitmapFactory.decodeResource(getResources(), R.drawable.wifi);
        } else {
            R_comunication = BitmapFactory.decodeResource(getResources(), R.drawable.none);
        }

        svWajah = findViewById(R.id.svWajah);
        svWajah.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                bg = new Background();
                mata = new Mata(R_mata, R_mata.getWidth(), R_mata.getHeight() / 5, 5);
                mulut = new Mulut(R_mulut, R_mulut.getWidth(), R_mulut.getHeight());

                thread = new MainThread(svWajah.getHolder(), MainActivity.this);
                //we can safely start the loop
                thread.setRunning(true);
                thread.start();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                boolean retry = true;
                thread.setRunning(false);
                while (retry) {
                    try {
                        thread.join();
                        retry = false;
                    } catch (InterruptedException e) {

                    }
                }
                thread = null;
            }
        });
        svWajah.setOnTouchListener(this);

        dialog = new Dialog(this, R.style.dialog_notitle);
        dialog.setContentView(R.layout.dialog);

        b1 = findViewById(R.id.b1);
        b2 = findViewById(R.id.b2);
        b3 = findViewById(R.id.b3);
        b1.setOnClickListener(this);
        b2.setOnClickListener(this);
        b3.setOnClickListener(this);

        bWifi = dialog.findViewById(R.id.bWifi);
        bBluetooth = dialog.findViewById(R.id.bBlue);
        bNone = dialog.findViewById(R.id.bNone);
        bWifi.setOnClickListener(this);
        bBluetooth.setOnClickListener(this);
        bNone.setOnClickListener(this);

        bSetRobot = dialog.findViewById(R.id.bSetRobot);
        bSetRobot.setOnClickListener(this);
        bResetConnection = dialog.findViewById(R.id.bResetConnection);
        bResetConnection.setOnClickListener(this);
        bAddress = dialog.findViewById(R.id.bAddress);
        bAddress.setOnClickListener(this);
        bResetBluetooth = dialog.findViewById(R.id.bResetBluetooth);
        bResetBluetooth.setOnClickListener(this);

        etAddress = dialog.findViewById(R.id.etAddress);
        etAddress.setHint(address);
        tvMaju = dialog.findViewById(R.id.tvMaju);
        tvBelok = dialog.findViewById(R.id.tvBelok);
        sbMaju = dialog.findViewById(R.id.sbMaju);
        sbBelok = dialog.findViewById(R.id.sbBelok);

        tgRobot = dialog.findViewById(R.id.tgRobot);
        if (ninebot.equals(ADDRESS_NAKULA)) {
            tgRobot.setChecked(true);
        }
        tgRobot.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    ninebot = ADDRESS_NAKULA;
                } else {
                    ninebot = ADDRESS_SADEWA;
                }
            }
        });

        try {
            int progres_maju = 201 - (int) (bluetooth.getPwm_maju() * 100);
            int progres_belok = 201 - (int) (bluetooth.getPwm_belok() * 100);
//        Log.d(TAG, "Progres : "+progres_maju+" "+progres_belok);
            sbMaju.setProgress(progres_maju);
            sbBelok.setProgress(progres_belok);
            tvMaju.setText("" + progres_maju);
            tvBelok.setText("" + progres_belok);
        } catch (Exception e) {

        }
        sbMaju.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                bluetooth.setPwm_maju(((201 - i) * 1f) / 100f);
                tvMaju.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        sbBelok.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                bluetooth.setPwm_belok(((201 - i) * 1f) / 100f);
                tvBelok.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        bPwm = dialog.findViewById(R.id.bPwm);
        bPwm.setOnClickListener(this);

        Display d = getWindowManager().getDefaultDisplay();
        maxHeight = d.getHeight();
        maxWidth = d.getWidth();
        maxWidth = d.getWidth();
        divWidth = maxWidth / 14;
        divHeight = maxHeight / 8;

        mainActivity = this;
        TTS.getInstance(MainActivity.this).speak("Divisi Robotika Unikom");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (comunitation != NONE) {
            scanLeDevice(true);
        }
        if (comunitation == WIFI) {
            serverUdp.onResume();
        } else if (comunitation == BLUETOOTH) {
//            bluetooth.onResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (comunitation != NONE) {
            scanLeDevice(false);
        }

        if (comunitation == BLUETOOTH) {
//            bluetooth.onStop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (comunitation == WIFI) {
            serverUdp.onDestroy();
        }
        if (comunitation != NONE) {
            if (isRegister) {
                bReady = false;
                unregisterReceiver(mGattUpdateReceiver);
                unbindService(mServiceConnection);
            }
            mBluetoothLeService = null;
        }
        Intent backIntent = new Intent(this, RSSPullService.class);
        stopService(backIntent);
    }

    Runnable setCharacteristic = new Runnable() {
        @Override
        public void run() {
            /*check if the service is available on the device*/
            BluetoothGattService mCustomService = BluetoothLeService.mBluetoothGatt
                    .getService(UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e"));

            /*get the read characteristic from the service*/
            BluetoothGattCharacteristic characteristic = mCustomService
                    .getCharacteristic(UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e"));
            final int charaProp = characteristic.getProperties();
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
                // If there is an active notification on a characteristic, clear
                // it first so it doesn't update the data field on the user interface.
                if (mNotifyCharacteristic != null) {
                    mBluetoothLeService.setCharacteristicNotification(
                            mNotifyCharacteristic, false);
                    mNotifyCharacteristic = null;
                }
                mBluetoothLeService.readCharacteristic(characteristic);
            }
            if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
                mNotifyCharacteristic = characteristic;
                mBluetoothLeService.setCharacteristicNotification(
                        characteristic, true);
            }
            mBluetoothLeService.setCharacteristicNotification(
                    characteristic, true);
            mBluetoothLeService.readCharacteristic(characteristic);
            //kirim perintah bluetooth ready
            bReady = true;
//            client.setData("client", "ON");
            mHandler.postDelayed(modeRide, 1000);
        }
    };

    Runnable modeRide = new Runnable() {
        @Override
        public void run() {
            for (int i = 0; i < 3; i++) {
                mBluetoothLeService.writeCharacteristic("55AA040A037A010073FF");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(comunitation == BLUETOOTH) {
                bluetooth.sendData("C");
            }
            startRequest = true;
            Toast.makeText(MainActivity.this, "Mode control done", Toast.LENGTH_LONG).show();
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (device.getAddress().equals(ninebot)) {
                                if (mScanning) {
                                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                                    mScanning = false;
                                }
                                //input code to control
                                mDeviceAddress = device.getAddress();
                                Intent gattServiceIntent = new Intent(MainActivity.this, BluetoothLeService.class);
                                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
                                isRegister = true;
                            }
                        }
                    });
                }
            };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                finish();
            }
            // Automatically connects to the device upon successful start-up initialization.
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
            mBluetoothLeService.disconnect();
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                // Show all the supported services and characteristics on the user interface.
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
                //System.out.println("Discovered Sucsess");
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
//                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
//                displayNotif(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;
        String unknownServiceString = getResources().getString(R.string.unknown_service);
        String unknownCharaString = getResources().getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData = new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        mGattCharacteristics = new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData = new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(LIST_NAME, SampleGattAttributes.lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();

            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData = new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid, unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
            mHandler.postDelayed(setCharacteristic, 1000);
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            mScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
    }

    //TODO: lakukan jika ada sentuhan
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        setButtonEnable(b1, true);
        setButtonEnable(b2, false);
        setButtonEnable(b3, false);
        isCanChangeCom = false;
        return false;
    }

    public void draw(Canvas canvas) {
        scaleFactorX = canvas.getWidth() / (WIDTH * 1.f);
        scaleFactorY = canvas.getHeight() / (HEIGHT * 1.f);
        if (canvas != null) {
            final int saveState = canvas.save();
            canvas.scale(scaleFactorX, scaleFactorY);

            //Background
            bg.draw(canvas);

            canvas.drawBitmap(R_comunication, 5, 5, null);

            //Mata
            mata.draw(canvas);

            //Mulut
            mulut.draw(canvas);

            canvas.restoreToCount(saveState);
        }
    }

    public void update() {
    }

    @Override
    public void setDataBluetooth(String s) {
        if (mConnected && bReady) {
            mBluetoothLeService.writeCharacteristic(s);
        }
    }

    public void restartAplication() {
        Toast.makeText(MainActivity.this, "Aplikasi akan di restart", Toast.LENGTH_SHORT).show();

        Intent i = getBaseContext().getPackageManager().
                getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    public int getComunication() {
        SharedPreferences settings = getSharedPreferences("prefs", 0); // 0 mode private
        return settings.getInt("com", 0);
    }

    public void setComunication(int i) {
        SharedPreferences settings = getSharedPreferences("prefs", 0); //0 mode private
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt("com", i);
        editor.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.b1:
                if (isCanChangeCom) {
                    dialog.show();
                    isCanChangeCom = false;
                    TTS.getInstance(MainActivity.this).speak(".");
                } else {
                    setButtonEnable(b2, true);
                    setButtonEnable(b1, false);
                    TTS.getInstance(MainActivity.this).speak(".");
                }
                break;
            case R.id.b2:
                setButtonEnable(b3, true);
                setButtonEnable(b2, false);
                TTS.getInstance(MainActivity.this).speak(".");
                break;
            case R.id.b3:
                setButtonEnable(b1, true);
                setButtonEnable(b3, false);
                isCanChangeCom = true;
                TTS.getInstance(MainActivity.this).speak(".");
                break;
            case R.id.bWifi:
                if (comunitation == WIFI) {
                    dialog.dismiss();
                    setButtonEnable(b1, true);
                    setButtonEnable(b2, false);
                    setButtonEnable(b3, false);
                    isCanChangeCom = false;
                } else {
                    setComunication(WIFI);
                    restartAplication();
                }
                break;
            case R.id.bBlue:
                if (comunitation == BLUETOOTH) {
                    dialog.dismiss();
                    setButtonEnable(b1, true);
                    setButtonEnable(b2, false);
                    setButtonEnable(b3, false);
                    isCanChangeCom = false;
                } else {
                    setComunication(BLUETOOTH);
                    restartAplication();
                }
                break;
            case R.id.bNone:
                if (comunitation == NONE) {
                    dialog.dismiss();
                    setButtonEnable(b1, true);
                    setButtonEnable(b2, false);
                    setButtonEnable(b3, false);
                    isCanChangeCom = false;
                } else {
                    setComunication(NONE);
                    restartAplication();
                }
                break;
            case R.id.bPwm:
                bluetooth.setPwm(bluetooth.getPwm_maju(), bluetooth.getPwm_belok());
                Toast.makeText(MainActivity.this, "PWM berhasil di save", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                break;
            case R.id.bResetConnection:
                if (comunitation != NONE) {
                    startRequest = false;
                    mConnected = false;
                    mScanning = false;
                    scanLeDevice(false);
                    if (isRegister) {
                        bReady = false;
                        isRegister = false;
                        unregisterReceiver(mGattUpdateReceiver);
                        unbindService(mServiceConnection);
                    }
                    mBluetoothLeService = null;
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    scanLeDevice(true);
                }
                dialog.dismiss();
                break;
            case R.id.bSetRobot:
            case R.id.bAddress:
                SharedPreferences settings = getSharedPreferences("prefs", 0); //0 mode private
                SharedPreferences.Editor editor = settings.edit();
                editor.putString("robot", ninebot);
                address = etAddress.getText().toString();
                if (!address.isEmpty()) {
                    editor.putString("address", address);
                }
                Toast.makeText(MainActivity.this, address, Toast.LENGTH_SHORT).show();
                editor.apply();

                restartAplication();
                break;
            case R.id.bResetBluetooth:
                bluetooth.onStop();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
//                bluetooth.onResume();
                dialog.dismiss();
                break;
        }
    }

    public void setButtonEnable(Button button, boolean b) {
        button.setEnabled(b);
    }

//    public void setButtonVisible(Button button, int i) {
//        button.setVisibility(i);
//    }
}
