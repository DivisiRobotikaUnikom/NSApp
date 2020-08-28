package id.ac.divisirobotikaunikom.nakulasadewa.Bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.UUID;

import id.ac.divisirobotikaunikom.nakulasadewa.InterfaceToNinebot;

public class Bluetooth {
    public static final String TAG = "BLUETOOTH";

    private Context context;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mBluetoothSocket;
    private String address;
    private UUID MY_UUID;
    private OutputStream outputStream = null;
    private InputStream inputStream = null;
    private Thread receiveThread;
    private boolean receiveRunning = false;
    float pwm_maju, pwm_belok;
    //Ninebot
    private float kecepatan = 0;
    private int inc_rem = 50;
    private boolean isTrackRecording = false;
    private boolean isAuto = false;

    private InterfaceToNinebot interfaceToNinebot;


    public Bluetooth(Context context, BluetoothAdapter bluetoothAdapter, String addr, UUID uuid, InterfaceToNinebot interfaceToNinebot){
        this.context = context;
        this.mBluetoothAdapter = bluetoothAdapter;
        this.interfaceToNinebot = interfaceToNinebot;
        address = addr;
        MY_UUID = uuid;

        pwm_maju = getPwm("pwm_maju");
        pwm_belok = getPwm("pwm_belok");
    }

