package com.nekiak.loader.authentication;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class HWID {
    public static String getSystemInfo() {
        return DigestUtils.sha256Hex(System.getenv("os")
                + System.getProperty("os.arch")
                + System.getProperty("user.name")
                + System.getenv("SystemRoot")
                + System.getenv("HOMEDRIVE")
                + System.getenv("PROCESSOR_LEVEL")
                + System.getenv("PROCESSOR_REVISION")
                + System.getenv("PROCESSOR_IDENTIFIER")
                + System.getenv("PROCESSOR_ARCHITECTURE")
                + System.getenv("PROCESSOR_ARCHITEW6432")
                + System.getenv("NUMBER_OF_PROCESSORS")
                + getMacAddress()
        );
    }




    public static String getMacAddress() {
        StringBuilder macAddressString = new StringBuilder();

        try {
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaces.nextElement();
                byte[] macAddress = networkInterface.getHardwareAddress();
                if (macAddress != null) {
                    for (byte b : macAddress) {
                        macAddressString.append(String.format("%02X:", b));
                    }
                    if (macAddressString.length() > 0) {
                        macAddressString.deleteCharAt(macAddressString.length() - 1);
                    }
                    break; // Exit the loop after obtaining the first MAC address
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }

        return macAddressString.toString();
    }
}