package dev.uday;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Clients {
    public static ConcurrentHashMap<UUID, Client> currentClients = new ConcurrentHashMap<>();
    public static HashMap<String, String> registeredClients = new HashMap<>(
            Map.of("uday", "Uday", "admin", "admin","test","test")
    );
}
