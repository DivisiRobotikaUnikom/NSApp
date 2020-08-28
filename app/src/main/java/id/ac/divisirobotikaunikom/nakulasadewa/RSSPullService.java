package id.ac.divisirobotikaunikom.nakulasadewa;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
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
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.view.SurfaceView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import id.ac.divisirobotikaunikom.nakulasadewa.BLE.BluetoothLeService;
import id.ac.divisirobotikaunikom.nakulasadewa.BLE.SampleGattAttributes;
import id.ac.divisirobotikaunikom.nakulasadewa.Bluetooth.Bluetooth;
import id.ac.divisirobotikaunikom.nakulasadewa.Display.MainThread;
import id.ac.divisirobotikaunikom.nakulasadewa.Wifi.ServerUdp;

public class RSSPullService extends Service implements InterfaceToNinebot {

    MediaPlayer myPlayer;

    public static final String TAG = "MAINACTIVITY";
    public static final String STR_BLUETOOTH = "BLUETOOTH";

    public static final String ADDRESS_NAKULA = "CD:3B:7F:02:2D:33";
    public static final String ADDRESS_SADEWA = "D1:33:4F:99:F4:A4";

    public static final int NONE = 0;
    public static final int WIFI = 1;
    public static final int BLUETOOTH = 2;

    //This Class
    public static RSSPullService rssPullService;

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

    //Main Thread
    private MainThread thread;

    private Handler mHandler;



    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "Service Successfully", Toast.LENGTH_LONG).show();
        
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
        bluetooth = new Bluetooth(this, mBluetoothAdapter, address, MY_UUID, this);
//        if (comunitation == WIFI) {
//            serverUdp = new ServerUdp();
//            serverUdp.setInterface(this);
//        } else if (comunitation == BLUETOOTH) {
//
//        }

        try {
            int progres_maju = 201 - (int) (bluetooth.getPwm_maju() * 100);
            int progres_belok = 201 - (int) (bluetooth.getPwm_belok() * 100);
        } catch (Exception e) {

        }

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
        }
    };

//    private BluetoothAdapter.LeScanCallback mLeScanCallback =
//            new BluetoothAdapter.LeScanCallback() {
//                @Override
//                public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (device.getAddress().equals(ninebot)) {
//                                if (mScanning) {
//                                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
//                                    mScanning = false;
//                                }
//                                //input code to control
//                                mDeviceAddress = device.getAddress();
//                                Intent gattServiceIntent = new Intent(RSSPullService.this, BluetoothLeService.class);
//                                bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//                                registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
//                                isRegister = true;
//                            }
//                        }
//                    });
//                }
//            };

    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
//                finish();
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

//    private void scanLeDevice(final boolean enable) {
//        if (enable) {
//            mScanning = true;
//            mBluetoothAdapter.startLeScan(mLeScanCallback);
//        } else {
//            mScanning = false;
//            mBluetoothAdapter.stopLeScan(mLeScanCallback);
//        }
//    }

    @Override
    public void setDataBluetooth(String s) {
        if (mConnected && bReady) {
            mBluetoothLeService.writeCharacteristic(s);
        }
    }

    public int getComunication() {
        SharedPreferences settings = getSharedPreferences("prefs", 0); // 0 mode private
//        return settings.getInt("com", 0);
        return 2;
    }

    @Override
    public void onStart(Intent intent, int startid) {
        Toast.makeText(this, "Service Started", Toast.LENGTH_LONG).show();
        bluetooth.onResume();
    }
    @Override
    public void onDestroy() {
        Toast.makeText(this, "Service Stopped", Toast.LENGTH_LONG).show();
        stopSelf();
    }

//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        bluetooth.onResume();
//        return START_STICKY;
//    }
}
