package com.zenflow.service;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Very small in-process event bus to notify controllers about config changes.
 * This avoids tightly coupling controllers to each other.
 */
public class AppEventBus {

    public interface Listener {
        void onConfigChanged(String key, String newValue);
    }

    private static final List<Listener> listeners = new CopyOnWriteArrayList<>();

    private AppEventBus() {
    }

    public static void subscribe(Listener l) {
        if (l != null) listeners.add(l);
    }

    public static void unsubscribe(Listener l) {
        if (l != null) listeners.remove(l);
    }

    public static void publishConfigChanged(String key, String newValue) {
        for (Listener l : listeners) {
            try {
                l.onConfigChanged(key, newValue);
            } catch (Exception ignored) {
            }
        }
    }
}

