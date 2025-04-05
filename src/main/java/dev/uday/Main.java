package dev.uday;

import dev.uday.AI.Ollama;
import dev.uday.GUI.ServerApp;
import dev.uday.Headless.HeadlessServer;

import java.awt.*;
import java.util.Objects;

public class Main {
    public static void main(String[] args) {

        System.out.println("Starting ProjectEXO Server...");

        // Initialize Ollama
        Ollama.init();

        if (args.length > 0) {
            if (Objects.equals(args[0], "headless")) {
                String[] args2 = new String[args.length - 1];
                System.arraycopy(args, 1, args2, 0, args.length - 1);
                HeadlessServer.startHeadlessServer(args2);
            } else if (Objects.equals(args[0], "gui")) {
                // Launch the UI (will show splash screen then config dialog)
                EventQueue.invokeLater(ServerApp::startServerApp);
            } else {
                System.out.println("Invalid argument. Use 'headless' or 'gui'.");
            }
        } else {
            // Launch the UI (will show splash screen then config dialog)
            ServerApp.startServerApp();
        }
    }
}