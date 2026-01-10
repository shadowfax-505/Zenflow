package com.zenflow.service;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UserService {
    private static final String STORE = System.getProperty("user.home") + File.separator + ".zenflow_users.properties";
    private final Map<String, String> users = new ConcurrentHashMap<>();
    private final Set<String> admins = Collections.synchronizedSet(new HashSet<>());
    private static final UserService INSTANCE = new UserService();

    private UserService() {
        load();
        users.putIfAbsent("admin", "admin");
        admins.add("admin");

        users.putIfAbsent("rahman669", "1234");
        users.putIfAbsent("shadowfax", "0987");
        admins.add("shadowfax");

        save();
    }

    public static UserService getInstance() {
        return INSTANCE;
    }

    public boolean authenticate(String username, String password) {
        return username != null && password != null && password.equals(users.get(username));
    }

    public boolean addUser(String username, String password) {
        if (username == null || username.isBlank() || password == null) return false;
        if (users.putIfAbsent(username, password) == null) {
            save();
            return true;
        }
        return false;
    }

    public boolean changePassword(String username, String newPassword) {
        if (!users.containsKey(username) || newPassword == null) return false;
        users.put(username, newPassword);
        save();
        return true;
    }

    public List<String> listUsers() {
        List<String> list = new ArrayList<>(users.keySet());
        Collections.sort(list);
        return list;
    }

    public boolean isAdmin(String username) {
        return username != null && admins.contains(username);
    }


    public boolean setAdmin(String username, boolean makeAdmin) {
        if (username == null || !users.containsKey(username)) return false;
        if (makeAdmin) admins.add(username); else admins.remove(username);
        save();
        return true;
    }


    public boolean removeUser(String username) {
        if (username == null || !users.containsKey(username)) return false;
        users.remove(username);
        admins.remove(username);
        save();
        return true;
    }

    private synchronized void save() {
        Properties p = new Properties();
        p.putAll(users);
        p.setProperty("__admins", String.join(",", admins));
        try (OutputStream out = Files.newOutputStream(Paths.get(STORE))) {
            p.store(out, "ZenFlow users and admins");
        } catch (IOException ignored) { }
    }

    private synchronized void load() {
        Path path = Paths.get(STORE);
        if (Files.exists(path)) {
            try (InputStream in = Files.newInputStream(path)) {
                Properties p = new Properties();
                p.load(in);
                for (String name : p.stringPropertyNames()) {
                    if ("__admins".equals(name)) continue;
                    users.put(name, p.getProperty(name));
                }
                String a = p.getProperty("__admins");
                if (a != null && !a.isBlank()) {
                    String[] parts = a.split(",");
                    for (String s : parts) {
                        if (!s.isBlank()) admins.add(s.trim());
                    }
                }
            } catch (IOException ignored) { }
        }
    }
}
