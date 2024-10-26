package com.sundar.devtech.Services;

import android.util.Log;

public class ByteConvertor {

    // Convert hex string to byte array
    public static byte[] hexStringToByteArr(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    public static byte[] hexStringToByteArray(String s) {
        if (s == null || s.length() % 2 != 0) {
            Log.e("ByteConvertor", "Invalid hex string: Length should be even.");
            return new byte[0];
        }

        int len = s.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            int firstDigit = Character.digit(s.charAt(i), 16);
            int secondDigit = Character.digit(s.charAt(i + 1), 16);

            if (firstDigit == -1 || secondDigit == -1) {
                throw new IllegalArgumentException("Invalid hex character in string at position: " + i);
            }

            data[i / 2] = (byte) ((firstDigit << 4) + secondDigit);
        }
        return data;
    }



    // Helper method to convert byte array to hex string for display
    public static String byteArrayToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
