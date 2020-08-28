package id.ac.divisirobotikaunikom.nakulasadewa.BLE;

import java.util.HashMap;

public class SampleGattAttributes {
    //=======================================Variable===============================================
    //HashMap
    private static HashMap<String, String> attributes = new HashMap();

    //String
    public static String HEART_RATE_MEASUREMENT = "00002a37-0000-1000-8000-00805f9b34fb";
    public static String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    public static String SERVER_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";
    //=====================================End_Variable=============================================

    //========================================Lookup================================================
    public static String lookup(String uuid, String defaultName) {
        String name = attributes.get(uuid);
        return name == null ? defaultName : name;
    }
    //======================================End_Lookup==============================================

}
