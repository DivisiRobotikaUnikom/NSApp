package id.ac.divisirobotikaunikom.nakulasadewa.Wifi;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import id.ac.divisirobotikaunikom.nakulasadewa.InterfaceToNinebot;

public class ServerUdp extends Thread {
    public static final String TAG = "WIFI";

    private DatagramSocket serverSocket;
    private InterfaceToNinebot interfaceToNinebot = null;
    private boolean running;
    private String data;

    public ServerUdp() {
        //do nothing
        running = true;
        start();
    }

    public void setInterface(InterfaceToNinebot i) {
        interfaceToNinebot = i;
    }

    @Override
    public void run() {
        super.run();
        try {
            serverSocket = new DatagramSocket(Komunikasi.portServer);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        byte[] receiveData = new byte[64];
        while (running) {
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                serverSocket.receive(receivePacket);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String sentence = new String(receivePacket.getData());
            data = sentence.substring(receivePacket.getOffset(), receivePacket.getLength());
//            Log.d(TAG, data);
            interfaceToNinebot.setDataBluetooth(data);
        }
    }

    public void onResume() {
    }

    public void onDestroy() {
        running = false;
        serverSocket.close();
        try {
            join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
