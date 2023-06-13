package com.nekiak.loader.authentication;

import org.apache.commons.codec.digest.DigestUtils;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

public class HWID {
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