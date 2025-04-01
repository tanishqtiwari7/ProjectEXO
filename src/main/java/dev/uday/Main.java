package dev.uday;

import dev.uday.AI.Ollama;
import dev.uday.GUI.ServerApp;
import dev.uday.Headless.HeadlessServer;

import java.awt.*;

public class Main {
    public static void main(String[] args) {

        System.out.println("Starting ProjectEXO Server...");

        // Initialize Ollama
        Ollama.init();

        // Check if machine is headless
        boolean headless = GraphicsEnvironment.isHeadless();
        String headlessProperty = System.getProperty("java.awt.headless");

        if (headless || "true".equalsIgnoreCase(headlessProperty)) {
            HeadlessServer.startHeadlessServer(args);
        } else {
            // Launch the UI (will show splash screen then config dialog)
            ServerApp.startServerApp();
        }
    }
}