package dev.uday;

import dev.uday.AI.Ollama;
import dev.uday.GUI.ServerApp;
import dev.uday.NET.Server;
import dev.uday.NET.ServerBroadcasting;

public class Main {
    public static void main(String[] args) {
        // Parse command-line arguments for port if provided
        if (!(args.length == 0)) {
            if (args[0].equals("-p")) {
                if (args.length == 2) {
                    try {
                        Server.PORT = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid port number. Will use configuration dialog.");
                    }
                }
            }
        }

        System.out.println("Starting ProjectEXO Server...");

        // Initialize Ollama
        Ollama.init();

        // Launch the UI (will show splash screen then config dialog)
        ServerApp.startServerApp();
    }
}