package dev.uday;

import dev.uday.AI.Ollama;
import dev.uday.GUI.ServerApp;
import dev.uday.NET.Server;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting server...");

        Ollama.init();

        // Start server in a background thread
        Thread serverThread = new Thread(() -> {
            Server server = new Server();
            server.start();
        });
        serverThread.setDaemon(true);
        serverThread.start();

        // Launch the UI (this will block until UI closes)
        ServerApp.startServerApp();
    }
}