    public void onResume(){
        if (mBluetoothAdapter.isEnabled()) {
            try {
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                try {
                    mBluetoothSocket = device.createInsecureRfcommSocketToServiceRecord(MY_UUID);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                mBluetoothAdapter.cancelDiscovery();
                try {
                    mBluetoothSocket.connect();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    inputStream = mBluetoothSocket.getInputStream();
                    outputStream = mBluetoothSocket.getOutputStream();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }catch (Exception e){
                Toast.makeText(context, "Pastikan address bluetooth mikro benar!", Toast.LENGTH_LONG).show();
            }

            sendData("A");
            receiveData();
        }
    }

    public void onStop(){
        receiveRunning = false;
        try {
            if(outputStream != null){
                outputStream.flush();
            }
            if(inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if(mBluetoothSocket != null) {
                mBluetoothSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean isPause = false;
//    private boolean isSaveOnPause = false;

    //Terima data bluetooth
    public void receiveData() {
        if (!receiveRunning) {
            receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    receiveRunning = true;
                    int bytes;
                    String data = "";
                    int x = 49, y = 49, z = 100, v = 100;
                    long elapse = 0;
                    long start;
                    long delay = 0;
                    long delay_track = 0;
                    String dataTrack = "";
                    // Keep looping to listen for received messages
                    while (receiveRunning) {
                        start = System.currentTimeMillis();
                        try {
//                            bytes = inputStream.read(buffer);            //read bytes from input buffer (bytes = inputStream.read(buffer); )
//                            String readMessage = new String(buffer, 0, bytes);
                            if(inputStream == null) {
                                continue;
                            }
                                bytes = inputStream.read();

                            char chr = (char) bytes;
                            if (chr == 110) { //ASCII 110 (n)
                                try {
                                    if(data.length() != 4){
                                        data = "";
                                        return;
                                    }
                                    if (isTrackRecording) {
                                        dataTrack += data + "x" + delay_track + "#\n";
                                    }
                                    char[] parts = data.toCharArray();
                                    x = parts[0];
                                    y = parts[1];
                                    z = parts[2];
                                    v = parts[3];
                                    Log.d(TAG, "X : "+x+" Y : "+y+" Z : "+z);
//                                    Log.d(STR_BLUETOOTH, kalkulasi(x, y, z));
                                    delay_track = 0;
                                    elapse = 0;
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                data = "";
                            } else if (chr == 111) { //ASCII 111 (o)
                                if (!isAuto) {
                                    isTrackRecording = true;
                                }
//                                else {
//                                    isPause = false;
//                                    isSaveOnPause = true;
//                                }
                            } else if (chr == 112) { //ASCII 112 (p)
                                if (isTrackRecording) {
                                    isTrackRecording = false;
                                    writeToFile(dataTrack, "pwm.txt", context);
                                    dataTrack = "";
                                    delay_track = 0;
                                }
                                else if(isAuto){
                                    isPause = true;
                                }
                            } else if (chr == 113) { //ASCII 113 (q)
                                isPause = false;
                                if (!isAuto && !isTrackRecording) {
                                    isAuto = true;
                                    String read = readFromFile("pwm.txt", context);
//                                    Log.d(TAG, read);
                                    final String track[] = read.split("#");
                                    final int length = track.length;
                                    new Thread(new Runnable() {
                                        @Override
                                        public void run() {
                                            for (int i = 0; i < length; i++) {
                                                try {
                                                    if (!isAuto) {
//                                                    Log.d(TAG, "STOP");
                                                        break;
                                                    }

                                                    if(isPause){
                                                        interfaceToNinebot.setDataBluetooth(kalkulasi(50, 50, 100, 100));
                                                        Thread.sleep(60);
                                                        i--;
                                                        continue;
                                                    }
//
//                                                    if(isSaveOnPause){
//                                                        String data = "";
//                                                        for(int j = i; j < length;j++){
//                                                            data += track[j] + "#";
//                                                        }
//                                                        writeToFile(data, "pwm.txt", context);
//                                                        isSaveOnPause = false;
//                                                        break;
//                                                    }

                                                    String data[] = track[i].split("x");
                                                    char[] parts = data[0].toCharArray();
                                                    int x = parts[0];
                                                    int y = parts[1];
                                                    int z = parts[2];
                                                    int v = parts[3];
//                                                Log.d(TAG, data[0]);

                                                    int delay = Integer.parseInt(data[1]);
                                                    if (delay <= 0) {
                                                        delay = 1;
                                                    }
//                                                Log.d(TAG, data[1]);

                                                    interfaceToNinebot.setDataBluetooth(kalkulasi(x, y, z, v));

                                                    Thread.sleep(delay);
                                                } catch (InterruptedException e) {
                                                    e.printStackTrace();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                            isAuto = false;
                                        }
                                    }).start();
                                }
                            } else if (chr == 114) { //ASCII 114 (r)
//                                isSaveOnPause = false;
                                isPause = false;
                                isAuto = false;
                                interfaceToNinebot.setDataBluetooth(kalkulasi(50, 50, 100, 100));
                            } else {
                                data += chr;
                            }
                        } catch (IOException e) {

                        }
                        interfaceToNinebot.setDataBluetooth(kalkulasi(x, y, z, v));

                        if (isTrackRecording) {
                            delay_track += System.currentTimeMillis() - start;
                        }

                        elapse += System.currentTimeMillis() - start;
                        if (elapse > 1000) {
                            delay += System.currentTimeMillis() - start;
                            if (delay > 30) {
                                if (x < 49) {
                                    x++;
                                } else if (x > 49) {
                                    x--;
                                }

                                if (y < 49) {
                                    y++;
                                } else if (y > 49) {
                                    y--;
                                }

                                if (z < 100) {
                                    z++;
                                }

                                if (x == 49 && y == 49 && z == 100) {
                                    elapse = 0;
                                }
                                delay = 0;
                            }
                        }
                    }
                }
            });
            receiveThread.start();
        }
    }

    //Ngirim data bluetooth
    public void sendData(String message) {
        message = message + "\r";
        byte[] msgBuffer = message.getBytes();
        try {
            if(outputStream != null) {
                outputStream.write(msgBuffer);
            }
        } catch (IOException e) {
        }
    }

    public float pwm(int v, int z) {
        return (float) ((z / 2) + (v * (Math.pow(v, 2) / (2 * v))) / 9);
    }

    public String sumbu_y(int y, float pwm) {
        if (y < 41) {
            kecepatan = (float) (Math.abs(y - 49) * 32767 / (pwm * pwm_maju));
            if (kecepatan >= 32767) {
                kecepatan = 32767;
            }
        } else if (y > 59) {
            kecepatan = (float) (65535 - (Math.abs(y - 49) * 32767 / (pwm * (pwm_maju + 0.25))));
            if (kecepatan <= 32768) {
                kecepatan = 32768;
            }
        } else {
            if (kecepatan > 0 && kecepatan <= 32767) {
                kecepatan -= inc_rem;
                if (kecepatan <= 0) {
                    kecepatan = 0;
                }
            } else if (kecepatan >= 32768 && kecepatan <= 65535) {
                kecepatan += inc_rem;
                if (kecepatan >= 65535) {
                    kecepatan = 0;
                }
            } else {
                kecepatan = 0;
            }
        }
        return String.format("%04X", Math.round(kecepatan));
    }

    public String sumbu_x(int x, float pwm) {
        float temp;
        if (x < 41) {
            temp = (float) (Math.abs(x - 49) * 32767 / (pwm * pwm_belok));
            if (temp >= 3034) {
                temp = 3034;
            }
        } else if (x > 59) {
            temp = (float) (65535 - (Math.abs(x - 49) * 32767 / (pwm * pwm_belok)));
            if (temp <= 62051) {
                temp = 62051;
            }
        } else {
            temp = 0;
        }
        return String.format("%04X", Math.round(temp));
    }

    public String kalkulasi(int x, int y, int z, int v) {
        float pwm = pwm(v, z);
        if (pwm < 49) {
            pwm = 49;
        }
        String initY = sumbu_y(y, pwm);
        String initX = sumbu_x(x, pwm);

        initY = initY.substring(initY.length() - 4);
        initY = initY.substring(initY.length() - 2) + initY.substring(0, 2);
        initX = initX.substring(initX.length() - 4);
        initX = initX.substring(initX.length() - 2) + initX.substring(0, 2);
        String temp = initY + initX;

        String b1hex = "0A", b2hex = "03", chex = "7B";
        String lenhex, cslohex, cshihex, dat;
        int b1int = 10, b2int = 3, cint = 123, datint = 0, len = 0, csint, cslo, cshi, i = 0;

        while (i < temp.length()) {
            dat = temp.substring(i, i + 2);
            datint = datint + Integer.valueOf(dat, 16);
            i = i + 2;
        }
        len = (temp.length() / 2) + 2;
        csint = len + b1int + b2int + cint + datint;
        csint = ~csint;
        cslo = csint & 0xFF;
        cshi = (csint >> 8) & 0xFF;
        cslohex = String.format("%02X", cslo);
        cshihex = String.format("%02X", cshi);
        lenhex = String.format("%02X", len);
        //print string
        String kirim = "55AA" + lenhex + b1hex + b2hex + chex + temp + cslohex + cshihex;
        return kirim;
    }

    public void writeToFile(String data, String name, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(name, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        } catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public String readFromFile(String name, Context context) {

        String ret = "";

        try {
            InputStream inputStream = context.openFileInput(name);

            if (inputStream != null) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = bufferedReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }

                inputStream.close();
                ret = stringBuilder.toString();
            }
        } catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }
        return ret;
    }

    public float getPwm(String s) {
        SharedPreferences settings = context.getSharedPreferences("prefs", 0); // 0 mode private
        return settings.getFloat(s, 1.5f);
    }

    public void setPwm(float m, float b) {
        SharedPreferences settings = context.getSharedPreferences("prefs", 0); //0 mode private
        SharedPreferences.Editor editor = settings.edit();
        editor.putFloat("pwm_maju", m);
        editor.putFloat("pwm_belok", b);
        editor.commit();
    }

    public float getPwm_maju(){
        return pwm_maju;
    }

    public float getPwm_belok(){
        return pwm_belok;
    }

    public void setPwm_maju(float pwm_maju) {
        this.pwm_maju = pwm_maju;
    }

    public void setPwm_belok(float pwm_belok){
        this.pwm_belok = pwm_belok;
    }
}
