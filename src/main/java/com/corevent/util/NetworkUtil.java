package com.corevent.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkUtil {
    public static boolean isOnline() {
        try {
            InetAddress address = InetAddress.getByName("8.8.8.8");
            return address.isReachable(3000);
        } catch (UnknownHostException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }
} 