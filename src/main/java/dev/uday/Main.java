package dev.uday;

import dev.uday.NET.Server;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting server...");
        Server server = new Server();
        server.start();
    }
}