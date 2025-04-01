package dev.uday.Headless;

import dev.uday.NET.Server;
import dev.uday.NET.ServerBroadcasting;

public class HeadlessServer {

    public static void startHeadlessServer(String[] args) {
        System.out.println("Starting headless server...");
        // Check if arguments are provided
        if (args.length > 0) {
            try {
                // Parse the port number from the first argument
                Server.PORT = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number. Using default port: " + Server.PORT);
            }
            // Check if a server name is provided
            if (args.length > 1) {
                Server.serverName = args[1];
            } else {
                Server.serverName = "ProjectEXO Server"; // Default server name
            }
        } else {
            // Use default values if no arguments are provided
            Server.PORT = 2005; // Default port
            Server.serverName = "ProjectEXO Server"; // Default server name
        }
        // Initialize the server
        // Start server in a background thread AFTER configuration
        Thread serverThread = new Thread(() -> {
            Server server = new Server(Server.PORT);
            server.start();
        });
        serverThread.start();

        // Start broadcasting thread AFTER configuration
        Thread broadcastingThread = new Thread(new ServerBroadcasting());
        broadcastingThread.setDaemon(true);
        broadcastingThread.start();

        System.out.println("Headless server started on port " + Server.PORT + " with name: " + Server.serverName);
        Commands commandsRunner = new Commands();
        Thread.startVirtualThread(commandsRunner);
    }
}
